package pl.musicplayer;

import android.content.Context;
import android.widget.MediaController;

public class MusicController extends MediaController {
    public MusicController(Context context) {
        super(context);
    }

    @Override
    public void hide() {
        super.hide();
    }
}
