/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

public interface HttpRoute<C extends HttpController> {

    HttpAction<? super C> action();

    String contextPath();

    Class<C> controller();

    HttpMethod method();
}
