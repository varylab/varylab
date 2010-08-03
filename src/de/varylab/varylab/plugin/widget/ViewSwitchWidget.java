package de.varylab.varylab.plugin.widget;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import de.varylab.varylab.plugin.ui.WidgetPlugin;

public class ViewSwitchWidget extends WidgetPlugin {

	private JComboBox
		viewsCombo = new JComboBox(new String[] {"Front", "Back", "Top", "Bottom", "Left", "Right"});
	
	@Override
	public JComponent getWidgetComponent() {
		return viewsCombo;
	}

}
