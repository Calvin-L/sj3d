package sj3d;

import java.util.ArrayList;

public class Model extends Object3D implements GeometryContainer, Cloneable {

	protected final ArrayList<ArrayList<Vertex>> frames;
	protected final ArrayList<Triangle> triangles;
	protected int currentFrame;
	protected int numFrames;
	public Material material = new Material();

	public Model() {
		triangles = new ArrayList<Triangle>();
		frames = new ArrayList<ArrayList<Vertex>>();
		currentFrame = 0;
		numFrames = 0;
	}
	
	public Model(int numFrames) {
		triangles = new ArrayList<Triangle>();
		frames = new ArrayList<ArrayList<Vertex>>(numFrames);
		for (int i = 0; i < numFrames; i++) {
			frames.add(i, new ArrayList<Vertex>());
		}
		this.numFrames = numFrames;
		currentFrame = 0;
	}
	
	public Model(Model blueprint) {
		frames = blueprint.frames;
		triangles = blueprint.triangles;
		currentFrame = 0;
		numFrames = frames.size();
	}
	
	public Vertex[] getVertices() {
		return (Vertex[])frames.get(currentFrame).toArray();
	}
	
	public Vertex addVertex(float x, float y, float z) {
		Vertex v = new Vertex(x, y, z);
		frames.get(currentFrame).add(v);
		return v;
	}

	public Triangle addTriangle(int a, int b, int c) {
		Triangle t = new Triangle(this, a, b, c);
		triangles.add(t);
		return t;
	}

	public void addTriangle(Triangle t) {
		t.setParent(this);
		triangles.add(t);
	}
	
	public ArrayList<Vertex> addFrame() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		frames.add(vertices);
		numFrames++;
		return vertices;
	}
	
	public void nextFrame() { currentFrame = (currentFrame+1) % numFrames; }
	public int getFrame() { return currentFrame; }
	public void setFrame(int i) { currentFrame = i % numFrames; }
	
	public void trim() {
		triangles.trimToSize();
		frames.trimToSize();
	}
	
	public Vertex getVertex(int index) { return frames.get(currentFrame).get(index); }
	public Triangle getTriangle(int index) { return triangles.get(index); }
	
	public int numVertices() { return numFrames > 0 ? frames.get(currentFrame).size() : 0; }
	public int numTriangles() { return triangles.size(); }
	
	@Override
	public Model clone() {
		Model m;
		m = new Model();
		
		// Add vertices
		for (int i = 0; i < frames.size(); i++) {
			ArrayList<Vertex> frame = frames.get(i);
			ArrayList<Vertex> newFrame = new ArrayList<Vertex>(frame.size());
			for (int j = 0; j < frame.size(); j++) {
				newFrame.add(frame.get(j).clone());
			}
			m.frames.add(newFrame);
		}
		
		// Add triangles
		for (int i = 0; i < triangles.size(); i++) {
			m.addTriangle(triangles.get(i));
		}
		
		m.material = material;
		
		return m;
	}
	

	// Events
	public void onMouseOver(int x, int y) {}
	public void onMouseOut(int x, int y) {}
	public void onClick(int x, int y) {}
	
}
