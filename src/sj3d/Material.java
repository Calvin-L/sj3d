package sj3d;

public final class Material {

    // Constants

    public static final int FLAT = 0;
    public static final int SMOOTH = 1;
    public static final int TEXTURED = 2;
    public static final int SMOOTH_TEXTURED = SMOOTH | TEXTURED;

    // Member variables

    /**
     * Material mode.
     *
     * Equal to one of the constants {@link #FLAT}, {@link #SMOOTH},
     * {@link #TEXTURED}, or {@link #SMOOTH_TEXTURED}.  Defaults to
     * {@link #FLAT}.
     */
    public int mode = FLAT;

    public float diffuseValue = 1.0f, ambientValue = 0.2f;
    public Texture texture;
    public int color = 0xaaaaaa;

    // Simple constructors

    public static Material flat(int color) {
        Material m = new Material();
        m.mode = FLAT;
        m.color = color;
        return m;
    }

    public static Material smooth(int color) {
        Material m = new Material();
        m.mode = SMOOTH;
        m.color = color;
        return m;
    }

    public static Material flatTextured(Texture texture) {
        Material m = new Material();
        m.mode = TEXTURED;
        m.texture = texture;
        return m;
    }

    public static Material smoothTextured(Texture texture) {
        Material m = new Material();
        m.mode = SMOOTH_TEXTURED;
        m.texture = texture;
        return m;
    }

}
