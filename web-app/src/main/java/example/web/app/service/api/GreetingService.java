/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app.service.api;

import java.util.*;
import java.util.function.BiFunction;

import static java.lang.String.format;
import static java.util.Locale.forLanguageTag;
import static java.util.Map.entry;
import static java.util.stream.Stream.*;

/**
 * Computes a localized greeting message for an optional subject.
 */
public interface GreetingService extends BiFunction<List<Locale>, Optional<String>, String> {

    /**
     * Returns a greeting message considering the given list of language ranges and optional subject.
     * If there is no message defined for the given language ranges, then a default message is returned.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4647#section-3.3.1">RFC 7231 - Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content - 5.3.5. Accept-Language</a>
     */
    @Override
    String apply(List<Locale> languageRanges, Optional<String> who);
}
