package ro.pub.cs.systems.eim.practicaltest02v7;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicaltest02v7.R;

public class PracticalTest02v7MainActivity extends AppCompatActivity {

    private EditText serverPortEditText = null;
    private EditText clientAddressEditText = null;
    private EditText clientPortEditText = null;
    private EditText hourEditText = null;
    private EditText minuteEditText = null;
    private TextView alarmFeedbackTextView = null;
    private ServerThread serverThread = null;

    private final ConnectClickButtonListener connectClickButtonListener = new ConnectClickButtonListener();

    private class ConnectClickButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String serverPort = serverPortEditText.getText().toString();
            if (serverPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Server port should be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            serverThread = new ServerThread(Integer.parseInt(serverPort));
            if (serverThread.getServerSocket() == null) {
                return;
            }

            serverThread.start();
        }
    }

    private final SetAlarmButtonClickListener setAlarmButtonClickListener = new SetAlarmButtonClickListener();
    private class SetAlarmButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Log.d(Constants.TAG, "onClick set");
            String clientAddress = clientAddressEditText.getText().toString();
            String clientPort = clientPortEditText.getText().toString();
            String hour = hourEditText.getText().toString();
            String minute = minuteEditText.getText().toString();

            Log.d(Constants.TAG, "set parameters");

            if (clientAddress.isEmpty() || clientPort.isEmpty() || hour.isEmpty() || minute.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client connection parameters should be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            alarmFeedbackTextView.setText("");

            Log.d(Constants.TAG, "set parameters");

            ClientThread clientThread = new ClientThread(
                    clientAddress, Integer.parseInt(clientPort), hour, minute, Constants.SET, alarmFeedbackTextView
            );
            clientThread.start();
        }
    }

    private final ResetAlarmButtonClickListener resetAlarmButtonClickListener = new ResetAlarmButtonClickListener();
    private class ResetAlarmButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String clientAddress = clientAddressEditText.getText().toString();
            String clientPort = clientPortEditText.getText().toString();

            alarmFeedbackTextView.setText("");

            ClientThread clientThread = new ClientThread(
                    clientAddress, Integer.parseInt(clientPort), null, null, Constants.RESET, alarmFeedbackTextView
            );
            clientThread.start();
        }
    }

    private final PollAlarmButtonClickListener pollAlarmButtonClickListener = new PollAlarmButtonClickListener();
    private class PollAlarmButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String clientAddress = clientAddressEditText.getText().toString();
            String clientPort = clientPortEditText.getText().toString();

            alarmFeedbackTextView.setText("");

            ClientThread clientThread = new ClientThread(
                    clientAddress, Integer.parseInt(clientPort), null, null, Constants.POLL, alarmFeedbackTextView
            );
            clientThread.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02v7_main);
        serverPortEditText = findViewById(R.id.server_port_edit_text);
        clientAddressEditText = findViewById(R.id.client_address_edit_text);
        clientPortEditText = findViewById(R.id.client_port_edit_text);
        hourEditText = findViewById(R.id.hour_edit_text);
        minuteEditText = findViewById(R.id.minute_edit_text);
        alarmFeedbackTextView = findViewById(R.id.alarm_feedback_text_view);
        Log.d(Constants.TAG, "onCreate: " + hourEditText);

        Button connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(connectClickButtonListener);

        Button setAlarmButton = findViewById(R.id.set_alarm_button);
        setAlarmButton.setOnClickListener(setAlarmButtonClickListener);

        Button resetAlarmButton = findViewById(R.id.reset_alarm_button);
        resetAlarmButton.setOnClickListener(resetAlarmButtonClickListener);

        Button pollAlarmButton = findViewById(R.id.poll_alarm_button);
        pollAlarmButton.setOnClickListener(pollAlarmButtonClickListener);
    }

    @Override
    protected void onDestroy() {
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}
