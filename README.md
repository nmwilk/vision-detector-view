# Vision Detector View

Based on https://github.com/googlesamples/android-vision

Code composed into a View for better re-usability. Added easier usage of different detections types (OCR, Barcode). Added easier camera configuration.

Sample app contains example usage (in kotlin).

Usage
-----

Import via Gradle

    compile 'com.nmwilkinson:vision-detector-view:1.0.1'


1) Add a `VisionDetectorView` to your layout.

2) Configure it by calling `createCameraSource()`, supplying a `VisionCameraConfig` and `VisionCaptureConfig`.

3) Receive detections via the `Detector.Processor` specified in your `VisionCaptureConfig`.

4) Start, stop, and destroy the session in your `Activity`'s `onResume()`, `onPause()`, and `onDestroy()` respectively.

Notes
-----
a) You'll need to request the `CAMERA` permission on Android 6 and above prior to configuration.

b) The device needs Google Play Services installed to use Google Mobile Vision.
 
<a href='https://bintray.com/nmwilkinson/maven/vision-detector-view?source=watch' alt='Get automatic notifications about new "vision-detector-view" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_greyscale.png'></a>
 
License
-------
Original work Copyright 2015 Google, Inc. All Rights Reserved.

Modified work Copyright 2017 Neil Wilkinson.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
