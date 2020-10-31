package pl.musicplayer.view.songs;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import java.util.Comparator;

import butterknife.ButterKnife;
import pl.musicplayer.MediaPlaybackService;
import pl.musicplayer.R;
import pl.musicplayer.data.Song;
import pl.musicplayer.ui.CustomTouchListener;
import pl.musicplayer.utils.StorageUtil;
import pl.musicplayer.view.base.BaseFragment;

public class SongsFragment extends BaseFragment {

    private RecyclerView.Adapter songsAdapter;
    private RecyclerView.LayoutManager layoutManager;


    private final static String CHANNEL_ID = "106";

    private void createNotificationChannel() {
        CharSequence name = "channel_name";
        String description = "channel_description";
        int importance = NotificationManager.IMPORTANCE_MIN;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        createNotificationChannel();

        layoutManager = new LinearLayoutManager(getActivity());

        listOfSongs = new ArrayList<>();
        getSongList();

        listOfSongs.sort(new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getSongTitle().compareTo(b.getSongTitle());
            }
        });
        songsAdapter = new SongsAdapter(listOfSongs);
        initRecyclerView();

        Intent service = new Intent(getContext(), MediaPlaybackService.class);
        getContext().bindService(service, musicConnection, Service.BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_songs, container, false);
    }

    private void initRecyclerView() {

        if (listOfSongs.size() > 0) {
            songView.setAdapter(songsAdapter);
            songView.setLayoutManager(layoutManager);
            songView.addOnItemTouchListener(new CustomTouchListener(this.getContext(), (view, index) -> {
                StorageUtil storageUtil = new StorageUtil(getContext());
                storageUtil.setSongIndex(index);
                musicService.setSong(index);
                musicService.playSong();
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                songTitle.setText(listOfSongs.get(index).getSongTitle());
                songTitle.setSelected(true);
                songArtist.setText(listOfSongs.get(index).getSongArtist());
                musicNavigation.setVisibility(View.VISIBLE);
                //controller.show(musicService.getSongDuration());
            }));


        }
    }

}
