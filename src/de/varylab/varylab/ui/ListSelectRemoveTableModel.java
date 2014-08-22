package de.varylab.varylab.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;

import de.jtem.halfedgetools.plugin.image.ImageHook;

public class ListSelectRemoveTableModel<E> extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	private String[] columnNames = {" ", "Name", " "};
	private PrettyPrinter<E> pp = null;
	
	private List<E> list = new LinkedList<E>();
	private List<E> checked = new LinkedList<E>();
	
	public ListSelectRemoveTableModel(String columnName) {
		this(columnName,null);
	}
	
	public ListSelectRemoveTableModel(String columnName, PrettyPrinter<E> printer) {
		this.columnNames = new String[3];
		this.columnNames[0] = this.columnNames[2] = " ";
		columnNames[1] = columnName;
		pp = printer;
	}
	
	public List<E> getList() {
		return Collections.unmodifiableList(list);
	}

	public boolean isChecked(E pt) {
		return checked.contains(pt);
	}

	public void removeChecked() {
		list.removeAll(checked);
		checked.clear();
	}

	public List<E> getChecked() {
		return Collections.unmodifiableList(checked);
	}

	public void clearChecked() {
		checked.clear();
	}

	public void checkAll() {
		checked.clear();
		checked.addAll(list);
	}

	public void add(E elem) {
		list.add(elem);
		checked.add(elem);
	}

	public boolean contains(E elem) {
		return list.contains(elem);
	}

	public void clear() {
		list.clear();
		checked.clear();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col].toString();
    }
	
    @Override
	public int getRowCount() { 
    	return (list==null)?0:list.size();
    }
    
    @Override
	public int getColumnCount() { 
    	return columnNames.length;
    }
    
    @Override
	public Object getValueAt(int row, int col) {
        if(col == 1) {
        	E elem = list.get(row);
        	if(pp == null) {
        		return elem.toString();
        	}
        	return pp.toString(elem);
        }
        if(col == 2) {
        	return new RemoveRowButton(row);
        }
        return checked.contains(list.get(row));
    }
    
    @Override
	public boolean isCellEditable(int row, int col) {
    	switch (col) {
		case 0:
		case 2:
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
    	fireTableDataChanged();
    }	
    
    @Override
    public Class<?> getColumnClass(int col) {
    	switch (col) {
		case 0:
			return Boolean.class;
		case 1:
			return String.class;
		case 2:
			return JButton.class;
		default:
			return String.class;
		}
    }
    
    private class RemoveRowButton extends JButton implements ActionListener {

		private static final long serialVersionUID = 1L;

		private int row = -1;

		public RemoveRowButton(int row) {
			super(ImageHook.getIcon("remove.png"));
			setSize(16,16);
			addActionListener(this);
			this.row = row;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			E elem = list.remove(row);
			checked.remove(elem);
			fireTableDataChanged();
		}

	}

	public E remove(int index) {
		checked.remove(list.get(index));
		return list.remove(index);
	}
	
	public boolean remove(E pl){
		checked.remove(pl);
		return list.remove(pl);
	}

	public boolean isSelected(int row) {
		return checked.contains(list.get(row));
	}

	public void setSelected(int row, boolean b) {
		E elem = list.get(row);
		boolean alreadySelected = checked.contains(elem);
		if(alreadySelected && !b) {
			checked.remove(elem);
		} else if (!alreadySelected && b) {
			checked.add(elem);
		}
	}

	public void addAll(Collection<E> col) {
		list.addAll(col);
		checked.addAll(col);
	}
}