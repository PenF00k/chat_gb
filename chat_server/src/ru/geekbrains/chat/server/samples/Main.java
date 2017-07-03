package ru.geekbrains.chat.server.samples;

public class Main {

    public static void main(String[] args) {
        try {
            Class.forName("ru.geekbrains.chat.server.samples.ExampleClass");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
