package com.vashisthg.startpointseekbardemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.vashisthg.startpointseekbar.StartPointSeekBar;


public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StartPointSeekBar seekBar = (StartPointSeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new StartPointSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onOnSeekBarValueChange(StartPointSeekBar bar, double value) {
                Log.d(LOGTAG, "seekbar value:" + value);
            }
        });


        // setting progress on your own
        seekBar.setProgress(+20);
    }
}
