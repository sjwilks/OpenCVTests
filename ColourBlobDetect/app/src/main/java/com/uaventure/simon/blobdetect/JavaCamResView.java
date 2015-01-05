package com.uaventure.simon.blobdetect;

/**
 * Created by simon on 02.01.15.
 */
import java.util.List;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

public class JavaCamResView extends JavaCameraView {
    private static final String TAG = "CBD::JavaCamResView";

    public JavaCamResView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<Camera.Size> getResolutionList() {
        return  mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Camera.Size resolution) {
        disconnectCamera();
        connectCamera((int)resolution.width, (int)resolution.height);
    }

    public void setWhiteBalance(int type) {
        Camera.Parameters params = mCamera.getParameters();
        List<String> BalModes = params.getSupportedWhiteBalance();

        switch (type) {
            case 0:
                if (BalModes.contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                    Log.e(TAG, "Auto white balance set");
                } else {
                    Log.e(TAG, "Auto white balance not supported");
                }
                break;
            case 1:
                if (BalModes.contains(Camera.Parameters.WHITE_BALANCE_DAYLIGHT)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
                    Log.e(TAG, "Daylight white balance set");
                } else{
                    Log.e(TAG, "Daylight white balance not supported");
                }
                break;
            case 2:
                if (BalModes.contains(Camera.Parameters.WHITE_BALANCE_INCANDESCENT)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
                    Log.e(TAG, "Incandescent white balance set");
                } else {
                    Log.e(TAG, "Incandescent white balance not supported");
                }
                break;
        }
        params.setAutoWhiteBalanceLock(true);
        mCamera.setParameters(params);
    }

    public void setExposure(int type) {
        Camera.Parameters params = mCamera.getParameters();
        int minExposure = params.getMinExposureCompensation();
        int maxExposure = params.getMaxExposureCompensation();

        params.set("max-brightness", 1);

        switch (type) {
            case 0:
                params.setExposureCompensation(minExposure);
                Log.e(TAG, "Min exposure set: " + minExposure);
                break;
            case 1:
                params.setExposureCompensation(maxExposure);
                Log.e(TAG, "Max exposure set: " + maxExposure);
                break;
        }
        // Lock exposure so it will not try to adjust due to flashing LEDs.
        params.setAutoExposureLock(true);

        mCamera.setParameters(params);
        Log.e(TAG, mCamera.getParameters().get("max-brightness"));
    }

    public void setFocusMode(int type) {
        Camera.Parameters params = mCamera.getParameters();
        List<String> FocusModes = params.getSupportedFocusModes();

        switch (type) {
            case 0:
                if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                else
                    Log.e(TAG, "Auto Mode not supported");
                break;
            case 1:
                if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                else
                    Log.e(TAG, "Continuous Mode not supported");
                break;
            case 2:
                if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_EDOF))
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_EDOF);
                else
                    Log.e(TAG, "EDOF Mode not supported");
                break;
            case 3:
                if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED))
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                else
                    Log.e(TAG, "Fixed Mode not supported");
                break;
            case 4:
                if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY))
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                else
                    Log.e(TAG, "Infinity Mode not supported");
                break;
            case 5:
                if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO))
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                else
                    Log.e(TAG, "Macro Mode not supported");
                break;
        }

        mCamera.setParameters(params);
    }

    public void setFlashMode (int type) {
        Camera.Parameters params = mCamera.getParameters();
        List<String> FlashModes = params.getSupportedFlashModes();

        switch (type) {
            case 0:
                if (FlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO))
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                else
                    Log.e(TAG, "Auto Mode not supported");
                break;
            case 1:
                if (FlashModes.contains(Camera.Parameters.FLASH_MODE_OFF))
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                else
                    Log.e(TAG, "Off Mode not supported");
                break;
            case 2:
                if (FlashModes.contains(Camera.Parameters.FLASH_MODE_ON))
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                else
                    Log.e(TAG, "On Mode not supported");
                break;
            case 3:
                if (FlashModes.contains(Camera.Parameters.FLASH_MODE_RED_EYE))
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_RED_EYE);
                else
                    Log.e(TAG, "Red Eye Mode not supported");
                break;
            case 4:
                if (FlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH))
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                else
                    Log.e(TAG, "Torch Mode not supported");
                break;
        }

        mCamera.setParameters(params);
    }

    public Camera.Size getResolution() {

        Camera.Parameters params = mCamera.getParameters();

        Camera.Size s = params.getPreviewSize();
        return s;
    }
}
