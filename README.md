![MIT License](https://img.shields.io/github/license/christian-schlichtherle/neuron-di-examples.svg)
[![Build Status](https://api.travis-ci.org/christian-schlichtherle/neuron-di-examples.svg)](https://travis-ci.org/christian-schlichtherle/neuron-di-examples)

# Neuron DI Examples For Java 

This repository contains sample code for [Neuron DI](https://github.com/christian-schlichtherle/neuron-di) for Java.
There are no published releases of this repository, so if you want to play with the sample code, please check it out 
and run the following command to build it:

    $ ./mvnw clean verify 

Neuron DI targets Java 8 or later.
However, the sample code in this repository targets a JDK for Java 11 or later.
If you don't have a JDK for Java 11, you can run the build in Docker instead:

    $ ./docker-mvnw 11-jdk-slim clean verify

# Example Web Framework & Web App

This repository provides a tiny web framework and application in the modules `web-framework` and `web-app`.

The web framework simply adapts the package `com.sun.net.httpserver`, which is bundled with the JDK since Java 6.
The focus of the web framework is on dependency injection, a nice 
[domain-specific-language](https://en.wikipedia.org/wiki/Domain-specific_language) for request routing and ease-of-use 
for web apps.
For the brevity of the sample code however, it does not completely encapsulate the API of the underlying JDK package, 
so it's a [leaky abstraction](https://en.wikipedia.org/wiki/Leaky_abstraction) and therefore you shouldn't use it in 
production!

The web app produces a simple Hello-world-in-JSON.
Using a JDK for Java 11, you can run it like this:

    $ java -jar web-app/target/web-app-*-all.jar
    08:36:40.707 [main] INFO example.web.framework.HttpServer - Serving HTTP/1.1 on port 8080.

If you don't have a JDK for Java 11, you can use the following Docker spell instead:

    $ docker run -it --rm -v $PWD:/workdir -w /workdir -p 8080:8080 openjdk:11-jdk-slim \
        java -jar web-app/target/web-app-*-all.jar
    08:37:40.123 [main] INFO example.web.framework.HttpServer - Serving HTTP/1.1 on port 8080.

Now you can ask the web app for a greeting in JSON:

    $ curl localhost:8080/greeting
    {"message":"Hello, world!"}
    $ curl localhost:8080/greeting?who=you
    {"message":"Hello, you!"}
    $ curl localhost:8080/greeting?who=Christian -H 'Accept-Language: de, en;q=0.9, *;q=0.8'
    {"message":"Hallo, Christian!"}

## License

The Neuron DI Examples for Java are covered by the MIT License.
