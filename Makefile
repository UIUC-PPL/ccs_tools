#
# Makefile for Charm++ Java tools
#
# 
#JAVAC=javac -deprecation 
#JAVAC=gcj --main=charm.debug.ParDebug
CLASS_PREFIX=classes
JAVAC=javac -deprecation -d $(CLASS_PREFIX) -classpath $(CLASS_PREFIX) $(OPTS) 
JAR=jar cf 
JAR_OPTS=-C $(CLASS_PREFIX)

all: liveViz setReadonly lvClient

############# Debugger ################
# The debugger now uses `ant' to compile
#SRC_DEBUG=charm/debug/*.java charm/debug/fmt/*.java charm/debug/pdata/*.java charm/debug/inspect/*.java charm/ccs/*.java 
#CLASS_DEBUG=charm/debug -C classes charm/ccs
#DEST_DEBUG=bin/charmdebug.jar

#debugger: $(DEST_DEBUG)

#$(DEST_DEBUG): $(SRC_DEBUG)
#	$(JAVAC) $(SRC_DEBUG)
#	$(JAR) $@ $(JAR_OPTS) $(CLASS_DEBUG)

############ lvClient ##############
SRC_LVCLIENT=charm/lvClient/*.java charm/util/*.java charm/ccs/*.java
CLASS_LVCLIENT=charm/lvClient -C classes charm/util -C classes charm/ccs
DEST_LVCLIENT=bin/lvClient.jar

lvClient: $(DEST_LVCLIENT)

$(DEST_LVCLIENT): $(SRC_LVCLIENT)
	$(JAVAC) $(SRC_LVCLIENT)
	$(JAR) $@ $(JAR_OPTS) $(CLASS_LVCLIENT)

############ LiveViz ##############
SRC_LV=charm/liveViz/*.java charm/util/*.java charm/ccs/*.java 
CLASS_LV=charm/liveViz -C classes charm/util -C classes charm/ccs
DEST_LV=bin/liveViz.jar

liveViz: $(DEST_LV) 

$(DEST_LV): $(SRC_LV)
	$(JAVAC) $(SRC_LV)
	$(JAR) $@ $(JAR_OPTS) $(CLASS_LV)

############ setReadonly utility ##############
CLASS_RO=$(CLASS_DEBUG)
DEST_RO=bin/setReadonly.jar

setReadonly: $(DEST_RO) 

$(DEST_RO): charm/debug/SetReadonly.java
	$(JAVAC) $<
	$(JAR) $@ $(JAR_OPTS) $(CLASS_RO)

############ pokeCCS utility ##############
CLASS_CCS=charm/ccs
DEST_CCS=bin/pokeCCS.jar

pokeCCS: $(DEST_CCS) 

$(DEST_CCS): charm/ccs/PokeCCS.java
	$(JAVAC) $<
	$(JAR) $@ $(JAR_OPTS) $(CLASS_CCS)


############ Sample
# $(DEST_): $(SRC_)
#	$(JAVAC) $(SRC_)
#	$(JAR) $@ $(JAR_OPTS) $(CLASS_)

clean:
	rm -f *~ */*.class */*/*.class */*/*/*.class */*/*/*/*.class

superclean: clean
	rm -f bin/*.jar
