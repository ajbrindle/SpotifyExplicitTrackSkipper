package com.sk7software.spotifyexplicittrackskipper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;

public class PrefsActivity extends AppCompatActivity {

    private Spinner spiKeepAlive;
    private EditText txtHistory;
    private Switch swiKeepAlive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);

        txtHistory = (EditText)findViewById(R.id.txtHistoryMax);

        swiKeepAlive = (Switch)findViewById(R.id.swiKeepAlive);

        spiKeepAlive = (Spinner)findViewById(R.id.spiKeepAlive);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.keep_alive_intervals, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiKeepAlive.setAdapter(adapter);

        Button btnClearImages = (Button)findViewById(R.id.btnClear);
        btnClearImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseUtil db = DatabaseUtil.getInstance(getApplicationContext());
                db.clearImageCache();
            }
        });

        Button btnDone = (Button)findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                savePreferences();
                finish();
            }
        });

        initPreferences(adapter);

        swiKeepAlive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                spiKeepAlive.setEnabled(isChecked);
            }
        });
    }

    private void savePreferences() {
        PreferencesUtil.addPreference(AppConstants.PREFERNECE_MAX_HISTORY_ITEMS, txtHistory.getText().toString());

        boolean keepAlive = swiKeepAlive.isChecked();
        PreferencesUtil.addPreference(AppConstants.PREFERENCE_KEEP_ALIVE, keepAlive);

        int keepAliveInterval = Integer.valueOf(spiKeepAlive.getSelectedItem().toString());
        PreferencesUtil.addPreference(AppConstants.PREFERENCE_KEEP_ALIVE_INTERVAL, keepAliveInterval);

        SpotifyKeepAlive alarm = new SpotifyKeepAlive();
        alarm.cancelAlarm(getApplicationContext());

        if (keepAlive) {
            alarm.initialise(getApplicationContext(), keepAliveInterval);
        }
    }

    private void initPreferences(ArrayAdapter adapter) {
        txtHistory.setText(PreferencesUtil.getStringPreference(AppConstants.PREFERNECE_MAX_HISTORY_ITEMS));
        swiKeepAlive.setChecked(PreferencesUtil.getBooleanPreference(AppConstants.PREFERENCE_KEEP_ALIVE));
        if (!swiKeepAlive.isChecked()) {
            spiKeepAlive.setEnabled(false);
        }

        Integer keepAliveS = PreferencesUtil.getIntPreference(AppConstants.PREFERENCE_KEEP_ALIVE_INTERVAL);
        int selectedPosition = adapter.getPosition(keepAliveS.toString());
        if (selectedPosition < 0) {
            selectedPosition = 2;
        }
        spiKeepAlive.setSelection(selectedPosition);
    }
}
