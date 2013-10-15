package sj3d;

final class UVCoord {

    public final float u;
    public final float v;

    public UVCoord(float u, float v) {
        this.u = u;
        this.v = v;
    }

    public String toString() {
        return "UV Coord: (" + u + "," + v + ")";
    }

    public boolean equals(UVCoord uv) {
        return (uv.u == u) && (uv.v == v);
    }

}
