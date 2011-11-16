package de.varylab.varylab.plugin.nurbs;

import java.util.Comparator;

import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;

public class IntersectionPointDistanceComparator implements
		Comparator<IntersectionPoint> {

	@Override
	public int compare(IntersectionPoint o1, IntersectionPoint o2) {
		return (int)Math.signum(o1.getSameIndexDist() - o2.getSameIndexDist());
	}

}
