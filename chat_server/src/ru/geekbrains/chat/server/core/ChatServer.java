package ru.geekbrains.chat.server.core;

public class ChatServer {

    private ChatServerListener eventListener;

    public ChatServer(ChatServerListener eventListener){
        this.eventListener = eventListener;
    }

    public void startListening(int port){
        putLog("Server started listening");
    }

    public void dropAllClients(){
        putLog("Server dropped all clients");
    }

    public void stopListening(){
        putLog("Server stopped listening");
    }

    private void putLog(String msg){
        eventListener.onChatServerLog(this, msg);
    }
}
