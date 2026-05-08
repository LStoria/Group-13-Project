package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SocketClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> messageListener;
    private boolean running;
    private String username = "";

    public void connect(String host, int port, Consumer<String> listener) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        messageListener = listener;
        running = true;
        startListening();
    }

    public void setMessageListener(Consumer<String> listener) {
        messageListener = listener;
    }

    public void sendRequest(String jsonRequest) {
        if (out != null && isConnected()) {
            out.println(jsonRequest);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && running;
    }

    public void closeConnection() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();
        } catch (IOException ex) {
            System.err.println("Khong dong duoc ket noi: " + ex.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                String response;
                while (running && (response = in.readLine()) != null) {
                    Consumer<String> listener = messageListener;
                    if (listener != null) {
                        listener.accept(response);
                    }
                }
            } catch (IOException ex) {
                if (running) {
                    System.err.println("Mat ket noi server: " + ex.getMessage());
                }
            } finally {
                closeConnection();
            }
        }, "socket-client-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
}
