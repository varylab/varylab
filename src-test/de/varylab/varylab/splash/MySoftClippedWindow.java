package de.varylab.varylab.splash;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

public class MySoftClippedWindow extends JPanel     {
	
	private static final long 
		serialVersionUID = 1L;
	private Image
		image = null;
	
    public MySoftClippedWindow() {
        super();
        try {
			image = ImageIO.read(MySoftClippedWindow.class.getResourceAsStream("varylab_color_01.png"));
		} catch (IOException e1) {}
        setLayout(new GridBagLayout());
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        int width = getWidth();
        int height = getHeight();

        // Create a soft clipped image for the background
        BufferedImage img = java_2d_tricker(g2d, width, height);
        g2d.drawImage(img, 0, 0, null);

        g2d.dispose();
    }

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JWindow w = new JWindow();
                Container cp = w.getContentPane();
                cp.setLayout(new BorderLayout());
                cp.add(new MySoftClippedWindow());
                w.setAlwaysOnTop(true);
                com.sun.awt.AWTUtilities.setWindowOpaque (w, false);
                w.setSize(1017 / 2, 688 / 2);
                w.setLocationRelativeTo(null);
                w.setVisible(true);
            }
        });
    }

    /*
     * This code is taken from
     * http://weblogs.java.net/blog/campbell/archive/2006/07/java_2d_tricker.html
     */
    private BufferedImage java_2d_tricker(Graphics2D g2d, int width, int height) {
    	AffineTransform T = new AffineTransform();
        T.scale(0.5, 0.5);
        GraphicsConfiguration gc = g2d.getDeviceConfiguration();
        BufferedImage img = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g2 = img.createGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, width, height);
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        
        g2.setColor(Color.WHITE);
        g2.drawImage(image, T, this);
//        g2.fillOval(width / 4, height / 4, width / 2, height / 2);
        g2.setComposite(AlphaComposite.SrcAtop);
//        g2.setPaint(new GradientPaint(0, 0, Color.RED, 0, height, Color.YELLOW));
        g2.fillRect(0, 0, width, height);
        g2.drawImage(image, T, this);
        g2.dispose();
        return img;
    }
}