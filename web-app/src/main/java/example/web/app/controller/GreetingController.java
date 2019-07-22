/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app.controller;

import example.web.app.dto.Greeting;
import example.web.app.service.api.GreetingService;
import example.web.framework.HttpController;

import java.util.concurrent.ThreadLocalRandom;

// A controller class/interface needs to implement/extend the `HttpController` interface.
// It must not be final and it must have either a non-private, no-argument constructor or no constructor at all.
// A controller can "summon" any dependencies provided by the module object as shown below.
// Yet, this interface does not have any compile-time dependency on the module class or Neuron DI.
public interface GreetingController extends HttpController {

    // This is a "synapse" method, that is, an abstract method without parameters.
    // Each call to this method is delegated to `example.web.app.Module.greetingService()`.
    // You could use the @Caching annotation to do this only once, but a new controller is created for every request,
    // so it wouldn't add any value here.
    // In general, synapse methods do not need to be public.
    GreetingService greetingService();

    default String message() {
        return greetingService().apply(acceptLanguages(), requestParam("who"));
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
        c.responseHeaders().add("X-message", c.message());
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new Exception("The controller has randomly chosen to throw an exception - bad luck!");
        }
        return 204;
    }
}
