package sj3d;

public interface GeometryContainer {

	public Vertex addVertex(float x, float y, float z);
	public Triangle addTriangle(int a, int b, int c);
	public void addTriangle(Triangle t);
	
	public Vertex getVertex(int index);
	public Triangle getTriangle(int index);
	
	public int numVertices();
	public int numTriangles();
	
	public void trim();
	
}
