package example.web.app;

import example.web.app.service.GreetingService;
import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.Neuron;

import java.util.Locale;

import static global.namespace.neuron.di.java.Incubator.wire;

// The module pattern bundles a group of factory methods for application components which may depend on each other.
// In GoF parlance, it's a blend of the Factory pattern and the Mediator pattern because a module creates (and
// optionally caches) components and delegates to itself for resolving their dependencies.
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

    // This is a "dependency provider field" which gets read by `example.web.app.service.GreetingService.locale()`.
    private static final Locale locale = Locale.ENGLISH;

    // Another dependency provider field, this time read by `example.web.app.service.GreetingService.greeting()`.
    private static final String greeting = "Hello, %s!";

    // This is a "dependency provider method", that is, a non-abstract method without parameters.
    // The synapse method `example.web.app.controller.GreetingController.greetingService()` delegates each call to this
    // method, so it's a good idea to cache its result - think of it as a quasi-singleton.
    // The @Caching annotation requires a virtual method, so we cannot use a field or a method which is private or
    // static here.
    @Caching
    GreetingService greetingService() {
        // In modules, components should be wired by delegating to the module itself:
        return wire(GreetingService.class).using(this);
    }
}
