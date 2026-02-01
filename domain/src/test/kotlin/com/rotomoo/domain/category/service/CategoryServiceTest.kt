package com.rotomoo.domain.category.service

import com.rotomoo.domain.category.entity.Category
import com.rotomoo.domain.category.repository.CategoryRepository
import com.rotomoo.domain.post.entity.Post
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
@DisplayName("CategoryService 단위 테스트")
class CategoryServiceTest {

    @MockK
    private lateinit var categoryRepository: CategoryRepository

    @MockK
    private lateinit var postRepository: PostRepository

    @InjectMockKs
    private lateinit var categoryService: CategoryServiceImpl

    @Nested
    @DisplayName("create")
    inner class Create {

        @Test
        @DisplayName("루트 카테고리를 생성한다")
        fun `should create root category`() {
            // given
            val name = "테스트 카테고리"
            val description = "테스트 설명"
            val sortOrder = 0
            val savedCategory = createTestCategory(1L, name, description)

            every { categoryRepository.existsByNameAndParentId(name, null) } returns false
            every { categoryRepository.save(any()) } returns savedCategory

            // when
            val result = categoryService.create(null, name, description, sortOrder)

            // then
            assertEquals(name, result.name)
            assertEquals(description, result.description)
            assertNull(result.parent)
            verify(exactly = 1) { categoryRepository.save(any()) }
        }

        @Test
        @DisplayName("하위 카테고리를 생성한다")
        fun `should create child category`() {
            // given
            val parentId = 1L
            val name = "하위 카테고리"
            val description = "하위 설명"
            val sortOrder = 0
            val parent = createTestCategory(parentId, "부모 카테고리")
            val savedCategory = createTestCategory(2L, name, description, parent)

            every { categoryRepository.findById(parentId) } returns Optional.of(parent)
            every { categoryRepository.existsByNameAndParentId(name, parentId) } returns false
            every { categoryRepository.save(any()) } returns savedCategory

            // when
            val result = categoryService.create(parentId, name, description, sortOrder)

            // then
            assertEquals(name, result.name)
            assertEquals(parent, result.parent)
            verify(exactly = 1) { categoryRepository.findById(parentId) }
            verify(exactly = 1) { categoryRepository.save(any()) }
        }

        @Test
        @DisplayName("존재하지 않는 부모 ID로 생성시 예외를 던진다")
        fun `should throw exception when parent not found`() {
            // given
            val parentId = 999L

            every { categoryRepository.findById(parentId) } returns Optional.empty()

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                categoryService.create(parentId, "카테고리", null, 0)
            }
            assertEquals("Parent category not found: $parentId", exception.message)
        }

        @Test
        @DisplayName("중복된 이름으로 생성시 예외를 던진다")
        fun `should throw exception when duplicate name`() {
            // given
            val name = "중복 이름"

            every { categoryRepository.existsByNameAndParentId(name, null) } returns true

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                categoryService.create(null, name, null, 0)
            }
            assertEquals("Category with name '$name' already exists in this level", exception.message)
        }

        @Test
        @DisplayName("무한 depth 허용 - 손자 카테고리를 생성할 수 있다")
        fun `should allow creating grandchild category with unlimited depth`() {
            // given
            val grandparent = createTestCategory(1L, "할아버지")
            val parent = createTestCategory(2L, "부모", parent = grandparent)
            val grandchild = createTestCategory(3L, "손자", parent = parent)

            every { categoryRepository.findById(2L) } returns Optional.of(parent)
            every { categoryRepository.existsByNameAndParentId("손자", 2L) } returns false
            every { categoryRepository.save(any()) } returns grandchild

            // when
            val result = categoryService.create(2L, "손자", null, 0)

            // then
            assertEquals("손자", result.name)
            assertEquals(parent, result.parent)
            verify(exactly = 1) { categoryRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("getById")
    inner class GetById {

        @Test
        @DisplayName("ID로 카테고리를 조회한다")
        fun `should return category by id`() {
            // given
            val categoryId = 1L
            val category = createTestCategory(categoryId)

            every { categoryRepository.findById(categoryId) } returns Optional.of(category)

            // when
            val result = categoryService.getById(categoryId)

            // then
            assertEquals(categoryId, result.id)
            verify(exactly = 1) { categoryRepository.findById(categoryId) }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회시 예외를 던진다")
        fun `should throw exception when category not found`() {
            // given
            val categoryId = 999L

            every { categoryRepository.findById(categoryId) } returns Optional.empty()

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                categoryService.getById(categoryId)
            }
            assertEquals("Category not found: $categoryId", exception.message)
        }
    }

    @Nested
    @DisplayName("getByIdWithParent")
    inner class GetByIdWithParent {

        @Test
        @DisplayName("ID로 부모와 함께 카테고리를 조회한다")
        fun `should return category with parent`() {
            // given
            val categoryId = 1L
            val category = createTestCategory(categoryId)

            every { categoryRepository.findByIdWithParent(categoryId) } returns category

            // when
            val result = categoryService.getByIdWithParent(categoryId)

            // then
            assertEquals(categoryId, result.id)
            verify(exactly = 1) { categoryRepository.findByIdWithParent(categoryId) }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회시 예외를 던진다")
        fun `should throw exception when category not found`() {
            // given
            val categoryId = 999L

            every { categoryRepository.findByIdWithParent(categoryId) } returns null

            // when & then
            assertThrows<IllegalArgumentException> {
                categoryService.getByIdWithParent(categoryId)
            }
        }
    }

    @Nested
    @DisplayName("getRootCategories")
    inner class GetRootCategories {

        @Test
        @DisplayName("루트 카테고리 목록을 반환한다")
        fun `should return root categories`() {
            // given
            val categories = listOf(
                createTestCategory(1L, "카테고리1"),
                createTestCategory(2L, "카테고리2")
            )

            every { categoryRepository.findRootCategories() } returns categories

            // when
            val result = categoryService.getRootCategories()

            // then
            assertEquals(2, result.size)
            verify(exactly = 1) { categoryRepository.findRootCategories() }
        }
    }

    @Nested
    @DisplayName("getActiveRootCategories")
    inner class GetActiveRootCategories {

        @Test
        @DisplayName("활성화된 루트 카테고리 목록을 반환한다")
        fun `should return active root categories`() {
            // given
            val categories = listOf(createTestCategory(1L))

            every { categoryRepository.findActiveRootCategories() } returns categories

            // when
            val result = categoryService.getActiveRootCategories()

            // then
            assertEquals(1, result.size)
            verify(exactly = 1) { categoryRepository.findActiveRootCategories() }
        }
    }

    @Nested
    @DisplayName("getChildCategories")
    inner class GetChildCategories {

        @Test
        @DisplayName("하위 카테고리 목록을 반환한다")
        fun `should return child categories`() {
            // given
            val parentId = 1L
            val children = listOf(
                createTestCategory(2L, "하위1"),
                createTestCategory(3L, "하위2")
            )

            every { categoryRepository.findByParentId(parentId) } returns children

            // when
            val result = categoryService.getChildCategories(parentId)

            // then
            assertEquals(2, result.size)
            verify(exactly = 1) { categoryRepository.findByParentId(parentId) }
        }
    }

    @Nested
    @DisplayName("getActiveChildCategories")
    inner class GetActiveChildCategories {

        @Test
        @DisplayName("활성화된 하위 카테고리 목록을 반환한다")
        fun `should return active child categories`() {
            // given
            val parentId = 1L
            val children = listOf(createTestCategory(2L))

            every { categoryRepository.findByParentIdAndIsActiveTrue(parentId) } returns children

            // when
            val result = categoryService.getActiveChildCategories(parentId)

            // then
            assertEquals(1, result.size)
            verify(exactly = 1) { categoryRepository.findByParentIdAndIsActiveTrue(parentId) }
        }
    }

    @Nested
    @DisplayName("getAll")
    inner class GetAll {

        @Test
        @DisplayName("모든 카테고리를 반환한다")
        fun `should return all categories`() {
            // given
            val categories = listOf(
                createTestCategory(1L),
                createTestCategory(2L)
            )

            every { categoryRepository.findAll() } returns categories

            // when
            val result = categoryService.getAll()

            // then
            assertEquals(2, result.size)
            verify(exactly = 1) { categoryRepository.findAll() }
        }

        @Test
        @DisplayName("카테고리가 없으면 빈 리스트를 반환한다")
        fun `should return empty list when no categories`() {
            // given
            every { categoryRepository.findAll() } returns emptyList()

            // when
            val result = categoryService.getAll()

            // then
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("getActiveAll")
    inner class GetActiveAll {

        @Test
        @DisplayName("활성화된 모든 카테고리를 반환한다")
        fun `should return all active categories`() {
            // given
            val categories = listOf(createTestCategory(1L))

            every { categoryRepository.findByIsActiveTrue() } returns categories

            // when
            val result = categoryService.getActiveAll()

            // then
            assertEquals(1, result.size)
            verify(exactly = 1) { categoryRepository.findByIsActiveTrue() }
        }
    }

    @Nested
    @DisplayName("update")
    inner class Update {

        @Test
        @DisplayName("카테고리를 수정한다")
        fun `should update category`() {
            // given
            val categoryId = 1L
            val category = createTestCategory(categoryId, "원래 이름", "원래 설명")

            every { categoryRepository.findById(categoryId) } returns Optional.of(category)

            // when
            val result = categoryService.update(categoryId, "수정된 이름", "수정된 설명", 1, true)

            // then
            assertEquals("수정된 이름", result.name)
            assertEquals("수정된 설명", result.description)
            assertEquals(1, result.sortOrder)
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 수정시 예외를 던진다")
        fun `should throw exception when updating non-existent category`() {
            // given
            val categoryId = 999L

            every { categoryRepository.findById(categoryId) } returns Optional.empty()

            // when & then
            assertThrows<IllegalArgumentException> {
                categoryService.update(categoryId, "이름", null, 0, true)
            }
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
            val category = createTestCategory(categoryId)

            every { categoryRepository.findById(categoryId) } returns Optional.of(category)
            every { categoryRepository.findByParentId(categoryId) } returns emptyList()
            every { postRepository.findByCategoryId(categoryId) } returns emptyList()
            every { postRepository.deleteAll(emptyList()) } returns Unit
            every { categoryRepository.delete(category) } returns Unit

            // when
            categoryService.delete(categoryId)

            // then
            verify(exactly = 1) { categoryRepository.delete(category) }
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 삭제시 예외를 던진다")
        fun `should throw exception when deleting non-existent category`() {
            // given
            val categoryId = 999L

            every { categoryRepository.findById(categoryId) } returns Optional.empty()

            // when & then
            assertThrows<IllegalArgumentException> {
                categoryService.delete(categoryId)
            }
        }

        @Test
        @DisplayName("자식 카테고리와 게시글을 함께 삭제한다")
        fun `should delete category with children and posts`() {
            // given
            val parentCategory = createTestCategory(1L, "부모")
            val childCategory = createTestCategory(2L, "자식", parent = parentCategory)
            val post = Post(
                id = 1L,
                category = childCategory,
                author = "테스터",
                title = "테스트",
                content = "내용"
            )

            every { categoryRepository.findById(1L) } returns Optional.of(parentCategory)
            every { categoryRepository.findByParentId(1L) } returns listOf(childCategory)
            every { categoryRepository.findByParentId(2L) } returns emptyList()
            every { postRepository.findByCategoryId(1L) } returns emptyList()
            every { postRepository.findByCategoryId(2L) } returns listOf(post)
            every { postRepository.deleteAll(emptyList()) } returns Unit
            every { postRepository.deleteAll(listOf(post)) } returns Unit
            every { categoryRepository.delete(childCategory) } returns Unit
            every { categoryRepository.delete(parentCategory) } returns Unit

            // when
            categoryService.delete(1L)

            // then
            verify(exactly = 1) { postRepository.deleteAll(listOf(post)) }
            verify(exactly = 1) { categoryRepository.delete(childCategory) }
            verify(exactly = 1) { categoryRepository.delete(parentCategory) }
        }
    }

    @Nested
    @DisplayName("activate")
    inner class Activate {

        @Test
        @DisplayName("카테고리를 활성화한다")
        fun `should activate category`() {
            // given
            val categoryId = 1L
            val category = createTestCategory(categoryId, isActive = false)

            every { categoryRepository.findById(categoryId) } returns Optional.of(category)

            // when
            val result = categoryService.activate(categoryId)

            // then
            assertTrue(result.isActive)
        }
    }

    @Nested
    @DisplayName("deactivate")
    inner class Deactivate {

        @Test
        @DisplayName("카테고리를 비활성화한다")
        fun `should deactivate category`() {
            // given
            val categoryId = 1L
            val category = createTestCategory(categoryId, isActive = true)

            every { categoryRepository.findById(categoryId) } returns Optional.of(category)

            // when
            val result = categoryService.deactivate(categoryId)

            // then
            assertFalse(result.isActive)
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
