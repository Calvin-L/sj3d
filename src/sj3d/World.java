package sj3d;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * A top-level container for everything that goes into a 3D scene.
 *
 * In particular:
 * <ul>
 *   <li>Models that get rendered</li>
 *   <li>A camera for the view perspective</li>
 *   <li>Render settings</li>
 * </ul>
 *
 * @see #render()
 * @see #getImage()
 */
public final class World {

    // Triangles & Vertices
    private final ArrayList<Model> models;

    // Screen variables
    private final Camera camera; // view perspective
    private final int width, height;
    private final int[] pixels; // color values of each pixel
    private final float[] zbuf; // depth of object at each pixel
    private final Model[] modelbuf; // Models at each pixel point
    private final Image renderImage; // the image that gets rendered to
    private final Image finalImage; // the image that gets returned
    private final Graphics2D graphics;
    private final Renderer renderer;
    private final RenderSettings settings;

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
    public World(int w, int h, RenderSettings settings) {
        width = w;
        height = h;
        this.settings = settings;

        final int fullWidth = (int) (width * settings.aaFactor);
        final int fullHeight = (int) (height * settings.aaFactor);

        finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
        renderImage = Toolkit.getDefaultToolkit().createImage(
                new ImageProducer(fullWidth, fullHeight, pixels, settings));

        models = new ArrayList<>();
        camera = new Camera();
    }

    /**
     * Get the model at the given screen coordinates in the most recently
     * rendered image.  If there are multiple models at the given coordinates,
     * the one closest to the camera is returned.
     *
     * <p>Always returns <code>null</code> before the first call to
     * {@link #render()}.
     *
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     * @return the model at the given coordinates, or <code>null</code>
     */
    public Model getModelAtPoint(final int x, final int y) {
        return modelbuf[(int) ((float) y * settings.aaFactor * width
                * settings.aaFactor + (float) x * settings.aaFactor)];
    }

    /**
     * Render the scene as a 2D image.
     *
     * @see #getImage()
     */
    public void render() {
        clearBuffers();
        for (Model model : models) {
            renderer.render(camera, model);
        }

        renderImage.flush();
        if (settings.hasAA()) {
            graphics.drawImage(renderImage, 0, 0, width, height, null);
        } else {
            graphics.drawImage(renderImage, 0, 0, null);
        }
    }

    /**
     * Get the rendered image.
     *
     * <p>The image contents are unspecified before the first call to
     * {@link #render()}.
     *
     * <p>The {@link #render()} method modifies the image in-place.  If you
     * need to retain a copy of a particular frame, you must copy the returned
     * image.
     *
     * @return the most recently rendered image
     * @see #render()
     */
    public Image getImage() {
        return finalImage;
    }

    /**
     * Set the lighting.  The x, y, and z arguments define the light direction
     * (not the light position).  The light source will be infinitely far
     * away, shining in the given direction.
     *
     * @param x
     *            the x-component of the light direction
     * @param y
     *            the y-component of the light direction
     * @param z
     *            the z-component of the light direction
     * @param intensity
     *            the intensity of the light (usually 1)
     * @param ambient
     *            the intensity of the ambient light (usually 1)
     */
    public void setLighting(float x, float y, float z, float intensity, float ambient) {
        renderer.setLighting(new Vector(x, y, z), intensity, ambient);
    }

    /**
     * Get the scene camera.  There is no corresponding <code>setCamera</code>
     * call; modify the camera in-place between calls to {@link #render()} to
     * achieve camera motion.
     *
     * @return the camera
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * Add the given model to the scene
     *
     * @param m
     *            the model to add
     */
    public void addModel(Model m) {
        models.add(m);
    }

    private void clearBuffers() {
        Util.fill(pixels, settings.bgcolor | settings.mblur);
        Util.fill(zbuf, 0);
        Util.fill(modelbuf, null);
    }

}
