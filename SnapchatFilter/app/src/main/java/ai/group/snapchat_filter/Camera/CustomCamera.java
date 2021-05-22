package ai.group.snapchat_filter.Camera;

import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import ai.group.snapchat_filter.Utils.Constants;

public class CustomCamera implements CameraBridgeViewBase.CvCameraViewListener {

    //Current captured image as a OpenCV Mat (Matrix)
    private Mat mCapturedImage;

    //Content Container in Android view
    private ViewGroup displayView;

    //Camera View with the camera
    private CustomCameraView cameraView;

    //Orientation of the camera eg. 90 / 180 / 270
    public int cameraOrientation;

    //Position of the camera front / rear
    private Constants.CameraPosition cameraPosition;

    //Face classifier
    private CascadeClassifier mFaceDetector;

    //Constructor
    public CustomCamera(ViewGroup view, int orientation, Constants.CameraPosition position, CascadeClassifier faceDetector) {

        this.displayView = view;
        this.cameraOrientation = orientation;
        this.cameraPosition = position;
        this.mFaceDetector = faceDetector;
        this.cameraView = new CustomCameraView(this.displayView.getContext(), this.cameraPosition.toInt(), this.cameraOrientation % 180 != 0);
        this.cameraView.setCvCameraViewListener(this);
        this.cameraView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.cameraView.enableView();
        this.displayView.addView(cameraView);
    }

    // <---- CameraListener Overrides

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.mCapturedImage = new Mat(height, width, CvType.CV_8UC4, Scalar.all(0));
    }

    @Override
    public void onCameraViewStopped() {
        this.mCapturedImage.release();
    }

    //Callback for every camera frame
    //Process the image and add a mustache.
    @Override
    public Mat onCameraFrame(Mat inputFrame) {

        //Rotate input image
        rotateImageByCamera(inputFrame);

        //return result after finding a face and placing mustache on the correct spot
        return findFacesOnImageAndPlaceMustache();
    }

    //CameraListener Overrides ---->

    //Depending on which phone is used the camera sensor is different, thus need to rotate to be viewed correctly by the OpenCV camera.
    private void rotateImageByCamera(Mat inputFrame){

        Mat transposedImage = inputFrame.t();
        Mat rotatedImage = new Mat(transposedImage.rows(), transposedImage.cols(), transposedImage.type());

        switch (this.cameraPosition) {
            case Back:

                if (this.cameraOrientation == 90) {

                    Core.flip(transposedImage, rotatedImage, 1);

                } else if (this.cameraOrientation == 270) {

                    Core.flip(transposedImage, rotatedImage, 0);

                }

                break;
            case Front:

                if (this.cameraOrientation == 90) {

                    Core.flip(transposedImage, rotatedImage, 1);

                } else if (this.cameraOrientation == 270) {

                    Core.flip(transposedImage, rotatedImage, -1);

                }
                break;
        }

        transposedImage.release();
        rotatedImage.copyTo(this.mCapturedImage);
        rotatedImage.release();

    }



    private Mat findFacesOnImageAndPlaceMustache(){

        //Here we can process mCapturedImage if classifier is loaded
        if(mFaceDetector != null){

            //Find faces in captured image.
            MatOfRect faceDetections = new MatOfRect();
            mFaceDetector.detectMultiScale(this.mCapturedImage, faceDetections);

            //Loop through the found faces and add a mustache
            for(Rect rect: faceDetections.toArray()){

                placeMustache(rect);

            }

        }

        return this.mCapturedImage;

    }

    private void placeMustache(Rect rect){

        int mustacheWidth = rect.width / 4;

        double x1 = rect.x + (double) rect.width / 2 - ((double) mustacheWidth / 2);
        double x2 = x1 + mustacheWidth;
        double y1 = rect.y + (double) rect.height - (double) rect.height / 3.5;
        double y2 = y1 + ((double) mustacheWidth / 20);

        int thickness;
        if(mustacheWidth < 50){
            thickness = 1;
        }
        else if(mustacheWidth < 80){
            thickness = 3;
        }
        else if(mustacheWidth < 100){
            thickness = 6;
        }
        else{
            thickness = 10;
        }

        Imgproc.rectangle(this.mCapturedImage,
                new Point(x1, y1),
                new Point(x2, y2),
                new Scalar(255, 255, 0, 1), thickness
        );

    }

    public Constants.CameraPosition getCameraPosition() {
        return this.cameraPosition;
    }

    public void setCameraPosition(Constants.CameraPosition position, int orientation) {
        if (this.cameraView.isRunning()) {
            this.stopCamera();
        }

        this.cameraPosition = position;
        this.cameraOrientation = orientation;
        this.cameraView.setWillRotate(this.cameraOrientation % 180 != 0);
        this.cameraView.setCameraIndex(position.toInt());

        this.startCamera();
    }

    public void startCamera() {
        if (this.cameraView != null && !this.isCameraRunning()) {
            this.displayView.addView(this.cameraView);
            this.cameraView.enableView();
        }
    }

    public void stopCamera() {
        if (this.cameraView != null && this.isCameraRunning()) {
            this.cameraView.disableView();
            this.displayView.removeView(this.cameraView);
        }
    }

    public boolean isCameraRunning() {
        if (this.cameraView != null) {
            return this.cameraView.isRunning();
        }

        return false;
    }

    //Capture an image of the current mat on the camera and convert it to a Bitmap.
    public Bitmap getCapturedImage() {

        Mat tempMat = new Mat(this.mCapturedImage.rows(), this.mCapturedImage.cols(), this.mCapturedImage.type());
        this.mCapturedImage.copyTo(tempMat);

        float viewerRatio = (float) cameraView.getHeight() / (float) cameraView.getWidth();
        float imageRatio = (float) tempMat.rows() / (float) tempMat.cols();

        //Assume that the image ratio is bigger than the viewers ratio, use the entire width and cut the height of the image
        //cv::Rect constructor takes x, y, width and height as constructor parameters
        Rect roi = new Rect(0, (int) ((tempMat.rows() - (tempMat.cols() * viewerRatio)) / 2), tempMat.cols(), (int) (tempMat.cols() * viewerRatio));

        //Check if the viewer ratio is bigger than the image ratio
        if (viewerRatio > imageRatio) {
            //Viewer ratio is bigger than the image ratio, use the entire height and cut the width of the image
            roi = new Rect((int) ((tempMat.cols() - (tempMat.rows() / viewerRatio)) / 2), 0, (int) (tempMat.rows() / viewerRatio), tempMat.rows());
        }

        //Cut out the select region of interest
        Mat capturedImageWithAspectRatio = tempMat.submat(roi);
        float scale = Math.max((float) cameraView.getHeight() / (float) capturedImageWithAspectRatio.rows(),
                (float) cameraView.getWidth() / (float) capturedImageWithAspectRatio.cols());


        Mat capturedImageResized = new Mat((int) (capturedImageWithAspectRatio.rows() * scale), (int) (capturedImageWithAspectRatio.cols() * scale), capturedImageWithAspectRatio.type());
        Imgproc.resize(capturedImageWithAspectRatio, capturedImageResized, new Size((int) (capturedImageWithAspectRatio.cols() * scale), (int) (capturedImageWithAspectRatio.rows() * scale)));
        capturedImageWithAspectRatio.release();

        Bitmap returnImage = Bitmap.createBitmap(capturedImageResized.cols(), capturedImageResized.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(capturedImageResized, returnImage);
        capturedImageResized.release();

        return returnImage;
    }


}
