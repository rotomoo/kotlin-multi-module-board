package com.rotomoo.domain.config

import org.hibernate.resource.jdbc.spi.StatementInspector
import org.slf4j.LoggerFactory

class QueryCountInspector : StatementInspector {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val queryCount = ThreadLocal.withInitial { 0 }

        fun getCount(): Int = queryCount.get()

        fun reset() {
            queryCount.set(0)
        }
    }

    override fun inspect(sql: String): String {
        queryCount.set(queryCount.get() + 1)
        log.info("[Query #{}] {}", queryCount.get(), sql.replace("\\s+".toRegex(), " ").take(200))
        return sql
    }
}
