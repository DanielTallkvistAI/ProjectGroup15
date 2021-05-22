package ai.group.snapchat_filter.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Toast;
import ai.group.snapchat_filter.R;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 101;

    //Create Android View
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    //Button Action to open CameraActivity.
    //Asks for camera permission at first use before navigating to CameraActivity
    public void onOpenCameraView(View view){

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        }else{

            startCameraIntent();

        }
    }

    //Navigate to CameraActivity
    private void startCameraIntent(){

        Intent cameraIntent =  new Intent(this, CameraActivity.class);
        startActivity(cameraIntent);

    }

    //Results from asking the user for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Success. Permission granted and open CameraActivity
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
                startCameraIntent();

            } else {

                //Denied. Permission granted and does not navigate to CameraActivity
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();

            }
        }
    }
}