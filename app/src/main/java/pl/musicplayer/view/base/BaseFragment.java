package pl.musicplayer.view.base;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.musicplayer.MediaPlaybackService;
import pl.musicplayer.MusicController;
import pl.musicplayer.R;
import pl.musicplayer.data.Song;
import pl.musicplayer.navigation.Navigator;
import pl.musicplayer.utils.StorageUtil;

public class BaseFragment extends Fragment {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.song_list)
    protected RecyclerView songView;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.music_player_navigation_id)
    protected LinearLayout musicNavigation;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.music_progress_bar)
    protected SeekBar progressBarController;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.button_play)
    protected ImageView playButton;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.button_pause)
    protected ImageView pauseButton;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.button_empty_heart)
    protected ImageView emptyHeartButton;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.button_heart)
    protected ImageView heartButton;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.song_title)
    protected TextView songTitle;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.song_artist)
    protected TextView songArtist;

    protected List<Song> listOfSongs;
    protected MediaPlaybackService musicService;
    private static Intent playIntent;
    private boolean musicBound = false;
    protected Handler handler = new Handler();


    /* Connecting to MediaPlaybackService */
    protected final ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlaybackService.MusicBinder binder = (MediaPlaybackService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setSongs(listOfSongs);
            musicBound = true;

            if(musicService.isPlaying()) {
                pauseButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.GONE);
                StrartbarUpdate();
            }else if(!musicService.isPlaying()) {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
            }


        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        progressBarController.setOnSeekBarChangeListener(new ProgressBarListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this.getContext(), MediaPlaybackService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
            musicNavigation.setVisibility(View.GONE);
            StorageUtil storageUtil = new StorageUtil(getContext());
            storageUtil.setSongIndex(-1);
        }
        getContext().bindService(playIntent, musicConnection, Service.BIND_AUTO_CREATE);
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
        getContext().unbindService(musicConnection);
        Navigator.startSingleSong(this.getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!musicBound) {
            getActivity().stopService(playIntent);
        }
    }

    protected void getSongList() {
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

    class ProgressBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser==true) {
                musicService.getPlayer().seekTo(progress);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }


    public void StrartbarUpdate(){
        handler.post(r);
    }
    Runnable r=new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            int CurrentPosition=musicService.getPlayer().getCurrentPosition();

            int mMax=musicService.getSongDuration();
            progressBarController.setMax(mMax);
            progressBarController.setProgress(CurrentPosition);
            handler.postDelayed(r, 300);
        }
    };

}
