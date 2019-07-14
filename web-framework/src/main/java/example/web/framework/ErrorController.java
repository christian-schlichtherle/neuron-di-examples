/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

import java.util.stream.Collectors;

interface ErrorController<S extends HttpServer<S>> extends HttpExchange<S> {

    default int notFound() {
        textHtmlUtf8().append("<h1>404 Not Found</h1>No handler found for request URI.").flush();
        return 404;
    }

    default int methodNotAllowed() {
        responseHeaders().add("Allow", routes()
                .get(context().getPath())
                .keySet()
                .stream()
                .map(HttpMethod::name)
                .collect(Collectors.joining(", ")));
        textHtmlUtf8().append("<h1>405 Not Allowed</h1>Request method not allowed for this request URI.").flush();
        return 405;
    }

    default int internalServerError() {
        textHtmlUtf8().append("<h1>500 Internal Server Error</h1>\n<pre><code>");
        throwable().ifPresent(t -> t.printStackTrace(textHtmlUtf8()));
        textHtmlUtf8().append("</code></pre>").flush();
        return 500;
    }

    default int notImplemented() {
        textHtmlUtf8().append("<h1>501 Not Implemented</h1>Unknown request method.").flush();
        return 501;
    }
}
