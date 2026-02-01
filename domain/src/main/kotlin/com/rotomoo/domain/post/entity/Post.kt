package com.rotomoo.domain.post.entity

import com.rotomoo.domain.category.entity.Category
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "post")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @Column(nullable = false, length = 100)
    var author: String,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false)
    var viewCount: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PostStatus = PostStatus.DRAFT,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(title: String, content: String) {
        this.title = title
        this.content = content
        this.updatedAt = LocalDateTime.now()
    }

    fun publish() {
        this.status = PostStatus.PUBLISHED
        this.updatedAt = LocalDateTime.now()
    }

    fun toDraft() {
        this.status = PostStatus.DRAFT
        this.updatedAt = LocalDateTime.now()
    }

    fun delete() {
        this.status = PostStatus.DELETED
        this.updatedAt = LocalDateTime.now()
    }

    fun incrementViewCount() {
        this.viewCount++
    }
}

enum class PostStatus {
    DRAFT,      // 임시저장 (본인만 조회 가능)
    PUBLISHED,  // 게시됨 (전체 공개)
    DELETED     // 삭제됨 (Soft Delete)
}
