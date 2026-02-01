package com.rotomoo.api.v1.category.integration

import com.rotomoo.api.v1.category.dto.CategoryCreateRequest
import com.rotomoo.api.v1.category.dto.CategoryUpdateRequest
import com.rotomoo.domain.category.entity.Category
import com.rotomoo.domain.category.repository.CategoryRepository
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
@DisplayName("Category API 통합 테스트")
class CategoryIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    private val baseUrl = "/api/v1/categories"

    @BeforeEach
    fun setUp() {
        categoryRepository.deleteAll()
    }

    @Nested
    @DisplayName("카테고리 생성 API")
    inner class CreateCategoryApi {

        @Test
        @DisplayName("루트 카테고리를 생성하고 201 CREATED를 반환한다")
        fun `should create root category and return 201`() {
            // given
            val request = CategoryCreateRequest(
                name = "통합 테스트 카테고리",
                description = "통합 테스트용",
                sortOrder = 0
            )

            // when & then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("통합 테스트 카테고리"))
                .andExpect(jsonPath("$.data.description").value("통합 테스트용"))
                .andExpect(jsonPath("$.data.parentId").doesNotExist())

            // DB 검증
            val categories = categoryRepository.findAll()
            assert(categories.size == 1)
            assert(categories[0].name == "통합 테스트 카테고리")
        }

        @Test
        @DisplayName("하위 카테고리를 생성하고 201 CREATED를 반환한다")
        fun `should create child category and return 201`() {
            // given
            val parentCategory = categoryRepository.save(
                Category(
                    name = "부모 카테고리",
                    description = "부모용",
                    sortOrder = 0
                )
            )

            val request = CategoryCreateRequest(
                parentId = parentCategory.id,
                name = "하위 카테고리",
                description = "하위용",
                sortOrder = 1
            )

            // when & then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("하위 카테고리"))
                .andExpect(jsonPath("$.data.parentId").value(parentCategory.id))

            // DB 검증
            val childCategories = categoryRepository.findByParentId(parentCategory.id)
            assert(childCategories.size == 1)
            assert(childCategories[0].name == "하위 카테고리")
        }

        @Test
        @DisplayName("중복된 이름의 카테고리 생성시 400 에러를 반환한다")
        fun `should return 400 when creating duplicate category name`() {
            // given
            categoryRepository.save(
                Category(
                    name = "기존 카테고리",
                    description = "기존용",
                    sortOrder = 0
                )
            )

            val request = CategoryCreateRequest(
                name = "기존 카테고리",
                description = "중복 이름",
                sortOrder = 1
            )

            // when & then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("카테고리 조회 API")
    inner class GetCategoryApi {

        @Test
        @DisplayName("ID로 카테고리를 조회하고 200 OK를 반환한다")
        fun `should return category by id with 200`() {
            // given
            val savedCategory = categoryRepository.save(
                Category(
                    name = "테스트 카테고리",
                    description = "테스트용",
                    sortOrder = 0
                )
            )

            // when & then
            mockMvc.perform(get("$baseUrl/${savedCategory.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(savedCategory.id))
                .andExpect(jsonPath("$.data.name").value("테스트 카테고리"))
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 조회시 400 에러를 반환한다")
        fun `should return 400 when category not found`() {
            // when & then
            mockMvc.perform(get("$baseUrl/999"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }

        @Test
        @DisplayName("모든 카테고리를 조회하고 200 OK를 반환한다")
        fun `should return all categories with 200`() {
            // given
            categoryRepository.save(Category(name = "카테고리1", sortOrder = 0))
            categoryRepository.save(Category(name = "카테고리2", sortOrder = 1))
            categoryRepository.save(Category(name = "카테고리3", sortOrder = 2))

            // when & then
            mockMvc.perform(get(baseUrl))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
        }

        @Test
        @DisplayName("카테고리가 없으면 빈 리스트를 반환한다")
        fun `should return empty list when no categories`() {
            // when & then
            mockMvc.perform(get(baseUrl))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0))
        }
    }

    @Nested
    @DisplayName("카테고리 트리 조회 API")
    inner class GetCategoryTreeApi {

        @Test
        @DisplayName("카테고리 트리를 조회하고 200 OK를 반환한다")
        fun `should return category tree with 200`() {
            // given
            val parent = categoryRepository.save(
                Category(name = "부모 카테고리", sortOrder = 0)
            )
            categoryRepository.save(
                Category(name = "하위 카테고리1", parent = parent, sortOrder = 0)
            )
            categoryRepository.save(
                Category(name = "하위 카테고리2", parent = parent, sortOrder = 1)
            )

            // when & then
            mockMvc.perform(get(baseUrl))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("부모 카테고리"))
                .andExpect(jsonPath("$.data[0].children.length()").value(2))
        }
    }

    @Nested
    @DisplayName("하위 카테고리 조회 API")
    inner class GetChildCategoriesApi {

        @Test
        @DisplayName("하위 카테고리를 조회하고 200 OK를 반환한다")
        fun `should return child categories with 200`() {
            // given
            val parent = categoryRepository.save(
                Category(name = "부모 카테고리", sortOrder = 0)
            )
            categoryRepository.save(
                Category(name = "하위1", parent = parent, sortOrder = 0)
            )
            categoryRepository.save(
                Category(name = "하위2", parent = parent, sortOrder = 1)
            )

            // when & then
            mockMvc.perform(get("$baseUrl/${parent.id}/children"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
        }
    }

    @Nested
    @DisplayName("카테고리 수정 API")
    inner class UpdateCategoryApi {

        @Test
        @DisplayName("카테고리를 수정하고 200 OK를 반환한다")
        fun `should update category and return 200`() {
            // given
            val savedCategory = categoryRepository.save(
                Category(name = "원래 이름", description = "원래 설명", sortOrder = 0)
            )
            val request = CategoryUpdateRequest(
                name = "수정된 이름",
                description = "수정된 설명",
                sortOrder = 1,
                isActive = true
            )

            // when & then
            mockMvc.perform(
                put("$baseUrl/${savedCategory.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("수정된 이름"))
                .andExpect(jsonPath("$.data.description").value("수정된 설명"))

            // DB 검증
            val updatedCategory = categoryRepository.findById(savedCategory.id).get()
            assert(updatedCategory.name == "수정된 이름")
            assert(updatedCategory.description == "수정된 설명")
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 수정시 400 에러를 반환한다")
        fun `should return 400 when updating non-existent category`() {
            // given
            val request = CategoryUpdateRequest(
                name = "수정",
                description = "수정"
            )

            // when & then
            mockMvc.perform(
                put("$baseUrl/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("카테고리 활성화/비활성화 API")
    inner class ActivateDeactivateCategoryApi {

        @Test
        @DisplayName("카테고리를 활성화하고 200 OK를 반환한다")
        fun `should activate category and return 200`() {
            // given
            val savedCategory = categoryRepository.save(
                Category(name = "비활성 카테고리", sortOrder = 0, isActive = false)
            )

            // when & then
            mockMvc.perform(patch("$baseUrl/${savedCategory.id}/activate"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(true))

            // DB 검증
            val activatedCategory = categoryRepository.findById(savedCategory.id).get()
            assert(activatedCategory.isActive)
        }

        @Test
        @DisplayName("카테고리를 비활성화하고 200 OK를 반환한다")
        fun `should deactivate category and return 200`() {
            // given
            val savedCategory = categoryRepository.save(
                Category(name = "활성 카테고리", sortOrder = 0, isActive = true)
            )

            // when & then
            mockMvc.perform(patch("$baseUrl/${savedCategory.id}/deactivate"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(false))

            // DB 검증
            val deactivatedCategory = categoryRepository.findById(savedCategory.id).get()
            assert(!deactivatedCategory.isActive)
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 API")
    inner class DeleteCategoryApi {

        @Test
        @DisplayName("카테고리를 삭제하고 204 NO_CONTENT를 반환한다")
        fun `should delete category and return 204`() {
            // given
            val savedCategory = categoryRepository.save(
                Category(name = "삭제 대상", sortOrder = 0)
            )

            // when & then
            mockMvc.perform(delete("$baseUrl/${savedCategory.id}"))
                .andExpect(status().isNoContent)

            // DB 검증
            assert(!categoryRepository.existsById(savedCategory.id))
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 삭제시 400 에러를 반환한다")
        fun `should return 400 when deleting non-existent category`() {
            // when & then
            mockMvc.perform(delete("$baseUrl/999"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("카테고리 CRUD 시나리오")
    inner class CategoryCrudScenario {

        @Test
        @DisplayName("카테고리 생성 -> 조회 -> 수정 -> 삭제 전체 플로우")
        fun `full CRUD flow test`() {
            // 1. 생성
            val createRequest = CategoryCreateRequest(
                name = "시나리오 카테고리",
                description = "시나리오용",
                sortOrder = 0
            )

            val createResult = mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            )
                .andExpect(status().isCreated)
                .andReturn()

            val createdId = objectMapper.readTree(createResult.response.contentAsString)["data"]["id"].asLong()

            // 2. 조회
            mockMvc.perform(get("$baseUrl/$createdId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.name").value("시나리오 카테고리"))

            // 3. 수정
            val updateRequest = CategoryUpdateRequest(
                name = "수정된 시나리오",
                description = "수정된 설명",
                sortOrder = 1,
                isActive = true
            )

            mockMvc.perform(
                put("$baseUrl/$createdId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.name").value("수정된 시나리오"))

            // 4. 삭제
            mockMvc.perform(delete("$baseUrl/$createdId"))
                .andExpect(status().isNoContent)

            // 5. 삭제 확인
            assert(!categoryRepository.existsById(createdId))
        }

        @Test
        @DisplayName("부모-자식 카테고리 계층 구조 생성 플로우")
        fun `parent-child hierarchy flow test`() {
            // 1. 부모 카테고리 생성
            val parentRequest = CategoryCreateRequest(
                name = "부모 카테고리",
                description = "부모용",
                sortOrder = 0
            )

            val parentResult = mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parentRequest))
            )
                .andExpect(status().isCreated)
                .andReturn()

            val parentId = objectMapper.readTree(parentResult.response.contentAsString)["data"]["id"].asLong()

            // 2. 하위 카테고리 생성
            val childRequest = CategoryCreateRequest(
                parentId = parentId,
                name = "하위 카테고리",
                description = "하위용",
                sortOrder = 0
            )

            mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(childRequest))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.data.parentId").value(parentId))

            // 3. 트리 조회로 계층 확인
            mockMvc.perform(get(baseUrl))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data[0].name").value("부모 카테고리"))
                .andExpect(jsonPath("$.data[0].children[0].name").value("하위 카테고리"))
        }
    }
}
