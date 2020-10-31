package pl.musicplayer.view.songs;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.musicplayer.R;
import pl.musicplayer.data.Song;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {

    private final List<Song> songs = new ArrayList<>();
    public SongsAdapter(List<Song> songs) {
        this.songs.addAll(songs);
    }


    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song,parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.bind(songs.get(position));
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.song_title)
        TextView title;

        @BindView(R.id.song_artist)
        TextView artist;

        private long songId;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void bind(final Song song){
            songId = song.getSongId();
            title.setText(song.getSongTitle());
            artist.setText(song.getSongArtist());
        }

    }
}
