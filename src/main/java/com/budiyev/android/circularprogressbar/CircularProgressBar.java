/*
 * MIT License
 *
 * Copyright (c) 2017 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.android.circularprogressbar;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Circular progress bar
 */
public class CircularProgressBar extends View {
    private static final float DEFAULT_MAXIMUM = 100f;
    private static final float DEFAULT_PROGRESS = 0f;
    private static final float DEFAULT_FOREGROUND_STROKE_WIDTH_DP = 3f;
    private static final float DEFAULT_BACKGROUND_STROKE_WIDTH_DP = 1f;
    private static final float DEFAULT_START_ANGLE = 270f;
    private static final float DEFAULT_INDETERMINATE_MINIMUM_ANGLE = 60f;
    private static final int DEFAULT_FOREGROUND_STROKE_COLOR = Color.BLUE;
    private static final int DEFAULT_BACKGROUND_STROKE_COLOR = Color.BLACK;
    private static final int DEFAULT_PROGRESS_ANIMATION_DURATION = 500;
    private static final int DEFAULT_INDETERMINATE_GROW_ANIMATION_DURATION = 2000;
    private static final int DEFAULT_INDETERMINATE_SWEEP_ANIMATION_DURATION = 1000;
    private static final boolean DEFAULT_ANIMATE_PROGRESS = true;
    private static final boolean DEFAULT_DRAW_BACKGROUND_STROKE = true;
    private static final boolean DEFAULT_INDETERMINATE = false;
    private float mMaximum;
    private float mProgress;
    private float mStartAngle;
    private float mIndeterminateStartAngle;
    private float mIndeterminateSweepAngle;
    private float mIndeterminateOffsetAngle;
    private float mIndeterminateMinimumAngle;
    private boolean mIndeterminate;
    private boolean mAnimateProgress;
    private boolean mDrawBackgroundStroke;
    private boolean mIndeterminateGrowMode;
    private Paint mForegroundStrokePaint;
    private Paint mBackgroundStrokePaint;
    private RectF mDrawRect;
    private ValueAnimator mProgressAnimator;
    private ValueAnimator mIndeterminateStartAnimator;
    private ValueAnimator mIndeterminateSweepAnimator;

    public CircularProgressBar(@NonNull Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public CircularProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public CircularProgressBar(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircularProgressBar(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Get current progress value for non-indeterminate mode
     */
    public float getProgress() {
        return mProgress;
    }

    /**
     * Set current progress value for non-indeterminate mode
     */
    public void setProgress(float progress) {
        if (mIndeterminate) {
            mProgress = progress;
        } else {
            stopProgressAnimation();
            if (mAnimateProgress && isLaidOutCompat()) {
                setProgressAnimated(progress);
            } else {
                setProgressInternal(progress);
            }
        }
    }

    /**
     * Configure progress bar
     */
    @NonNull
    public Configurator configure() {
        return new Configurator();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int measuredHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
        invalidateDrawRect(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        invalidateDrawRect();
        if (mIndeterminate) {
            startIndeterminateAnimations();
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        invalidateDrawRect(width, height);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            if (mIndeterminate) {
                startIndeterminateAnimations();
            }
        } else {
            stopIndeterminateAnimations();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIndeterminate && isLaidOutCompat()) {
            startIndeterminateAnimations();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopIndeterminateAnimations();
        stopProgressAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawBackgroundStroke) {
            canvas.drawOval(mDrawRect, mBackgroundStrokePaint);
        }
        float start;
        float sweep;
        if (mIndeterminate) {
            float startAngle = mIndeterminateStartAngle;
            float sweepAngle = mIndeterminateSweepAngle;
            float offsetAngle = mIndeterminateOffsetAngle;
            float minimumAngle = mIndeterminateMinimumAngle;
            if (mIndeterminateGrowMode) {
                start = startAngle - offsetAngle;
                sweep = sweepAngle + minimumAngle;
            } else {
                start = startAngle + sweepAngle - offsetAngle;
                sweep = 360f - sweepAngle - minimumAngle;
            }
        } else {
            float maximum = mMaximum;
            float progress = mProgress;
            start = mStartAngle;
            sweep = 360f * Math.abs(progress) > Math.abs(maximum) ? maximum : progress / maximum;
        }
        canvas.drawArc(mDrawRect, start, sweep, false, mForegroundStrokePaint);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attributeSet,
            @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        mDrawRect = new RectF();
        mForegroundStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mForegroundStrokePaint.setStyle(Paint.Style.STROKE);
        mBackgroundStrokePaint.setStyle(Paint.Style.STROKE);
        mProgressAnimator = new ValueAnimator();
        mIndeterminateStartAnimator = new ValueAnimator();
        mIndeterminateSweepAnimator = new ValueAnimator();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (attributeSet == null) {
            mMaximum = DEFAULT_MAXIMUM;
            mProgress = DEFAULT_PROGRESS;
            mStartAngle = DEFAULT_START_ANGLE;
            mIndeterminateMinimumAngle = DEFAULT_INDETERMINATE_MINIMUM_ANGLE;
            mProgressAnimator.setDuration(DEFAULT_PROGRESS_ANIMATION_DURATION);
            mIndeterminate = DEFAULT_INDETERMINATE;
            mAnimateProgress = DEFAULT_ANIMATE_PROGRESS;
            mDrawBackgroundStroke = DEFAULT_DRAW_BACKGROUND_STROKE;
            mForegroundStrokePaint.setColor(DEFAULT_FOREGROUND_STROKE_COLOR);
            mForegroundStrokePaint.setStrokeWidth(
                    Math.round(DEFAULT_FOREGROUND_STROKE_WIDTH_DP * displayMetrics.density));
            mBackgroundStrokePaint.setColor(DEFAULT_BACKGROUND_STROKE_COLOR);
            mBackgroundStrokePaint.setStrokeWidth(
                    Math.round(DEFAULT_BACKGROUND_STROKE_WIDTH_DP * displayMetrics.density));
            mIndeterminateStartAnimator.setDuration(DEFAULT_INDETERMINATE_GROW_ANIMATION_DURATION);
            mIndeterminateSweepAnimator.setDuration(DEFAULT_INDETERMINATE_SWEEP_ANIMATION_DURATION);
        } else {
            TypedArray attributes = null;
            try {
                attributes = context.getTheme()
                        .obtainStyledAttributes(attributeSet, R.styleable.CircularProgressBar,
                                defStyleAttr, defStyleRes);
                mMaximum = attributes
                        .getFloat(R.styleable.CircularProgressBar_maximum, DEFAULT_MAXIMUM);
                mProgress = attributes
                        .getFloat(R.styleable.CircularProgressBar_progress, DEFAULT_PROGRESS);
                mStartAngle = attributes
                        .getFloat(R.styleable.CircularProgressBar_startAngle, DEFAULT_START_ANGLE);
                mIndeterminateMinimumAngle = attributes
                        .getFloat(R.styleable.CircularProgressBar_indeterminateMinimumAngle,
                                DEFAULT_INDETERMINATE_MINIMUM_ANGLE);
                mProgressAnimator.setDuration(attributes
                        .getInteger(R.styleable.CircularProgressBar_progressAnimationDuration,
                                DEFAULT_PROGRESS_ANIMATION_DURATION));
                mIndeterminateStartAnimator.setDuration(attributes.getInteger(
                        R.styleable.CircularProgressBar_indeterminateGrowAnimationDuration,
                        DEFAULT_INDETERMINATE_GROW_ANIMATION_DURATION));
                mIndeterminateSweepAnimator.setDuration(attributes.getInteger(
                        R.styleable.CircularProgressBar_indeterminateSweepAnimationDuration,
                        DEFAULT_INDETERMINATE_SWEEP_ANIMATION_DURATION));
                mForegroundStrokePaint.setColor(attributes
                        .getColor(R.styleable.CircularProgressBar_foregroundStrokeColor,
                                DEFAULT_FOREGROUND_STROKE_COLOR));
                mBackgroundStrokePaint.setColor(attributes
                        .getColor(R.styleable.CircularProgressBar_backgroundStrokeColor,
                                DEFAULT_BACKGROUND_STROKE_COLOR));
                mForegroundStrokePaint.setStrokeWidth(attributes.getDimensionPixelSize(
                        R.styleable.CircularProgressBar_foregroundStrokeWidth,
                        Math.round(DEFAULT_FOREGROUND_STROKE_WIDTH_DP * displayMetrics.density)));
                mBackgroundStrokePaint.setStrokeWidth(attributes.getDimensionPixelSize(
                        R.styleable.CircularProgressBar_backgroundStrokeWidth,
                        Math.round(DEFAULT_BACKGROUND_STROKE_WIDTH_DP * displayMetrics.density)));
                mAnimateProgress = attributes
                        .getBoolean(R.styleable.CircularProgressBar_animateProgress,
                                DEFAULT_ANIMATE_PROGRESS);
                mDrawBackgroundStroke = attributes
                        .getBoolean(R.styleable.CircularProgressBar_drawBackgroundStroke,
                                DEFAULT_DRAW_BACKGROUND_STROKE);
                mIndeterminate = attributes
                        .getBoolean(R.styleable.CircularProgressBar_indeterminate,
                                DEFAULT_INDETERMINATE);
            } finally {
                if (attributes != null) {
                    attributes.recycle();
                }
            }
        }
        mProgressAnimator.setInterpolator(new DecelerateInterpolator());
        mProgressAnimator.addUpdateListener(new ProgressUpdateListener());
        mIndeterminateStartAnimator.setFloatValues(360f);
        mIndeterminateStartAnimator.setRepeatMode(ValueAnimator.RESTART);
        mIndeterminateStartAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mIndeterminateStartAnimator.setInterpolator(new LinearInterpolator());
        mIndeterminateStartAnimator.addUpdateListener(new StartUpdateListener());
        mIndeterminateSweepAnimator.setFloatValues(360f - mIndeterminateMinimumAngle * 2f);
        mIndeterminateSweepAnimator.setRepeatMode(ValueAnimator.RESTART);
        mIndeterminateSweepAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mIndeterminateSweepAnimator.setInterpolator(new DecelerateInterpolator());
        mIndeterminateSweepAnimator.addUpdateListener(new SweepUpdateListener());
        mIndeterminateSweepAnimator.addListener(new SweepAnimatorListener());
    }

    private boolean isLaidOutCompat() {
        return getWidth() > 0 && getHeight() > 0;
    }

    private void invalidateDrawRect() {
        int width = getWidth();
        int height = getHeight();
        if (width > 0 && height > 0) {
            invalidateDrawRect(width, height);
        }
    }

    private void invalidateDrawRect(int width, int height) {
        int size;
        if (mDrawBackgroundStroke) {
            size = Math.round(Math.max(mForegroundStrokePaint.getStrokeWidth(),
                    mBackgroundStrokePaint.getStrokeWidth()));
        } else {
            size = Math.round(mForegroundStrokePaint.getStrokeWidth());
        }
        if (width > height) {
            int space = (width - height) / 2;
            mDrawRect.set(space + size / 2 + 1, size / 2 + 1, width - space - size / 2 - 1,
                    height - size / 2 - 1);
        } else if (width < height) {
            int space = (height - width) / 2;
            mDrawRect.set(size / 2 + 1, space + size / 2 + 1, width - size / 2 - 1,
                    height - space - size / 2 - 1);
        } else {
            mDrawRect.set(size / 2 + 1, size / 2 + 1, width - size / 2 - 1, height - size / 2 - 1);
        }
    }

    private void setProgressInternal(float progress) {
        mProgress = progress;
        invalidate();
    }

    private void setProgressAnimated(float progress) {
        ValueAnimator progressAnimator = mProgressAnimator;
        if (progressAnimator == null) {
            setProgressInternal(progress);
        } else {
            progressAnimator.setFloatValues(mProgress, progress);
            progressAnimator.start();
        }
    }

    private void stopProgressAnimation() {
        ValueAnimator progressAnimator = mProgressAnimator;
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
    }

    private void stopIndeterminateAnimations() {
        ValueAnimator growAnimator = mIndeterminateStartAnimator;
        if (growAnimator != null && growAnimator.isRunning()) {
            growAnimator.cancel();
        }
        ValueAnimator sweepAnimator = mIndeterminateSweepAnimator;
        if (sweepAnimator != null && sweepAnimator.isRunning()) {
            sweepAnimator.cancel();
        }
    }

    private void startIndeterminateAnimations() {
        if (isLaidOutCompat()) {
            ValueAnimator growAnimator = mIndeterminateStartAnimator;
            if (growAnimator != null && !growAnimator.isRunning()) {
                growAnimator.start();
            }
            ValueAnimator sweepAnimator = mIndeterminateSweepAnimator;
            if (sweepAnimator != null && !sweepAnimator.isRunning()) {
                sweepAnimator.start();
            }
        }
    }

    public final class Configurator {
        private long progressAnimationDuration;
        private long indeterminateGrowAnimationDuration;
        private long indeterminateSweepAnimationDuration;
        private float maximum;
        private float progress;
        private float startAngle;
        private float indeterminateMinimumAngle;
        private float foregroundStrokeWidth;
        private float backgroundStrokeWidth;
        private int foregroundStrokeColor;
        private int backgroundStrokeColor;
        private boolean indeterminate;
        private boolean animateProgress;
        private boolean drawBackgroundStroke;

        private Configurator() {
            progressAnimationDuration = mProgressAnimator.getDuration();
            indeterminateGrowAnimationDuration = mIndeterminateStartAnimator.getDuration();
            indeterminateSweepAnimationDuration = mIndeterminateSweepAnimator.getDuration();
            maximum = mMaximum;
            progress = mProgress;
            startAngle = mStartAngle;
            indeterminateMinimumAngle = mIndeterminateMinimumAngle;
            indeterminate = mIndeterminate;
            animateProgress = mAnimateProgress;
            drawBackgroundStroke = mDrawBackgroundStroke;
            Paint foregroundStrokePaint = mForegroundStrokePaint;
            Paint backgroundStrokePaint = mBackgroundStrokePaint;
            foregroundStrokeColor = foregroundStrokePaint.getColor();
            backgroundStrokeColor = backgroundStrokePaint.getColor();
            foregroundStrokeWidth = foregroundStrokePaint.getStrokeWidth();
            backgroundStrokeWidth = backgroundStrokePaint.getStrokeWidth();
        }

        @MainThread
        public void apply() {
            stopProgressAnimation();
            boolean indeterminateChanged = indeterminate != mIndeterminate;
            if (indeterminateChanged && !indeterminate) {
                stopIndeterminateAnimations();
            }
            mIndeterminate = indeterminate;
            mMaximum = maximum;
            mAnimateProgress = animateProgress;
            mProgressAnimator.setDuration(progressAnimationDuration);
            mIndeterminateStartAnimator.setDuration(indeterminateGrowAnimationDuration);
            mIndeterminateSweepAnimator.setDuration(indeterminateSweepAnimationDuration);
            mIndeterminateSweepAnimator.setFloatValues(360f - indeterminateMinimumAngle * 2f);
            Paint foregroundStrokePaint = mForegroundStrokePaint;
            Paint backgroundStrokePaint = mBackgroundStrokePaint;
            foregroundStrokePaint.setColor(foregroundStrokeColor);
            backgroundStrokePaint.setColor(backgroundStrokeColor);
            foregroundStrokePaint.setStrokeWidth(foregroundStrokeWidth);
            backgroundStrokePaint.setStrokeWidth(backgroundStrokeWidth);
            mStartAngle = startAngle;
            mIndeterminateMinimumAngle = indeterminateMinimumAngle;
            mDrawBackgroundStroke = drawBackgroundStroke;
            invalidateDrawRect();
            if (indeterminate) {
                mProgress = progress;
            } else {
                if (animateProgress && isLaidOutCompat()) {
                    setProgressAnimated(progress);
                } else {
                    setProgressInternal(progress);
                }
            }
            if (indeterminateChanged && indeterminate) {
                startIndeterminateAnimations();
            }
            invalidate();
        }

        /**
         * Maximum progress value for non-indeterminate mode
         */
        @NonNull
        public Configurator maximum(float value) {
            maximum = value;
            return this;
        }

        /**
         * Current progress value for non-indeterminate mode
         */
        @NonNull
        public Configurator progress(float value) {
            progress = value;
            return this;
        }

        /**
         * Start angle for non-indeterminate mode
         */
        @NonNull
        public Configurator startAngle(float value) {
            startAngle = value;
            return this;
        }

        /**
         * Minimum angle for indeterminate mode
         */
        @NonNull
        public Configurator indeterminateMinimumAngle(float value) {
            indeterminateMinimumAngle = value;
            return this;
        }

        /**
         * Grow animation duration in milliseconds for indeterminate mode
         */
        public void indeterminateGrowAnimationDuration(long value) {
            indeterminateGrowAnimationDuration = value;
        }

        /**
         * Sweep animation duration in milliseconds for indeterminate mode
         */
        public void indeterminateSweepAnimationDuration(long value) {
            indeterminateSweepAnimationDuration = value;
        }

        /**
         * Set indeterminate mode enabled or disabled
         */
        @NonNull
        public Configurator indeterminate(boolean value) {
            indeterminate = value;
            return this;
        }

        /**
         * Animate progress change or not for non-indeterminate mode
         */
        @NonNull
        public Configurator animateProgress(boolean value) {
            animateProgress = value;
            return this;
        }

        /**
         * Progress change animation duration in milliseconds for non-indeterminate mode
         */
        @NonNull
        public Configurator progressAnimationDuration(long value) {
            progressAnimationDuration = value;
            return this;
        }

        /**
         * Draw background stroke or not
         */
        @NonNull
        public Configurator drawBackgroundStroke(boolean value) {
            drawBackgroundStroke = value;
            return this;
        }

        /**
         * Foreground stroke color
         */
        @NonNull
        public Configurator foregroundStrokeColor(@ColorInt int value) {
            foregroundStrokeColor = value;
            return this;
        }

        /**
         * Background stroke color
         */
        @NonNull
        public Configurator backgroundStrokeColor(@ColorInt int value) {
            backgroundStrokeColor = value;
            return this;
        }

        /**
         * Foreground stroke width
         */
        @NonNull
        public Configurator foregroundStrokeWidth(@Px int value) {
            foregroundStrokeWidth = value;
            return this;
        }

        /**
         * Background stroke width
         */
        @NonNull
        public Configurator backgroundStrokeWidth(@Px int value) {
            backgroundStrokeWidth = value;
            return this;
        }
    }

    private final class ProgressUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            setProgressInternal((float) animation.getAnimatedValue());
        }
    }

    private final class StartUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mIndeterminateStartAngle = (float) animation.getAnimatedValue();
            invalidate();
        }
    }

    private final class SweepUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mIndeterminateSweepAngle = (float) animation.getAnimatedValue();
        }
    }

    private final class SweepAnimatorListener implements ValueAnimator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
            // Do nothing
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // Do nothing
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            // Do nothing
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mIndeterminateStartAnimator.pause();
            }
            boolean growMode = !mIndeterminateGrowMode;
            mIndeterminateGrowMode = growMode;
            if (growMode) {
                mIndeterminateOffsetAngle =
                        (mIndeterminateOffsetAngle + mIndeterminateMinimumAngle * 2f) % 360f;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mIndeterminateStartAnimator.resume();
            }
        }
    }
}
