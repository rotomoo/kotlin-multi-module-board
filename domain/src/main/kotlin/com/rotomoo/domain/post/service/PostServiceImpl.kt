package com.rotomoo.domain.post.service

import com.rotomoo.domain.category.repository.CategoryRepository
import com.rotomoo.domain.post.entity.Post
import com.rotomoo.domain.post.entity.PostStatus
import com.rotomoo.domain.post.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val categoryRepository: CategoryRepository
) : PostService {

    @Transactional
    override fun create(categoryId: Long, author: String, title: String, content: String, status: PostStatus): Post {
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { NoSuchElementException("Category not found: $categoryId") }

        // leaf 카테고리(자식이 없는 것)에만 게시글 작성 가능
        if (categoryRepository.existsByParentId(categoryId)) {
            throw IllegalArgumentException("Cannot create post in a category that has children. Please select a leaf category.")
        }

        val post = Post(
            category = category,
            author = author,
            title = title,
            content = content,
            status = status
        )
        return postRepository.save(post)
    }

    override fun getById(id: Long): Post {
        return postRepository.findByIdWithCategory(id)
            ?: throw NoSuchElementException("Post not found: $id")
    }

    override fun getByCategoryId(categoryId: Long): List<Post> {
        return postRepository.findByCategoryId(categoryId)
    }

    override fun getPublishedByCategoryId(categoryId: Long): List<Post> {
        return postRepository.findPublishedByCategoryId(categoryId)
    }

    override fun getByAuthor(author: String): List<Post> {
        return postRepository.findByAuthor(author)
    }

    @Transactional
    override fun update(id: Long, title: String, content: String): Post {
        val post = getById(id)
        post.update(title, content)
        return post
    }

    @Transactional
    override fun updateStatus(id: Long, status: PostStatus): Post {
        val post = getById(id)
        when (status) {
            PostStatus.PUBLISHED -> post.publish()
            PostStatus.DRAFT -> post.toDraft()
            PostStatus.DELETED -> post.delete()
        }
        return post
    }

    @Transactional
    override fun publish(id: Long): Post {
        val post = getById(id)
        post.publish()
        return post
    }

    @Transactional
    override fun delete(id: Long) {
        val post = postRepository.findById(id)
            .orElseThrow { NoSuchElementException("Post not found: $id") }
        post.delete()
    }

    @Transactional
    override fun incrementViewCount(post: Post) {
        post.incrementViewCount()
    }

    override fun findAllPublished(pageable: Pageable): Page<Post> {
        return postRepository.findAllPublished(pageable)
    }

    override fun findPublishedByCategoryId(categoryId: Long, pageable: Pageable): Page<Post> {
        return postRepository.findPublishedByCategoryId(categoryId, pageable)
    }

    override fun search(keyword: String, searchType: String, pageable: Pageable): Page<Post> {
        return when (searchType.lowercase()) {
            "title" -> postRepository.searchByTitle(keyword, pageable)
            "content" -> postRepository.searchByContent(keyword, pageable)
            "author" -> postRepository.searchByAuthor(keyword, pageable)
            else -> postRepository.searchByKeyword(keyword, pageable) // "all" or default
        }
    }
}
