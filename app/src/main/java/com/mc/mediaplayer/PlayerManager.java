package com.mc.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.util.ArrayList;

public class PlayerManager {
    private MediaPlayer mediaPlayer;
    private ArrayList<Uri> uris;
    private int current_song;
    private Context context;

    public PlayerManager(Context context) {
        this.mediaPlayer = new MediaPlayer();
        this.uris = new ArrayList<>();
        this.current_song = 0;
        this.context= context;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public ArrayList<Uri> getUris() {
        return uris;
    }

    public int getCurrent_song() {
        return current_song;
    }

    public void setCurrent_song(int current_song) {
        this.current_song = current_song;
    }

    public void createPlayer(int index){
        mediaPlayer= MediaPlayer.create(context, uris.get(index));
    }
    public  void next(){
        mediaPlayer.stop();
        int nxt= current_song+1;
        if(nxt>uris.size()){
            nxt=current_song;
        }
        mediaPlayer= MediaPlayer.create(context, uris.get(nxt));
        mediaPlayer.start();
    }

    public void prev(){
        mediaPlayer.stop();
        int prev= current_song-1;
        if(prev<0){
            prev=current_song;
        }
        mediaPlayer= MediaPlayer.create(context,uris.get(prev));
        mediaPlayer.start();
    }
}
