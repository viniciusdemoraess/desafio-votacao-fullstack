package br.com.desafio_votacao.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
public class SwaggerRedirectController {

    @GetMapping("/api-docs/swagger-config")
    public Mono<ResponseEntity<Void>> redirectSwaggerConfig() {
        return Mono.just(ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create("/v3/api-docs/swagger-config"))
                .build());
    }
}
