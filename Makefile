#
# Makefile for Charm++ Java tools
#
# 
JAVAC=javac -deprecation 
JAR=jar cf 

all: debugger liveViz setReadonly lvClient

############# Debugger ################
SRC_DEBUG=charm/debug/*.java charm/debug/fmt/*.java charm/debug/pdata/*.java charm/debug/inspect/*.java charm/ccs/*.java 
CLASS_DEBUG=charm/debug/*.class charm/debug/fmt/*.class charm/debug/pdata/*.class charm/debug/inspect/*.class charm/ccs/*.class 
DEST_DEBUG=bin/charmdebug.jar

debugger: $(DEST_DEBUG)

$(DEST_DEBUG): $(SRC_DEBUG)
	$(JAVAC) $(SRC_DEBUG)
	$(JAR) $@ $(CLASS_DEBUG)

############ lvClient ##############
SRC_LVCLIENT=charm/lvClient/*.java charm/util/*.java charm/ccs/*.java
CLASS_LVCLIENT=charm/lvClient/*.class charm/util/*.class charm/ccs/*.class
DEST_LVCLIENT=bin/lvClient.jar

lvClient: $(DEST_LVCLIENT)

$(DEST_LVCLIENT): $(SRC_LVCLIENT)
	$(JAVAC) $(SRC_LVCLIENT)
	$(JAR) $@ $(CLASS_LVCLIENT)

############ LiveViz ##############
SRC_LV=charm/liveViz/*.java charm/util/*.java charm/ccs/*.java 
CLASS_LV=charm/liveViz/*.class charm/util/*.class charm/ccs/*.class 
DEST_LV=bin/liveViz.jar

liveViz: $(DEST_LV) 

$(DEST_LV): $(SRC_LV)
	$(JAVAC) $(SRC_LV)
	$(JAR) $@ $(CLASS_LV)

############ setReadonly utility ##############
CLASS_RO=$(CLASS_DEBUG)
DEST_RO=bin/setReadonly.jar

setReadonly: $(DEST_RO) 

$(DEST_RO): charm/debug/SetReadonly.java
	$(JAVAC) $<
	$(JAR) $@ $(CLASS_RO)

############ pokeCCS utility ##############
CLASS_CCS=charm/ccs/*.class
DEST_CCS=bin/pokeCCS.jar

pokeCCS: $(DEST_CCS) 

$(DEST_CCS): charm/ccs/PokeCCS.java
	$(JAVAC) $<
	$(JAR) $@ $(CLASS_CCS)


############ Sample
# $(DEST_): $(SRC_)
#	$(JAVAC) $(SRC_)
#	$(JAR) $@ $(CLASS_)

clean:
	rm -f *~ */*.class */*/*.class */*/*/*.class

superclean: clean
	rm -f bin/*.jar
