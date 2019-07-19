/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
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
@SuppressWarnings("unused")
@Neuron(cachingStrategy = NOT_THREAD_SAFE)
public interface HttpExchange {

    /**
     * @see com.sun.net.httpserver.HttpExchange#getHttpContext()
     */
    @Caching(NOT_THREAD_SAFE)
    default HttpContext context() {
        return underlying().getHttpContext();
    }

    /**
     * @see com.sun.net.httpserver.HttpExchange#getLocalAddress()
     */
    @Caching(NOT_THREAD_SAFE)
    default InetSocketAddress localAddress() {
        return underlying().getLocalAddress();
    }

    /**
     * @see com.sun.net.httpserver.HttpExchange#getPrincipal()
     */
    @Caching(NOT_THREAD_SAFE)
    default HttpPrincipal principal() {
        return underlying().getPrincipal();
    }

    /**
     * @see com.sun.net.httpserver.HttpExchange#getProtocol()
     */
    @Caching(NOT_THREAD_SAFE)
    default String protocol() {
        return underlying().getProtocol();
    }

    /**
     * @see com.sun.net.httpserver.HttpExchange#getRemoteAddress()
     */
    @Caching(NOT_THREAD_SAFE)
    default InetSocketAddress remoteAddress() {
        return underlying().getRemoteAddress();
    }

    /**
     * @see com.sun.net.httpserver.HttpExchange#getRequestBody()
     */
    @Caching(NOT_THREAD_SAFE)
    default InputStream requestBody() {
        return underlying().getRequestBody();
    }

    /**
     * @see com.sun.net.httpserver.HttpExchange#getRequestHeaders()
     */
    @Caching(NOT_THREAD_SAFE)
    default Headers requestHeaders() {
        return underlying().getRequestHeaders();
    }

    /**
     * @see com.sun.net.httpserver.HttpExchange#getRequestMethod()
     */
    @Caching(NOT_THREAD_SAFE)
    default String requestMethod() {
        return underlying().getRequestMethod();
    }

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
    @Caching(NOT_THREAD_SAFE)
    default URI requestURI() {
        return underlying().getRequestURI();
    }

    /**
     * @see com.sun.net.httpserver.HttpExchange#getResponseBody()
     */
    OutputStream responseBody();

    /**
     * @see com.sun.net.httpserver.HttpExchange#getResponseHeaders()
     */
    @Caching(NOT_THREAD_SAFE)
    default Headers responseHeaders() {
        return underlying().getResponseHeaders();
    }

    /**
     * Returns the configured routes.
     * The returned map as well as its nested maps are immutable.
     */
    Map<String, Map<HttpMethod, HttpRoute<?>>> routes();

    /**
     * Returns the exception thrown during request processing, if any.
     */
    default Optional<Throwable> throwable() {
        return ofNullable((Throwable) context().getAttributes().get("throwable"));
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

    com.sun.net.httpserver.HttpExchange underlying();

    private PrintWriter withContentType(final String value, final Charset charset) {
        responseHeaders().add("Content-Type", value + "; charset=" + charset.name());
        return new PrintWriter(responseBody(), true, charset);
    }
}
