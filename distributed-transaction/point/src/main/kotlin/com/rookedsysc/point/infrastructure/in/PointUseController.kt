package com.rookedsysc.point.infrastructure.`in`

import com.rookedsysc.point.application.PointUseService
import com.rookedsysc.point.infrastructure.`in`.dto.PointUseCancelRequest
import com.rookedsysc.point.infrastructure.`in`.dto.PointUseRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "포인트 사용")
@RestController
@RequestMapping("/points")
class PointUseController(
    private val pointUseService: PointUseService
) {

    @Operation(summary = "포인트 사용")
    @PostMapping("use")
    fun use(@RequestBody request: PointUseRequest) {
        pointUseService.use(request.toCommand())
    }

    @Operation(summary = "포인트 사용취소")
    @PostMapping("cancel")
    fun cancel(@RequestBody request: PointUseCancelRequest) {
        pointUseService.cancel(request.toCommand())
    }
}
