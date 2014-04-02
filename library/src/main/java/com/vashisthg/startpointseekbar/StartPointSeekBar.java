package com.vashisthg.startpointseekbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import java.math.BigDecimal;

/**
 * Created by vashisthg on 01/04/14.
 */
public class StartPointSeekBar<T extends Number>  extends View
{
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final T absoluteMinValue, absoluteMaxValue;
    private final double absoluteMinValuePrim, absoluteMaxValuePrim;
    private final Bitmap thumbImage;
    private final Bitmap thumbPressedImage;
    private final float thumbWidth;
    private final float thumbHalfWidth;
    private final float thumbHalfHeight;
    private final float lineHeight;
    private final float padding;
    private int mScaledTouchSlop;
    private boolean mIsDragging;
    private final NumberType numberType;


    private boolean isThumbPressed;
    private double normalizedThumbValue = 0d;

    private boolean notifyWhileDragging = true;

    private OnSeekBarChangeListener<T> listener;

    public final int SINGLE_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5);
    public static final int BACKGROUND_COLOR = Color.GRAY;

    /**
     * Callback listener interface to notify about changed range values.
     *
     * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
     *
     * @param <T>
     * The Number type the Seekbar has been declared with.
     */
    public interface OnSeekBarChangeListener<T> {
        public void onOnSeekBarValueChange(StartPointSeekBar<?> bar,
                                                T value);
    }

    /**
     * Registers given listener callback to notify about changed selected
     * values.
     *
     * @param listener
     *            The listener to notify about changed selected values.
     */
    public void setOnSeekBarChangeListener(
            OnSeekBarChangeListener<T> listener) {
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

    public StartPointSeekBar(T absoluteMinValue, T absoluteMaxValue, Context context)
    {
        super(context);
        thumbImage = BitmapFactory.decodeResource(getResources(),
                R.drawable.seek_thumb_normal);
        thumbPressedImage = BitmapFactory.decodeResource(getResources(),
                R.drawable.seek_thumb_pressed);

        this.absoluteMinValue = absoluteMinValue;
        this.absoluteMaxValue = absoluteMaxValue;
        absoluteMinValuePrim = absoluteMinValue.doubleValue();
        absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
        numberType = NumberType.fromNumber(absoluteMinValue);
        thumbWidth = thumbImage.getWidth();
        thumbHalfWidth = 0.5f * thumbWidth;
        thumbHalfHeight = 0.5f * thumbImage.getHeight();
        lineHeight = 0.3f * thumbHalfHeight;
        padding = thumbHalfWidth;
        setFocusable(true);
        setFocusableInTouchMode(true);
        init();
    }

    private final void init() {
        mScaledTouchSlop = ViewConfiguration.get(getContext())
                .getScaledTouchSlop();
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
    public boolean onTouchEvent(MotionEvent event)
    {
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

                    if (mIsDragging) {
                        trackTouchEvent(event);
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);

                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
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
                if (mIsDragging) {
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
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private final void onSecondaryPointerUp(MotionEvent ev) {
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

    private final void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);


        setNormalizedValue(screenToNormalized(x));
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoord
     *            The x-coordinate in screen space to convert.
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
    private T normalizedToValue(double normalized) {
        return (T) numberType.toNumber(absoluteMinValuePrim + normalized
                * (absoluteMaxValuePrim - absoluteMinValuePrim));
    }

    /**
     * Converts the given Number value to a normalized double.
     *
     * @param value
     *            The Number value to normalize.
     * @return The normalized double.
     */
    private double valueToNormalized(T value) {
        if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            // prevent division by zero, simply return 0.
            return 0d;
        }
        return (value.doubleValue() - absoluteMinValuePrim)
                / (absoluteMaxValuePrim - absoluteMinValuePrim);
    }

    private double valueToNormalized(double value) {
        if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            // prevent division by zero, simply return 0.
            return 0d;
        }
        return (value - absoluteMinValuePrim)
                / (absoluteMaxValuePrim - absoluteMinValuePrim);
    }


    /**
     * Sets normalized max value to value so that 0 <= normalized min value <=
     * value <= 1. The View will get invalidated when calling this method.
     *
     * @param value
     *            The new normalized max value to set.
     */
    public void setNormalizedValue(double value) {
        normalizedThumbValue =  Math.max(0d, value);
        invalidate();
    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch() {
        mIsDragging = true;
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    void onStopTrackingTouch() {
        mIsDragging = false;
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX
     *            The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private boolean evalPressedThumb(float touchX) {
        return isInThumbRange(touchX, normalizedThumbValue);

    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as
     * "within" the normalized thumb x-coordinate.
     *
     * @param touchX
     *            The x-coordinate in screen space to check.
     * @param normalizedThumbValue
     *            The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth;
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoord
     *            The normalized value to convert.
     * @return The converted value in screen space.
     */
    private float normalizedToScreen(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);


        // draw seek bar background line
        final RectF rect = new RectF(padding,
                0.5f * (getHeight() - lineHeight), getWidth() - padding,
                0.5f * (getHeight() + lineHeight));
        paint.setColor(BACKGROUND_COLOR);
        canvas.drawRect(rect, paint);

        // draw seek bar active range line

        if(normalizedToScreen(valueToNormalized(0.0d)) < normalizedToScreen(normalizedThumbValue))
        {
            Log.d(VIEW_LOG_TAG, "thumb: right");
            rect.left = normalizedToScreen(valueToNormalized(0.0d));
            rect.right = normalizedToScreen(normalizedThumbValue);
        } else {
            Log.d(VIEW_LOG_TAG, "thumb: left");
            rect.right = normalizedToScreen(valueToNormalized(0.0d));
            rect.left = normalizedToScreen(normalizedThumbValue);
        }

        paint.setColor(SINGLE_COLOR);
        canvas.drawRect(rect, paint);

        drawThumb(normalizedToScreen(normalizedThumbValue),
                isThumbPressed, canvas);
        Log.d(VIEW_LOG_TAG, "thumb: " + normalizedToValue(normalizedThumbValue));
    }


    /**
     * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
     *
     * @param screenCoord
     *            The x-coordinate in screen space where to draw the image.
     * @param pressed
     *            Is the thumb currently in "pressed" state?
     * @param canvas
     *            The canvas to draw upon.
     */
    private void drawThumb(float screenCoord, boolean pressed, Canvas canvas) {
        canvas.drawBitmap(pressed ? thumbPressedImage : thumbImage, screenCoord
                        - thumbHalfWidth,
                (float) ((0.5f * getHeight()) - thumbHalfHeight), paint);
    }

    /**
     * Utility enumaration used to convert between Numbers and doubles.
     *
     * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
     *
     */
    private static enum NumberType {
        LONG, DOUBLE, INTEGER, FLOAT, SHORT, BYTE, BIG_DECIMAL;

        public static <E extends Number> NumberType fromNumber(E value)
                throws IllegalArgumentException {
            if (value instanceof Long) {
                return LONG;
            }
            if (value instanceof Double) {
                return DOUBLE;
            }
            if (value instanceof Integer) {
                return INTEGER;
            }
            if (value instanceof Float) {
                return FLOAT;
            }
            if (value instanceof Short) {
                return SHORT;
            }
            if (value instanceof Byte) {
                return BYTE;
            }
            if (value instanceof BigDecimal) {
                return BIG_DECIMAL;
            }
            throw new IllegalArgumentException("Number class '"
                    + value.getClass().getName() + "' is not supported");
        }

        public Number toNumber(double value) {
            switch (this) {
                case LONG:
                    return new Long((long) value);
                case DOUBLE:
                    return value;
                case INTEGER:
                    return new Integer((int) value);
                case FLOAT:
                    return new Float(value);
                case SHORT:
                    return new Short((short) value);
                case BYTE:
                    return new Byte((byte) value);
                case BIG_DECIMAL:
                    return new BigDecimal(value);
            }
            throw new InstantiationError("can't convert " + this
                    + " to a Number object");
        }
    }
}
