package com.example.shopifyappspringboottemplate.controllers

import io.netty.handler.codec.http.HttpResponseStatus
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.netty.http.server.HttpServerResponse

@RestController
@RequestMapping("/shopifyApp")
class ShopifyAppController {

    @GetMapping("/install")
    fun install(
        @RequestParam("shop") shop: String?,
        response: HttpServerResponse,
    ): Mono<ServerResponse> {

        println("shop: $shop")

        return if (shop == null) {
            ServerResponse.status(HttpStatus.BAD_REQUEST).build()
        } else {
            val redirectUrl =
                UriComponentsBuilder.fromHttpUrl("https://$shop/admin/oauth/authorize")
                    .queryParam("client_id", System.getenv("SHOPIFY_APP_API_KEY"))
                    .queryParam("scope", "read_products,write_products")
                    .queryParam("redirect_uri", "https://${System.getenv("SHOPIFY_APP_DOMAIN")}/shopifyApp/callback")
                    .build()
                    .toUriString()

            ServerResponse
                .status(HttpStatus.TEMPORARY_REDIRECT)
                .header("Location", redirectUrl)
                .build()
        }
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam("shop") shop: String?,
        @RequestParam("code") code: String?,
        response: HttpServerResponse,
    ): Mono<Void> = if (shop == null || code == null) {
        response.sendNotFound()
    } else {

        response.sendRedirect("https://$shop/admin/apps")
    }
}
