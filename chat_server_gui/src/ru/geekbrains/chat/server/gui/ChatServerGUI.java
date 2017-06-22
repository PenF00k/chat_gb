package ru.geekbrains.chat.server.gui;

import javax.swing.*;

public class ChatServerGUI extends JFrame {

    private static final int POS_X = 1100;
    private static final int POS_Y = 150;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final String TITLE = "Chat Server";
    private static final String START_LISTENING = "Start listening";
    private static final String DROP_ALL_CLIENTS = "Drop all clients";
    private static final String STOP_LISTENING = "Stop listening";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatServerGUI();
            }
        });
    }

    private ChatServerGUI(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setTitle(TITLE);

        setVisible(true);
    }

}