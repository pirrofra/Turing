CLIENT=TuringClient
SERVER=TuringServer
CLASS_PATH = .

CLIENT_DEPENDECIES = ChatRoom/ChatRoom.java \
			      ClientGui/ButtonHandler.java \
			      ClientGui/ClientNotifier.java \
			      ClientGui/ConfigEditor.java \
			      ClientGui/EditorForm.java \
			      ClientGui/EnterListener.java \
			      ClientGui/LogForm.java \
			      ClientGui/MainForm.java \
			      ClientGui/ResultDialog.java \
			      ClientGui/SelectOperation.java \
                  Message/MessageBuffer.java \
                  Message/Operation.java \
                  RemoteClientNotifier/RemoteClientNotifier.java \
                  RemoteUserTable/RemoteUserTable.java \
                  RequestExecutor/RequestExecutor.java

SERVER_DEPENDECIES = ChatRoom/ChatOrganizer.java \
                  Message/MessageBuffer.java \
                  Message/Operation.java \
                  RemoteClientNotifier/RemoteClientNotifier.java \
                  RemoteUserTable/RemoteUserTable.java \
                  ServerData/Document.java \
                  ServerData/DocumentTable.java \
                  ServerData/ServerData.java \
                  ServerData/ServerExecutor.java \
                  ServerData/User.java \
                  ServerData/UserTable.java

OUT=out/


clean:
	if [ -d "$(OUT)" ] ; then \
		rm -r $(OUT) ; \
	fi
	mkdir $(OUT)

client:
	make clean
	make clientpackages
	javac -d $(OUT) -classpath $(CLASS_PATH) $(CLIENT).java
	cd $(OUT) && jar cvfe $(CLIENT).jar $(CLIENT) *
	if [ -d "$(CLIENT)" ] ; then \
		rm -r $(CLIENT) ; \
	fi
	mkdir $(CLIENT)
	cp $(OUT)/$(CLIENT).jar $(CLIENT)/
	rm -r $(OUT)

server:
	make clean
	make serverpackages
	javac -d $(OUT) -classpath $(CLASS_PATH) $(SERVER).java
	cd $(OUT) && jar cvfe $(SERVER).jar $(SERVER) *
	if [ -d "$(SERVER)" ] ; then \
    		rm -r $(SERVER) ; \
        fi
	mkdir $(SERVER)
	cp $(OUT)/$(SERVER).jar $(SERVER)/
	rm -r $(OUT)


clientpackages:
	javac -d $(OUT) -classpath $(CLASS_PATH) $(CLIENT_DEPENDECIES)

serverpackages:
	javac -d $(OUT) -classpath $(CLASS_PATH) $(SERVER_DEPENDECIES)

all:
	make server
	make client