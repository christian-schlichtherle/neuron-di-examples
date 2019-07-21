package example.web.framework;

import com.sun.net.httpserver.Headers;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Float.parseFloat;
import static java.util.Locale.forLanguageTag;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toUnmodifiableList;

final class HeadersFun {

    private static final Comparator<Map.Entry<Float, String>> QVALUE_COMPARATOR =
            Comparator.<Map.Entry<Float, String>, Float>comparing(Map.Entry::getKey).reversed();

    // See https://tools.ietf.org/html/rfc4647#section-2.1 and https://tools.ietf.org/html/rfc7231#section-5.3.1 :
    private static final Pattern QVALUE_PATTERN =
            compile("(?<languageRange>\\p{Alpha}{1,8}(?:-\\p{Alnum}{1,8})*|\\*)\\s*;\\s*q=(?<qvalue>0(?:\\.\\d{0,3})?|1(?:\\.0{0,3})?)", CASE_INSENSITIVE);

    static List<Locale> acceptLanguages(Headers headers) {
        return Stream.ofNullable(headers.getFirst("Accept-Language"))
                .flatMap(l -> Stream.of(l.split(",")))
                .map(s -> {
                    final var t = s.trim();
                    final var m = QVALUE_PATTERN.matcher(t);
                    return m.matches() ? Map.entry(parseFloat(m.group("qvalue")), m.group("languageRange")) : Map.entry(1F, t);
                })
                .sorted(QVALUE_COMPARATOR)
                .map(e -> forLanguageTag(e.getValue()))
                .collect(toUnmodifiableList());
    }

    private HeadersFun() {
    }
}
