package de.varylab.varylab.startup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JProgressBar;

import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;
import de.varylab.varylab.plugin.ui.image.ImageHook;
import de.varylab.varylab.startup.image.SplashImageHook;

public class VarylabSplashScreen extends SplashScreen {

	private static final long 
		serialVersionUID = 1L;
	private Image
		lowResImage = SplashImageHook.getImage("varylab_01_low_res.png"),
		hiResImage = SplashImageHook.getImage("varylab_01_high_res.png");
	private JProgressBar
		progressBar = new JProgressBar(0, 100);
	private boolean
		useHighRes = false;
	
	public VarylabSplashScreen() {
		com.sun.awt.AWTUtilities.setWindowOpaque(this, false);
		int res = Toolkit.getDefaultToolkit().getScreenResolution();
		useHighRes = res > 75;
		System.out.println("Splash resolution: " + res + "dpi");
		setIconImage(ImageHook.getImage("main_03.png"));
		setLayout(new BorderLayout());
		Dimension size = new Dimension();
		if (useHighRes) {
			size.width = hiResImage.getWidth(this) / 2;
			size.height = hiResImage.getWidth(this) / 2;
		} else {
			size.width = lowResImage.getWidth(this);
			size.height = lowResImage.getWidth(this);
		}
		setSize(size);
		setPreferredSize(size);
		setMinimumSize(size);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;
		if (useHighRes) {
			int w = hiResImage.getWidth(this);
			int h = hiResImage.getHeight(this);
			g2d.drawImage(hiResImage, 0, 0, w/2, h/2, this);
		} else {
			int w = lowResImage.getWidth(this);
			int h = lowResImage.getHeight(this);
			g2d.drawImage(hiResImage, 0, 0, w, h, this);
		}
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
	
	public static void main(String[] args) {
		VarylabSplashScreen splash = new VarylabSplashScreen();
		splash.setVisible(true);
	}

}
