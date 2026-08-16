package com.rookedsysc.order.application

import com.rookedsysc.common.lock.DistributedLockWithTransaction
import com.rookedsysc.order.application.dto.OrderDto
import com.rookedsysc.order.application.dto.PlaceOrderCommand
import com.rookedsysc.order.infrastructure.out.point.PointApiClient
import com.rookedsysc.order.infrastructure.out.point.dto.PointReserveApiRequest
import com.rookedsysc.order.infrastructure.out.point.dto.PointReserveCancelRequest
import com.rookedsysc.order.infrastructure.out.point.dto.PointReserveConfirmRequest
import com.rookedsysc.order.infrastructure.out.product.ProductApiClient
import com.rookedsysc.order.infrastructure.out.product.dto.ProductReserveApiRequest
import com.rookedsysc.order.infrastructure.out.product.dto.ProductReserveApiResponse
import com.rookedsysc.order.infrastructure.out.product.dto.ProductReserveCancelRequest
import com.rookedsysc.order.infrastructure.out.product.dto.ProductReserveConfirmRequest
import org.springframework.stereotype.Component

@Component
class OrderCoordinator(
    private val orderService: OrderService,
    private val productApiClient: ProductApiClient,
    private val pointApiClient: PointApiClient
) {

    @DistributedLockWithTransaction(
        key = "order:{command.orderId}",
        fairLock = true
    )
    fun placeOrder(command: PlaceOrderCommand) {
        reserve(command = command)
        confirm(command = command)
    }

    private fun reserve(command: PlaceOrderCommand) {
        val orderId: Long = command.orderId
        val requestId: String = orderId.toString()
        orderService.reserve(orderId)

        try {
            val orderInfo : OrderDto = orderService.getOrder(orderId)

            val productReserveApiRequest = ProductReserveApiRequest(
                requestId = requestId,
                items = orderInfo.orderItems.map {
                    ProductReserveApiRequest.ReserveItem(
                        productId = it.productId,
                        reserveQuantity = it.quantity
                    )
                }
            )

            val productReserveApiResponse: ProductReserveApiResponse = productApiClient.reserveProduct(request = productReserveApiRequest)

            val pointReserveApiRequest = PointReserveApiRequest(
                requestId = requestId,
                userId = orderInfo.userId,
                amount = productReserveApiResponse.totalPrice
            )

            pointApiClient.reservePoint(request = pointReserveApiRequest)
        } catch (e: Exception) {
            // 주문 취소
            orderService.cancel(orderId)

            // 상품 취소
            val productReserveCancelRequest =ProductReserveCancelRequest(
                requestId = requestId
            )
            productApiClient.reserveCancel(request = productReserveCancelRequest)

            // 포인트 취소
            val pointReserveCancelRequest = PointReserveCancelRequest(requestId = requestId)
            pointApiClient.reserveCancel(request = pointReserveCancelRequest)
        }
    }

    private fun confirm(command: PlaceOrderCommand) {
        val orderId: Long = command.orderId
        val requestId: String = command.orderId.toString()

        try {
            val productReserveConfirmRequest = ProductReserveConfirmRequest(
                requestId = requestId
            )
            productApiClient.reserveConfirm(productReserveConfirmRequest)

            val pointReserveConfirmRequest = PointReserveConfirmRequest(
                requestId = requestId
            )
            pointApiClient.reserveConfirm(pointReserveConfirmRequest)

            orderService.confirm(orderId)
        } catch(e: Exception) {
            // Confirm에 실패하면 데이터베이스 오류라던지 인프라 레벨의 오류일 확률이 높음
            // 수동으로 처리할 수 있도록 주문 상태를 Pending으로 변경
            orderService.pending(orderId)
            throw e
        }
    }
}
