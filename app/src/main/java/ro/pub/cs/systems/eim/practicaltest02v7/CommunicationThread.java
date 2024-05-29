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

public class CommunicationThread extends Thread {
    private final ServerThread serverThread;
    private final Socket socket;
    private String result;
    private HashMap<String, AlarmInfo> activeAlarms;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null");
            return;
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            String action = bufferedReader.readLine();
            if (action == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Action is null");
                return;
            }
            Log.d(Constants.TAG, "[COMMUNICATION THREAD] Received action: " + action);

            if (action.equals(Constants.SET)) {
                Log.d(Constants.TAG, "[COMMUNICATION THREAD] Set action");

                try {
                    String hour = bufferedReader.readLine();
                    String minute = bufferedReader.readLine();
                    if (hour == null || minute == null) {
                        Log.e(Constants.TAG, "[COMMUNICATION THREAD] Hour or minute is null");
                        return;
                    }

                    AlarmInfo alarmInfo = new AlarmInfo(Integer.parseInt(hour), Integer.parseInt(minute));
                    serverThread.addAlarm(socket.getInetAddress().toString(), alarmInfo);

                    result = "Alarm set" + hour + ":" + minute;
                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] Alarm set: " + hour + ":" + minute);
                    printWriter.println(result);
                    printWriter.flush();
                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] Sent result: " + result);
                } catch (IOException e) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving information from client");
                }

            } else if (action.equals(Constants.RESET)) {
                serverThread.deleteAlarm(socket.getInetAddress().toString());
                result = "Alarm reset";
                Log.d(Constants.TAG, "[COMMUNICATION THREAD] Alarm reset");
                printWriter.println(result);
                printWriter.flush();
                Log.d(Constants.TAG, "[COMMUNICATION THREAD] Sent result: " + result);
            } else if (action.equals(Constants.POLL)) {
                Log.d(Constants.TAG, "[COMMUNICATION THREAD] Poll action");
                // make connection to utcnist.colorado.edu on port 13
                String host = "utcnist.colorado.edu";
                int port = 13;
                StringBuilder pageSourceCode = new StringBuilder();
                Log.d(Constants.TAG, "[COMMUNICATION THREAD] creating socket");
                try(Socket socket = new Socket(host, port);) {

                    BufferedReader buff = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] created socket");
                    String line;
                    while ((line = buff.readLine()) != null) {
                        pageSourceCode.append(line);
                    }

                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] received page source code");
                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] page source code: " + pageSourceCode.toString());

                    String[] tokens = pageSourceCode.toString().split(" ");

                    String time = tokens[2];
                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] time: " + time);
                    String[] timeTokens = time.split(":");
                    int serverHour = Integer.parseInt(timeTokens[0]);
                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] serverHour: " + serverHour);
                    int serverMinute = Integer.parseInt(timeTokens[1]);
                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] serverMinute: " + serverMinute);

                    AlarmInfo alarmInfo = serverThread.getAlarm(this.socket.getInetAddress().toString());
                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] Alarm info: " + alarmInfo);
                    if (alarmInfo == null) {
                        Log.e(Constants.TAG, "[COMMUNICATION THREAD] Alarm info is null");
                        result = "none";
                    } else {
                        if (alarmInfo.getHour() < serverHour) {
                            Log.d(Constants.TAG, "[COMMUNICATION THREAD] Alarm hour < server hour");
                            result = "active";
                        } else if (alarmInfo.getHour() == serverHour) {
                            if (alarmInfo.getMinute() < serverMinute) {
                                Log.d(Constants.TAG, "[COMMUNICATION THREAD] Alarm minute < server minute");
                                result = "active";
                            } else {
                                Log.d(Constants.TAG, "[COMMUNICATION THREAD] Alarm minute >= server minute");
                                result = "inactive";
                            }
                        } else {
                            Log.d(Constants.TAG, "[COMMUNICATION THREAD] Alarm hour > server hour");
                            result = "inactive";
                        }
                    }
                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] Poll result: " + result);
                    printWriter.println(result);
                    printWriter.flush();
                    Log.d(Constants.TAG, "[COMMUNICATION THREAD] Sent result: " + result);
                }  catch (IOException e) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving information from client");
                }

            } else {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Invalid action");
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving information from client");
        }
    }

}
