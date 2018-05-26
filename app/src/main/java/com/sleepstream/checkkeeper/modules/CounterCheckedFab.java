package com.sleepstream.checkkeeper.modules;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.*;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import com.sleepstream.checkkeeper.R;

import static com.sleepstream.checkkeeper.MainActivity.getThemeColor;

public class CounterCheckedFab extends FloatingActionButton {

    private final Property<CounterCheckedFab, Float> ANIMATION_PROPERTY =
            new Property<CounterCheckedFab, Float>(Float.class, "animation") {

                @Override
                public void set(CounterCheckedFab object, Float value) {
                    mAnimationFactor = value;
                    postInvalidateOnAnimation();
                }

                @Override
                public Float get(CounterCheckedFab object) {
                    return 0f;
                }
            };

    private static final int MAX_COUNT = 99;
    private static final String MAX_COUNT_TEXT = "99+";
    private static final int TEXT_SIZE_DP = 11;
    private static final int TEXT_PADDING_DP = 2;
    private static final int MASK_COLOR = Color.parseColor("#33000000"); // Translucent black as mask color
    private static final Interpolator ANIMATION_INTERPOLATOR = new OvershootInterpolator();
    private Bitmap image;

    /**
     *
     */
    private final Paint paintCheck;
    private final Rect mContentBounds;
    private final Paint mTextPaint;
    private final float mTextSize;
    private final Paint mCirclePaint;
    private final Rect mCircleBounds;
    private final Paint mMaskPaint;
    private final int mAnimationDuration;
    private float mAnimationFactor;

    private Integer mCount = null;
    private String mText;
    private float mTextHeight;
    private ObjectAnimator mAnimator;
    private boolean selected = false;



    public CounterCheckedFab(Context context) {
             this(context, null, 0);
    }

    public CounterCheckedFab(Context context, AttributeSet attrs) {
             this(context, attrs, 0);
    }
    public CounterCheckedFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUseCompatPadding(true);

        final float density = getResources().getDisplayMetrics().density;
        image = BitmapFactory.decodeResource(getResources(),R.drawable.ic_done_black_24dp);

        mTextSize = TEXT_SIZE_DP * density;
        float textPadding = TEXT_PADDING_DP * density;

        mAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mAnimationFactor = 1;

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTypeface(Typeface.SANS_SERIF);

        paintCheck = new Paint(Paint.ANTI_ALIAS_FLAG);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(getThemeColor(context, R.attr.colorBackground));

        mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaskPaint.setStyle(Paint.Style.FILL);
        mMaskPaint.setColor(MASK_COLOR);

        Rect textBounds = new Rect();
        mTextPaint.getTextBounds(MAX_COUNT_TEXT, 0, MAX_COUNT_TEXT.length(), textBounds);
        mTextHeight = textBounds.height();

        float textWidth = mTextPaint.measureText(MAX_COUNT_TEXT);
        float circleRadius = Math.max(textWidth, mTextHeight) / 2f + textPadding;
        mCircleBounds = new Rect(0, 0, (int) (circleRadius * 2), (int) (circleRadius * 2));
        mContentBounds = new Rect();

        onCountChanged();
    }

    /**
     * @return The current count value
     */
    public int getCount() {
        return mCount;
    }

    /**
     * Set the count to show on badge
     *
     * @param count The count value starting from 0
     */
    public void setCount(@IntRange(from = 0) int count) {
        if (mCount != null && count == mCount) return;
        mCount = count > 0 ? count : 0;
        onCountChanged();
        if (ViewCompat.isLaidOut(this)) {
            startAnimationCount();
        }
    }

    /**
     * Increase the current count value by 1
     */
    public void increase() {
        setCount(mCount + 1);
    }

    /**
     * Decrease the current count value by 1
     */
    public void decrease() {
        setCount(mCount > 0 ? mCount - 1 : 0);
    }

    public void selectedChange()
    {
        selected = !selected;
        if (ViewCompat.isLaidOut(this)) {
            startAnimationChecked();
        }
    }

    private void onCountChanged() {
        if(mCount != null) {
            if (mCount > MAX_COUNT) {
                mText = String.valueOf(MAX_COUNT_TEXT);
            } else {
                mText = String.valueOf(mCount);
            }
        }
        else
            mText = "";
    }

    private void startAnimationCount() {
        float start = 0f;
        float end = 1f;
        if (mCount == 0) {
            start = 1f;
            end = 0f;
        }
        if (isAnimating()) {
            mAnimator.cancel();
        }
        mAnimator = ObjectAnimator.ofObject(this, ANIMATION_PROPERTY, null, start, end);
        mAnimator.setInterpolator(ANIMATION_INTERPOLATOR);
        mAnimator.setDuration(mAnimationDuration);
        mAnimator.start();
    }

    private void startAnimationChecked() {
        float start = 0f;
        float end = 1f;
        if (isAnimating()) {
            mAnimator.cancel();
        }
        mAnimator = ObjectAnimator.ofObject(this, ANIMATION_PROPERTY, null, start, end);
        mAnimator.setInterpolator(ANIMATION_INTERPOLATOR);
        mAnimator.setDuration(mAnimationDuration);
        mAnimator.start();
    }

    private boolean isAnimating() {
        return mAnimator != null && mAnimator.isRunning();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((mCount!= null && mCount > 0) || isAnimating()) {
            if (getContentRect(mContentBounds)) {
                mCircleBounds.offsetTo(mContentBounds.left + mContentBounds.width() - mCircleBounds.width(), mContentBounds.top);
            }
            float cx = mCircleBounds.centerX();
            float cy = mCircleBounds.centerY();
            float radius = mCircleBounds.width() / 2f * mAnimationFactor;
            // Solid circle
            canvas.drawCircle(cx, cy, radius, mCirclePaint);
            // Mask circle
            canvas.drawCircle(cx, cy, radius, mMaskPaint);
            // Count text
            mTextPaint.setTextSize(mTextSize * mAnimationFactor);
            canvas.drawText(mText, cx, cy + mTextHeight / 2f, mTextPaint);
        }
        else if(mCount==null && selected ||  isAnimating()) {
            if (getContentRect(mContentBounds)) {
                mCircleBounds.offsetTo(mContentBounds.left + mContentBounds.width() - mCircleBounds.width(), mContentBounds.top);
            }
            float cx = mCircleBounds.centerX();
            float cy = mCircleBounds.centerY();
            float radius = mCircleBounds.width() / 2f * mAnimationFactor;
            // Solid circle
            canvas.drawCircle(cx, cy, radius, mCirclePaint);
            // Mask circle
            canvas.drawCircle(cx, cy, radius, mMaskPaint);
            // Check or not check
            canvas.drawBitmap(image, cx-radius, cy-radius, paintCheck);
        }
    }

    private static class SavedState extends View.BaseSavedState {

        private int count;

        /**
         * Constructor called from {@link CounterCheckedFab#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            count = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(count);
        }

        @Override
        public String toString() {
            return CounterCheckedFab.class.getSimpleName() + "." + CounterCheckedFab.SavedState.class.getSimpleName() + "{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " count=" + count + "}";
        }

        public static final Creator<CounterCheckedFab.SavedState> CREATOR
                = new Creator<CounterCheckedFab.SavedState>() {
            public CounterCheckedFab.SavedState createFromParcel(Parcel in) {
                return new CounterCheckedFab.SavedState(in);
            }

            public CounterCheckedFab.SavedState[] newArray(int size) {
                return new CounterCheckedFab.SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CounterCheckedFab.SavedState ss = new CounterCheckedFab.SavedState(superState);
        ss.count = mCount;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        CounterCheckedFab.SavedState ss = (CounterCheckedFab.SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCount(ss.count);
        requestLayout();
    }
}
