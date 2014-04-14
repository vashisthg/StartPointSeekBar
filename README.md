Forked/Inspired from https://code.google.com/p/range-seek-bar/ by tittel@kom.e-technik.tu-darmstadt.de


This solves the problem as described in http://stackoverflow.com/questions/17415096/seekbar-for-two-values-50-0-50

![ScreenShot](http://imgur.com/N4TOD31.png)



So you can set the start position of the seekbar anywhere.

Example code:
```java
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
```



