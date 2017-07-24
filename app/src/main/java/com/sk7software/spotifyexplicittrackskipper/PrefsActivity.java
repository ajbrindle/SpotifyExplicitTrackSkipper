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
    private Spinner spiHistory;
    private Switch swiKeepAlive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);

        swiKeepAlive = (Switch)findViewById(R.id.swiKeepAlive);

        spiHistory = (Spinner)findViewById(R.id.spiHistory);
        ArrayAdapter<CharSequence> historyAdapter = ArrayAdapter.createFromResource(this,
                R.array.history_items, android.R.layout.simple_spinner_item);
        historyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiHistory.setAdapter(historyAdapter);

        spiKeepAlive = (Spinner)findViewById(R.id.spiKeepAlive);
        ArrayAdapter<CharSequence> aliveAdapter = ArrayAdapter.createFromResource(this,
                R.array.keep_alive_intervals, android.R.layout.simple_spinner_item);
        aliveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiKeepAlive.setAdapter(aliveAdapter);

        Button btnClearImages = (Button)findViewById(R.id.btnClear);
        btnClearImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseUtil db = DatabaseUtil.getInstance(getApplicationContext());
                db.clearImageCache();
            }
        });

        Button btnClearHistory = (Button)findViewById(R.id.btnPurgeList);
        btnClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseUtil db = DatabaseUtil.getInstance(getApplicationContext());
                db.deleteAllTracks();
            }
        });

        Button btnDone = (Button)findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                savePreferences();
                finish();
            }
        });

        initPreferences(historyAdapter, aliveAdapter);

        swiKeepAlive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                spiKeepAlive.setEnabled(isChecked);
            }
        });
    }

    private void savePreferences() {
        int historyItems = Integer.valueOf(spiHistory.getSelectedItem().toString());
        PreferencesUtil.getInstance().addPreference(AppConstants.PREFERNECE_MAX_HISTORY_ITEMS, historyItems);

        boolean keepAlive = swiKeepAlive.isChecked();
        PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_KEEP_ALIVE, keepAlive);

        int keepAliveInterval = Integer.valueOf(spiKeepAlive.getSelectedItem().toString());
        PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_KEEP_ALIVE_INTERVAL, keepAliveInterval);

        SpotifyKeepAlive alarm = new SpotifyKeepAlive();
        alarm.cancelAlarm(getApplicationContext());

        if (keepAlive) {
            alarm.initialise(getApplicationContext(), keepAliveInterval);
        }
    }

    private void initPreferences(ArrayAdapter historyAdapter, ArrayAdapter aliveAdapter) {
        Integer historyItems = PreferencesUtil.getInstance().getIntPreference(AppConstants.PREFERNECE_MAX_HISTORY_ITEMS);
        int selectedPosition = historyAdapter.getPosition(historyItems.toString());
        if (selectedPosition < 0) {
            selectedPosition = 3;
        }
        spiHistory.setSelection(selectedPosition);

        swiKeepAlive.setChecked(PreferencesUtil.getInstance().getBooleanPreference(AppConstants.PREFERENCE_KEEP_ALIVE));
        if (!swiKeepAlive.isChecked()) {
            spiKeepAlive.setEnabled(false);
        }

        Integer keepAliveS = PreferencesUtil.getInstance().getIntPreference(AppConstants.PREFERENCE_KEEP_ALIVE_INTERVAL);
        selectedPosition = aliveAdapter.getPosition(keepAliveS.toString());
        if (selectedPosition < 0) {
            selectedPosition = 2;
        }
        spiKeepAlive.setSelection(selectedPosition);
    }
}
