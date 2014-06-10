package de.varylab.varylab.plugin.nurbs;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.Test;

import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;

public class IntersectionPointTest {

	@Test
	public void makeOrientedNbrsTest(){
		
		LinkedList<IntersectionPoint> nbrs = new LinkedList<>();
		IntersectionPoint ip = new IntersectionPoint();
		ip.setPoint(new double[]{0,0});
		IntersectionPoint nbr1 = new IntersectionPoint();
		nbr1.setPoint(new double[]{1,4});
		IntersectionPoint nbr2 = new IntersectionPoint();
		nbr2.setPoint(new double[]{-2,1});
		IntersectionPoint nbr3 = new IntersectionPoint();
		nbr3.setPoint(new double[]{3,-2});
		IntersectionPoint nbr4 = new IntersectionPoint();
		nbr4.setPoint(new double[]{1,1});
		IntersectionPoint nbr5 = new IntersectionPoint();
		nbr5.setPoint(new double[]{-2,3});
		IntersectionPoint nbr6 = new IntersectionPoint();
		nbr6.setPoint(new double[]{-2,-2});
		nbrs.add(nbr1); nbrs.add(nbr2); nbrs.add(nbr3); nbrs.add(nbr4); nbrs.add(nbr5); nbrs.add(nbr6);
		ip.setNbrs(nbrs);
		
		ip.makeOrientedNbrs();
		for (IntersectionPoint nbr : ip.getNbrs()) {
			System.out.println(Arrays.toString(nbr.getPoint()));
		}
		
		
		
	}

}
