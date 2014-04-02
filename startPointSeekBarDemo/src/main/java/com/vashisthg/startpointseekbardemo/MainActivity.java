package com.vashisthg.startpointseekbardemo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.vashisthg.startpointseekbar.StartPointSeekBar;


public class MainActivity extends ActionBarActivity {

    private static final String LOGTAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StartPointSeekBar<Integer> seekBar = new StartPointSeekBar<Integer>(-100, +100, this);
        seekBar.setOnSeekBarChangeListener(new StartPointSeekBar.OnSeekBarChangeListener<Integer>()
        {
            @Override
            public void onOnSeekBarValueChange(StartPointSeekBar<?> bar, Integer value)
            {
                Log.d(LOGTAG, "seekbar value:" + value);
            }
        });

        // add RangeSeekBar to pre-defined layout
        ViewGroup layout = (ViewGroup) findViewById(R.id.seekbarwrapper);
        layout.addView(seekBar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
