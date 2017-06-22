package ru.geekbrains.chat.server.gui;

import ru.geekbrains.chat.server.core.ChatServer;
import ru.geekbrains.chat.server.core.ChatServerListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatServerGUI extends JFrame implements ActionListener, ChatServerListener, Thread.UncaughtExceptionHandler {

    private static final int POS_X = 1100;
    private static final int POS_Y = 150;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final String TITLE = "Chat Server";
    private static final String START_LISTENING = "Start listening";
    private static final String DROP_ALL_CLIENTS = "Drop all clients";
    private static final String STOP_LISTENING = "Stop listening";

    private final ChatServer chatServer = new ChatServer(this);
    private final JButton btnStartListening = new JButton(START_LISTENING);
    private final JButton btnStopListening = new JButton(DROP_ALL_CLIENTS);
    private final JButton btnDropAllClients = new JButton(STOP_LISTENING);
    private final JTextArea log = new JTextArea();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatServerGUI();
            }
        });
    }

    private ChatServerGUI(){
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setTitle(TITLE);

        btnStartListening.addActionListener(this);
        btnDropAllClients.addActionListener(this);
        btnStopListening.addActionListener(this);

        JPanel upperPanel = new JPanel(new GridLayout(1, 3));
        upperPanel.add(btnStartListening);
        upperPanel.add(btnDropAllClients);
        upperPanel.add(btnStopListening);

        add(upperPanel, BorderLayout.NORTH);

        log.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);


        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnStartListening){
            chatServer.startListening(8189);
            throw new RuntimeException("Ой, что-то пошло не так");
        } else if (src == btnDropAllClients){
            chatServer.dropAllClients();
        } else if (src == btnStopListening){
            chatServer.stopListening();
        } else throw new RuntimeException("Unknown src = " + src);
    }

    @Override
    public void onChatServerLog(ChatServer chatServer, String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
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
}