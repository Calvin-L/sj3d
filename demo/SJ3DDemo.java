package demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import sj3d.Camera;
import sj3d.Material;
import sj3d.Model;
import sj3d.RenderSettings;
import sj3d.Texture;
import sj3d.Triangle;
import sj3d.UVCoord;
import sj3d.World;

class FPSCounter {
    private static final long FPS_RESOLUTION_MS = 500;

    private boolean initialized = false;
    private long startTimeNs;
    private long frameCount = 0;

    private volatile float fps = Float.NaN;

    public float getFPS() {
        return fps;
    }

    public void tick() {
        long nowNs = System.nanoTime();
        if (initialized) {
            ++frameCount;
            if (nowNs - startTimeNs > FPS_RESOLUTION_MS * 1_000_000) {
                fps = frameCount * (float)Math.pow(10, 9) / (nowNs - startTimeNs);
                startTimeNs = nowNs;
                frameCount = 0;
            }
        } else {
            startTimeNs = nowNs;
            initialized = true;
        }
    }

}

public class SJ3DDemo {

    private static final int W = 720;
    private static final int H = 480;

    private static Texture createTexture() {
        final int texWidth = 80;
        final int texHeight = 80;
        Texture tex = new Texture(texWidth, texHeight);
        int i = 0;
        for (int y = 0; y < texHeight; ++y) {
            for (int x = 0; x < texWidth; ++x) {
                tex.pixels[i] = (x % 10 == 0 || y % 10 == 0) ? 0x201005 : 0xffcc99;
                ++i;
            }
        }
        return tex;
    }

    /**
     * Create a unit cube with corners (0, 0, 0) and (1, 1, 1).  The cube
     * will not have UV coordinates, so it can't be used with a textured
     * material.
     *
     * @return a new cube model
     */
    private static Model createCube() {
        final Model m = new Model();

        m.addFrame();

        m.addVertex(0, 0, 0);
        m.addVertex(0, 0, 1);
        m.addVertex(0, 1, 0);
        m.addVertex(0, 1, 1);
        m.addVertex(1, 0, 0);
        m.addVertex(1, 0, 1);
        m.addVertex(1, 1, 0);
        m.addVertex(1, 1, 1);

        //  0------1
        //  |\     |\
        //  | 4----|-5
        //  2------3 |
        //   \|     \|
        //    6------7

        m.addTriangle(new Triangle(m, 0, 1, 2));
        m.addTriangle(new Triangle(m, 1, 3, 2));

        m.addTriangle(new Triangle(m, 3, 1, 5));
        m.addTriangle(new Triangle(m, 5, 7, 3));

        m.addTriangle(new Triangle(m, 2, 3, 7));
        m.addTriangle(new Triangle(m, 7, 6, 2));

        m.addTriangle(new Triangle(m, 5, 4, 6));
        m.addTriangle(new Triangle(m, 5, 6, 7));

        m.addTriangle(new Triangle(m, 0, 2, 6));
        m.addTriangle(new Triangle(m, 6, 4, 0));

        m.addTriangle(new Triangle(m, 1, 0, 4));
        m.addTriangle(new Triangle(m, 4, 5, 1));

        return m;
    }

    private static Model createModel(Texture tex) {
        final Model m = new Model();
        m.material = Material.flatTextured(tex);

        m.addFrame();

        UVCoord uv1 = new UVCoord(0f, 0f);
        UVCoord uv2 = new UVCoord(1f, 1f);
        UVCoord uv3 = new UVCoord(1f, 0f);

        int i = 0;
        for (float f = 0f; f < 2*Math.PI; f += 0.7) {
            float x = (float)Math.cos(f);
            float z = (float)Math.sin(f);
            m.addVertex(x/10f, -1f, z/10f); // 0
            m.addVertex(x/10f, 1f, z/10f);  // 1
            m.addVertex(x, 0f, z);  // 2
            m.addTriangle(new Triangle(m, i + 1, i, i + 2, uv1, uv2, uv3)); // front
            m.addTriangle(new Triangle(m, i, i + 1, i + 2, uv2, uv1, uv3)); // back
            i += 3;
        }

        return m;
    }

    public static void main(String[] args) {

        final RenderSettings settings = new RenderSettings();
        settings.setBackgroundColor(0x201005);

        final World world = new World(W, H, settings);
        final Texture texture = createTexture();
        final Model model = createModel(texture);
        world.addModel(model);
        final Camera camera = world.getCamera();
        final FPSCounter fpsCounter = new FPSCounter();

        Model unitCube = createCube();
        for (int i = 0; i < 3; ++i) {
            Model cube = new Model(unitCube);
            switch (i) {
                case 0:
                    cube.setScale(3.0f, 0.1f, 0.1f);
                    cube.material = Material.flat(0xFF0000);
                    break;
                case 1:
                    cube.setScale(0.1f, 3.0f, 0.1f);
                    cube.material = Material.flat(0x00FF00);
                    break;
                case 2:
                    cube.setScale(0.1f, 0.1f, 3.0f);
                    cube.material = Material.flat(0x0000FF);
                    break;
            }
            world.addModel(cube);
        }

        final JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                synchronized (world) {
                    g.drawImage(world.getImage(), 0, 0, W, H, null);
                }
                g.setColor(Color.WHITE);
                float fps = fpsCounter.getFPS();
                g.drawString(
                        Float.isNaN(fps)
                                ? "FPS: ???"
                                : String.format("FPS: %.2f", fpsCounter.getFPS()),
                        15,
                        30);
            }
        };
        panel.setPreferredSize(new Dimension(W, H));

        final JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        long start = System.currentTimeMillis();

        world.setLighting(1f, 1f, 1f, 1f, 1f);

        while (true) {
            long now = System.currentTimeMillis();
            int delta = (int)(now - start);
            float x = (float)Math.cos((float)delta / 1000f) * 3f;
            float y = (float)Math.cos((float)delta / 3000f) * 1f + 2.5f;
            float z = (float)Math.sin((float)delta / 1000f) * 3f;

            synchronized (world) { // avoid conflicts with the paint thread
                camera.setPos(x * 3, 5, z * 3);
                camera.lookAt(0, 0, 0);

                model.setRotation(x, x, x);
                model.setScale(y, y, y);
                model.setPos(0, z, 0);

                world.render();
            }

            fpsCounter.tick();
            panel.repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }

    }

}
