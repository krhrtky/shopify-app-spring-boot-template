package com.example.shopifyappspringboottemplate.config

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class HMACVerificationFilter : WebFilter {

    private val HMAC = "hmac"
    private val algorithm = "HmacSHA256"

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.value()
        if (!path.startsWith("/shopifyApp", false)) {
            println("path($path) is not required to be verified.")
            return chain.filter(exchange)
        }

        val hmac = exchange.request.queryParams.toSingleValueMap()[HMAC]
        println("hmac($hmac)")

        val withoutHmac = exchange
            .request
            .queryParams
            .toSingleValueMap()
            .filterNot { it.key == HMAC }
            .toSortedMap()
            .map { "${it.key}=${it.value}" }
            .joinToString("&")

        println("withoutHmac($withoutHmac)")

        val mac = Mac.getInstance(algorithm)
        val keySpec = SecretKeySpec(
            System.getenv("SHOPIFY_APP_API_SECRET").toByteArray(),
            algorithm,
        )
        mac.init(keySpec)

        val signBytes = mac.doFinal(withoutHmac.toByteArray())
        val builder = StringBuilder()
        for (signByte in signBytes) {
            builder.append(String.format("%02x", signByte and 0xff.toByte()))
        }

        return if (builder.toString() == hmac) {
            println("hmac($hmac) is verified.")
            chain.filter(exchange)
        } else {
            println("hmac($hmac), ${builder.toString()} is not verified.")
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return exchange.response.setComplete()
        }
    }
}
