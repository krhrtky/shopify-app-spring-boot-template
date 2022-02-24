package com.example.shopifyappspringboottemplate.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.server.WebSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI
import kotlin.random.Random

@RestController
@RequestMapping("/shopifyApp")
class ShopifyAppController {

    @GetMapping("/install")
    fun install(
        @RequestParam(value = "shop") shop: String?,
        session: WebSession,
    ): Mono<ResponseEntity<Void>> = if (shop == null) {
        Mono.just(ResponseEntity.badRequest().build())
    } else {
        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        val randomString = (1..20)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

        session.attributes["state"] = randomString

        val redirectUrl =
            UriComponentsBuilder.fromHttpUrl("https://$shop/admin/oauth/authorize")
                .queryParam("client_id", System.getenv("SHOPIFY_APP_API_KEY"))
                .queryParam("scope", "read_products,write_products")
                .queryParam("redirect_uri", "${System.getenv("SHOPIFY_APP_DOMAIN")}/shopifyApp/callback")
                .queryParam("state", randomString)
                .build()
                .toUri()

        Mono.just(
            ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .location(redirectUrl)
                .build()
        )
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam("shop") shop: String?,
        @RequestParam("code") code: String?,
        session: WebSession,
    ): Mono<ResponseEntity<Void>> = if (shop == null || code == null) {
        Mono.just(ResponseEntity.badRequest().build())
    } else {
        WebClient
            .create()
            .post()
            .uri("https://$shop/admin/oauth/access_token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "code" to code,
                    "client_id" to System.getenv("SHOPIFY_APP_API_KEY"),
                    "client_secret" to System.getenv("SHOPIFY_APP_API_SECRET"),
                )
            )
            .retrieve()
            .bodyToMono<HashMap<String, String>>()
            .doOnNext {
                session.attributes.remove("state")
            }
            .log()
            .thenReturn(
                ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .location(URI("https://$shop/admin/apps"))
                    .build()
            )
    }
}
