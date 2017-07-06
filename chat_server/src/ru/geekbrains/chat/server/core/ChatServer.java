package ru.geekbrains.chat.server.core;

import com.sun.xml.internal.ws.api.message.MessageWritable;
import ru.geekbrains.chat.library.Messages;
import ru.geekbrains.chat.network.ServerSocketThread;
import ru.geekbrains.chat.network.ServerSocketThreadListener;
import ru.geekbrains.chat.network.SocketThread;
import ru.geekbrains.chat.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
        if (client.isAuthorized() && !client.isReconnected()){
            sendToAllAuthorizedClients(Messages.getBroadcast("Server", client.getNick() + " disconnected"));
        }
        sendToAllAuthorizedClients(Messages.getUsersList(getUser()));
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

    private void handleNonAuthorizedClient(ChatSocketThread newClient, String msg){
        String[] tokens = msg.split(Messages.DELIMITER);
        if (tokens.length != 3 || !tokens[0].equals(Messages.AUTH_REQUEST)){
            newClient.messageFormatError(msg);
            return;
        }
        String login = tokens[1];
        String password = tokens[2];
        String nickname = securityManager.getNick(login, password);
        if (nickname == null){
            newClient.authError();
            return;
        }

        ChatSocketThread client = getClientByNick(nickname);
        newClient.setAuthorized(nickname);
        if (client == null){
            putLog(nickname + " connected");
            sendToAllAuthorizedClients(Messages.getBroadcast("Server", newClient.getNick() + " connected"));
            sendToAllAuthorizedClients(Messages.getUsersList(getUser()));
        } else {
            putLog(nickname + " reconnected");
            client.reconnect();
            newClient.sendMsg(Messages.getUsersList(getUser()));
        }
    }

    @Override
    public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }

    private ChatSocketThread getClientByNick(String nickname){
        final int cnt = clients.size();
        for (int i = 0; i < cnt; i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getNick().equals(nickname)) return client;
        }
        return null;
    }

    private String getUser(){
        //        if (clients.size() == 0) return;
        StringBuilder sb = new StringBuilder();
        final int cnt = clients.size();
        final int last = cnt -1;
        for (int i = 0; i < cnt; i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            sb.append(client.getNick()).append(Messages.DELIMITER);
            if (i != last) sb.append(Messages.DELIMITER);
        }
        return sb.toString();
    }
}
