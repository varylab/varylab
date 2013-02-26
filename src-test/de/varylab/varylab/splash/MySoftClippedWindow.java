package de.varylab.varylab.splash;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
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
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.drawImage(image, 0, 0, image.getWidth(this)/ 2, image.getHeight(this) / 2, this);
        g2d.setColor(Color.BLACK);
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

}