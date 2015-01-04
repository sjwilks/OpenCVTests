package com.uaventure.simon.blobdetect;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "CBD::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private Scalar               BLUE_RGB;
    private Scalar               BLUE_MIN;
    private Scalar               BLUE_MAX;
    private Scalar               RED_RGB;
    private Scalar               RED_MIN;
    private Scalar               RED_MAX;
    private Scalar               GREEN_RGB;
    private Scalar               GREEN_MIN;
    private Scalar               GREEN_MAX;
    private Scalar               YELLOW_RGB;
    private Scalar               YELLOW_MIN;
    private Scalar               YELLOW_MAX;
    private Scalar               BLACK_RGB;
    private Scalar               WHITE_RGB;
    private Rect                 mRect;
    private int                  count = 0;

    private JavaCamResView mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(com.uaventure.simon.blobdetect.R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (JavaCamResView) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,255,0,255);

        // Colour scalars
        BLUE_RGB = new Scalar(0, 0, 255);
        BLUE_MIN = new Scalar(120, 100, 100);
        BLUE_MAX = new Scalar(179, 255, 255);
        RED_RGB = new Scalar(255, 0, 0);
        RED_MIN = new Scalar(0, 100, 100);
        RED_MAX = new Scalar(12, 255, 255);
        GREEN_RGB = new Scalar(0, 255, 0);
        GREEN_MIN = new Scalar(85, 100, 100);
        GREEN_MAX = new Scalar(95, 255, 255);
        YELLOW_RGB = new Scalar(255, 255, 0);
        YELLOW_MIN = new Scalar(10, 100, 100);
        YELLOW_MAX = new Scalar(40, 255, 255);
        BLACK_RGB = new Scalar(0, 0, 0);
        WHITE_RGB = new Scalar(255, 255, 255);

        // Define the target rectangle on the preview screen.
        Log.i(TAG, "W: " + width + " H: " + height);
        int cols = mRgba.cols();
        int rows = mRgba.rows();
        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
        int x = 640-100;// - xOffset;
        int y = 360-100;// - yOffset;
        mRect = new Rect();
        mRect.x = x;//(x>4) ? x-4 : 0;
        mRect.y = y;//(y>4) ? y-4 : 0;
        mRect.width = 100;//(x+4 < cols) ? x + 4 - mRect.x : cols - mRect.x;
        mRect.height = 100;//(y+4 < rows) ? y + 4 - mRect.y : rows - mRect.y;

        // Set the camera properties.
        mOpenCvCameraView.setFocusMode(5);
        mOpenCvCameraView.setWhiteBalance(1);
        mOpenCvCameraView.setExposure(0);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        Mat touchedRegionRgba = mRgba.submat(mRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = mRect.width * mRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        //Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Core.rectangle(mRgba, mRect.tl(), mRect.br(), new Scalar(255, 255, 255), 0, 8, 0);

        Mat touchedRegionRgba = mRgba.submat(mRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        Mat mask = new Mat();

        Core.inRange(touchedRegionHsv, GREEN_MIN, GREEN_MAX, mask);

        boolean ledOn = false;
        Scalar maskScalar = Core.sumElems(mask);
        for (int i = 0; i < maskScalar.val.length; ++i) {
            if (maskScalar.val[i] > 0.0) {
                ledOn = true;
                break;
            }
        }
        mBlobColorRgba = ledOn ? GREEN_RGB : BLACK_RGB;

        if (!ledOn) {
            Core.inRange(touchedRegionHsv, RED_MIN, RED_MAX, mask);

            ledOn = false;
            maskScalar = Core.sumElems(mask);
            for (int i = 0; i < maskScalar.val.length; ++i) {
                if (maskScalar.val[i] > 0.0) {
                    ledOn = true;
                    break;
                }
            }

            mBlobColorRgba = ledOn ? RED_RGB : BLACK_RGB;
        }

        if (!ledOn) {
            Core.inRange(touchedRegionHsv, BLUE_MIN, BLUE_MAX, mask);

            ledOn = false;
            maskScalar = Core.sumElems(mask);
            for (int i = 0; i < maskScalar.val.length; ++i) {
                if (maskScalar.val[i] > 0.0) {
                    ledOn = true;
                    break;
                }
            }

            mBlobColorRgba = ledOn ? BLUE_RGB : BLACK_RGB;
        }

        if (!ledOn) {
            Core.inRange(touchedRegionHsv, YELLOW_MIN, YELLOW_MAX, mask);

            ledOn = false;
            maskScalar = Core.sumElems(mask);
            for (int i = 0; i < maskScalar.val.length; ++i) {
                if (maskScalar.val[i] > 0.0) {
                    ledOn = true;
                    break;
                }
            }

            mBlobColorRgba = ledOn ? YELLOW_RGB : BLACK_RGB;
        }

        // Calculate average color of the selected region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = mRect.width * mRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++) {
            mBlobColorHsv.val[i] /= pointCount;
        }

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mask.release();
        touchedRegionRgba.release();
        touchedRegionHsv.release();

        //mDetector.process(mRgba);
        //List<MatOfPoint> contours = mDetector.getContours();
        //Log.e(TAG, "Contours count: " + contours.size());
        //Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

        //mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        //Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
        //        ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        // Display colour box in the top left corner.

        Mat colorLabel = mRgba.submat(4, 68, 4, 68);
        colorLabel.setTo(mBlobColorRgba);

        Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
        mSpectrum.copyTo(spectrumLabel);

        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
}
