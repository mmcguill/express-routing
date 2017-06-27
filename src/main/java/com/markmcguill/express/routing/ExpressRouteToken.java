package com.markmcguill.express.routing;

/**
 * Created by mark on 13/06/2017.
 */
public class ExpressRouteToken {
    private final String name;
    private final String prefix;
    private final String delimiter;
    private final boolean optional;
    private final boolean repeat;
    private final boolean partial;
    private final String asterisk;
    private final String pattern;
    private final ExpressRouteTokenType type;

    public ExpressRouteToken(String name) {
        this.name = name;
        prefix = null;
        delimiter = null;
        optional = false;
        repeat = false;
        partial = false;
        asterisk = null;
        pattern = null;
        this.type = ExpressRouteTokenType.PATH_FRAGMENT;
    }

    public ExpressRouteToken(String name, String prefix, String delimiter, boolean optional, boolean repeat, boolean partial, String asterisk, String pattern) {
        this.name = name;
        this.prefix = prefix;
        this.delimiter = delimiter;
        this.optional = optional;
        this.repeat = repeat;
        this.partial = partial;
        this.asterisk = asterisk;
        this.pattern = pattern;
        this.type = ExpressRouteTokenType.PARAMETRIC;
    }

    public ExpressRouteTokenType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public boolean isPartial() {
        return partial;
    }

    public String getAsterisk() {
        return asterisk;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return "ExpressRouteToken{" +
                "name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", delimiter='" + delimiter + '\'' +
                ", optional=" + optional +
                ", repeat=" + repeat +
                ", partial=" + partial +
                ", asterisk='" + asterisk + '\'' +
                ", pattern='" + pattern + '\'' +
                '}';
    }
}

