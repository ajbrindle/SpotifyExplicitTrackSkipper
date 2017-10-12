package com.sk7software.spotifyexplicittrackskipper.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.sk7software.spotifyexplicittrackskipper.AppConstants;
import com.sk7software.spotifyexplicittrackskipper.TrackBroadcastReceiver;
import com.sk7software.spotifyexplicittrackskipper.model.Track;
import com.sk7software.spotifyexplicittrackskipper.util.PreferencesUtil;
import com.sk7software.spotifyexplicittrackskipper.R;
import com.sk7software.spotifyexplicittrackskipper.SpotifyKeepAlive;
import com.sk7software.spotifyexplicittrackskipper.TrackLookup;
import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener, View.OnClickListener, ActivityDataExchange {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView trackView;
    private GestureDetectorCompat gestureDetector;

    private Switch swiExplicit;
    private TrackAdapter trackAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private AdView mAdView;

    private BroadcastReceiver trackReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        final DatabaseUtil db = DatabaseUtil.getInstance(getApplicationContext());

        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);

        // Initialise context for preferences
        PreferencesUtil.init(getApplicationContext());

        swiExplicit = (Switch)findViewById(R.id.swiExplicitTracks);

        boolean isSet = PreferencesUtil.getInstance().getBooleanPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT);
        swiExplicit.setChecked(isSet);
        updateTrackService(true, isSet);

        swiExplicit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            PreferencesUtil.getInstance().addPreference(AppConstants.PREFERENCE_SKIP_EXPLICIT, isChecked);
                updateTrackService(true, isChecked);
            }
        });

        trackView = (RecyclerView)findViewById(R.id.listHistory);
        trackView.addOnItemTouchListener(this);
        trackAdapter = new TrackAdapter(LayoutInflater.from(this));
        trackAdapter.setDB(db);
        trackView.setLayoutManager(new LinearLayoutManager(this));
        trackView.setAdapter(trackAdapter);
        //showHistoryList();

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
        final BitmapFactory.Options options;
        final List<Drawable> backgrounds = new ArrayList<>();

        Resources res = this.getResources();
        options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap b = BitmapFactory.decodeResource(res, R.drawable.deletex, options);
        backgrounds.add(new BitmapDrawable(res, b));
        b = BitmapFactory.decodeResource(res, R.drawable.explicitx, options);
        backgrounds.add(new BitmapDrawable(res, b));
        b = BitmapFactory.decodeResource(res, R.drawable.notexplicitx, options);
        backgrounds.add(new BitmapDrawable(res, b));

        ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        // Determine swipe action
                        int swipeAction = PreferencesUtil.getInstance().getIntPreference(AppConstants.PREFERENCE_SWIPE_ACTION);
                        int adapterPosition = viewHolder.getAdapterPosition();

                        if (swipeAction == AppConstants.SWIPE_ACTION_DELETE) {
                            // Remove item from backing list here
                            db.deleteTrack(trackAdapter.getIdAtPosition(adapterPosition), trackAdapter.getPlayTimeAtPosition(adapterPosition));
                            trackAdapter.removeItem(adapterPosition);
                        } else if (swipeAction == AppConstants.SWIPE_ACTION_TAG) {
                            // Left = tag explicit
                            boolean isExplicit = swipeDir == ItemTouchHelper.LEFT;
                            db.tagTrack(trackAdapter.getIdAtPosition(adapterPosition), isExplicit);
                            trackAdapter.tagItem(adapterPosition, isExplicit, getApplicationContext());
                        }
                    }

                    public boolean onMove(RecyclerView view, RecyclerView.ViewHolder v1, RecyclerView.ViewHolder v2) {
                        final int fromPos = v1.getAdapterPosition();
                        final int toPos = v2.getAdapterPosition();
                        // move item in `fromPos` to `toPos` in adapter.
                        return true;
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        int background;
                        View itemView = viewHolder.itemView;
                        Drawable d;

                        // Get the right drawable
                        int swipeAction = PreferencesUtil.getInstance().getIntPreference(AppConstants.PREFERENCE_SWIPE_ACTION);
                        if (swipeAction == AppConstants.SWIPE_ACTION_DELETE) {
                            d = backgrounds.get(0);
                            background = Color.BLUE;
                        } else {
                            if (dX < 0) {
                                d = backgrounds.get(1);
                                background = Color.RED;
                            } else {
                                d = backgrounds.get(2);
                                background = Color.GREEN;
                            }
                        }
                        int wid = ((BitmapDrawable)d).getBitmap().getWidth();
                        int margin = calcMargin(((BitmapDrawable) d).getBitmap(), itemView.getTop(), itemView.getBottom());
                        if (margin == 0) {
                            wid = calcScaledWidth(((BitmapDrawable) d).getBitmap(), itemView.getTop(), itemView.getBottom());
                        }
                        d.setBounds(0, itemView.getTop()+margin, wid, itemView.getBottom()-margin);

                        Paint p = new Paint();
                        ClipDrawable cd = new ClipDrawable(d,
                                (dX > 0 ? Gravity.LEFT : Gravity.RIGHT), ClipDrawable.HORIZONTAL);
                        cd.setLevel((int) (Math.abs(dX) * 10000 / wid));
                        p.setColor(background);

                        if (dX > 0) {
                            // Draw Rect with varying right side, equal to displacement dX
                            c.drawRect((float)0, (float) itemView.getTop(), dX,
                                    (float) itemView.getBottom(), p);
                            cd.setBounds(0, itemView.getTop()+margin, wid, itemView.getBottom()-margin);
                        } else {
                            // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                            c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                    (float) itemView.getRight(), (float) itemView.getBottom(), p);
                            cd.setBounds(itemView.getRight()-wid, itemView.getTop()+margin, itemView.getRight(), itemView.getBottom()-margin);
                        }

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

    private int calcMargin(Bitmap b, int top, int bottom) {
        int bmpHt = b.getHeight();
        int viewHt = Math.abs(top - bottom);

        if (viewHt > bmpHt) {
            return (viewHt - bmpHt) / 2;
        }

        return 0;
    }

    private int calcScaledWidth(Bitmap b, int top, int bottom) {
        int bmpWid = b.getWidth();
        int bmpHt = b.getHeight();
        int viewHt = Math.abs(top - bottom);

        if (bmpHt > viewHt) {
            // Image will get scaled, so ensure the aspect ratio is preserved
            double scaleFactor = (double)viewHt/(double)bmpHt;
            bmpWid = (int)(bmpWid * scaleFactor);
        }

        return bmpWid;
    }

    @Override
    protected void onResume() {
        super.onResume();
        showHistoryList();
        setupReceiver();
    }

    private void setupReceiver() {
        IntentFilter intentFilter = new IntentFilter(AppConstants.APP_BROADCAST_INTENT);

        trackReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received broadcast intent: " + intent.getAction());
                updateActivity();
            }
        };
        registerReceiver(trackReceiver, intentFilter);
        Log.d(TAG, "Registered receiver");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(trackReceiver);
        Log.d(TAG, "Unregistered receiver");
    }

    @Override
    public void onClick(View view) {
        if (view == null) return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent i = new Intent(getApplicationContext(), PrefsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_quit:
                // Stop service
                updateTrackService(false, false);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void updateActivity() {
        final DatabaseUtil db = DatabaseUtil.getInstance(getApplicationContext());
        int limit = PreferencesUtil.getInstance().getIntPreference(AppConstants.PREFERNECE_MAX_HISTORY_ITEMS);
        final List<Track> tracksList = db.getTracks(limit);
        trackAdapter.updateTracks(tracksList);
        trackAdapter.notifyDataSetChanged();
        swipeRefresh.setRefreshing(false);
    }

    public void showHistoryList() {
        TrackLookup tl = new TrackLookup(getApplicationContext());
        tl.getTrackInfo();
    }

    private void myToggleSelection(int idx) {
        trackAdapter.toggleSelection(idx);
    }

    private void updateTrackService(boolean start, boolean skipExplicit) {
        Intent i = new Intent(getApplicationContext(), TrackBroadcastReceiver.class);
        if (start) {
            stopService(i);
            Log.d(TAG, "Track broadcast service started");
            i.putExtra("skipExplicit", skipExplicit);
            startService(i);
        } else {
            Log.d(TAG, "Track broadcast service stopped");
            stopService(i);
        }
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

    public static class Presenter {
        public void presentListItem(TrackAdapter.TrackViewHolder view, List<Track> tracks, int position) {
            view.bindData(tracks.get(position));
        }
    }

}
