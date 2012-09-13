package de.varylab.varylab.plugin.generator;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.utilities.Rectangle2D;

public class HexMeshFactory {

	private VHDS 
		hds = new VHDS();
	
	private int 
		vHex = 1,
		uHex = 1,
		uRes = 2,
		vRes = 4;
	
	private double[] 
	    v = new double[]{1.0,0.0},
	    w = new double[]{Math.cos(Math.PI/3.0),Math.sin(Math.PI/3.0)};

	private boolean
		needsRecomputation = true;
	
	public HexMeshFactory(int uHex, int vHex) {
		setResolution(uHex, vHex);
	}

	public HexMeshFactory(int uHex, int vHex, VHDS hds) {
		if(hds != null) {
			this.hds=hds;
		}
		setResolution(uHex, vHex);
	}
	
	public HexMeshFactory(double[] u, int uHex, double[] v, int vHex ) {
		setResolution(uHex, vHex);
		setGenerators(u, v);
	}
	
	public HexMeshFactory(int uHex, int vHex, double alpha) {
		setResolution(uHex, vHex);
		setGenerators(v, new double[]{Math.cos(alpha), Math.sin(alpha)});
	}
	
	public HexMeshFactory(Rectangle2D intBox, double alpha) {
		setGenerators(v, new double[]{Math.cos(alpha), Math.sin(alpha)});
		setSize(intBox);
	}
	
	public HexMeshFactory(double alpha) {
		setGenerators(v, new double[]{Math.cos(alpha), Math.sin(alpha)});
	}


	public VHDS getHDS() {
		if(needsRecomputation) {
			updateHDS();
			needsRecomputation = false;
		} 
		return hds;
	}

	public void setGenerators(double[] v, double[] w) {
		System.arraycopy(v, 0, this.v, 0, v.length);
		System.arraycopy(w, 0, this.w, 0, v.length);
		needsRecomputation = true;
	}

	public void setResolution(int uHex, int vHex) {
		
		if(this.uHex != uHex || this.vHex != vHex) {
			this.uHex = uHex;
			this.vHex = vHex;
			updateResolution();
			needsRecomputation = true;
		}
	}

	private void updateResolution() {
		uRes = 1+uHex;
		vRes = 2*vHex+1+((uHex >= 2)?1:0);
	}
	
	public Rectangle2D getInsideRectangle() {
		double[] ll = hds.getVertex(0).P;
		double[] ur = hds.getVertex(hds.numVertices()-1).P;
		return new Rectangle2D(
			new double[]{ll[0] + w[0], ll[1] + w[1]},
			new double[]{ur[0] - w[0], ur[1] - w[1]}
		);
	}
	
	public Rectangle2D getOutsideRectangle() {
		double[] ur = hds.getVertex(hds.numVertices()-1).P;
		return new Rectangle2D(
			new double[]{0.0,0.0},
			new double[]{ur[0]+w[0],ur[1]+((uHex%2==1)?w[1]:0)}
		);
	}
	
	private void updateHDS() {
		hds.clear();
		generateVertices(uRes, vRes);
		generateHexagons();

		// remove singleton vertices
		if(uRes % 2 == 1) {
			hds.removeVertex(hds.getVertex(vRes*(uRes-1)));
			if(vRes % 2 == 1) {
				hds.removeVertex(hds.getVertex(uRes*vRes - 2));
			} else {
				hds.removeVertex(hds.getVertex(vRes - 1));
			}
		}
		if(uRes % 2 == 0 && vRes % 2 == 0) {
			hds.removeVertex(hds.getVertex(vRes - 1));
			hds.removeVertex(hds.getVertex(uRes*vRes - 1 - 1));
		}
	}

	private void generateVertices(int vSize, int wSize) {
		
		double proj = Rn.innerProduct(v, w)/Rn.euclideanNormSquared(v);
		double[] wv = new double[]{proj*v[0],proj*v[1]};
		
		for (int i = 0; i < vSize; i++) {
			for (int j = 0; j < wSize; j++) {
				VVertex vv = hds.addNewVertex();
				double xPos = i * (v[0]+wv[0]) + j * (w[0]-wv[0]);
				double yPos = i * (v[1]+wv[1]) + j * (w[1]-wv[1]);
				double move = (j % 2 == 0) ? 1 : -1;
				move *= (i % 2 == 0) ? 1 : -1;
				xPos += move*wv[0]/2;
				yPos += move*wv[1]/2;
				double[] pos = {xPos, yPos, 0, 1};
				vv.P = pos;
			}
		}
	}

	private void generateHexagons() {
		for (int i = 0; i < uRes - 1; i++) {
			for (int j = 0; j < vRes - 2; j+=2) {
				int colStep = i % 2 == 0 ? 0 : 1;
				if (j + 2 + colStep >= vRes) {
					continue;
				}
				VVertex v1 = hds.getVertex(i*vRes + j + colStep); 
				VVertex v2 = hds.getVertex((i+1)*vRes + j + colStep); 
				VVertex v3 = hds.getVertex((i+1)*vRes + j + 1 + colStep); 
				VVertex v4 = hds.getVertex((i+1)*vRes + j + 2 + colStep); 
				VVertex v5 = hds.getVertex(i*vRes + j + 2 + colStep); 
				VVertex v6 = hds.getVertex(i*vRes + j + 1 + colStep); 
				HalfEdgeUtils.constructFaceByVertices(hds, v1, v2, v3, v4, v5, v6);
			}
		}
	}

	public void setSize(Rectangle2D intBox) {
		setResolution((int)Math.ceil(intBox.getWidth()/(v[0]+w[0]))+1,(int)Math.ceil(intBox.getHeight()/(2.0*(v[1]+w[1])))+1);	
	}
	
}
