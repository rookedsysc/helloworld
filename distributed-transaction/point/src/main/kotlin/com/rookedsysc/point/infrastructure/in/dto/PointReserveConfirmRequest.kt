package com.rookedsysc.point.infrastructure.`in`.dto

import com.rookedsysc.point.application.dto.PointReserveConfirmCommand

data class PointReserveConfirmRequest(
    val requestId: String
) {
    fun toCommand(): PointReserveConfirmCommand {
        return PointReserveConfirmCommand(
            requestId = requestId
        )
    }
}
