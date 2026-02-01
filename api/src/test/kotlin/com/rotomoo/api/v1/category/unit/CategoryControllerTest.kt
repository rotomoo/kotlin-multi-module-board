package com.rotomoo.api.v1.category.unit

import com.rotomoo.api.exception.handler.GlobalExceptionHandler
import com.rotomoo.api.v1.category.controller.CategoryController
import com.rotomoo.api.v1.category.dto.CategoryCreateRequest
import com.rotomoo.api.v1.category.dto.CategoryResponse
import com.rotomoo.api.v1.category.dto.CategoryUpdateRequest
import com.rotomoo.api.v1.category.dto.CategoryWithChildrenResponse
import com.rotomoo.api.v1.category.facade.CategoryFacade
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

@WebMvcTest(CategoryController::class)
@ContextConfiguration(classes = [CategoryController::class, GlobalExceptionHandler::class])
@ImportAutoConfiguration(JacksonAutoConfiguration::class)
@DisplayName("CategoryController 단위 테스트")
class CategoryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var categoryFacade: CategoryFacade

    private val baseUrl = "/api/v1/categories"

    @Nested
    @DisplayName("POST /api/v1/categories")
    inner class CreateCategory {

        @Test
        @DisplayName("카테고리 생성 성공시 201 CREATED를 반환한다")
        fun `should return 201 when category created successfully`() {
            // given
            val request = CategoryCreateRequest(
                name = "테스트 카테고리",
                description = "테스트 설명",
                sortOrder = 0
            )
            val response = createTestCategoryResponse(1L)

            whenever(categoryFacade.create(any())).thenReturn(response)

            // when & then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("테스트 카테고리"))

            verify(categoryFacade).create(any())
        }

        @Test
        @DisplayName("하위 카테고리 생성 성공시 201 CREATED를 반환한다")
        fun `should return 201 when child category created successfully`() {
            // given
            val request = CategoryCreateRequest(
                parentId = 1L,
                name = "하위 카테고리",
                description = "하위 설명",
                sortOrder = 0
            )
            val response = createTestCategoryResponse(2L, parentId = 1L)

            whenever(categoryFacade.create(any())).thenReturn(response)

            // when & then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.parentId").value(1))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/{id}")
    inner class GetCategoryById {

        @Test
        @DisplayName("존재하는 카테고리 조회시 200 OK를 반환한다")
        fun `should return 200 when category found`() {
            // given
            val categoryId = 1L
            val response = createTestCategoryResponse(categoryId)

            whenever(categoryFacade.getById(categoryId)).thenReturn(response)

            // when & then
            mockMvc.perform(get("$baseUrl/$categoryId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(categoryId))
                .andExpect(jsonPath("$.data.name").value("테스트 카테고리"))
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 조회시 400 에러를 반환한다")
        fun `should return 400 when category not found`() {
            // given
            val categoryId = 999L
            whenever(categoryFacade.getById(categoryId)).thenThrow(IllegalArgumentException("Category not found: $categoryId"))

            // when & then
            mockMvc.perform(get("$baseUrl/$categoryId"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories")
    inner class GetCategoryTree {

        @Test
        @DisplayName("카테고리 트리 조회시 200 OK를 반환한다")
        fun `should return 200 and category tree`() {
            // given
            val treeResponse = listOf(
                CategoryWithChildrenResponse(
                    id = 1L,
                    name = "부모 카테고리",
                    description = "부모 설명",
                    sortOrder = 0,
                    isActive = true,
                    children = listOf(
                        CategoryWithChildrenResponse(
                            id = 2L,
                            name = "자식 카테고리",
                            description = "자식 설명",
                            sortOrder = 0,
                            isActive = true,
                            children = emptyList(),
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now()
                        )
                    ),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            )

            whenever(categoryFacade.getCategoryTree()).thenReturn(treeResponse)

            // when & then
            mockMvc.perform(get(baseUrl))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].children.length()").value(1))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/all")
    inner class GetAllCategories {

        @Test
        @DisplayName("모든 카테고리 조회시 200 OK와 리스트를 반환한다")
        fun `should return 200 and list of categories`() {
            // given
            val responses = listOf(
                createTestCategoryResponse(1L),
                createTestCategoryResponse(2L)
            )

            whenever(categoryFacade.getAll()).thenReturn(responses)

            // when & then
            mockMvc.perform(get("$baseUrl/all"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
        }

        @Test
        @DisplayName("카테고리가 없으면 빈 리스트를 반환한다")
        fun `should return empty list when no categories`() {
            // given
            whenever(categoryFacade.getAll()).thenReturn(emptyList())

            // when & then
            mockMvc.perform(get("$baseUrl/all"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/{parentId}/children")
    inner class GetChildCategories {

        @Test
        @DisplayName("하위 카테고리 조회시 200 OK와 리스트를 반환한다")
        fun `should return 200 and list of child categories`() {
            // given
            val parentId = 1L
            val responses = listOf(
                createTestCategoryResponse(2L, parentId = parentId),
                createTestCategoryResponse(3L, parentId = parentId)
            )

            whenever(categoryFacade.getChildCategories(parentId)).thenReturn(responses)

            // when & then
            mockMvc.perform(get("$baseUrl/$parentId/children"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/categories/{id}")
    inner class UpdateCategory {

        @Test
        @DisplayName("카테고리 수정 성공시 200 OK를 반환한다")
        fun `should return 200 when category updated successfully`() {
            // given
            val categoryId = 1L
            val request = CategoryUpdateRequest(
                name = "수정된 카테고리",
                description = "수정된 설명",
                sortOrder = 1,
                isActive = true
            )
            val response = CategoryResponse(
                id = categoryId,
                parentId = null,
                name = "수정된 카테고리",
                description = "수정된 설명",
                sortOrder = 1,
                isActive = true,
                isParent = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            whenever(categoryFacade.update(eq(categoryId), any())).thenReturn(response)

            // when & then
            mockMvc.perform(
                put("$baseUrl/$categoryId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(categoryId))
                .andExpect(jsonPath("$.data.name").value("수정된 카테고리"))
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 수정시 400 에러를 반환한다")
        fun `should return 400 when updating non-existent category`() {
            // given
            val categoryId = 999L
            val request = CategoryUpdateRequest(name = "카테고리", description = "설명")

            whenever(categoryFacade.update(eq(categoryId), any())).thenThrow(IllegalArgumentException("Category not found: $categoryId"))

            // when & then
            mockMvc.perform(
                put("$baseUrl/$categoryId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/categories/{id}/activate")
    inner class ActivateCategory {

        @Test
        @DisplayName("카테고리 활성화 성공시 200 OK를 반환한다")
        fun `should return 200 when category activated successfully`() {
            // given
            val categoryId = 1L
            val response = createTestCategoryResponse(categoryId, isActive = true)

            whenever(categoryFacade.activate(categoryId)).thenReturn(response)

            // when & then
            mockMvc.perform(patch("$baseUrl/$categoryId/activate"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(true))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/categories/{id}/deactivate")
    inner class DeactivateCategory {

        @Test
        @DisplayName("카테고리 비활성화 성공시 200 OK를 반환한다")
        fun `should return 200 when category deactivated successfully`() {
            // given
            val categoryId = 1L
            val response = createTestCategoryResponse(categoryId, isActive = false)

            whenever(categoryFacade.deactivate(categoryId)).thenReturn(response)

            // when & then
            mockMvc.perform(patch("$baseUrl/$categoryId/deactivate"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(false))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/categories/{id}")
    inner class DeleteCategory {

        @Test
        @DisplayName("카테고리 삭제 성공시 204 NO_CONTENT를 반환한다")
        fun `should return 204 when category deleted successfully`() {
            // given
            val categoryId = 1L

            // when & then
            mockMvc.perform(delete("$baseUrl/$categoryId"))
                .andExpect(status().isNoContent)

            verify(categoryFacade).delete(categoryId)
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 삭제시 400 에러를 반환한다")
        fun `should return 400 when deleting non-existent category`() {
            // given
            val categoryId = 999L
            whenever(categoryFacade.delete(categoryId)).thenThrow(IllegalArgumentException("Category not found: $categoryId"))

            // when & then
            mockMvc.perform(delete("$baseUrl/$categoryId"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    private fun createTestCategoryResponse(
        id: Long,
        parentId: Long? = null,
        isActive: Boolean = true
    ) = CategoryResponse(
        id = id,
        parentId = parentId,
        name = "테스트 카테고리",
        description = "테스트 설명",
        sortOrder = 0,
        isActive = isActive,
        isParent = false,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}
