Forked/Inspired from https://code.google.com/p/range-seek-bar/ by tittel@kom.e-technik.tu-darmstadt.de


This solves the problem as described in http://stackoverflow.com/questions/17415096/seekbar-for-two-values-50-0-50

![ScreenShot](http://imgur.com/N4TOD31.png)



So you can set the start position of the seekbar anywhere.

Example code:
```xml
    <com.vashisthg.startpointseekbar.StartPointSeekBar
        android:id="@+id/seek_bar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        custom:minValue="-40.0"
        custom:maxValue="+40.0"
        custom:defaultBackgroundColor="@color/default_background_color"
        custom:defaultBackgroundRangeColor="@color/default_background_range_color"
        />
```


```java
        StartPointSeekBar seekBar = (StartPointSeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new StartPointSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onOnSeekBarValueChange(StartPointSeekBar bar, double value) {
                Log.d(LOGTAG, "seekbar value:" + value);
            }
        });
```



