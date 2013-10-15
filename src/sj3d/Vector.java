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

    public Vector cross(Vector v) {
        float i = (y * v.z) - (z * v.y);
        float j = (z * v.x) - (x * v.z);
        float k = (x * v.y) - (y * v.x);
        return new Vector(i, j, k);
    }

    // Dot product: will only work with normalized vectors!
    public float dot(Vector v) {
        return (x * v.x) + (y * v.y) + (z * v.z);
    }

    public Vector add(Vector v) {
        return new Vector(x + v.x, y + v.y, z + v.z);
    }

    public Vector subtract(Vector v) {
        return new Vector(x - v.x, y - v.y, z - v.z);
    }

    @Override
    public Vector clone() {
        return new Vector(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector (" + x + ", " + y + ", " + z + ")";
    }

}
