package com.nmwilkinson.vision_detector_view;

import com.google.android.gms.samples.vision.ocrreader.camera.CameraSource;

/**
 * Config for the {@link CameraSource}
 */
public class VisionCameraConfig {
    private final boolean useFlash;
    private final boolean autoFocus;
    private final float fps;
    private final int captureWidth;
    private final int captureHeight;

    private VisionCameraConfig(boolean useFlash, boolean autoFocus, float fps, int captureWidth, int captureHeight) {
        this.useFlash = useFlash;
        this.autoFocus = autoFocus;
        this.fps = fps;
        this.captureWidth = captureWidth;
        this.captureHeight = captureHeight;
    }

    public boolean useFlash() {
        return useFlash;
    }

    public boolean autoFocus() {
        return autoFocus;
    }

    public float getFps() {
        return fps;
    }

    public int getCaptureWidth() {
        return captureWidth;
    }

    public int getCaptureHeight() {
        return captureHeight;
    }

    public static class Builder {
        private boolean useFlash = false;
        private boolean autoFocus = true;
        private float fps = 2.0f;
        private int captureWidth = 1280;
        private int captureHeight = 1024;

        public Builder setUseFlash(final boolean useFlash) {
            this.useFlash = useFlash;
            return this;
        }

        public Builder setAutoFocus(final boolean autoFocus) {
            this.autoFocus = autoFocus;
            return this;
        }

        public Builder setFps(final float fps) {
            this.fps = fps;
            return this;
        }

        public Builder setCaptureWidth(final int captureWidth) {
            this.captureWidth = captureWidth;
            return this;
        }

        public Builder setCaptureHeight(final int captureHeight) {
            this.captureHeight = captureHeight;
            return this;
        }

        public VisionCameraConfig build() {
            return new VisionCameraConfig(useFlash, autoFocus, fps, captureWidth, captureHeight);
        }
    }
}
