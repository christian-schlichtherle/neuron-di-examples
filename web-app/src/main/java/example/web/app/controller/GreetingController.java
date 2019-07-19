/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app.controller;

import example.web.app.dto.Greeting;
import example.web.framework.HttpController;

import java.util.concurrent.ThreadLocalRandom;

// A controller needs to extend/implement the `HttpController` interface.
// It must not be final and it needs to have either a non-private, no-argument constructor or no constructor at all.
// It can also "summon" any dependencies provided by the server object as shown below.
public interface GreetingController extends HttpController {

    // This is a synapse method, that is an abstract, parameter-less method.
    // Any call to this method gets delegated to the dependency provider method `Server.greeting()`.
    // This method could also be package-private if it was defined in an abstract class.
    String greeting();

    default String message() {
        return String.format(greeting(), requestParam("who", "world"));
    }

    // A controller method can be a virtual method with no parameter...
    default int get() throws Exception {
        final var g = new Greeting();
        g.message = message();
        applicationJson().encode(g);
        return 200;
    }

    // ... or a static method with the declaring class or any of its super types as its parameter.
    static int post(final GreetingController c) throws Exception {
        c.responseHeaders().add("Expires", "0");
        c.responseHeaders().add("X-greeting", c.message());
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new Exception("The controller has randomly chosen to throw an exception - bad luck!");
        }
        return 204;
    }
}
