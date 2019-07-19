/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

public interface HttpAction<C> {

    int apply(C exchange) throws Exception;
}
