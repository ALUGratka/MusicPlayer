package pl.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class StorageUtil {
    private static final String STORAGE = "pl.musicplayer.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void setSongIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("songIndex", index);
        editor.apply();
    }

    public int getSongIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("songIndex", -1);
    }
}
