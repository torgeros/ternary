package torgeros.connect3;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import torgeros.connect3.Board;
import torgeros.connect3.ConnectThree.PlayerColor;

public class GameClient {
    private final String gamename;
    private final String color;

    private Socket socket;
    private InputStream istream;
    private BufferedReader reader;
    private OutputStream ostream;
    private BufferedWriter writer;

    private final String HOSTNAME = "156trlinux-1.ece.mcgill.ca";
    private final int PORT = 12345;

    public GameClient(String gamename, String color) {
        this.gamename = gamename;
        this.color = color;
        
        try {
            socket = new Socket(HOSTNAME, PORT);
            istream = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(istream));
            ostream = socket.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(ostream));
        } catch (UnknownHostException ex) {
            System.err.println("hostname is wrong, halting");
            while (true);
        } catch (IOException ex) {
            System.err.println("could not connect, halting");
            while (true);
        }
    }

    public void connect() {
        String gamestring = gamename + " " + color;
        writeLine(gamestring);
        if (!readAndVerify(gamestring)) {
            System.err.println("DID NOT RETURN CORRECT GAMESTRING! HALTING");
            while(true);
        }
        System.out.println("connected to game server");
    }

    public void makeMove(String move) {
        writeLine(move);
        readAndVerify(move);
    }

    public String getOpponentMove() {
        // game is considered to be connected
        try {
            String input = reader.readLine();
            if (input.startsWith("Timeout")) {
                System.err.println("Server timeout trigerred, halting.");
                while (true);
            }
            return input;
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("halting");
            while (true);
        }
    }

    private void writeLine(String line) {
        try {
            writer.write(line + "\n");
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("halting");
            while (true);
        }
    }

    private boolean readAndVerify(String correct) {
        try {
            String iLine = reader.readLine();
            //System.out.println(iLine);
            return correct.equals(iLine);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("halting");
            while (true);
        }
    }
}
