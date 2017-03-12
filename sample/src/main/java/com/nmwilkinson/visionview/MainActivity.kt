package com.nmwilkinson.visionview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nmwilkinson.visiondetectorview.VisionDetectorConfig.DetectorType.BARCODE
import com.nmwilkinson.visiondetectorview.VisionDetectorConfig.DetectorType.OCR
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        ocr.isChecked = true

        autofocus.isChecked = true

        ocr.setOnClickListener { if (ocr.isChecked) barcode.isChecked = false }
        barcode.setOnClickListener { if (barcode.isChecked) ocr.isChecked = false }

        start.setOnClickListener {
            val mode = if (ocr.isChecked) OCR else BARCODE
            startActivity(CaptureActivity.launchIntent(this, mode, autofocus.isChecked, flash.isChecked))
        }
    }
}
