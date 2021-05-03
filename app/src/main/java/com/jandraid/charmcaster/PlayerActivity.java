package com.jandraid.charmcaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jandraid.charmcaster.Additional.Constants;
import com.jandraid.charmcaster.Additional.NotificationService2;
import com.jandraid.charmcaster.Additional.Utility;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity implements SensorEventListener,ActionPlaying {
    static MediaPlayer mp;
    int position;
    SeekBar sb;
    ArrayList<File> mySongs;
    Thread updateSeekBar;
    ImageButton pause,next,previous;
    TextView songNameText,maxpos,cpos;
    String sname;
    byte [] art;

    //Prox
    private SensorManager sensorManager;
    private Sensor proximity;
    //Shake
    private SensorManager sensorManager2;
    private Sensor sensor2;
    private boolean accelerometer_available,First=false;
    private Float currentY,currentZ,lastY,lastZ,Ydiff,Zdiff;
    private Float Shakethreshold = 8f;
    private MediaMetadataRetriever metadataRetriever;
    private ImageView album_art;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        album_art = findViewById(R.id.imageview);
        metadataRetriever = new MediaMetadataRetriever();

        songNameText = (TextView) findViewById(R.id.songtitle);
        pause = findViewById(R.id.pause);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        sb=(SeekBar)findViewById(R.id.seekBar);
        maxpos = findViewById(R.id.maxpos);
        cpos = findViewById(R.id.cpos);

        //Prox
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        //Shake
        sensorManager2 = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager2.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensor2 = sensorManager2.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            accelerometer_available=true;
        }



        updateSeekBar=new Thread(){
            @Override
            public void run(){
                int totalDuration = mp.getDuration();
                int currentPosition = 0;
                while(currentPosition < totalDuration){
                    try{
                        sleep(500);
                        currentPosition=mp.getCurrentPosition();
                        sb.setProgress(currentPosition);
                    }
                    catch (InterruptedException e){

                    }
                }
            }
        };

        if(mp != null){
            mp.stop();
            mp.release();
        }
        Intent i = getIntent();
        Bundle b = i.getExtras();

        mySongs = (ArrayList) b.getParcelableArrayList("songs");

        sname = mySongs.get(position).getName().toString();

        String SongName = i.getStringExtra("songname");
        songNameText.setText(SongName);
        songNameText.setSelected(true);

        position = b.getInt("pos",0);
        Uri u = Uri.parse(mySongs.get(position).toString());

        setAlbumart(u);

        mp = MediaPlayer.create(getApplicationContext(),u);
        mp.start();
        startService();
        sb.setMax(mp.getDuration());

        String strtotal = Utility.convertDuration(mp.getDuration());
        maxpos.setText(strtotal);

        updateSeekBar.start();
        sb.getProgressDrawable().setColorFilter(getResources().getColor(R.color.Primary), PorterDuff.Mode.MULTIPLY);
        sb.getThumb().setColorFilter(getResources().getColor(R.color.Primary), PorterDuff.Mode.SRC_IN);


        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i,
                                          boolean b) {
                String str = Utility.convertDuration(Long.parseLong(String.valueOf(mp.getCurrentPosition())));
                cpos.setText(str);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());

            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sb.setMax(mp.getDuration());
                if(mp.isPlaying()){
                    pause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                    mp.pause();

                }
                else {
                    pause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                    mp.start();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    mp.stop();
                    mp.release();
                    position=((position+1)%mySongs.size());
                    Uri u = Uri.parse(mySongs.get( position).toString());
                    setAlbumart(u);
                    mp = MediaPlayer.create(getApplicationContext(),u);
                    sname = mySongs.get(position).getName().toString();
                    songNameText.setText(sname);
                    String strtotal = Utility.convertDuration(mp.getDuration());
                    maxpos.setText(strtotal);
                }catch(Exception e){}


                try{
                    mp.start();
                }catch(Exception e){}

            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //songNameText.setText(getSongName);
                    mp.stop();
                    mp.release();

                    position=((position-1)<0)?(mySongs.size()-1):(position-1);
                    Uri u = Uri.parse(mySongs.get(position).toString());
                    setAlbumart(u);
                    mp = MediaPlayer.create(getApplicationContext(),u);
                    sname = mySongs.get(position).getName().toString();
                    songNameText.setText(sname);
                    String strtotal = Utility.convertDuration(mp.getDuration());
                    maxpos.setText(strtotal);
                    mp.start();
                }catch(Exception e){

                }
            }
        });

        findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerActivity.super.onBackPressed();
            }
        });

    }

    private void setAlbumart(Uri u) {
        try {

            metadataRetriever.setDataSource(this, u);
            art = metadataRetriever.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            if (bitmap!=null)
            {
                album_art.setImageBitmap(bitmap);
            }else{
                album_art.setImageResource(R.drawable.musicalnotebig);
            }

        }catch(Exception e){

        }

    }

    public void pause(){
        sb.setMax(mp.getDuration());
        if(mp.isPlaying()){
            pause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            mp.pause();

        }
        else {
            pause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            mp.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager2.registerListener(this,sensor2, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        sensorManager2.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        String Enable;
        SharedPreferences settings = getSharedPreferences("SENSOR", Context.MODE_PRIVATE);
        Enable = settings.getString("Enable",null);
        if (Enable == null){
            Enable = "false";
        }
        if (Enable.equals("true")) {
            //Proximity
            if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                float distance = sensorEvent.values[0];
                if (distance == 0) {
                    //pause/play
                    pause();

                }
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                currentY = sensorEvent.values[1];
                currentZ = sensorEvent.values[2];

                if (First) {
                    Ydiff = Math.abs(lastY - currentY);
                    Zdiff = Math.abs(lastZ - currentZ);
                    if (Ydiff > Shakethreshold && Zdiff > Shakethreshold) {
                        Toast.makeText(this, "Shake Detected", Toast.LENGTH_SHORT).show();
                        //NextSong
                        mp.stop();
                        mp.release();
                        position = ((position + 1) % mySongs.size());
                        Uri u = Uri.parse(mySongs.get(position).toString());
                        // songNameText.setText(getSongName);
                        mp = MediaPlayer.create(getApplicationContext(), u);

                        sname = mySongs.get(position).getName().toString();
                        songNameText.setText(sname);

                        try {
                            mp.start();
                        } catch (Exception e) {
                        }
                    }
                }

                lastY = currentY;
                lastZ = currentZ;
                First = true;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void nextclicked() {

    }

    @Override
    public void previousclicked() {

    }

    @Override
    public void pauseplayclicked() {
        if(mp.isPlaying()){
            mp.pause();

        }
        else {
            mp.start();
        }
    }

    @Override
    public void close() {
        if (mp!=null && mp.isPlaying()){
            mp.stop();
            mp.reset();
        }
    }

    public void startService() {
        Intent serviceIntent = new Intent(PlayerActivity.this, NotificationService2.class);
        serviceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(serviceIntent);
    }

}