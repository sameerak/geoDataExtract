package org.trajectory.clustering;

import java.util.Comparator;

public class LineUsageDescendingComparator implements Comparator<Line> {

    @Override
    public int compare(Line line1, Line line2) {
        return line2.getUsageCount() - line1.getUsageCount();
    }
}
