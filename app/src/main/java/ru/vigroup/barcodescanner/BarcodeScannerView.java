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
            RectF framingArea = new RectF(framingRect);
            Rect focusArea = new Rect();

            Matrix matrix = new Matrix();
            prepareMatrix(matrix, false, getDisplayOrientation(), mPreview.getWidth(), mPreview.getHeight());
            matrix.mapRect(framingArea);
            rectFToRect(framingArea, focusArea);

            mPreview.setupFocusArea(focusArea);
        }
    }

    private void prepareMatrix(Matrix dst, boolean mirror, int displayOrientation,
                              int viewWidth, int viewHeight) {
        Matrix matrix = new Matrix();
        // Need mirror for front camera.
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(-1000, -1000);
//        matrix.invert(dst);
        dst.set(matrix);
    }

    public static void rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }

    private int getDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

}
