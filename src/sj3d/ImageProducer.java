package sj3d;

import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;

/**
 * This is an adaptor between raw <code>int[] pixels</code> and {@link java.awt.Image}.
 * It is the fastest way I know to pump an array of pixels to the screen.  In fact,
 * writing to a pixels array and then using this adaptor to convert the pixels to an
 * image is even faster than directly modifying the pixels of a
 * {@link java.awt.image.BufferedImage}.
 *
 * <p>Usage:
 * <pre>
 *   int width = ..., height = ...;
 *   int[] pixels = new int[width * height];
 *   Image img = {@link java.awt.Toolkit}.getDefaultToolkit().createImage(
 *       new ImageProducer(width, height, pixels, ...));
 *
 *   while (...) {
 *       ... modify pixels ...
 *       img.flush();
 *       graphics.drawImage(img, ...);
 *   }
 * </pre>
 */
class ImageProducer implements java.awt.image.ImageProducer {

    // SUPER PEDANTIC IMPLEMENTATION NOTES (2021/6/15):
    //
    // This class does not track the set of interested consumers, even though
    // the official AWT ImageProducer JavaDocs says it should.  I think this
    // is a perfectly reasonable simplification, and I want to justify it to
    // future readers.
    //
    // Typical ImageProducer implementations are asynchronous.  Consumers
    // register interest using `addConsumer`, then kick off an asynchronous
    // production using `startProduction`, which will complete on a different
    // thread.  Asynchronous ImageProducers need to store the set of
    // interested consumers so that the asynchronous job can inform them when
    // it completes.  Consumers are expected to call `removeConsumer` to
    // unregister themselves when they receive an `imageComplete` callback.
    //
    // By contrast, this ImageProducer is synchronous.  The pixels are sent
    // immediately when a consumer calls `startProduction`.  Therefore, there
    // is no need to record that the consumer wants the pixels later, and
    // there is no need to remember the consumer since it is supposed to
    // remove itself right away.
    //
    // Things could go awry if a consumer relies on a correct return value
    // from `isConsumer`, or if a consumer relies on a different component
    // calling `startProduction` on its behalf.  However, in practice I have
    // not seen the standard AWT classes do either of these things.

    private final int w, h;
    private final int[] pixels;
    private final ColorModel cm;
    private final int hints;

    public ImageProducer(int w, int h, int[] pixels, RenderSettings settings) {
        this.w = w;
        this.h = h;

        if (settings.hasMotionBlur()) {
            this.cm = new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF, 0xFF000000);
        } else {
            this.cm = new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF);
        }

        this.pixels = pixels;
        this.hints = ImageConsumer.TOPDOWNLEFTRIGHT
                   | ImageConsumer.COMPLETESCANLINES
                   | ImageConsumer.SINGLEPASS
                   | ImageConsumer.SINGLEFRAME;
    }

    @Override
    public void addConsumer(ImageConsumer ic) {
    }

    @Override
    public boolean isConsumer(ImageConsumer ic) {
        return false;
    }

    @Override
    public void removeConsumer(ImageConsumer ic) {
    }

    @Override
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
        ic.setDimensions(w, h);
        ic.setColorModel(cm);
        ic.setHints(hints);
        ic.setPixels(0, 0, w, h, cm, pixels, 0, w);
        ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }

    @Override
    public void startProduction(ImageConsumer ic) {
        addConsumer(ic);
        requestTopDownLeftRightResend(ic);
    }

}
