package kaganovych.ua.testfacedetector;


import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CameraPreview.class.getSimpleName();
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

            startFaceDetection();
        } catch (IOException e) {
            // left blank for now
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

            startFaceDetection();
        } catch (Exception e) {
            // intentionally left blank for a test
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    public void startFaceDetection(){
        // Try starting Face Detection
        Camera.Parameters params = mCamera.getParameters();

        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            // camera supports face detection, so can start it:
            mCamera.startFaceDetection();
        } else {
            Log.e(TAG, "The device doesn't support face detection preview feature.");
        }
    }
}
