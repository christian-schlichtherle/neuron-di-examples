/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app;

import example.web.app.controller.GreetingController;
import example.web.framework.HttpServer;

import java.io.IOException;

@SuppressWarnings("unused")
public class Server implements HttpServer {

    private Server() {
    }

    // This is a dependency provider method, that is, a non-abstract parameter-less method.
    // The synapse method `GreetingController.greeting()` delegates every call to this method.
    // Dependency provider methods do not need to be public.
    String greeting() {
        return "Hello, %s!";
    }

    public static void main(String... args) throws IOException {
        // If you don't want your main class to implement the `HttpServer` interface, then you might as well use an
        // anonymous inner class instead here like so:
        //
        //     new HttpServer() {
        //         String greeting() { return "Hello, %s!"; }
        //     }.with(GreetingController.class)...
        //
        new Server()
                .with(GreetingController.class)
                    .route("/greeting")
                        .get(GreetingController::get)
                        .post(GreetingController::post)
                .start(args.length > 0 ? Integer.parseInt(args[0]) : 8080);
    }
}
