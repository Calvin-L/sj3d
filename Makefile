COMPILE=javac
COMPILE_FLAGS=
SRC_DIR=src
BIN_DIR=bin


all: binfolder sj3d jar

binfolder:
	mkdir -p $(BIN_DIR)

sj3d:
	$(COMPILE) $(COMPILE_FLAGS) -sourcepath $(SRC_DIR) -d $(BIN_DIR) $(SRC_DIR)/sj3d/*.java

jar: sj3d
	jar cvf sj3d.jar -C $(BIN_DIR)/ sj3d 
	jar i sj3d.jar
	
clean: 
	rm -rf $(BIN_DIR)
	rm -f sj3d.jar