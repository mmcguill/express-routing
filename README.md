# A Java Port of the Express Routing Library 'path-to-regexp'
*A port of: https://www.npmjs.com/package/path-to-regexp*

A Library to allow matching and parameter extraction from a URL based on an Express style route definition.

##### Example Usage:

```
@Test
public void test() {
    String routeDefinition = "/:commoditySlug/options/:optionId";
    final String path      = "/porkbelly/options/1234";

    ExpressRoute route = new ExpressRoute(routeDefinition);
    
    if (route.matches(path)) {
        final Map<String, String> parametersFromPath = route.getParametersFromPath(path);

        System.out.println(parametersFromPath);
    }
}
```
