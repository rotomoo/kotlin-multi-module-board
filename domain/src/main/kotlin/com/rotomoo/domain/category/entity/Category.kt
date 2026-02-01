package com.rotomoo.domain.category.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "category")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false)
    var sortOrder: Int = 0,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(name: String, description: String?, sortOrder: Int, isActive: Boolean) {
        this.name = name
        this.description = description
        this.sortOrder = sortOrder
        this.isActive = isActive
        this.updatedAt = LocalDateTime.now()
    }

    fun isParentCategory(): Boolean = parent == null

    fun isChildCategory(): Boolean = parent != null
}
