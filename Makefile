#
# Makefile for Charm++ Java tools
#
# 
JAVAC=javac -deprecation 
JAR=jar cf 

all: debugger liveViz

############# Debugger ################
SRC_DEBUG=charm/debug/*.java charm/debug/fmt/*.java charm/ccs/*.java 
CLASS_DEBUG=charm/debug/*.class charm/debug/fmt/*.class charm/ccs/*.class 
DEST_DEBUG=bin/charmdebug.jar

debugger: $(DEST_DEBUG)

$(DEST_DEBUG): $(SRC_DEBUG)
	$(JAVAC) $(SRC_DEBUG)
	$(JAR) $@ $(CLASS_DEBUG)

############ LiveViz ##############
SRC_LV=charm/liveViz/*.java charm/util/*.java charm/ccs/*.java 
CLASS_LV=charm/liveViz/*.class charm/liveViz/*.jpg charm/util/*.class charm/ccs/*.class 
DEST_LV=bin/liveViz.jar

liveViz: $(DEST_LV) 

$(DEST_LV): $(SRC_LV)
	$(JAVAC) $(SRC_LV)
	$(JAR) $@ $(CLASS_LV)

############ Sample
# $(DEST_): $(SRC_)
#	$(JAVAC) $(SRC_)
#	$(JAR) $@ $(CLASS_)

clean:
	rm -f *~ */*.class */*/*.class */*/*/*.class

superclean: clean
	rm -f bin/*.jar
