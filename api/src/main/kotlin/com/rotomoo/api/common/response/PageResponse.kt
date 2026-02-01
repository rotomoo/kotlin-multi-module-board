package com.rotomoo.api.common.response

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val content: List<T>,
    val page: PageInfo
) {
    companion object {
        fun <T : Any> from(page: Page<T>): PageResponse<T> = PageResponse(
            content = page.content,
            page = PageInfo.from(page)
        )

        fun <T : Any, R> from(page: Page<T>, transform: (T) -> R): PageResponse<R> = PageResponse(
            content = page.content.map(transform),
            page = PageInfo.from(page)
        )
    }
}

data class PageInfo(
    val number: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    companion object {
        fun <T : Any> from(page: Page<T>): PageInfo = PageInfo(
            number = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages
        )
    }
}
