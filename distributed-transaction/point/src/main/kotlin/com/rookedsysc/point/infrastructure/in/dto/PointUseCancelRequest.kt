package com.rookedsysc.point.infrastructure.`in`.dto

import com.rookedsysc.point.application.dto.PointUseCancelCommand

data class PointUseCancelRequest(
    val requestId: String,
) {
    fun toCommand(): PointUseCancelCommand {
        return PointUseCancelCommand(requestId = requestId)
    }
}
