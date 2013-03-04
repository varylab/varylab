package de.varylab.varylab.plugin.lnf;

import java.awt.Font;

import javax.swing.plaf.FontUIResource;

import org.pushingpixels.substance.api.fonts.FontSet;

public class TahomaFontSet implements FontSet {
	
	private FontUIResource 
		tahoma11 = null;

	public TahomaFontSet(int size) {
		tahoma11 = new FontUIResource("Tahoma", Font.PLAIN, size);
	}
	
	public FontUIResource getControlFont() {
		return tahoma11;
	}

	public FontUIResource getMenuFont() {
		return tahoma11;
	}

	public FontUIResource getMessageFont() {
		return tahoma11;
	}

	public FontUIResource getSmallFont() {
		return tahoma11;
	}

	public FontUIResource getTitleFont() {
		return tahoma11;
	}

	public FontUIResource getWindowTitleFont() {
		return tahoma11;
	}
	
}