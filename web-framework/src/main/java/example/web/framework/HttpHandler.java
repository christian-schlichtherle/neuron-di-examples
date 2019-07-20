/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

import com.sun.net.httpserver.HttpExchange;
import global.namespace.fun.io.bios.BIOS;

import java.util.Map;

import static global.namespace.neuron.di.java.Incubator.wire;

interface HttpHandler<C extends HttpController> extends HttpRoute<C> {

    Map<String, Map<HttpMethod, HttpRoute<?>>> routes();

    HttpServer server();

    default void apply(final HttpExchange exchange) throws Exception {
        final var response = BIOS.memory();
        final int statusCode = response.applyWriter(responseBody ->
                action().apply(
                        wire(controller())
                                .bind(HttpController::exchange).to(exchange)
                                .bind(HttpController::responseBody).to(responseBody)
                                .bind(HttpController::routes).to(this::routes)
                                .using(server())
                )
        );
        final var responseLength = response.size().orElse(-1);
        if (responseLength <= 0) {
            exchange.sendResponseHeaders(statusCode, -1);
        } else {
            exchange.sendResponseHeaders(statusCode, responseLength);
            BIOS.copy(response, BIOS.stream(exchange.getResponseBody()));
        }
    }
}
