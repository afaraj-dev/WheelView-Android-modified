package com.view.picker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.OverScroller;

import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;

import com.view.picker.library.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Modified from https://github.com/lantouzi/WheelView-Android.
 * Sample project with history of changes here: https://github.com/amitav13/WheelView-Android-modified
 */
public class HorizontalWheelPicker extends View implements GestureDetector.OnGestureListener {
    public static final float DEFAULT_INTERVAL_FACTOR = 1.5f;
    public static final float DEFAULT_MARK_RATIO = 0.7f;

    public static final int HIGHLIGHT_SP_SIZE = 35;
    public static final int NORMAL_SP_SIZE = 27;
    public static final int TITLE_SP_SIZE = 20;

    private Paint mMarkPaint;
    private TextPaint mMarkTextPaint, mTitleTextPaint;
    private int mCenterIndex = -1;

    private int mHighlightColor, mItemNormalTextColor, mTitleTextColor;
    private int mMarkColor, mFadeMarkColor;

    private int mViewHeight;
    private float mTitleTextHeight, mItemTextHeight;
    private List<String> mItems;
    private final Map<Integer, String> mItemTitleMap = new HashMap<>();
    private String mAdditionCenterMark;
    private OnWheelItemSelectedListener mOnWheelItemSelectedListener;
    private float mIntervalFactor = DEFAULT_INTERVAL_FACTOR;
    private float mMarkRatio = DEFAULT_MARK_RATIO;
    private boolean mDrawMarks = false;

    private int mMarkCount;
    private final Path mCenterIndicatorPath = new Path();
    private float mTopIndexHeight;
    private int mViewScopeSize;

    // scroll control args ---- start
    private OverScroller mScroller;
    private float mMaxOverScrollDistance;
    private RectF mContentRectF;
    private boolean mFling = false;
    private float mCenterTextSize, mNormalTextSize, mTitleTextSize;
    private float mIntervalDis;
    private float mCenterMarkWidth, mMarkWidth;
    private GestureDetectorCompat mGestureDetectorCompat;
    // scroll control args ---- end

    private int mLastSelectedIndex = -1;
    private int mMinSelectableIndex = Integer.MIN_VALUE;
    private int mMaxSelectableIndex = Integer.MAX_VALUE;

    public HorizontalWheelPicker(Context context) {
        super(context);
        init(null);
    }

    public HorizontalWheelPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public HorizontalWheelPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    protected void init(AttributeSet attrs) {
        mCenterMarkWidth = 0;
        mMarkWidth = 0;

        mHighlightColor = 0xFF666666;
        mItemNormalTextColor = 0x80666666;
        mTitleTextColor = 0x80999999;
        mMarkColor = 0xFFEEEEEE;
        mTopIndexHeight = 0;

        mCenterTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                HIGHLIGHT_SP_SIZE, getResources().getDisplayMetrics());

        mNormalTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                NORMAL_SP_SIZE, getResources().getDisplayMetrics());

        mTitleTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                TITLE_SP_SIZE, getResources().getDisplayMetrics());

        TypedArray ta = attrs == null ? null : getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalWheelPicker);
        if (ta != null) {
            mHighlightColor = ta.getColor(R.styleable.HorizontalWheelPicker_wpHighlightColor, mHighlightColor);
            mItemNormalTextColor = ta.getColor(R.styleable.HorizontalWheelPicker_wpItemNormalTextColor, mItemNormalTextColor);
            mMarkColor = ta.getColor(R.styleable.HorizontalWheelPicker_wpMarkColor, mMarkColor);
            mIntervalFactor = ta.getFloat(R.styleable.HorizontalWheelPicker_wpIntervalFactor, mIntervalFactor);
            mMarkRatio = ta.getFloat(R.styleable.HorizontalWheelPicker_wpMarkRatio, mMarkRatio);
            mAdditionCenterMark = ta.getString(R.styleable.HorizontalWheelPicker_wpAdditionalCenterMark);
            mCenterTextSize = ta.getDimension(R.styleable.HorizontalWheelPicker_wpItemSelectedTextSize, mCenterTextSize);
            mNormalTextSize = ta.getDimension(R.styleable.HorizontalWheelPicker_wpItemNormalTextSize, mNormalTextSize);
            mTopIndexHeight = ta.getDimension(R.styleable.HorizontalWheelPicker_wpCursorSize, mTopIndexHeight);
            mTitleTextSize = ta.getDimension(R.styleable.HorizontalWheelPicker_wpTitleTextSize, mTitleTextSize);
            mDrawMarks = ta.getBoolean(R.styleable.HorizontalWheelPicker_wpDrawHashMarks, mDrawMarks);
            mTitleTextColor = ta.getColor(R.styleable.HorizontalWheelPicker_wpTitleTextColor, mTitleTextColor);
            ta.recycle();
        }
        mNormalTextSize = Math.min(mNormalTextSize, mCenterTextSize);
        mFadeMarkColor = mHighlightColor & 0xAAFFFFFF;
        mIntervalFactor = Math.max(1, mIntervalFactor);
        mMarkRatio = Math.min(1, mMarkRatio);

        mMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mMarkTextPaint.setTextAlign(Paint.Align.CENTER);
        mMarkTextPaint.setColor(mHighlightColor);

        mTitleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTitleTextPaint.setTextAlign(Paint.Align.CENTER);
        mTitleTextPaint.setTextSize(mTitleTextSize);
        mTitleTextPaint.setColor(mTitleTextColor);
        Paint.FontMetrics fm = mTitleTextPaint.getFontMetrics();
        mTitleTextHeight = fm.descent - fm.ascent;

        mMarkPaint.setColor(mMarkColor);
        mMarkPaint.setStrokeWidth(mCenterMarkWidth);

        mMarkTextPaint.setTextSize(mCenterTextSize);
        fm = mMarkTextPaint.getFontMetrics();
        mItemTextHeight = fm.bottom - fm.top;
        calcIntervalDis();

        mScroller = new OverScroller(getContext());
        mContentRectF = new RectF();

        mGestureDetectorCompat = new GestureDetectorCompat(getContext(), this);

        if (isInEditMode()) {
            ArrayList<String> preview = new ArrayList<>();
            for (int i = 0; i < 20; i++)
                preview.add(String.format(Locale.ENGLISH, "%d", i));
            setItems(preview);
            mItemTitleMap.put(0, "have");
            mItemTitleMap.put(1, "a");
            mItemTitleMap.put(3, "nice");
            mItemTitleMap.put(4, "day");
        }

        selectIndex(0);
    }

    /**
     * calculate distance between Interval Marks
     */
    private void calcIntervalDis() {
        if (mMarkTextPaint == null) {
            return;
        }
        String defaultText = "888888";
        Rect temp = new Rect();
        int max = 0;
        if (mItems != null && mItems.size() > 0) {
            for (String i : mItems) {
                mMarkTextPaint.getTextBounds(i, 0, i.length(), temp);
                if (temp.width() > max) {
                    max = temp.width();
                }
            }
        } else {
            mMarkTextPaint.getTextBounds(defaultText, 0, defaultText.length(), temp);
            max = temp.width();
        }

        if (!TextUtils.isEmpty(mAdditionCenterMark)) {
            mMarkTextPaint.getTextBounds(mAdditionCenterMark, 0, mAdditionCenterMark.length(), temp);
            max += temp.width();
        }

        mIntervalDis = max * mIntervalFactor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = MeasureSpec.getSize(widthMeasureSpec);
        int result = getSuggestedMinimumWidth();
        switch (measureMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = measureSize;
                break;
            default:
                break;
        }
        return result;
    }

    private int measureHeight(int heightMeasure) {
        int measureMode = MeasureSpec.getMode(heightMeasure);
        int measureSize = MeasureSpec.getSize(heightMeasure);
        int result = (int) (getPaddingBottom() + mItemTextHeight + getPaddingTop() + mTitleTextHeight);
        switch (measureMode) {
            case MeasureSpec.EXACTLY:
                result = Math.max(result, measureSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, measureSize);
                break;
            default:
                break;
        }
        return result;
    }

    public void fling(int velocityX, int velocityY) {
        mScroller.fling(getScrollX(), getScrollY(),
                velocityX, velocityY,
                (int) (-mMaxOverScrollDistance + mMinSelectableIndex * mIntervalDis), (int) (mContentRectF.width() - mMaxOverScrollDistance - (mMarkCount - 1 - mMaxSelectableIndex) * mIntervalDis),
                0, 0,
                (int) mMaxOverScrollDistance, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            mViewHeight = h;
            mMaxOverScrollDistance = w / 2.f;
            mContentRectF.set(0, 0, (mMarkCount - 1) * mIntervalDis, h);
            mViewScopeSize = (int) Math.ceil(mMaxOverScrollDistance / mIntervalDis);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int top = getPaddingTop();
        int bottom = getPaddingBottom();

        mCenterIndicatorPath.reset();
        if (mTopIndexHeight > 0) {
            float sizeDiv2 = mTopIndexHeight / 2f;
            float sizeDiv3 = mTopIndexHeight / 3f;
            mCenterIndicatorPath.moveTo(mMaxOverScrollDistance - sizeDiv2 + getScrollX(), top);
            mCenterIndicatorPath.rLineTo(0, sizeDiv3);
            mCenterIndicatorPath.rLineTo(sizeDiv2, sizeDiv2);
            mCenterIndicatorPath.rLineTo(sizeDiv2, -sizeDiv2);
            mCenterIndicatorPath.rLineTo(0, -sizeDiv3);
            mCenterIndicatorPath.close();
        }

        mMarkPaint.setColor(mHighlightColor);
        canvas.drawPath(mCenterIndicatorPath, mMarkPaint);

        int start = mCenterIndex - mViewScopeSize;
        int end = mCenterIndex + mViewScopeSize + 1;

        start = Math.max(start, -mViewScopeSize * 2);
        end = Math.min(end, mMarkCount + mViewScopeSize * 2);

        // extends both ends
        if (mCenterIndex == mMaxSelectableIndex) {
            end += mViewScopeSize;
        } else if (mCenterIndex == mMinSelectableIndex) {
            start -= mViewScopeSize;
        }

        float x = start * mIntervalDis;

        float markHeight = mViewHeight - mCenterTextSize - top - bottom;
        // small scale Y offset
        float smallMarkShrinkY = markHeight * (1 - mMarkRatio) / 2f;
        smallMarkShrinkY = Math.min((markHeight - mMarkWidth) / 2f, smallMarkShrinkY);

        for (int i = start; i < end; i++) {
            float subMark = mIntervalDis / 5f;
            // offset: Small mark offset Big mark
            if (mDrawMarks) {
                for (int offset = -2; offset < 3; offset++) {
                    float subX = x + offset * subMark;
                    if (i >= 0 && i <= mMarkCount && mCenterIndex == i) {
                        int tempOffset = Math.abs(offset);
                        if (tempOffset == 0) {
                            mMarkPaint.setColor(mHighlightColor);
                        } else if (tempOffset == 1) {
                            mMarkPaint.setColor(mFadeMarkColor);
                        } else {
                            mMarkPaint.setColor(mMarkColor);
                        }
                    } else {
                        mMarkPaint.setColor(mMarkColor);
                    }

                    if (offset == 0) {
                        // main hash mark
                        mMarkPaint.setStrokeWidth(mCenterMarkWidth);
                        canvas.drawLine(subX, getPaddingTop(), subX, top + markHeight, mMarkPaint);
                    } else {
                        // sub hash mark
                        mMarkPaint.setStrokeWidth(mMarkWidth);
                        canvas.drawLine(subX, top + smallMarkShrinkY, subX, top + markHeight - smallMarkShrinkY, mMarkPaint);
                    }
                }
            }

            // item text
            float centerOffsetY = mCenterTextSize / 2f - mNormalTextSize / 2f;
            if (mMarkCount > 0 && i >= 0 && i < mMarkCount) {
                CharSequence itemStr = mItems.get(i);
                if (mCenterIndex == i) {
                    mMarkTextPaint.setColor(mHighlightColor);
                    mMarkTextPaint.setTextSize(mCenterTextSize);
                    Paint.FontMetrics fm = mMarkTextPaint.getFontMetrics();
                    if (!TextUtils.isEmpty(mAdditionCenterMark)) {
                        //draw item with additional text
                        float additionalTextWidth = mMarkTextPaint.measureText(mAdditionCenterMark, 0, mAdditionCenterMark.length());
                        float itemWidth = mMarkTextPaint.measureText(itemStr, 0, itemStr.length());
                        canvas.drawText(itemStr, 0, itemStr.length(), x - (additionalTextWidth) / 2, mViewHeight - bottom - fm.descent, mMarkTextPaint);
                        mMarkTextPaint.setTextSize(mNormalTextSize);
                        canvas.drawText(mAdditionCenterMark, x + itemWidth / 2, mViewHeight - bottom - fm.descent, mMarkTextPaint);
                    } else {
                        //draw item
                        canvas.drawText(itemStr, 0, itemStr.length(), x, mViewHeight - bottom - fm.descent, mMarkTextPaint);
                    }
                } else {
                    //draw non-highlighted item
                    mMarkTextPaint.setColor(mItemNormalTextColor);
                    mMarkTextPaint.setTextSize(mNormalTextSize);
                    Paint.FontMetrics fm = mMarkTextPaint.getFontMetrics();
                    canvas.drawText(itemStr, 0, itemStr.length(), x, mViewHeight - bottom - centerOffsetY - fm.descent, mMarkTextPaint);
                }
                String title = mItemTitleMap.get(i);
                if (title != null)
                    canvas.drawText(title, x, mTitleTextHeight + top, mTitleTextPaint);
            }
            x += mIntervalDis;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mItems == null || mItems.size() == 0 || !isEnabled()) {
            return false;
        }
        boolean ret = mGestureDetectorCompat.onTouchEvent(event);
        if (!mFling && MotionEvent.ACTION_UP == event.getAction()) {
            autoSettle();
            ret = true;
        }
        return ret || super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            refreshCenter();
            invalidate();
        } else {
            if (mFling) {
                mFling = false;
                autoSettle();
            }
        }
    }

    public void setAdditionCenterMark(String additionCenterMark) {
        mAdditionCenterMark = additionCenterMark;
        calcIntervalDis();
        invalidate();
    }

    private void autoSettle() {
        int sx = getScrollX();
        float dx = mCenterIndex * mIntervalDis - sx - mMaxOverScrollDistance;
        mScroller.startScroll(sx, 0, (int) dx, 0);
        postInvalidate();
        if (mLastSelectedIndex != mCenterIndex) {
            mLastSelectedIndex = mCenterIndex;
            if (null != mOnWheelItemSelectedListener) {
                mOnWheelItemSelectedListener.onWheelItemSelected(this, mCenterIndex);
            }
        }
    }

    /**
     * limit center index in bounds.
     *
     * @param center
     * @return
     */
    private int safeCenter(int center) {
        if (center < mMinSelectableIndex) {
            center = mMinSelectableIndex;
        } else if (center > mMaxSelectableIndex) {
            center = mMaxSelectableIndex;
        }
        return center;
    }

    private void refreshCenter(int offsetX) {
        int offset = (int) (offsetX + mMaxOverScrollDistance);
        int tempIndex = Math.round(offset / mIntervalDis);
        tempIndex = safeCenter(tempIndex);
        if (mCenterIndex == tempIndex) {
            return;
        }
        mCenterIndex = tempIndex;
        if (null != mOnWheelItemSelectedListener) {
            mOnWheelItemSelectedListener.onWheelItemChanged(this, mCenterIndex);
        }
    }

    private void refreshCenter() {
        refreshCenter(getScrollX());
    }

    public void selectIndex(int index) {
        mCenterIndex = index;
        post(new Runnable() {
            @Override
            public void run() {
                scrollTo((int) (mCenterIndex * mIntervalDis - mMaxOverScrollDistance), 0);
                invalidate();
                refreshCenter();
            }
        });
    }

    public void smoothSelectIndex(int index) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        int deltaIndex = index - mCenterIndex;
        mScroller.startScroll(getScrollX(), 0, (int) (deltaIndex * mIntervalDis), 0);
        invalidate();
    }

    public int getMinSelectableIndex() {
        return mMinSelectableIndex;
    }

    public void setMinSelectableIndex(int minSelectableIndex) {
        if (minSelectableIndex > mMaxSelectableIndex) {
            minSelectableIndex = mMaxSelectableIndex;
        }
        mMinSelectableIndex = minSelectableIndex;
        int afterCenter = safeCenter(mCenterIndex);
        if (afterCenter != mCenterIndex) {
            selectIndex(afterCenter);
        }
    }

    public int getMaxSelectableIndex() {
        return mMaxSelectableIndex;
    }

    public void setMaxSelectableIndex(int maxSelectableIndex) {
        if (maxSelectableIndex < mMinSelectableIndex) {
            maxSelectableIndex = mMinSelectableIndex;
        }
        mMaxSelectableIndex = maxSelectableIndex;
        int afterCenter = safeCenter(mCenterIndex);
        if (afterCenter != mCenterIndex) {
            selectIndex(afterCenter);
        }
    }

    public List<String> getItems() {
        return mItems;
    }

    public void setItems(List<String> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }
        mItems.addAll(items);
        mMarkCount = null == mItems ? 0 : mItems.size();
        if (mMarkCount > 0) {
            mMinSelectableIndex = Math.max(mMinSelectableIndex, 0);
            mMaxSelectableIndex = Math.min(mMaxSelectableIndex, mMarkCount - 1);
        }
        mContentRectF.set(0, 0, (mMarkCount - 1) * mIntervalDis, getMeasuredHeight());
        mCenterIndex = Math.min(mCenterIndex, mMarkCount);
        calcIntervalDis();
        invalidate();
    }

    public void setTitles(HashMap<Integer, String> itemTitleMap) {
        mItemTitleMap.putAll(itemTitleMap);
    }

    public int getSelectedPosition() {
        return mCenterIndex;
    }

    public void setOnWheelItemSelectedListener(OnWheelItemSelectedListener onWheelItemSelectedListener) {
        mOnWheelItemSelectedListener = onWheelItemSelectedListener;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(false);
        }
        mFling = false;
        if (null != getParent()) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        playSoundEffect(SoundEffectConstants.CLICK);
        refreshCenter((int) (getScrollX() + e.getX() - mMaxOverScrollDistance));
        autoSettle();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float dis = distanceX;
        float scrollX = getScrollX();
        if (scrollX < mMinSelectableIndex * mIntervalDis - 2 * mMaxOverScrollDistance) {
            dis = 0;
        } else if (scrollX < mMinSelectableIndex * mIntervalDis - mMaxOverScrollDistance) {
            dis = distanceX / 4.f;
        } else if (scrollX > mContentRectF.width() - (mMarkCount - mMaxSelectableIndex - 1) * mIntervalDis) {
            dis = 0;
        } else if (scrollX > mContentRectF.width() - (mMarkCount - mMaxSelectableIndex - 1) * mIntervalDis - mMaxOverScrollDistance) {
            dis = distanceX / 4.f;
        }
        scrollBy((int) dis, 0);
        refreshCenter();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float scrollX = getScrollX();
        if (scrollX < -mMaxOverScrollDistance + mMinSelectableIndex * mIntervalDis || scrollX > mContentRectF.width() - mMaxOverScrollDistance - (mMarkCount - 1 - mMaxSelectableIndex) * mIntervalDis) {
            return false;
        } else {
            mFling = true;
            fling((int) -velocityX, 0);
            return true;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.index = getSelectedPosition();
        ss.min = mMinSelectableIndex;
        ss.max = mMaxSelectableIndex;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mMinSelectableIndex = ss.min;
        mMaxSelectableIndex = ss.max;
        selectIndex(ss.index);
        requestLayout();
    }

    public interface OnWheelItemSelectedListener {
        void onWheelItemChanged(HorizontalWheelPicker wheelView, int position);

        void onWheelItemSelected(HorizontalWheelPicker wheelView, int position);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int index;
        int min;
        int max;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            index = in.readInt();
            min = in.readInt();
            max = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(index);
            out.writeInt(min);
            out.writeInt(max);
        }

        @Override
        public String toString() {
            return "WheelView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " index=" + index + " min=" + min + " max=" + max + "}";
        }
    }
}
