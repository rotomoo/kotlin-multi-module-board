package com.rotomoo.api.v1.post.integration

import com.rotomoo.api.v1.post.dto.PostCreateRequest
import com.rotomoo.api.v1.post.dto.PostUpdateRequest
import com.rotomoo.domain.category.entity.Category
import com.rotomoo.domain.category.repository.CategoryRepository
import com.rotomoo.domain.post.entity.Post
import com.rotomoo.domain.post.entity.PostStatus
import com.rotomoo.domain.post.repository.PostRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("local")
@DisplayName("Post API 통합 테스트")
class PostIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    private lateinit var parentCategory: Category
    private lateinit var testCategory: Category

    @BeforeEach
    fun setUp() {
        postRepository.deleteAll()
        categoryRepository.deleteAll()

        // 부모 카테고리 생성
        parentCategory = categoryRepository.save(
            Category(
                name = "부모 카테고리",
                description = "부모용",
                sortOrder = 0
            )
        )

        // 자식 카테고리 생성 (게시글은 자식 카테고리에만 작성 가능)
        testCategory = categoryRepository.save(
            Category(
                parent = parentCategory,
                name = "테스트 카테고리",
                description = "테스트용",
                sortOrder = 0
            )
        )
    }

    @Nested
    @DisplayName("게시글 생성 API")
    inner class CreatePostApi {

        @Test
        @DisplayName("게시글을 생성하고 201 CREATED를 반환한다")
        fun `should create post and return 201`() {
            // given
            val request = PostCreateRequest(
                author = "테스터",
                title = "통합 테스트 제목",
                content = "통합 테스트 내용",
                categoryId = testCategory.id
            )

            // when & then
            mockMvc.perform(
                post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.category.id").value(testCategory.id))
                .andExpect(jsonPath("$.data.author").value("테스터"))
                .andExpect(jsonPath("$.data.title").value("통합 테스트 제목"))
                .andExpect(jsonPath("$.data.content").value("통합 테스트 내용"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists())

            // DB 검증
            val posts = postRepository.findAll()
            assert(posts.size == 1)
            assert(posts[0].title == "통합 테스트 제목")
        }
    }

    @Nested
    @DisplayName("게시글 조회 API")
    inner class GetPostApi {

        @Test
        @DisplayName("ID로 게시글을 조회하고 200 OK를 반환한다")
        fun `should return post by id with 200`() {
            // given
            val savedPost = createTestPost()

            // when & then
            mockMvc.perform(get("/api/v1/posts/${savedPost.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(savedPost.id))
                .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                .andExpect(jsonPath("$.data.content").value("테스트 내용"))
                .andExpect(jsonPath("$.data.author").value("테스터"))
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회시 404 에러를 반환한다")
        fun `should return 404 when post not found`() {
            // when & then
            mockMvc.perform(get("/api/v1/posts/999"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("게시글 수정 API")
    inner class UpdatePostApi {

        @Test
        @DisplayName("게시글을 수정하고 200 OK를 반환한다")
        fun `should update post and return 200`() {
            // given
            val savedPost = createTestPost()
            val request = PostUpdateRequest(
                title = "수정된 제목",
                content = "수정된 내용"
            )

            // when & then
            mockMvc.perform(
                put("/api/v1/posts/${savedPost.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(savedPost.id))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"))

            // DB 검증
            val updatedPost = postRepository.findById(savedPost.id).get()
            assert(updatedPost.title == "수정된 제목")
            assert(updatedPost.content == "수정된 내용")
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정시 404 에러를 반환한다")
        fun `should return 404 when updating non-existent post`() {
            // given
            val request = PostUpdateRequest(title = "제목", content = "내용")

            // when & then
            mockMvc.perform(
                put("/api/v1/posts/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("게시글 삭제 API")
    inner class DeletePostApi {

        @Test
        @DisplayName("게시글을 삭제하고 204 NO_CONTENT를 반환한다")
        fun `should delete post and return 204`() {
            // given
            val savedPost = createTestPost()

            // when & then
            mockMvc.perform(delete("/api/v1/posts/${savedPost.id}"))
                .andExpect(status().isNoContent)

            // DB 검증 - Soft Delete이므로 status가 DELETED로 변경됨
            val deletedPost = postRepository.findById(savedPost.id).get()
            assert(deletedPost.status == PostStatus.DELETED)
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제시 404 에러를 반환한다")
        fun `should return 404 when deleting non-existent post`() {
            // when & then
            mockMvc.perform(delete("/api/v1/posts/999"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("게시글 CRUD 시나리오")
    inner class PostCrudScenario {

        @Test
        @DisplayName("게시글 생성 -> 조회 -> 수정 -> 삭제 전체 플로우")
        fun `full CRUD flow test`() {
            // 1. 생성
            val createRequest = PostCreateRequest(
                author = "테스터",
                title = "시나리오 테스트",
                content = "시나리오 내용",
                categoryId = testCategory.id
            )

            val createResult = mockMvc.perform(
                post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            )
                .andExpect(status().isCreated)
                .andReturn()

            val createdId = objectMapper.readTree(createResult.response.contentAsString)["data"]["id"].asLong()

            // 2. 조회
            mockMvc.perform(get("/api/v1/posts/$createdId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.title").value("시나리오 테스트"))

            // 3. 수정
            val updateRequest = PostUpdateRequest(
                title = "수정된 시나리오",
                content = "수정된 시나리오 내용"
            )

            mockMvc.perform(
                put("/api/v1/posts/$createdId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.title").value("수정된 시나리오"))

            // 4. 삭제
            mockMvc.perform(delete("/api/v1/posts/$createdId"))
                .andExpect(status().isNoContent)

            // 5. 삭제 확인 - Soft Delete이므로 조회는 가능하지만 status가 DELETED
            val deletedPost = postRepository.findById(createdId).get()
            assert(deletedPost.status == PostStatus.DELETED)
        }
    }

    private fun createTestPost() = postRepository.save(
        Post(
            category = testCategory,
            author = "테스터",
            title = "테스트 제목",
            content = "테스트 내용"
        )
    )

    private fun createPublishedPost(): Post {
        val post = Post(
            category = testCategory,
            author = "테스터",
            title = "테스트 제목",
            content = "테스트 내용"
        )
        post.publish()
        return postRepository.save(post)
    }
}
