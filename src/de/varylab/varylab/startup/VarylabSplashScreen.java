package de.varylab.varylab.startup;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class VarylabSplashScreen extends SplashScreen {

	private static final long 
		serialVersionUID = 1L;
	private JLabel
		image = new JLabel(ImageHook.getIcon("splashJReality01.png"));
	private JProgressBar
		progressBar = new JProgressBar(0, 100);
	
	public VarylabSplashScreen() {
		super();
		setLayout(new BorderLayout());
		add(image, BorderLayout.CENTER);
		add(progressBar, BorderLayout.SOUTH);
		progressBar.setStringPainted(true);
	}
	
	@Override
	public void setStatus(String status) {
		progressBar.setString(status);
		paint(getGraphics());
	}

	@Override
	public void setProgress(double progress) {
		progressBar.setValue((int)(progress * 100));
		paint(getGraphics());
	}

}
