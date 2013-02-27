package de.varylab.varylab.startup;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.awt.AWTUtilities;

import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;
import de.varylab.varylab.plugin.ui.image.ImageHook;
import de.varylab.varylab.startup.image.SplashImageHook;

public class VarylabSplashScreen extends SplashScreen {

	private static final long 
		serialVersionUID = 1L;
	private Image
		lowResImage = SplashImageHook.getImage("varylab_01_low_res.png"),
		hiResImage = SplashImageHook.getImage("varylab_01_high_res.png");
	private String
		status = "Status";
	private double
		progress = 0.0;
	private SplashComponent
		splashComponent = new SplashComponent();
	private JLabel
		statusLabel = new JLabel("Status");
	private boolean
		useHighRes = false;
	private Random 
		rnd = new Random();
	
	public VarylabSplashScreen() {
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(hiResImage, 0);
		mt.addImage(lowResImage, 0);
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
		}
		AWTUtilities.setWindowOpaque(this, false);
		setAlwaysOnTop(true);
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		int[] dpi = getDPI(gc);
		useHighRes = dpi[0] > 110;
//		System.out.println("Splash is high resolution: " + useHighRes + " (" + dpi[0] + "dpi)");
		setIconImage(ImageHook.getImage("main_03.png"));
		
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
		
		setLayout(new GridLayout());
		add(splashComponent);
		splashComponent.setLayout(new GridBagLayout());
		splashComponent.add(new JLabel(status));
	}
	
	
    public static int[] getDPI(final GraphicsConfiguration gc){
        // get the Graphics2D of a compatible image for this configuration
        final Graphics2D g2d = (Graphics2D) gc.createCompatibleImage(1, 1).getGraphics();
        // after these transforms, 72 units in either direction == 1 inch; see JavaDoc for getNormalizingTransform()
        g2d.setTransform(gc.getDefaultTransform() );
        g2d.transform(gc.getNormalizingTransform() );
        final AffineTransform oneInch = g2d.getTransform();
        g2d.dispose();
        return new int[]{(int) (oneInch.getScaleX() * 72), (int) (oneInch.getScaleY() * 72) };
    }
	
    
    private class SplashComponent extends JPanel {

		private static final long serialVersionUID = 1L;
	    
		public SplashComponent() {
		}
		
	    @Override
	    public void paint(Graphics g) {
	    	System.out.println("VarylabSplashScreen.SplashComponent.paint()");
	    	Graphics2D g2d = (Graphics2D)g;
			if (useHighRes) {
				int w = hiResImage.getWidth(this);
				int h = hiResImage.getHeight(this);
				g2d.drawImage(hiResImage, 0, 0, w/2, h/2, this);
			} else {
				int w = lowResImage.getWidth(this);
				int h = lowResImage.getHeight(this);
				g2d.drawImage(lowResImage, 0, 0, w, h, this);
			}
			g2d.setColor(new Color(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat()));
			g2d.fillOval(0, 0, 100, 100);
			g2d.drawString(status, 0, 0);
			super.paintComponents(g);
	    }
    
    }
    
	@Override
	public void setStatus(String status) {
		this.status = status;
		updateSplash();
	}

	@Override
	public void setProgress(double progress) {
		this.progress = progress;
		updateSplash();
	}
	
	public static void main(String[] args) throws Exception {
		VarylabSplashScreen splash = new VarylabSplashScreen();
		splash.setVisible(true);
		while (true) {
			splash.repaint();
			Thread.sleep(50);
		}
	}

	protected void updateSplash() {
		getLayout().layoutContainer(getRootPane());
		Rectangle r = new Rectangle(getWidth(), getHeight());
		statusLabel.setText(status);
		splashComponent.paintImmediately(r);
	}
	
}
