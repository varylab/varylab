package de.varylab.varylab.plugin.nurbs;

import java.util.LinkedList;
import java.util.List;

import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.VectorFields;
import de.varylab.varylab.plugin.nurbs.plugin.PointSelectionPlugin.Parameter;
import de.varylab.varylab.plugin.nurbs.scene.DraggableIntegralNurbsCurves;
import de.varylab.varylab.plugin.nurbs.scene.NurbsSurfaceConstraint;
import de.varylab.varylab.plugin.nurbs.scene.NurbsSurfaceDirectionConstraint;
import de.varylab.varylab.ui.ListSelectRemoveTableModel;
import de.varylab.varylab.ui.PrettyPrinter;

public class DraggableCurvesModel extends ListSelectRemoveTableModel<DraggableIntegralNurbsCurves> {

	public DraggableCurvesModel(String columnName) {
		super(columnName, null);
		pp = new DCPrinter();
		columnNames = new String[]{" ", columnName, " ", "VF", "PC"};
	}

	private static final long serialVersionUID = 1L;
	
    @Override
	public int getColumnCount() { 
    	return columnNames.length;
    }
    
    @Override
	public Object getValueAt(int row, int col) {
    	if(col < 3) {
            return super.getValueAt(row, col);        	
        } 
    	if(col == 3) {
    		return list.get(row).getVectorFields();
    	}
    	NurbsSurfaceConstraint constraint = list.get(row).getConstraint();
    	if(constraint instanceof NurbsSurfaceDirectionConstraint) {
    		return ((NurbsSurfaceDirectionConstraint) constraint).getParameterDirection(); 
    	}
    	return null;
    }
    
    @Override
	public boolean isCellEditable(int row, int col) {
    	switch (col) {
		case 0:
		case 2:
		case 3:
		case 4:
			return true;
		default:
			return false;
		}
    }
    
    @Override
	public void setValueAt(Object value, int row, int col) {
    	if(col == 0) {
    		Boolean b = (Boolean)value;
    		if(b) {
    			checked.add(list.get(row));
    		} else {
    			checked.remove(list.get(row));
    		}
    	}
    	if(col == 3) {
    		VectorFields vf = (VectorFields) value;
    		list.get(row).setVectorFields(vf);
    	}
    	if(col == 4) {
    		Parameter p = (Parameter) value;
    		DraggableIntegralNurbsCurves dc = list.get(row);
    		dc.setParameterDirection(p);
    		if(dc.getCommonCurves() != null) {
    			for(DraggableIntegralNurbsCurves c : dc.getCommonCurves()) {
    				c.setParameterDirection(p);
    				c.getConstraint().resetInitialUV();
    			}
    		}

    	}
    	fireTableDataChanged();
    }

	public List<PolygonalLine> getCheckedPolygonalLines() {
		List<PolygonalLine> lines = new LinkedList<>();
		for(DraggableIntegralNurbsCurves dc : checked) {
			switch (dc.getVectorFields()) {
			case FIRST:
				lines.add(dc.getPolygonalLine(VectorFields.FIRST));
				break;
			case SECOND:
				lines.add(dc.getPolygonalLine(VectorFields.SECOND));
				break;
			case BOTH:
				lines.add(dc.getPolygonalLine(VectorFields.FIRST));
				lines.add(dc.getPolygonalLine(VectorFields.SECOND));
				break;
			}
		}
		return lines;
	}		
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
		case 1:
		case 2:
			return super.getColumnClass(columnIndex);
		case 3:
			return VectorFields.class;
		case 4:
			return Parameter.class;
		default:
			return String.class;
		}
	}
	
	private class DCPrinter implements PrettyPrinter<DraggableIntegralNurbsCurves> {

		@Override
		public String toString(DraggableIntegralNurbsCurves t) {
			return t.getName();
		}
		
	}

}
