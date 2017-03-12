package com.nmwilkinson.visiondetectorview;

import com.google.android.gms.samples.vision.ocrreader.camera.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.text.TextBlock;

/**
 * Config for the {@link CameraSource}.
 */
public class VisionDetectorConfig {
    private final DetectorType detectorType;
    private final Detector.Processor<TextBlock> ocrProcessor;
    private final Detector.Processor<Barcode> barcodeProcessor;
    private final ErrorCallback errorCallback;

    public enum DetectorType {
        OCR,
        BARCODE
    }

    public static VisionDetectorConfig createOcr(Detector.Processor<TextBlock> ocrListener, ErrorCallback errorCallback) {
        return new VisionDetectorConfig(DetectorType.OCR, ocrListener, null, errorCallback);
    }

    public static VisionDetectorConfig createBarcode(Detector.Processor<Barcode> barcodeListener, ErrorCallback errorCallback) {
        return new VisionDetectorConfig(DetectorType.BARCODE, null, barcodeListener, errorCallback);
    }

    private VisionDetectorConfig(DetectorType detectorType, Detector.Processor<TextBlock> ocrProcessor, Detector.Processor<Barcode> barcodeProcessor, ErrorCallback errorCallback) {
        this.detectorType = detectorType;
        this.ocrProcessor = ocrProcessor;
        this.barcodeProcessor = barcodeProcessor;
        this.errorCallback = errorCallback;
    }

    public DetectorType getDetectorType() {
        return detectorType;
    }

    public Detector.Processor<TextBlock> getOcrProcessor() {
        return ocrProcessor;
    }

    public Detector.Processor<Barcode> getBarcodeProcessor() {
        return barcodeProcessor;
    }

    public ErrorCallback getErrorCallback() {
        return errorCallback;
    }

    public interface ErrorCallback {
        int ERROR_DEPENDENCIES_NOT_DOWNLOADED = 1;

        void onError(int errorCode);
    }
}
