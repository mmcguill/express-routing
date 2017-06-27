package com.markmcguill.express.routing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mark on 13/06/2017.
 */
public class ExpressRouterTest {
    private static final Map<String, String[]> allPathDefinitions;

    static {
        HashMap<String, String[]> x = new HashMap<>();

        x.put("*", new String[]{"garbage", "absolutely"});
        x.put("/*", new String[]{"/anything", "/absolutely"});
        x.put("/", new String[]{"/"});
        x.put("/active", new String[]{"/active"});
        x.put("/offers", new String[]{"/offers"});
        x.put("/offers/:offerslug", new String[]{"/offers/e1264"});
        x.put("/trades", new String[]{"/trades"});
        x.put("/trades/:commoditySlug/:date(\\d{2}-\\d{2}-\\d{4})?", new String[]{"/trades/porkbelly", "/trades/porkbelly/07-06-2017"});
        x.put("/trades/:commoditySlug/:optionId/:date?", new String[]{"/trades/porkbelly/2000", "/trades/porkbelly/2000/07-06-2017"});
        x.put("/trades/:commoditySlug/:optionId/bid/:bidId", new String[]{"/trades/porkbelly/2000/bid/1"});
        x.put("/:commoditySlug", new String[]{"/porkbelly"});
        x.put("/:commoditySlug/alpha", new String[]{"/porkbelly/alpha"});
        x.put("/:commoditySlug/active", new String[]{"/porkbelly/active"});
        x.put("/:commoditySlug/active/bid/:bidId", new String[]{"/porkbelly/active/bid/2"});
        x.put("/:commoditySlug/futures/:date?", new String[]{"/porkbelly/futures", "/porkbelly/futures/10-06-2017"});
        x.put("/:commoditySlug/futures/bid/:bidId", new String[]{"/porkbelly/futures/bid/1"});
        x.put("/:commoditySlug/options", new String[]{"/porkbelly/options"});
        x.put("/:commoditySlug/options/:optionId", new String[]{"/porkbelly/options/2000"});
        x.put("/:commoditySlug/options/bid/:bidId", new String[]{"/porkbelly/options/bid/1"});
        x.put("/:commoditySlug/exchanges", new String[]{"/porkbelly/exchanges"});
        x.put("/:commoditySlug/shorts", new String[]{"/porkbelly/shorts"});
        x.put("/:commoditySlug/shorts/:shortId", new String[]{"/porkbelly/shorts/481"});
        x.put("/:commoditySlug/straddles/:straddleSlug?", new String[]{"/porkbelly/straddles", "/porkbelly/straddles/porkbelly-euro"});
        x.put("/:commoditySlug/straddles/:straddleSlug/bid/:bidId", new String[]{"/porkbelly/straddles/porkbelly-euro/bid/1"});
        x.put("/:commoditySlug/future", new String[]{"/porkbelly/future"});
        x.put("/:commoditySlug/trades", new String[]{"/porkbelly/trades"});

        allPathDefinitions = Collections.unmodifiableMap(x);
    }

    @Test
    public void testSimple() {
        String pathDefinition = "/:commoditySlug/options/:optionId";
        final String PATH = "/porkbelly/options/1234";

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        if (route.matches(PATH)) {
            final Map<String, String> parametersFromPath = route.getParametersFromPath(PATH);

            System.out.println(parametersFromPath);
        }
    }

    @Test
    public void testCatchAllWithGarbage() {
        String pathDefinition = "/*";
        final String PATH = "/literal garbage";

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        Assert.assertTrue(route.matches(PATH));
    }

    @Test
    public void testCatchAllStar() {
        String pathDefinition = "*";
        final String PATH = "/";

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        Assert.assertTrue(route.matches(PATH));
    }

    @Test
    public void testCatchAll() {
        String pathDefinition = "/*";
        final String PATH = "/";

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        Assert.assertTrue(route.matches(PATH));
    }

    @Test
    public void testCatchAllIsSortedLast() {
        String catchAll = "/*";
        ExpressRoute ca = new ExpressRoute(catchAll);

        String otherMatch = "/:commoditySlug/options/:optionId?";
        ExpressRoute other = new ExpressRoute(otherMatch);

        final String PATH = "/porkbelly/options/1234";

        ExpressRoutePatternComparator foo = new ExpressRoutePatternComparator(PATH);

        Assert.assertTrue(ca.matches(PATH));
        Assert.assertTrue(other.matches(PATH));

        List<ExpressRoute> routes = Arrays.asList(ca, other);

        System.out.println(routes.stream().map(ExpressRoute::getPathDefinition).collect(Collectors.toList()));

        routes.sort(new ExpressRoutePatternComparator(PATH));

        System.out.println(routes.stream().map(ExpressRoute::getPathDefinition).collect(Collectors.toList()));

        Assert.assertTrue(foo.compare(ca, other) > 0);
    }

    @Test
    public void testOptionalParamParamPresentWithTrailingForwardSlash() {
        String pathDefinition = "/:commoditySlug/straddles/:straddleSlug?";
        final String PATH = "/porkbelly/straddles/sluggy/"; //, "/porkbelly/straddles/porkbelly-euro"});

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        Assert.assertTrue(route.matches(PATH));

        final Map<String, String> parametersFromPath = route.getParametersFromPath(PATH);
        System.out.println(parametersFromPath);

        Assert.assertTrue(parametersFromPath.size() == 2);
        Assert.assertTrue(parametersFromPath.get("commoditySlug").equals("porkbelly"));
        Assert.assertTrue(parametersFromPath.get("straddleSlug").equals("sluggy"));
    }

    @Test
    public void testOptionalParamParamPresent() {
        String pathDefinition = "/:commoditySlug/straddles/:straddleSlug?";
        final String PATH = "/porkbelly/straddles/sluggy"; //, "/porkbelly/straddles/porkbelly-euro"});

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        Assert.assertTrue(route.matches(PATH));

        final Map<String, String> parametersFromPath = route.getParametersFromPath(PATH);
        System.out.println(parametersFromPath);

        Assert.assertTrue(parametersFromPath.size() == 2);
        Assert.assertTrue(parametersFromPath.get("commoditySlug").equals("porkbelly"));
        Assert.assertTrue(parametersFromPath.get("straddleSlug").equals("sluggy"));
    }

    @Test
    public void testCustomPatternForDate() {
        String pathDefinition = "/trades/:commoditySlug/:date(\\d{2}-\\d{2}-\\d{4})?";
        final String PATH = "/trades/porkbelly/07-06-2017";

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        Assert.assertTrue(route.matches(PATH));

        final Map<String, String> parametersFromPath = route.getParametersFromPath(PATH);
        System.out.println(parametersFromPath);

        Assert.assertTrue(parametersFromPath.size() == 2);
        Assert.assertTrue(parametersFromPath.get("commoditySlug").equals("porkbelly"));
        Assert.assertTrue(parametersFromPath.get("date").equals("07-06-2017"));
    }

    @Test
    public void testCustomPatternForDateTrailingSlash() {
        String pathDefinition = "/trades/:commoditySlug/:date(\\d{2}-\\d{2}-\\d{4})?";
        final String PATH = "/trades/porkbelly/07-06-2017/";

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        Assert.assertTrue(route.matches(PATH));

        final Map<String, String> parametersFromPath = route.getParametersFromPath(PATH);
        System.out.println(parametersFromPath);

        Assert.assertTrue(parametersFromPath.size() == 2);
        Assert.assertTrue(parametersFromPath.get("commoditySlug").equals("porkbelly"));
        Assert.assertTrue(parametersFromPath.get("date").equals("07-06-2017"));
    }

    @Test
    public void testCustomPatternForDateMissingDateTrailingSlash() {
        String pathDefinition = "/trades/:commoditySlug/:date(\\d{2}-\\d{2}-\\d{4})?";
        final String PATH = "/trades/porkbelly/";

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        Assert.assertTrue(route.matches(PATH));

        final Map<String, String> parametersFromPath = route.getParametersFromPath(PATH);
        System.out.println(parametersFromPath);

        Assert.assertTrue(parametersFromPath.size() == 2);
        Assert.assertTrue(parametersFromPath.get("commoditySlug").equals("porkbelly"));
        Assert.assertTrue(parametersFromPath.get("date") == null);
    }

    @Test
    public void testCustomPatternForDateMissingDate() {
        String pathDefinition = "/trades/:commoditySlug/:date(\\d{2}-\\d{2}-\\d{4})?";
        final String PATH = "/trades/porkbelly";

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        Assert.assertTrue(route.matches(PATH));

        final Map<String, String> parametersFromPath = route.getParametersFromPath(PATH);
        System.out.println(parametersFromPath);

        Assert.assertTrue(parametersFromPath.size() == 2);
        Assert.assertTrue(parametersFromPath.get("commoditySlug").equals("porkbelly"));
        Assert.assertTrue(parametersFromPath.get("date") == null);
    }

    @Test
    public void testOptionalParamMissingParamWithTrailingForwardSlash() {
        String pathDefinition = "/:commoditySlug/straddles/:straddleSlug?";
        final String PATH = "/porkbelly/straddles/"; //, "/porkbelly/straddles/porkbelly-euro"});

        ExpressRoute route = new ExpressRoute(pathDefinition);

        Assert.assertTrue(route.matches(PATH));

        final Map<String, String> parametersFromPath = route.getParametersFromPath(PATH);
        System.out.println(parametersFromPath);

        Assert.assertTrue(parametersFromPath.size() == 2);
        Assert.assertTrue(parametersFromPath.get("commoditySlug").equals("porkbelly"));
        Assert.assertTrue(parametersFromPath.get("straddleSlug") == null);
    }

    @Test
    public void testOptionalParamMissingParam() {
        String pathDefinition = "/:commoditySlug/straddles/:straddleSlug?";
        final String PATH = "/porkbelly/straddles"; //, "/porkbelly/straddles/porkbelly-euro"});

        ExpressRoute route = new ExpressRoute(pathDefinition);

        System.out.println(route);

        Assert.assertTrue(route.matches(PATH));

        final Map<String, String> parametersFromPath = route.getParametersFromPath(PATH);
        System.out.println(parametersFromPath);

        Assert.assertTrue(parametersFromPath.size() == 2);
        Assert.assertTrue(parametersFromPath.get("commoditySlug").equals("porkbelly"));
        Assert.assertTrue(parametersFromPath.get("straddleSlug") == null);
    }

    @Test
    public void testCompileAgainstSlash() {
        for (String pathDefinition : allPathDefinitions.keySet()) {
            ExpressRoute route = new ExpressRoute(pathDefinition);

            if (route.matches("/")) {
                System.out.println(route);
            }
        }
    }

    @Test
    public void testCompileAll() {
        for (String pathDefinition : allPathDefinitions.keySet()) {
            ExpressRoute route = new ExpressRoute(pathDefinition);

            System.out.println(route);

            final String[] tests = allPathDefinitions.get(pathDefinition);

            for (String test : tests) {
                if (route.matches(test)) {
                    final Map<String, String> parametersFromPath = route.getParametersFromPath(test);

                    System.out.println(parametersFromPath);
                }
                else {
                    System.out.println(route);
                    System.out.println(test);
                    Assert.assertTrue(false);
                }
            }
        }
    }

    @Test
    public void testMultipleMatchingPatternsCompareMostSpecific() {
        final String test = "/trades/porkbelly/12-07-2017";

        final List<String> sorted = allPathDefinitions.keySet().stream()
                                                      .map(ExpressRoute::new)
                                                      .filter(route -> route.matches(test))
                                                      .sorted((a, b) -> {
                                                          ExpressRoutePatternComparator foo = new ExpressRoutePatternComparator(test);
                                                          return foo.compare(a, b);
                                                      })
                                                      .map(ExpressRoute::getPathDefinition)
                                                      .collect(Collectors.toList());

        System.out.println(sorted);
    }
}
