package pl.musicplayer.view.song;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.musicplayer.MediaPlaybackService;
import pl.musicplayer.MusicController;
import pl.musicplayer.R;
import pl.musicplayer.data.Song;
import pl.musicplayer.utils.StorageUtil;
import pl.musicplayer.view.base.BaseFragment;

public class SingleSongActivity extends AppCompatActivity {

    @BindView(R.id.single_song_top_bar)
    Toolbar toolbar;

    @BindView(R.id.single_song_tittle_text_view)
    TextView songTitle;

    @BindView(R.id.single_song_artist_text_view)
    TextView songArtist;

    @BindView(R.id.single_song_play_button)
    ImageView playButton;

    @BindView(R.id.single_song_pause_button)
    ImageView pauseButton;

    @BindView(R.id.single_song_seek_bar)
    SeekBar seekBar;

    private List<Song> songList;
    private MediaPlaybackService musicService;
    private static Intent playIntent;
    protected Handler handler = new Handler();

    private final ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlaybackService.MusicBinder binder = (MediaPlaybackService.MusicBinder) service;
            musicService = binder.getService();
            onStart();

            if(musicService.isPlaying()) {
                pauseButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.GONE);
                StrartbarUpdate();
            }else {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            detachService();
        }
    };

    private void attachService() {
        Intent service = new Intent(this, MediaPlaybackService.class);
        bindService(service, musicConnection, Service.BIND_AUTO_CREATE);
    }

    private void detachService() {
        unbindService(musicConnection);
    }

    public static Intent getStartingIntent(final Context context) {
        return new Intent(context, SingleSongActivity.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MediaPlaybackService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        StorageUtil storageUtil = new StorageUtil(this);
        int currentSongId = storageUtil.getSongIndex();
        if(currentSongId!=-1) {
            songTitle.setText(songList.get(currentSongId).getSongTitle());
            songTitle.setSelected(true);
            songArtist.setText(songList.get(currentSongId).getSongArtist());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_song);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        songList = new ArrayList<>();
        getSongList();

        attachService();

        loadSongData(getCurrentSongId());
        seekBar.setOnSeekBarChangeListener(new ProgressBarListener());
    }


    @OnClick(R.id.single_song_play_button)
    public void onPlayButtonClicked(){
        musicService.resumeMedia();
        pauseButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.single_song_pause_button)
    public void onPauseButtonClicked(){
        musicService.pauseMedia();
        pauseButton.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.single_song_prev_button)
    public void onPrevButtonClicked(){
        musicService.playPrev();
        StorageUtil storageUtil = new StorageUtil(this);
        loadSongData(storageUtil.getSongIndex());

    }

    @OnClick(R.id.single_song_next_button)
    public void onNextButtonClicked(){
        musicService.playNext();
        StorageUtil storageUtil = new StorageUtil(this);
        loadSongData(storageUtil.getSongIndex());
    }

    public int getCurrentSongId() {
        StorageUtil storageUtil = new StorageUtil(this);
        return storageUtil.getSongIndex();
    }

    private void loadSongData(int currentSongIndex) {
        Song song = songList.get(currentSongIndex);

        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.more:
                Toast.makeText(this,"more", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_song_app_bar, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        detachService();
        super.onDestroy();
    }

    private void getSongList() {
        String[] projection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = getApplicationContext().getContentResolver().query(
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

                songList.add(new Song(id, title, artist));
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
            seekBar.setMax(mMax);
            seekBar.setProgress(CurrentPosition);
            handler.postDelayed(r, 300);
        }
    };

}
