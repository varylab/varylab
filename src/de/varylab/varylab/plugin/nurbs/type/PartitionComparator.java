package de.varylab.varylab.plugin.nurbs.type;

import java.util.Comparator;

import de.varylab.varylab.plugin.nurbs.data.LineSegment;


public class PartitionComparator  implements Comparator<LineSegment> {


	@Override
	public int compare(LineSegment ls1, LineSegment ls2) {
		if(ls1.getSegment()[0][0] != ls2.getSegment()[0][0]){
			return (int) Math.signum(ls1.getSegment()[0][0] - ls2.getSegment()[0][0]);
		}
		else if(ls1.getSegment()[0][1] != ls2.getSegment()[0][1]){
			return (int) Math.signum(ls1.getSegment()[0][1] - ls2.getSegment()[0][1]);
		}
		else if(ls1.getSegment()[1][0] != ls2.getSegment()[1][0]){
			return (int) Math.signum(ls1.getSegment()[1][0] - ls2.getSegment()[1][0]);
		}
		else if(ls1.getSegment()[1][1] != ls2.getSegment()[1][1]){
			return (int) Math.signum(ls1.getSegment()[1][1] - ls2.getSegment()[1][1]);
		}
		else{
			return 0;
		}
	}

}
