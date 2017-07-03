package ru.geekbrains.chat.network;

import java.net.Socket;

public interface SocketThreadListener {

    void onStartSocketThread(SocketThread socketThread);
    void onStopSocketThread(SocketThread socketThread);

    void onReadySocketThread(SocketThread socketThread, Socket socket);
    void onRecieveString(SocketThread socketThread, Socket socket, String value);

    void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e);
}
