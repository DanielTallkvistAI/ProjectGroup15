package ai.group.snapchat_filter.Camera;

import android.annotation.SuppressLint;
import android.content.Context;

import org.opencv.android.JavaCameraView;

@SuppressLint("ViewConstructor")
public class CustomCameraView  extends JavaCameraView {

    //If the camera needs to transpose the frame.
    private boolean mWillTranspose;

    public CustomCameraView(Context context, int cameraId, boolean willTranspose) {
        super(context, cameraId);
        this.mWillTranspose = willTranspose;
    }

    //Rotate with transpose
    public void setWillRotate(boolean willRotate){
        this.mWillTranspose = willRotate;
    }

    @Override
    protected boolean initializeCamera(int width, int height) {
        if(!super.initializeCamera(width, height)){
            return false;
        }

        int tempWidth = mFrameWidth;
        int tempHeight = mFrameHeight;
        if(this.mWillTranspose){
            mFrameWidth = tempHeight;
            mFrameHeight = tempWidth;
        }else{
            mFrameWidth = tempWidth;
            mFrameHeight = tempHeight;
        }

        enableFpsMeter();

        mScale = Math.max(((float)height)/mFrameHeight, ((float)width)/mFrameWidth);
        AllocateCache();
        return true;
    }

    public boolean isRunning(){
        return mEnabled;
    }
}
