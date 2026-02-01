package com.rotomoo.domain.category.service

import com.rotomoo.domain.category.entity.Category

interface CategoryService {

    fun create(parentId: Long?, name: String, description: String?, sortOrder: Int): Category

    fun getById(id: Long): Category

    fun getByIdWithParent(id: Long): Category

    fun getRootCategories(): List<Category>

    fun getActiveRootCategories(): List<Category>

    fun getChildCategories(parentId: Long): List<Category>

    fun getActiveChildCategories(parentId: Long): List<Category>

    fun getAll(): List<Category>

    fun getActiveAll(): List<Category>

    fun update(id: Long, name: String, description: String?, sortOrder: Int, isActive: Boolean): Category

    fun delete(id: Long)

    fun activate(id: Long): Category

    fun deactivate(id: Long): Category
}
