package ro.pub.cs.systems.eim.practicaltest02v7;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private final String address;
    private final int port;
    private final String action;
    private final String hour;
    private final String minute;
    private final TextView alarmFeedbackTextView;
    private Socket socket;

    public ClientThread(String address, int port, String action, String hour, String minute, TextView alarmFeedbackTextView) {
        this.address = address;
        this.port = port;
        this.action = action;
        this.hour = hour;
        this.minute = minute;
        this.alarmFeedbackTextView = alarmFeedbackTextView;
    }

    @Override
    public void run() {
        try {
            Log.d(Constants.TAG, "[CLIENT THREAD] Starting client thread on port " + port);
            socket = new Socket(address, port);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            printWriter.println(action);
            Log.d(Constants.TAG, "[CLIENT THREAD] Sent action: " + action);
            printWriter.flush();
            printWriter.println(hour);
            Log.d(Constants.TAG, "[CLIENT THREAD] Sent hour: " + hour);
            printWriter.flush();
            printWriter.println(minute);
            Log.d(Constants.TAG, "[CLIENT THREAD] Sent minute: " + minute);
            printWriter.flush();

            String result;
            while ((result = bufferedReader.readLine()) != null) {
                Log.v(Constants.TAG, "Client received: " + result);
                final String finalizedResult = result;
                alarmFeedbackTextView.post(() -> alarmFeedbackTextView.setText(finalizedResult));
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + e.getMessage());
                }
            }
        }
    }
}
