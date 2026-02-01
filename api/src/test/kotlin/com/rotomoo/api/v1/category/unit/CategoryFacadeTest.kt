package com.rotomoo.api.v1.category.unit

import com.rotomoo.api.v1.category.dto.*
import com.rotomoo.api.v1.category.facade.CategoryFacade
import com.rotomoo.domain.category.entity.Category
import com.rotomoo.domain.category.service.CategoryService
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

@ExtendWith(MockKExtension::class)
@DisplayName("CategoryFacade 단위 테스트")
class CategoryFacadeTest {

    @MockK
    private lateinit var categoryService: CategoryService

    @InjectMockKs
    private lateinit var categoryFacade: CategoryFacade

    @Nested
    @DisplayName("create")
    inner class Create {

        @Test
        @DisplayName("카테고리를 생성하고 CategoryResponse를 반환한다")
        fun `should create category and return response`() {
            // given
            val request = CategoryCreateRequest(
                name = "테스트 카테고리",
                description = "테스트 설명",
                sortOrder = 0
            )
            val savedCategory = createTestCategory(1L)

            every { categoryService.create(null, request.name, request.description, request.sortOrder) } returns savedCategory

            // when
            val response = categoryFacade.create(request)

            // then
            assertEquals(1L, response.id)
            assertEquals("테스트 카테고리", response.name)
            assertEquals("테스트 설명", response.description)
            verify(exactly = 1) { categoryService.create(null, request.name, request.description, request.sortOrder) }
        }

        @Test
        @DisplayName("하위 카테고리를 생성하고 CategoryResponse를 반환한다")
        fun `should create child category and return response`() {
            // given
            val parentId = 1L
            val request = CategoryCreateRequest(
                parentId = parentId,
                name = "하위 카테고리",
                description = "하위 설명",
                sortOrder = 0
            )
            val parent = createTestCategory(parentId)
            val savedCategory = createTestCategory(2L, parent = parent)

            every { categoryService.create(parentId, request.name, request.description, request.sortOrder) } returns savedCategory

            // when
            val response = categoryFacade.create(request)

            // then
            assertEquals(2L, response.id)
            assertEquals(parentId, response.parentId)
            verify(exactly = 1) { categoryService.create(parentId, request.name, request.description, request.sortOrder) }
        }
    }

    @Nested
    @DisplayName("getById")
    inner class GetById {

        @Test
        @DisplayName("ID로 카테고리를 조회하고 CategoryResponse를 반환한다")
        fun `should return category response when category exists`() {
            // given
            val categoryId = 1L
            val category = createTestCategory(categoryId)

            every { categoryService.getById(categoryId) } returns category

            // when
            val response = categoryFacade.getById(categoryId)

            // then
            assertEquals(categoryId, response.id)
            assertEquals("테스트 카테고리", response.name)
            verify(exactly = 1) { categoryService.getById(categoryId) }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회시 IllegalArgumentException을 던진다")
        fun `should throw IllegalArgumentException when category not found`() {
            // given
            val categoryId = 999L
            every { categoryService.getById(categoryId) } throws IllegalArgumentException("Category not found: $categoryId")

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                categoryFacade.getById(categoryId)
            }
            assertEquals("Category not found: $categoryId", exception.message)
        }
    }

    @Nested
    @DisplayName("getAll")
    inner class GetAll {

        @Test
        @DisplayName("모든 카테고리를 조회하고 리스트를 반환한다")
        fun `should return all categories`() {
            // given
            val categories = listOf(
                createTestCategory(1L),
                createTestCategory(2L)
            )
            every { categoryService.getAll() } returns categories

            // when
            val responses = categoryFacade.getAll()

            // then
            assertEquals(2, responses.size)
            verify(exactly = 1) { categoryService.getAll() }
        }

        @Test
        @DisplayName("카테고리가 없으면 빈 리스트를 반환한다")
        fun `should return empty list when no categories`() {
            // given
            every { categoryService.getAll() } returns emptyList()

            // when
            val responses = categoryFacade.getAll()

            // then
            assertTrue(responses.isEmpty())
        }
    }

    @Nested
    @DisplayName("getCategoryTree")
    inner class GetCategoryTree {

        @Test
        @DisplayName("카테고리 트리를 반환한다")
        fun `should return category tree`() {
            // given
            val parent = createTestCategory(1L)
            val child = createTestCategory(2L, parent = parent)

            every { categoryService.getActiveAll() } returns listOf(parent, child)

            // when
            val response = categoryFacade.getCategoryTree()

            // then
            assertEquals(1, response.size)
            assertEquals(1, response[0].children.size)
            verify(exactly = 1) { categoryService.getActiveAll() }
        }
    }

    @Nested
    @DisplayName("getChildCategories")
    inner class GetChildCategories {

        @Test
        @DisplayName("하위 카테고리를 조회하고 리스트를 반환한다")
        fun `should return child categories`() {
            // given
            val parentId = 1L
            val children = listOf(
                createTestCategory(2L),
                createTestCategory(3L)
            )
            every { categoryService.getChildCategories(parentId) } returns children

            // when
            val responses = categoryFacade.getChildCategories(parentId)

            // then
            assertEquals(2, responses.size)
            verify(exactly = 1) { categoryService.getChildCategories(parentId) }
        }
    }

    @Nested
    @DisplayName("update")
    inner class Update {

        @Test
        @DisplayName("카테고리를 수정하고 CategoryResponse를 반환한다")
        fun `should update category and return response`() {
            // given
            val categoryId = 1L
            val request = CategoryUpdateRequest(
                name = "수정된 카테고리",
                description = "수정된 설명",
                sortOrder = 1,
                isActive = true
            )
            val updatedCategory = createTestCategory(categoryId, name = "수정된 카테고리", description = "수정된 설명")

            every { categoryService.update(categoryId, request.name, request.description, request.sortOrder, request.isActive) } returns updatedCategory

            // when
            val response = categoryFacade.update(categoryId, request)

            // then
            assertEquals(categoryId, response.id)
            assertEquals("수정된 카테고리", response.name)
            assertEquals("수정된 설명", response.description)
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 수정시 IllegalArgumentException을 던진다")
        fun `should throw IllegalArgumentException when updating non-existent category`() {
            // given
            val categoryId = 999L
            val request = CategoryUpdateRequest(name = "카테고리", description = "설명")
            every { categoryService.update(categoryId, request.name, request.description, request.sortOrder, request.isActive) } throws
                    IllegalArgumentException("Category not found: $categoryId")

            // when & then
            assertThrows<IllegalArgumentException> {
                categoryFacade.update(categoryId, request)
            }
        }
    }

    @Nested
    @DisplayName("activate")
    inner class Activate {

        @Test
        @DisplayName("카테고리를 활성화하고 CategoryResponse를 반환한다")
        fun `should activate category and return response`() {
            // given
            val categoryId = 1L
            val activatedCategory = createTestCategory(categoryId, isActive = true)

            every { categoryService.activate(categoryId) } returns activatedCategory

            // when
            val response = categoryFacade.activate(categoryId)

            // then
            assertEquals(categoryId, response.id)
            assertTrue(response.isActive)
            verify(exactly = 1) { categoryService.activate(categoryId) }
        }
    }

    @Nested
    @DisplayName("deactivate")
    inner class Deactivate {

        @Test
        @DisplayName("카테고리를 비활성화하고 CategoryResponse를 반환한다")
        fun `should deactivate category and return response`() {
            // given
            val categoryId = 1L
            val deactivatedCategory = createTestCategory(categoryId, isActive = false)

            every { categoryService.deactivate(categoryId) } returns deactivatedCategory

            // when
            val response = categoryFacade.deactivate(categoryId)

            // then
            assertEquals(categoryId, response.id)
            assertFalse(response.isActive)
            verify(exactly = 1) { categoryService.deactivate(categoryId) }
        }
    }

    @Nested
    @DisplayName("delete")
    inner class Delete {

        @Test
        @DisplayName("카테고리를 삭제한다")
        fun `should delete category`() {
            // given
            val categoryId = 1L
            every { categoryService.delete(categoryId) } returns Unit

            // when
            categoryFacade.delete(categoryId)

            // then
            verify(exactly = 1) { categoryService.delete(categoryId) }
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 삭제시 IllegalArgumentException을 던진다")
        fun `should throw IllegalArgumentException when deleting non-existent category`() {
            // given
            val categoryId = 999L
            every { categoryService.delete(categoryId) } throws IllegalArgumentException("Category not found: $categoryId")

            // when & then
            assertThrows<IllegalArgumentException> {
                categoryFacade.delete(categoryId)
            }
        }
    }

    private fun createTestCategory(
        id: Long,
        name: String = "테스트 카테고리",
        description: String? = "테스트 설명",
        parent: Category? = null,
        isActive: Boolean = true
    ): Category {
        return Category(
            id = id,
            parent = parent,
            name = name,
            description = description,
            sortOrder = 0,
            isActive = isActive
        )
    }
}
