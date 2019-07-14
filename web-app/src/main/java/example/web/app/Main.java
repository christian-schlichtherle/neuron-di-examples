/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app;

import example.web.app.controller.GreetingController;
import example.web.framework.HttpServer;

import java.io.IOException;

public class Main implements HttpServer<Main> {

    private Main() {
    }

    public String message() {
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
