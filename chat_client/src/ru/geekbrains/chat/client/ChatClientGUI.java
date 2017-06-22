package ru.geekbrains.chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClientGUI();
            }
        });
    }

    private static final int WIDTH = 900;
    private static final int HEIGHT = 300;
    private static final String TITLE = "Chat client";

    private final JPanel upperPanel = new JPanel(new GridLayout(2, 3));
    private final JTextField fieldIPAddr = new JTextField("89.222.249.131");
    private final JTextField fieldPort = new JTextField("8189");
    private final JCheckBox chkAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField fieldLogin = new JTextField("penf00k");
    private final JPasswordField fieldPass = new JPasswordField("000000");
    private final JButton btnLogin = new JButton("Login");

    private final JTextArea log = new JTextArea();
    private final JList<String> userList = new JList<>();

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

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

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

    private void connect(){
        System.out.println("Connect");
    }

    private void disconnect(){
        System.out.println("Disconnect");
    }

    private void sendMessage(){
        System.out.println("Send message");
    }
}
