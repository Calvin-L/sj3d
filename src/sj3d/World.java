package sj3d;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Iterator;

public final class World implements MouseListener, MouseMotionListener {

    // Triangles & Vertices
    private final ArrayList<Model> models;

    // World variables
    private int i, l; // for quicker looping
    private int bgcolor;

    // Screen variables
    private final int width, height;
    private int[] pixels; // color values of each pixel
    private float[] zbuf; // depth of object at each pixel
    private final Model[] modelbuf; // Models at each pixel poin
    private Model mouseOverModel = null; // the model under the mouse
    private int mouseX, mouseY; // mouse coordinates
    private final ImageProducer producer;
    private final Image renderImage; // the image that gets rendered to
    private final Image finalImage; // the image that gets returned
    private final Graphics2D graphics;
    private Renderer renderer;
    private final RenderSettings settings;
    private final ImageObserver observer;

    private final ArrayList<WorldEventListener> listeners = new ArrayList<WorldEventListener>();

    /**
     * Create a world with default render settings
     *
     * @param w
     *            the width of the screen
     * @param h
     *            the height of the screen
     */
    public World(int w, int h, ImageObserver observer) {
        this(w, h, observer, new RenderSettings());
    }

    /**
     * Create a world with custom render settings
     *
     * @param w
     *            the width of the screen
     * @param h
     *            the height of the screen
     * @param settings
     *            the <code>RenderSettings</code> object to use
     */
    public World(int w, int h, ImageObserver observer, RenderSettings settings) {
        width = w;
        height = h;
        this.settings = settings;
        this.observer = observer;

        final int fullWidth = (int) (width * settings.aaFactor);
        final int fullHeight = (int) (height * settings.aaFactor);

        if (settings.hasMotionBlur()) {
            finalImage = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);
        } else {
            finalImage = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
        }
        graphics = ((BufferedImage) finalImage).createGraphics();

        if (settings.hasAA()) {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }

        final int size = fullWidth * fullHeight;

        pixels = new int[size];
        zbuf = new float[size];
        modelbuf = new Model[size];
        renderer = new Renderer(fullWidth, fullHeight, pixels, zbuf, modelbuf,
                settings);
        producer = new ImageProducer(fullWidth, fullHeight, pixels, settings);
        renderImage = Toolkit.getDefaultToolkit().createImage(producer);

        models = new ArrayList<Model>();

        setBackgroundColor(0);
        clearBuffers();
    }

    public void addListener(WorldEventListener l) {
        listeners.add(l);
    }

    public void removeListner(WorldEventListener l) {
        listeners.remove(l);
    }

    public void setBackgroundColor(int color) {
        bgcolor = color | renderer.ALPHA;
    }

    public Model getModelAtPoint(final int x, final int y) {
        return modelbuf[(int) ((float) y * settings.aaFactor * width
                * settings.aaFactor + (float) x * settings.aaFactor)];
    }

    public void render() {
        render(defaultCamera);
    }

    public void render(Camera c) {
        clearBuffers();
        for (i = 0, l = models.size(); i < l; i++) {
            renderer.render(c, models.get(i));
        }
        producer.update();

        Model m = getModelAtPoint(mouseX, mouseY);
        if (m != mouseOverModel) {
            if (mouseOverModel != null) {
                mouseOverModel.onMouseOut(mouseX, mouseY);
            }
            if (m != null) {
                m.onMouseOver(mouseX, mouseY);
            }
            mouseOverModel = m;
        }

        if (settings.hasAA()) {
            graphics.drawImage(renderImage, 0, 0, width, height, observer);
        } else {
            graphics.drawImage(renderImage, 0, 0, observer);
        }

        Iterator<WorldEventListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().worldRenderCompleted();
        }

    }

    public Image getImage() {
        return finalImage;
    }

    // Lighting

    public void setLighting(float x, float y, float z, float intensity,
            float ambient) {
        renderer.setLighting(new Vector(x, y, z), intensity, ambient);
    }

    // Camera stuff

    private Camera defaultCamera = new Camera(3, 0, 0, 0, 0, 0);

    public void setDefaultCamera(Camera c) {
        defaultCamera = c;
    }

    public Camera getDefaultCamera() {
        return defaultCamera;
    }

    // Models

    public void addModel(Model m) {
        models.add(m);
    }

    // Buffer Operations

    private void clearBuffer(int[] buffer, int value) {
        int size = buffer.length - 1;
        int cleared = 1;
        int index = 1;
        buffer[0] = value;
        while (cleared < size) {
            System.arraycopy(buffer, 0, buffer, index, cleared);
            size -= cleared;
            index += cleared;
            cleared <<= 1;
        }
        System.arraycopy(buffer, 0, buffer, index, size);
    }

    private void clearBuffer(float[] buffer, float value) {
        int size = buffer.length - 1;
        int cleared = 1;
        int index = 1;
        buffer[0] = value;
        while (cleared < size) {
            System.arraycopy(buffer, 0, buffer, index, cleared);
            size -= cleared;
            index += cleared;
            cleared <<= 1;
        }
        System.arraycopy(buffer, 0, buffer, index, size);
    }

    private void clearBuffer(Model[] buffer, Model value) {
        int size = buffer.length - 1;
        int cleared = 1;
        int index = 1;
        buffer[0] = value;
        while (cleared < size) {
            System.arraycopy(buffer, 0, buffer, index, cleared);
            size -= cleared;
            index += cleared;
            cleared <<= 1;
        }
        System.arraycopy(buffer, 0, buffer, index, size);
    }

    private void clearBuffers() {
        clearBuffer(pixels, bgcolor);
        clearBuffer(zbuf, 0);
        clearBuffer(modelbuf, null);
    }

    public void mouseClicked(MouseEvent e) {
        if (mouseOverModel != null) {
            mouseOverModel.onClick(e.getX(), e.getY());
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

}
