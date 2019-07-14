/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

import java.io.IOException;
import java.util.HashMap;

import static global.namespace.neuron.di.java.Incubator.wire;
import static java.util.Objects.requireNonNull;

public interface HttpServer<S extends HttpServer<S>> {

    @SuppressWarnings("unchecked")
    default <T> WithController<T> with(Class<T> controller) {
        return wire(HttpService.class)
                .bind(HttpService<S, T>::handlers).to(new HashMap<>())
                .bind(HttpService<S, T>::controller).to(requireNonNull(controller))
                .bind(HttpService::contextPath).to("/")
                .bind(HttpService::server).to(this)
                .breed();
    }

    interface WithController<T> {

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

    interface WithContextPath<T> {

        WithMethod<T> connect(HttpAction<? super T> action);

        WithMethod<T> delete(HttpAction<? super T> action);

        WithMethod<T> get(HttpAction<? super T> action);

        WithMethod<T> head(HttpAction<? super T> action);

        WithMethod<T> options(HttpAction<? super T> action);

        WithMethod<T> post(HttpAction<? super T> action);

        WithMethod<T> put(HttpAction<? super T> action);

        WithMethod<T> trace(HttpAction<? super T> action);
    }

    interface WithMethod<T> extends WithController<T>, WithContextPath<T> {

        <U> WithController<U> with(Class<U> controller);

        void start(int port) throws IOException;
    }
}
