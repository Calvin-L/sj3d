
# Settings

SRC_DIR=src
BIN_DIR=bin
DOC_DIR=doc
JAVAC=javac
JAVAC_FLAGS=

# Targets

.PHONY: all sj3d doc demo clean

all: sj3d.jar

$(BIN_DIR):
	mkdir -p $(BIN_DIR)

sj3d: $(BIN_DIR)
	find $(SRC_DIR) -iname '*.java' | xargs javac -d $(BIN_DIR) $(JAVAC_FLAGS)

demo/SJ3DDemo.class: sj3d.jar demo/SJ3DDemo.java
	javac -cp sj3d.jar demo/SJ3DDemo.java

demo: sj3d.jar demo/SJ3DDemo.class
	java -cp .:sj3d.jar demo.SJ3DDemo

sj3d.jar: $(BIN_DIR) sj3d
	jar cf sj3d.jar -C $(BIN_DIR)/ sj3d

doc: $(SRC)
	javadoc -sourcepath $(SRC_DIR) -protected -verbose -d $(DOC_DIR) -version -author sj3d

clean:
	rm -rf $(BIN_DIR)
	rm -rf $(DOC_DIR)
	rm -f sj3d.jar
	rm -f demo/SJ3DDemo.class
