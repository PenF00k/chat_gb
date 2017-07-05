package ru.geekbrains.chat.server.core;

import ru.geekbrains.chat.library.Messages;
import ru.geekbrains.chat.network.SocketThread;
import ru.geekbrains.chat.network.SocketThreadListener;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

class ChatSocketThread extends SocketThread {

    private boolean isAuthorized;
    private String nick;

    ChatSocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(eventListener, name, socket);
    }

    void setAuthorized(String nick){
        isAuthorized = true;
        this.nick = nick;
        sendMsg(Messages.getAuthAccept(nick));
    }

    boolean isAuthorized(){
        return isAuthorized;
    }

    void authError(){
        sendMsg(Messages.getAuthError());
        close();
    }

    void messageFormatError(String msg){
        sendMsg(Messages.getMsgFormatError(msg));
        close();
    }

    String getNick(){return nick;}

}
