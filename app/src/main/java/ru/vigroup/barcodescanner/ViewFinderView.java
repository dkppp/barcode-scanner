package ru.vigroup.barcodescanner;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ViewFinderView extends View {
    private static final String TAG = "ViewFinderView";

    private RectF mFramingRect;

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

    public ViewFinderView(Context context) {
        super(context);
    }

    public ViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setupViewFinder() {
        updateFramingRect();
        invalidate();
    }

    public RectF getFramingRect() {
        return mFramingRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mFramingRect == null) {
            return;
        }

        /*
        Paint paint = new Paint();
        Resources resources = getResources();
        paint.setColor(resources.getColor(R.color.transparent));
        paint.setAntiAlias(true);
        canvas.drawRoundRect(mFramingRect, 4f, 4f, paint);
        */

        drawViewFinderMask(canvas);
        drawViewFinderBorder(canvas);
    }

    public void drawViewFinderMask(Canvas canvas) {
        Paint paint = new Paint();
        Resources resources = getResources();
        paint.setColor(resources.getColor(R.color. viewfinder_mask));

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        canvas.drawRect(0, 0, width, mFramingRect.top, paint);
        canvas.drawRect(0, mFramingRect.top, mFramingRect.left, mFramingRect.bottom, paint);
        canvas.drawRect(mFramingRect.right, mFramingRect.top, width, mFramingRect.bottom, paint);
        canvas.drawRect(0, mFramingRect.bottom, width, height, paint);
    }

    public void drawViewFinderBorder(Canvas canvas) {
        Paint paint = new Paint();
        Resources resources = getResources();
        paint.setColor(resources.getColor(R.color.viewfinder_border));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.viewfinder_border_width));

        paint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        paint.setPathEffect(new CornerPathEffect(4));   // set the path effect when they join.
        paint.setAntiAlias(true);

        int lineLength = resources.getDimensionPixelSize(R.dimen.viewfinder_border_length);
        int padding = resources.getDimensionPixelSize(R.dimen.viewfinder_border_padding);

        Path path = new Path();
        path.moveTo(mFramingRect.left - padding, mFramingRect.top - padding + lineLength);
        path.lineTo(mFramingRect.left - padding, mFramingRect.top - padding);
        path.lineTo(mFramingRect.left - padding + lineLength, mFramingRect.top - padding);
        canvas.drawPath(path, paint);

        path.moveTo(mFramingRect.left - padding, mFramingRect.bottom + padding - lineLength);
        path.lineTo(mFramingRect.left - padding, mFramingRect.bottom + padding);
        path.lineTo(mFramingRect.left - padding + lineLength, mFramingRect.bottom + padding);
        canvas.drawPath(path, paint);

        path.moveTo(mFramingRect.right + padding, mFramingRect.top - padding + lineLength);
        path.lineTo(mFramingRect.right + padding, mFramingRect.top - padding);
        path.lineTo(mFramingRect.right + padding - lineLength, mFramingRect.top - padding);
        canvas.drawPath(path, paint);

        path.moveTo(mFramingRect.right + padding, mFramingRect.bottom + padding - lineLength);
        path.lineTo(mFramingRect.right + padding, mFramingRect.bottom + padding);
        path.lineTo(mFramingRect.right + padding - lineLength, mFramingRect.bottom + padding);
        canvas.drawPath(path, paint);

    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    public synchronized void updateFramingRect() {
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
