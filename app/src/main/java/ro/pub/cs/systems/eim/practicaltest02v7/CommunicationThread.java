package ro.pub.cs.systems.eim.practicaltest02v7;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpCookie;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class CommunicationThread extends Thread {
    private final ServerThread serverThread;
    private final Socket socket;
    private String action;
    private String result;
    private HashMap<String, AlarmInfo> activeAlarms;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            return;
        }

        if (action == null) {
            return;
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            action = bufferedReader.readLine();

            activeAlarms = serverThread.getActiveAlarms();

            switch (action) {
                case Constants.SET:
                    performSetAction(bufferedReader, printWriter);
                    break;
                case Constants.RESET:
                    performResetAction();
                    break;
                case Constants.POLL:
                    performPollAction();
                    break;
            }

            printWriter.println(result);
            printWriter.flush();
            Log.d(Constants.TAG, "[COMMUNICATION THREAD] Sent result: " + result);
        } catch (IOException e) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving information from client");
        }
    }

    private void performSetAction(BufferedReader bufferedReader, PrintWriter printWriter) {
        String hour = null;
        String minute = null;

        try {
            hour = bufferedReader.readLine();
            minute = bufferedReader.readLine();
        } catch (IOException e) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving information from client");
        }

        if (hour == null || minute == null) {
            return;
        }

        AlarmInfo alarmInfo = new AlarmInfo(Integer.parseInt(hour), Integer.parseInt(minute));
        serverThread.addAlarm(socket.getInetAddress().toString(), alarmInfo);

        result = "Alarm set";
    }

    private void performResetAction() {
        serverThread.deleteAlarm(socket.getInetAddress().toString());
        result = "Alarm reset";
    }

    private void performPollAction() {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String pageSourceCode = "";
            URI address = new URI("http", null, "utcnist.colorado.edu", 13, null, null, null);
            HttpGet httpGet = new HttpGet(address);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity= httpResponse.getEntity();
            if (httpEntity != null) {
                pageSourceCode = EntityUtils.toString(httpEntity);
            }

            String[] tokens = pageSourceCode.split(" ");
            String time = tokens[2];
            String[] timeTokens = time.split(":");
            int serverHour = Integer.parseInt(timeTokens[0]);
            int serverMinute = Integer.parseInt(timeTokens[1]);

            AlarmInfo alarmInfo = activeAlarms.get(socket.getInetAddress().toString());
            if (alarmInfo == null) {
                result = "none";
            } else {
                if (alarmInfo.getHour() < serverHour) {
                    result = "active";
                } else if (alarmInfo.getHour() == serverHour) {
                    if (alarmInfo.getMinute() < serverMinute) {
                        result = "active";
                    } else {
                        result = "inactive";
                    }
                } else {
                    result = "inactive";
                }
            }

        } catch (URISyntaxException e) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error creating URI");
        } catch (IOException e) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the time from the server");
        }

    }


}
