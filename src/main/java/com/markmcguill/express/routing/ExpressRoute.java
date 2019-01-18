package com.markmcguill.express.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by mark on 13/06/2017.
 */
public class ExpressRoute {
    private static final Pattern PATH_REGEXP = Pattern.compile(String.join("|", (CharSequence[]) new String[]{
        "(\\\\.)",
        "([\\/.])?(?:(?:\\:(\\w+)(?:\\(((?:\\\\.|[^\\\\()])+)\\))?|\\(((?:\\\\.|[^\\\\()])+)\\))([+*?])?|(\\*))"
    }));

    private static final Pattern escapeStringPattern = Pattern.compile("([.+*?=^!:${}()\\[\\]|/\\\\])");
    private static final Pattern escapeGroupPattern = Pattern.compile("([=!:$/()])");

    private final List<ExpressRouteToken> keys;
    private final Pattern pattern;
    private String pathDefinition;

    public ExpressRoute(String pathDefinition) {
        this.pathDefinition = pathDefinition;

        final List<ExpressRouteToken> tokens = parsePathDefinition(pathDefinition);

        this.keys = tokens.stream().filter(token -> token.getType() == ExpressRouteTokenType.PARAMETRIC).collect(Collectors.toList());
        this.pattern = tokensToRegex(tokens);
    }

    public List<ExpressRouteToken> getKeys() {
        return keys;
    }

    public Pattern getPattern() {
        return pattern;
    }

    private Pattern tokensToRegex(List<ExpressRouteToken> tokens) {
        final boolean strict = false;
        final boolean end = true;
        String route = "";

        for (ExpressRouteToken token : tokens) {
            if (token.getType() == ExpressRouteTokenType.PATH_FRAGMENT) {
                route += escapeString(token.getName());
            }
            else {
                String capture = "(" + token.getPattern() + ")";

                String prefix = escapeString(token.getPrefix() == null ? "" : token.getPrefix());

                if (token.isRepeat()) {
                    capture += "(?:" + prefix + capture + ")*";
                }

                if (token.isOptional()) {
                    if (!token.isPartial()) {
                        capture = "(?:" + prefix + capture + ")?";
                    }
                    else {
                        capture = prefix + "(" + capture + ")?";
                    }
                }
                else {
                    capture = prefix + "(?:" + capture + ")";
                }

                route += capture;
            }
        }

        final String delimiter = escapeString("/");
        boolean endsWithDelimiter = route.endsWith(delimiter);

        // In non-strict mode we allow a slash at the end of match. If the path to
        // match already ends with a slash, we remove it for consistency. The slash
        // is valid at the end of a path match, not in the middle. This is important
        // in non-ending mode, where "/test/" shouldn't match "/test//route".

        if (!strict) {
            if (endsWithDelimiter) {
                int endIndex = route.length() - delimiter.length() - 1;

                if (endIndex >= 0) {
                    route = route.substring(0, endIndex);
                }
            }

            route += "(?:" + delimiter + "(?=$))?";
        }

        if (end) {
            route += "$";
        }
        else {
            // In non-ending mode, we need the capturing groups to match as much as
            // possible by using a positive lookahead to the end or next path segment.
            route += strict && endsWithDelimiter ? "" : "(?=" + delimiter + "|$)";
        }

        return Pattern.compile("^" + route);
    }

    private static List<ExpressRouteToken> parsePathDefinition(String pathDefinition) {
        List<ExpressRouteToken> tokens = new ArrayList<>();
        int key = 0;
        int index = 0;
        String path = "";
        final String defaultDelimiter = "/";

        final Matcher matcher = PATH_REGEXP.matcher(pathDefinition);

        while (matcher.find()) {
            String m = matcher.group(0);
            String escaped = matcher.group(1);
            int offset = matcher.start();

            path += pathDefinition.substring(index, offset);
            index = offset + m.length();

            // Ignore already escaped sequences.

            if (!isEmpty(escaped) && escaped.length() > 1) {
                path += escaped.substring(1, 2);
                continue;
            }

            String next = (index >= pathDefinition.length()) ? null : pathDefinition.substring(index, index + 1);
            String prefix = matcher.group(2);
            String name = matcher.group(3);
            String capture = matcher.group(4);
            String group = matcher.group(5);
            String modifier = matcher.group(6);
            String asterisk = matcher.group(7);

            // Push the current path onto the tokens.

            if (!isEmpty(path)) {
                tokens.add(new ExpressRouteToken(path));
                path = "";
            }

            // TODO: Can Prefix be null? or empty?
            boolean partial = !isEmpty(prefix) && !isEmpty(next) && !Objects.equals(next, prefix);
            boolean repeat = (Objects.equals(modifier, "+")) || (Objects.equals(modifier, "*"));
            boolean optional = Objects.equals(modifier, "?") || Objects.equals(modifier, "*");
            String delimiter = isEmpty(prefix) ? defaultDelimiter : prefix;
            String pattern = isEmpty(capture) ? group : capture;

            String keyName = isEmpty(name) ? ((Integer) (key++)).toString() : name;

            String tokenPattern = isEmpty(pattern) ?
                    (!isEmpty(asterisk) ? ".*" : "[^" + escapeString(delimiter) + "]+?") :
                    escapeGroup(pattern);

            tokens.add(new ExpressRouteToken(keyName, prefix, delimiter, optional, repeat, partial, asterisk, tokenPattern));
        }

        // Match any characters still remaining.

        if (index < pathDefinition.length()) {
            path += pathDefinition.substring(index);
        }

        // If the path exists, push it onto the end.

        if (!isEmpty(path)) {
            tokens.add(new ExpressRouteToken(path));
        }

        return tokens;
    }

    private static boolean isEmpty(String test) {
        return test == null || test.length() == 0;
    }

    private static String escapeString(String str) {
        Matcher m = escapeStringPattern.matcher(str);
        List<Integer> findIndex = new ArrayList<>();
        while (m.find()) {
            findIndex.add(m.start());
        }
        if (findIndex.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder(str);
            for (int i = findIndex.size()-1; i >= 0; i--) {
                stringBuilder.insert(findIndex.get(i), "\\");
            }
            return stringBuilder.toString();
        } else {
            return str;
        }
    }

    private static String escapeGroup(String group) {
        Matcher m = escapeGroupPattern.matcher(group);
        List<Integer> findIndex = new ArrayList<>();
        while (m.find()) {
            findIndex.add(m.start());
        }
        if (findIndex.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder(group);
            for (int i = findIndex.size()-1; i >= 0; i--) {
                stringBuilder.insert(findIndex.get(i), "\\");
            }
            return stringBuilder.toString();
        } else {
            return group;
        }
    }

    public Matcher getMatcher(String path) {
        return pattern.matcher(path);
    }

    public boolean matches(String path) {
        return pattern.matcher(path).find();
    }

    public Map<String, String> getParametersFromPath(String path) {
        final Matcher matcher = pattern.matcher(path);
        Map<String, String> ret = new HashMap<>();

        if (matcher.find()) {
            for (int i = 1; i < matcher.groupCount() + 1; i++) {
                ret.put(keys.get(i - 1).getName(), matcher.group(i));
            }
        }

        return ret;
    }

    public String getPathDefinition() {
        return pathDefinition;
    }

    @Override
    public String toString() {
        return "ExpressRoute{" +
                "keys=" + keys +
                ", pattern=" + pattern +
                ", pathDefinition='" + pathDefinition + '\'' +
                '}';
    }
}
