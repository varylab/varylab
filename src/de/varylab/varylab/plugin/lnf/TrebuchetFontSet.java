package de.varylab.varylab.plugin.lnf;

import java.awt.Font;

import javax.swing.plaf.FontUIResource;

import org.pushingpixels.substance.api.fonts.FontSet;

public class TrebuchetFontSet implements FontSet {
	
	private FontUIResource 
		tahoma11 = null;

	public TrebuchetFontSet(int size) {
		tahoma11 = new FontUIResource("Trebuchet MS", Font.BOLD, size);
	}
	
	@Override
	public FontUIResource getControlFont() {
		return tahoma11;
	}

	@Override
	public FontUIResource getMenuFont() {
		return tahoma11;
	}

	@Override
	public FontUIResource getMessageFont() {
		return tahoma11;
	}

	@Override
	public FontUIResource getSmallFont() {
		return tahoma11;
	}

	@Override
	public FontUIResource getTitleFont() {
		return tahoma11;
	}

	@Override
	public FontUIResource getWindowTitleFont() {
		return tahoma11;
	}
	
}