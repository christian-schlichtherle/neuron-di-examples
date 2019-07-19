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
interface HttpService<C> extends WithMethod<C> {

    Map<String, Map<HttpMethod, HttpHandler<?>>> handlers();

    Class<C> controller();

    String contextPath();

    Object server();

    @SuppressWarnings("unchecked")
    @Override
    default <D> WithController<D> with(Class<D> controller) {
        return wire(HttpService.class)
                .bind(HttpService<D>::controller).to(controller)
                .using(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    default WithContextPath<C> route(final String contextPath) {
        if (!contextPath.startsWith("/")) {
            throw new IllegalArgumentException("`contextPath` needs to start using `/`.");
        }
        return wire(HttpService.class)
                .bind(HttpService::contextPath).to(contextPath)
                .using(this);
    }

    @Override
    default WithMethod<C> connect(HttpAction<? super C> action) {
        return invoke(CONNECT, action);
    }

    @Override
    default WithMethod<C> delete(HttpAction<? super C> action) {
        return invoke(DELETE, action);
    }

    @Override
    default WithMethod<C> get(HttpAction<? super C> action) {
        return invoke(GET, action);
    }

    @Override
    default WithMethod<C> head(HttpAction<? super C> action) {
        return invoke(HEAD, action);
    }

    @Override
    default WithMethod<C> options(HttpAction<? super C> action) {
        return invoke(OPTIONS, action);
    }

    @Override
    default WithMethod<C> post(HttpAction<? super C> action) {
        return invoke(POST, action);
    }

    @Override
    default WithMethod<C> put(HttpAction<? super C> action) {
        return invoke(PUT, action);
    }

    @Override
    default WithMethod<C> trace(HttpAction<? super C> action) {
        return invoke(TRACE, action);
    }

    @Override
    default WithMethod<C> notFound(HttpAction<? super C> action) {
        return invoke("404", GET, action);
    }

    @Override
    default WithMethod<C> methodNotAllowed(HttpAction<? super C> action) {
        return invoke("405", GET, action);
    }

    @Override
    default WithMethod<C> internalServerError(HttpAction<? super C> action) {
        return invoke("500", GET, action);
    }

    @Override
    default WithMethod<C> notImplemented(HttpAction<? super C> action) {
        return invoke("501", GET, action);
    }

    default WithMethod<C> invoke(HttpMethod method, HttpAction<? super C> action) {
        return invoke(contextPath(), method, action);
    }

    default WithMethod<C> invoke(
            String contextPath,
            HttpMethod method,
            HttpAction<? super C> action) {
        handlers(contextPath).put(method, handler(contextPath, method, controller(), action));
        return this;
    }

    default Map<HttpMethod, HttpHandler<?>> handlers(String contextPath) {
        return handlers().computeIfAbsent(contextPath, p -> new EnumMap<>(HttpMethod.class));
    }

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
                    HttpHandler<?> handler;
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
    default <D> HttpHandler<D> handler(
            String contextPath,
            HttpMethod method,
            Class<D> controller,
            HttpAction<? super D> action
    ) {
        return wire(HttpHandler.class)
                .bind(HttpHandler::contextPath).to(contextPath)
                .bind(HttpHandler::method).to(method)
                .bind(HttpHandler<D>::controller).to(controller)
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
