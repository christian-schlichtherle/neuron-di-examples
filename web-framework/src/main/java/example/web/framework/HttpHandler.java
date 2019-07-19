/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

import global.namespace.fun.io.bios.BIOS;

import java.util.Map;

import static global.namespace.neuron.di.java.Incubator.wire;

interface HttpHandler<C> extends HttpRoute<C> {

    Map<String, Map<HttpMethod, HttpRoute<?>>> routes();

    Object server();

    @SuppressWarnings("unchecked")
    default void apply(final com.sun.net.httpserver.HttpExchange exchange) throws Exception {
        final var response = BIOS.memory();
        final int statusCode = response.applyWriter(out -> {
            final C controller;
            if (HttpExchange.class.isAssignableFrom(controller())) {
                controller = (C) wire((Class<? extends HttpExchange>) controller())
                        .bind(HttpExchange::responseBody).to(out)
                        .bind(HttpExchange::routes).to(this::routes)
                        .bind(HttpExchange::underlying).to(exchange)
                        .using(server());
            } else {
                final var delegate = wire(HttpExchange.class)
                        .bind(HttpExchange::responseBody).to(out)
                        .bind(HttpExchange::routes).to(this::routes)
                        .bind(HttpExchange::underlying).to(exchange)
                        .using(server());
                controller = wire(controller()).using(delegate);
            }
            return action().apply(controller);
        });
        final var responseLength = response.size().orElse(-1);
        if (responseLength <= 0) {
            exchange.sendResponseHeaders(statusCode, -1);
        } else {
            exchange.sendResponseHeaders(statusCode, responseLength);
            BIOS.copy(response, BIOS.stream(exchange.getResponseBody()));
        }
    }
}
