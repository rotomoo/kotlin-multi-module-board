package com.rotomoo.api.v1.post.facade

import com.rotomoo.api.v1.post.dto.*
import com.rotomoo.domain.post.entity.PostStatus
import com.rotomoo.domain.post.service.PostService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PostFacade(
    private val postService: PostService
) {
    fun create(request: PostCreateRequest): PostResponse {
        val post = postService.create(
            categoryId = request.categoryId,
            author = request.author,
            title = request.title,
            content = request.content,
            status = request.status
        )
        return PostResponse.from(post)
    }

    @Transactional
    fun getById(id: Long): PostResponse {
        val post = postService.getById(id)
        postService.incrementViewCount(post)
        return PostResponse.from(post)
    }

    fun findAll(pageable: Pageable): Page<PostListResponse> {
        return postService.findAllPublished(pageable).map { post ->
            PostListResponse.from(post)
        }
    }

    fun findByCategoryId(categoryId: Long, pageable: Pageable): Page<PostListResponse> {
        return postService.findPublishedByCategoryId(categoryId, pageable).map { post ->
            PostListResponse.from(post)
        }
    }

    fun search(keyword: String, searchType: String, pageable: Pageable): Page<PostListResponse> {
        return postService.search(keyword, searchType, pageable).map { post ->
            PostListResponse.from(post)
        }
    }

    fun getByAuthor(author: String): List<PostListResponse> {
        return postService.getByAuthor(author).map { post ->
            PostListResponse.from(post)
        }
    }

    fun update(id: Long, request: PostUpdateRequest): PostResponse {
        val post = postService.update(
            id = id,
            title = request.title,
            content = request.content
        )
        return PostResponse.from(post)
    }

    fun updateStatus(id: Long, status: PostStatus): PostResponse {
        val post = postService.updateStatus(id, status)
        return PostResponse.from(post)
    }

    fun publish(id: Long): PostResponse {
        val post = postService.publish(id)
        return PostResponse.from(post)
    }

    fun delete(id: Long) {
        postService.delete(id)
    }
}
