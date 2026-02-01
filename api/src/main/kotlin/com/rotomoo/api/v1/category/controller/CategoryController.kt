package com.rotomoo.api.v1.category.controller

import com.rotomoo.api.v1.category.dto.*
import com.rotomoo.api.v1.category.facade.CategoryFacade
import com.rotomoo.api.common.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/categories")
class CategoryController(
    private val categoryFacade: CategoryFacade
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody request: CategoryCreateRequest
    ): ApiResponse<CategoryResponse> {
        return ApiResponse.success(categoryFacade.create(request))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long
    ): ApiResponse<CategoryResponse> {
        return ApiResponse.success(categoryFacade.getById(id))
    }

    // 기획서: GET /api/v1/categories - 전체 카테고리 트리 조회
    @GetMapping
    fun getCategoryTree(): ApiResponse<List<CategoryWithChildrenResponse>> {
        return ApiResponse.success(categoryFacade.getCategoryTree())
    }

    // 관리자용: 모든 카테고리 플랫 리스트
    @GetMapping("/all")
    fun getAll(): ApiResponse<List<CategoryResponse>> {
        return ApiResponse.success(categoryFacade.getAll())
    }

    @GetMapping("/{parentId}/children")
    fun getChildCategories(@PathVariable parentId: Long): ApiResponse<List<CategoryResponse>> {
        return ApiResponse.success(categoryFacade.getChildCategories(parentId))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: CategoryUpdateRequest
    ): ApiResponse<CategoryResponse> {
        return ApiResponse.success(categoryFacade.update(id, request))
    }

    @PatchMapping("/{id}/activate")
    fun activate(@PathVariable id: Long): ApiResponse<CategoryResponse> {
        return ApiResponse.success(categoryFacade.activate(id))
    }

    @PatchMapping("/{id}/deactivate")
    fun deactivate(@PathVariable id: Long): ApiResponse<CategoryResponse> {
        return ApiResponse.success(categoryFacade.deactivate(id))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        categoryFacade.delete(id)
    }
}
