package ru.vigroup.barcodescanner;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ViewFinderView extends View {

    public interface FramingRectChangeListener {
        void onRectChanged(RectF rect);
    }

    private static final String TAG = "ViewFinderView";

    private RectF mFramingRect = null;

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;

    private static final float LANDSCAPE_WIDTH_RATIO = 5f / 8;
    private static final float LANDSCAPE_HEIGHT_RATIO = 5f / 8;
    private static final int LANDSCAPE_MAX_FRAME_WIDTH = (int) (1920 * LANDSCAPE_WIDTH_RATIO); // = 5/8 * 1920
    private static final int LANDSCAPE_MAX_FRAME_HEIGHT = (int) (1080 * LANDSCAPE_HEIGHT_RATIO); // = 5/8 * 1080

    private static final float PORTRAIT_WIDTH_RATIO = 7f / 8;
    private static final float PORTRAIT_HEIGHT_RATIO = 3f / 8;
    private static final int PORTRAIT_MAX_FRAME_WIDTH = (int) (1080 * PORTRAIT_WIDTH_RATIO); // = 7/8 * 1080
    private static final int PORTRAIT_MAX_FRAME_HEIGHT = (int) (1920 * PORTRAIT_HEIGHT_RATIO); // = 3/8 * 1920
    private Paint mMaskPaint;
    private Paint mBorderPaint;
    private int mLineLength;
    private int mPadding;

    private FramingRectChangeListener framingRectChangeListener;

    public ViewFinderView(Context context) {
        this(context, null);
    }

    public ViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources resources = getResources();

        int maskColor = resources.getColor(R.color.viewfinder_mask);
        int borderColor = resources.getColor(R.color.viewfinder_border);
        int borderWidth = resources.getDimensionPixelSize(R.dimen.viewfinder_border_width);

        mLineLength = resources.getDimensionPixelSize(R.dimen.viewfinder_border_length);
        mPadding = resources.getDimensionPixelSize(R.dimen.viewfinder_border_padding);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BarcodeScannerView, 0, 0);

            maskColor = a.getColor(R.styleable.BarcodeScannerView_maskColor, maskColor);
            borderColor = a.getColor(R.styleable.BarcodeScannerView_borderColor, borderColor);
            borderWidth = a.getDimensionPixelSize(R.styleable.BarcodeScannerView_borderStrokeWidth, borderWidth);
            mLineLength = a.getDimensionPixelSize(R.styleable.BarcodeScannerView_borderLength, mLineLength);
            mPadding = a.getDimensionPixelSize(R.styleable.BarcodeScannerView_borderPadding, mPadding);

            a.recycle();
        }

        mMaskPaint = new Paint();
        mMaskPaint.setColor(maskColor);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(borderWidth);

        mBorderPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mBorderPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mBorderPaint.setPathEffect(new CornerPathEffect(4));   // set the path effect when they join.
        mBorderPaint.setAntiAlias(true);
    }

    public void setFramingRectChangeListener(FramingRectChangeListener framingRectChangeListener) {
        this.framingRectChangeListener = framingRectChangeListener;
    }

    public RectF getFramingRect() {
        return mFramingRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mFramingRect == null) {
            return;
        }

        drawViewFinderMask(canvas);
        drawViewFinderBorder(canvas);
    }

    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        canvas.drawRect(0, 0, width, mFramingRect.top, mMaskPaint);
        canvas.drawRect(0, mFramingRect.top, mFramingRect.left, mFramingRect.bottom, mMaskPaint);
        canvas.drawRect(mFramingRect.right, mFramingRect.top, width, mFramingRect.bottom, mMaskPaint);
        canvas.drawRect(0, mFramingRect.bottom, width, height, mMaskPaint);
    }

    public void drawViewFinderBorder(Canvas canvas) {
        Path path = new Path();
        path.moveTo(mFramingRect.left - mPadding, mFramingRect.top - mPadding + mLineLength);
        path.lineTo(mFramingRect.left - mPadding, mFramingRect.top - mPadding);
        path.lineTo(mFramingRect.left - mPadding + mLineLength, mFramingRect.top - mPadding);
        canvas.drawPath(path, mBorderPaint);

        path.moveTo(mFramingRect.left - mPadding, mFramingRect.bottom + mPadding - mLineLength);
        path.lineTo(mFramingRect.left - mPadding, mFramingRect.bottom + mPadding);
        path.lineTo(mFramingRect.left - mPadding + mLineLength, mFramingRect.bottom + mPadding);
        canvas.drawPath(path, mBorderPaint);

        path.moveTo(mFramingRect.right + mPadding, mFramingRect.top - mPadding + mLineLength);
        path.lineTo(mFramingRect.right + mPadding, mFramingRect.top - mPadding);
        path.lineTo(mFramingRect.right + mPadding - mLineLength, mFramingRect.top - mPadding);
        canvas.drawPath(path, mBorderPaint);

        path.moveTo(mFramingRect.right + mPadding, mFramingRect.bottom + mPadding - mLineLength);
        path.lineTo(mFramingRect.right + mPadding, mFramingRect.bottom + mPadding);
        path.lineTo(mFramingRect.right + mPadding - mLineLength, mFramingRect.bottom + mPadding);
        canvas.drawPath(path, mBorderPaint);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    private synchronized void updateFramingRect() {
        Point viewResolution = new Point(getWidth(), getHeight());

        int width;
        int height;
        int orientation = DisplayUtils.getScreenOrientation(getContext());

        boolean landscape = orientation != Configuration.ORIENTATION_PORTRAIT;
        if (landscape) {
            width = findDesiredDimensionInRange(LANDSCAPE_WIDTH_RATIO, viewResolution.x, MIN_FRAME_WIDTH, LANDSCAPE_MAX_FRAME_WIDTH);
            height = findDesiredDimensionInRange(LANDSCAPE_HEIGHT_RATIO, viewResolution.y, MIN_FRAME_HEIGHT, LANDSCAPE_MAX_FRAME_HEIGHT);
        } else {
            width = findDesiredDimensionInRange(PORTRAIT_WIDTH_RATIO, viewResolution.x, MIN_FRAME_WIDTH, PORTRAIT_MAX_FRAME_WIDTH);
            height = findDesiredDimensionInRange(PORTRAIT_HEIGHT_RATIO, viewResolution.y, MIN_FRAME_HEIGHT, PORTRAIT_MAX_FRAME_HEIGHT);
        }

        int leftOffset = (viewResolution.x - width) / 2;
        int topOffset = landscape ? (viewResolution.y - height) / 8 : leftOffset;

        mFramingRect = new RectF(leftOffset, topOffset, leftOffset + width, topOffset + height);

        if (framingRectChangeListener != null) {
            framingRectChangeListener.onRectChanged(mFramingRect);
        }
    }

    private static int findDesiredDimensionInRange(float ratio, int resolution, int hardMin, int hardMax) {
        int dim = (int) (ratio * resolution);
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

}
