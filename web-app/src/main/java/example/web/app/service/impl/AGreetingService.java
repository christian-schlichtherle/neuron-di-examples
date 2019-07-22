package example.web.app.service.impl;

import example.web.app.service.api.GreetingService;

import java.util.*;

import static java.lang.String.format;
import static java.util.Locale.forLanguageTag;
import static java.util.Map.entry;
import static java.util.stream.Stream.*;

public interface AGreetingService extends GreetingService {

    /**
     * The undetermined locale matching the basic language range "*".
     *
     * @see <a href="https://tools.ietf.org/html/rfc4647#section-2.1">RFC 4647 - Matching of Language Tags - 2.1. Basic Language Range</a>
     */
    Locale UNDETERMINED = forLanguageTag("*");

    /**
     * Returns the default locale to use if there is no greeting message defined for any given language ranges.
     */
    // This is a "synapse method", that is, an abstract method without parameters.
    // Each call to this method reads the field `example.web.app.Module.defaultLocale`.
    // In general, synapse methods do not need to be public.
    Locale defaultLocale();

    /**
     * Returns an unmodifiable map of greeting messages.
     * The keys are locales.
     * The values are lists of exactly two strings:
     * The first string is a format string as defined by the second parameter of
     * {@link String#format(Locale, String, Object...)}.
     * The second string is the default value to use when the subject of a greeting message is undefined.
     */
    // Another synapse method.
    Map<Locale, List<String>> greetingMessages();

    @Override
    default String apply(List<Locale> languageRanges, Optional<String> who) {
        return concat(languageRanges.stream(), of(defaultLocale()))
                .map(l -> l.equals(UNDETERMINED) ? defaultLocale() : l)
                .flatMap(l -> ofNullable(greetingMessages().get(l)).map(g -> entry(l, g)))
                .findFirst()
                .map(lg -> {
                    final Locale l = lg.getKey();
                    final Iterator<String> s = lg.getValue().iterator();
                    return format(l, s.next(), who.orElseGet(s::next));
                })
                .orElseThrow();
    }
}
