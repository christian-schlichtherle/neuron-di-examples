/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app;

import example.web.app.controller.GreetingController;
import example.web.framework.HttpServer;

import java.io.IOException;

import static global.namespace.neuron.di.java.Incubator.breed;

// Neuron types should be abstract in order to discourage anybody to "new" them - this would ignore the @Caching
// annotation.
public abstract class Main extends Module implements HttpServer {

    public static void main(String... args) throws IOException {
        // We need to "breed" this class so that the `@Caching` annotations in the `Module` interface are effective.
        // After breeding, we use the fluent API of the mixed-in `HttpServer` interface to configure the HTTP server
        // and start it.
        // At runtime, the server instantiates a new `GreetingController` for every request and delegates any
        // dependencies to itself, that is, its `Module` superclass.
        breed(Main.class)
                .with(GreetingController.class)
                    .route("/greeting")
                        .get(GreetingController::get)
                        .post(GreetingController::post)
                .start(args.length > 0 ? Integer.parseInt(args[0]) : 8080);
    }
}
