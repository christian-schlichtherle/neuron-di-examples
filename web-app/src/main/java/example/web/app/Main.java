/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app;

import example.web.app.controller.GreetingController;
import example.web.framework.HttpServer;

import java.io.IOException;

@SuppressWarnings("unused")
public class Main implements HttpServer {

    private Main() {
    }

    // This is a dependency provider method.
    // It's return value gets injected into the synapse method `GreetingController.greeting()`.
    public String greeting() {
        return "Hello, %s!";
    }

    public static void main(String... args) throws IOException {
        new Main()
                .with(GreetingController.class)
                    .route("/greeting")
                        .get(GreetingController::get)
                        .post(GreetingController::post)
                .start(args.length > 0 ? Integer.parseInt(args[0]) : 8080);
    }
}
