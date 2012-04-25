package de.varylab.varylab.plugin.nurbs.data;

	
	import java.util.LinkedList;
	import java.util.List;

	public class Partition {
		
		private List<LineSegment> segList;
		private List<Integer> indexList;
		
		public Partition(){
			segList = new LinkedList<LineSegment>();
			indexList = new LinkedList<Integer>();
		}

		public List<LineSegment> getSegList() {
			return segList;
		}

		public void setSegList(List<LineSegment> segList) {
			this.segList = segList;
		}

		public List<Integer> getIndexList() {
			return indexList;
		}

		public void setIndexList(List<Integer> indexList) {
			this.indexList = indexList;
		}
		
		@Override
		public String toString(){
			String str = new String();
			str = str + segList.toString() + indexList.toString();
			return str;
		}
		

	}



