package com.sk7software.spotifyexplicittrackskipper.ui;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk7software.spotifyexplicittrackskipper.AppConstants;
import com.sk7software.spotifyexplicittrackskipper.R;
import com.sk7software.spotifyexplicittrackskipper.db.DatabaseUtil;
import com.sk7software.spotifyexplicittrackskipper.model.Track;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            if (id.equals(t.getId())) {
                // Deselect this item
                selectedItems.delete(i);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public String getIdAtPosition(int position) {
        return tracks.get(position).getId();
    }

    public Date getPlayTimeAtPosition(int position) {
        return tracks.get(position).getPlayDate();
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = inflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        rowView.setTag(new MainActivity.Presenter());
        return new TrackViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        ((MainActivity.Presenter)holder.itemView.getTag()).presentListItem(holder, tracks, position);
//        holder.bindData(tracks.get(position));
//        holder.itemView.setActivated(selectedItems.get(position, false));
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

        public String getId() {
            return id.getText().toString();
        }

        public TrackViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.txtSongTitle);
            artistAlbum = (TextView)itemView.findViewById(R.id.txtArtistAlbum);
            playTime = (TextView)itemView.findViewById(R.id.txtPlayTime);
            albumImg = (ImageView)itemView.findViewById(R.id.imgAlbumArt);
            id = (TextView)itemView.findViewById(R.id.txtSpotifyId);
        }

        public void bindData(Track t) {
            title.setText(t.getName());
            if (t.isExplicit()) {
                // Set explicit tracks to red
                title.setTextColor(Color.RED);
            } else {
                // Use default colour of another text field (that won't have been changed)
                title.setTextColor(playTime.getTextColors().getDefaultColor());
            }
            artistAlbum.setText(t.getArtistName() + " / " + t.getAlbumName());
            playTime.setText(PLAY_TIME_FORMAT.format(t.getPlayDate()) + (t.isSkipped() ? "  [skipped]" : ""));
            id.setText(t.getId());

            if (db.imageExists(t.getId())) {
                // Load image from database
                albumImg.setImageBitmap(db.retrieveAlbumArt(t.getId()));
            } else {
                // Load image from URL
                new ImageLoadTask(t.getAlbumArt(), t.getId(), db, albumImg).execute();
            }
        }
    }

}
