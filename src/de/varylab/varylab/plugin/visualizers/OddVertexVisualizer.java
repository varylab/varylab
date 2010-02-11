package de.varylab.varylab.plugin.visualizers;

/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

import java.util.Collections;
import java.util.Set;

import javax.swing.JPanel;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Ifs;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public class OddVertexVisualizer extends VisualizerPlugin {

	private JPanel
		panel = new JPanel();
	
	public OddVertexVisualizer() {
	}
	
	
	public class OddVertexAdapter <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> implements  ColorAdapter2Ifs<V> {

		@Override
		public AdapterType getAdapterType() {
			return AdapterType.VERTEX_ADAPTER;
		}
		
		@Override
		public double[] getColor(V v) {
			return (HalfEdgeUtilsExtra.getDegree(v)%2==0)?
					new double[]{0,1,0}:
					new double[]{1,0,0};
		}
		
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<? extends Adapter> getAdapters() {
		return Collections.singleton(new OddVertexAdapter());
	}


	@Override
	public String getName() {
		return "Odd Vertices";
	}

}
