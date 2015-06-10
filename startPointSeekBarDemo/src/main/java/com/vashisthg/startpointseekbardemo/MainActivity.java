package com.vashisthg.startpointseekbardemo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.vashisthg.startpointseekbar.StartPointSeekBar;


public class MainActivity extends ActionBarActivity {

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
    }
}
