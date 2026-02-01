package com.rotomoo.domain.post.repository

import com.rotomoo.domain.post.entity.Post
import com.rotomoo.domain.post.entity.PostStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostRepository : JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.id = :id")
    fun findByIdWithCategory(id: Long): Post?

    fun findByCategoryId(categoryId: Long): List<Post>

    fun findByCategoryIdAndStatus(categoryId: Long, status: PostStatus): List<Post>

    fun findByAuthor(author: String): List<Post>

    fun findByAuthorAndStatus(author: String, status: PostStatus): List<Post>

    // 페이징 - 전체 게시글 (PUBLISHED만)
    @Query(
        value = "SELECT p FROM Post p JOIN FETCH p.category WHERE p.status = 'PUBLISHED'",
        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status = 'PUBLISHED'"
    )
    fun findAllPublished(pageable: Pageable): Page<Post>

    // 페이징 - 카테고리별 게시글 (PUBLISHED만)
    @Query(
        value = "SELECT p FROM Post p JOIN FETCH p.category WHERE p.category.id = :categoryId AND p.status = 'PUBLISHED'",
        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.category.id = :categoryId AND p.status = 'PUBLISHED'"
    )
    fun findPublishedByCategoryId(categoryId: Long, pageable: Pageable): Page<Post>

    // 페이징 - 검색 (제목+내용)
    @Query(
        value = "SELECT p FROM Post p JOIN FETCH p.category WHERE p.status = 'PUBLISHED' AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)",
        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status = 'PUBLISHED' AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)"
    )
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<Post>

    // 페이징 - 제목 검색
    @Query(
        value = "SELECT p FROM Post p JOIN FETCH p.category WHERE p.status = 'PUBLISHED' AND p.title LIKE %:keyword%",
        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status = 'PUBLISHED' AND p.title LIKE %:keyword%"
    )
    fun searchByTitle(keyword: String, pageable: Pageable): Page<Post>

    // 페이징 - 내용 검색
    @Query(
        value = "SELECT p FROM Post p JOIN FETCH p.category WHERE p.status = 'PUBLISHED' AND p.content LIKE %:keyword%",
        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status = 'PUBLISHED' AND p.content LIKE %:keyword%"
    )
    fun searchByContent(keyword: String, pageable: Pageable): Page<Post>

    // 페이징 - 작성자 검색
    @Query(
        value = "SELECT p FROM Post p JOIN FETCH p.category WHERE p.status = 'PUBLISHED' AND p.author LIKE %:keyword%",
        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status = 'PUBLISHED' AND p.author LIKE %:keyword%"
    )
    fun searchByAuthor(keyword: String, pageable: Pageable): Page<Post>

    // 리스트 (페이징 없이)
    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.category.id = :categoryId AND p.status = 'PUBLISHED' ORDER BY p.createdAt DESC")
    fun findPublishedByCategoryId(categoryId: Long): List<Post>

    fun countByCategoryId(categoryId: Long): Long

    fun countByCategoryIdAndStatus(categoryId: Long, status: PostStatus): Long

    fun existsByCategoryId(categoryId: Long): Boolean
}
