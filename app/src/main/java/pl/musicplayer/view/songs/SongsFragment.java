package pl.musicplayer.view.songs;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.musicplayer.MediaPlaybackService;
import pl.musicplayer.R;
import pl.musicplayer.data.Song;
import pl.musicplayer.MediaPlaybackService.MusicBinder;
import pl.musicplayer.navigation.Navigator;
import pl.musicplayer.ui.CustomTouchListener;
import pl.musicplayer.ui.OnItemClickListener;
import pl.musicplayer.utils.StorageUtil;

public class SongsFragment extends Fragment {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.song_list)
    RecyclerView songView;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.music_player_navigation_id)
    LinearLayout musicNavigation;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.music_progress_bar)
    ProgressBar progressBar;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.button_play)
    ImageView playButton;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.button_pause)
    ImageView pauseButton;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.button_empty_heart)
    ImageView emptyHeartButton;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.button_heart)
    ImageView heartButton;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.song_title)
    TextView songTitle;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.song_artist)
    TextView songArtist;

    private List<Song> listOfSongs;
    private RecyclerView.Adapter songsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private MediaPlaybackService musicService;
    private static Intent playIntent;
    private boolean musicBound = false;

    private final static String CHANNEL_ID = "106";


    /* Connecting to MediaPlaybackService */
    private final ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            musicService = binder.getService();
            musicService.setSongs(listOfSongs);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

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
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this.getContext(), MediaPlaybackService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            musicNavigation.setVisibility(View.GONE);

        }
        else {
            if(musicService.isPlaying()) {
                pauseButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.GONE);
            }else {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        StorageUtil storageUtil = new StorageUtil(getContext());
        int currentSongId = storageUtil.getSongIndex();
        if(currentSongId!=-1) {
            songTitle.setText(listOfSongs.get(currentSongId).getSongTitle());
            songTitle.setSelected(true);
            songArtist.setText(listOfSongs.get(currentSongId).getSongArtist());
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.button_pause)
    public void onPauseButtonClicked() {
        musicService.pauseMedia();
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.button_play)
    public void onPlayButtonClicked() {
        musicService.resumeMedia();
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.button_heart)
    public void onHeartButtonClicked() {
        emptyHeartButton.setVisibility(View.VISIBLE);
        heartButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.button_empty_heart)
    public void onEmptyHeartButtonClicked() {
        emptyHeartButton.setVisibility(View.GONE);
        heartButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.music_player_navigation_id)
    public void onSongNavigationClicked() {
        Navigator.startSingleSong(this.getContext());
    }

    /*@Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("ServiceState", musicBound);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        musicBound = savedInstanceState.getBoolean("ServiceState");
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(musicBound) {
            getContext().unbindService(musicConnection);
            musicService.stopSelf();
        }
        /*getActivity().stopService(playIntent);
        musicService = null;*/
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
            songView.addOnItemTouchListener(new CustomTouchListener(this.getContext(), new OnItemClickListener() {
                @Override
                public void onClick(View view, int index) {
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
                }
            }));
        }
    }

    private void getSongList() {
        String[] projection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.getCount() > 0) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);

                listOfSongs.add(new Song(id, title, artist));
            }
            cursor.close();
        }
    }

}
