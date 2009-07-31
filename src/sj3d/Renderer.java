package sj3d;

final class Renderer {

	/*
	 * 
	 * All these members are defined out here for speed. The methods themselves, 
	 * when called, do not need to allocate/deallocate as much memory this way.
	 * 
	 */
	
	// Lighting
	private Vector lightVector;
	private float lightAmbient, lightIntensity;
	private float Kd, Ka;
	private float v;
	private int red, green, blue;
	
	// Angles
	private float cos;
	
	// Quick looping
	private int i, l, x, y, index;
	
	// Triangle rendering
	private Triangle t;
	private Material material;
	private Texture texture;
	private int color = 0xffffff;
	private int mode; // shade mode
	private Vertex a, b, c, tempVertex;
	private Vector n; // Triangle normal; used in lighting calculation
	private Vector projN; // Projected triangle normal; used in back-face culling
	private UVCoord uva, uvb, uvc, tempUV;
	private Model currentContainer;
	private float ax, bx, cx;		// Projected position
	private int ay, by, cy;			// Projected position
	private float az, bz, cz;		// Projected position
	private float dx_ac, dx_ab, dx_bc, sx, ex;			// X delta values
	private float dz_ac, dz_ab, dz_bc, dz, sz, ez, z;	// Z delta values
	private float av, bv, cv;
	private float dv_ac, dv_ab, dv_bc, dv, sv, ev, current_v;		// Light intensity values
	private int screen_start, screen_end;
	private float relative_start_y, screen_end_y;
	
	// Texturing
	private float dtexu_ab, dtexu_ac, dtexu_bc,
		dtexv_ab, dtexv_ac, dtexv_bc,
		dtexu, dtexv,
		stexu, stexv, etexu, etexv,
		texu, texv,
		texau, texav, texbu, texbv, texcu, texcv;
	
	// Settings
	private final int width, height;
	private final int halfwidth, halfheight;
	private final float scl;
	private float sclOverZ;
	private final RenderSettings settings;
	final int ALPHA;
	
	// Buffers
	private final int[] pixels;
	private final float[] zbuf; // depth of object at each pixel
	private final Model[] modelbuf; // Models at each pixel point
	//private final byte[] shadowStencil; // for shadows...
	
	// Buffer methods
	private final void set(float[] buf, int x, int y, float value) { buf[y * width + x] = value; }
	private final void set(int[] buf, int x, int y, int value) { buf[y * width + x] = value; }
	private final void set(GeometryContainer[] buf, int x, int y, GeometryContainer value) { buf[y * width + x] = value; }
	//private final int get(int[] buf, int x, int y) { return buf[y * width + x]; }
	private final float get(float[] buf, int x, int y) { return buf[y * width + x]; }
	
	
	Renderer(
			final int width, final int height, 
			final int[] pixels, 
			final float[] zbuf,
			final Model[] modelbuf,
			final RenderSettings settings) {
		
		// Screen size
		this.width = width; this.height = height;
		halfwidth = width/2; halfheight = height/2;
		
		// Scale factor to make coordinates more natural
		scl = (float)height / 2;
		
		//this.vertices = vertices;
		this.pixels = pixels;
		this.zbuf = zbuf;
		this.modelbuf = modelbuf;
		this.lightVector = new Vector(0, 1, 0);
		this.lightAmbient = 0;
		this.lightIntensity = 1;
		
		// Configure settings
		this.settings = settings;
		ALPHA = settings.mblur;
		
		//if (settings.hasShadows()) shadowStencil = new byte[width*height];
		//else shadowStencil = null;
		
	}
	
	/**
	 * Set the scene lighting.
	 * @param direction the direction to which the light will point. The direction vector does not need to be normalized.
	 * @param intensity the intensity of the light
	 * @param ambient the ambient intensity
	 * @param shadeType <code>World.SHADE_SMOOTH</code> or <code>World.SHADE_FLAT</code>
	 */
	void setLighting(final Vector direction, final float intensity, final float ambient) {
		lightVector = direction;
		lightVector.normalize();
		lightIntensity = intensity;
		lightAmbient = ambient;
		//this.shadeType = shadeType;
	}

	
	
	/**
	 * Render a model to the specified camera.
	 * @param camera the camera
	 * @param object the model to render
	 */
	void render(final Camera camera, final Model object) {
		//Triangle t;
		material = object.material;
		currentContainer = object;
		projectAllVertices(camera);
		
		mode = material.mode & settings.shadeMode;
		
		Kd = lightIntensity * material.diffuseValue;
		Ka = lightAmbient * material.ambientValue;
		
		if ((material.mode & RenderSettings.TEXTURED) == 0) {
			color = material.color;
			red = (color & 0xff0000) >> 16;
			green = (color & 0x00ff00) >> 8;
			blue = (color & 0x0000ff);
		} else {
			texture = material.texture;
		}
		
		//int count = 0;
		for (i = 0, l = object.numTriangles(); i < l; i++) {
			t = object.getTriangle(i);
			n = t.getNormal();
			//cos = n.dot(camera.getForwardVector());
			projN = camera.getMatrix().multiply(t.getNormal()); 
			// back-face culling: only render one side of triangle
			//if (cos <= 0) {
			if (projN.z < 0) {
				renderTriangle(t);
				//count++;
			}
		}
		//System.out.println(count + " tris drawn");
		
	}

	
	/**
	 * Project all vertices into their correct screen coordinates. This function has the effect of updating the
	 * <code>projX</code>, <code>projY</code>, and <code>projZ</code> variables in each vertex of the model.
	 * @param camera
	 */
	private void projectAllVertices(final Camera camera) {
		final Matrix m = camera.getMatrix().multiply(currentContainer.getMatrix());
		Vertex v0, v;
		
		for (i = 0, l = currentContainer.numVertices(); i < l; i++) {
			
			v0 = currentContainer.getVertex(i);
			
			// Transform to camera coordinates
			v = m.multiply(v0);
			
			// Calculate position on screen & depth from camera
			// This bit performs the transformation from orthographic to perspective
			sclOverZ = Math.abs(scl / v.z);
			v0.projX = v.x * sclOverZ + halfwidth;
			v0.projY = v.y * sclOverZ + halfheight;
			v0.projZ = 1.0f/(v.z * scl);
			
		}
		
	}
	
	/**
	 * Render a specific triangle.
	 * @param t the triangle to render
	 */
	private void renderTriangle(final Triangle t) {
		
		// Vertices & vertex texture coordinates
		a = t.getVertexA();
		b = t.getVertexB();
		c = t.getVertexC();
		uva = t.getUVA();
		uvb = t.getUVB();
		uvc = t.getUVC();
		
		/*
		 * Sort vertices by projected y-coordinate; "a" on top, followed by "b" and then "c".
		 * This gives us two cases, where point "b" is either left or right of line "a-c".
		 * 
		 *   Case 1   |    Case 2
		 * -----------+------------
		 *    a       |       a
		 *    | \     |     / |
		 *    |  b    |    b  |
		 *    | /     |     \ |
		 *    c       |       c
		 *            |
		 */
		
		if (b.projY < a.projY) { 
			tempVertex = a; a = b; b = tempVertex; 
			tempUV = uva; uva = uvb; uvb = tempUV;
		}
		if (c.projY < b.projY) { 
			tempVertex = b; b = c; c = tempVertex; 
			tempUV = uvb; uvb = uvc; uvc = tempUV;
		}
		if (b.projY < a.projY) { 
			tempVertex = a; a = b; b = tempVertex;
			tempUV = uva; uva = uvb; uvb = tempUV;
		}
		
		// Triangle screen coordinates
		ax = a.projX; ay = (int)a.projY; az = a.projZ;
		bx = b.projX; by = (int)b.projY; bz = b.projZ;
		cx = c.projX; cy = (int)c.projY; cz = c.projZ;
		
		// Ignore triangles behind camera
		if (az < 0 && bz < 0 && cz < 0) return;

		// Skip if left or right of screen
		if ((ax > width && cx > width && bx > width) || (ax < 0 && bx < 0 && cx < 0)) { return; }
		
		// Skip if above or below screen
		if (ay >= height || cy < 0) { return; }

		// Skip if entirely horizontal
		if (ay == cy) { return; }
		
		// delta-x values -- the change in screen-x as we go down the rows of screen-y
		dx_ac = ((ay == cy) ? 0 : (float)(ax - cx) / (ay - cy)); 		// a to c
		dx_ab = ((ay == by) ? bx - ax : (float)(ax - bx) / (ay - by)); 	// a to b
		dx_bc = ((by == cy) ? 0 : (float)(bx - cx) / (by - cy)); 		// b to c
		
		// delta-z values -- the change in distance from camera as we go down the rows of screen-y
		dz_ac = ((ay == cy) ? 0 : (float)(az - cz) / (ay - cy)); // a to c
		dz_ab = ((ay == by) ? 0 : (float)(az - bz) / (ay - by)); // a to b
		dz_bc = ((by == cy) ? 0 : (float)(bz - cz) / (by - cy)); // b to c
		
		y = Math.max(ay, 0);
		relative_start_y = (float)y - ay;
		screen_end_y = (float)Math.floor(Math.min(by, height));
		
		switch (mode) {
		case RenderSettings.SMOOTH_TEXTURED:
			setupSmooth();
			setupTextured();
			renderTriangleSmoothTextured();
			break;
		case RenderSettings.TEXTURED:
			setupFlat();
			setupTextured();
			renderTriangleFlatTextured();
			break;
		case RenderSettings.SMOOTH:
			setupSmooth();
			renderTriangleSmoothUntextured();
			break;
		default:
			setupFlat();
			renderTriangleFlatUntextured();
		}
		
	}
	
	private final void setupFlat() {
		cos = n.dot(lightVector);
		if (cos <= 0) v = Ka;
		else v = cos * (Kd - Ka) + Ka;
	}
	
	private final void setupSmooth() {

		// Calculate lighting for each vertex
		
		cos = a.n.dot(lightVector);
		if (cos <= 0) av = Ka;
		else av = cos * (Kd - Ka) + Ka;
		
		cos = b.n.dot(lightVector);
		if (cos <= 0) bv = Ka;
		else bv = cos * (Kd - Ka) + Ka;
		
		cos = c.n.dot(lightVector);
		if (cos <= 0) cv = Ka;
		else cv = cos * (Kd - Ka) + Ka;
		
		// Change in color value as we progress down rows
		
		dv_ac = ((ay == cy) ? 0 : (float)(av - cv) / (ay - cy)); // a to c
		dv_ab = ((ay == by) ? 0 : (float)(av - bv) / (ay - by)); // a to b
		dv_bc = ((by == cy) ? 0 : (float)(bv - cv) / (by - cy)); // b to c
		
	}

	private final void setupTextured() {
		
		texau = uva.u * az;
		texav = uva.v * az;
		texbu = uvb.u * bz;
		texbv = uvb.v * bz;
		texcu = uvc.u * cz;
		texcv = uvc.v * cz;

		dtexu_ab = (texau - texbu) / (ay - by);
		dtexv_ab = (texav - texbv) / (ay - by);
		
		dtexu_ac = (texau - texcu) / (ay - cy);
		dtexv_ac = (texav - texcv) / (ay - cy);
		
		dtexu_bc = (texbu - texcu) / (by - cy);
		dtexv_bc = (texbv - texcv) / (by - cy);
		
	}
	
	private final void renderTriangleFlatUntextured() {

		if (dx_ab > dx_ac) {				// case 1: point "b" is right of line a-c
			
			// a to b
			for (
					sx = ax + relative_start_y*dx_ac, ex = ax + relative_start_y*dx_ab, 
					sz = az + relative_start_y*dz_ac, ez = az + relative_start_y*dz_ab;
					y < screen_end_y; 
					y++, 
					sx += dx_ac, ex += dx_ab, 
					sz += dz_ac, ez += dz_ab
			) {
				drawLineFlat();
			}
			
			// b to c
			screen_end_y = Math.min(cy + 1, height);
			for (
					ex = bx, ez = bz; 
					y < screen_end_y; 
					y++, 
					sx += dx_ac, ex += dx_bc, 
					sz += dz_ac, ez += dz_bc
			) {
				drawLineFlat();
			}
			
		} else if (dx_ab < dx_ac) {		// case 2: point "b" is left of line a-c
			// a to b
			for (
					sx = ax + relative_start_y*dx_ab, ex = ax + relative_start_y * dx_ac, 
					sz = az + relative_start_y*dz_ab, ez = az + relative_start_y*dz_ac;
					y < screen_end_y; 
					y++, 
					sx += dx_ab, ex += dx_ac, 
					sz += dz_ab, ez += dz_ac
			) {
				drawLineFlat();
			}
			
			// b to c
			screen_end_y = Math.min(cy + 1, height);
			for (
					sx = bx, sz = bz; 
					y < screen_end_y; 
					y++, 
					sx += dx_bc, ex += dx_ac, 
					sz += dz_bc, ez += dz_ac
			) {
				drawLineFlat();
			}
			
		}
		
	}
	
	private final void renderTriangleSmoothUntextured() {
		
		if (dx_ab > dx_ac) {				// case 1: point "b" is right of line a-c
			
			// a to b
			for (
					sx = ax + relative_start_y*dx_ac, ex = ax + relative_start_y*dx_ab, 
					sz = az + relative_start_y*dz_ac, ez = az + relative_start_y*dz_ab,
					sv = av + relative_start_y*dv_ac, ev = av + relative_start_y*dv_ab; 
					y < screen_end_y; 
					y++, 
					sx += dx_ac, ex += dx_ab, 
					sz += dz_ac, ez += dz_ab,
					sv += dv_ac, ev += dv_ab
			) {
				drawLineSmooth(Math.round(sx), Math.round(ex), sz, ez, sv, ev, color);
			}
			
			// b to c
			screen_end_y = Math.min(cy + 1, height);
			for (
					ex = bx, ez = bz, ev = bv; 
					y < screen_end_y; 
					y++, 
					sx += dx_ac, ex += dx_bc, 
					sz += dz_ac, ez += dz_bc,
					sv += dv_ac, ev += dv_bc
			) {
				drawLineSmooth(Math.round(sx), Math.round(ex), sz, ez, sv, ev, color);
			}
			
		} else if (dx_ab < dx_ac) {		// case 2: point "b" is left of line a-c
			// a to b
			for (
					sx = ax + relative_start_y*dx_ab, ex = ax + relative_start_y * dx_ac, 
					sz = az + relative_start_y*dz_ab, ez = az + relative_start_y*dz_ac,
					sv = av + relative_start_y*dv_ab, ev = av + relative_start_y*dv_ac;
					y < screen_end_y; 
					y++, 
					sx += dx_ab, ex += dx_ac, 
					sz += dz_ab, ez += dz_ac,
					sv += dv_ab, ev += dv_ac
			) {
				drawLineSmooth(Math.round(sx), Math.round(ex), sz, ez, sv, ev, color);
			}
			
			// b to c
			screen_end_y = Math.min(cy + 1, height);
			for (
					sx = bx, sz = bz, sv = bv; 
					y < screen_end_y; 
					y++, 
					sx += dx_bc, ex += dx_ac, 
					sz += dz_bc, ez += dz_ac,
					sv += dv_bc, ev += dv_ac
			) {
				drawLineSmooth(Math.round(sx), Math.round(ex), sz, ez, sv, ev, color);
			}
			
		}		// Note that if point "b" lies on line a-c, the triangle is treated as invisibly thin for optimization
		
	}
	
	private final void renderTriangleFlatTextured() {
		
	}
	
	private final void renderTriangleSmoothTextured() {
		
		if (dx_ab > dx_ac) {				// case 1: point "b" is right of line a-c
			// a to b
			for (
					sx = ax + relative_start_y*dx_ac, ex = ax + relative_start_y*dx_ab, 
					sz = az + relative_start_y*dz_ac, ez = az + relative_start_y*dz_ab,
					sv = av + relative_start_y*dv_ac, ev = av + relative_start_y*dv_ab,
					stexu = texau + relative_start_y*dtexu_ac,
					stexv = texav + relative_start_y*dtexv_ac,
					etexu = texau + relative_start_y*dtexu_ab,
					etexv = texav + relative_start_y*dtexv_ab;
					y < screen_end_y; 
					y++
			) {
				drawLineSmoothTextured();
				sx += dx_ac; ex += dx_ab;
				sz += dz_ac; ez += dz_ab;
				sv += dv_ac; ev += dv_ab;
				stexu += dtexu_ac; stexv += dtexv_ac;
				etexu += dtexu_ab; etexv += dtexv_ab;
			}
			
			// b to c
			screen_end_y = Math.min(cy + 1, height);
			etexu = texbu;
			etexv = texbv;
			for (
					ex = bx, ez = bz, ev = bv
					;
					y < screen_end_y; 
					y++
			) {
				drawLineSmoothTextured();
				sx += dx_ac; ex += dx_bc; 
				sz += dz_ac; ez += dz_bc;
				sv += dv_ac; ev += dv_bc;
				stexu += dtexu_ac; stexv += dtexv_ac;
				etexv += dtexv_bc; etexu += dtexu_bc;
			}
			
		} else if (dx_ab < dx_ac) {		// case 2: point "b" is left of line a-c
			
			// a to b
			for (
					sx = ax + relative_start_y*dx_ab, ex = ax + relative_start_y * dx_ac, 
					sz = az + relative_start_y*dz_ab, ez = az + relative_start_y*dz_ac,
					sv = av + relative_start_y*dv_ab, ev = av + relative_start_y*dv_ac,
					stexu = texau + relative_start_y*dtexu_ab,
					stexv = texav + relative_start_y*dtexv_ab,
					etexu = texau + relative_start_y*dtexu_ac,
					etexv = texav + relative_start_y*dtexv_ac;
					y < screen_end_y; 
					y++
			) {
				drawLineSmoothTextured();
				sx += dx_ab; ex += dx_ac; 
				sz += dz_ab; ez += dz_ac;
				sv += dv_ab; ev += dv_ac;
				stexu += dtexu_ab; stexv += dtexv_ab;
				etexu += dtexu_ac; etexv += dtexv_ac;
			}
			
			
			// b to c
			screen_end_y = Math.min(cy + 1, height);
			stexu = texbu;
			stexv = texbv;
			for (
					sx = bx, sz = bz, sv = bv
					; 
					y < screen_end_y; 
					y++
			) {
				drawLineSmoothTextured();
				sx += dx_bc; ex += dx_ac;
				sz += dz_bc; ez += dz_ac;
				sv += dv_bc; ev += dv_ac;
				stexu += dtexu_bc; stexv += dtexv_bc;
				etexu += dtexu_ac; etexv += dtexv_ac;
			}
			
		}		// Note that if point "b" lies on line a-c, the triangle is treated as invisibly thin for optimization
		
	}
	
	// Line drawing
	
	private final void drawLineFlat() {
		dz = (float)(sz - ez) / (sx - ex);
		z = sz;
		screen_start = (int)Math.max(sx, 0);
		screen_end = (int)Math.min(ex, width-1);
		for (x = screen_start; x <= screen_end; x++) {
			if (get(zbuf, x, y) < z) {
				set(zbuf, x, y, z);
				set(pixels, x, y, ((int)(blue * v) | (((int)(green * v)) << 8) | (((int)(red * v)) << 16) | (ALPHA)));
				set(modelbuf, x, y, currentContainer);
			}
			z += dz;
		}
	}
	
	private final void drawLineSmooth(int start, int end, float start_z, float end_z, float start_v, float end_v, int color) {
		dz = (float)(start_z - end_z) / (start - end);
		z = start_z;
		dv = (float)(start_v - end_v) / (start - end);
		current_v = start_v;
		red = (color & 0xff0000) >> 16;
		green = (color & 0x00ff00) >> 8;
		blue = (color & 0x0000ff);
		screen_start = Math.max(start, 0);
		screen_end = Math.min(end, width-1);
		index = y * width + screen_start;
		
		for (x = screen_start; x <= screen_end; x++) {
			
			if (zbuf[index] < z) {
				zbuf[index] = z;
				pixels[index] = (int)(blue * current_v) | (((int)(green * current_v)) << 8) | (((int)(red * current_v)) << 16) | ALPHA;
				modelbuf[index] = currentContainer;
				set(modelbuf, x, y, currentContainer);
			}
			
			index++;
			z += dz;
			current_v += dv;
			
		}
	}
	
	private final void drawLineSmoothTextured() {

		
		// Width of this line
		screen_start = (int)Math.max(sx, 0);
		screen_end = (int)Math.min(ex, width-1);
		final int w = screen_start - screen_end;
		
		// Z values
		dz = (sz - ez) / w;
		z = sz;
		
		// Lighting
		dv = (sv - ev) / w;
		current_v = sv;
		
		// Texturing
		dtexu = (stexu - etexu) / w;
		texu = stexu;
		dtexv = (stexv - etexv) / w;
		texv = stexv;
		
		
		index = y * width + screen_start;
		
		for (x = screen_start; x <= screen_end; x++) {
			
			if (zbuf[index] < z) {
				
				final float recip = 1/z;
				final int tex_index = (((int)(texv * recip * texture.height)) * texture.width + (int)(texu * recip * texture.width));
				
				color = texture.pixels[tex_index];
				
				
				red = (color & 0xff0000) >> 16;
				green = (color & 0x00ff00) >> 8;
				blue = (color & 0x0000ff);
				color = ((int)(blue * current_v) | (((int)(green * current_v)) << 8) | (((int)(red * current_v)) << 16) | ALPHA);
				
				zbuf[index] = z;
				pixels[index] = color;
				modelbuf[index] = currentContainer;
				
				/*
				if (color == 0xFFFFFFFE) {
					
					System.out.println("WHITE " + Integer.toHexString(texture.pixels[tex_index]));
					System.out.println(" ==> ("+sv+", "+ev+") " + current_v);
					
				}
				*/
				
			}
			
			index++;
			z += dz;
			current_v += dv;
			texv += dtexv;
			texu += dtexu; 
		}
	}
	
	
	
}
