package com.rotomoo.api.config

import com.rotomoo.domain.config.QueryCountInspector
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Profile("local")
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val method = request.method
        val uri = request.requestURI
        val query = request.queryString?.let { "?$it" } ?: ""

        QueryCountInspector.reset()
        log.info(">>> API 시작: {} {}{}", method, uri, query)

        val start = System.currentTimeMillis()
        try {
            filterChain.doFilter(request, response)
        } finally {
            val duration = System.currentTimeMillis() - start
            val queryCount = QueryCountInspector.getCount()
            log.info("<<< API 완료: {} {}{} | 쿼리 {}개 | {}ms | status={}",
                method, uri, query, queryCount, duration, response.status)
        }
    }
}
