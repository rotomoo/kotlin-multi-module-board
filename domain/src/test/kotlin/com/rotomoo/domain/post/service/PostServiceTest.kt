package com.rotomoo.domain.post.service

import com.rotomoo.domain.category.entity.Category
import com.rotomoo.domain.category.repository.CategoryRepository
import com.rotomoo.domain.post.entity.Post
import com.rotomoo.domain.post.entity.PostStatus
import com.rotomoo.domain.post.repository.PostRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("PostService 단위 테스트")
class PostServiceTest {

    @MockK
    private lateinit var postRepository: PostRepository

    @MockK
    private lateinit var categoryRepository: CategoryRepository

    @InjectMockKs
    private lateinit var postService: PostServiceImpl

    @Nested
    @DisplayName("create")
    inner class Create {

        @Test
        @DisplayName("게시글을 생성한다")
        fun `should create post`() {
            // given
            val categoryId = 1L
            val author = "테스터"
            val title = "테스트 제목"
            val content = "테스트 내용"
            val parent = createTestCategory(99L, "부모")
            val category = createTestCategory(categoryId, parent = parent)
            val savedPost = createTestPost(1L, category, author, title, content)

            every { categoryRepository.findById(categoryId) } returns Optional.of(category)
            every { categoryRepository.existsByParentId(categoryId) } returns false
            every { postRepository.save(any()) } returns savedPost

            // when
            val result = postService.create(categoryId, author, title, content, PostStatus.DRAFT)

            // then
            assertEquals(title, result.title)
            assertEquals(content, result.content)
            assertEquals(category, result.category)
            assertEquals(author, result.author)
            verify(exactly = 1) { postRepository.save(any()) }
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 생성시 예외를 던진다")
        fun `should throw exception when category not found`() {
            // given
            val categoryId = 999L

            every { categoryRepository.findById(categoryId) } returns Optional.empty()

            // when & then
            val exception = assertThrows<NoSuchElementException> {
                postService.create(categoryId, "테스터", "제목", "내용", PostStatus.DRAFT)
            }
            assertEquals("Category not found: $categoryId", exception.message)
        }

        @Test
        @DisplayName("자식이 있는 카테고리에 게시글 생성시 예외를 던진다")
        fun `should throw exception when creating post in category with children`() {
            // given
            val categoryId = 1L
            val category = createTestCategory(categoryId)

            every { categoryRepository.findById(categoryId) } returns Optional.of(category)
            every { categoryRepository.existsByParentId(categoryId) } returns true

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                postService.create(categoryId, "테스터", "제목", "내용", PostStatus.DRAFT)
            }
            assertEquals("Cannot create post in a category that has children. Please select a leaf category.", exception.message)
        }
    }

    @Nested
    @DisplayName("getById")
    inner class GetById {

        @Test
        @DisplayName("ID로 게시글을 조회한다")
        fun `should return post by id`() {
            // given
            val postId = 1L
            val post = createTestPost(postId)

            every { postRepository.findByIdWithCategory(postId) } returns post

            // when
            val result = postService.getById(postId)

            // then
            assertEquals(postId, result.id)
            verify(exactly = 1) { postRepository.findByIdWithCategory(postId) }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회시 예외를 던진다")
        fun `should throw exception when post not found`() {
            // given
            val postId = 999L

            every { postRepository.findByIdWithCategory(postId) } returns null

            // when & then
            val exception = assertThrows<NoSuchElementException> {
                postService.getById(postId)
            }
            assertEquals("Post not found: $postId", exception.message)
        }
    }

    @Nested
    @DisplayName("getByCategoryId")
    inner class GetByCategoryId {

        @Test
        @DisplayName("카테고리 ID로 게시글 목록을 조회한다")
        fun `should return posts by category id`() {
            // given
            val categoryId = 1L
            val posts = listOf(
                createTestPost(1L),
                createTestPost(2L)
            )

            every { postRepository.findByCategoryId(categoryId) } returns posts

            // when
            val result = postService.getByCategoryId(categoryId)

            // then
            assertEquals(2, result.size)
            verify(exactly = 1) { postRepository.findByCategoryId(categoryId) }
        }

        @Test
        @DisplayName("게시글이 없으면 빈 리스트를 반환한다")
        fun `should return empty list when no posts`() {
            // given
            val categoryId = 1L

            every { postRepository.findByCategoryId(categoryId) } returns emptyList()

            // when
            val result = postService.getByCategoryId(categoryId)

            // then
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("getPublishedByCategoryId")
    inner class GetPublishedByCategoryId {

        @Test
        @DisplayName("카테고리 ID로 게시된 게시글 목록을 조회한다")
        fun `should return published posts by category id`() {
            // given
            val categoryId = 1L
            val posts = listOf(createTestPost(1L, status = PostStatus.PUBLISHED))

            every { postRepository.findPublishedByCategoryId(categoryId) } returns posts

            // when
            val result = postService.getPublishedByCategoryId(categoryId)

            // then
            assertEquals(1, result.size)
            verify(exactly = 1) { postRepository.findPublishedByCategoryId(categoryId) }
        }
    }

    @Nested
    @DisplayName("getByAuthor")
    inner class GetByAuthor {

        @Test
        @DisplayName("작성자로 게시글 목록을 조회한다")
        fun `should return posts by author`() {
            // given
            val author = "테스터"
            val posts = listOf(
                createTestPost(1L),
                createTestPost(2L)
            )

            every { postRepository.findByAuthor(author) } returns posts

            // when
            val result = postService.getByAuthor(author)

            // then
            assertEquals(2, result.size)
            verify(exactly = 1) { postRepository.findByAuthor(author) }
        }
    }

    @Nested
    @DisplayName("update")
    inner class Update {

        @Test
        @DisplayName("게시글을 수정한다")
        fun `should update post`() {
            // given
            val postId = 1L
            val post = createTestPost(postId, title = "원래 제목", content = "원래 내용")

            every { postRepository.findByIdWithCategory(postId) } returns post

            // when
            val result = postService.update(postId, "수정된 제목", "수정된 내용")

            // then
            assertEquals("수정된 제목", result.title)
            assertEquals("수정된 내용", result.content)
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정시 예외를 던진다")
        fun `should throw exception when updating non-existent post`() {
            // given
            val postId = 999L

            every { postRepository.findByIdWithCategory(postId) } returns null

            // when & then
            assertThrows<NoSuchElementException> {
                postService.update(postId, "제목", "내용")
            }
        }
    }

    @Nested
    @DisplayName("updateStatus")
    inner class UpdateStatus {

        @Test
        @DisplayName("게시글 상태를 PUBLISHED로 변경한다")
        fun `should update status to PUBLISHED`() {
            // given
            val postId = 1L
            val post = createTestPost(postId, status = PostStatus.DRAFT)

            every { postRepository.findByIdWithCategory(postId) } returns post

            // when
            val result = postService.updateStatus(postId, PostStatus.PUBLISHED)

            // then
            assertEquals(PostStatus.PUBLISHED, result.status)
        }

        @Test
        @DisplayName("게시글 상태를 DRAFT로 변경한다")
        fun `should update status to DRAFT`() {
            // given
            val postId = 1L
            val post = createTestPost(postId, status = PostStatus.PUBLISHED)

            every { postRepository.findByIdWithCategory(postId) } returns post

            // when
            val result = postService.updateStatus(postId, PostStatus.DRAFT)

            // then
            assertEquals(PostStatus.DRAFT, result.status)
        }

        @Test
        @DisplayName("게시글 상태를 DELETED로 변경한다")
        fun `should update status to DELETED`() {
            // given
            val postId = 1L
            val post = createTestPost(postId, status = PostStatus.DRAFT)

            every { postRepository.findByIdWithCategory(postId) } returns post

            // when
            val result = postService.updateStatus(postId, PostStatus.DELETED)

            // then
            assertEquals(PostStatus.DELETED, result.status)
        }
    }

    @Nested
    @DisplayName("publish")
    inner class Publish {

        @Test
        @DisplayName("게시글을 발행한다")
        fun `should publish post`() {
            // given
            val postId = 1L
            val post = createTestPost(postId, status = PostStatus.DRAFT)

            every { postRepository.findByIdWithCategory(postId) } returns post

            // when
            val result = postService.publish(postId)

            // then
            assertEquals(PostStatus.PUBLISHED, result.status)
        }
    }

    @Nested
    @DisplayName("delete")
    inner class Delete {

        @Test
        @DisplayName("게시글을 삭제한다 (Soft Delete)")
        fun `should soft delete post`() {
            // given
            val postId = 1L
            val post = createTestPost(postId, status = PostStatus.DRAFT)

            every { postRepository.findById(postId) } returns Optional.of(post)

            // when
            postService.delete(postId)

            // then
            assertEquals(PostStatus.DELETED, post.status)
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제시 예외를 던진다")
        fun `should throw exception when deleting non-existent post`() {
            // given
            val postId = 999L

            every { postRepository.findById(postId) } returns Optional.empty()

            // when & then
            assertThrows<NoSuchElementException> {
                postService.delete(postId)
            }
        }
    }

    @Nested
    @DisplayName("incrementViewCount")
    inner class IncrementViewCount {

        @Test
        @DisplayName("조회수를 증가시킨다")
        fun `should increment view count`() {
            // given
            val postId = 1L
            val post = createTestPost(postId, viewCount = 10)

            // when
            postService.incrementViewCount(post)

            // then
            assertEquals(11, post.viewCount)
        }
    }

    private fun createTestCategory(
        id: Long,
        name: String = "테스트 카테고리",
        parent: Category? = null
    ): Category {
        return Category(
            id = id,
            name = name,
            parent = parent,
            description = null,
            sortOrder = 0
        )
    }

    private fun createTestPost(
        id: Long,
        category: Category = createTestCategory(1L, parent = createTestCategory(99L, "부모")),
        author: String = "테스터",
        title: String = "테스트 제목",
        content: String = "테스트 내용",
        viewCount: Int = 0,
        status: PostStatus = PostStatus.DRAFT
    ): Post {
        return Post(
            id = id,
            category = category,
            author = author,
            title = title,
            content = content,
            viewCount = viewCount,
            status = status
        )
    }
}
