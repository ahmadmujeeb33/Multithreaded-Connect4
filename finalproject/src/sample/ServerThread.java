package sample;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


public class ServerThread extends Thread {
    private Socket socket;
    private ArrayList<ServerThread> threadList;
    private PrintWriter output;
    private char symb;

    public ServerThread(Socket socket, ArrayList<ServerThread> threads,char symbol) {
        this.socket = socket;
        this.threadList = threads;
        symb = symbol;
    }

    @Override
    public void run() {
        try {
            //Reading the input from Client
            BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream())); // reading from client

            //returning the output to the client : true statement is to flush the buffer otherwise
            //we have to do it manuallyy
            output = new PrintWriter(socket.getOutputStream(),true);    // writing to client
            int count = 0;


            //inifite loop for server
            while(true) {
                String outputString = input.readLine();
                //if user types exit command


                System.out.println("in here");
                count+=1;
                outputString = outputString + symb;
                printToALlClients(outputString);

                // r,cX
            }


        } catch (Exception e) {
            System.out.println("Error occured " +e.getStackTrace());
        }
    }

    private void printToALlClients(String outputString) {
        for( ServerThread sT: threadList) {
            sT.output.println(outputString);
        }

    }
}

