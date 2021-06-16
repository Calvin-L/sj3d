package sj3d;

/**
 * A <code>Camera</code> has a position and looks in a direction.
 *
 * @see #getMatrix()
 */
public final class Camera {

    private float posX, posY, posZ;
    private final Vector lookAtVector;
    private final Vector forward = new Vector();
    private final Vector up = new Vector();
    private final Vector right = new Vector();
    private boolean rebuildFlag = true;
    private final Matrix transform = new Matrix();

    public Camera() {
        posX = posY = 0;
        posZ = -2;
        lookAtVector = new Vector(0, 0, 0);
    }

    public Camera(float x, float y, float z, float lookX, float lookY, float lookZ) {
        posX = x;
        posY = y;
        posZ = z;
        lookAtVector = new Vector(lookX, lookY, lookZ);
    }

    public void setPos(float x, float y, float z) {
        posX = x;
        posY = y;
        posZ = z;
        rebuildFlag = true;
    }

    public void lookAt(float x, float y, float z) {
        lookAtVector.set(x, y, z);
    }

    public Vector getLookVector() {
        return lookAtVector;
    }

    public Vector getForwardVector() {
        getMatrix();
        return forward;
    }

    public Matrix getMatrix() {
        if (rebuildFlag) {
            forward.set(
                    lookAtVector.x - posX,
                    lookAtVector.y - posY,
                    lookAtVector.z - posZ);

            up.set(0f, 1f, 0f);
            right.setToCrossProduct(up, forward);
            up.setToCrossProduct(right, forward);

            forward.normalize();
            up.normalize();
            right.normalize();

            transform.setBasisVectors(right, up, forward);
            transform.data[0][3] = posX;
            transform.data[1][3] = posY;
            transform.data[2][3] = posZ;

            transform.invert();
            rebuildFlag = false;
        }

        return transform;
    }

}
