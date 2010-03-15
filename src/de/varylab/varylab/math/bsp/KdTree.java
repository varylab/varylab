package de.varylab.varylab.math.bsp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import de.varylab.varylab.math.geom3d.Point;

/**
 * Simple KdTree class to query neighbourhood information 
 *
 */
public class KdTree <
	DataClass extends HasKdTreePosition
> implements Serializable{

	private static final long 
		serialVersionUID = 1L;
	
	/**
	 * Simple (kd)-tree node structure
	 */
	public static class KdNode implements Serializable{
		
		private static final long 
			serialVersionUID = 1L;
		
		KdNode[] children; // The two childeren of the Node 

		int dim; // Dimension in which plane lies

		HasKdTreePosition splitPos; // Position of plane in

		KdNode() {
			children = new KdNode[2];
		}

		public KdNode[] getChildren() {
			return children;
		}

		public int getDim() {
			return dim;
		}

		public HasKdTreePosition getSplitPos() {
			return splitPos;
		}

		final public int getEndIndex() {
			if (this instanceof KdLeaf)
				return ((KdLeaf) this).endIndex;
			else
				return children[1].getEndIndex();
		}

		final public int getStartIndex() {
			if (this instanceof KdLeaf)
				return ((KdLeaf) this).startIndex;
			else
				return children[0].getStartIndex();
		}

	}

	/**
	 * Only leafs need to store a bucket of points
	 */
	public static class KdLeaf extends KdNode implements Serializable{
		
		private static final long 
			serialVersionUID = 1L;

		int startIndex;

		int endIndex;

		/**
		 * Create a leaf/bucket starting at startIndex and ending at endIndex
		 * @param startIndex
		 * @param endIndex
		 */
		public KdLeaf(int startIndex, int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

	}

	private int maxBucketSize; // maximum size of points per cell

	private  DataClass[] points; // HasPosition data

	private KdNode root; // root node

	/**
	 * Constructor for a kd tree
	 * Builds an KdTree for the given points with maxBucketSize
	 * The order of the points in the array may change!
	 * @param points
	 * @param maxBucketSize
	 */
	public KdTree(DataClass[] points, int maxBucketSize, boolean useMedian) {
		this.points = points;
		this.maxBucketSize = maxBucketSize;
        if (useMedian)
        	root = buildKdTree(0, points.length - 1, 0);
        else
        	root = buildKdTree(0, points.length - 1, getBBox());
	}

	/**
	 * Returns the root node of the KdTree
	 * @return
	 */
	public KdNode getRoot() {
		return root;
	}

	/**
	 * Returns the points within a given radius of HasPosition p
	 * @param p
	 * @param radius
	 * @return An array containing the points or null, if there weren't any points
	 * in that radius 
	 */
	final public Vector<DataClass> collectInRadius(HasKdTreePosition p, double radius) {
		Vector<DataClass> result = new Vector<DataClass>();
		// Only search in a radius greater than zero
		if(radius <= 0) 
			return result;
		// Start the collectInRadius search
		result = collectInRadius(root, p, radius, result);
		return result;
	}

	/**
	 * Implementation of the collectInRadius functonality.
	 * @param node
	 * @param target
	 * @param radius2
	 * @param vec
	 * @return A vector with the points or null, if there weren't any points
	 * in that radius 
	 */
	final protected Vector<DataClass> collectInRadius(KdNode node, HasKdTreePosition target, double radius2,
			Vector<DataClass> vec) {
		// If we reached a leaf, perform a linear search
		if (node instanceof KdLeaf) {
			for (int i = node.getStartIndex(); i <= node.getEndIndex(); i++) {
				double dist2 = distance2(points[i], target);
				if (dist2 != 0 && dist2 < radius2) {
					vec.add(points[i]);
				}
			}
			return vec;
		}
		// Traverse into child closer to query HasPosition
		if (isSmaller(target, node.splitPos, node.dim)) {
			// Traverse left child
			vec = collectInRadius(node.children[0], target, radius2, vec);
			// Traverse right child, if it is possible that it contains a nearer HasPosition
			if (Math.abs(node.splitPos.getPosition().get(node.dim) - target.getPosition().get(node.dim)) < radius2) {
				vec = collectInRadius(node.children[1], target, radius2, vec);
			}
		} else {
			// Traverse right child
			vec = collectInRadius(node.children[1], target, radius2, vec);
			// Traverse left child, if it is possible that it contains a nearer HasPosition
			if (Math.abs(node.splitPos.getPosition().get(node.dim) - target.getPosition().get(node.dim)) < radius2) {
				vec = collectInRadius(node.children[0], target, radius2, vec);
			}
		}
		return vec;
	}
	
	/**
	 * Returns the k nearest points around the given HasPosition p
	 * @param p
	 * @param knearest
	 * @return An array containing the points or null
	 */
	final public Vector<DataClass> collectKNearest(HasKdTreePosition p, int knearest) {
		Vector<DataClass> result = new Vector<DataClass>();
		if( knearest <= 0 ) { 
			return result;
		} 
		if( points.length <= knearest ) {
			for (DataClass point : points)
				result.add(point);
			return result;
		}
		// Create a priority queue
		KdPQueue<DataClass> pq = new KdPQueue<DataClass>(knearest);
		// Start the collectKNearest search
		pq = collectKNearest(root, p, pq);
		int num = pq.getNumberItems();
		if (num != 0) {
			result.setSize(num);
			for (int i = 0; i < num; i++)
                result.set(i, pq.pollSplitPos());
		}
	    return result;
	}

	/**
	 * Implementation of the collectKNearest functionality.
	 * @param node
	 * @param target
	 * @param pq
	 * @return
	 */
	private KdPQueue<DataClass> collectKNearest(KdNode node, HasKdTreePosition target, KdPQueue<DataClass> pq) {
		// If we reached a leaf, perform a linear search
		if (node instanceof KdLeaf) {
            int s = node.getStartIndex(); int e = node.getEndIndex();
            for (int i = s ; i <= e; i++) {
				double dist2 = distance2(points[i], target);
				if (dist2 != 0 && dist2 < pq.getMaximumDistance()) {
					pq.add(dist2, points[i]);
				}
			}
			return pq;
		}
		// Traverse into child closer to query HasPosition
		if (isSmaller(target, node.splitPos, node.dim)) {
			// Traverse left child
			pq = collectKNearest(node.children[0], target, pq);
			// Traverse right child, if it is possible that it contains a nearer HasPosition
			if (Math.abs(node.splitPos.getPosition().get(node.dim)- target.getPosition().get(node.dim)) < pq
					.getMaximumDistance()) {
				pq = collectKNearest(node.children[1], target, pq);
			}
		} else {
			// Traverse right child
			pq = collectKNearest(node.children[1], target, pq);
			// Traverse left child, if it is possible that it contains a nearer HasPosition
			if (Math.abs(node.splitPos.getPosition().get(node.dim) - target.getPosition().get(node.dim)) < pq
					.getMaximumDistance()) {
				pq = collectKNearest(node.children[0], target, pq);
			}
		}
		return pq;
	}

	/**
	 * Calculate the distance between two points in three dimensional space
	 * @param p1
	 * @param p2
	 * @return
	 */
	final public double distance2(final HasKdTreePosition p1, final HasKdTreePosition p2) {
        double result = p1.getPosition().distanceTo(p2.getPosition());
		return result;
	}

	/**
	 * Dump the kd-tree in text form
	 */
	public void dump() {
		dump(root, 0);
	}

	protected void dump(KdNode node, int depth) {
		for (int i = 0; i < depth; i++)
			System.out.print(" ");
		if (node != null) {
			System.out.println(node);
			if (node.getClass() == KdNode.class) {
				dump(node.children[0], depth + 1);
				dump(node.children[1], depth + 1);
			}
		} else
			System.out.println("Null");
	}

	/**
	 * Compare to 3 dimensional numbers in composite-number space
	 * Hence 
	 * @param a
	 * @param b
	 * @param dim
	 * @return true if a is smaller than b, false otherwise
	 */
	final boolean isSmaller(HasKdTreePosition a, HasKdTreePosition b, int dim) {
		if (a.getPosition().get(dim) < b.getPosition().get(dim))
			return true;
//		int codim = (dim + 1) % 3;
		if (a.getPosition().get(dim) == b.getPosition().get(dim) && a.getPosition().get(dim) < b.getPosition().get(dim))
			return true;
//		codim = (dim + 2) % 3;
        return a.getPosition().get(dim) == b.getPosition().get(dim) && a.getPosition().get(dim) < b.getPosition().get(dim);
    }


	/**
	 * The points in the interval [start..end] are sorted around the median
	 * Hence all points < median are to the left of the array and all points > median to the right
	 * This is performed at the given dimension
	 * @param dim
	 * @param start
	 * @param end
	 * @return
	 */
	protected int medianSplit(int start, int end, int dim) {
		int lower, upper;
		int mid = (start + end) / 2;
		DataClass x;
		DataClass y;
		while (start < end) {
			x = points[mid];
			lower = start;
			upper = end;
			do {
				while (isSmaller(points[lower], x, dim))
					lower++;
				while (isSmaller(x, points[upper], dim))
					upper--;
				//while (x<points[upper][dim]) upper--;
				if (lower <= upper) {
					// swap
					y = points[lower];
					points[lower] = points[upper];
					points[upper] = y;
					lower++;
					upper--;
				}
			} while (lower <= upper);
			if (upper < mid)
				start = lower;
			if (mid < lower)
				end = upper;
		}
		// shift median to the right to allow multiple points on one axis
		// while (mid<last  && points[mid][dim] == points[mid+1][dim]) mid++;
		return mid;
	}
	
	
	
	/**
	 * Splits the points around the value pos at dimension dim returning the range
	 * where the exact pos matched. Hence:
	 * points[start..lowerMid-1] < pos = points[lowerMid...upperMid-1] < points[upperMid..end]
	 * 
	 * @param start
	 * @param end
	 * @param dim
	 * @param pos
	 * @return
	 */
	protected int[] posSplit(int start, int end, int dim, double pos) {
		DataClass tmp;
		int lowerMid;
		int upperMid;

		int lower = start;
		int upper = end;
		do {
			while (lower < end && points[lower].getPosition().get(dim) < pos)
				lower++;
			while (upper >= start && pos <= points[upper].getPosition().get(dim) )
				upper--;
			if (lower <= upper) {
				// swap
				tmp = points[lower];
				points[lower] = points[upper];
				points[upper] = tmp;
				lower++;
				upper--;
			}
		} while (lower <= upper);
		lowerMid = lower;
		upper = end;
		do {
			while (lower < end && points[lower].getPosition().get(dim) <= pos)
				lower++;
			while (upper >= lowerMid && pos < points[upper].getPosition().get(dim) )
				upper--;
			if (lower <= upper) {
				// swap
				tmp = points[lower];
				points[lower] = points[upper];
				points[upper] = tmp;
				lower++;
				upper--;
			}
		} while (lower <= upper);
		upperMid = lower;
		return new int[] { lowerMid - 1, upperMid - 1};
	}


	
	/**
	 * The actual kd-Tree Algorithm using the median as a cutting plane
	 * 
	 * This leads to a O(log n) height
	 * 
	 * @param start
	 * @param end
	 * @param depth
	 * @return
	 */
	protected KdNode buildKdTree(int start, int end, int depth) {
		if (end - start < maxBucketSize) {
			// We are finished when there are less than maxBucketSize points in
			// our interval
			return new KdLeaf(start, end);
		}

		// Otherwise we split the pointset at the given dimension around the
		// median
		int dim = depth % 3;
		int median = medianSplit(start, end, dim);

		// And continue recursively with the two subsets
		KdNode result = new KdNode();
		result.dim = dim; // store dimension
		result.splitPos = points[median]; // store the position of the plane
		result.children[0] = buildKdTree(start, median, depth + 1); // points below or on our splitting line
		result.children[1] = buildKdTree(median + 1, end, depth + 1); // points above our splitting line
		return result;
	}

	int getLongestDim(HasKdTreePosition[] bbox) {
		int dim;
		double[] diff = new double[]{bbox[1].getPosition().get(0) - bbox[0].getPosition().get(0), bbox[1].getPosition().get(1) - bbox[0].getPosition().get(1), bbox[1].getPosition().get(2) - bbox[0].getPosition().get(2)};
		if (diff[0] > diff[1]) {
			if (diff[0] > diff[2]) {
				dim = 0;
			} else {
				dim = 2;
			}
		} else {
			if (diff[1] > diff[2]) {
				dim = 1;
			} else {
				dim = 2;
			}
		}
		return dim;
	}

	/**
	 * This kd-Tree does not use the median as a split plane
	 * but rather chooses the midpoint of the bounding box on the
	 * longest axis. If the midpoint is outside the points use the min/max HasPosition
	 * accordingly.
	 * 
	 * This leads to a better aspect ratio of the cells but on the other hand
	 * can lead to O(n) tree depth
	 * 
	 * @param start
	 * @param end
	 * @param bbox
	 * @return
	 */
	protected KdNode buildKdTree(int start, int end, HasKdTreePosition[] bbox) {
		if (end - start < maxBucketSize) {
			// We are finished when there are less than maxBucketSize points in
			// our interval
			return new KdLeaf(start, end);
		}

		// getPosition().get the longest axis to split at
		int dim = getLongestDim(bbox);

		// Caluclate the midpoint along the axis 
		double midpoint = (bbox[0].getPosition().get(dim) + bbox[1].getPosition().get(dim)) / 2.0f;

		// getPosition().get the actual minimal and maximal position of the pointset along the axis
		double min;
		double max;
		min = points[start].getPosition().get(dim);
		max = points[start].getPosition().get(dim);
		for (int i = start; i < end; i++) {
			if (points[i].getPosition().get(dim) < min) {
				min = points[i].getPosition().get(dim);
			} else if (points[i].getPosition().get(dim) > max) {
				max = points[i].getPosition().get(dim);
			}
		}

		// Now we generate the node
		KdNode result = new KdNode();

		
		result.splitPos = new KdPosition();
		// If the midpoint is outside of our pointset move it to maximum/minimum
		if (midpoint < min)
			result.splitPos.getPosition().set(dim,min);
		else if (midpoint > max)
			result.splitPos.getPosition().set(dim, max);
		else
			result.splitPos.getPosition().set(dim, midpoint);

		// Permute the points that we have two sets cut along our splitPos
		int[] split;
		split = posSplit(start, end, dim, result.splitPos.getPosition().get(dim));

		// either choose as split index
		int mid;
		if (midpoint < min)  
			mid = start;
		else if (midpoint > max)  
			mid = end;
		// if we did not cut the getPosition().set in the mid use this as split index
		else if (split[0] > (start + end) / 2) 
			mid = split[0];
		else if (split[1] < (start + end) / 2) 
			mid = split[1];
		// otherwise take the real median if its within the range
		else 
			mid = (start + end) / 2;
		
		result.dim = dim; // store dimension

		// resize bbox and continue recursively on one side of the plane
		double oldMax = bbox[1].getPosition().get(dim);
		bbox[1].getPosition().set(dim,result.splitPos.getPosition().get(dim));
		result.children[0] = buildKdTree(start, mid, bbox);
		bbox[1].getPosition().set(dim,oldMax);

		// accordingly to the other side of the plane
		double oldMin = bbox[0].getPosition().get(dim);
		bbox[0].getPosition().set(dim, result.splitPos.getPosition().get(dim));
		result.children[1] = buildKdTree(mid + 1, end, bbox);
		bbox[0].getPosition().set(dim, oldMin);

		return result;
	}

	/**
	 * Compute the bounding box of the points
	 * @return
	 */
	public HasKdTreePosition[] getBBox() {
		HasKdTreePosition [] result = new HasKdTreePosition[2];
		
		result[0] = new KdPosition(points[0].getPosition());
		result[1] = new KdPosition(points[0].getPosition());
		for (int i = 1; i < points.length; i++) {
			for (int d = 0; d < 3; d++) {
				if (result[0].getPosition().get(d) > points[i].getPosition().get(d))
					result[0].getPosition().set(d,points[i].getPosition().get(d));
				if (result[1].getPosition().get(d) < points[i].getPosition().get(d))
					result[1].getPosition().set(d,points[i].getPosition().get(d)) ;
			}
		}
		return result;
	}

	private Collection<KdLeaf> getLeafs(KdNode root) {
		if (root instanceof KdLeaf)
			return Collections.singleton((KdLeaf) root);
		else {
			LinkedList<KdLeaf> result = new LinkedList<KdLeaf>();
			result.addAll(getLeafs(root.getChildren()[0]));
			result.addAll(getLeafs(root.getChildren()[1]));
			return result;
		}
	}

	public Collection<KdLeaf> getLeafs() {
		return getLeafs(getRoot());
	}

	public HasKdTreePosition[] getPoints() {
		return points;
	}

	public static class KdPosition implements HasKdTreePosition{

		private Point
			p = new Point();
		
		public KdPosition() {
		}

		public KdPosition(Point p){
			this.p.set(p);
		}
		
		public KdPosition(double[] vec) {
			p = new Point(vec);
		}
		
		public Point getPosition() {
			return p;
		}

		public void setPosition(Point p) {
			this.p.set(p);
		}
		
	}
	
	
}
