/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app.controller;

import example.web.app.dto.Greeting;
import example.web.framework.HttpExchange;

import java.util.concurrent.ThreadLocalRandom;

public interface GreetingController extends HttpExchange {

    // A controller method can be a virtual method with no parameter...
    default int get() throws Exception {
        final var g = new Greeting();
        g.message = "Hello, " + requestParam("who", "world") + '!';
        applicationJson().encode(g);
        return 200;
    }

    // ... or a static method with the declaring class or any of its super types as its parameter.
    // In this case, `HttpExchange` is used because it's a super-interface of `GreetingController` and the latter does
    // not add anything but the controller methods.
    static int post(final HttpExchange x) throws Exception {
        x.responseHeaders().add("Expires", "0");
        x.responseHeaders().add("X-greeting", "Hello, " + x.requestParam("who", "world") + '!');
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new Exception("The controller has chosen to throw a random exception - bad luck!");
        }
        return 204;
    }
}
