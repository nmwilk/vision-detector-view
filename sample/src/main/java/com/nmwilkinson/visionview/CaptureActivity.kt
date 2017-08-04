package com.nmwilkinson.visionview

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.text.TextBlock
import com.nmwilkinson.visiondetectorview.VisionCameraConfig
import com.nmwilkinson.visiondetectorview.VisionDetectorConfig
import kotlinx.android.synthetic.main.activity_capture.*

class CaptureActivity : AppCompatActivity(), VisionDetectorConfig.ErrorCallback {

    private lateinit var captureViewConfig: VisionCameraConfig

    private val detectionSet = HashMap<String, Int>()

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        setTitle(if (getDetectorType() == VisionDetectorConfig.DetectorType.OCR) R.string.ocr else R.string.barcode)

        takePicture.setOnClickListener {
            capture.takePicture({
            }, { imageData ->
                val message = "${resources.getString(R.string.picture_taken)} of size ${imageData.size / 1024}KB"
                Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show()
            })
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            captureViewConfig = VisionCameraConfig.Builder()
                    .setAutoFocus(intent.getBooleanExtra(OPTION_AUTOFOCUS, true))
                    .setUseFlash(intent.getBooleanExtra(OPTION_FLASH, true))
                    .setCaptureWidth(capture.width)
                    .setCaptureHeight(capture.height)
                    .setFps(2.0f)
                    .build()

            // Check for the camera permission before accessing the camera.  If the
            // permission is not granted yet, request permission.
            val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            if (rc == PackageManager.PERMISSION_GRANTED) {
                capture.createCameraSource(applicationContext, captureViewConfig, createDetectorConfig())
                startCameraSource()
            } else {
                requestCameraPermission()
            }
        } else {
            capture.stopCapturing()
        }
    }

    private val ocrProcessor = object : Detector.Processor<TextBlock> {
        override fun receiveDetections(detections: Detector.Detections<TextBlock>?) {
            detections?.let {
                for (i in 0 until it.detectedItems.size()) {
                    addToResults(it.detectedItems.valueAt(i).value, 10)
                }
            }
        }

        override fun release() {
        }
    }

    private val barcodeProcessor = object : Detector.Processor<Barcode> {
        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            detections?.let {
                for (i in 0 until it.detectedItems.size()) {
                    addToResults(it.detectedItems.valueAt(i).displayValue, 1)
                }
            }
        }

        override fun release() {
        }
    }

    private fun addToResults(text: String, maxResults: Int) {
        val count = detectionSet[text] ?: 0
        detectionSet[text] = count + 1
        updateResultsView(maxResults)
    }

    /* show the top n most detected strings */
    private fun updateResultsView(maxResults: Int) {
        val concatedValues = detectionSet.keys.sortedByDescending { detectionSet[it] }.take(maxResults).fold(StringBuffer()) {
            buffer, value ->
            buffer.append("$value ")
            buffer
        }
        handler.post { results.text = concatedValues.toString() }
    }

    private fun createDetectorConfig(): VisionDetectorConfig {
        return when (getDetectorType()) {
            VisionDetectorConfig.DetectorType.OCR -> VisionDetectorConfig.createOcr(ocrProcessor, this)
            VisionDetectorConfig.DetectorType.BARCODE -> VisionDetectorConfig.createBarcode(barcodeProcessor, this)
            else -> throw IllegalStateException("Unhandled detector type ${getDetectorType()}")
        }
    }

    private fun getDetectorType() = VisionDetectorConfig.DetectorType.values()[intent.getIntExtra(OPTION_MODE, 0)]

    override fun onDestroy() {
        super.onDestroy()
        capture.shutdown()
    }

    override fun onError(errorCode: Int) {
        val message = when (errorCode) {
            VisionDetectorConfig.ErrorCallback.ERROR_DEPENDENCIES_NOT_DOWNLOADED -> R.string.low_storage_error
            else -> R.string.unknown_error
        }
        Snackbar.make(capture, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, {})
                .show()
    }

    private fun requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission")

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }

        Snackbar.make(capture, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, {
                    ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
                })
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode)
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source")
            // We have permission, so create the camerasource
            capture.createCameraSource(applicationContext, captureViewConfig, createDetectorConfig())
            return
        }

        Log.e(TAG, "Permission not granted: results len = ${grantResults.size} Result code = " + if (grantResults.isNotEmpty()) grantResults[0] else "(empty)")

        val listener = DialogInterface.OnClickListener { _, _ -> finish() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.app_name))
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show()
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    @Throws(SecurityException::class)
    private fun startCameraSource() {
        // Check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

        capture.startCapturing()
    }

    companion object {
        private const val OPTION_MODE = "4t5yflr"
        private const val OPTION_AUTOFOCUS = "gb90voi3kl"
        private const val OPTION_FLASH = "n9iok4d"

        private val TAG = CaptureActivity::class.java.simpleName

        private val permissions = arrayOf(Manifest.permission.CAMERA)
        private val RC_HANDLE_CAMERA_PERM = 2
        private val RC_HANDLE_GMS = 9001

        fun launchIntent(context: Context, mode: VisionDetectorConfig.DetectorType, autofocus: Boolean, flash: Boolean): Intent {
            val intent = Intent(context, CaptureActivity::class.java)
            intent.putExtra(OPTION_MODE, mode.ordinal)
            intent.putExtra(OPTION_AUTOFOCUS, autofocus)
            intent.putExtra(OPTION_FLASH, flash)
            return intent
        }
    }
}
