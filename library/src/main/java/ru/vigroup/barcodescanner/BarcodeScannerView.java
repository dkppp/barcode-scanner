package ru.vigroup.barcodescanner;

import android.content.Context;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public abstract class BarcodeScannerView extends FrameLayout implements Camera.PreviewCallback {

    private CameraHandlerThread mThread = null;

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

    public void startCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        mThread.openCamera();
    }

    public void startCamera(int cameraId) {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        mThread.openCamera(cameraId);
    }

    public void startCameraOld() {
        startCameraOld(CameraUtils.getCameraInstance());
    }

    private void startCameraOld(int cameraId) {
        startCameraOld(CameraUtils.getCameraInstance(cameraId));
    }

    private void startCameraOld(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    mViewFinderView.setupViewFinder();
                    mPreview.setCamera(mCamera, BarcodeScannerView.this);
                    mPreview.initCameraPreview();
                }
            });
        }
    }

    public synchronized void stopCamera() {
        if (mCamera != null) {
            mPreview.stopCameraPreview();
            mPreview.setCamera(null, null);
            mCamera.release();
            mCamera = null;
        }
    }

    public synchronized void setOneShotCallback() {
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(this);
        }
    }

    public synchronized boolean isStopped() {
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

    private class CameraHandlerThread extends HandlerThread {
        private Handler mHandler = null;

        public CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        public void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startCameraOld();
                }
            });
        }

        public void openCamera(final int cameraId) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startCameraOld(cameraId);
                }
            });
        }
    }
}
