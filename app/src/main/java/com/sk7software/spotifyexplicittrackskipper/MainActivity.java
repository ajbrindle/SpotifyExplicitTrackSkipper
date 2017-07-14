package com.sk7software.spotifyexplicittrackskipper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.list.TrackAdapter;
import com.sk7software.spotifyexplicittrackskipper.music.Track;

import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView trackView;
    //private List<Track> tracksList;
    private GestureDetectorCompat gestureDetector;
    private TrackAdapter trackAdapter;

    private Switch swiExplicit;
    private Button btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //this.deleteDatabase(DatabaseUtil.DATABASE_NAME);


        // Initialise context for preferences
        PreferencesUtil.setContext(getApplicationContext());

        swiExplicit = (Switch)findViewById(R.id.swiExplicitTracks);

        boolean isSet = PreferencesUtil.getBooleanPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT);
        swiExplicit.setChecked(isSet);

        swiExplicit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            PreferencesUtil.addPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT, isChecked);
            }
        });

        btnClear = (Button)findViewById(R.id.btnClear);

        showHistoryList();
        trackView.addOnItemTouchListener(this);
        gestureDetector =
                new GestureDetectorCompat(this, new TacksOnGestureListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showHistoryList();
    }

    @Override
    public void onClick(View view) {
        if (view == null) return;
        if (view.getId() == R.id.btnClear) {
            DatabaseUtil db = DatabaseUtil.getInstance(getApplicationContext());
            db.clearImageCache();
        }
    }

    public void showHistoryList() {
        DatabaseUtil tracksDB = DatabaseUtil.getInstance(getApplicationContext());
        List<Track> tracksList = tracksDB.getTracks(0);

        trackAdapter = new TrackAdapter(tracksList, tracksDB, LayoutInflater.from(this));

        trackView = (RecyclerView)findViewById(R.id.listHistory);
        trackView.setLayoutManager(new LinearLayoutManager(this));
        trackView.setAdapter(trackAdapter);
        trackAdapter.updateTracks(tracksList);
        trackAdapter.notifyDataSetChanged();
    }

    private void myToggleSelection(int idx) {
        trackAdapter.toggleSelection(idx);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
    private class TacksOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = trackView.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }
    }
}
