package sample;

import sample.ServerThread;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMain {

    public static void main(String[] args) {
        //using serversocket as argument to automatically close the socket
        //the port number is unique for each server

        //list to add all the clients thread
        ArrayList<ServerThread> threadList = new ArrayList<>();
        int count = 0;
        try (ServerSocket serversocket = new ServerSocket(8080)){
            while(true) {
                Socket socket = serversocket.accept();
                if(count %2 == 0){
                    count+=1;
                    ServerThread serverThread = new ServerThread(socket, threadList,'X');
                    threadList.add(serverThread);
                    serverThread.start();
                }
                else{
                    count+=1;
                    ServerThread serverThread = new ServerThread(socket, threadList,'O');
                    threadList.add(serverThread);
                    serverThread.start();
                }

                System.out.println("in this one");
                //starting the thread



                //get all the list of currently running thread

            }
        } catch (Exception e) {
//            System.out.println("Error occured in main: " + e.getStackTrace());
            e.printStackTrace();
        }
    }
}
