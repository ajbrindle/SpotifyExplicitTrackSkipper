package com.sk7software.spotifyexplicittrackskipper.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.sk7software.spotifyexplicittrackskipper.AppConstants;
import com.sk7software.spotifyexplicittrackskipper.util.PreferencesUtil;
import com.sk7software.spotifyexplicittrackskipper.R;
import com.sk7software.spotifyexplicittrackskipper.SpotifyKeepAlive;
import com.sk7software.spotifyexplicittrackskipper.TrackLookup;
import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;

public class MainActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView trackView;
    private GestureDetectorCompat gestureDetector;

    private Switch swiExplicit;
    private TrackAdapter trackAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);

        // Initialise context for preferences
        PreferencesUtil.init(getApplicationContext());

        swiExplicit = (Switch)findViewById(R.id.swiExplicitTracks);

        boolean isSet = PreferencesUtil.getInstance().getBooleanPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT);
        swiExplicit.setChecked(isSet);

        swiExplicit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT, isChecked);
            }
        });

        trackView = (RecyclerView)findViewById(R.id.listHistory);
        trackView.addOnItemTouchListener(this);
        trackAdapter = new TrackAdapter(LayoutInflater.from(this));
        showHistoryList();

        gestureDetector =
                new GestureDetectorCompat(this, new TacksOnGestureListener());

        swipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.d(TAG, "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        showHistoryList();
                    }
                }
        );

        // Initialise ads
        MobileAds.initialize(this, AppConstants.ADMOB_APP_ID);
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("E54B1FD6DA8E4366B1A1621B72868A5B")
                .build();
        mAdView.loadAd(adRequest);

        // Set the item touch helper to define list item behaviour on swipe
        final DatabaseUtil db = DatabaseUtil.getInstance(getApplicationContext());
        final BitmapFactory.Options options;
        final Drawable d;

        Resources res = this.getResources();
        options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap b = BitmapFactory.decodeResource(res, R.drawable.ic_explicit_background, options);
        d = new BitmapDrawable(res, b);

        ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        // Remove item from backing list here
                        int adapterPosition = viewHolder.getAdapterPosition();
                        db.deleteTrack(trackAdapter.getIdAtPosition(adapterPosition), trackAdapter.getPlayTimeAtPosition(adapterPosition));
                        trackAdapter.removeItem(adapterPosition);
                    }

                    public boolean onMove(RecyclerView view, RecyclerView.ViewHolder v1, RecyclerView.ViewHolder v2) {
                        final int fromPos = v1.getAdapterPosition();
                        final int toPos = v2.getAdapterPosition();
                        // move item in `fromPos` to `toPos` in adapter.
                        return true;
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        final ColorDrawable background = new ColorDrawable(Color.RED);
                        View itemView = viewHolder.itemView;
                        d.setBounds(0, itemView.getTop(), itemView.getRight(), itemView.getBottom());

                        ClipDrawable cd = new ClipDrawable(d,
                                (dX > 0 ? Gravity.LEFT : Gravity.RIGHT), ClipDrawable.HORIZONTAL);
                        cd.setLevel((int) (Math.abs(dX) * 10000 / itemView.getRight()));
                        cd.setBounds(0, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        cd.draw(c);

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                });

        itemTouchHelper.attachToRecyclerView(trackView);

        if (PreferencesUtil.getInstance().getBooleanPreference(AppConstants.PREFERENCE_KEEP_ALIVE)) {
            int interval = PreferencesUtil.getInstance().getIntPreference(AppConstants.PREFERENCE_KEEP_ALIVE_INTERVAL);
            SpotifyKeepAlive alarm = new SpotifyKeepAlive();
            alarm.initialise(getApplicationContext(), (interval > 0 ? interval : 90));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showHistoryList();
    }

    @Override
    public void onClick(View view) {
        if (view == null) return;
        if (view.getId() == R.id.txtAdvanced) {
            Intent i = new Intent(getApplicationContext(), PrefsActivity.class);
            startActivity(i);
        }
    }

    public void showHistoryList() {
        TrackLookup tl = new TrackLookup(getApplicationContext());
        tl.getTrackInfo(this, trackView, trackAdapter, swipeRefresh);
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
