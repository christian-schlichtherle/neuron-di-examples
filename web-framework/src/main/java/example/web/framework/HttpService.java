package example.web.framework;

import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.Neuron;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EnumMap;
import java.util.Map;

import static example.web.framework.HttpMethod.*;
import static global.namespace.neuron.di.java.Incubator.wire;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static example.web.framework.HttpServer.*;

@Neuron
interface HttpService<S extends HttpServer<S>, T> extends WithMethod<T> {

    Map<String, Map<HttpMethod, HttpHandler<S, ?>>> handlers();

    Class<T> controller();

    String contextPath();

    S server();

    @SuppressWarnings("unchecked")
    @Override
    default <U> WithController<U> with(Class<U> controller) {
        return wire(HttpService.class)
                .bind(HttpService<S, U>::controller).to(controller)
                .using(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    default WithContextPath<T> route(final String contextPath) {
        if (!contextPath.startsWith("/")) {
            throw new IllegalArgumentException("`contextPath` needs to start using `/`.");
        }
        return wire(HttpService.class)
                .bind(HttpService::contextPath).to(contextPath)
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

    default Map<HttpMethod, HttpHandler<S, ?>> handlers(String contextPath) {
        return handlers().computeIfAbsent(contextPath, p -> new EnumMap<>(HttpMethod.class));
    }

    @SuppressWarnings("unchecked")
    @Override
    default void start(final int port) throws IOException {
        final var log = LoggerFactory.getLogger(HttpService.class);
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
        //noinspection CodeBlock2Expr
        routes().forEach((path, handlers) -> {
            server.createContext(path, exchange -> {
                try {
                    HttpHandler handler;
                    try {
                        final var result = HttpMethod.valueOf(exchange.getRequestMethod().toUpperCase(ENGLISH));
                        handler = handlers.isEmpty() ? notFound : (HttpHandler<S, ?>) handlers.getOrDefault(result, methodNotAllowed);
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
    default <U> HttpHandler<S, U> handler(
            String contextPath,
            HttpMethod method,
            Class<U> controller,
            HttpAction<? super U> action
    ) {
        return wire(HttpHandler.class)
                .bind(HttpHandler::contextPath).to(contextPath)
                .bind(HttpHandler::method).to(method)
                .bind(HttpHandler<S, U>::controller).to(controller)
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
