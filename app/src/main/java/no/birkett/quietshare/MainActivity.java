package no.birkett.quietshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.transmit).setOnClickListener(v -> {
            startActivity(new Intent(this, TransmitActivity.class));
        });

        findViewById(R.id.receive).setOnClickListener(v -> {
            startActivity(new Intent(this, ReceiveActivity.class));
        });
    }

}
