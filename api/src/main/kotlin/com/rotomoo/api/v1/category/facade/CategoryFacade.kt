package com.rotomoo.api.v1.category.facade

import com.rotomoo.api.v1.category.dto.*
import com.rotomoo.domain.category.entity.Category
import com.rotomoo.domain.category.service.CategoryService
import org.springframework.stereotype.Component

@Component
class CategoryFacade(
    private val categoryService: CategoryService
) {

    fun create(request: CategoryCreateRequest): CategoryResponse {
        val category = categoryService.create(
            parentId = request.parentId,
            name = request.name,
            description = request.description,
            sortOrder = request.sortOrder
        )
        return CategoryResponse.from(category)
    }

    fun getById(id: Long): CategoryResponse {
        val category = categoryService.getById(id)
        return CategoryResponse.from(category)
    }

    fun getCategoryTree(): List<CategoryWithChildrenResponse> {
        // 모든 활성 카테고리를 가져와서 메모리에서 트리 구축 (무한 depth 지원)
        val allCategories = categoryService.getActiveAll()

        // parentId -> children 맵 생성
        val childrenMap: Map<Long?, List<Category>> = allCategories.groupBy { it.parent?.id }

        // 루트 카테고리들 (parent가 null인 것)
        val rootCategories = childrenMap[null] ?: emptyList()

        return rootCategories
            .sortedBy { it.sortOrder }
            .map { CategoryWithChildrenResponse.from(it, childrenMap) }
    }

    fun getAll(): List<CategoryResponse> {
        val categories = categoryService.getAll()
        return categories
            .sortedWith(compareBy({ it.parent?.id }, { it.sortOrder }))
            .map { CategoryResponse.from(it) }
    }

    fun getChildCategories(parentId: Long): List<CategoryResponse> {
        val children = categoryService.getChildCategories(parentId)
        return children.sortedBy { it.sortOrder }.map { CategoryResponse.from(it) }
    }

    fun update(id: Long, request: CategoryUpdateRequest): CategoryResponse {
        val category = categoryService.update(
            id = id,
            name = request.name,
            description = request.description,
            sortOrder = request.sortOrder,
            isActive = request.isActive
        )
        return CategoryResponse.from(category)
    }

    fun activate(id: Long): CategoryResponse {
        val category = categoryService.activate(id)
        return CategoryResponse.from(category)
    }

    fun deactivate(id: Long): CategoryResponse {
        val category = categoryService.deactivate(id)
        return CategoryResponse.from(category)
    }

    fun delete(id: Long) {
        categoryService.delete(id)
    }
}
