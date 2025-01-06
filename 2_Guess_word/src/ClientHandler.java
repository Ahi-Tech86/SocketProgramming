import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ClientHandler implements Runnable {

    private Server server;
    private Socket socket;
    private String clientUsername;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    public static HashMap<String, Integer> clientsPointsMap = new HashMap<>();
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            clientsPointsMap.put(this.clientUsername, 0);
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient != null) {
                    handleWordGuess(messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void handleWordGuess(String messageFromClient) {
        String guessedWord = server.getGuessedWord();

        if (messageFromClient.length() == 1) {
            processingSingleCharacterGuess(messageFromClient.charAt(0), guessedWord);
        } else {
            processingFullWordGuess(messageFromClient, guessedWord);
        }

        broadcastMessage(new String(server.hiddenArray));
    }

    private void processingSingleCharacterGuess(char guess, String guessedWord) {
        int matches = 0;

        for (int i = 0; i < guessedWord.length(); i++) {
            if (guessedWord.charAt(i) == guess) {
                server.hiddenArray[i] = guess;
                matches++;
            }
        }

        updateClientPoints(matches);
        checkForWinCondition();
    }

    private void processingFullWordGuess(String messageFromClient, String guessedWord) {
        if (messageFromClient.equals(guessedWord)) {
            int matches = (int) IntStream.range(0, server.hiddenArray.length)
                    .filter(i -> server.hiddenArray[i] == '*')
                    .count();
            matches = (int) (matches * 1.5f);
            updateClientPoints(matches);

            broadcastMessage("SERVER: user " + clientUsername + " guessed word");
            broadcastPoints();
            resetGame();
        }
    }

    private void updateClientPoints(int matches) {
        clientsPointsMap.put(clientUsername, clientsPointsMap.get(clientUsername) + matches);
    }

    private void checkForWinCondition() {
        if (!new String(server.hiddenArray).contains("*")) {
            broadcastMessage("SERVER: user " + clientUsername + " guessed word");
            broadcastPoints();
            resetGame();
        }
    }

    private void resetGame() {
        server.choiceWord();
        broadcastMessage("SERVER: a new number has been generated. Try to guess it!");
    }

    private void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.bufferedWriter.write(messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    private void broadcastPoints() {
        StringBuilder pointsMessage = new StringBuilder("SERVER: Current points:\n");
        for (Map.Entry<String, Integer> entry : clientsPointsMap.entrySet()) {
            pointsMessage.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        broadcastMessage(pointsMessage.toString());
    }

    private void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat");
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }

            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
