package com.fleetapp.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Logs one line per request: method, path, route, status and duration.
 *
 * Deliberately never logs the query string, the headers or the body. The Authorization
 * header carries the JWT, and the bodies of the auth endpoints carry OTPs and refresh
 * tokens — none of that belongs in a log file.
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.nanoTime();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange).doOnSuccess(a -> {
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            long millis = (System.nanoTime() - start) / 1_000_000;
            String route = exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            log.info("{} {} -> {} [{}] {} ms", method, path, route, status, millis);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
