package de.varylab.varylab.startup;

import java.awt.BorderLayout;
import java.awt.Rectangle;

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
		setIconImage(ImageHook.getImage("main_03.png"));
		setLayout(new BorderLayout());
		add(image, BorderLayout.CENTER);
		add(progressBar, BorderLayout.SOUTH);
		progressBar.setStringPainted(true);
	}
	
	@Override
	public void setStatus(String status) {
		getLayout().layoutContainer(getRootPane());
		progressBar.setString(status);
		Rectangle r = new Rectangle(getWidth(), getHeight());
		getRootPane().paintImmediately(r);
	}

	@Override
	public void setProgress(double progress) {
		getLayout().layoutContainer(getRootPane());
		progressBar.setValue((int)(progress * 100));
		Rectangle r = new Rectangle(getWidth(), getHeight());
		getRootPane().paintImmediately(r);
	}

}
