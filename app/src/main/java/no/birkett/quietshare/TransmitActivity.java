package no.birkett.quietshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.quietmodem.Quiet.FrameTransmitter;
import org.quietmodem.Quiet.FrameTransmitterConfig;
import org.quietmodem.Quiet.ModemException;

import java.io.IOException;

public class TransmitActivity extends AppCompatActivity {

    private FrameTransmitter transmitter;
    private EditText sendMessage;
    private Spinner profileSpinner;
    private ArrayAdapter<String> spinnerArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmit);
        findViewById(R.id.transmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendClick();
            }
        });

        sendMessage = (EditText) findViewById(R.id.message);
        profileSpinner = (Spinner) findViewById(R.id.profile);
        setupProfileSpinner();
        setupTransmitter();
        handleDataFromIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (transmitter != null) {
            transmitter.close();
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

    private void handleSendClick() {
        if (transmitter == null) {
            setupTransmitter();
        }
        send();
    }

    private void send() {
        String payload = sendMessage.getText().toString();
        try {
            transmitter.send(payload.getBytes());
        } catch (IOException e) {
            // our message might be too long or the transmit queue full
        }
    }

    private void setupProfileSpinner() {
        spinnerArrayAdapter = ProfilesHelper.createArrayAdapter(this);
        profileSpinner.setAdapter(spinnerArrayAdapter);
        profileSpinner.setSelection(0, false);
        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                transmitter = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private String getProfile() {
        String profile = spinnerArrayAdapter.getItem(profileSpinner.getSelectedItemPosition());
        return profile;
    }

    private void handleDataFromIntent() {

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                sendMessage.setText(sharedText);
            }
        }

    }
}
