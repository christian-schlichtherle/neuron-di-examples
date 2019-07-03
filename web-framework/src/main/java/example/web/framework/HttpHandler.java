/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

import global.namespace.fun.io.bios.BIOS;

import java.lang.reflect.Method;
import java.util.Map;

import static global.namespace.neuron.di.java.Incubator.wire;
import static java.util.Locale.ENGLISH;

interface HttpHandler<T> extends HttpRoute<T> {

    Map<String, Map<HttpMethod, HttpRoute<?>>> routes();

    @SuppressWarnings("unchecked")
    default void apply(final com.sun.net.httpserver.HttpExchange exchange) throws Exception {
        final var response = BIOS.memory();
        final int statusCode = response.applyWriter(out -> {
            final T controller;
            if (HttpExchange.class.isAssignableFrom(controller())) {
                controller = (T) wire((Class<? extends HttpExchange>) controller())
                        .bind(HttpExchange::responseBody).to(out)
                        .bind(HttpExchange::routes).to(this::routes)
                        .using(exchange, HttpHandler::name);
            } else {
                final var delegate = wire(HttpExchange.class)
                        .bind(HttpExchange::responseBody).to(out)
                        .bind(HttpExchange::routes).to(this::routes)
                        .using(exchange, HttpHandler::name);
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

    @SuppressWarnings("StringBufferReplaceableByString")
    private static String name(Method method) {
        final var name = method.getName();
        return new StringBuilder(name.length() + 3)
                .append("get")
                .append(name.substring(0, 1).toUpperCase(ENGLISH))
                .append(name.substring(1))
                .toString();
    }
}
