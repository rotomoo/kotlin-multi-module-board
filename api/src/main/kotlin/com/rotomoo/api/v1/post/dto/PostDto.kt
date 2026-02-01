package com.rotomoo.api.v1.post.dto

import com.rotomoo.domain.post.entity.Post
import com.rotomoo.domain.post.entity.PostStatus
import java.time.LocalDateTime

data class PostCreateRequest(
    val author: String,
    val title: String,
    val content: String,
    val categoryId: Long,
    val status: PostStatus = PostStatus.PUBLISHED
)

data class PostUpdateRequest(
    val title: String,
    val content: String
)

data class PostStatusUpdateRequest(
    val status: PostStatus
)

// 상세 조회 응답
data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val category: CategoryInfo,
    val author: String,
    val viewCount: Int,
    val status: PostStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(post: Post) = PostResponse(
            id = post.id,
            title = post.title,
            content = post.content,
            category = CategoryInfo(post.category.id, post.category.name),
            author = post.author,
            viewCount = post.viewCount,
            status = post.status,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt
        )
    }
}

// 목록 조회 응답
data class PostListResponse(
    val id: Long,
    val title: String,
    val category: CategoryInfo,
    val author: String,
    val viewCount: Int,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(post: Post) = PostListResponse(
            id = post.id,
            title = post.title,
            category = CategoryInfo(post.category.id, post.category.name),
            author = post.author,
            viewCount = post.viewCount,
            createdAt = post.createdAt
        )
    }
}

data class CategoryInfo(
    val id: Long,
    val name: String
)
