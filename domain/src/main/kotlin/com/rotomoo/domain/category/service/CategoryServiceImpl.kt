package com.rotomoo.domain.category.service

import com.rotomoo.domain.category.entity.Category
import com.rotomoo.domain.category.repository.CategoryRepository
import com.rotomoo.domain.post.repository.PostRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val postRepository: PostRepository
) : CategoryService {

    @Transactional
    override fun create(parentId: Long?, name: String, description: String?, sortOrder: Int): Category {
        val parent = parentId?.let {
            categoryRepository.findById(it)
                .orElseThrow { IllegalArgumentException("Parent category not found: $it") }
        }

        if (categoryRepository.existsByNameAndParentId(name, parentId)) {
            throw IllegalArgumentException("Category with name '$name' already exists in this level")
        }

        val category = Category(
            parent = parent,
            name = name,
            description = description,
            sortOrder = sortOrder
        )

        return categoryRepository.save(category)
    }

    override fun getById(id: Long): Category {
        return categoryRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Category not found: $id") }
    }

    override fun getByIdWithParent(id: Long): Category {
        return categoryRepository.findByIdWithParent(id)
            ?: throw IllegalArgumentException("Category not found: $id")
    }

    override fun getRootCategories(): List<Category> {
        return categoryRepository.findRootCategories()
    }

    override fun getActiveRootCategories(): List<Category> {
        return categoryRepository.findActiveRootCategories()
    }

    override fun getChildCategories(parentId: Long): List<Category> {
        return categoryRepository.findByParentId(parentId)
    }

    override fun getActiveChildCategories(parentId: Long): List<Category> {
        return categoryRepository.findByParentIdAndIsActiveTrue(parentId)
    }

    override fun getAll(): List<Category> {
        return categoryRepository.findAll()
    }

    override fun getActiveAll(): List<Category> {
        return categoryRepository.findByIsActiveTrue()
    }

    @Transactional
    override fun update(id: Long, name: String, description: String?, sortOrder: Int, isActive: Boolean): Category {
        val category = getById(id)
        category.update(name, description, sortOrder, isActive)
        return category
    }

    @Transactional
    override fun delete(id: Long) {
        val category = getById(id)
        deleteRecursively(category)
    }

    private fun deleteRecursively(category: Category) {
        // 1. 자식 카테고리 재귀 삭제
        val children = categoryRepository.findByParentId(category.id)
        children.forEach { deleteRecursively(it) }

        // 2. 해당 카테고리의 게시글 삭제
        val posts = postRepository.findByCategoryId(category.id)
        postRepository.deleteAll(posts)

        // 3. 카테고리 삭제
        categoryRepository.delete(category)
    }

    @Transactional
    override fun activate(id: Long): Category {
        val category = getById(id)
        category.isActive = true
        return category
    }

    @Transactional
    override fun deactivate(id: Long): Category {
        val category = getById(id)
        category.isActive = false
        return category
    }
}
