package ai.group.snapchat_filter.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ai.group.snapchat_filter.Camera.CameraManager;
import ai.group.snapchat_filter.R;
import ai.group.snapchat_filter.Utils.Constants;

public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";

    private LinearLayout cameraViewContainer;
    private CameraManager cameraManager;
    private File cascadeFile;

    private CascadeClassifier faceDetector;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");


                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    cascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");

                    FileOutputStream fos = new FileOutputStream(cascadeFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1){
                        fos.write(buffer, 0, bytesRead);
                    }

                    is.close();
                    fos.close();

                    faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());

                    if(faceDetector.empty()){
                        faceDetector = null;
                    }
                    else{
                        cascadeDir.delete();
                        Log.i(TAG, "FaceDetector loaded successfully");

                        //Start the camera once the view is loaded
                        startupCamera();
                    }

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.camera_main);

        this.cameraViewContainer = (LinearLayout) findViewById(R.id.camera_view_container);

    }


    @Override
    public void onPause()
    {
        super.onPause();
        if(this.cameraManager != null){
            this.cameraManager.stopCamera();
        }
    }

    @Override
    public void onStart() {
        //Initialization of classes after the view has been loaded
        //because they are dependent on that views have been loaded

        super.onStart();

    }

    @Override
    public void onResume()
    {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            try {
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(this.cameraManager != null){
            this.cameraManager.startCamera();
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void startupCamera(){

        Constants.CameraPosition tempPosition = Constants.CameraPosition.Back;

        //Initiate camera manager
        this.cameraManager = new CameraManager(CameraActivity.this, cameraViewContainer, tempPosition, faceDetector);

        if(!this.cameraManager.canTurnCamera()){
            //this.switchCameraButton.setVisibility(View.INVISIBLE);
        }

        //Update the GUI to the new filter
        this.cameraManager.startCamera();
    }

    public void flipCameraPosition(View view){
        if(this.cameraManager.canTurnCamera()){
            this.cameraManager.turnCamera();
        }
    }

}
