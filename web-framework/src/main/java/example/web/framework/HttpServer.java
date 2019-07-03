/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.Neuron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static example.web.framework.HttpMethod.*;
import static global.namespace.neuron.di.java.Incubator.wire;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableMap;

public class HttpServer {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private HttpServer() {
    }

    @SuppressWarnings("unchecked")
    public static <T> WithController<T> with(Class<T> controller) {
        return wire(Reactor.class)
                .bind(Reactor::handlers).to(new HashMap<>())
                .bind(Reactor<T>::controller).to(requireNonNull(controller))
                .bind(Reactor::contextPath).to("/")
                .breed();
    }

    @Neuron
    private interface Reactor<T> extends WithMethod<T> {

        Map<String, Map<HttpMethod, HttpHandler<?>>> handlers();

        Class<T> controller();

        String contextPath();

        @SuppressWarnings("unchecked")
        @Override
        default <U> WithController<U> with(Class<U> controller) {
            return wire(Reactor.class)
                    .bind(Reactor<U>::controller).to(controller)
                    .using(this);
        }

        @SuppressWarnings("unchecked")
        @Override
        default WithContextPath<T> route(final String contextPath) {
            if (!contextPath.startsWith("/")) {
                throw new IllegalArgumentException("`contextPath` needs to start using `/`.");
            }
            return wire(Reactor.class)
                    .bind(Reactor::contextPath).to(contextPath)
                    .using(this);
        }

        @Override
        default WithMethod<T> connect(HttpAction<? super T> action) {
            return invoke(CONNECT, action);
        }

        @Override
        default WithMethod<T> delete(HttpAction<? super T> action) {
            return invoke(DELETE, action);
        }

        @Override
        default WithMethod<T> get(HttpAction<? super T> action) {
            return invoke(GET, action);
        }

        @Override
        default WithMethod<T> head(HttpAction<? super T> action) {
            return invoke(HEAD, action);
        }

        @Override
        default WithMethod<T> options(HttpAction<? super T> action) {
            return invoke(OPTIONS, action);
        }

        @Override
        default WithMethod<T> post(HttpAction<? super T> action) {
            return invoke(POST, action);
        }

        @Override
        default WithMethod<T> put(HttpAction<? super T> action) {
            return invoke(PUT, action);
        }

        @Override
        default WithMethod<T> trace(HttpAction<? super T> action) {
            return invoke(TRACE, action);
        }

        @Override
        default WithMethod<T> notFound(HttpAction<? super T> action) {
            return invoke("404", GET, action);
        }

        @Override
        default WithMethod<T> methodNotAllowed(HttpAction<? super T> action) {
            return invoke("405", GET, action);
        }

        @Override
        default WithMethod<T> internalServerError(HttpAction<? super T> action) {
            return invoke("500", GET, action);
        }

        @Override
        default WithMethod<T> notImplemented(HttpAction<? super T> action) {
            return invoke("501", GET, action);
        }

        default WithMethod<T> invoke(HttpMethod method, HttpAction<? super T> action) {
            return invoke(contextPath(), method, action);
        }

        default WithMethod<T> invoke(
                String contextPath,
                HttpMethod method,
                HttpAction<? super T> action) {
            handlers(contextPath).put(method, handler(contextPath, method, controller(), action));
            return this;
        }

        default Map<HttpMethod, HttpHandler<?>> handlers(String contextPath) {
            return handlers().computeIfAbsent(contextPath, p -> new EnumMap<>(HttpMethod.class));
        }

        @Override
        default void start(final int port) throws IOException {
            final var server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
            final var notFound = handlers("404")
                    .getOrDefault(GET, handler("404", GET, ErrorController.class, ErrorController::notFound));
            final var methodNotAllowed = handlers("405")
                    .getOrDefault(GET, handler("405", GET, ErrorController.class, ErrorController::methodNotAllowed));
            final var internalServerError = handlers("500")
                    .getOrDefault(GET, handler("500", GET, ErrorController.class, ErrorController::internalServerError));
            final var notImplemented = handlers("501")
                    .getOrDefault(GET, handler("501", GET, ErrorController.class, ErrorController::notImplemented));
            handlers("/"); // ensure entry as side effect
            routes().forEach((path, handlers) -> {
                server.createContext(path, exchange -> {
                    try {
                        HttpHandler handler;
                        try {
                            final var result = HttpMethod.valueOf(exchange.getRequestMethod().toUpperCase(ENGLISH));
                            handler = handlers.isEmpty() ? notFound : (HttpHandler<?>) handlers.getOrDefault(result, methodNotAllowed);
                        } catch (IllegalArgumentException noSuchRequestMethod) {
                            handler = notImplemented;
                        }
                        try {
                            handler.apply(exchange);
                        } catch (final Throwable t1) {
                            final var attributes = exchange.getHttpContext().getAttributes();
                            attributes.put("throwable", t1);
                            exchange.getResponseHeaders().clear();
                            try {
                                internalServerError.apply(exchange);
                            } catch (Throwable t2) {
                                t1.addSuppressed(t2);
                            }
                            throw t1;
                        }
                    } catch (Throwable t) {
                        log.error("Internal Server Error:", t);
                    }
                    exchange.close();
                });
            });
            server.start();
            log.info("Serving HTTP/1.1 on port {}.", port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Initiating shutdown.");
                server.stop(Integer.MAX_VALUE);
                log.info("Shutdown completed.");
            }));
        }

        @SuppressWarnings("unchecked")
        default <U> HttpHandler<U> handler(
                String contextPath,
                HttpMethod method,
                Class<U> controller,
                HttpAction<? super U> action
        ) {
            return wire(HttpHandler.class)
                    .bind(HttpHandler::contextPath).to(contextPath)
                    .bind(HttpHandler::method).to(method)
                    .bind(HttpHandler<U>::controller).to(controller)
                    .bind(HttpHandler::action).to(() -> (HttpAction) action)
                    .using(this);
        }

        @Caching
        default Map<String, Map<HttpMethod, HttpRoute<?>>> routes() {
            return handlers()
                    .entrySet()
                    .stream()
                    .filter(e1 -> e1.getKey().startsWith("/"))
                    .collect(toUnmodifiableMap(Map.Entry::getKey, e -> Map.copyOf(e.getValue())));
        }
    }

    public interface WithController<T> {

        WithContextPath<T> route(String contextPath);

        default WithMethod<T> routeConnect(String contextPath, HttpAction<? super T> action) {
            return route(contextPath).connect(action);
        }

        default WithMethod<T> routeDelete(String contextPath, HttpAction<? super T> action) {
            return route(contextPath).delete(action);
        }

        default WithMethod<T> routeGet(String contextPath, HttpAction<? super T> action) {
            return route(contextPath).get(action);
        }

        default WithMethod<T> routeHead(String contextPath, HttpAction<? super T> action) {
            return route(contextPath).head(action);
        }

        default WithMethod<T> routeOptions(String contextPath, HttpAction<? super T> action) {
            return route(contextPath).options(action);
        }

        default WithMethod<T> routePost(String contextPath, HttpAction<? super T> action) {
            return route(contextPath).post(action);
        }

        default WithMethod<T> routePut(String contextPath, HttpAction<? super T> action) {
            return route(contextPath).put(action);
        }

        default WithMethod<T> routeTrace(String contextPath, HttpAction<? super T> action) {
            return route(contextPath).trace(action);
        }

        WithMethod<T> notFound(HttpAction<? super T> action);

        WithMethod<T> methodNotAllowed(HttpAction<? super T> action);

        WithMethod<T> internalServerError(HttpAction<? super T> action);

        WithMethod<T> notImplemented(HttpAction<? super T> action);
    }

    public interface WithContextPath<T> {

        WithMethod<T> connect(HttpAction<? super T> action);

        WithMethod<T> delete(HttpAction<? super T> action);

        WithMethod<T> get(HttpAction<? super T> action);

        WithMethod<T> head(HttpAction<? super T> action);

        WithMethod<T> options(HttpAction<? super T> action);

        WithMethod<T> post(HttpAction<? super T> action);

        WithMethod<T> put(HttpAction<? super T> action);

        WithMethod<T> trace(HttpAction<? super T> action);
    }

    public interface WithMethod<T> extends WithController<T>, WithContextPath<T> {

        <U> WithController<U> with(Class<U> controller);

        void start(int port) throws IOException;
    }
}
