package com.rookedsysc.order.application

import com.rookedsysc.common.lock.DistributedLock
import com.rookedsysc.order.application.dto.OrderDto
import com.rookedsysc.order.application.dto.PlaceOrderCommand
import com.rookedsysc.order.entity.CompensationRegistry
import com.rookedsysc.order.entity.Order
import com.rookedsysc.order.infrastructure.out.CompensationRegistryRepository
import com.rookedsysc.order.infrastructure.out.point.PointApiClient
import com.rookedsysc.order.infrastructure.out.point.dto.PointUseApiRequest
import com.rookedsysc.order.infrastructure.out.point.dto.PointUseCancelRequest
import com.rookedsysc.order.infrastructure.out.product.ProductApiClient
import com.rookedsysc.order.infrastructure.out.product.dto.ProductBuyApiRequest
import com.rookedsysc.order.infrastructure.out.product.dto.ProductBuyApiResponse
import com.rookedsysc.order.infrastructure.out.product.dto.ProductBuyCancelRequest
import com.rookedsysc.order.infrastructure.out.product.dto.ProductBuyCancelResponse
import org.springframework.stereotype.Component

@Component
class OrderCoordinator(
    private val orderService: OrderService,
    private val compensationRegistryRepository: CompensationRegistryRepository,
    private val productApiClient: ProductApiClient,
    private val pointApiClient: PointApiClient
) {


    @DistributedLock(
        key = "order:{command.orderId}", fairLock = true, withTransaction = false
    )
    fun placeOrder(command: PlaceOrderCommand) {
        orderService.request(command.orderId)
        val orderDto: OrderDto = orderService.getOrder(command.orderId)

        try {
            val productBuyApiRequest: ProductBuyApiRequest = ProductBuyApiRequest(
                requestId = command.orderId.toString(), productInfos = orderDto.orderItems.map {
                    ProductBuyApiRequest.ProductInfo(
                        it.productId, it.quantity
                    )
                })

            val buyApiResponse: ProductBuyApiResponse = productApiClient.buy(productBuyApiRequest)

            val pointUseApiRequest: PointUseApiRequest = PointUseApiRequest(
                requestId = command.orderId.toString(), userId = orderDto.userId, amount = buyApiResponse.totalPrice
            )

            pointApiClient.use(pointUseApiRequest)

            orderService.complete(command.orderId)
        } catch (e: Exception) {
            rollback(command.orderId)

            throw e
        }
    }

    fun rollback(orderId: Long) {
        try {
            val productBuyCancelApiRequest: ProductBuyCancelRequest = ProductBuyCancelRequest(orderId.toString())

            val productBuyCancelApiResponse: ProductBuyCancelResponse =
                productApiClient.cancel(productBuyCancelApiRequest)

            if (productBuyCancelApiResponse.totalPrice > 0) {
                val pointUseCancelApiRequest: PointUseCancelRequest = PointUseCancelRequest(orderId.toString())

                pointApiClient.cancel(pointUseCancelApiRequest)
            }

            orderService.fail(orderId)
        } catch (e: java.lang.Exception) {
            compensationRegistryRepository.save(
                CompensationRegistry(orderId = orderId)
            )
            throw e
        }
    }
}
