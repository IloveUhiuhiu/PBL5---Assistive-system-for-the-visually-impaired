package com.example.gpsapp.utils;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.example.gpsapp.R;


public class CustomCaptureActivity extends CaptureActivity {
    protected DecoratedBarcodeView initializeDecoratedBarcodeView() {
        setContentView(R.layout.custom_scanner_layout);
        return findViewById(R.id.zxing_barcode_scanner);
    }
}