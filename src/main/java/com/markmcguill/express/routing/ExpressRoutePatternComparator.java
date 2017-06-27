package com.markmcguill.express.routing;

import java.util.Comparator;

/**
 * Created by mark on 14/06/2017.
 */
public class ExpressRoutePatternComparator implements Comparator<ExpressRoute> {
    private final String path;

    public ExpressRoutePatternComparator(String path) {
        this.path = path;
    }

    @Override
    public int compare(ExpressRoute o1, ExpressRoute o2) {
        final String pattern1 = o1.getPathDefinition();
        final String pattern2 = o2.getPathDefinition();

        ExpressRoutePatternInfo info1 = new ExpressRoutePatternInfo(o1);
        ExpressRoutePatternInfo info2 = new ExpressRoutePatternInfo(o2);

        if (info1.isLeastSpecific() && info2.isLeastSpecific()) {
            return 0;
        }
        else if (info1.isLeastSpecific()) {
            return 1;
        }
        else if (info2.isLeastSpecific()) {
            return -1;
        }
        else {
            boolean pattern1EqualsPath = pattern1.equals(this.path);
            boolean pattern2EqualsPath = pattern2.equals(this.path);

            return pattern1EqualsPath && pattern2EqualsPath ? 0 :
                    (pattern1EqualsPath ? -1 : (pattern2EqualsPath ? 1 :
                            (info1.getTotalCount() != info2.getTotalCount() ? info1.getTotalCount() - info2.getTotalCount() :
                                    (info1.getLength() != info2.getLength() ? info2.getLength() - info1.getLength() :
                                            (info1.getWildcards() < info2.getWildcards() ? -1 :
                                                    (info2.getWildcards() < info1.getWildcards() ? 1 :
                                                            (info1.getUriVars() < info2.getUriVars() ? -1 : (info2.getUriVars() < info1
                                                                    .getUriVars() ? 1 : 0))))))));
        }
    }
}
