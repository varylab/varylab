package de.varylab.varylab.plugin.nodeeditor;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;



public class DoubleArrayValueContainerCustomizer extends JPanel implements Customizer {

	private static final long 
		serialVersionUID = 1L;
	private JTable
		table = new JTable(new ArrayModel());
	private DoubleArrayValueContainer
		value = null;
	private List<PropertyChangeListener>
		listeners = new LinkedList<PropertyChangeListener>();
	
	
	public DoubleArrayValueContainerCustomizer() {
		setLayout(new GridLayout());
		add(table);
		table.getTableHeader().setPreferredSize(new Dimension(10, 0));
	}
	
	
	private class ArrayModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		
		@Override
		public int getColumnCount() {
			return 1;
		}
		
		@Override
		public int getRowCount() {
			if (value == null) return 0;
			return value.getValue().length;
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (value == null) return -1;
			else return value.getValue()[row];
		}
		
		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (value == null) {
				return;
			} else {
				Double old = value.getValue()[row];
				Double val = Double.parseDouble(aValue.toString());
				value.getValue()[row] = val;
				PropertyChangeEvent e = new PropertyChangeEvent(this, "value", old, val);
				firePropertyChangedEvent(e);
			}
		}
		
	}
	
	
	@Override
	public void setObject(Object bean) {
		value = (DoubleArrayValueContainer)bean;
	}

	private void firePropertyChangedEvent(PropertyChangeEvent e) {
		for (PropertyChangeListener l : listeners) {
			l.propertyChange(e);
		}
	}
	
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

}
