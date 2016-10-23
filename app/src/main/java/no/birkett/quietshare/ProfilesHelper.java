package no.birkett.quietshare;


import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;
import org.quietmodem.Quiet.FrameTransmitterConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ProfilesHelper {

    public static ArrayList<String> getProfiles(Context context) {
        ArrayList<String> profiles = new ArrayList<>();
        try {
            String json = FrameTransmitterConfig.getDefaultProfiles(context);
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

    public static ArrayAdapter createArrayAdapter(Context context) {
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, getProfiles(context));
        return arrayAdapter;
    }

}
