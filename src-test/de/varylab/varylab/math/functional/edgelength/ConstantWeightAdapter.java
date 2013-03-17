package de.varylab.varylab.math.functional.edgelength;

import de.jtem.halfedgetools.jreality.node.DefaultJREdge;
import de.varylab.varylab.functional.adapter.WeightFunction;

public class ConstantWeightAdapter implements WeightFunction<DefaultJREdge> {

		public double 
			w = 1.0;
		
		public ConstantWeightAdapter(double w) {
			this.w = w;
		}
		
		@Override
		public Double getWeight(DefaultJREdge e) {
			return w;
		}
		
	}
	