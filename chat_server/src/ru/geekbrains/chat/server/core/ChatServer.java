package ru.geekbrains.chat.server.core;

import ru.geekbrains.chat.network.ServerSocketThread;
import ru.geekbrains.chat.network.ServerSocketThreadListener;
import ru.geekbrains.chat.network.SocketThread;
import ru.geekbrains.chat.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private ChatServerListener eventListener;
    private final SecurityManager securityManager;
    private ServerSocketThread serverSocketThread;

    public ChatServer(ChatServerListener eventListener, SecurityManager securityManager){
        this.eventListener = eventListener;
        this.securityManager = securityManager;
    }

    public void startListening(int port){
        if (serverSocketThread != null && serverSocketThread.isAlive()){
            putLog("Сервер уже запущен");
            return;
        }
        serverSocketThread = new ServerSocketThread(this, "ServerSocketThread", 8189, 2000);
        securityManager.init();
    }

    public void dropAllClients(){
        putLog("Server dropped all clients");
    }

    public void stopListening(){
        if (serverSocketThread == null || !serverSocketThread.isAlive()){
            putLog("Сервер не запущен");
            return;
        }
        serverSocketThread.interrupt();
        securityManager.dispose();
    }

    //ServerSocketThread
    @Override
    public void onStartServerSocketThread(ServerSocketThread thread) {
        putLog("Started...");
    }

    @Override
    public void onStopServerSocketThread(ServerSocketThread thread) {
        putLog("Stopped...");
    }

    @Override
    public void onReadyServerSocketThread(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("Socket is ready...");
    }

    @Override
    public void onTimeOutAccept(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("accept() timeout");
    }

    @Override
    public void onAcceptedSocket(ServerSocketThread thread, ServerSocket serverSocket, Socket socket) {
        putLog("Client connected: " + socket);
        String threadName = "Socket thread: " + socket.getInetAddress() + ": " + socket.getPort();
        new SocketThread(this, threadName, socket);
    }

    @Override
    public void onExceptionServerSocketThread(ServerSocketThread thread, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }

    private synchronized void putLog(String msg){
        String msgLog = dateFormat.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() + ": " + msg;
        eventListener.onChatServerLog(this, msgLog);
    }

    //SocketThread
    @Override
    public synchronized void onStartSocketThread(SocketThread socketThread) {

    }

    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {

    }

    @Override
    public synchronized void onReadySocketThread(SocketThread socketThread, Socket socket) {

    }

    @Override
    public synchronized void onRecieveString(SocketThread socketThread, Socket socket, String value) {

    }

    @Override
    public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {

    }
}
