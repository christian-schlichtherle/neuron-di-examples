/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

public interface HttpRoute<T> {

    String contextPath();

    HttpMethod method();

    Class<T> controller();

    HttpAction<? super T> action();
}
