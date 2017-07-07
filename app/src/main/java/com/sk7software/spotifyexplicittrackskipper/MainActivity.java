package com.sk7software.spotifyexplicittrackskipper;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private Switch swiExplicit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swiExplicit = (Switch)findViewById(R.id.swiExplicitTracks);

        boolean isSet = PreferencesUtil.getBooleanPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT);
        swiExplicit.setChecked(isSet);

        swiExplicit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            PreferencesUtil.addPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT, isChecked);
            }
        });
    }
}
