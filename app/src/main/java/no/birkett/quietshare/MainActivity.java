package no.birkett.quietshare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.quietmodem.Quiet.*;

public class MainActivity extends AppCompatActivity {

    private FrameReceiver receiver;
    private FrameTransmitter transmitter;
    private TextView receivedContent;
    private EditText sendMessage;
    private Spinner profileSpinner;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private TextView receiveStatus;
    
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendClick();
            }
        });
        findViewById(R.id.receive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleReceiveClick();
            }
        });
        receivedContent = (TextView) findViewById(R.id.received_content);
        sendMessage = (EditText) findViewById(R.id.send_message);
        profileSpinner = (Spinner) findViewById(R.id.profile);
        receiveStatus = (TextView) findViewById(R.id.receive_status);
        setupProfileSpinner();
        setupTransmitter();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupReceiver();
                    receive();
                } else {
                    showMissingAudioPermissionToast();
                }
            }
        }
    }

    private void setupTransmitter() {
        FrameTransmitterConfig transmitterConfig;
        try {
            transmitterConfig = new FrameTransmitterConfig(
                    this,getProfile());

            transmitter = new FrameTransmitter(transmitterConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ModemException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupReceiver() {
        try {
            FrameReceiverConfig receiverConfig = new FrameReceiverConfig(this, getProfile());
            receiver = new FrameReceiver(receiverConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ModemException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleReceiveClick() {
        if (receiver == null) {
            if (hasRecordAudioPersmission()) {
                setupReceiver();
            } else {
                requestPermission();
            }
        } else {
            receive();
        }
    }

    private void handleSendClick() {
        if (transmitter == null) {
            setupTransmitter();
        }
        send();
    }

    private void receive() {
        receiver.setBlocking(5, 0);

        byte[] buf = new byte[1024];
        long recvLen = 0;
        try {
            recvLen = receiver.receive(buf);
            receivedContent.setText(new String(buf, Charset.forName("UTF-8")));
            receiveStatus.setText("Received " + recvLen);
        } catch (IOException e) {
            receiveStatus.setText(e.toString());
        }

    }
    private void send() {
        String payload = sendMessage.getText().toString();
        try {
            transmitter.send(payload.getBytes());
        } catch (IOException e) {
            // our message might be too long or the transmit queue full
        }
    }

    boolean hasRecordAudioPersmission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
    }


    private void showMissingAudioPermissionToast() {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, R.string.missing_audio_permission, duration);
        toast.show();
    }

    private ArrayList<String> getProfiles() {
        ArrayList<String> profiles = new ArrayList<>();
        try {
            String json = FrameTransmitterConfig.getDefaultProfiles(this);
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> iterator = jsonObject.keys();

            while(iterator.hasNext()) {
                profiles.add(iterator.next());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return profiles;
    }

    private void setupProfileSpinner() {
        final ArrayList<String> profiles = getProfiles();
        spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, profiles);
        profileSpinner.setAdapter(spinnerArrayAdapter);

        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                transmitter = null;
                receiver = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                transmitter = null;
                receiver = null;
            }
        });

    }

    private String getProfile() {
        String profile = spinnerArrayAdapter.getItem(profileSpinner.getSelectedItemPosition());
        return profile;
    }
 }
