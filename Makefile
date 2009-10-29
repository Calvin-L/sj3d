
# Settings

SRC_DIR=src
BIN_DIR=bin
DOC_DIR=doc
JAVAC=javac
JAVAC_FLAGS=

# Files

SRC=Animator.java \
	Camera.java \
	ImageProducer.java \
	Material.java \
	Matrix.java \
	Model.java \
	Object3D.java \
	ObjImporter.java \
	Renderer.java \
	RenderSettings.java \
	Texture.java \
	Triangle.java \
	UVCoord.java \
	Vector.java \
	Vertex.java \
	World.java \
	WorldEventListener.java
OBJ=Animator.class \
	Camera.class \
	ImageProducer.class \
	Material.class \
	Matrix.class \
	Model.class \
	Object3D.class \
	ObjImporter.class \
	Renderer.class \
	RenderSettings.class \
	Texture.class \
	Triangle.class \
	UVCoord.class \
	Vector.class \
	Vertex.class \
	World.class \
	WorldEventListener.class

# Targets

all: bin sj3d jar

bin: 
	mkdir -p $(BIN_DIR)

sj3d: bin $(OBJ)

jar: bin sj3d
	jar cvf sj3d.jar -C $(BIN_DIR)/ sj3d 
	jar i sj3d.jar

doc: $(SRC)
	javadoc -sourcepath $(SRC_DIR) -protected -verbose -d $(DOC_DIR) -version -author sj3d

clean: 
	rm -rf $(BIN_DIR)
	rm -rf $(DOC_DIR)
	rm -f sj3d.jar


# Implicit rules

.SUFFIXES: .java .class
%.java: $(SRC_DIR)/sj3d/$<     # Dummy rule to convince 'make' that our java files DO exist
	
.java.class:
	$(JAVAC) $(JAVAC_FLAGS) -d $(BIN_DIR) -sourcepath $(SRC_DIR) $(SRC_DIR)/sj3d/$<

# Dependencies

Animator.class: World.class Animator.java
Camera.class: Object3D.class Vector.class Matrix.class Camera.java
ImageProducer.class: ImageProducer.java
Material.class: Texture.class Material.java
Matrix.class: Vector.class Vertex.class Matrix.java
Model.class: Object3D.class Material.class Vertex.class Triangle.class Model.java
Object3D.class: Matrix.class Object3D.java
ObjImporter.class: Triangle.class Vertex.class Vector.class Material.class \
	Texture.class Model.class ObjImporter.java
Renderer.class: Vector.class Vertex.class Matrix.class UVCoord.class \
	Model.class Material.class Texture.class Camera.class RenderSettings.class \
	Renderer.java
RenderSettings.class: RenderSettings.java
Texture.class: Texture.java
Triangle.class: Vertex.class Vector.class UVCoord.class Triangle.java
UVCoord.class: UVCoord.java
Vector.class: Vector.java
Vertex.class: Vector.class Vertex.java
World.class: Model.class Renderer.class RenderSettings.class ImageProducer.class \
	WorldEventListener.class World.java
WorldEventListener.class: WorldEventListener.java
