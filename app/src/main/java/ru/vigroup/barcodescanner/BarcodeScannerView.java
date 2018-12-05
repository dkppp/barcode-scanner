package ru.vigroup.barcodescanner;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.FrameLayout;

public abstract class BarcodeScannerView extends FrameLayout implements Camera.PreviewCallback, ViewFinderView.FramingRectChangeListener {

    private Camera mCamera;
    private CameraPreview mPreview;
    private ViewFinderView mViewFinderView;
    private RectF mFramingRectInPreview;

    public BarcodeScannerView(Context context) {
        super(context);
        setupLayout(context, null);
    }

    public BarcodeScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupLayout(context, attributeSet);
    }

    public void setupLayout(Context context, AttributeSet attrs) {
        mPreview = new CameraPreview(getContext());
        mViewFinderView = new ViewFinderView(getContext(), attrs);
        mViewFinderView.setFramingRectChangeListener(this);

        addView(mPreview);
        addView(mViewFinderView);
    }

    public void startCamera(int cameraId) {
        startCamera(CameraUtils.getCameraInstance(cameraId));
    }

    public void startCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mPreview.setCamera(mCamera, this);
            mPreview.initCameraPreview();
        }
    }

    public void startCamera() {
        startCamera(CameraUtils.getCameraInstance());
    }

    public void stopCamera() {
        if (mCamera != null) {
            mPreview.stopCameraPreview();
            mPreview.setCamera(null, null);
            mCamera.release();
            mCamera = null;
        }
    }

    public boolean isStopped() {
        return mCamera == null;
    }

    public synchronized RectF getFramingRectInPreview(int previewWidth, int previewHeight) {
        if (mFramingRectInPreview == null) {
            mFramingRectInPreview = getFramingRect(previewWidth, previewHeight);
        }
        return mFramingRectInPreview;
    }

    private RectF getFramingRect(int previewWidth, int previewHeight) {
        RectF framingRect = mViewFinderView.getFramingRect();
        int viewFinderViewWidth = mPreview.getWidth();
        int viewFinderViewHeight = mPreview.getHeight();
        if (framingRect == null || viewFinderViewWidth == 0 || viewFinderViewHeight == 0) {
            return null;
        }

        RectF rect = new RectF();

        rect.left = framingRect.left * previewWidth / viewFinderViewWidth;
        rect.top = framingRect.top * previewHeight / viewFinderViewHeight;
        rect.right = framingRect.right * previewWidth / viewFinderViewWidth;
        rect.bottom = framingRect.bottom * previewHeight / viewFinderViewHeight;
        return rect;
    }

    public void setAutoFocus(boolean state) {
        if (mPreview != null) {
            mPreview.setAutoFocus(state);
        }
    }

    @Override
    public void onRectChanged(RectF framingRect) {
        mFramingRectInPreview = null;
        if (mPreview != null) {
            RectF rectF = getFramingRectInPreview(2000, 2000);
            float x = rectF.centerX();
            float y = rectF.centerY();

            Matrix matrix = new Matrix();

            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int rotation = display.getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    matrix.setRotate(90, x, y);
                    break;
                case Surface.ROTATION_90:
                    break;
                case Surface.ROTATION_180:
                    matrix.setRotate(270, x, y);
                    break;
                case Surface.ROTATION_270:
                    matrix.setRotate(180, x, y);
                    break;
            }
            matrix.mapRect(rectF);

            int left = (int) (rectF.left - 1000);
            int top = (int) (rectF.top - 1000);
            int right = (int) (rectF.right - 1000);
            int bottom = (int) (rectF.bottom - 1000);

            mPreview.setupFocusArea(new Rect(left, top, right, bottom));

        }
    }

}
