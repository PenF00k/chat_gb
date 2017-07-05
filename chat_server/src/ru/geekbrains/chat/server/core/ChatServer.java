package ru.geekbrains.chat.server.core;

import ru.geekbrains.chat.library.Messages;
import ru.geekbrains.chat.network.ServerSocketThread;
import ru.geekbrains.chat.network.ServerSocketThreadListener;
import ru.geekbrains.chat.network.SocketThread;
import ru.geekbrains.chat.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private ChatServerListener eventListener;
    private final SecurityManager securityManager;
    private ServerSocketThread serverSocketThread;
    private final Vector<SocketThread> clients = new Vector<>();
    private ChatSocketThread client;

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
        new ChatSocketThread(this, threadName, socket);
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
        System.out.println("started...");
    }

    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {
        clients.remove(socketThread);
        putLog("Stopped");
        if (client.isAuthorized()){
            sendToAllAuthorizedClients(Messages.getBroadcast("Server", client.getNick() + " disconnected"));
        }
        sendUsersList();
    }

    @Override
    public synchronized void onReadySocketThread(SocketThread socketThread, Socket socket) {
        System.out.println("Socket is ready...");
        clients.add(socketThread);
    }

    @Override
    public synchronized void onRecieveString(SocketThread socketThread, Socket socket, String value) {
        client = (ChatSocketThread) socketThread;
        if (client.isAuthorized()){
            handleAuthorizedClient(client, value);
        } else {
            handleNonAuthorizedClient(client, value);
        }

    }

    private void handleAuthorizedClient(ChatSocketThread client, String msg){
        sendToAllAuthorizedClients(Messages.getBroadcast(client.getNick(), msg));
    }

    private void sendToAllAuthorizedClients(String msg){
        for (int i = 0; i < clients.size(); i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if (client.isAuthorized()) clients.get(i).sendMsg(msg);
        }
    }

    private void handleNonAuthorizedClient(ChatSocketThread client, String msg){
        String[] tokens = msg.split(Messages.DELIMITER);
        if (tokens.length != 3 || !tokens[0].equals(Messages.AUTH_REQUEST)){
            client.messageFormatError(msg);
            return;
        }
        String login = tokens[1];
        String password = tokens[2];
        String nickname = securityManager.getNick(login, password);
        if (nickname == null){
            client.authError();
            return;
        }
        client.setAuthorized(nickname);
        putLog(nickname + " connected");
        sendToAllAuthorizedClients(Messages.getBroadcast("Server", client.getNick() + " connected"));
        sendUsersList();
    }

    @Override
    public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }

    private void sendUsersList(){
        if (clients.size() == 0) return;
        StringBuilder sb = new StringBuilder();
        for (SocketThread client : clients) {
            ChatSocketThread cst = (ChatSocketThread)client;
            sb.append(cst.getNick()).append(Messages.DELIMITER);
        }
        //По идее можно убрать строчку ниже, т.к. split (в клиенте) откинет пустую строку после разделителя
        sb.substring(0, sb.lastIndexOf(Messages.DELIMITER));
        sendToAllAuthorizedClients(Messages.getUsersList(sb.toString()));
    }
}
