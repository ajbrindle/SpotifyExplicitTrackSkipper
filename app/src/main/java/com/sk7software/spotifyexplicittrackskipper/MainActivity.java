package com.sk7software.spotifyexplicittrackskipper;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private ToggleButton togExplicit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        togExplicit = (ToggleButton)findViewById(R.id.togExplicitTracks);

        boolean isSet = PreferencesUtil.getBooleanPreference(getApplicationContext(), AppConstants.PREFERENCE_SKIP_EXPLICIT);
        togExplicit.setChecked(isSet);

        togExplicit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            PreferencesUtil.addPreference(getApplicationContext(), AppConstants.PREFERENCE_SKIP_EXPLICIT, isChecked);
            }
        });
    }
}
