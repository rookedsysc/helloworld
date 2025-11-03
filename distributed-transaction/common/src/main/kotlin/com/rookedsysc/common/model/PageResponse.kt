package com.rookedsysc.common.model

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val page: Int,
    val size: Int,
    val items: List<T>,
) {
    companion object {
        fun <T> from(page: Page<T>): PageResponse<T> {
            return PageResponse(
                page = page.number,
                size = page.size,
                items = page.content
            )
        }
    }
}
