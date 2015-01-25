package kaganovych.ua.testfacedetector;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.IOException;
import java.io.OutputStream;


public class MainActivity extends ActionBarActivity {

    public final static int PICK_PHOTO_CODE = 1046;
    public Uri photoUri;
    public String path;

    private static final String TAG = "CameraApp";
    private Camera mCamera;
    private CameraPreview mPreview;
    private Button mCaptureButton;

    private Camera.FaceDetectionListener mFaceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0){
                Log.d(TAG, "Face detected.");

                Camera.Face face[] = new Camera.Face[3];
                Log.d(TAG, face.length + " face(s) detected.");

                Rect rect = face[0].rect;
                Log.d(TAG, "width: " + rect.width());
                Log.d(TAG, "height: " + rect.height());
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamera = getCameraInstance();
        mCamera.setFaceDetectionListener(mFaceDetectionListener);
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        mCaptureButton = (Button) findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.e(TAG, "Exception caught: ", e);
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            OutputStream imageFileOS;
            try {
                imageFileOS = getContentResolver().openOutputStream(uri);
                imageFileOS.write(data);
                imageFileOS.flush();
                imageFileOS.close();

                Log.d(TAG, "Photo saved" + uri.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_camera:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_PHOTO_CODE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            photoUri = data.getData();
            path = getPath(photoUri);

            setContentView(new myView(this));
        }
    }

    public String getPath(Uri uri) {
        // just some safety built in
        if (uri == null) {
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    public class myView extends View {

        private int imageWidth, imageHeight;
        private int numberOfFace = 5;
        private FaceDetector myFaceDetect;
        private FaceDetector.Face[] myFace;
        float myEyesDistance;
        int numberOfFaceDetected;

        Bitmap myBitmap;


        public myView(Context context) {
            super(context);

            BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
            BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
            myBitmap = BitmapFactory.decodeFile(path, BitmapFactoryOptionsbfo);
            imageWidth = myBitmap.getWidth();
            imageHeight = myBitmap.getHeight();
            myFace = new FaceDetector.Face[numberOfFace];
            myFaceDetect = new FaceDetector(imageWidth, imageHeight, numberOfFace);
            numberOfFaceDetected = myFaceDetect.findFaces(myBitmap, myFace);

        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onDraw(Canvas canvas) {

            canvas.drawBitmap(myBitmap, 0, 0, null);

            Paint myPaint = new Paint();
            myPaint.setColor(Color.GREEN);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(3);

            for (int i = 0; i < numberOfFaceDetected; i++) {
                FaceDetector.Face face = myFace[i];
                PointF myMidPoint = new PointF();
                face.getMidPoint(myMidPoint);
                myEyesDistance = face.eyesDistance();
                canvas.drawOval(
                        (int) (myMidPoint.x - myEyesDistance),
                        (int) (myMidPoint.y - myEyesDistance),
                        (int) (myMidPoint.x + myEyesDistance),
                        (int) (myMidPoint.y + myEyesDistance * 2),
                        myPaint);
                canvas.drawCircle(
                        (int) (myMidPoint.x - myEyesDistance / 2),
                        (int) (myMidPoint.y),
                        20,
                        myPaint);
                canvas.drawCircle(
                        (int) (myMidPoint.x + myEyesDistance / 2),
                        (int) (myMidPoint.y),
                        20,
                        myPaint);
            }
        }
    }
}
