package de.varylab.varylab.startup;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import com.sun.awt.AWTUtilities;

import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;
import de.varylab.varylab.plugin.ui.image.ImageHook;
import de.varylab.varylab.startup.image.SplashImageHook;

public class VarylabSplashScreen extends SplashScreen {

	private static final long 
		serialVersionUID = 1L;
	private Image
		lowResImage = null,
		hiResImage = null;
	private String
		status = "Status";
	private double
		progress = 0.0;
	private SplashComponent
		splashComponent = new SplashComponent();
	private boolean
		useHighRes = false;
	private static boolean 
		isWindows = false;
	private double
		statusX = 0.45,
		statusY = 0.75,
		fontSize = 0.02;
	
	static {
		isWindows = System.getProperty("os.name").toLowerCase().contains("win");
	}
	
	public VarylabSplashScreen() {
		this(
			SplashImageHook.getImage("varylab_ultimate_low_res.png"),
			SplashImageHook.getImage("varylab_ultimate_high_res.png")
		);
	}
	
	public VarylabSplashScreen(Image lowResImage, Image hiResImage) {
		this.lowResImage = lowResImage;
		this.hiResImage = hiResImage;
		AWTUtilities.setWindowOpaque(this, false);
		setAlwaysOnTop(true);
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		int[] dpi = getDPI(gc);
		useHighRes = dpi[0] > 110;
		System.out.println("Splash is high resolution: " + useHighRes + " (" + dpi[0] + "dpi)");
		setIconImage(ImageHook.getImage("main_03.png"));
		
		Dimension size = new Dimension();
		if (useHighRes) {
			size.width = hiResImage.getWidth(this) / 2;
			size.height = hiResImage.getHeight(this) / 2;
			if (isWindows) {
				size.width = hiResImage.getWidth(this);
				size.height = hiResImage.getHeight(this);
			}
		} else {
			size.width = lowResImage.getWidth(this);
			size.height = lowResImage.getHeight(this);
		}
		setSize(size);
		setPreferredSize(size);
		setMinimumSize(size);
		
		setLayout(new GridLayout());
		add(splashComponent);
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

		private static final long 
			serialVersionUID = 1L;
//		private Color
//			clearColor = new Color(255, 255, 255, 0);
		private Font	
			font = null;
	    
		public SplashComponent() {
		}
		
	    @Override
	    public void paint(Graphics g) {
	    	Graphics2D g2d = (Graphics2D)g;
//	    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    	g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    	g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	    	g2d.setComposite(AlphaComposite.DstOver);
			if (useHighRes) {
				int w = hiResImage.getWidth(this);
				int h = hiResImage.getHeight(this);
				if (!isWindows) {
					w /= 2;
					h /= 2;
				}
				g2d.drawImage(hiResImage, 0, 0, w, h, this);
			} else {
				int w = lowResImage.getWidth(this);
				int h = lowResImage.getHeight(this);
				g2d.drawImage(lowResImage, 0, 0, w, h, this);
			}
			g2d.setBackground(Color.WHITE);
			g2d.setColor(Color.BLACK);
			g2d.setFont(getStatusFont());
			String[] statusArray = status.split("\\.");
			int percentage = (int)Math.round(progress * 100);
			String progressString = "";
			if (progress == 0.0) {
				progressString = "initializing - ";
			} else {
				progressString = percentage + "% - loading ";
			}
			progressString += statusArray[statusArray.length - 1];
			int textX = (int)(statusX * getWidth());
			int textY = (int)(statusY * getHeight());
			System.out.println(progressString);
			g2d.setComposite(AlphaComposite.Src);
			g2d.drawString(progressString, textX, textY);
	    }
    
	    private Font getStatusFont() {
	    	if (font == null) {
	    		int fontSize = (int)(VarylabSplashScreen.this.fontSize * getHeight());
	    		font =new Font("verdana", Font.PLAIN, fontSize);
	    	}
	    	return font;
	    }
	    
    }
    
	@Override
	public void setStatus(String status) {
		String oldStatus = this.status;
		this.status = status;
		if (!oldStatus.equals(status)) {
			updateSplash();
		}
	}

	@Override
	public void setProgress(double progress) {
		double oldProgress = this.progress;
		this.progress = progress;
		if (oldProgress != progress) {
			updateSplash();
		}
	}
	
	public static void main(String[] args) throws Exception {
		VarylabSplashScreen splash = new VarylabSplashScreen();
		splash.setVisible(true);
	}

	protected void updateSplash() {
		Rectangle r = new Rectangle(getWidth(), getHeight());
		splashComponent.paintImmediately(r);
	}
	
}
