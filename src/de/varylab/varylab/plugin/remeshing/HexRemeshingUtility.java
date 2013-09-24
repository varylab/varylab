package de.varylab.varylab.plugin.remeshing;

import java.awt.geom.Rectangle2D;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class HexRemeshingUtility {

	public static void createHexMesh(VHDS hds, Rectangle2D bbox) {
		double xSpan = bbox.getWidth();
		double ySpan = bbox.getHeight();
		int xRes = (int)Math.ceil(xSpan * 2);
		int yRes = (int)Math.ceil(ySpan * 2); 
		
		double radius = 1.0/3.0;
		double xOffset = 0.75*radius % 1.0;
		for (int i = 0; i < xRes; i++) {
			for (int j = 0; j < yRes; j++) {
				VVertex v = hds.addNewVertex();
				double xPos = bbox.getMinX() + i * 0.5 + xOffset;
				double yPos = bbox.getMinY() + j * 0.5;
				double move = (j % 2 == 0) ? 1 : -1;
				move *= (i % 2 == 0) ? 1 : -1;
				xPos += move*radius/4;
				double[] P = v.getP();
				double[] T = v.getP();
				P[0] = xPos;
				P[1] = yPos;
				P[2] = 0.0;
				P[3] = 1.0;
				T[0] = xPos;
				T[1] = yPos;
				T[2] = 0.0;
				T[3] = 1.0;		
			}
		}
		for (int i = 0; i < xRes - 1; i++) {
			for (int j = 0; j < yRes - 2; j+=2) {
				int colStep = i % 2 == 0 ? 0 : 1;
				if (j + 2 + colStep >= yRes) continue;
				VVertex v1 = hds.getVertex(i*yRes + j + colStep); 
				VVertex v2 = hds.getVertex((i+1)*yRes + j + colStep); 
				VVertex v3 = hds.getVertex((i+1)*yRes + j + 1 + colStep); 
				VVertex v4 = hds.getVertex((i+1)*yRes + j + 2 + colStep); 
				VVertex v5 = hds.getVertex(i*yRes + j + 2 + colStep); 
				VVertex v6 = hds.getVertex(i*yRes + j + 1 + colStep); 
				HalfEdgeUtils.constructFaceByVertices(hds, v1, v2, v3, v4, v5, v6);
			}
		}	
	}

}
