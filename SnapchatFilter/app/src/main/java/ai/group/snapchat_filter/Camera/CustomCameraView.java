package ai.group.snapchat_filter.Camera;

import android.content.Context;
import android.hardware.Camera;

import org.opencv.android.JavaCameraView;

public class CustomCameraView  extends JavaCameraView {


    private boolean mWillTranspose;
    public CustomCameraView(Context context, int cameraId, boolean willTranspose) {
        super(context, cameraId);
        this.mWillTranspose = willTranspose;
    }

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

        mScale = Math.max(((float)height)/mFrameHeight, ((float)width)/mFrameWidth);
        AllocateCache();
        return true;
    }

    public boolean isRunning(){
        return mEnabled;
    }
}
