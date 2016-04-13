package ru.vigroup.barcodescanner;

import android.content.Context;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public abstract class BarcodeScannerView extends FrameLayout implements Camera.PreviewCallback {
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

        addView(mPreview);
        addView(mViewFinderView);
    }

    public void startCamera(int cameraId) {
        startCamera(CameraUtils.getCameraInstance(cameraId));
    }

    public void startCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mViewFinderView.setupViewFinder();
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

            mFramingRectInPreview = rect;
        }
        return mFramingRectInPreview;
    }

    public void setAutoFocus(boolean state) {
        if (mPreview != null) {
            mPreview.setAutoFocus(state);
        }
    }
}
