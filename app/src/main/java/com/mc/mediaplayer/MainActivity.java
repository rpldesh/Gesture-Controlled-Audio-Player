package com.mc.mediaplayer;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private TextView state_view, s1_val, playTime_view;
    private ImageView img;
    private Button button;
    private boolean started;

    private SensorManager sensorManager;
    private Sensor sensor1;
    private Sensor sensor2;
    private SensorEventListener sensorListener;
    private SensorEventListener sensorEventListener2;

    private float val;
    private float preVal;

    private long startTime;
    private long endtime;
    private long timeDiff;
    private long waveTime;
    private boolean waveStarted;
    private boolean waveEnded;

    private String state;
    private GestureManager gestureManager;

    private Context context;
    private PlayerManager playerManager;

    private float max=0.0f;
    private float min= 0.0f;
    private long r_shake;
    private long l_shake;

    private ArrayList<Uri> uris;
    private int current_song;
    private Handler handler= new Handler();
    private SeekBar seekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        started=false;
        context = getApplicationContext();
        uris= new ArrayList<>();

        state_view=findViewById(R.id.state_view);
        img= findViewById(R.id.imageView);
        playTime_view= findViewById(R.id.text_play);
        seekBar= findViewById(R.id.seekBar);
        s1_val= findViewById(R.id.s1_val);
        button= findViewById(R.id.button2);
        button.setText("start");
        button.setBackgroundColor(Color.rgb(0,220,120));




        preVal = 5;
        state = "idle";
        //mediaPlayer = MediaPlayer.create(context, R.raw.song);
//        mediaPlayer = new MediaPlayer();
        playerManager= new PlayerManager(context);

//        Uri uri = Uri.parse("android.resource://com.mc.mediaplayer/raw/song_0");
//        final Uri uri2 = Uri.parse("android.resource://com.mc.mediaplayer/raw/song_1");
        String path= "android.resource://com.mc.mediaplayer/raw/song_";

        for(int i=0; i<6;i++){
            Uri uri= Uri.parse(path+Integer.toString(i));
            playerManager.getUris().add(uri);
        }

        try {
            playerManager.getMediaPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
            playerManager.getMediaPlayer().setDataSource(context,playerManager.getUris().get(2));
            playerManager.getMediaPlayer().prepare();
            playerManager.setCurrent_song(2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //       sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        sensorManager.registerListener(sensorListener, sensor1, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener2,sensor2,SensorManager.SENSOR_DELAY_NORMAL);

        gestureManager = new GestureManager(state, playerManager, state_view, img);
        r_shake = System.currentTimeMillis();
        l_shake = System.currentTimeMillis();

        handler.postDelayed(UpdateSongTime,100);

        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                val = event.values[0];
                //value_x.setText(Float.toString(val));

                if ((preVal > 4.1) && (val < 0.1) && !waveStarted) {
                    startTime = System.currentTimeMillis();
                    waveStarted = true;
                    System.out.println("Started");
                }
                if ((preVal < 0.1) && (val > 4.1) && !waveEnded) {
                    endtime = System.currentTimeMillis();
                    waveEnded = true;
                    System.out.println("Ended");
                }
                if (waveStarted && waveEnded) {
                    timeDiff = endtime - startTime;
                    s1_val.setText(Float.toString(timeDiff));
                    waveStarted = false;
                    waveEnded = false;
                    System.out.println(timeDiff);
                    if ((timeDiff > 175) && (timeDiff < 225)) {
                        gestureManager.process("wave");
                    } else if ((timeDiff > 350) && (timeDiff < 450)) {
                        gestureManager.process("tap");
                    } else {
                        System.out.println("Not Recognized");
                    }


                }
                preVal = val;

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

        };

        sensorEventListener2 = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                Float accel= event.values[0];
                if(accel>4){
                    r_shake= System.currentTimeMillis();
//                    System.out.println(accel);
                }
                if(accel<-4){
                    l_shake = System.currentTimeMillis();
//                    System.out.println(accel);
                }
                if((r_shake>l_shake)&& ((r_shake-l_shake)<500)){
                    System.out.println("Right Shake");
                    gestureManager.process("R_Shake");
                    r_shake=System.currentTimeMillis();
                    l_shake= System.currentTimeMillis();
                }
                else if((r_shake<l_shake)&&((l_shake-r_shake)<500)){
                    System.out.println("Left Shake");
                    gestureManager.process("L_Shake");
                    r_shake=System.currentTimeMillis();
                    l_shake= System.currentTimeMillis();
                }
//                Float valy=event.values[1];
//                Float valz= event.values[2];

//                value_y.setText(Float.toString(valy));
//                value_x.setText(Float.toString(valx));
//                value_z.setText(Float.toString(valz));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(!started){
                    sensorManager.registerListener(sensorListener, sensor1, SensorManager.SENSOR_DELAY_UI);
                    sensorManager.registerListener(sensorEventListener2,sensor2,SensorManager.SENSOR_DELAY_NORMAL);
                    started=true;
                    String label= "Stop";
                    button.setText(label);

                    button.setBackgroundColor(Color.rgb(255,60,75));
                }
                else {
                    sensorManager.unregisterListener(sensorListener);
                    sensorManager.unregisterListener(sensorEventListener2);
                    started=false;
                    String label= "Start";
                    button.setText(label);
                    button.setBackgroundColor(Color.rgb(0,220,120));

                }
            }
        });
    }
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            long playTime = playerManager.getMediaPlayer().getCurrentPosition();
            long length= playerManager.getMediaPlayer().getDuration();
//            tx1.setText(String.format("%d min, %d sec",
//                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
//                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
//                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
//                                    toMinutes((long) startTime)))
//            );
            playTime_view.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) playTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) playTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) playTime)))
            );
            seekBar.setMax((int)length/1000);
            seekBar.setProgress((int) playTime/1000);
            handler.postDelayed(this, 100);
        }
    };
}
