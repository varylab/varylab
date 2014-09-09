package de.varylab.varylab.plugin.nurbs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.VectorFields;
import de.varylab.varylab.plugin.nurbs.plugin.PointSelectionPlugin.Parameter;
import de.varylab.varylab.plugin.nurbs.scene.DraggableIntegralNurbsCurves;
import de.varylab.varylab.ui.ListSelectRemoveTable;

public class DraggableCurvesTable extends ListSelectRemoveTable<DraggableIntegralNurbsCurves> {

	private static final long serialVersionUID = 1L;

	public DraggableCurvesTable(DraggableCurvesModel listModel) {
		super(listModel);
		setDefaultEditor(VectorFields.class, new VectorFieldsEditor());
		setDefaultRenderer(VectorFields.class, new VectorFieldsRenderer());
		setDefaultEditor(Parameter.class, new ParameterDirectionEditor());
		setDefaultRenderer(Parameter.class, new ParameterDirectionRenderer());
	}

	public void adjustColumnSizes() {
		getColumnModel().getColumn(4).setMaxWidth(22);
		getColumnModel().getColumn(4).setPreferredWidth(22);
		getColumnModel().getColumn(3).setMaxWidth(22);
		getColumnModel().getColumn(3).setPreferredWidth(22);
		getColumnModel().getColumn(2).setMaxWidth(22);
		getColumnModel().getColumn(2).setPreferredWidth(22);
		getColumnModel().getColumn(0).setMaxWidth(22);
		getColumnModel().getColumn(0).setPreferredWidth(22);
	}
	
	private class VectorFieldsEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
		private static final long serialVersionUID = 1L;
		private final JButton
			firstButton = new JButton(ImageHook.getIcon("vf_first.png")),
			secondButton = new JButton(ImageHook.getIcon("vf_second.png")),
			bothButton = new JButton(ImageHook.getIcon("vf_both.png"));
		private VectorFields currentValue = null;
		
		public VectorFieldsEditor() {
			super();
			firstButton.addActionListener(this);
			secondButton.addActionListener(this);
			bothButton.addActionListener(this);
		}
		
		@Override
		public Object getCellEditorValue() {
			return currentValue;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			currentValue = (VectorFields)value;
			if(currentValue == VectorFields.FIRST) {
				return firstButton;	
			} else if(currentValue == VectorFields.SECOND) {
				return secondButton;
			} else { //if(currentValue == VectorFields.BOTH){
				return bothButton;
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if(firstButton == source) {
				currentValue = VectorFields.SECOND;
			} else if(secondButton == source) {
				currentValue = VectorFields.BOTH;
			} else if(bothButton == source) {
				currentValue = VectorFields.FIRST;
			}
			fireEditingStopped();
		}
	}
	
	private class VectorFieldsRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public VectorFieldsRenderer() {
			super();
			setHorizontalAlignment(CENTER);
		}
		
		@Override
		protected void setValue(Object value) {
			if(value == VectorFields.BOTH) {
				setIcon(ImageHook.getIcon("vf_both.png"));
				setToolTipText("Both");
			} else if(value == VectorFields.FIRST) {
				setIcon(ImageHook.getIcon("vf_first.png"));
				setToolTipText("First");
			} else { // value == VectorFields.SECOND
				setIcon(ImageHook.getIcon("vf_second.png"));
				setToolTipText("Second");
			}
		}
	}
	
	private class ParameterDirectionEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
		private static final long serialVersionUID = 1L;
		private final JButton
			uButton = new JButton("u"),
			vButton = new JButton("v"),
			uvButton = new JButton("*");
		private Parameter currentValue = null;
		
		public ParameterDirectionEditor() {
			super();
			uButton.addActionListener(this);
			vButton.addActionListener(this);
			uvButton.addActionListener(this);
		}
		
		@Override
		public Object getCellEditorValue() {
			return currentValue;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			currentValue = (Parameter)value;
			if(currentValue == Parameter.U) {
				return uButton;	
			} else if(currentValue == Parameter.V) {
				return vButton;
			} else { //if(currentValue == Parameter.UV){
				return uvButton;
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if(uButton == source) {
				currentValue = Parameter.V;
			} else if(vButton == source) {
				currentValue = Parameter.UV;
			} else if(uvButton == source) {
				currentValue = Parameter.U;
			}
			fireEditingStopped();
		}
	}
	
	private class ParameterDirectionRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public ParameterDirectionRenderer() {
			super();
			setHorizontalAlignment(CENTER);
		}
		
		@Override
		protected void setValue(Object value) {
			if(value == Parameter.U) {
				setText("u");
				setToolTipText("U");
			} else if(value == Parameter.UV) {
				setText("*");
				setToolTipText("UV");
			} else { // value == VectorFields.SECOND
				setText("v");
				setToolTipText("V");
			}
		}
	}
}
