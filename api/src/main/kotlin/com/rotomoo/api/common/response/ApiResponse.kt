package com.rotomoo.api.common.response

import org.springframework.data.domain.Page

data class ApiResponse<T>(
    val success: Boolean,   // 성공 여부
    val data: T?,           // 실제 데이터
    val message: String?    // 에러 메시지 (실패 시)
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message)

        fun <T> success(message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, data = null, message = message)

        fun <T> error(message: String): ApiResponse<T> =
            ApiResponse(success = false, data = null, message = message)
    }
}

data class PagedApiResponse<T>(
    val success: Boolean,
    val data: List<T>,
    val pagination: Pagination,
    val message: String?
) {
    companion object {
        fun <T> success(data: List<T>, pagination: Pagination, message: String? = null): PagedApiResponse<T> =
            PagedApiResponse(success = true, data = data, pagination = pagination, message = message)

        fun <T : Any, R : Any> success(page: Page<T>, transform: (T) -> R): PagedApiResponse<R> =
            PagedApiResponse(
                success = true,
                data = page.content.map(transform),
                pagination = Pagination.of(page.number, page.size, page.totalElements),
                message = null
            )

        fun <T> error(message: String): PagedApiResponse<T> =
            PagedApiResponse(success = false, data = emptyList(), pagination = Pagination.empty(), message = message)
    }
}

data class Pagination(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    companion object {
        fun of(page: Int, size: Int, totalElements: Long): Pagination {
            val totalPages = if (totalElements == 0L) 0 else ((totalElements - 1) / size + 1).toInt()
            return Pagination(page = page, size = size, totalElements = totalElements, totalPages = totalPages)
        }

        fun empty(): Pagination = Pagination(page = 0, size = 0, totalElements = 0, totalPages = 0)
    }
}
