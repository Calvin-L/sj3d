package sj3d;

/*
 * This is a simple implementation of an image producer.
 * It allows for really fast drawing to the screen, since we have to set pixels individually.
 */

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.DirectColorModel;

class ImageProducer implements java.awt.image.ImageProducer {
	
	private ImageConsumer consumer;
	private int w, h;
	private int[] pixels;
	private ColorModel cm;
	private int hints, sfd;
	
	public ImageProducer(int w, int h, int pixels[], RenderSettings settings) {
		this.w = w;
		this.h = h;
		
		if (settings.hasMotionBlur())
			this.cm = new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF, 0xFF000000);
		else
			this.cm = new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF);
		
		this.pixels = pixels;
		hints = ImageConsumer.TOPDOWNLEFTRIGHT
			|ImageConsumer.COMPLETESCANLINES
			|ImageConsumer.SINGLEPASS
			|ImageConsumer.SINGLEFRAME;
		sfd = ImageConsumer.SINGLEFRAMEDONE;
	}

	public void addConsumer(ImageConsumer ic) {
		this.consumer = ic;

	}

	public boolean isConsumer(ImageConsumer ic) {
		return consumer == ic;
	}

	public void removeConsumer(ImageConsumer ic) {
		if (isConsumer(ic)) consumer = null;
	}

	public void requestTopDownLeftRightResend(ImageConsumer ic) {
		return;
	}

	public void startProduction(ImageConsumer ic) {
		if (consumer != ic) {
			consumer = ic;
			consumer.setDimensions(w, h);
			consumer.setProperties(null);
			consumer.setColorModel(cm);
			consumer.setHints(hints);
		}
		consumer.setPixels(0, 0, w, h, cm, pixels, 0, w);
		consumer.imageComplete(sfd);
	}
	
	public void update() {
		if (consumer != null) startProduction(consumer);
	}

}
