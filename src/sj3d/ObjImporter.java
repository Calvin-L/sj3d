package sj3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ObjImporter implements Runnable {
	
	private Thread t;
	private Queue<Importable> q;
	private boolean paused;
	private int numModels;
	private int bytesRead;
	private int bytesToRead;
	
	private String current;
	private float percentDone;
	
	private String codebase;
	
	private class Importable {
		public final String name;
		public final Model container;
		public final BufferedReader reader;
		public Importable(String n, String filename, Model m) throws IOException {
			name = n;
			container = m;
			reader = getReader(filename);
		}
	}
	
	
	public ObjImporter(String codebase) {
		this.codebase = codebase;
		numModels = 0;
		q = new LinkedBlockingQueue<Importable>();
		t = new Thread(this, "importer");
		current = "";
	}
	
	private BufferedReader getReader(String filename) throws IOException {
		filename = codebase + filename;
			
		URL url = new URL(filename);
		URLConnection urlConn = url.openConnection();
	    urlConn.setDoInput(true);
	    urlConn.setDoOutput(false);
	    urlConn.setUseCaches(false); 
	    int l = urlConn.getContentLength();
	    if (l > -1)
	    	bytesToRead += l;
	    
	    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
	    
	    return reader;
	}
	
	public void add(String name, String obj, Model container) {
		numModels++;
		Importable i;
		try {
			i = new Importable(name, obj, container);
			q.offer(i);
			if (paused) t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		paused = false;
		if (!t.isAlive())
			t.start();
	}
	
	public boolean importing() {
		return (!q.isEmpty() || !current.equals(""));
	}
	
	private void importNext() {
		if (q.isEmpty()) {
			current = "";
			paused = true;
		} else {
			Importable obj = q.poll();
			current = obj.name;
			importObj(obj, false);
		}
	}
	
	public void run() {
		while(true) {
			if (!paused) {
				importNext();
			}
			
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				break;
			}
		}
	}
	
	private boolean importObj(Importable obj, boolean trim) {
		
		Model model = obj.container;
		int vOffset = model.numVertices();
		ArrayList<Vector> vn = null; // vertex normals
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<UVCoord> uvs = new ArrayList<UVCoord>();
		
		model.addFrame();
		
		try {
			
			String line = "";
			
			BufferedReader reader = obj.reader;
			String[] parts;
			String[] parts2;
			int[] verts, uvs2;
			float x, y, z;
			
			while ((line = reader.readLine()) != null) {
				
				bytesRead += line.length() + 1;
				percentDone = (float)bytesRead / bytesToRead * 100;
				
				if (line.charAt(0) == 'v') {
					if (line.charAt(1) == 't') {
						parts = line.split(" ");
						x = Float.parseFloat(parts[1]);
						y = Float.parseFloat(parts[2]);
						uvs.add(new UVCoord(x, y));
					} else if (line.charAt(1) == 'n') {
						if (vn == null) vn = new ArrayList<Vector>(model.numVertices());
						parts = line.split(" ");
						x = Float.parseFloat(parts[1]);
						y = Float.parseFloat(parts[2]);
						z = Float.parseFloat(parts[3]);
						Vector v = new Vector(x, y, z);
						v.normalize();
						vn.add(v);
					} else {
						parts = line.split(" ");
						x = Float.parseFloat(parts[1]);
						y = Float.parseFloat(parts[2]);
						z = Float.parseFloat(parts[3]);
						vertices.add(model.addVertex(x, y, z));
					}
					//System.out.println("v");
				} else if (line.charAt(0) == 'f') {
					
					// f vertex/texture vertex/vertex normal ...
					parts = line.split(" ");
					verts = new int[3];
					uvs2 = new int[3];
					for (int i = 1; i < 4; i++) {
						parts2 = parts[i].split("/");
						if (parts2.length != 3) {
							System.out.println("Error: you MUST have vertex normals");
						} else {
							verts[i - 1] = Integer.parseInt(parts2[0]) + vOffset;
							Vector v = vn.get(Integer.parseInt(parts2[2]) - 1);
							//v.invert(); // Ugh... wtf?
							model.getVertex(Integer.parseInt(parts2[0]) + vOffset - 1).n = v;
							if (parts2[1].length() > 0) uvs2[i - 1] = Integer.parseInt(parts2[1]);
						}
					}
					Triangle t;
					if (uvs.size() > 0) {
						t = new Triangle(model, 
								verts[0] - 1, verts[1] - 1, verts[2] - 1, 
								uvs.get(uvs2[0] - 1), uvs.get(uvs2[1] - 1), uvs.get(uvs2[2] - 1));
					} else {
						t = new Triangle(model, 
								verts[0] - 1, verts[1] - 1, verts[2] - 1);
					}
					model.addTriangle(t);
					//System.out.println(t.getNormal());
					//System.out.println(container.getVertex(verts[0] - 1).n + " | " + container.getVertex(verts[1] - 1).n + " | " + container.getVertex(verts[2] - 1).n);
				}
				
			}
			reader.close();
			percentDone = 100;
			
			if (trim) {
				model.trim();
			}
			
			//System.out.println("Object imported: '"+obj.name+"' ("+model.numVertices()+" vertices, "+model.numTriangles()+" triangles)");
			
			return true;

		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	public String getCurrent() { return current; }
	public float percentDone() { return percentDone; }
	public String toString() { return ( importing() ? "Loading... ("+percentDone+"%)" : "" ); }
	
}
