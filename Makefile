
# Settings

SRC_DIR=src
BIN_DIR=bin
DOC_DIR=doc
JAVAC=javac
JAVAC_FLAGS=
JAVA=java

# Targets

.PHONY: all sj3d doc demo clean

all: sj3d.jar

$(BIN_DIR):
	mkdir -p $(BIN_DIR)

sj3d: $(BIN_DIR)
	find $(SRC_DIR) -iname '*.java' | xargs $(JAVAC) -d $(BIN_DIR) $(JAVAC_FLAGS)

demo/SJ3DDemo.class: sj3d.jar demo/SJ3DDemo.java
	$(JAVAC) -cp sj3d.jar demo/SJ3DDemo.java

demo: sj3d.jar demo/SJ3DDemo.class
	$(JAVA) -cp .:sj3d.jar demo.SJ3DDemo

sj3d.jar: $(BIN_DIR) sj3d
	jar cf sj3d.jar -C $(BIN_DIR)/ sj3d

doc: $(SRC)
	javadoc -sourcepath $(SRC_DIR) -protected -verbose -d $(DOC_DIR) -version -author sj3d

clean:
	$(RM) -rf $(BIN_DIR)
	$(RM) -rf $(DOC_DIR)
	$(RM) -f sj3d.jar
	$(RM) -f demo/*.class
