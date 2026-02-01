package com.rotomoo.api.v1.post.controller

import com.rotomoo.api.v1.post.dto.*
import com.rotomoo.api.v1.post.facade.PostFacade
import com.rotomoo.api.common.response.ApiResponse
import com.rotomoo.api.common.response.PagedApiResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class PostController(
    private val postFacade: PostFacade
) {
    // 게시글 생성 - 기획서: POST /api/v1/posts
    // TODO: memberId는 실제로는 인증에서 가져와야 함. 현재는 request body에 포함
    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody request: PostCreateRequest
    ): ApiResponse<PostResponse> {
        return ApiResponse.success(postFacade.create(request))
    }

    // 게시글 목록 조회 (페이징) - 기획서: GET /api/v1/posts
    @GetMapping("/posts")
    fun findAll(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false, defaultValue = "all") searchType: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): PagedApiResponse<PostListResponse> {
        val page = if (keyword.isNullOrBlank()) {
            postFacade.findAll(pageable)
        } else {
            postFacade.search(keyword, searchType, pageable)
        }
        return PagedApiResponse.success(page) { it }
    }

    // 카테고리별 게시글 조회 - 기획서: GET /api/v1/categories/{categoryId}/posts
    @GetMapping("/categories/{categoryId}/posts")
    fun findByCategoryId(
        @PathVariable categoryId: Long,
        @PageableDefault(size = 20) pageable: Pageable
    ): PagedApiResponse<PostListResponse> {
        val page = postFacade.findByCategoryId(categoryId, pageable)
        return PagedApiResponse.success(page) { it }
    }

    // 게시글 상세 조회 - 기획서: GET /api/v1/posts/{id}
    @GetMapping("/posts/{id}")
    fun getById(@PathVariable id: Long): ApiResponse<PostResponse> {
        return ApiResponse.success(postFacade.getById(id))
    }

    // 게시글 수정 - 기획서: PUT /api/v1/posts/{id}
    @PutMapping("/posts/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: PostUpdateRequest
    ): ApiResponse<PostResponse> {
        return ApiResponse.success(postFacade.update(id, request))
    }

    // 게시글 상태 변경
    @PatchMapping("/posts/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: PostStatusUpdateRequest
    ): ApiResponse<PostResponse> {
        return ApiResponse.success(postFacade.updateStatus(id, request.status))
    }

    // 게시글 삭제 - 기획서: DELETE /api/v1/posts/{id}
    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        postFacade.delete(id)
    }
}
