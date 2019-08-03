package com.mc.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class GestureManager {
    private long gesture_startTime;
    private long tap_startTime;
    private String state;
//    private MediaPlayer mediaPlayer;
    private TextView textView;
    private ImageView imageView;
    private Handler handler;
    private boolean waved;
    private PlayerManager playerManager;


    public GestureManager(String state, PlayerManager playerManager, TextView textView, ImageView imageView) {
        this.state = state;
        this.playerManager = playerManager;
        this.textView = textView;
        this.tap_startTime = System.currentTimeMillis();
        this.imageView=imageView;
    }

    public void process(String signal) {
        if ((System.currentTimeMillis() - gesture_startTime) > 3000) {
            waved = false;
            System.out.println("wave off");
        }
        if (signal.equals("wave")) {
            waved = true;
            gesture_startTime = System.currentTimeMillis();
            System.out.println("wave on");
        } else if (signal.equals("tap")) {
            if (System.currentTimeMillis() - tap_startTime < 3000) {
                playerManager.getMediaPlayer().stop();
                state = "stoped";
                imageView.setImageResource(R.drawable.stop);
            } else {
                tap_startTime = System.currentTimeMillis();
                if (state.equals("idle")) {
                    playerManager.getMediaPlayer().start();
                    state = "started";
                    imageView.setImageResource(R.drawable.play);
                } else if (state.equals("stoped")) {
                    try {
                        playerManager.getMediaPlayer().prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    playerManager.getMediaPlayer().start();
                    state = "started";
                    imageView.setImageResource(R.drawable.play);
                } else if (state.equals("started")) {
                    playerManager.getMediaPlayer().pause();
                    state = "paused";
                    imageView.setImageResource(R.drawable.pause);
                } else if (state.equals("paused")) {
                    playerManager.getMediaPlayer().start();
                    state = "started";
                    imageView.setImageResource(R.drawable.play);
                }
            }
        } else if (waved && signal.equals("R_Shake")) {
            if (System.currentTimeMillis() - gesture_startTime < 3000) {
                state = "next";
                playerManager.next();
                state="started";
                imageView.setImageResource(R.drawable.next);
                waved = false;
            }
        } else if (waved && signal.equals("L_Shake")) {
            if (System.currentTimeMillis() - gesture_startTime < 3000) {
                state = "previous";
                playerManager.prev();
                state="started";
                imageView.setImageResource(R.drawable.prev);
                waved = false;
            }
        }


        textView.setText(state);
    }
}
