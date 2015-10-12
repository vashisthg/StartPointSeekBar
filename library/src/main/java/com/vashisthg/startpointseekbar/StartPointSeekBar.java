package com.vashisthg.startpointseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.math.BigDecimal;

/**
 * Created by vashisthg on 01/04/14.
 */
public class StartPointSeekBar extends View {
    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int DEFAULT_RANGE_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5);
    private static final int DEFAULT_BACKGROUND_COLOR = Color.GRAY;
    private static final float DEFAULT_MIN_VALUE = -100f;
    private static final float DEFAULT_MAX_VALUE = +100f;


    private double absoluteMinValue;
    private double absoluteMaxValue;
    private final Bitmap thumbImage;
    private final Bitmap thumbPressedImage;
    private final int defaultRangeColor;
    private final int defaultBackgroundColor;

    private final float thumbWidth;
    private final float thumbHalfWidth;
    private final float thumbHalfHeight;
    private final float lineHeight;
    private final float padding;
    private int scaledTouchSlop;
    private boolean isDragging;
    private boolean isThumbPressed;
    private double normalizedThumbValue = 0d;
    private boolean notifyWhileDragging = true;
    private OnSeekBarChangeListener listener;



    /**
     * Callback listener interface to notify about changed range values.
     *
     */
    public interface OnSeekBarChangeListener {
        void onOnSeekBarValueChange(StartPointSeekBar bar,
                                           double value);
    }

    /**
     * Registers given listener callback to notify about changed selected
     * values.
     *
     * @param listener The listener to notify about changed selected values.
     */
    public void setOnSeekBarChangeListener(
            OnSeekBarChangeListener listener) {
        this.listener = listener;
    }

    /**
     * An invalid pointer id.
     */
    public static final int INVALID_POINTER_ID = 255;

    // Localized constants from MotionEvent for compatibility
    // with API < 8 "Froyo".
    public static final int ACTION_POINTER_UP = 0x6,
            ACTION_POINTER_INDEX_MASK = 0x0000ff00,
            ACTION_POINTER_INDEX_SHIFT = 8;

    private float mDownMotionX;
    private int mActivePointerId = INVALID_POINTER_ID;

    public StartPointSeekBar(Context context) {
        this(context, null);
    }

    public StartPointSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StartPointSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Attribute initialization
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StartPointSeekBar,
                defStyleAttr, 0);

        Drawable thumbImageDrawable = a.getDrawable(R.styleable.StartPointSeekBar_thumbDrawable);
        if (thumbImageDrawable == null) {
            thumbImageDrawable = getResources().getDrawable(R.drawable.seek_thumb_normal);
        }
        this.thumbImage = ((BitmapDrawable) thumbImageDrawable).getBitmap();

        Drawable thumbImagePressedDrawable = a.getDrawable(R.styleable.StartPointSeekBar_thumbPressedDrawable);
        if (thumbImagePressedDrawable == null) {
            thumbImagePressedDrawable = getResources().getDrawable(R.drawable.seek_thumb_pressed);
        }
        this.thumbPressedImage = ((BitmapDrawable) thumbImagePressedDrawable).getBitmap();

        this.absoluteMinValue = a.getFloat(R.styleable.StartPointSeekBar_minValue, DEFAULT_MIN_VALUE);
        this.absoluteMaxValue = a.getFloat(R.styleable.StartPointSeekBar_maxValue, DEFAULT_MAX_VALUE);

        this.defaultBackgroundColor = a.getColor(R.styleable.StartPointSeekBar_defaultBackgroundColor,
                DEFAULT_BACKGROUND_COLOR);
        this.defaultRangeColor = a.getColor(R.styleable.StartPointSeekBar_defaultBackgroundRangeColor,
                DEFAULT_RANGE_COLOR);

        a.recycle();

        thumbWidth = thumbImage.getWidth();
        thumbHalfWidth = 0.5f * thumbWidth;
        thumbHalfHeight = 0.5f * thumbImage.getHeight();
        lineHeight = 0.3f * thumbHalfHeight;
        padding = thumbHalfWidth;
        setFocusable(true);
        setFocusableInTouchMode(true);
        scaledTouchSlop = ViewConfiguration.get(getContext())
                .getScaledTouchSlop();
    }

    public void setAbsoluteMinMaxValue(double absoluteMinValue, double absoluteMaxValue) {
        this.absoluteMinValue = absoluteMinValue;
        this.absoluteMaxValue = absoluteMaxValue;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
                                          int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = thumbImage.getHeight();
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return false;

        int pointerIndex;

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);

                isThumbPressed = evalPressedThumb(mDownMotionX);

                // Only handle thumb presses.
                if (!isThumbPressed)
                    return super.onTouchEvent(event);

                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();

                break;
            case MotionEvent.ACTION_MOVE:
                if (isThumbPressed) {

                    if (isDragging) {
                        trackTouchEvent(event);
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);

                        if (Math.abs(x - mDownMotionX) > scaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }

                    if (notifyWhileDragging && listener != null) {
                        listener.onOnSeekBarValueChange(this, normalizedToValue(normalizedThumbValue));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                isThumbPressed = false;
                invalidate();
                if (listener != null) {
                    listener.onOnSeekBarValueChange(this, normalizedToValue(normalizedThumbValue));
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = event.getPointerCount() - 1;
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);
        setNormalizedValue(screenToNormalized(x));
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoord The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    private double screenToNormalized(float screenCoord) {
        int width = getWidth();
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0d;
        } else {
            double result = (screenCoord - padding) / (width - 2 * padding);
            return Math.min(1d, Math.max(0d, result));
        }
    }

    /**
     * Converts a normalized value to a Number object in the value space between
     * absolute minimum and maximum.
     *
     * @param normalized
     * @return
     */
    @SuppressWarnings("unchecked")
    private double normalizedToValue(double normalized) {
        return  absoluteMinValue + normalized
                * (absoluteMaxValue - absoluteMinValue);
    }

    /**
     * Converts the given Number value to a normalized double.
     *
     * @param value The Number value to normalize.
     * @return The normalized double.
     */

    private double valueToNormalized(double value) {
        if (0 == absoluteMaxValue - absoluteMinValue) {
            // prevent division by zero, simply return 0.
            return 0d;
        }
        return (value - absoluteMinValue)
                / (absoluteMaxValue - absoluteMinValue);
    }


    /**
     * Sets normalized max value to value so that 0 <= normalized min value <=
     * value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized max value to set.
     */

    private void setNormalizedValue(double value) {
        normalizedThumbValue = Math.max(0d, value);
        invalidate();
    }

    /**
     * Sets value of seekbar to the given value
     * @param value The new value to set
     */
    public void setProgress(double value) {
        double newThumbValue = valueToNormalized(value);
        if (newThumbValue > absoluteMaxValue || newThumbValue < absoluteMinValue) {
            throw new IllegalArgumentException("Value should be in the middle of max and min value");
        }
        normalizedThumbValue = newThumbValue;
        invalidate();
    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch() {
        isDragging = true;
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    void onStopTrackingTouch() {
        isDragging = false;
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private boolean evalPressedThumb(float touchX) {
        return isInThumbRange(touchX, normalizedThumbValue);
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as
     * "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth;
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoord The normalized value to convert.
     * @return The converted value in screen space.
     */
    private float normalizedToScreen(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw seek bar background line
        final RectF rect = new RectF(padding,
                0.5f * (getHeight() - lineHeight), getWidth() - padding,
                0.5f * (getHeight() + lineHeight));
        paint.setColor(defaultBackgroundColor);
        canvas.drawRect(rect, paint);

        // draw seek bar active range line

        if (normalizedToScreen(valueToNormalized(0.0d)) < normalizedToScreen(normalizedThumbValue)) {
            Log.d(VIEW_LOG_TAG, "thumb: right");
            rect.left = normalizedToScreen(valueToNormalized(0.0d));
            rect.right = normalizedToScreen(normalizedThumbValue);
        } else {
            Log.d(VIEW_LOG_TAG, "thumb: left");
            rect.right = normalizedToScreen(valueToNormalized(0.0d));
            rect.left = normalizedToScreen(normalizedThumbValue);
        }

        paint.setColor(defaultRangeColor);
        canvas.drawRect(rect, paint);

        drawThumb(normalizedToScreen(normalizedThumbValue),
                isThumbPressed, canvas);
        Log.d(VIEW_LOG_TAG, "thumb: " + normalizedToValue(normalizedThumbValue));
    }


    /**
     * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
     *
     * @param screenCoord The x-coordinate in screen space where to draw the image.
     * @param pressed     Is the thumb currently in "pressed" state?
     * @param canvas      The canvas to draw upon.
     */
    private void drawThumb(float screenCoord, boolean pressed, Canvas canvas) {
        canvas.drawBitmap(pressed ? thumbPressedImage : thumbImage, screenCoord
                        - thumbHalfWidth,
                (float) ((0.5f * getHeight()) - thumbHalfHeight), paint);
    }
}
