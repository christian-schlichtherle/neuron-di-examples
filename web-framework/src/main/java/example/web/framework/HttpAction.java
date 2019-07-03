/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework;

public interface HttpAction<T> {

    int apply(T exchange) throws Exception;
}
