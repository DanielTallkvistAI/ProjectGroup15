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

    //Layout content that displays the CameraView
    private LinearLayout cameraViewContainer;

    //CameraManager that control the cameras functionality
    private CameraManager cameraManager;

    //File to load in faceDetection model
    private File cascadeFile;

    //CascadeClassifier to use for face detection with OpenCV
    private CascadeClassifier faceDetector;


    //Load in OpenCV and models from file system
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    //Get Resources
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    cascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");
                    FileOutputStream fos = new FileOutputStream(cascadeFile);

                    //Read in contents of resource
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1){

                        //Write contents to in system file.
                        fos.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    fos.close();

                    //Set Classifier
                    faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());

                    //Check results of operation
                    if(faceDetector.empty()){
                        faceDetector = null;
                        Log.i(TAG, "FaceDetector loaded failed.");
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


    //Create Android View
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.camera_main);

        this.cameraViewContainer = (LinearLayout) findViewById(R.id.camera_view_container);

    }

    //When the app is minimized we want to stop the camera.
    @Override
    public void onPause()
    {
        super.onPause();
        if(this.cameraManager != null){
            this.cameraManager.stopCamera();
        }
    }

    //Check that OpenCV is loaded when resuming to use the app.
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

    //Start to use the camera
    private void startupCamera(){

        Constants.CameraPosition tempPosition = Constants.CameraPosition.Back;

        //Initiate camera manager with faceDetector
        this.cameraManager = new CameraManager(cameraViewContainer, tempPosition, faceDetector);

        if(!this.cameraManager.canTurnCamera()){
            //this.switchCameraButton.setVisibility(View.INVISIBLE);
        }

        //Update the GUI to the new filter
        this.cameraManager.startCamera();
    }

    //Turn the camera position from rear to front (or vice versa)
    public void flipCameraPosition(View view){
        if(this.cameraManager.canTurnCamera()){
            this.cameraManager.turnCamera();
        }
    }

}
