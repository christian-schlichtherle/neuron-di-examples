package example.web.app.service;

import java.util.Locale;

public interface GreetingService {

    // This is a "synapse" method, that is, an abstract method without parameters.
    // Each call to this method reads the field `example.web.app.Module.locale`.
    // In a real application, you might want to add the @Caching annotation, so the field is read at most once.
    // In general, synapse methods do not need to be public.
    Locale locale();

    // Another "synapse" method, this time reading the field `example.web.app.Module.greeting`.
    String greeting();

    default String message(String who) {
        return String.format(locale(), greeting(), who);
    }
}
