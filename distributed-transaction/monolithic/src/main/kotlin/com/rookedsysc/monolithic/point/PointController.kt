package com.rookedsysc.monolithic.point

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PointController(
    val pointService: PointService
) {
    @GetMapping("/point")
    fun getPoint(): PointResponse {
        return pointService.getPoint()
    }
}
