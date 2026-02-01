package com.rotomoo.domain.category.repository

import com.rotomoo.domain.category.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CategoryRepository : JpaRepository<Category, Long> {

    fun findByParentId(parentId: Long): List<Category>

    fun findByParentIdAndIsActiveTrue(parentId: Long): List<Category>

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.sortOrder")
    fun findRootCategories(): List<Category>

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.sortOrder")
    fun findActiveRootCategories(): List<Category>

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent WHERE c.id = :id")
    fun findByIdWithParent(id: Long): Category?

    fun existsByNameAndParentId(name: String, parentId: Long?): Boolean

    fun findByIsActiveTrue(): List<Category>

    // 해당 카테고리가 자식을 가지고 있는지 확인 (leaf 카테고리 판별용)
    fun existsByParentId(parentId: Long): Boolean
}
