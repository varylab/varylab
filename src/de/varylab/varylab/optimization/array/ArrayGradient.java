package de.varylab.varylab.optimization.array;

import de.jtem.halfedgetools.functional.Gradient;

public class ArrayGradient extends ArrayDomainValue implements Gradient {

	public ArrayGradient(double[] vec) {
		super(vec);
	}

}