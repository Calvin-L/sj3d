package sj3d;

/**
 * 
 * 4x4 transformation matrix implementation
 * 
 * @author Calvin Loncaric
 * 
 */
final class Matrix implements Cloneable {

	public final float[][] data;

	/**
	 * Create a new identity matrix.
	 */
	public Matrix() {
		data = new float[][] { { 1, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 },
				{ 0, 0, 0, 1 } };
	}

	/**
	 * Create a new matrix from the specified data.
	 * 
	 * @param d
	 *            the data to use (a 4x4 float array)
	 */
	public Matrix(float[][] d) {
		data = d;
	}

	/**
	 * Create a new matrix from the given values. The matrix will be in the
	 * form:
	 * 
	 * <pre>
	 *   [ m00 m01 m02 m03 ]
	 *   [ m10 m11 m12 m13 ]
	 *   [ m20 m21 m22 m23 ]
	 *   [ m30 m31 m32 m33 ]
	 * </pre>
	 * 
	 * @param m00
	 * @param m01
	 * @param m02
	 * @param m03
	 * @param m10
	 * @param m11
	 * @param m12
	 * @param m13
	 * @param m20
	 * @param m21
	 * @param m22
	 * @param m23
	 * @param m30
	 * @param m31
	 * @param m32
	 * @param m33
	 */
	public Matrix(float m00, float m01, float m02, float m03, float m10,
			float m11, float m12, float m13, float m20, float m21, float m22,
			float m23, float m30, float m31, float m32, float m33) {
		float[][] d = { { m00, m01, m02, m03 }, { m10, m11, m12, m13 },
				{ m20, m21, m22, m23 }, { m30, m31, m32, m33 } };
		data = d;
	}

	/**
	 * Create a new matrix from the specified vectors.
	 * 
	 * @param right
	 *            the right-pointing vector in the new coordinate space
	 * @param up
	 *            the up-pointing vector in the new coordinate space
	 * @param forward
	 *            the forward-pointing vector in the new coordinate space
	 */
	public Matrix(Vector right, Vector up, Vector forward) {
		data = new float[4][4];
		data[0][0] = right.x;
		data[1][0] = right.y;
		data[2][0] = right.z;
		data[0][1] = up.x;
		data[1][1] = up.y;
		data[2][1] = up.z;
		data[0][2] = forward.x;
		data[1][2] = forward.y;
		data[2][2] = forward.z;
		data[3][3] = 1;
	}

	/**
	 * Get a new matrix that is the result of multiplying this matrix by matrix
	 * m, in that order.
	 * 
	 * @param m
	 *            the matrix to multiply by
	 * @return the resulting matrix
	 */
	public Matrix multiply(Matrix m) {
		Matrix result = this.clone();
		for (int i = 0; i < 4; i++) {
			result.data[i][0] = (data[i][0] * m.data[0][0])
					+ (data[i][1] * m.data[1][0]) + (data[i][2] * m.data[2][0])
					+ (data[i][3] * m.data[3][0]);
			result.data[i][1] = (data[i][0] * m.data[0][1])
					+ (data[i][1] * m.data[1][1]) + (data[i][2] * m.data[2][1])
					+ (data[i][3] * m.data[3][1]);
			result.data[i][2] = (data[i][0] * m.data[0][2])
					+ (data[i][1] * m.data[1][2]) + (data[i][2] * m.data[2][2])
					+ (data[i][3] * m.data[3][2]);
			result.data[i][3] = (data[i][0] * m.data[0][3])
					+ (data[i][1] * m.data[1][3]) + (data[i][2] * m.data[2][3])
					+ (data[i][3] * m.data[3][3]);
		}
		return result;
	}

	/**
	 * Update this matrix's data to be the result of multiplying this matrix by
	 * matrix m, in that order.
	 * 
	 * @param m
	 *            the matrix to multiply by
	 */
	public void multiplySelf(final Matrix m) {
		for (int i = 0; i < 4; i++) {
			data[i][0] = (data[i][0] * m.data[0][0])
					+ (data[i][1] * m.data[1][0]) + (data[i][2] * m.data[2][0])
					+ (data[i][3] * m.data[3][0]);
			data[i][1] = (data[i][0] * m.data[0][1])
					+ (data[i][1] * m.data[1][1]) + (data[i][2] * m.data[2][1])
					+ (data[i][3] * m.data[3][1]);
			data[i][2] = (data[i][0] * m.data[0][2])
					+ (data[i][1] * m.data[1][2]) + (data[i][2] * m.data[2][2])
					+ (data[i][3] * m.data[3][2]);
			data[i][3] = (data[i][0] * m.data[0][3])
					+ (data[i][1] * m.data[1][3]) + (data[i][2] * m.data[2][3])
					+ (data[i][3] * m.data[3][3]);
		}
	}

	/**
	 * Invert this matrix.
	 */
	public void invert() {

		final float[][] m = {
				{ data[0][0], data[0][1], data[0][2], data[0][3] },
				{ data[1][0], data[1][1], data[1][2], data[1][3] },
				{ data[2][0], data[2][1], data[2][2], data[2][3] },
				{ data[3][0], data[3][1], data[3][2], data[3][3] } };

		final float q1 = m[1][2];
		final float q6 = m[1][0] * m[0][1];
		final float q7 = m[1][0] * m[2][1];
		final float q8 = m[0][2];
		final float q13 = m[2][0] * m[0][1];
		final float q14 = m[2][0] * m[1][1];
		final float q21 = m[0][2] * m[2][1];
		final float q22 = m[0][3] * m[2][1];
		final float q25 = m[0][1] * m[1][2];
		final float q26 = m[0][1] * m[1][3];
		final float q27 = m[0][2] * m[1][1];
		final float q28 = m[0][3] * m[1][1];
		final float q29 = m[1][0] * m[2][2];
		final float q30 = m[1][0] * m[2][3];
		final float q31 = m[2][0] * m[1][2];
		final float q32 = m[2][0] * m[1][3];
		final float q35 = m[0][0] * m[2][2];
		final float q36 = m[0][0] * m[2][3];
		final float q37 = m[2][0] * m[0][2];
		final float q38 = m[2][0] * m[0][3];
		final float q41 = m[0][0] * m[1][2];
		final float q42 = m[0][0] * m[1][3];
		final float q43 = m[1][0] * m[0][2];
		final float q44 = m[1][0] * m[0][3];
		final float q45 = m[0][0] * m[1][1];
		final float q48 = m[0][0] * m[2][1];
		final float q49 = q45 * m[2][2] - q48 * q1 - q6 * m[2][2] + q7 * q8;
		final float q50 = q13 * q1 - q14 * q8;
		final float q51 = 1 / (q49 + q50);

		data[0][0] = (m[1][1] * m[2][2] * m[3][3] - m[1][1] * m[2][3] * m[3][2]
				- m[2][1] * m[1][2] * m[3][3] + m[2][1] * m[1][3] * m[3][2]
				+ m[3][1] * m[1][2] * m[2][3] - m[3][1] * m[1][3] * m[2][2])
				* q51;
		data[0][1] = -(m[0][1] * m[2][2] * m[3][3] - m[0][1] * m[2][3]
				* m[3][2] - q21 * m[3][3] + q22 * m[3][2])
				* q51;
		data[0][2] = (q25 * m[3][3] - q26 * m[3][2] - q27 * m[3][3] + q28
				* m[3][2])
				* q51;
		data[0][3] = -(q25 * m[2][3] - q26 * m[2][2] - q27 * m[2][3] + q28
				* m[2][2] + q21 * m[1][3] - q22 * m[1][2])
				* q51;
		data[1][0] = -(q29 * m[3][3] - q30 * m[3][2] - q31 * m[3][3] + q32
				* m[3][2])
				* q51;
		data[1][1] = (q35 * m[3][3] - q36 * m[3][2] - q37 * m[3][3] + q38
				* m[3][2])
				* q51;
		data[1][2] = -(q41 * m[3][3] - q42 * m[3][2] - q43 * m[3][3] + q44
				* m[3][2])
				* q51;
		data[1][3] = (q41 * m[2][3] - q42 * m[2][2] - q43 * m[2][3] + q44
				* m[2][2] + q37 * m[1][3] - q38 * m[1][2])
				* q51;
		data[2][0] = (q7 * m[3][3] - q30 * m[3][1] - q14 * m[3][3] + q32
				* m[3][1])
				* q51;
		data[2][1] = -(q48 * m[3][3] - q36 * m[3][1] - q13 * m[3][3] + q38
				* m[3][1])
				* q51;
		data[2][2] = (q45 * m[3][3] - q42 * m[3][1] - q6 * m[3][3] + q44
				* m[3][1])
				* q51;
		data[2][3] = -(q45 * m[2][3] - q42 * m[2][1] - q6 * m[2][3] + q44
				* m[2][1] + q13 * m[1][3] - q38 * m[1][1])
				* q51;

	}

	public Vector multiply(final Vector v) {
		final Vector result = new Vector();
		result.x = data[0][0] * v.x + data[0][1] * v.y + data[0][2] * v.z;// +
																			// data[0][3]
																			// *
																			// v.w;
		result.y = data[1][0] * v.x + data[1][1] * v.y + data[1][2] * v.z;// +
																			// data[1][3]
																			// *
																			// v.w;
		result.z = data[2][0] * v.x + data[2][1] * v.y + data[2][2] * v.z;// +
																			// data[2][3]
																			// *
																			// v.w;
		return result;
	}

	public Vertex multiply(final Vertex v) {
		final float x = (data[0][0] * v.x) + (data[0][1] * v.y)
				+ (data[0][2] * v.z) + (data[0][3]); // * v.w);
		final float y = (data[1][0] * v.x) + (data[1][1] * v.y)
				+ (data[1][2] * v.z) + (data[1][3]); // * v.w);
		final float z = (data[2][0] * v.x) + (data[2][1] * v.y)
				+ (data[2][2] * v.z) + (data[2][3]); // * v.w);
		final Vertex result = new Vertex(x, y, z);
		return result;
	}

	@Override
	public Matrix clone() {

		final float[][] m = {
				{ data[0][0], data[0][1], data[0][2], data[0][3] },
				{ data[1][0], data[1][1], data[1][2], data[1][3] },
				{ data[2][0], data[2][1], data[2][2], data[2][3] },
				{ data[3][0], data[3][1], data[3][2], data[3][3] } };

		return new Matrix(m);

	}
	
	private String pad(String s) {
		for (int i = s.length(); i < 14; i++) {
			s += ' ';
		}
		return s;
	}

	@Override
	public String toString() {
		return    "Matrix " + pad(data[0][0] + ",") + pad(data[0][1] + ",") + 
				              pad(data[0][2] + ",") + pad(data[0][3] + "") + 
				              "\n" 
				+ "       " + pad(data[1][0] + ",") + pad(data[1][1] + ",") + 
				              pad(data[1][2] + ",") + pad(data[1][3] + "") + 
				              "\n" 
				+ "       " + pad(data[2][0] + ",") + pad(data[2][1] + ",") + 
				              pad(data[2][2] + ",") + pad(data[2][3] + "") + 
				              "\n" 
				+ "       " + pad(data[3][0] + ",") + pad(data[3][1] + ",") + 
				              pad(data[3][2] + ",") + pad(data[3][3] + "");
	}

	public static Matrix rotationMatrix(final float rotX, final float rotY,
			final float rotZ) {

		final float s1 = (float) Math.sin(rotX);
		final float s2 = (float) Math.sin(rotY);
		final float s3 = (float) Math.sin(rotZ);

		final float c1 = (float) Math.cos(rotX);
		final float c2 = (float) Math.cos(rotY);
		final float c3 = (float) Math.cos(rotZ);

		return new Matrix(c2 * c3, c2 * s3, -s2, 0, s1 * s2 * c3 - c1 * s3, s1
				* s2 * s3 + c1 * c3, s1 * c2, 0, c1 * s2 * c3 - s1 * s3, c1
				* s2 * s3 + s1 * c3, c1 * c2, 0, 0, 0, 0, 1);

	}

	public static Matrix scaleMatrix(float sx, float sy, float sz) {

		return new Matrix(sx, 0, 0, 0, 0, sy, 0, 0, 0, 0, sz, 0, 0, 0, 0, 1);

	}

	public static Matrix identity() {

		return new Matrix();

	}

}