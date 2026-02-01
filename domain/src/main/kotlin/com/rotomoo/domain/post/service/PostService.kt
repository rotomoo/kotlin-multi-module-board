package com.rotomoo.domain.post.service

import com.rotomoo.domain.post.entity.Post
import com.rotomoo.domain.post.entity.PostStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PostService {
    fun create(categoryId: Long, author: String, title: String, content: String, status: PostStatus = PostStatus.DRAFT): Post
    fun getById(id: Long): Post
    fun getByCategoryId(categoryId: Long): List<Post>
    fun getPublishedByCategoryId(categoryId: Long): List<Post>
    fun getByAuthor(author: String): List<Post>
    fun update(id: Long, title: String, content: String): Post
    fun updateStatus(id: Long, status: PostStatus): Post
    fun publish(id: Long): Post
    fun delete(id: Long)
    fun incrementViewCount(post: Post)

    // 페이징
    fun findAllPublished(pageable: Pageable): Page<Post>
    fun findPublishedByCategoryId(categoryId: Long, pageable: Pageable): Page<Post>

    // 검색
    fun search(keyword: String, searchType: String, pageable: Pageable): Page<Post>
}
