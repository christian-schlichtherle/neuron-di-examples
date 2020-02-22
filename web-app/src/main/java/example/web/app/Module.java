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
    // so it's a good idea to apply the `@Caching` annotation in order to instantiate the returned object at most once
    // and return the same instance again upon each subsequent call.
    // Since there is no other instantiation, adding this annotation effectively makes the returned object a singleton.
    // In general, a dependency provider method may also be static or private, but that's not possible with the @Caching
    // annotation, which requires a non-private, non-static method.
    @Caching
    GreetingService greetingService() {
        // In modules, components should be wired by delegating to the module itself:
        return wire(AGreetingService.class).using(this);
    }

    // Alternatively, you could also use a "dependency provider field".
    // In general, a dependency provider field may also be static, but in this particular case that's not possible
    // because the `.using(...)` call in the initialization needs a real object as its parameter.
    // Note that using a non-static field will cause the `AGreetingService` instance to be created whenever the `Module`
    // interface gets instantiated, even if this field is never accessed.
    // In contrast, the `greetingService()` method above will only be called when it's really needed, so that's
    // generally preferable.
    // As for another subtle detail, the Java language allows you to have both a method and a field with the same name,
    // so you could just uncomment the following line and it would compile and run fine.
    // However, there is no guarantee which instance would be picked by Neuron DI, so this is discouraged.
//    private final GreetingService greetingService = wire(AGreetingService.class).using(this);
}
