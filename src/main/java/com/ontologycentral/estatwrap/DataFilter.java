package com.ontologycentral.estatwrap;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Dimension filters on {@code /da/{id}}, passed through to Eurostat's SDMX 3.0 data API.
 *
 * <p>{@code ?geo=DE254,DE255&TIME_PERIOD=ge:2022} becomes
 * {@code &c[geo]=DE254,DE255&c[TIME_PERIOD]=ge:2022} upstream. Eurostat does the
 * filtering; the wrapper only checks the syntax and never drops rows itself. Eurostat
 * matches codes exactly; it rejects prefixes ({@code sw:}, {@code *}) and filters on
 * {@code OBS_VALUE} (HTTP 400, tested 2026-11-21), so neither is offered here.
 */
public final class DataFilter {

    public static final String TIME = "TIME_PERIOD";

    /** A dimension id as it appears in Eurostat's TSV header (geo, na_item, TIME_PERIOD). */
    public static final String NAME_PATTERN = "^[A-Za-z][A-Za-z0-9_]*$";

    /** One code or a comma-separated list of codes, matched exactly. */
    public static final String CODE_PATTERN = "^[A-Za-z0-9_-]+(,[A-Za-z0-9_-]+)*$";

    /**
     * Periods (2023, 2023-Q1, 2023-01), optionally with a comparison operator, joined by
     * {@code ,} (any of) or {@code +} (all of, for a range: {@code ge:2015+le:2020}).
     */
    public static final String TIME_PATTERN =
            "^((ge|gt|le|lt):)?[0-9]{4}[0-9A-Z-]*([+,]((ge|gt|le|lt):)?[0-9]{4}[0-9A-Z-]*)*$";

    private static final Pattern NAME = Pattern.compile(NAME_PATTERN);
    private static final Pattern CODE = Pattern.compile(CODE_PATTERN);
    private static final Pattern PERIOD = Pattern.compile(TIME_PATTERN);

    private final Map<String, String> _filters;

    private DataFilter(Map<String, String> filters) {
        _filters = Collections.unmodifiableMap(filters);
    }

    /**
     * Reads the filters from a request's parameter map. Repeated parameters are joined with
     * a comma, the same as one comma-separated value.
     *
     * @throws IllegalArgumentException naming the parameter, for a malformed name or value
     */
    public static DataFilter parse(Map<String, String[]> params) {
        Map<String, String> filters = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> e : params.entrySet()) {
            String name = e.getKey();
            if (!NAME.matcher(name).matches()) {
                throw new IllegalArgumentException("not a dimension name: " + name);
            }
            boolean time = name.equalsIgnoreCase(TIME);
            String value = String.join(",", e.getValue());
            if (time) {
                // an unencoded '+' in a query string arrives as a space
                value = value.replace(' ', '+');
            }
            Pattern p = time ? PERIOD : CODE;
            if (!p.matcher(value).matches()) {
                throw new IllegalArgumentException(
                        "value of " + name + " does not match " + p.pattern() + ": " + value);
            }
            filters.put(time ? TIME : name, value);
        }
        return new DataFilter(filters);
    }

    public static DataFilter none() {
        return new DataFilter(new LinkedHashMap<>());
    }

    public boolean isEmpty() {
        return _filters.isEmpty();
    }

    public Map<String, String> filters() {
        return _filters;
    }

    /** The upstream query parameters, each starting with {@code &}, percent-encoded. */
    public String toUpstreamQuery() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : _filters.entrySet()) {
            sb.append("&c%5B").append(e.getKey()).append("%5D=")
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
