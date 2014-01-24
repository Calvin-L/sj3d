package sj3d;

/**
 *      _____
 *     /     \
 *    |  X X  |
 *     \     /
 *      |||||
 *
 * This code is highly bizarre. Read/edit at your own risk.
 *
 */
final class Renderer {

    /*
     * I don't actually have a good reason why these are defined out here. It
     * just seemed to make sense at the time.
     */

    // Lighting
    private Vector lightVector;
    private float lightAmbient, lightIntensity;
    private float Kd, Ka;
    private float v;
    private int red, green, blue;

    // Triangle rendering
    private Texture texture;
    private int color = 0xffffff;
    private int mode; // shade mode
    private Vertex a, b, c;
    private Vector n; // Triangle normal; used in lighting calculation
    private UVCoord uva, uvb, uvc;
    private Model currentModel;
    private float ax, bx; // Projected position
    private int ay, by, cy; // Projected position
    private float az, bz, cz; // Projected position
    private float dx_ac, dx_ab, dx_bc, sx, ex; // X delta values
    private float dz_ac, dz_ab, dz_bc, sz, ez; // Z delta values
    private float av, bv;
    private float dv_ac, dv_ab, dv_bc, sv, ev; // Light intensity values

    // Texturing
    private float dtexu_ab, dtexu_ac, dtexu_bc, dtexv_ab, dtexv_ac, dtexv_bc,
            stexu, stexv, etexu, etexv, texau, texav,
            texbu, texbv;

    // Settings
    private final int width, height;
    private final int halfwidth, halfheight;
    private final float sclX, sclY;
    private final RenderSettings settings;
    final int ALPHA;

    // Buffers
    private final int[] pixels;
    private final float[] zbuf; // depth of object at each pixel
    private final Model[] modelbuf; // Models at each pixel poin

    Renderer(final int width, final int height, final int[] pixels,
            final float[] zbuf, final Model[] modelbuf,
            final RenderSettings settings) {

        // Screen size
        this.width = width;
        this.height = height;
        halfwidth = width / 2;
        halfheight = height / 2;

        // Scale factor to make coordinates more natural
        final float FOV = 1.04719755f, d = 1.0f / ((float) Math.tan(FOV));
        sclX = (d / (width / height) + 1) * halfwidth;
        sclY = (d + 1) * halfheight;

        // this.vertices = vertices;
        this.pixels = pixels;
        this.zbuf = zbuf;
        this.modelbuf = modelbuf;
        this.lightVector = new Vector(0, 1, 0);
        this.lightAmbient = 0;
        this.lightIntensity = 1;

        // Configure settings
        this.settings = settings;
        ALPHA = settings.mblur;

        // if (settings.hasShadows()) shadowStencil = new byte[width*height];
        // else shadowStencil = null;

    }

    /**
     * Set the scene lighting.
     *
     * @param direction
     *            the direction to which the light will point. The direction
     *            vector does not need to be normalized.
     * @param intensity
     *            the intensity of the light
     * @param ambient
     *            the ambient intensity
     */
    void setLighting(final Vector direction, final float intensity,
            final float ambient) {
        lightVector = direction;
        lightVector.normalize();
        lightIntensity = intensity;
        lightAmbient = ambient;
    }

    /**
     * Render a model to the specified camera.
     *
     * @param camera
     *            the camera
     * @param object
     *            the model to render
     */
    void render(final Camera camera, final Model object) {

        final Material material = object.material;
        currentModel = object;
        projectAllVertices(camera);

        mode = material.mode & settings.shadeMode;

        Kd = lightIntensity * material.diffuseValue;
        Ka = lightAmbient * material.ambientValue;

        if ((material.mode & RenderSettings.TEXTURED) == 0) {
            color = material.color;
            red = (color & 0xff0000) >> 16;
            green = (color & 0x00ff00) >> 8;
            blue = (color & 0x0000ff);
        } else {
            texture = material.texture;
        }

        for (int i = 0, l = object.numTriangles(); i < l; i++) {

            final Triangle t = object.getTriangle(i);
            n = t.getNormal();
            final float cos = n.dot(camera.getForwardVector());

            // back-face culling: only render one side of triangle
            if (cos <= 0.001) {
                renderTriangle(t);
            }

        }

    }

    /**
     * Project all vertices into their correct screen coordinates. This function
     * has the effect of updating the <code>projX</code>, <code>projY</code>,
     * and <code>projZ</code> variables in each vertex of the model.
     *
     * @param camera
     */
    private void projectAllVertices(final Camera camera) {
        final Matrix m = camera.getMatrix().multiply(currentModel.getMatrix());

        for (int i = 0, l = currentModel.numVertices(); i < l; i++) {

            final Vertex v0 = currentModel.getVertex(i);

            // Transform to camera coordinates
            final Vertex v = m.multiply(v0);

            // Calculate position on screen & depth from camera
            // This bit performs the transformation from orthographic to
            // perspective
            v0.projZ = 1.0f / v.z;
            v0.projX = v.x * sclX * v0.projZ + halfwidth;
            v0.projY = v.y * sclY * v0.projZ + halfheight;

        }

    }

    /**
     * Render a specific triangle.
     *
     * @param t the triangle to render
     */
    private void renderTriangle(final Triangle t) {

        // Vertices & vertex texture coordinates
        a = t.getVertexA();
        b = t.getVertexB();
        c = t.getVertexC();
        uva = t.getUVA();
        uvb = t.getUVB();
        uvc = t.getUVC();

        Vertex tempVertex;
        UVCoord tempUV;

        /*
         * Sort vertices by projected y-coordinate; "a" on top, followed by "b"
         * and then "c". This gives us two cases, where point "b" is either left
         * or right of line "a-c".
         */

        if (b.projY < a.projY) {
            tempVertex = a;
            a = b;
            b = tempVertex;
            tempUV = uva;
            uva = uvb;
            uvb = tempUV;
        }
        if (c.projY < b.projY) {
            tempVertex = b;
            b = c;
            c = tempVertex;
            tempUV = uvb;
            uvb = uvc;
            uvc = tempUV;
        }
        if (b.projY < a.projY) {
            tempVertex = a;
            a = b;
            b = tempVertex;
            tempUV = uva;
            uva = uvb;
            uvb = tempUV;
        }

        // Triangle screen coordinates
        ax = a.projX;
        ay = (int) a.projY;
        az = a.projZ;
        bx = b.projX;
        by = (int) b.projY;
        bz = b.projZ;
        final int cx = (int) c.projX;
        cy = (int) c.projY;
        cz = c.projZ;

        // Ignore triangles behind camera
        if (az < 0 && bz < 0 && cz < 0)
            return;

        // TODO: Lame near-plane clipping
        if (az < 0 || bz < 0 || cz < 0 || az > 1 || bz > 1 || cz > 1)
            return;

        // Skip if left or right of screen
        if ((ax > width && cx > width && bx > width)
                || (ax < 0 && bx < 0 && cx < 0)) {
            return;
        }

        // Skip if above or below screen
        if (ay >= height || cy < 0) {
            return;
        }

        // Skip if entirely horizontal
        if (ay == cy) {
            return;
        }

        // delta-x values -- the change in screen-x as we go down the rows of
        // screen-y
        dx_ac = ((ay == cy) ? 0 : (ax - cx) / (ay - cy)); // a to c
        dx_ab = ((ay == by) ? bx - ax : (ax - bx) / (ay - by)); // a to
                                                                        // b
        dx_bc = ((by == cy) ? 0 : (bx - cx) / (by - cy)); // b to c

        // delta-z values -- the change in distance from camera as we go down
        // the rows of screen-y
        dz_ac = ((ay == cy) ? 0 : (az - cz) / (ay - cy)); // a to c
        dz_ab = ((ay == by) ? 0 : (az - bz) / (ay - by)); // a to b
        dz_bc = ((by == cy) ? 0 : (bz - cz) / (by - cy)); // b to c

        final int y = Math.max(ay, 0);
        final int relative_start_y = y - ay;
        final int screen_end_y = Math.min(by, height);

        switch (mode) {
        case RenderSettings.SMOOTH_TEXTURED:
            setupSmooth();
            setupTextured();
            renderTriangleSmoothTextured(relative_start_y, y, screen_end_y);
            break;
        case RenderSettings.TEXTURED:
            setupFlat();
            setupTextured();
            renderTriangleFlatTextured(relative_start_y, y, screen_end_y);
            break;
        case RenderSettings.SMOOTH:
            setupSmooth();
            renderTriangleSmoothUntextured(relative_start_y, y, screen_end_y);
            break;
        default:
            setupFlat();
            renderTriangleFlatUntextured(relative_start_y, y, screen_end_y);
        }

    }

    private void setupFlat() {
        final float cos = n.dot(lightVector);
        v = cos <= 0 ? Ka : cos * (Kd - Ka) + Ka;
        color =    ((int) (blue  * v)
                | (((int) (green * v)) << 8)
                | (((int) (red   * v)) << 16)
                | (ALPHA));
    }

    private void setupSmooth() {

        // Calculate lighting for each vertex

        float cos = a.n.dot(lightVector);

        if (cos <= 0)
            cos = 0;

        final float diff = Kd - Ka;

        av = Ka + cos * diff;

        cos = b.n.dot(lightVector);
        bv = cos <= 0 ? Ka : cos * (Kd - Ka) + Ka;

        cos = c.n.dot(lightVector);
        float cv = cos <= 0 ? Ka : cos * (Kd - Ka) + Ka;

        // Change in color value as we progress down rows

        dv_ac = ((ay == cy) ? 0 : (av - cv) / (ay - cy)); // a to c
        dv_ab = ((ay == by) ? 0 : (av - bv) / (ay - by)); // a to b
        dv_bc = ((by == cy) ? 0 : (bv - cv) / (by - cy)); // b to c

    }

    private void setupTextured() {

        texau = uva.u * az;
        texav = uva.v * az;
        texbu = uvb.u * bz;
        texbv = uvb.v * bz;
        final float texcu = uvc.u * cz;
        final float texcv = uvc.v * cz;

        dtexu_ab = (texau - texbu) / (ay - by);
        dtexv_ab = (texav - texbv) / (ay - by);

        dtexu_ac = (texau - texcu) / (ay - cy);
        dtexv_ac = (texav - texcv) / (ay - cy);

        dtexu_bc = (texbu - texcu) / (by - cy);
        dtexv_bc = (texbv - texcv) / (by - cy);

    }

    private void renderTriangleFlatUntextured(int relative_start_y, int y, int screen_end_y) {

        if (dx_ab > dx_ac) { // case 1: point "b" is right of line a-c

            // a to b
            for (sx = ax + relative_start_y * dx_ac, ex = ax + relative_start_y
                    * dx_ab, sz = az + relative_start_y * dz_ac, ez = az
                    + relative_start_y * dz_ab; y < screen_end_y; y++, sx += dx_ac, ex += dx_ab, sz += dz_ac, ez += dz_ab) {
                drawLineFlat(y);
            }

            // b to c
            screen_end_y = Math.min(cy + 1, height);
            for (ex = bx, ez = bz; y < screen_end_y; y++, sx += dx_ac, ex += dx_bc, sz += dz_ac, ez += dz_bc) {
                drawLineFlat(y);
            }

        } else if (dx_ab < dx_ac) { // case 2: point "b" is left of line a-c
            // a to b
            for (sx = ax + relative_start_y * dx_ab, ex = ax + relative_start_y
                    * dx_ac, sz = az + relative_start_y * dz_ab, ez = az
                    + relative_start_y * dz_ac; y < screen_end_y; y++, sx += dx_ab, ex += dx_ac, sz += dz_ab, ez += dz_ac) {
                drawLineFlat(y);
            }

            // b to c
            screen_end_y = Math.min(cy + 1, height);
            for (sx = bx, sz = bz; y < screen_end_y; y++, sx += dx_bc, ex += dx_ac, sz += dz_bc, ez += dz_ac) {
                drawLineFlat(y);
            }

        }

    }

    private void renderTriangleSmoothUntextured(int relative_start_y, int y, int screen_end_y) {

        if (dx_ab > dx_ac) { // case 1: point "b" is right of line a-c

            // a to b
            for (sx = ax + relative_start_y * dx_ac, ex = ax + relative_start_y
                    * dx_ab, sz = az + relative_start_y * dz_ac, ez = az
                    + relative_start_y * dz_ab, sv = av + relative_start_y
                    * dv_ac, ev = av + relative_start_y * dv_ab; y < screen_end_y; y++, sx += dx_ac, ex += dx_ab, sz += dz_ac, ez += dz_ab, sv += dv_ac, ev += dv_ab) {
                drawLineSmooth(y, Math.round(sx), Math.round(ex), sz, ez, sv, ev, color);
            }

            // b to c
            screen_end_y = Math.min(cy + 1, height);
            for (ex = bx, ez = bz, ev = bv; y < screen_end_y; y++, sx += dx_ac, ex += dx_bc, sz += dz_ac, ez += dz_bc, sv += dv_ac, ev += dv_bc) {
                drawLineSmooth(y, Math.round(sx), Math.round(ex), sz, ez, sv, ev, color);
            }

        } else if (dx_ab < dx_ac) { // case 2: point "b" is left of line a-c
            // a to b
            for (sx = ax + relative_start_y * dx_ab, ex = ax + relative_start_y
                    * dx_ac, sz = az + relative_start_y * dz_ab, ez = az
                    + relative_start_y * dz_ac, sv = av + relative_start_y
                    * dv_ab, ev = av + relative_start_y * dv_ac; y < screen_end_y; y++, sx += dx_ab, ex += dx_ac, sz += dz_ab, ez += dz_ac, sv += dv_ab, ev += dv_ac) {
                drawLineSmooth(y, Math.round(sx), Math.round(ex), sz, ez, sv, ev, color);
            }

            // b to c
            screen_end_y = Math.min(cy + 1, height);
            for (sx = bx, sz = bz, sv = bv; y < screen_end_y; y++, sx += dx_bc, ex += dx_ac, sz += dz_bc, ez += dz_ac, sv += dv_bc, ev += dv_ac) {
                drawLineSmooth(y, Math.round(sx), Math.round(ex), sz, ez, sv, ev, color);
            }

        } // Note that if point "b" lies on line a-c, the triangle is treated as
            // invisibly thin for optimization

    }

    private void renderTriangleFlatTextured(int relative_start_y, int y, int screen_end_y) {

        if (dx_ab > dx_ac) { // case 1: point "b" is right of line a-c
            // a to b
            for (sx = ax + relative_start_y * dx_ac, ex = ax + relative_start_y
                    * dx_ab, sz = az + relative_start_y * dz_ac, ez = az
                    + relative_start_y * dz_ab, stexu = texau
                    + relative_start_y * dtexu_ac, stexv = texav
                    + relative_start_y * dtexv_ac, etexu = texau
                    + relative_start_y * dtexu_ab, etexv = texav
                    + relative_start_y * dtexv_ab; y < screen_end_y; y++) {
                drawLineFlatTextured(y, Math.round(sx), Math.round(ex), sz, ez, stexu, etexu, stexv, etexv);
                sx += dx_ac;
                ex += dx_ab;
                sz += dz_ac;
                ez += dz_ab;
                stexu += dtexu_ac;
                stexv += dtexv_ac;
                etexu += dtexu_ab;
                etexv += dtexv_ab;
            }

            // b to c
            screen_end_y = Math.min(cy + 1, height);
            etexu = texbu;
            etexv = texbv;
            for (ex = bx, ez = bz; y < screen_end_y; y++) {
                drawLineFlatTextured(y, Math.round(sx), Math.round(ex), sz, ez, stexu, etexu, stexv, etexv);
                sx += dx_ac;
                ex += dx_bc;
                sz += dz_ac;
                ez += dz_bc;
                stexu += dtexu_ac;
                stexv += dtexv_ac;
                etexv += dtexv_bc;
                etexu += dtexu_bc;
            }

        } else if (dx_ab < dx_ac) { // case 2: point "b" is left of line a-c

            // a to b
            for (sx = ax + relative_start_y * dx_ab, ex = ax + relative_start_y
                    * dx_ac, sz = az + relative_start_y * dz_ab, ez = az
                    + relative_start_y * dz_ac, stexu = texau
                    + relative_start_y * dtexu_ab, stexv = texav
                    + relative_start_y * dtexv_ab, etexu = texau
                    + relative_start_y * dtexu_ac, etexv = texav
                    + relative_start_y * dtexv_ac; y < screen_end_y; y++) {
                drawLineFlatTextured(y, Math.round(sx), Math.round(ex), sz, ez, stexu, etexu, stexv, etexv);
                sx += dx_ab;
                ex += dx_ac;
                sz += dz_ab;
                ez += dz_ac;
                stexu += dtexu_ab;
                stexv += dtexv_ab;
                etexu += dtexu_ac;
                etexv += dtexv_ac;
            }

            // b to c
            screen_end_y = Math.min(cy + 1, height);
            stexu = texbu;
            stexv = texbv;
            for (sx = bx, sz = bz; y < screen_end_y; y++) {
                drawLineFlatTextured(y, Math.round(sx), Math.round(ex), sz, ez, stexu, etexu, stexv, etexv);
                sx += dx_bc;
                ex += dx_ac;
                sz += dz_bc;
                ez += dz_ac;
                stexu += dtexu_bc;
                stexv += dtexv_bc;
                etexu += dtexu_ac;
                etexv += dtexv_ac;
            }

        } // Note that if point "b" lies on line a-c, the triangle is treated as
            // invisibly thin for optimization

    }

    private void renderTriangleSmoothTextured(int relative_start_y, int y, int screen_end_y) {

        if (dx_ab > dx_ac) { // case 1: point "b" is right of line a-c
            // a to b
            for (float sx = ax + relative_start_y * dx_ac, ex = ax + relative_start_y
                    * dx_ab, sz = az + relative_start_y * dz_ac, ez = az
                    + relative_start_y * dz_ab, sv = av + relative_start_y
                    * dv_ac, ev = av + relative_start_y * dv_ab, stexu = texau
                    + relative_start_y * dtexu_ac, stexv = texav
                    + relative_start_y * dtexv_ac, etexu = texau
                    + relative_start_y * dtexu_ab, etexv = texav
                    + relative_start_y * dtexv_ab; y < screen_end_y; y++) {
                drawLineSmoothTextured(y, Math.round(sx), Math.round(ex), sz, ez, sv, ev, stexu, etexu, stexv, etexv);
                sx += dx_ac;
                ex += dx_ab;
                sz += dz_ac;
                ez += dz_ab;
                sv += dv_ac;
                ev += dv_ab;
                stexu += dtexu_ac;
                stexv += dtexv_ac;
                etexu += dtexu_ab;
                etexv += dtexv_ab;
            }

            // b to c
            screen_end_y = Math.min(cy + 1, height);
            etexu = texbu;
            etexv = texbv;
            for (float ex = bx, ez = bz, ev = bv; y < screen_end_y; y++) {
                drawLineSmoothTextured(y, Math.round(sx), Math.round(ex), sz, ez, sv, ev, stexu, etexu, stexv, etexv);
                sx += dx_ac;
                ex += dx_bc;
                sz += dz_ac;
                ez += dz_bc;
                sv += dv_ac;
                ev += dv_bc;
                stexu += dtexu_ac;
                stexv += dtexv_ac;
                etexv += dtexv_bc;
                etexu += dtexu_bc;
            }

        } else if (dx_ab < dx_ac) { // case 2: point "b" is left of line a-c

            // a to b
            for (float sx = ax + relative_start_y * dx_ab, ex = ax + relative_start_y
                    * dx_ac, sz = az + relative_start_y * dz_ab, ez = az
                    + relative_start_y * dz_ac, sv = av + relative_start_y
                    * dv_ab, ev = av + relative_start_y * dv_ac, stexu = texau
                    + relative_start_y * dtexu_ab, stexv = texav
                    + relative_start_y * dtexv_ab, etexu = texau
                    + relative_start_y * dtexu_ac, etexv = texav
                    + relative_start_y * dtexv_ac; y < screen_end_y; y++) {
                drawLineSmoothTextured(y, Math.round(sx), Math.round(ex), sz, ez, sv, ev, stexu, etexu, stexv, etexv);
                sx += dx_ab;
                ex += dx_ac;
                sz += dz_ab;
                ez += dz_ac;
                sv += dv_ab;
                ev += dv_ac;
                stexu += dtexu_ab;
                stexv += dtexv_ab;
                etexu += dtexu_ac;
                etexv += dtexv_ac;
            }

            // b to c
            screen_end_y = Math.min(cy + 1, height);
            stexu = texbu;
            stexv = texbv;
            for (float sx = bx, sz = bz, sv = bv; y < screen_end_y; y++) {
                drawLineSmoothTextured(y, Math.round(sx), Math.round(ex), sz, ez, sv, ev, stexu, etexu, stexv, etexv);
                sx += dx_bc;
                ex += dx_ac;
                sz += dz_bc;
                ez += dz_ac;
                sv += dv_bc;
                ev += dv_ac;
                stexu += dtexu_bc;
                stexv += dtexv_bc;
                etexu += dtexu_ac;
                etexv += dtexv_ac;
            }

        } // Note that if point "b" lies on line a-c, the triangle is treated as
            // invisibly thin for optimization

    }

    // Line drawing

    private void drawLineFlat(int y) {
        final float dz = (sz - ez) / (sx - ex);
        float z = sz;
        final int screen_start = (int) Math.max(sx, 0);
        final int screen_end = (int) Math.min(ex, width - 1);
        final int row = y * width;
        final int start = row + screen_start;
        final int end = row + screen_end;
        for (int index = start; index <= end; index++) {
            if (zbuf[index] < z) {
                zbuf[index] = z;
                pixels[index] = color;
                modelbuf[index] = currentModel;
            }
            z += dz;
        }
    }

    private void drawLineSmooth(int y, int start, int end, float start_z,
            float end_z, float start_v, float end_v, int color) {
        final float dz = (start_z - end_z) / (start - end);
        float z = start_z;
        final float dv = (start_v - end_v) / (start - end);
        float current_v = start_v;
        final int red = (color & 0xff0000) >> 16;
        final int green = (color & 0x00ff00) >> 8;
        final int blue = (color & 0x0000ff);
        final int screen_start = Math.max(start, 0);
        final int screen_end = Math.min(end, width - 1);
        int index = y * width + screen_start;

        for (int x = screen_start; x <= screen_end; x++) {

            if (zbuf[index] < z) {
                zbuf[index] = z;
                pixels[index] = (int) (blue * current_v)
                        | (((int) (green * current_v)) << 8)
                        | (((int) (red * current_v)) << 16) | ALPHA;
                modelbuf[index] = currentModel;
            }

            index++;
            z += dz;
            current_v += dv;

        }
    }

    private void drawLineFlatTextured(int y, int sx, int ex, float sz, float ez, float stexu, float etexu, float stexv, float etexv) {

        // Width of this line
        final int screen_start = Math.max(sx, 0);
        final int screen_end = Math.min(ex, width - 1);
        final int w = screen_start - screen_end;

        // Z values
        final float dz = (sz - ez) / w;
        float z = sz;

        // Texturing
        final float dtexu = (stexu - etexu) / w;
        float texu = stexu;
        final float dtexv = (stexv - etexv) / w;
        float texv = stexv;

        int index = y * width + screen_start;

        final int texXMax = texture.width - 1;
        final int texYMax = texture.height - 1;

        for (int x = screen_start; x <= screen_end; x++) {

            if (zbuf[index] < z) {

                final float recip = 1 / z;
                final int tex_index = (((int) (texv * recip * texYMax))
                        * texture.width + (int) (texu * recip * texXMax));

                final int base_color = texture.pixels[tex_index];

                final int red = (base_color & 0xff0000) >> 16;
                final int green = (base_color & 0x00ff00) >> 8;
                final int blue = (base_color & 0x0000ff);
                final int color = ((int) (blue * v)
                        | (((int) (green * v)) << 8)
                        | (((int) (red * v)) << 16) | ALPHA);

                zbuf[index] = z;
                pixels[index] = color;
                modelbuf[index] = currentModel;

            }

            index++;
            z += dz;
            texv += dtexv;
            texu += dtexu;
        }
    }

    private void drawLineSmoothTextured(int y, int sx, int ex, float sz, float ez, float sv, float ev, float stexu, float etexu, float stexv, float etexv) {

        // Width of this line
        final int screen_start = Math.max(sx, 0);
        final int screen_end = Math.min(ex, width - 1);
        final int w = screen_start - screen_end;

        // Z values
        final float dz = (sz - ez) / w;
        float z = sz;

        // Lighting
        final float dv = (sv - ev) / w;
        float current_v = sv;

        // Texturing
        final float dtexu = (stexu - etexu) / w;
        float texu = stexu;
        final float dtexv = (stexv - etexv) / w;
        float texv = stexv;

        int index = y * width + screen_start;

        final int texXMax = texture.width - 1;
        final int texYMax = texture.height - 1;

        for (int x = screen_start; x <= screen_end; x++) {

            if (zbuf[index] < z) {

                final float recip = 1 / z;
                final int tex_index = (((int) (texv * recip * texYMax))
                        * texture.width + (int) (texu * recip * texXMax));

                final int base_color = texture.pixels[tex_index];

                final int red = (base_color & 0xff0000) >> 16;
                final int green = (base_color & 0x00ff00) >> 8;
                final int blue = (base_color & 0x0000ff);
                final int color = ((int) (blue * current_v)
                        | (((int) (green * current_v)) << 8)
                        | (((int) (red * current_v)) << 16) | ALPHA);

                zbuf[index] = z;
                pixels[index] = color;
                modelbuf[index] = currentModel;

            }

            index++;
            z += dz;
            current_v += dv;
            texv += dtexv;
            texu += dtexu;
        }
    }

}
