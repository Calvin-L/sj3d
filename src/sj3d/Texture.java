package sj3d;

import java.awt.Image;
import java.awt.image.PixelGrabber;

public final class Texture {
	
	public final int[] pixels;
	public final int width, height;
	
	public Texture(Image img, int width, int height) {
		pixels = new int[width * height];
		PixelGrabber grabber = new PixelGrabber(img, 0, 0,
                width, height,
                pixels,
                0,
                width);
		try {
			grabber.grabPixels();
			System.out.println("Texture created (" + width + "x" + height + ")");
		} catch (Exception e) {
			grabber.abortGrabbing();
			e.printStackTrace();
		}
		this.width = width;
		this.height = height;
	}

}
