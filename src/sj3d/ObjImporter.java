package sj3d;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public interface ModelFormat {

        public boolean load(InputStream stream, Model model);

    }

    private class ObjFormat implements ModelFormat {

        public boolean load(InputStream stream, Model model) {
            int vOffset = model.numVertices();
            ArrayList<Vector> vn = null; // vertex normals
            ArrayList<Vertex> vertices = new ArrayList<Vertex>();
            ArrayList<UVCoord> uvs = new ArrayList<UVCoord>();

            model.addFrame();

            try {

                String line = "";

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String[] parts;
                String[] parts2;
                int[] verts, uvs2;
                float x, y, z;

                while ((line = reader.readLine()) != null) {

                    bytesRead += line.length() + 1;
                    percentDone = (float) bytesRead / bytesToRead * 100;

                    if (line.charAt(0) == 'v') {
                        if (line.charAt(1) == 't') {
                            parts = line.split(" ");
                            x = Float.parseFloat(parts[1]);
                            y = Float.parseFloat(parts[2]);
                            uvs.add(new UVCoord(x, y));
                        } else if (line.charAt(1) == 'n') {
                            if (vn == null)
                                vn = new ArrayList<Vector>(model.numVertices());
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
                        // System.out.println("v");
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
                                verts[i - 1] = Integer.parseInt(parts2[0])
                                        + vOffset;
                                Vector v = vn.get(Integer.parseInt(parts2[2]) - 1);
                                // v.invert(); // Ugh... wtf?
                                model.getVertex(Integer.parseInt(parts2[0])
                                        + vOffset - 1).n = v;
                                if (parts2[1].length() > 0)
                                    uvs2[i - 1] = Integer.parseInt(parts2[1]);
                            }
                        }
                        Triangle t;
                        if (uvs.size() > 0) {
                            t = new Triangle(model, verts[0] - 1, verts[1] - 1,
                                    verts[2] - 1, uvs.get(uvs2[0] - 1), uvs
                                            .get(uvs2[1] - 1), uvs.get(uvs2[2] - 1));
                        } else {
                            t = new Triangle(model, verts[0] - 1, verts[1] - 1,
                                    verts[2] - 1);
                        }
                        model.addTriangle(t);
                        // System.out.println(t.getNormal());
                        // System.out.println(container.getVertex(verts[0] - 1).n +
                        // " | " + container.getVertex(verts[1] - 1).n + " | " +
                        // container.getVertex(verts[2] - 1).n);
                    }

                }
                reader.close();
                percentDone = 100;

                model.trim();

                // System.out.println("Object imported: '"+obj.name+"' ("+model.numVertices()+" vertices, "+model.numTriangles()+" triangles)");

                return true;

            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }

            return false;
        }

    }

    private class MD2Format implements ModelFormat {

        private DataInputStream istream;
        private int offset; // Position in file (in bytes)


        private float[][] normals = {
            { -0.525731f,  0.000000f,  0.850651f },
            { -0.442863f,  0.238856f,  0.864188f },
            { -0.295242f,  0.000000f,  0.955423f },
            { -0.309017f,  0.500000f,  0.809017f },
            { -0.162460f,  0.262866f,  0.951056f },
            {  0.000000f,  0.000000f,  1.000000f },
            {  0.000000f,  0.850651f,  0.525731f },
            { -0.147621f,  0.716567f,  0.681718f },
            {  0.147621f,  0.716567f,  0.681718f },
            {  0.000000f,  0.525731f,  0.850651f },
            {  0.309017f,  0.500000f,  0.809017f },
            {  0.525731f,  0.000000f,  0.850651f },
            {  0.295242f,  0.000000f,  0.955423f },
            {  0.442863f,  0.238856f,  0.864188f },
            {  0.162460f,  0.262866f,  0.951056f },
            { -0.681718f,  0.147621f,  0.716567f },
            { -0.809017f,  0.309017f,  0.500000f },
            { -0.587785f,  0.425325f,  0.688191f },
            { -0.850651f,  0.525731f,  0.000000f },
            { -0.864188f,  0.442863f,  0.238856f },
            { -0.716567f,  0.681718f,  0.147621f },
            { -0.688191f,  0.587785f,  0.425325f },
            { -0.500000f,  0.809017f,  0.309017f },
            { -0.238856f,  0.864188f,  0.442863f },
            { -0.425325f,  0.688191f,  0.587785f },
            { -0.716567f,  0.681718f, -0.147621f },
            { -0.500000f,  0.809017f, -0.309017f },
            { -0.525731f,  0.850651f,  0.000000f },
            {  0.000000f,  0.850651f, -0.525731f },
            { -0.238856f,  0.864188f, -0.442863f },
            {  0.000000f,  0.955423f, -0.295242f },
            { -0.262866f,  0.951056f, -0.162460f },
            {  0.000000f,  1.000000f,  0.000000f },
            {  0.000000f,  0.955423f,  0.295242f },
            { -0.262866f,  0.951056f,  0.162460f },
            {  0.238856f,  0.864188f,  0.442863f },
            {  0.262866f,  0.951056f,  0.162460f },
            {  0.500000f,  0.809017f,  0.309017f },
            {  0.238856f,  0.864188f, -0.442863f },
            {  0.262866f,  0.951056f, -0.162460f },
            {  0.500000f,  0.809017f, -0.309017f },
            {  0.850651f,  0.525731f,  0.000000f },
            {  0.716567f,  0.681718f,  0.147621f },
            {  0.716567f,  0.681718f, -0.147621f },
            {  0.525731f,  0.850651f,  0.000000f },
            {  0.425325f,  0.688191f,  0.587785f },
            {  0.864188f,  0.442863f,  0.238856f },
            {  0.688191f,  0.587785f,  0.425325f },
            {  0.809017f,  0.309017f,  0.500000f },
            {  0.681718f,  0.147621f,  0.716567f },
            {  0.587785f,  0.425325f,  0.688191f },
            {  0.955423f,  0.295242f,  0.000000f },
            {  1.000000f,  0.000000f,  0.000000f },
            {  0.951056f,  0.162460f,  0.262866f },
            {  0.850651f, -0.525731f,  0.000000f },
            {  0.955423f, -0.295242f,  0.000000f },
            {  0.864188f, -0.442863f,  0.238856f },
            {  0.951056f, -0.162460f,  0.262866f },
            {  0.809017f, -0.309017f,  0.500000f },
            {  0.681718f, -0.147621f,  0.716567f },
            {  0.850651f,  0.000000f,  0.525731f },
            {  0.864188f,  0.442863f, -0.238856f },
            {  0.809017f,  0.309017f, -0.500000f },
            {  0.951056f,  0.162460f, -0.262866f },
            {  0.525731f,  0.000000f, -0.850651f },
            {  0.681718f,  0.147621f, -0.716567f },
            {  0.681718f, -0.147621f, -0.716567f },
            {  0.850651f,  0.000000f, -0.525731f },
            {  0.809017f, -0.309017f, -0.500000f },
            {  0.864188f, -0.442863f, -0.238856f },
            {  0.951056f, -0.162460f, -0.262866f },
            {  0.147621f,  0.716567f, -0.681718f },
            {  0.309017f,  0.500000f, -0.809017f },
            {  0.425325f,  0.688191f, -0.587785f },
            {  0.442863f,  0.238856f, -0.864188f },
            {  0.587785f,  0.425325f, -0.688191f },
            {  0.688191f,  0.587785f, -0.425325f },
            { -0.147621f,  0.716567f, -0.681718f },
            { -0.309017f,  0.500000f, -0.809017f },
            {  0.000000f,  0.525731f, -0.850651f },
            { -0.525731f,  0.000000f, -0.850651f },
            { -0.442863f,  0.238856f, -0.864188f },
            { -0.295242f,  0.000000f, -0.955423f },
            { -0.162460f,  0.262866f, -0.951056f },
            {  0.000000f,  0.000000f, -1.000000f },
            {  0.295242f,  0.000000f, -0.955423f },
            {  0.162460f,  0.262866f, -0.951056f },
            { -0.442863f, -0.238856f, -0.864188f },
            { -0.309017f, -0.500000f, -0.809017f },
            { -0.162460f, -0.262866f, -0.951056f },
            {  0.000000f, -0.850651f, -0.525731f },
            { -0.147621f, -0.716567f, -0.681718f },
            {  0.147621f, -0.716567f, -0.681718f },
            {  0.000000f, -0.525731f, -0.850651f },
            {  0.309017f, -0.500000f, -0.809017f },
            {  0.442863f, -0.238856f, -0.864188f },
            {  0.162460f, -0.262866f, -0.951056f },
            {  0.238856f, -0.864188f, -0.442863f },
            {  0.500000f, -0.809017f, -0.309017f },
            {  0.425325f, -0.688191f, -0.587785f },
            {  0.716567f, -0.681718f, -0.147621f },
            {  0.688191f, -0.587785f, -0.425325f },
            {  0.587785f, -0.425325f, -0.688191f },
            {  0.000000f, -0.955423f, -0.295242f },
            {  0.000000f, -1.000000f,  0.000000f },
            {  0.262866f, -0.951056f, -0.162460f },
            {  0.000000f, -0.850651f,  0.525731f },
            {  0.000000f, -0.955423f,  0.295242f },
            {  0.238856f, -0.864188f,  0.442863f },
            {  0.262866f, -0.951056f,  0.162460f },
            {  0.500000f, -0.809017f,  0.309017f },
            {  0.716567f, -0.681718f,  0.147621f },
            {  0.525731f, -0.850651f,  0.000000f },
            { -0.238856f, -0.864188f, -0.442863f },
            { -0.500000f, -0.809017f, -0.309017f },
            { -0.262866f, -0.951056f, -0.162460f },
            { -0.850651f, -0.525731f,  0.000000f },
            { -0.716567f, -0.681718f, -0.147621f },
            { -0.716567f, -0.681718f,  0.147621f },
            { -0.525731f, -0.850651f,  0.000000f },
            { -0.500000f, -0.809017f,  0.309017f },
            { -0.238856f, -0.864188f,  0.442863f },
            { -0.262866f, -0.951056f,  0.162460f },
            { -0.864188f, -0.442863f,  0.238856f },
            { -0.809017f, -0.309017f,  0.500000f },
            { -0.688191f, -0.587785f,  0.425325f },
            { -0.681718f, -0.147621f,  0.716567f },
            { -0.442863f, -0.238856f,  0.864188f },
            { -0.587785f, -0.425325f,  0.688191f },
            { -0.309017f, -0.500000f,  0.809017f },
            { -0.147621f, -0.716567f,  0.681718f },
            { -0.425325f, -0.688191f,  0.587785f },
            { -0.162460f, -0.262866f,  0.951056f },
            {  0.442863f, -0.238856f,  0.864188f },
            {  0.162460f, -0.262866f,  0.951056f },
            {  0.309017f, -0.500000f,  0.809017f },
            {  0.147621f, -0.716567f,  0.681718f },
            {  0.000000f, -0.525731f,  0.850651f },
            {  0.425325f, -0.688191f,  0.587785f },
            {  0.587785f, -0.425325f,  0.688191f },
            {  0.688191f, -0.587785f,  0.425325f },
            { -0.955423f,  0.295242f,  0.000000f },
            { -0.951056f,  0.162460f,  0.262866f },
            { -1.000000f,  0.000000f,  0.000000f },
            { -0.850651f,  0.000000f,  0.525731f },
            { -0.955423f, -0.295242f,  0.000000f },
            { -0.951056f, -0.162460f,  0.262866f },
            { -0.864188f,  0.442863f, -0.238856f },
            { -0.951056f,  0.162460f, -0.262866f },
            { -0.809017f,  0.309017f, -0.500000f },
            { -0.864188f, -0.442863f, -0.238856f },
            { -0.951056f, -0.162460f, -0.262866f },
            { -0.809017f, -0.309017f, -0.500000f },
            { -0.681718f,  0.147621f, -0.716567f },
            { -0.681718f, -0.147621f, -0.716567f },
            { -0.850651f,  0.000000f, -0.525731f },
            { -0.688191f,  0.587785f, -0.425325f },
            { -0.587785f,  0.425325f, -0.688191f },
            { -0.425325f,  0.688191f, -0.587785f },
            { -0.425325f, -0.688191f, -0.587785f },
            { -0.587785f, -0.425325f, -0.688191f },
            { -0.688191f, -0.587785f, -0.425325f }
        };

        // Little-endian nextByte() implementation
        private byte nextByte() throws IOException {

            offset += 1;
            return istream.readByte();

        }

        private short nextUnsignedByte() throws IOException {
            offset += 1;
            return (short)(istream.readUnsignedByte() & 0xff);
        }

        private int nextInt() throws IOException {
            short[] bytes = {
                    nextUnsignedByte(),
                    nextUnsignedByte(),
                    nextUnsignedByte(),
                    nextUnsignedByte()
            };
            int result = (bytes[3] << 24) | (bytes[2] << 16) | (bytes[1] << 8) | (bytes[0]);
            return result;
        }

        private short nextShort() throws IOException {
            int[] bytes = {
                    nextUnsignedByte(),
                    nextUnsignedByte()
            };
            short result = (short)((bytes[1] << 8) | (bytes[0]));
            return result;
        }

        private float nextFloat() throws IOException {
            int[] bytes = {
                    nextUnsignedByte(),
                    nextUnsignedByte(),
                    nextUnsignedByte(),
                    nextUnsignedByte()
            };
            float result = Float.intBitsToFloat((bytes[3] << 24) + (bytes[2] << 16) + (bytes[1] << 8) + (bytes[0]));
            return result;
        }

        private String nextString(int l) throws IOException {
            byte[] chars = new byte[l];
            offset += l;
            istream.read(chars);
            return new String(chars);
        }

        private void skipTo(int position) throws IOException {
            int numBytes = position - offset;
            if (numBytes > 0) {
                istream.skip(numBytes);
                offset = position;
            }
        }

        public boolean load(InputStream stream, Model model) {
            istream = new DataInputStream(stream);

            try {

                offset = 0;

                // Header
                int ident = nextInt();
                if (ident != 844121161) {
                    write("Invalid file identifier: " + ident);
                    close();
                    return false;
                }

                int version = nextInt();
                if (version != 8) {
                    write("Invalid file version: " + version);
                    close();
                    return false;
                }

                int skinWidth = nextInt();
                int skinHeight = nextInt();
                nextInt(); //int frameSize = nextInt();

                int numSkins = nextInt();
                int numVertices = nextInt();
                int numTexCoords = nextInt();
                int numTriangles = nextInt();
                int numGLCommands = nextInt();
                int numFrames = nextInt();

                int offsetSkins = nextInt();
                int offsetTexCoords = nextInt();
                int offsetTriangles = nextInt();
                int offsetFrames = nextInt();
                int offsetGLCommands = nextInt();
                int offsetEnd = nextInt(); //int offsetEnd = nextInt();


                write("Skins:                  " + numSkins);
                write("Vertices:               " + numVertices);
                write("Texture Coords:         " + numTexCoords);
                write("Triangles:              " + numTriangles);
                write("GL Commands:            " + numGLCommands);
                write("Frames:                 " + numFrames);

                write("Skin offset:            " + offsetSkins);
                write("Texture Coords offset:  " + offsetTexCoords);
                write("Triangles offset:       " + offsetTriangles);
                write("Frames offset:          " + offsetFrames);
                write("GL Commands offset:     " + offsetGLCommands);
                write("End offset:             " + offsetEnd);

                // Skins
                if (numSkins > 0) {
                    skipTo(offsetSkins);
                    write("Skins ("+numSkins+")");
                    for (int i = 0; i < numSkins; i++) {
                        String filename = nextString(64);
                        write("    " + (i) + ": " + filename);
                        /*
                        InputStream s = getStream(filename);
                        m.setTexture(TextureIO.newTexture(s, false, null));
                        */
                        // TODO: Texture impor
                    }
                }

                ArrayList<UVCoord> uvs = new ArrayList<UVCoord>();

                // Texture coords
                if (numTexCoords > 0) {
                    //m.setNumTexCoords(numTexCoords);
                    skipTo(offsetTexCoords);
                    write("Texture coordinates ("+numTexCoords+")");
                    for (int i = 0; i < numTexCoords; i++) {
                        float u = (float)nextShort() / skinWidth;
                        float v = (float)nextShort() / skinHeight;
                        write("    " + (i) + ": " + u + ", " + v);
                        uvs.add(new UVCoord(u,v));
                        //m.addTexCoord(u, v);
                    }
                }

                // Triangles
                if (numTriangles > 0) {
                    //model.setNumTriangles(numTriangles);
                    skipTo(offsetTriangles);
                    write("Triangles ("+numTriangles+")");
                    for (int i = 0; i < numTriangles; i++) {
                        short v1 = nextShort();
                        short v2 = nextShort();
                        short v3 = nextShort();
                        short uv1 = nextShort();
                        short uv2 = nextShort();
                        short uv3 = nextShort();
                        Triangle t = new Triangle(model, v1, v2, v3, uvs.get(uv1), uvs.get(uv2), uvs.get(uv3));
                        model.addTriangle(t);
                        write("    "+(i)+": vertices ("+v1+"," + v2 + "," + v3 + "), tex coords (" + uv1 + "," + uv2 + "," + uv3 + ")");
                    }
                }

                // Frames -- here is where the real content is
                if (numFrames > 0) {
                    //m.setNumVertices(numVertices);
                    //model.setNumFrames(numFrames);
                    skipTo(offsetFrames);
                    write("Frames ("+numFrames+")");
                    for (int i = 0; i < numFrames; i++) {
                        float sclx = nextFloat();
                        float scly = nextFloat();
                        float sclz = nextFloat();
                        float transx = nextFloat();
                        float transy = nextFloat();
                        float transz = nextFloat();
                        //nextString(16);
                        String name = nextString(16);
                        write("    " + (i) + ": " + name);
                        //if (i==0) write("      scale: ("+sclx+","+scly+","+sclz+")");
                        //if (i==0) write("      trans: ("+transx+","+transy+","+transz+")");
                        //model.addFrame(sclx, scly, sclz, transx, transy, transz); // TODO: support scale/translate for MD2
                        model.addFrame();
                        write("      vertices: ");
                        for (int j = 0; j < numVertices; j++) {
                            short x = nextUnsignedByte();
                            short y = nextUnsignedByte();
                            short z = nextUnsignedByte();
                            short n = nextUnsignedByte();
                            n -= 2; // ZOMG retarded blender export scrip
                            write("        -> "+(j)+": " + x + ", " + y + ", " + z + ", " + n);
                            Vertex v = new Vertex(x, y, z, new Vector(normals[n][0], normals[n][1], normals[n][2]));
                            model.addVertex(v);
                        }
                    }
                }

                // GL Commands
                /*
                if (numGLCommands > 0) {
                    skipTo(offsetGLCommands);
                    //write("GL Commands ("+numGLCommands+")");
                    int end = offset + (numGLCommands*4);
                    while (offset < end) {
                        int num_cmds = nextInt();
                        if (num_cmds == 0) break;
                        //write(" -> # cmds: " + num_cmds);
                        int cmd_mode = GL.GL_TRIANGLE_STRIP;
                        if (num_cmds < 0) {
                            cmd_mode = GL.GL_TRIANGLE_FAN;
                            num_cmds = -num_cmds;
                        }
                        //if (cmd_mode == GL.GL_TRIANGLE_STRIP) write("          : Triangle Strip");
                        //else write("          : Triangle Fan");
                        m.startGLCommandList(cmd_mode, num_cmds);
                        for (int i = 0; i < num_cmds; i++) {
                            float u = nextFloat();
                            float v = nextFloat();
                            int vindex = nextInt();
                            //write("       -> Vertex "+vindex+": uv("+u+","+v+")");
                            m.addGLCommand(u, v, vindex);
                        }
                    }
                }
                */

                write("File reading reached "+offset+"/"+offsetEnd);

                //model.rotate(0, -90, 0);

                model.trim();

                close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        private void close() throws IOException {
            istream.close();
        }

        private void write(String s) {
            System.out.println("    | " + s);
        }

    }

    public ModelFormat OBJ = new ObjFormat();
    public ModelFormat MD2 = new MD2Format();

    private class Importable {
        public final String name;
        public final Model container;
        public final String filename;
        public final ModelFormat format;

        public Importable(String n, String file, ModelFormat f, Model m) throws IOException {
            name = n;
            container = m;
            format = f;
            filename = file;
        }
    }

    public ObjImporter(String codebase) {
        this.codebase = codebase;
        numModels = 0;
        q = new LinkedBlockingQueue<Importable>();
        t = new Thread(this, "importer");
        current = "";
    }

    private InputStream getInputStream(String filename) throws IOException {
        filename = codebase + filename;

        URL url = new URL(filename);
        URLConnection urlConn = url.openConnection();
        urlConn.setDoInput(true);
        urlConn.setDoOutput(false);
        urlConn.setUseCaches(false);
        int l = urlConn.getContentLength();
        if (l > -1)
            bytesToRead += l;

        return urlConn.getInputStream();
    }

    /*
     * private BufferedReader getReader(String filename) throws IOException {
     * filename = codebase + filename;
     *
     * URL url = new URL(filename); URLConnection urlConn =
     * url.openConnection(); urlConn.setDoInput(true);
     * urlConn.setDoOutput(false); urlConn.setUseCaches(false); int l =
     * urlConn.getContentLength(); if (l > -1) bytesToRead += l;
     *
     * BufferedReader reader = new BufferedReader(new
     * InputStreamReader(urlConn.getInputStream()));
     *
     * return reader; }
     */

    public void add(String name, String obj, ModelFormat format, Model container) {
        numModels++;
        Importable i;
        try {
            i = new Importable(name, obj, format, container);
            q.offer(i);
            if (paused)
                t.start();
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
            boolean success = false;
            try {
                success = obj.format.load(getInputStream(obj.filename), obj.container);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!success) {
                System.out.println("Failed to import object: '" + obj.name + "'");
            }
            //importObj(obj, false);
        }
    }

    public void run() {
        while (true) {
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

    public String getCurrent() {
        return current;
    }

    public float percentDone() {
        return percentDone;
    }

    public String toString() {
        return (importing() ? "Loading... (" + percentDone + "%)" : "");
    }

}
