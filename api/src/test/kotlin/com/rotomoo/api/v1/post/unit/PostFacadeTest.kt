package com.rotomoo.api.v1.post.unit

import com.rotomoo.api.v1.post.dto.PostCreateRequest
import com.rotomoo.api.v1.post.dto.PostUpdateRequest
import com.rotomoo.api.v1.post.facade.PostFacade
import com.rotomoo.domain.category.entity.Category
import com.rotomoo.domain.post.entity.Post
import com.rotomoo.domain.post.entity.PostStatus
import com.rotomoo.domain.post.service.PostService
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("PostFacade 단위 테스트")
class PostFacadeTest {

    @MockK
    private lateinit var postService: PostService

    @InjectMockKs
    private lateinit var postFacade: PostFacade

    @Nested
    @DisplayName("create")
    inner class Create {

        @Test
        @DisplayName("게시글을 생성하고 PostResponse를 반환한다")
        fun `should create post and return response`() {
            // given
            val categoryId = 1L
            val author = "테스터"
            val request = PostCreateRequest(
                author = author,
                title = "테스트 제목",
                content = "테스트 내용",
                categoryId = categoryId
            )
            val savedPost = createTestPost(1L, categoryId, author)

            every { postService.create(categoryId, author, request.title, request.content, any()) } returns savedPost

            // when
            val response = postFacade.create(request)

            // then
            assertEquals(1L, response.id)
            assertEquals(categoryId, response.category.id)
            assertEquals("테스트 제목", response.title)
            assertEquals("테스트 내용", response.content)
            verify(exactly = 1) { postService.create(categoryId, author, request.title, request.content, any()) }
        }
    }

    @Nested
    @DisplayName("getById")
    inner class GetById {

        @Test
        @DisplayName("ID로 게시글을 조회하고 PostResponse를 반환한다")
        fun `should return post response when post exists`() {
            // given
            val postId = 1L
            val post = createTestPost(postId, 1L, "테스터")

            every { postService.getById(postId) } returns post
            every { postService.incrementViewCount(post) } just Runs

            // when
            val response = postFacade.getById(postId)

            // then
            assertEquals(postId, response.id)
            assertEquals("테스트 제목", response.title)
            verify(exactly = 1) { postService.getById(postId) }
            verify(exactly = 1) { postService.incrementViewCount(post) }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회시 NoSuchElementException을 던진다")
        fun `should throw NoSuchElementException when post not found`() {
            // given
            val postId = 999L
            every { postService.getById(postId) } throws NoSuchElementException("Post not found: $postId")

            // when & then
            val exception = assertThrows<NoSuchElementException> {
                postFacade.getById(postId)
            }
            assertEquals("Post not found: $postId", exception.message)
        }
    }

    @Nested
    @DisplayName("update")
    inner class Update {

        @Test
        @DisplayName("게시글을 수정하고 PostResponse를 반환한다")
        fun `should update post and return response`() {
            // given
            val postId = 1L
            val request = PostUpdateRequest(
                title = "수정된 제목",
                content = "수정된 내용"
            )
            val updatedPost = createTestPost(postId, 1L, "테스터", "수정된 제목", "수정된 내용")

            every { postService.update(postId, request.title, request.content) } returns updatedPost

            // when
            val response = postFacade.update(postId, request)

            // then
            assertEquals(postId, response.id)
            assertEquals("수정된 제목", response.title)
            assertEquals("수정된 내용", response.content)
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정시 NoSuchElementException을 던진다")
        fun `should throw NoSuchElementException when updating non-existent post`() {
            // given
            val postId = 999L
            val request = PostUpdateRequest(title = "제목", content = "내용")
            every { postService.update(postId, request.title, request.content) } throws
                    NoSuchElementException("Post not found: $postId")

            // when & then
            assertThrows<NoSuchElementException> {
                postFacade.update(postId, request)
            }
        }
    }

    @Nested
    @DisplayName("delete")
    inner class Delete {

        @Test
        @DisplayName("게시글을 삭제한다")
        fun `should delete post`() {
            // given
            val postId = 1L
            every { postService.delete(postId) } returns Unit

            // when
            postFacade.delete(postId)

            // then
            verify(exactly = 1) { postService.delete(postId) }
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제시 NoSuchElementException을 던진다")
        fun `should throw NoSuchElementException when deleting non-existent post`() {
            // given
            val postId = 999L
            every { postService.delete(postId) } throws NoSuchElementException("Post not found: $postId")

            // when & then
            assertThrows<NoSuchElementException> {
                postFacade.delete(postId)
            }
        }
    }

    private fun createTestPost(
        id: Long,
        categoryId: Long,
        author: String,
        title: String = "테스트 제목",
        content: String = "테스트 내용"
    ): Post {
        val parent = Category(
            id = 99L,
            name = "부모 카테고리",
            description = null,
            sortOrder = 0
        )
        val category = Category(
            id = categoryId,
            parent = parent,
            name = "테스트 카테고리",
            description = null,
            sortOrder = 0
        )
        return Post(
            id = id,
            category = category,
            author = author,
            title = title,
            content = content,
            viewCount = 0,
            status = PostStatus.DRAFT
        )
    }
}
