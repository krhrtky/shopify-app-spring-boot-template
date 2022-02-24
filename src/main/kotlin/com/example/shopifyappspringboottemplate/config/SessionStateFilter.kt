package com.example.shopifyappspringboottemplate.config

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class SessionStateFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.value()
        if (path != "/shopifyApp/callback") {
            return chain.filter(exchange)
        }

        val requestParamState = exchange.request.queryParams.toSingleValueMap()["state"]
        return exchange
            .session
            .map { it.getAttribute<String>("state") ?: "" }
            .flatMap {
                if (requestParamState == it) {
                    chain.filter(exchange)
                } else {
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    exchange.response.setComplete()
                }
            }
            .toMono()
    }
}
