/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import global.namespace.fun.io.api.Encoder;
import global.namespace.fun.io.bios.BIOS;
import global.namespace.fun.io.jackson.Jackson;
import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.Neuron;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

import static global.namespace.neuron.di.java.CachingStrategy.NOT_THREAD_SAFE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * Provides access to the HTTP request/response model available for injection in controller classes.
 * This is a subset of the abstract class {@link com.sun.net.httpserver.HttpsExchange}, with the addition of some
 * default methods.
 * <p>
 * A controller may simply extend this interface or selectively "summon" any method provided by this interface by adding
 * an abstract, parameter-less method of the same name and the same return type (or any of its super-types).
 * For example:
 * <pre>{@code
 * public interface IndexController {
 *
 *     // "Summons" a PrintWriter to produce a `text/plain; charset=UTF-8` response:
 *     PrintWriter textPlainUtf8();
 *
 *     // The actual controller method:
 *     default int index() {
 *         textPlainUtf8().println("Hello, world!");
 *         return 200;
 *     }
 * }
 * }</pre>
 */
@Neuron(cachingStrategy = NOT_THREAD_SAFE)
public interface HttpExchange {

    /**
     * @see com.sun.net.httpserver.HttpExchange#getHttpContext()
     */
    HttpContext httpContext();

    /**
     * @see com.sun.net.httpserver.HttpExchange#getLocalAddress()
     */
    InetSocketAddress localAddress();

    /**
     * @see com.sun.net.httpserver.HttpExchange#getPrincipal()
     */
    HttpPrincipal principal();

    /**
     * @see com.sun.net.httpserver.HttpExchange#getProtocol()
     */
    String protocol();

    /**
     * @see com.sun.net.httpserver.HttpExchange#getRemoteAddress()
     */
    InetSocketAddress remoteAddress();

    /**
     * @see com.sun.net.httpserver.HttpExchange#getRequestBody()
     */
    InputStream requestBody();

    /**
     * @see com.sun.net.httpserver.HttpExchange#getRequestHeaders()
     */
    Headers requestHeaders();

    /**
     * @see com.sun.net.httpserver.HttpExchange#getRequestMethod()
     */
    String requestMethod();

    /**
     * Returns the first value of the request parameter with the given name or the given default value if not present.
     */
    default String requestParam(String name, String defaultValue) {
        return requestParameters().getOrDefault(name, emptyList()).stream().findFirst().orElse(defaultValue);
    }

    /**
     * Returns an immutable map of the request parameters.
     */
    @Caching(NOT_THREAD_SAFE)
    default Map<String, List<String>> requestParameters() {
        final var params = new LinkedHashMap<String, List<String>>();
        stream(ofNullable(requestURI().getQuery()).orElse("").split("&")).forEach(p -> {
            final var ps = p.split("=");
            final var lists = params.computeIfAbsent(ps[0], k -> new LinkedList<>());
            if (ps.length > 1) {
                lists.add(ps[1]);
            }
        });
        return params
                .entrySet()
                .stream()
                .collect(toUnmodifiableMap(Map.Entry::getKey, e -> unmodifiableList(e.getValue())));
    }

    /**
     * @see com.sun.net.httpserver.HttpExchange#getRequestURI()
     */
    URI requestURI();

    /**
     * @see com.sun.net.httpserver.HttpExchange#getResponseBody()
     */
    OutputStream responseBody();

    /**
     * @see com.sun.net.httpserver.HttpExchange#getResponseHeaders()
     */
    Headers responseHeaders();

    /**
     * Returns the configured routes.
     * The returned map as well as its nested maps are immutable.
     */
    Map<String, Map<HttpMethod, HttpRoute<?>>> routes();

    /**
     * Returns the exception thrown during request processing, if any.
     */
    default Optional<Throwable> throwable() {
        return ofNullable((Throwable) httpContext().getAttributes().get("throwable"));
    }

    @Caching(NOT_THREAD_SAFE)
    default Encoder applicationJson() {
        responseHeaders().add("Content-Type", "application/json");
        return Jackson.json(objectMapper()).encoder(BIOS.stream(responseBody()));
    }

    @Caching(NOT_THREAD_SAFE)
    default ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Adds the header {@code Content-Type: text/plain; charset=UTF-8} and returns a {@code PrintWriter} for output.
     * It's the caller's responsibility to {@linkplain PrintWriter#flush() flush} the returned {@code PrintWriter}.
     * However, the {@code println}, {@code printf}, and {@code format} methods will flush the output buffer themselves.
     */
    @Caching(NOT_THREAD_SAFE)
    default PrintWriter textPlainUtf8() {
        return withContentType("text/plain", UTF_8);
    }

    /**
     * Adds the header {@code Content-Type: text/html; charset=UTF-8} and returns a {@code PrintWriter} for output.
     * It's the caller's responsibility to {@linkplain PrintWriter#flush() flush} the returned {@code PrintWriter}.
     * However, the {@code println}, {@code printf}, and {@code format} methods will flush the output buffer themselves.
     */
    @Caching(NOT_THREAD_SAFE)
    default PrintWriter textHtmlUtf8() {
        return withContentType("text/html", UTF_8);
    }

    private PrintWriter withContentType(final String value, final Charset charset) {
        responseHeaders().add("Content-Type", value + "; charset=" + charset.name());
        return new PrintWriter(responseBody(), true, charset);
    }
}
