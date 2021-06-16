package sj3d;

public final class Vertex {

    public final float x, y, z;
    public Vector n;

    float projX, projY, projZ;

    public Vertex() {
        x = y = z = 0.0f;
    }

    public Vertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vertex(float x, float y, float z, Vector n) {
        this(x,y,z);
        this.n = n;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public String toString() {
        return "Vertex (" + x + ", " + y + ", " + z + ")";
    }

}
