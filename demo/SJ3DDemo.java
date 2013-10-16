package demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import sj3d.Animator;
import sj3d.Camera;
import sj3d.Model;
import sj3d.RenderSettings;
import sj3d.Texture;
import sj3d.Triangle;
import sj3d.UVCoord;
import sj3d.World;

class SJ3DDemo {

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

    private static Model createModel(Texture tex) {
        final Model m = new Model();
        m.addFrame();

        UVCoord uv1 = new UVCoord(0f, 0f);
        UVCoord uv2 = new UVCoord(0.9f, 0.9f);
        UVCoord uv3 = new UVCoord(0.9f, 0f);

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

        m.material.texture = tex;
        m.material.mode = RenderSettings.TEXTURED;
        return m;
    }

    public static void main(String[] args) {

        final World world = new World(W, H, null);
        final Texture texture = createTexture();
        final Model model = createModel(texture);
        world.addModel(model);
        world.setBackgroundColor(0x201005);
        final Animator animator = new Animator(world);
        final Camera camera = world.getDefaultCamera();

        final JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.drawImage(world.getImage(), 0, 0, W, H, null);
                g.setColor(Color.WHITE);
                g.drawString("FPS: " + animator.getFPS(), 15, 30);
            }
        };
        panel.setPreferredSize(new Dimension(W, H));

        final JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);

        animator.start();

        long start = System.currentTimeMillis();

        while (true) {
            long now = System.currentTimeMillis();
            int delta = (int)(now - start);
            float x = (float)Math.cos((float)delta / 1000f) * 3f;
            float y = (float)Math.cos((float)delta / 3000f) * 1f + 2.5f;
            float z = (float)Math.sin((float)delta / 1000f) * 3f;

            world.setLighting(x, y, z, 1f, 0.3f);
            camera.setPos(x, y, z);
            camera.lookAt(0, 0, 0);
            panel.repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }

    }

}
