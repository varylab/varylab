package de.varylab.varylab.plugin.nurbs.adapter;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.varylab.varylab.plugin.nurbs.type.CurvatureLineField;

public class  FlatIndexFormAdapter extends AbstractAdapter<Double> {

	public FlatIndexFormAdapter() {
		super(Double.class, true, false);
	}
	
	@Override
	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>
	>Double getF(F f, AdapterSet a) {
		List<E> boundary = HalfEdgeUtilsExtra.getBoundary(f);
		double sum = 0;
		double[] z1, z2, p1;
		for (E e : boundary) {
			z1 = a.get(CurvatureLineField.class,e.getStartVertex(),double[].class);
			if (Rn.euclideanNorm(z1) == 0) {
				z1 = new double[]{1., 0.};
			} else {
				Rn.normalize(z1, z1);
			}
			z1= multiply(z1, z1);
			z2 = a.get(CurvatureLineField.class,e.getTargetVertex(),double[].class);
			if (Rn.euclideanNorm(z2) == 0) {
				z2 = new double[]{1., 0.};
			} else {
				Rn.normalize(z2, z2);
				z2= new double[]{z2[0],-z2[1]};
			}
			z2= multiply(z2, z2);
			p1 = multiply(z1, z2);
			sum += arg(p1);
		}
		return sum/(2*Math.PI);
	}
	
	private double[] multiply(double[] u, double[] v) {
		return new double[]{u[0] * v[0] - u[1] * v[1], u[0] * v[1]
				+ u[1] * v[0]};
	}
	
	private double arg(double[] z) {
		return Math.atan2(z[1], z[0]);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Face.class.isAssignableFrom(nodeClass);
	}

}