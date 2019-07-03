CLIENT=TuringClient.java

CLIENT_DEPENDECIES = ChatRoom/ChatRoom.java \
			      ClientGui/ButtonHandler.java \
			      ClientGui/ClientNotifier.java \
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

SERVER_DEPENDECIES = ChatRoom/ChatOrganizer \
                  Message/MessageBuffer.java \
                  Message/Operation.java \
                  RemoteClientNotifier/RemoteClientNotifier.java \
                  RemoteUserTable/RemoteUserTable.java \
                  ServerData/Document.Java \
                  ServerData/DocumentTable.java \
                  ServerData/ServerData.java \
                  ServerData/ServerExecutor.java \
                  ServerData/User.java \
                  ServerData/UserTable.java

OUT=out/


clean:
	rm -r $(OUT)
	mkdir $(OUT)

client:
	make clean
	make clientpackages
	javac $(CLIENT) -d $(OUT)

server:
	echo

clientpackages:
	cp -R *.java $(OUT)
	javac $(CLIENT_DEPENDECIES) -d $(OUT)

