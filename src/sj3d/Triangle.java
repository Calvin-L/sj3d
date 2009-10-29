package sj3d;

public final class Triangle {

	private final int vertAindex, vertBindex, vertCindex;
	private final UVCoord uvA, uvB, uvC;
	//private final Vector n;
	private Model parent;

	public Triangle(Model p, int a, int b, int c, UVCoord uvA,
			UVCoord uvB, UVCoord uvC) {
		vertAindex = a;
		vertBindex = b;
		vertCindex = c;
		parent = p;
		this.uvA = uvA;
		this.uvB = uvB;
		this.uvC = uvC;
		//n = calculateNormal();
	}

	public Triangle(Model p, int a, int b, int c) {
		vertAindex = a;
		vertBindex = b;
		vertCindex = c;
		parent = p;
		uvA = uvB = uvC = null;
		//n = calculateNormal();
	}

	public void setParent(Model p) {
		parent = p;
	}

	public Vertex getVertexA() {
		return parent.getVertex(vertAindex);
	}

	public Vertex getVertexB() {
		return parent.getVertex(vertBindex);
	}

	public Vertex getVertexC() {
		return parent.getVertex(vertCindex);
	}

	public UVCoord getUVA() {
		return uvA;
	}

	public UVCoord getUVB() {
		return uvB;
	}

	public UVCoord getUVC() {
		return uvC;
	}

	private Vector calculateNormal() {
		Vertex a = getVertexA();
		Vertex b = getVertexB();
		Vertex c = getVertexC();
		Vector v = new Vector((a.getX() - b.getX()), (a.getY() - b.getY()), (a
				.getZ() - b.getZ()));
		Vector w = new Vector((a.getX() - c.getX()), (a.getY() - c.getY()), (a
				.getZ() - c.getZ()));
		Vector n = v.cross(w);
		n.normalize();
		return n;
	}

	public Vector getNormal() {
		return calculateNormal();
		//return n;
	}

}
