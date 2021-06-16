
# Simple Java 3D

This is a 3D software rendering library for Java.

It was largely inspired by Peter Walser's excellent IDX3D Java software
renderer.  (IDX3D's official website is long-dead, but Alessandro Borges has
thrown up a [clone of IDX3D on GitHub](https://github.com/AlessandroBorges/IDX3D).)

## Features

 - all Java, no native libraries
 - tiny codebase (suitable for applets and small applications)
 - no external dependencies beyond the Java standard library
 - pretty fast (for a software renderer)
 - flat shading and smooth (Gouraud) shading
 - single-color and UV texture mapped materials
 - motion blur
 - can import OBJ files
 - can import MD2 files
 - picking (can identify what model is at a given x,y screen location)
 - antialiasing (pretty hacky, but it works)

Stuff that this does NOT do:

 - shadows
 - hardware acceleration

## Building the library

This command will generate sj3d.jar:

    make

To create a "doc" folder with javadocs:

    make doc

## Running the demo

A small demo is provided to prove that this stuff works. To build and run the
demo:

    make demo

## Using the library (and scene organization)

The library is pretty arcane. Here's how the pieces fit together. (The best
source of information on how to use this stuff is probably the demo app.)

    World
        RenderSettings
        Camera
            Position
            Target point (what the camera is looking at)
            Transformation (matrix)
        Models
            Material (color, diffuse, ambient properties)
                Texture (required only if material is textured)
            Transformation (matrix)
            Frames (to support animated models - different verts for each frame)
                Vertices
                    Vector (vertex normal, required only for smooth shading)
            Triangles (references vertices on the model by number)
                UVCoord (required only for textured surfaces)

## Known issues

 - API design does not prevent you from misunderstanding it and causing
   exceptions in the rendering engine (null pointer, array out of bounds, etc)
 - documentation is slim to nonexistant

## Why?

Mostly as an educational experience. It could possibly fill a role where native
libraries are not an option or the application needs to be very very small.
