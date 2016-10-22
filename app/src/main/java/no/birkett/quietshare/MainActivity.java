package no.birkett.quietshare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.nio.charset.Charset;

import org.quietmodem.Quiet.*;

public class MainActivity extends AppCompatActivity {

    private FrameReceiver receiver;
    private FrameTransmitter transmitter;
    private TextView receivedContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
        findViewById(R.id.receive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receive();
            }
        });
        receivedContent = (TextView) findViewById(R.id.received_content);
        setup();
    }

    private void setup() {
        FrameReceiverConfig receiverConfig;
        FrameTransmitterConfig transmitterConfig;
        try {
            transmitterConfig = new FrameTransmitterConfig(
                    this,
                    "audible-7k-channel-0");
            receiverConfig = new FrameReceiverConfig(
                    this,
                    "audible-7k-channel-0");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            receiver = new FrameReceiver(receiverConfig);
            transmitter = new FrameTransmitter(transmitterConfig);
        } catch (ModemException e) {
            throw new RuntimeException(e);
        }
    }

    public void receive() {
        // set receiver to block for at most 20 seconds
        receiver.setBlocking(5, 0);

        byte[] buf = new byte[1024];
        long recvLen = 0;
        try {
            recvLen = receiver.receive(buf);
            receivedContent.setText(new String(buf, Charset.forName("UTF-8")));
        } catch (IOException e) {
            // read timed out
        }
    }

    public void send() {
        String payload = "Hello, World!";
        try {
            transmitter.send(payload.getBytes());
        } catch (IOException e) {
            // our message might be too long or the transmit queue full
        }
    }
}
