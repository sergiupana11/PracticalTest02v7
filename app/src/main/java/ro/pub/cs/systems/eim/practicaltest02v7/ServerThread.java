package ro.pub.cs.systems.eim.practicaltest02v7;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class ServerThread extends Thread {
    private Action action;
    private ServerSocket serverSocket;

    private final HashMap<String, AlarmInfo> activeAlarms;

    public ServerThread(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            Log.e("ServerThread", "An exception has occurred: " + ioException.getMessage());
        }
        this.activeAlarms = new HashMap<>();
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public HashMap<String, AlarmInfo> getActiveAlarms() {
        return activeAlarms;
    }

    public synchronized void addAlarm(String clientIp, AlarmInfo alarmInfo) {
        activeAlarms.put(clientIp, alarmInfo);
    }

    public synchronized void deleteAlarm(String clientIp) {
        activeAlarms.remove(clientIp);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation");
                CommunicationThread communicationThread = new CommunicationThread(this, serverSocket.accept());
                communicationThread.start();
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + e.getMessage());
        }
    }

    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + e.getMessage());
            }
        }
    }
}
