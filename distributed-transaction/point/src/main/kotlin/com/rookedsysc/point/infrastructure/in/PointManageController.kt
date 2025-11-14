package com.rookedsysc.point.infrastructure.`in`

import com.rookedsysc.common.model.PageResponse
import com.rookedsysc.point.application.PointCreateService
import com.rookedsysc.point.application.PointListService
import com.rookedsysc.point.application.dto.PointListResult
import com.rookedsysc.point.infrastructure.`in`.dto.PointCreateRequest
import com.rookedsysc.point.infrastructure.`in`.dto.PointListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@Tag(name = "포인트 관리")
@RestController
@RequestMapping("/points")
class PointController(
    private val pointListService: PointListService,
    private val pointCreateService: PointCreateService
) {
    @Operation(summary = "포인트 목록 조회")
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): PageResponse<PointListResponse> {
        val pageable: Pageable = Pageable.ofSize(size).withPage(page)
        val pointResults: Page<PointListResult> = pointListService.list(pageable)
        val pointResponses: Page<PointListResponse> = pointResults.map { pointResult ->
            PointListResponse.from(pointResult)
        }
        return PageResponse.from(pointResponses)
    }

    @Operation(summary = "포인트 생성")
    @PostMapping
    fun create(@RequestBody request: PointCreateRequest): PointListResponse {
        val pointResult = pointCreateService.create(request.toCommand())
        return PointListResponse.from(pointResult)
    }
}
