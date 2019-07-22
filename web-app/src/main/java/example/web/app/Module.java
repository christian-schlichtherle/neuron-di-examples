/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app;

import example.web.app.service.api.GreetingService;
import example.web.app.service.impl.AGreetingService;
import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.Neuron;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static global.namespace.neuron.di.java.Incubator.wire;
import static java.util.Locale.*;

// A module bundles a group of factory methods for application components which may depend on each other into a single
// object.
//
// In design pattern parlance, the module pattern is a blend of the factory pattern and the mediator pattern because a
// module not only creates (and optionally caches) application components, but may also delegate back to itself whenever
// some of their dependencies need to get resolved.
//
// The @Neuron annotation is required to apply the @Caching annotation to the methods of this interface.
//
// The @SuppressWarnings annotation makes IntelliJ IDEA stop complaining that the dependency provider fields and methods
// would not be used: IDEA simply doesn't know about the delegation model supported by Neuron DI.
// Note that a Neuron DI plugin for IDEA is in the making to change this and aid developers in locating synapses and
// their potential dependency providers at design time.
//
// Note that there is no trace of the web framework here - it will be added by the `Main` class.
@Neuron
@SuppressWarnings("unused")
abstract class Module {

    private static final Locale AUSTRIA = forLanguageTag("de-AT");
    private static final Locale SWITZERLAND = forLanguageTag("de-CH");

    // This is a "dependency provider field" which gets read by
    // `example.web.app.service.impl.AGreetingService.defaultLocale()`.
    private static final Locale defaultLocale = ENGLISH;

    // Another dependency provider field, this time read by
    // `example.web.app.service.impl.AGreetingService.greetingMessages()`.
    private static final Map<Locale, List<String>> greetingMessages = Map.of(
            AUSTRIA, List.of("Servus, %s!", "miteinander"),
            ENGLISH, List.of("Hello, %s!", "world"),
            GERMAN, List.of("Hallo, %s!", "Welt"),
            SWITZERLAND, List.of("Grüazie, %s!", "miteinander"),
            US, List.of("Howdy, %s!", "y'all")
    );

    // This is a "dependency provider method", that is, a method without parameters.
    // The method `example.web.app.controller.GreetingController.greetingService()` delegates each call to this method,
    // so it's a good idea to cache its return value - think of it as a quasi-singleton.
    // The @Caching annotation requires a virtual method, so we cannot use a field or a method which is private or
    // static here.
    @Caching
    GreetingService greetingService() {
        // In modules, components should be wired by delegating to the module itself:
        return wire(AGreetingService.class).using(this);
    }
}
