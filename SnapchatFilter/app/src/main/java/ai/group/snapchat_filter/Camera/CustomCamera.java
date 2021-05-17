package ai.group.snapchat_filter.Camera;

import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ai.group.snapchat_filter.Utils.Constants;

public class CustomCamera  implements CameraBridgeViewBase.CvCameraViewListener {

    private Mat mCapturedImage;
    private Mat mGray;

    private ViewGroup displayView;
    private CustomCameraView cameraView;
    public int cameraOrientation;
    private Constants.CameraPosition cameraPosition;
    public boolean usesGrayscale;

    public CustomCamera(ViewGroup view, int orientation, Constants.CameraPosition position, boolean grayscale) {

        this.displayView = view;
        this.cameraOrientation = orientation;
        this.cameraPosition = position;
        this.usesGrayscale = grayscale;

        this.cameraView = new CustomCameraView(this.displayView.getContext(), this.cameraPosition.toInt(), this.cameraOrientation % 180 != 0);
        this.cameraView.setCvCameraViewListener(this);
        this.cameraView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.cameraView.enableView();

        this.displayView.addView(cameraView);
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

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.mCapturedImage = new Mat(height, width, CvType.CV_8UC4, Scalar.all(0));
        this.mGray = new Mat(height, width, CvType.CV_8UC1, Scalar.all(0));
    }

    @Override
    public void onCameraViewStopped() {
        this.mCapturedImage.release();
        this.mGray.release();
    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {

        switch (this.cameraPosition) {
            case Back:

                if (this.cameraOrientation == 90) {
                    Mat transposedImage = inputFrame.t();
                    Mat rotatedImage = new Mat(transposedImage.rows(), transposedImage.cols(), transposedImage.type());
                    Core.flip(transposedImage, rotatedImage, 1);
                    transposedImage.release();
                    rotatedImage.copyTo(this.mCapturedImage);
                    rotatedImage.release();
                } else if (this.cameraOrientation == 270) {
                    Mat transposedImage = inputFrame.t();
                    Mat rotatedImage = new Mat(transposedImage.rows(), transposedImage.cols(), transposedImage.type());
                    Core.flip(transposedImage, rotatedImage, 0);
                    transposedImage.release();
                    rotatedImage.copyTo(this.mCapturedImage);
                    rotatedImage.release();
                }

                break;
            case Front:

                if (this.cameraOrientation == 90) {
                    Mat transposedImage = inputFrame.t();
                    Mat rotatedImage = new Mat(transposedImage.rows(), transposedImage.cols(), transposedImage.type());
                    Core.flip(transposedImage, rotatedImage, 1);
                    transposedImage.release();
                    rotatedImage.copyTo(this.mCapturedImage);
                    rotatedImage.release();
                } else if (this.cameraOrientation == 270) {
                    Mat transposedImage = inputFrame.t();
                    Mat rotatedImage = new Mat(transposedImage.rows(), transposedImage.cols(), transposedImage.type());
                    Core.flip(transposedImage, rotatedImage, -1);
                    transposedImage.release();
                    rotatedImage.copyTo(this.mCapturedImage);
                    rotatedImage.release();
                }
                break;
        }

        return this.mCapturedImage;
    }
}
