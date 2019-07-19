/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

public interface HttpRoute<C> {

    String contextPath();

    HttpMethod method();

    Class<C> controller();

    HttpAction<? super C> action();
}
