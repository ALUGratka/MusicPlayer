package pl.musicplayer;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.List;

import pl.musicplayer.data.Song;
import pl.musicplayer.utils.StorageUtil;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer player;
    private AudioManager audioManager;
    private List<Song> songs;
    private int resumePosition;
    private final IBinder binder = new MusicBinder();

    public class MusicBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnInfoListener(this);
    }

    public void setSongs(List<Song>songs) {
        this.songs = songs;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN:
                if (player == null) initMusicPlayer();
                else if (!player.isPlaying()) player.start();
                player.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (player.isPlaying()) player.stop();
                player.release();
                player = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (player.isPlaying()) player.pause();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
                break;
            default:
                if (player.isPlaying()) player.pause();
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) { }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) { }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player != null) {
            stopMedia();
            player.release();
        }
        removeAudioFocus();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        StorageUtil storageUtil = new StorageUtil(this);
        mp.reset();
        if (player.getCurrentPosition() != songs.size() - 1) {
            storageUtil.setSongIndex(storageUtil.getSongIndex()+1);;
        } else {
            storageUtil.setSongIndex(0);
        }
        playSong();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //mp.start();
        playMusic();
    }


    public void playSong() {
        player.reset();
        Song playSong = songs.get(new StorageUtil(this).getSongIndex());
        long currentSong = playSong.getSongId();

        Uri songUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currentSong);

        try{
            player.setDataSource(getApplicationContext(),songUri);
        }catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public void setSong(int songIndex) {
        new StorageUtil(this).setSongIndex(songIndex);
    }

    public void playMusic() {
        if(!player.isPlaying()) {
            player.start();
        }
    }

    public void stopMedia() {
        if(player == null) return;
        if(player.isPlaying()) {
            player.stop();
        }
    }

    public void pauseMedia() {
        if(player == null) return;
        if(player.isPlaying()) {
            player.pause();
            resumePosition = player.getCurrentPosition();
        }
    }

    public void resumeMedia() {
        if(!player.isPlaying()){
            player.seekTo(resumePosition);
            player.start();
        }
    }

    public MediaPlayer getMusicPlayer() {
        return player;
    }

    public int getSongDuration() {
        return player.getDuration();
    }

    public int getSongPosition() {
        return player.getCurrentPosition();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void seek(int songPosition) {
        player.seekTo(songPosition);
    }

    public void playPrev() {
        StorageUtil storageUtil = new StorageUtil(this);
        int songPosition = storageUtil.getSongIndex()-1;
        if(songPosition<0) storageUtil.setSongIndex(songs.size()-1);
        else storageUtil.setSongIndex(songPosition);
        playSong();
    }

    public void playNext() {
        StorageUtil storageUtil = new StorageUtil(this);
        int songPosition = storageUtil.getSongIndex()+1;
        if(songPosition==songs.size()) storageUtil.setSongIndex(0);
        else storageUtil.setSongIndex(songPosition);
        playSong();
    }
}
