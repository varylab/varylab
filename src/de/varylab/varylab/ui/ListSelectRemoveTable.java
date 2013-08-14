package de.varylab.varylab.ui;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JTable;

public class ListSelectRemoveTable<E> extends JTable {

	private static final long serialVersionUID = 1L;
	
	public ListSelectRemoveTable(ListSelectRemoveTableModel<E> listModel) {
		super();
		setModel(listModel);
		getTableHeader().setPreferredSize(new Dimension(10, 0));
		setRowHeight(22);
		setDefaultEditor(JButton.class, new ButtonCellEditor());
		setDefaultRenderer(JButton.class, new ButtonCellRenderer());
		getColumnModel().getColumn(2).setMaxWidth(22);
		getColumnModel().getColumn(2).setPreferredWidth(22);
		getColumnModel().getColumn(0).setMaxWidth(22);
		getColumnModel().getColumn(0).setPreferredWidth(22);
	}
	
	@SuppressWarnings("unchecked")
	public ListSelectRemoveTableModel<E> getListModel() {
		return ((ListSelectRemoveTableModel<E>)dataModel);
	}

}
