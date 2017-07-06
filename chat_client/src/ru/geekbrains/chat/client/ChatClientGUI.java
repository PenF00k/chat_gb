package ru.geekbrains.chat.client;

import ru.geekbrains.chat.library.Messages;
import ru.geekbrains.chat.network.SocketThread;
import ru.geekbrains.chat.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ChatClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClientGUI();
            }
        });
    }

    private Socket socket;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static final int WIDTH = 900;
    private static final int HEIGHT = 300;
    private static final String TITLE = "Chat client";

    private final JPanel upperPanel = new JPanel(new GridLayout(2, 3));
//    private final JTextField fieldIPAddr = new JTextField("127.0.0.1");
    private final JTextField fieldIPAddr = new JTextField("89.222.249.131");
    private final JTextField fieldPort = new JTextField("8189");
    private final JCheckBox chkAlwaysOnTop = new JCheckBox("Always on top");
//    private final JTextField fieldLogin = new JTextField("login_1");
    private final JTextField fieldLogin = new JTextField("penf00k");
//    private final JPasswordField fieldPass = new JPasswordField("pass_1");
    private final JPasswordField fieldPass = new JPasswordField("123456");
    private final JButton btnLogin = new JButton("Login");

    private final JTextArea log = new JTextArea();
    private static DefaultListModel<String> userListModel = new DefaultListModel();
    private final JList<String> userList = new JList<>(userListModel);


    private final JPanel bottomPanel = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final JTextField fieldInput = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private ChatClientGUI(){
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);
        setLocationRelativeTo(null);

        upperPanel.add(fieldIPAddr);
        upperPanel.add(fieldPort);
        upperPanel.add(chkAlwaysOnTop);
        upperPanel.add(fieldLogin);
        upperPanel.add(fieldPass);
        upperPanel.add(btnLogin);

        fieldIPAddr.addActionListener(this);
        fieldPort.addActionListener(this);
        fieldLogin.addActionListener(this);
        fieldPass.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);
        btnSend.addActionListener(this);
        chkAlwaysOnTop.addActionListener(this);
        fieldInput.addActionListener(this);

        add(upperPanel, BorderLayout.NORTH);

        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);

        JScrollPane scrollUsers = new JScrollPane(userList);

        scrollUsers.setPreferredSize(new Dimension(150, 0));
        add(scrollUsers, BorderLayout.EAST);

        bottomPanel.add(btnDisconnect, BorderLayout.WEST);
        bottomPanel.add(fieldInput, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);
        bottomPanel.setVisible(true);

        add(bottomPanel, BorderLayout.SOUTH);
        setConnectedViewVisible(false);

        setVisible(true);
    }

    private SocketThread socketThread;

    private void connect(){
        try {
            socket = new Socket(fieldIPAddr.getText(), Integer.parseInt(fieldPort.getText()));
            socketThread = new SocketThread(this, "SocketThread", socket);
        } catch (IOException e) {
            e.printStackTrace();
            log.append("Exception: " + e.getMessage() + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        }
        setConnectedViewVisible(true);
    }

    private void disconnect(){
        socketThread.close();
        setConnectedViewVisible(false);
    }

    private void sendMessage(){
        String msg = fieldInput.getText();
        if (msg.equals("")) return;
        fieldInput.setText(null);
        socketThread.sendMsg(msg);
    }

    private void setConnectedViewVisible(boolean isConnected){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                upperPanel.setVisible(!isConnected);
                bottomPanel.setVisible(isConnected);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (    src == btnLogin ||
                src == fieldIPAddr ||
                src == fieldPort ||
                src == fieldLogin ||
                src == fieldPass){
            connect();
        } else if (src == btnDisconnect){
            disconnect();
        } else if (src == fieldInput || src == btnSend){
            sendMessage();
        } else if (src == chkAlwaysOnTop){
            setAlwaysOnTop(chkAlwaysOnTop.isSelected());
        } else {
            throw new RuntimeException("Unknown src = " + src);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String msg;
        if (stackTraceElements.length == 0){
            msg = "Пустой";
        } else {
            msg = e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + stackTraceElements[0];
        }
        JOptionPane.showMessageDialog(null, msg, "Exception: ", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    //SocketThread
    @Override
    public void onStartSocketThread(SocketThread socketThread) {
        System.out.println(dateFormat.format(System.currentTimeMillis()) + ": " + "Поток сокета запущен.\n");
    }

    private static final String[] EMPTY = new String[0];

    @Override
    public void onStopSocketThread(SocketThread socketThread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(dateFormat.format(System.currentTimeMillis()) + ": " + "Соединение разорвано.\n");
                log.setCaretPosition(log.getDocument().getLength());
                setTitle(TITLE);
                userList.setListData(EMPTY);
                setConnectedViewVisible(false);
            }
        });
    }

    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(dateFormat.format(System.currentTimeMillis()) + ": " + "Соединение установлено.\n");
                log.setCaretPosition(log.getDocument().getLength());

                String login = fieldLogin.getText();
                String password = new String(fieldPass.getPassword());
                socketThread.sendMsg(Messages.getAuthRequest(login, password));
            }
        });
    }

    @Override
    public void onRecieveString(SocketThread socketThread, Socket socket, String value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String[] tokens = value.split(Messages.DELIMITER);
                String type = tokens[0];
                switch (type){
                    case Messages.BROADCAST:
                        log.append(dateFormat.format(Long.parseLong(tokens[1])) + ": " + tokens[2] + " написал(а): " + tokens[3] + ".\n");
                        log.setCaretPosition(log.getDocument().getLength());
                        break;
                    case Messages.USERS_LIST:
                        String allUsers = value.substring(Messages.USERS_LIST.length() + Messages.DELIMITER.length());
                        String[] users = allUsers.split(Messages.DELIMITER);
                        Arrays.sort(users);
                        userList.setListData(users);
                    case Messages.AUTH_ACCEPT:
                        setTitle(TITLE + " - Current user: " + tokens[1]);
                        break;
                    case Messages.AUTH_ERROR:
                        log.append(dateFormat.format(System.currentTimeMillis()) + ": " + "Неправильные имя/пароль\n");
                        log.setCaretPosition(log.getDocument().getLength());
                        break;
                    case Messages.RECONNECT:
                        log.append("Переподключен с другого клиента.\n");
                        log.setCaretPosition(log.getDocument().getLength());
                        break;
                    case Messages.MSG_FORMAT_ERROR:
                        log.append("Неверный формат сообщения: '" + value + "'\n");
                        log.setCaretPosition(log.getDocument().getLength());
                        break;
                    default:
                        throw new RuntimeException(dateFormat.format(System.currentTimeMillis()) + ": " + "Неизвестный тип сообщения: " + value);
                }
            }
        });
    }

    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                e.printStackTrace();
            }
        });
    }
}
