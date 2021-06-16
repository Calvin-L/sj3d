package sj3d;

public final class Vector {

    public float x, y, z;

    public Vector() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector(float _x, float _y, float _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void normalize() {
        float l = length();
        x /= l;
        y /= l;
        z /= l;
    }

    public boolean isNormal() {
        float l = length();
        return (l > 0.999999 || l < 1.000001);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public void setToCrossProduct(Vector v1, Vector v2) {
        setToCrossProduct(
                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z);
    }

    public void setToCrossProduct(float v1x, float v1y, float v1z, float v2x, float v2y, float v2z) {
        set((v1y * v2z) - (v1z * v2y), (v1z * v2x) - (v1x * v2z), (v1x * v2y) - (v1y * v2x));
    }

    // Dot product: will only work with normalized vectors!
    public float dot(Vector v) {
        return (x * v.x) + (y * v.y) + (z * v.z);
    }

    @Override
    public String toString() {
        return "Vector (" + x + ", " + y + ", " + z + ")";
    }

}
