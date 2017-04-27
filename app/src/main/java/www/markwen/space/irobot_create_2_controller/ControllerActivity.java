package www.markwen.space.irobot_create_2_controller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Created by markw on 4/17/2017.
 */

public class ControllerActivity extends AppCompatActivity {

    private boolean isConnected = false;
    private MaterialDialog connectionDialog, connectingDialog;
    private VideoView videoView;
    private ImageButton recordButton, stopRecordButton, leftButton, rightButton, upButton, downButton;
    private String robotIP = "";
    private static ControllerHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        videoView = (VideoView)findViewById(R.id.videoView);
        recordButton = (ImageButton)findViewById(R.id.recordButton);
        stopRecordButton = (ImageButton)findViewById(R.id.stopRecordButton);
        leftButton = (ImageButton)findViewById(R.id.leftButton);
        upButton = (ImageButton)findViewById(R.id.upButton);
        rightButton = (ImageButton)findViewById(R.id.rightButton);
        downButton = (ImageButton)findViewById(R.id.downButton);
        stopRecordButton.setVisibility(View.GONE);

        // Get screen resolution
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        // Set VideoView height based on screen resolution
        ViewGroup.LayoutParams params = videoView.getLayoutParams();
        params.height = width * 9 / 16;
        videoView.setLayoutParams(params);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) !=  PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) !=  PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=  PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }

        // Initialize Http Client
        httpClient = new ControllerHttpClient();

        // Dialog for connecting to a robot
        connectionDialog = new MaterialDialog.Builder(this)
                .title("Connecting")
                .customView(R.layout.client_connect_dialog, false)
//                .cancelable(false)
                .buttonsGravity(GravityEnum.END)
                .neutralText("Connect")
                .negativeText("Cancel")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        // Get IP address
                        isConnected = true;
                        EditText robotIpEditText = (EditText) dialog.getView().findViewById(R.id.client_IP_input);
                        robotIP = robotIpEditText.getText().toString();

                        // Connect to robot
                        httpClient.testConnection(robotIP, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                setConnected();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(ControllerActivity.this, "Connection failed: " + error.toString(), Toast.LENGTH_LONG).show();
                                isConnected = false;
                                connectionDialog.show();
                            }
                        });

                        // Set up and show connecting dialog
                        connectingDialog = new MaterialDialog.Builder(ControllerActivity.this)
                                .title("Connecting")
                                .content("Connecting to " + robotIP)
                                .progress(true, 100, false)
                                .cancelable(false)
                                .build();
                        connectingDialog.show();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ControllerActivity.this.finish();
                    }
                })
                .neutralColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null))
                .negativeColor(ResourcesCompat.getColor(getResources(), R.color.colorNegative, null))
                .build();

        if (!isConnected) {
            connectionDialog.show();
        }

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setVisibility(View.GONE);
                stopRecordButton.setVisibility(View.VISIBLE);
                // TODO: start recording streamed video

            }
        });

        stopRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setVisibility(View.VISIBLE);
                stopRecordButton.setVisibility(View.GONE);
                // TODO: stop recording streamed video and save it to device

            }
        });

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveRobot("Left", 10);
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveRobot("Right", 10);
            }
        });

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveRobot("Up", 10);
            }
        });

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveRobot("Down", 10);
            }
        });
    }

    public void setConnected() {
        isConnected = true;
        connectionDialog.dismiss();
        connectingDialog.dismiss();
        Toast.makeText(this, "Connected to " + robotIP, Toast.LENGTH_SHORT).show();
    }

    /*
    Command list:
    "Connect" to initially connect to robot
    "Up" / "Down" / "Left" / "Right" to send robot directions command
     */
    private void moveRobot(String direction, int speed) {
        if (!robotIP.equals("")) {
            httpClient.moveRobot(robotIP, direction, String.valueOf(speed), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    // Purposely left empty
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(ControllerActivity.this, "Connection failed, please reconnect: " + error.toString(), Toast.LENGTH_LONG).show();
                    isConnected = false;
                    connectionDialog.show();
                }
            });
        }
    }

}
