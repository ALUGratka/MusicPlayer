package pl.musicplayer.navigation;

import android.content.Context;

import pl.musicplayer.data.Song;
import pl.musicplayer.view.song.SingleSongActivity;

public class Navigator {

    public static void startSingleSong(final Context context) {
        context.startActivity(SingleSongActivity.getStartingIntent(context));
    }
}
