/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app.controller;

import example.web.app.Main;
import example.web.app.dto.Greeting;
import example.web.framework.HttpExchange;

import java.util.concurrent.ThreadLocalRandom;

// A controller can be an interface or a class.
// It must not be final and it needs to have either a non-private, no-argument constructor or no constructor at all.
// It does not need to extend or implement a particular class or interface.
// However, extending or implementing the `HttpExchange` interface is the most convenient way to get access to the
// dependencies which a controller requires to produce any output.
public interface GreetingController extends HttpExchange {

    // This is a synapse method.
    // It's return value gets injected from the dependency provider method `Main.greeting()`:
    String greeting();

    // A controller method can be a virtual method with no parameter...
    default int get() throws Exception {
        final var g = new Greeting();
        g.message = String.format(greeting(), requestParam("who", "world"));
        applicationJson().encode(g);
        return 200;
    }

    // ... or a static method with the declaring class or any of its super types as its parameter.
    // For this method, `HttpExchange` is used because it's a super-interface of `GreetingController` and the method
    // doesn't need anything from that.
    static int post(final HttpExchange x) throws Exception {
        x.responseHeaders().add("Expires", "0");
        x.responseHeaders().add("X-greeting", "Hello, " + x.requestParam("who", "world") + '!');
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new Exception("The controller has chosen to throw a random exception - bad luck!");
        }
        return 204;
    }
}
