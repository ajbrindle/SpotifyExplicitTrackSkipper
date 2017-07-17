package com.sk7software.spotifyexplicittrackskipper.list;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.sk7software.spotifyexplicittrackskipper.AppConstants;
import com.sk7software.spotifyexplicittrackskipper.R;
import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.music.Track;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Created by andre_000 on 13/07/2017.
 */

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<Track> tracks;
    private LayoutInflater inflater;
    private SparseBooleanArray selectedItems;
    private DatabaseUtil db;

    private static final SimpleDateFormat PLAY_TIME_FORMAT = new SimpleDateFormat(AppConstants.PLAY_TIME_DISPLAY_FORMAT);
    private static final String TAG = TrackAdapter.class.getSimpleName();

    public TrackAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
        this.selectedItems = new SparseBooleanArray();
    }

    public TrackAdapter(List<Track> tracks, DatabaseUtil db, LayoutInflater inflater) {
        this.tracks = tracks;
        this.db = db;
        this.inflater = inflater;
        this.selectedItems = new SparseBooleanArray();
    }

    public void updateTracks(List<Track> tracks) {
        this.tracks = tracks;
//        if (tracks != null) {
//            for (int i = 0; i < tracks.size(); i++) {
//                selectedItems.put(i, true);
//            }
//        }
    }

    public void setDB(DatabaseUtil db) {
        this.db = db;
    }

    public void deselectTrack(String id) {
        for (int i = 0; i< tracks.size(); i++) {
            Track t = tracks.get(i);
            if (id.equals(t.getSpotifyId())) {
                // Deselect this item
                selectedItems.delete(i);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public String getIdAtPosition(int position) {
        return tracks.get(position).getSpotifyId();
    }

    public Date getPlayTimeAtPosition(int position) {
        return tracks.get(position).getPlayTime();
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = inflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new TrackViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        holder.bindData(tracks.get(position));
        holder.itemView.setActivated(selectedItems.get(position, false));
    }

    @Override
    public int getItemCount() {
        if (tracks != null) {
            return tracks.size();
        } else {
            return 0;
        }
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void removeItem(int position) {
        tracks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tracks.size());
        notifyDataSetChanged();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView artistAlbum;
        final TextView playTime;
        final ImageView albumImg;
        final TextView id;

        public TrackViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.txtSongTitle);
            artistAlbum = (TextView)itemView.findViewById(R.id.txtArtistAlbum);
            playTime = (TextView)itemView.findViewById(R.id.txtPlayTime);
            albumImg = (ImageView)itemView.findViewById(R.id.imgAlbumArt);
            id = (TextView)itemView.findViewById(R.id.txtSpotifyId);
        }

        public void bindData(Track t) {
            title.setText(t.getTitle());
            if (t.isExplicit()) {
                // Set explicit tracks to red
                title.setTextColor(Color.RED);
            } else {
                // Use default colour of another text field (that won't have been changed)
                title.setTextColor(playTime.getTextColors().getDefaultColor());
            }
            artistAlbum.setText(t.getArtist() + " / " + t.getAlbum());
            playTime.setText(PLAY_TIME_FORMAT.format(t.getPlayTime()) + (t.isSkipped() ? "  [skipped]" : ""));
            id.setText(t.getSpotifyId());

            if (db.imageExists(t.getSpotifyId())) {
                Log.d(TAG, "Image for " + t.getTitle() + " from DB");
                albumImg.setImageBitmap(db.retrieveAlbumArt(t.getSpotifyId()));
            } else {
                Log.d(TAG, "Image for " + t.getTitle() + " from URL");
                new ImageLoadTask(t.getImageURL(), t.getSpotifyId(), db, albumImg).execute();
            }
        }
    }
}
