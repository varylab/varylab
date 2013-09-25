package de.varylab.varylab.plugin.nurbs.data;

public class ValidSegment {
	
	private boolean isValid;
	private int shiftedIndex;
	private int rightShift;
	private int upShift;
	
	public ValidSegment(){
		
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public int getShiftedIndex() {
		return shiftedIndex;
	}

	public void setShiftedIndex(int shiftedIndex) {
		this.shiftedIndex = shiftedIndex;
	}

	public int getRightShift() {
		return rightShift;
	}

	public void setRightShift(int rightShift) {
		this.rightShift = rightShift;
	}

	public int getUpShift() {
		return upShift;
	}

	public void setUpShift(int upShift) {
		this.upShift = upShift;
	}
	
}
