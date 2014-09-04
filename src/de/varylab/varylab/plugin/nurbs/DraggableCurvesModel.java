package de.varylab.varylab.plugin.nurbs;

import java.util.LinkedList;
import java.util.List;

import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.VectorFields;
import de.varylab.varylab.plugin.nurbs.scene.DraggableIntegralNurbsCurves;
import de.varylab.varylab.ui.ListSelectRemoveTableModel;
import de.varylab.varylab.ui.PrettyPrinter;

public class DraggableCurvesModel extends ListSelectRemoveTableModel<DraggableIntegralNurbsCurves> {

	public DraggableCurvesModel(String columnName, PrettyPrinter<DraggableIntegralNurbsCurves> printer) {
		super(columnName, printer);
		columnNames = new String[]{" ", columnName, " ", "VF"};
	}

	private static final long serialVersionUID = 1L;
	
    @Override
	public int getColumnCount() { 
    	return columnNames.length;
    }
    
    @Override
	public Object getValueAt(int row, int col) {
    	if(col != 3) {
            return super.getValueAt(row, col);        	
        }
    	return list.get(row).getVectorFields();
    }
    
    @Override
	public boolean isCellEditable(int row, int col) {
    	switch (col) {
		case 0:
		case 2:
		case 3:
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
    	fireTableDataChanged();
    }

	public List<PolygonalLine> getCheckedPolygonalLines() {
		List<PolygonalLine> lines = new LinkedList<>();
		for(DraggableIntegralNurbsCurves dc : checked) {
			lines.addAll(dc.getPolygonalLines());
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
		default:
			return String.class;
		}
	}
}
