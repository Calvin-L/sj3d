package sj3d;

abstract class Object3D {

    Matrix transform;

    protected float rotX, rotY, rotZ, sclX, sclY, sclZ, posX, posY, posZ;

    protected boolean rebuildFlag = false;

    public Object3D() {

        rotX = rotY = rotZ = posX = posY = posZ = 0.0f;
        sclX = sclY = sclZ = 1.0f;
        transform = Matrix.identity();

    }

    Matrix getMatrix() {

        if (!rebuildFlag)
            return transform;
        rebuildFlag = false;
        transform = new Matrix();
        transform.multiplySelf(Matrix.rotationMatrix(rotX, rotY, rotZ)
                .multiply(Matrix.scaleMatrix(sclX, sclY, sclZ)));
        transform.data[0][3] = posX;
        transform.data[1][3] = posY;
        transform.data[2][3] = posZ;
        return transform;

    }

    public void rotate(float rx, float ry, float rz) {

        this.rotX += rx;
        this.rotY += ry;
        this.rotZ += rz;
        rebuildFlag = true;

    }

    public void scale(float sx, float sy, float sz) {

        sclX *= sx;
        sclY *= sy;
        sclZ *= sz;
        rebuildFlag = true;

    }

    public void translate(float xAmt, float yAmt, float zAmt) {

        posX += xAmt;
        posY += yAmt;
        posZ += zAmt;
        rebuildFlag = true;

    }

    public void setRotation(float rx, float ry, float rz) {

        rotX = rx;
        rotY = ry;
        rotZ = rz;
        rebuildFlag = true;

    }

    public void setPos(float x, float y, float z) {

        posX = x;
        posY = y;
        posZ = z;
        rebuildFlag = true;

    }

    public void setScale(float x, float y, float z) {

        sclX = x;
        sclY = y;
        sclZ = z;
        rebuildFlag = true;

    }

}
