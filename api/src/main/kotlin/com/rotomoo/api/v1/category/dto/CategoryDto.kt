package com.rotomoo.api.v1.category.dto

import com.rotomoo.domain.category.entity.Category
import java.time.LocalDateTime

data class CategoryCreateRequest(
    val parentId: Long? = null,
    val name: String,
    val description: String? = null,
    val sortOrder: Int = 0
)

data class CategoryUpdateRequest(
    val name: String,
    val description: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)

data class CategoryResponse(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val description: String?,
    val sortOrder: Int,
    val isActive: Boolean,
    val isParent: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(category: Category): CategoryResponse {
            return CategoryResponse(
                id = category.id,
                parentId = category.parent?.id,
                name = category.name,
                description = category.description,
                sortOrder = category.sortOrder,
                isActive = category.isActive,
                isParent = category.isParentCategory(),
                createdAt = category.createdAt,
                updatedAt = category.updatedAt
            )
        }
    }
}

// 무한 depth 트리 구조 - children도 같은 타입으로 재귀
data class CategoryWithChildrenResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val sortOrder: Int,
    val isActive: Boolean,
    val children: List<CategoryWithChildrenResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(category: Category, childrenMap: Map<Long?, List<Category>>): CategoryWithChildrenResponse {
            val directChildren = childrenMap[category.id] ?: emptyList()
            return CategoryWithChildrenResponse(
                id = category.id,
                name = category.name,
                description = category.description,
                sortOrder = category.sortOrder,
                isActive = category.isActive,
                children = directChildren
                    .sortedBy { it.sortOrder }
                    .map { from(it, childrenMap) },  // 재귀 호출
                createdAt = category.createdAt,
                updatedAt = category.updatedAt
            )
        }
    }
}
