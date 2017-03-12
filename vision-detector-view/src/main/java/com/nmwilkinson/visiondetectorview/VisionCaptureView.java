package com.nmwilkinson.visiondetectorview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.samples.vision.ocrreader.camera.CameraSource;
import com.google.android.gms.samples.vision.ocrreader.camera.CameraSourcePreview;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

import static com.nmwilkinson.visiondetectorview.VisionDetectorConfig.ErrorCallback.ERROR_DEPENDENCIES_NOT_DOWNLOADED;

/**
 * Code taken from OcrCaptureActivity from https://github.com/googlesamples/android-vision.git, put into a View, overlay removed, and easier support for different detection types.
 */
public class VisionCaptureView extends FrameLayout {
    private static final String TAG = VisionCaptureView.class.getSimpleName();

    private final ScaleGestureDetector mScaleGestureDetector;
    private final CameraSourcePreview mPreview;

    private CameraSource mCameraSource;

    public VisionCaptureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        mPreview = new CameraSourcePreview(context);
        addView(mPreview, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void startCapturing() {
        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (final IOException | SecurityException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public void stopCapturing() {
        mPreview.stop();
    }

    public void shutdown() {
        mPreview.release();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent e) {
        return mScaleGestureDetector.onTouchEvent(e) || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    public void createCameraSource(final Context appContext, VisionCameraConfig cameraConfig, VisionDetectorConfig detectorConfig) {
        final Detector<?> detector = createDetector(appContext, detectorConfig);
        // Creates and starts the camera.
        mCameraSource = new CameraSource.Builder(appContext, detector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(cameraConfig.getCaptureWidth(), cameraConfig.getCaptureHeight())
                .setRequestedFps(cameraConfig.getFps())
                .setFlashMode(cameraConfig.useFlash() ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setFocusMode(cameraConfig.autoFocus() ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                .build();
    }

    private Detector<?> createDetector(Context appContext, VisionDetectorConfig detectorConfig) {
        final Detector<?> detector;

        switch (detectorConfig.getDetectorType()) {
            case OCR: {
                final Detector.Processor<TextBlock> ocrListener = detectorConfig.getOcrProcessor();

                // A text recognizer is created to find text.  An associated processor instance
                // is set to receive the text recognition results and display graphics for each text block
                // on screen.
                final TextRecognizer textRecognizer = new TextRecognizer.Builder(appContext).build();
                textRecognizer.setProcessor(ocrListener);

                if (!textRecognizer.isOperational()) {
                    // Note: The first time that an app using a Vision API is installed on a
                    // device, GMS will download a native libraries to the device in order to do detection.
                    // Usually this completes before the app is run for the first time.  But if that
                    // download has not yet completed, then the above call will not detect any text,
                    // barcodes, or faces.
                    //
                    // isOperational() can be used to check if the required native libraries are currently
                    // available.  The detectors will automatically become operational once the library
                    // downloads complete on device.
                    Log.w(TAG, "Detector dependencies are not yet available.");

                    // Check for low storage.  If there is low storage, the native library will not be
                    // downloaded, so detection will not become operational.
                    final IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                    final boolean hasLowStorage = getContext().registerReceiver(null, lowstorageFilter) != null;

                    VisionDetectorConfig.ErrorCallback errorCallback = detectorConfig.getErrorCallback();
                    if (hasLowStorage && errorCallback != null) {
                        errorCallback.onError(ERROR_DEPENDENCIES_NOT_DOWNLOADED);
                    }
                }

                detector = textRecognizer;
                break;
            }
            case BARCODE: {
                final Detector.Processor<Barcode> barcodeProcessor = detectorConfig.getBarcodeProcessor();
                if (barcodeProcessor == null) {
                    throw new IllegalStateException("Must specify " + Detector.Processor.class.getSimpleName() + " for barcode detection");
                }

                final BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(appContext).build();
                barcodeDetector.setProcessor(barcodeProcessor);

                if (!barcodeDetector.isOperational()) {
                    // Note: The first time that an app using the barcode or face API is installed on a
                    // device, GMS will download a native libraries to the device in order to do detection.
                    // Usually this completes before the app is run for the first time.  But if that
                    // download has not yet completed, then the above call will not detect any barcodes
                    // and/or faces.
                    //
                    // isOperational() can be used to check if the required native libraries are currently
                    // available.  The detectors will automatically become operational once the library
                    // downloads complete on device.
                    Log.w(TAG, "Detector dependencies are not yet available.");

                    // Check for low storage.  If there is low storage, the native library will not be
                    // downloaded, so detection will not become operational.
                    IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                    boolean hasLowStorage = getContext().registerReceiver(null, lowstorageFilter) != null;

                    VisionDetectorConfig.ErrorCallback errorCallback = detectorConfig.getErrorCallback();

                    if (hasLowStorage && errorCallback != null) {
                        errorCallback.onError(ERROR_DEPENDENCIES_NOT_DOWNLOADED);
                    }
                }
                detector = barcodeDetector;
                break;
            }
            default: {
                throw new IllegalStateException("Unhandled " + VisionDetectorConfig.DetectorType.class.getSimpleName() + " specified: " + detectorConfig.getDetectorType());
            }
        }

        return detector;
    }

    /**
     * Takes a picture
     */
    public void takePicture(final CameraSource.ShutterCallback shutter, final CameraSource.PictureCallback jpeg) {
        mCameraSource.takePicture(shutter, jpeg);
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 *                 retrieve extended info about event state.
         *                 *
         * @return Whether or not the detector should consider this event
         * * as handled. If an event was not handled, the detector
         * * will continue to accumulate movement until an event is
         * * handled. This can be useful if an application, for example,
         * * only wants to update scaling factors if the change is
         * * greater than 0.01.
         */

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 *                 retrieve extended info about event state.
         *                 *
         * @return Whether or not the detector should continue recognizing
         * * this gesture. For example, if a gesture is beginning
         * * with a focal point outside of a region where it makes
         * * sense, onScaleBegin() may return false to ignore the
         * * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p>
         * <p>
         * Once a scale has ended, [ScaleGestureDetector.getFocusX]
         * and [ScaleGestureDetector.getFocusY] will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(final ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }
}
