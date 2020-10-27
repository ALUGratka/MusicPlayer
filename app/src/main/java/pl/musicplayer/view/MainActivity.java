package pl.musicplayer.view;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.musicplayer.R;
import pl.musicplayer.data.Song;
import pl.musicplayer.view.albums.AlbumsFragment;
import pl.musicplayer.view.artists.ArtistsFragment;
import pl.musicplayer.view.favourite.FavouriteFragment;
import pl.musicplayer.view.folders.FoldersFragment;
import pl.musicplayer.view.loops.LoopsFragment;
import pl.musicplayer.view.main.SectionsPagerAdapter;
import pl.musicplayer.view.songs.SongsFragment;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter sectionsPagerAdapter;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tabs)
    TabLayout tabs;

    public static Intent getStartingIntent(final Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupWithViewPager(viewPager);
        tabs.setupWithViewPager(viewPager);
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
    }

    private void setupWithViewPager(ViewPager viewPager) {
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sectionsPagerAdapter.addPage(new SongsFragment(),getString(R.string.main_tabs_songs));
        sectionsPagerAdapter.addPage(new ArtistsFragment(), getString(R.string.main_tabs_artists));
        sectionsPagerAdapter.addPage(new AlbumsFragment(), getString(R.string.main_tabs_albums));
        sectionsPagerAdapter.addPage(new FoldersFragment(), getString(R.string.main_tabs_folders));
        sectionsPagerAdapter.addPage(new LoopsFragment(), getString(R.string.main_tabs_loops));
        sectionsPagerAdapter.addPage(new FavouriteFragment(), getString(R.string.main_tabs_favourite));
        viewPager.setAdapter(sectionsPagerAdapter);
    }

}