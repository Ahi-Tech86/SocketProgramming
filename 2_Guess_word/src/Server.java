import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
    public char[] hiddenArray;
    private String guessedWord;
    private List<String> wordsList;
    private ServerSocket serverSocket;
    private static final int PORT = 11111;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.wordsList = initWordsList();
        choiceWord();
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Server server = new Server(serverSocket);
        server.start();
    }

    public void choiceWord() {
        int randomIndex = ThreadLocalRandom.current().nextInt(wordsList.size());
        guessedWord = wordsList.get(randomIndex);
        initHiddenArray();
    }

    public String getGuessedWord() {
        return guessedWord;
    }

    private void start() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket, this);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    private List<String> initWordsList() {
        List<String> wordsList = new ArrayList<>();
        wordsList.add("network");
        wordsList.add("iteration");
        wordsList.add("algorithm");
        wordsList.add("framework");
        wordsList.add("blockchain");
        wordsList.add("compilation");
        wordsList.add("cybersecurity");
        wordsList.add("multithreading");

        return wordsList;
    }

    private void initHiddenArray() {
        hiddenArray = new char[guessedWord.length()];

        for (int i = 0; i < hiddenArray.length; i++) {
            hiddenArray[i] = '*';
        }
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}