package chatapp;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static java.lang.Integer.parseInt;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket client;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private int user_id;

    public ClientHandler(Socket client) {
        try {
            this.client = client;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String str = bufferedReader.readLine();
            System.out.println("reading from buffer: " + str);
            clientHandlers.add(this);
        } catch(IOException e) {
            e.printStackTrace();
            closeEveryting(client, bufferedReader, bufferedWriter);
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
    }

    public int getUserId() {
        return user_id;
    }

    @Override
    public void run() {
        String messageFromClient;
        while (client.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                System.out.println(messageFromClient);
            } catch(IOException e) {
                closeEveryting(client, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void sendMessageToClients(String msg) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.getUserId() != user_id) {
                    clientHandler.bufferedWriter.write(msg);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch(IOException e) {
                closeEveryting(client, bufferedReader, bufferedWriter);
            }
        }
    }

    private void closeEveryting(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
