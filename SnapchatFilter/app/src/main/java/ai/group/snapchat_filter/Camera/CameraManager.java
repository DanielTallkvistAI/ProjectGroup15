package ai.group.snapchat_filter.Camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.view.ViewGroup;

import ai.group.snapchat_filter.Utils.Constants;

public class CameraManager {

    private Context mContext;

    private boolean backCameraAvailable;
    private boolean frontCameraAvailable;

    private int backCameraOrientation;
    private int frontCameraOrientation;
    private Constants.CameraPosition preferredPosition;
    private ViewGroup displayView;

    private CustomCamera camera;

    public CameraManager(Context context, ViewGroup view){
        this.internalInit(context, view, Constants.CameraPosition.Back);
    }

    public CameraManager(Context context, ViewGroup view, Constants.CameraPosition cameraPosition){
        this.internalInit(context, view, cameraPosition);
    }

    private void internalInit(Context context, ViewGroup view, Constants.CameraPosition cameraPosition){

        //Set context to the current fragment that created the camera manager
        this.mContext = context;

        //Set view where the camera feed should be displayed
        this.displayView = view;

        //set users preferred position of the camera when booting up
        this.preferredPosition = cameraPosition;

        //Search the phone for available devices (eg. back and front camera)
        //Sets the preferred position based on which devices are found
        this.getAvailableCameras();

        //Check if users preferred position is available on the device
        boolean canStartCamera = false;
        switch (this.preferredPosition){
            case Back:
                //users preferred position is back
                //check if back camera is available
                if(backCameraAvailable){
                    //back camera is available
                    canStartCamera = true;
                    break;
                }

                //Back camera is not available, set the preferred position to front
                this.preferredPosition = Constants.CameraPosition.Front;
                //check if front camera is available
                if(frontCameraAvailable){
                    //front camera is available
                    canStartCamera = true;
                    break;
                }
                //front camera is not available
                //cannot start camera because no cameras exist on the device
                canStartCamera = false;
                break;
            case Front:
                //users preferred position is front
                //check if front camera is available
                if(frontCameraAvailable){
                    //front camera is available
                    canStartCamera = true;
                    break;
                }

                //front camera is not available, set the preferred position to back
                this.preferredPosition = Constants.CameraPosition.Back;
                //check if back camera is available
                if(backCameraAvailable){
                    //back camera is available
                    canStartCamera = true;
                    break;
                }

                //back camera is not available
                //cannot start camera because no cameras exist on the device
                canStartCamera = false;
                break;
        }

        //if can start camera is false do not start a camera
        if(!canStartCamera) return;

        //create a new camera
        this.camera = new CustomCamera(
                this.displayView,
                this.preferredPosition == Constants.CameraPosition.Back ? this.backCameraOrientation : this.frontCameraOrientation,
                this.preferredPosition,
                true);

    }

    //Camera availability
    public boolean backCameraExists(){
        return backCameraAvailable;
    }
    public boolean frontCameraExists(){
        return frontCameraAvailable;
    }
    public boolean canTurnCamera(){
        return backCameraExists() && frontCameraExists();
    }
    public boolean isCameraRunning(){
        return this.camera.isCameraRunning();
    }
    private void getAvailableCameras(){
        //loop through all found devices and search for front and back camera

        for(int i = 0; i < Camera.getNumberOfCameras(); i++){
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                this.backCameraAvailable = true;
                this.backCameraOrientation = cameraInfo.orientation;
            }
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                this.frontCameraAvailable = true;
                this.frontCameraOrientation = cameraInfo.orientation;
            }
        }
    }

    //Camera actions
    public void startBackCamera(){
        if(!this.backCameraAvailable){
            //no back camera available, do nothing
            return;
        }
        this.setPreferredPosition(Constants.CameraPosition.Back);
    }
    public void startFrontCamera(){
        if(!this.frontCameraAvailable){
            //no front camera available, do nothing
            return;
        }
        this.setPreferredPosition(Constants.CameraPosition.Front);
    }
    public void startCamera(){
        if(this.camera.isCameraRunning()){
            //camera is already running, no need to start it
            return;
        }
        this.camera.startCamera();
    }
    public void stopCamera(){
        if(!this.camera.isCameraRunning()){
            //camera is not running, no need to send stop to camera
            return;
        }
        this.camera.stopCamera();
    }
    public void turnCamera(){
        //get current position of camera
        Constants.CameraPosition currentPosition = this.camera.getCameraPosition();
        if(currentPosition == Constants.CameraPosition.Back){
            //current camera is back camera
            if(!frontCameraAvailable){
                //no front camera available, do nothing
                return;
            }
            setPreferredPosition(Constants.CameraPosition.Front);
        }else{
            if(!backCameraAvailable){
                //no back camera available, do nothing
                return;
            }
            setPreferredPosition(Constants.CameraPosition.Back);
        }
    }

    //Helpers
    public Bitmap getCapturedPhoto(){
        return this.camera.getCapturedImage();
    }

    private void setPreferredPosition(Constants.CameraPosition position){
        //Set the preferred position in camera manager
        this.preferredPosition = position;
        //stop the camera before setting a preferred position
        this.camera.stopCamera();
        this.camera.setCameraPosition(position, this.preferredPosition == Constants.CameraPosition.Back ? this.backCameraOrientation : this.frontCameraOrientation);
        //Start the camera after setting a preferred position
        this.camera.startCamera();
    }

    public Constants.CameraPosition getPreferredPosition(){
        return this.preferredPosition;
    }
}
