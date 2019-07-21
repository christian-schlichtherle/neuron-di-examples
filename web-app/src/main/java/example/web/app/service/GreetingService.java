package example.web.app.service;

import java.util.*;

import static java.lang.String.format;
import static java.util.Locale.forLanguageTag;
import static java.util.Map.entry;
import static java.util.stream.Stream.*;

/**
 * Computes a localized greeting message for an optional subject.
 */
public interface GreetingService {

    /**
     * The undetermined locale matching the basic language range "*".
     *
     * @see <a href="https://tools.ietf.org/html/rfc4647#section-2.1">RFC 4647 - Matching of Language Tags - 2.1. Basic Language Range</a>
     */
    Locale UNDETERMINED = forLanguageTag("*");

    /**
     * Returns an unmodifiable map of greeting messages.
     * The keys are locales.
     * The values are lists of exactly two strings:
     * The first string is a format string as defined by the second parameter of
     * {@link String#format(Locale, String, Object...)}.
     * The second string is the default value to use when the subject of a greeting message is undefined.
     */
    // This is a "synapse method", that is, an abstract method without parameters.
    // Each call to this method reads the field `example.web.app.Module.greetingMessages`.
    // In general, synapse methods do not need to be public.
    Map<Locale, List<String>> greetingMessages();

    /**
     * Returns the default locale to use if there is no greeting message defined for the given language ranges.
     */
    // Another synapse method.
    Locale defaultLocale();

    /**
     * Returns a greeting message considering the given list of language ranges and the optional subject.
     *
     * @throws NoSuchElementException if there is no message defined for the {@link #defaultLocale()} or if the message
     *         is not composed of a second and third parameter for {@link String#format(Locale, String, Object...)}.
     * @see <a href="https://tools.ietf.org/html/rfc4647#section-3.3.1">RFC 7231 - Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content - 5.3.5. Accept-Language</a>
     */
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
