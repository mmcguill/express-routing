package com.markmcguill.express.routing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mark on 14/06/2017.
 */
public class ExpressRoutePatternInfo {
    private final String pattern;
    private int uriVars;
    private Integer wildcards;
    private boolean catchAllPattern;
    private Integer length;

    public ExpressRoutePatternInfo(ExpressRoute route) {
        this.uriVars = route.getKeys().size();
        this.pattern = route.getPathDefinition();

        this.catchAllPattern = this.pattern.equals("/*");

        if (this.uriVars == 0) {
            this.length = this.pattern.length();
        }
    }

    public int getUriVars() {
        return this.uriVars;
    }

    public boolean isLeastSpecific() {
        return this.catchAllPattern;
    }

    public int getTotalCount() {
        return this.getUriVars() + this.getWildcards();
    }

    private static final Pattern NON_KEY_WILDCARDS = Pattern.compile("/[^:(/]*?[\\*]+[^:]*?");

    public int getWildcards() {
        if (this.wildcards == null) {
            final Matcher matcher = NON_KEY_WILDCARDS.matcher(this.pattern);

            wildcards = 0;
            while (matcher.find()) {
                wildcards++;
            }
        }

        return this.wildcards;
    }

    private static final Pattern VARIABLE_PATTERN = Pattern.compile(":[^/]+");

    public int getLength() {
        if (this.length == null) {
            final String s = VARIABLE_PATTERN.matcher(this.pattern).replaceAll("#");

            this.length = s.length();
        }

        return this.length;
    }

    @Override
    public String toString() {
        return "ExpressRoutePatternInfo{" +
                "pattern='" + pattern + '\'' +
                ", uriVars=" + getUriVars() +
                ", wildcards=" + getWildcards() +
                ", catchAllPattern=" + catchAllPattern +
                ", length=" + getLength() +
                '}';
    }
}
