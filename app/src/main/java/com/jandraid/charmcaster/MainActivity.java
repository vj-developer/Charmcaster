package com.jandraid.charmcaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    ListView listView;
    String[] items;
    String Enable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.listview);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        display();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    void display(){
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        if (!mySongs.isEmpty())
        {
            items = new String[ mySongs.size() ];
            for(int i=0;i<mySongs.size();i++){
                items[i] = mySongs.get(i).getName().toString().replace(".mp3","").replace(".wav","");
            }

            ArrayAdapter<String> adp = new ArrayAdapter<String>(this,R.layout.list_item,items);
            listView.setAdapter(adp);
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int
                    position, long l) {

                String songName = listView.getItemAtPosition(position).toString();
                startActivity(new Intent(getApplicationContext(),PlayerActivity.class)
                        .putExtra("pos",position).putExtra("songs",mySongs).putExtra("songname",songName));
            }
        });

    }

    public ArrayList<File> findSong(File root){
        ArrayList<File> at = new ArrayList<File>();
        File[] files = root.listFiles();
        assert files != null;
        for(File singleFile : files){
            if(singleFile.isDirectory() && !singleFile.isHidden()){
                at.addAll(findSong(singleFile));
            }
            else{
                if(singleFile.getName().endsWith(".mp3") ||
                        singleFile.getName().endsWith(".wav")){
                    at.add(singleFile);
                }
            }
        }
        return at;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu,menu);
        MenuItem item = menu.findItem(R.id.sensor);
        String Enable;
        SharedPreferences settings = getSharedPreferences("SENSOR", Context.MODE_PRIVATE);
        Enable = settings.getString("Enable",null);
        if (Enable!=null){
            if (Enable.equals("true")){item.setTitle("Disable Sensors");}
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.sensor)
        {
            SharedPreferences settings = getSharedPreferences("SENSOR", Context.MODE_PRIVATE);
            Enable = settings.getString("Enable",null);
            if (Enable==null || Enable.equals("false")){
                SharedPreferences settings3 = getSharedPreferences("SENSOR", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings3.edit();
                editor.putString("Enable", "true");
                editor.commit();
                Toast.makeText(this, "Sensor Controls Enabled", Toast.LENGTH_SHORT).show();
                item.setTitle("Disable Sensors");


            }
            else if (Enable != null && Enable.equals("true")){
                SharedPreferences settings2 = getSharedPreferences("SENSOR", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings2.edit();
                editor.putString("Enable", "false");
                editor.commit();
                Toast.makeText(this, "Sensor Controls Disabled", Toast.LENGTH_SHORT).show();
                item.setTitle("Enable Sensors");
            }



        }
        if(item.getItemId() == R.id.share)
        {
            //share app
            shareApplication();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareApplication() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            String shareMessage= "\nHey! Try this Music Player\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch(Exception e) {
            //e.toString();
        }
    }

}