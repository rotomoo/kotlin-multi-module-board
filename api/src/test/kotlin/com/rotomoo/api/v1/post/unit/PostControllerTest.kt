package com.rotomoo.api.v1.post.unit

import com.rotomoo.api.exception.handler.GlobalExceptionHandler
import com.rotomoo.api.v1.post.controller.PostController
import com.rotomoo.api.v1.post.dto.*
import com.rotomoo.api.v1.post.facade.PostFacade
import com.rotomoo.domain.post.entity.PostStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime

@WebMvcTest(PostController::class)
@ContextConfiguration(classes = [PostController::class, GlobalExceptionHandler::class])
@ImportAutoConfiguration(JacksonAutoConfiguration::class)
@DisplayName("PostController 단위 테스트")
class PostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var postFacade: PostFacade

    @Nested
    @DisplayName("POST /api/v1/posts")
    inner class CreatePost {

        @Test
        @DisplayName("게시글 생성 성공시 201 CREATED를 반환한다")
        fun `should return 201 when post created successfully`() {
            // given
            val request = PostCreateRequest(
                author = "테스터",
                title = "테스트 제목",
                content = "테스트 내용",
                categoryId = 1L
            )
            val response = createTestPostResponse(1L)

            whenever(postFacade.create(any())).thenReturn(response)

            // when & then
            mockMvc.perform(
                post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                .andExpect(jsonPath("$.data.content").value("테스트 내용"))

            verify(postFacade).create(any())
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/{id}")
    inner class GetPostById {

        @Test
        @DisplayName("존재하는 게시글 조회시 200 OK를 반환한다")
        fun `should return 200 when post found`() {
            // given
            val postId = 1L
            val response = createTestPostResponse(postId)

            whenever(postFacade.getById(postId)).thenReturn(response)

            // when & then
            mockMvc.perform(get("/api/v1/posts/$postId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.title").value("테스트 제목"))
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회시 404 에러를 반환한다")
        fun `should return 404 when post not found`() {
            // given
            val postId = 999L
            whenever(postFacade.getById(postId)).thenThrow(NoSuchElementException("Post not found: $postId"))

            // when & then
            mockMvc.perform(get("/api/v1/posts/$postId"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/posts/{id}")
    inner class UpdatePost {

        @Test
        @DisplayName("게시글 수정 성공시 200 OK를 반환한다")
        fun `should return 200 when post updated successfully`() {
            // given
            val postId = 1L
            val request = PostUpdateRequest(
                title = "수정된 제목",
                content = "수정된 내용"
            )
            val response = PostResponse(
                id = postId,
                title = "수정된 제목",
                content = "수정된 내용",
                category = CategoryInfo(1L, "테스트 카테고리"),
                author = "테스터",
                viewCount = 0,
                status = PostStatus.DRAFT,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            whenever(postFacade.update(eq(postId), any())).thenReturn(response)

            // when & then
            mockMvc.perform(
                put("/api/v1/posts/$postId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"))
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정시 404 에러를 반환한다")
        fun `should return 404 when updating non-existent post`() {
            // given
            val postId = 999L
            val request = PostUpdateRequest(title = "제목", content = "내용")

            whenever(postFacade.update(eq(postId), any())).thenThrow(NoSuchElementException("Post not found: $postId"))

            // when & then
            mockMvc.perform(
                put("/api/v1/posts/$postId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/posts/{id}")
    inner class DeletePost {

        @Test
        @DisplayName("게시글 삭제 성공시 204 NO_CONTENT를 반환한다")
        fun `should return 204 when post deleted successfully`() {
            // given
            val postId = 1L

            // when & then
            mockMvc.perform(delete("/api/v1/posts/$postId"))
                .andExpect(status().isNoContent)

            verify(postFacade).delete(postId)
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제시 404 에러를 반환한다")
        fun `should return 404 when deleting non-existent post`() {
            // given
            val postId = 999L
            whenever(postFacade.delete(postId)).thenThrow(NoSuchElementException("Post not found: $postId"))

            // when & then
            mockMvc.perform(delete("/api/v1/posts/$postId"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    private fun createTestPostResponse(id: Long) = PostResponse(
        id = id,
        title = "테스트 제목",
        content = "테스트 내용",
        category = CategoryInfo(1L, "테스트 카테고리"),
        author = "테스터",
        viewCount = 0,
        status = PostStatus.DRAFT,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}
