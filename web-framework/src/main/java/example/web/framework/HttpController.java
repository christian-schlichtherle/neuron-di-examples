/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
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
import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * Provides access to the HTTP request/response model.
 * This interface leaks types from the class {@link HttpExchange}, so don't use it in production.
 */
@SuppressWarnings("unused")
@Neuron
public interface HttpController {

    /**
     * Returns the underlying {@code HttpExchange}.
     */
    HttpExchange exchange();

    /**
     * @see HttpExchange#getResponseBody()
     */
    OutputStream responseBody();

    /**
     * Returns the configured routes.
     * The returned map as well as its nested maps are immutable.
     */
    Map<String, Map<HttpMethod, HttpRoute<?>>> routes();

    /**
     * @see HttpExchange#getHttpContext()
     */
    @Caching(NOT_THREAD_SAFE)
    default HttpContext context() {
        return exchange().getHttpContext();
    }

    /**
     * @see HttpExchange#getLocalAddress()
     */
    @Caching(NOT_THREAD_SAFE)
    default InetSocketAddress localAddress() {
        return exchange().getLocalAddress();
    }

    /**
     * @see HttpExchange#getPrincipal()
     */
    @Caching(NOT_THREAD_SAFE)
    default HttpPrincipal principal() {
        return exchange().getPrincipal();
    }

    /**
     * @see HttpExchange#getProtocol()
     */
    @Caching(NOT_THREAD_SAFE)
    default String protocol() {
        return exchange().getProtocol();
    }

    /**
     * @see HttpExchange#getRemoteAddress()
     */
    @Caching(NOT_THREAD_SAFE)
    default InetSocketAddress remoteAddress() {
        return exchange().getRemoteAddress();
    }

    /**
     * @see HttpExchange#getRequestBody()
     */
    @Caching(NOT_THREAD_SAFE)
    default InputStream requestBody() {
        return exchange().getRequestBody();
    }

    /**
     * @see HttpExchange#getRequestHeaders()
     */
    @Caching(NOT_THREAD_SAFE)
    default Headers requestHeaders() {
        return exchange().getRequestHeaders();
    }

    /**
     * Returns the prioritized list of language ranges parsed from the `Accept-Language` header.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.5">RFC 7231 - Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content - 5.3.5. Accept-Language</a>
     */
    @Caching(NOT_THREAD_SAFE)
    default List<Locale> acceptLanguages() {
        return HeadersFun.acceptLanguages(requestHeaders());
    }

    /**
     * @see HttpExchange#getRequestMethod()
     */
    @Caching(NOT_THREAD_SAFE)
    default String requestMethod() {
        return exchange().getRequestMethod();
    }

    /**
     * Returns the first value of the request parameter with the given name if present.
     */
    default Optional<String> requestParam(String name) {
        return requestParameters().getOrDefault(name, emptyList()).stream().findFirst();
    }

    /**
     * Returns the first value of the request parameter with the given name or the given default value if not present.
     */
    default String requestParam(String name, String defaultValue) {
        return requestParam(name).orElse(defaultValue);
    }

    /**
     * Returns an immutable map of the request parameters.
     */
    @Caching(NOT_THREAD_SAFE)
    default Map<String, List<String>> requestParameters() {
        final var params = new LinkedHashMap<String, List<String>>();
        stream(Optional.ofNullable(requestURI().getQuery()).orElse("").split("&")).forEach(p -> {
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
     * @see HttpExchange#getRequestURI()
     */
    @Caching(NOT_THREAD_SAFE)
    default URI requestURI() {
        return exchange().getRequestURI();
    }

    /**
     * @see HttpExchange#getResponseHeaders()
     */
    @Caching(NOT_THREAD_SAFE)
    default Headers responseHeaders() {
        return exchange().getResponseHeaders();
    }

    /**
     * Returns the exception thrown during request processing, if any.
     */
    default Optional<Throwable> throwable() {
        return Optional.ofNullable((Throwable) context().getAttributes().get("throwable"));
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
