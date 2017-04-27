package www.markwen.space.irobot_create_2_controller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by markw on 4/17/2017.
 */

public class ControllerActivity extends AppCompatActivity {

    private boolean isConnected = false;
    private MaterialDialog connectionDialog, connectingDialog;
    private VideoView videoView;
    private ImageButton recordButton, stopRecordButton, leftButton, rightButton, upButton, downButton;
    private AppCompatSpinner spinner;
    private String robotIP = "", speed = "Medium";
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
        spinner = (AppCompatSpinner)findViewById(R.id.speedSpinner);
        stopRecordButton.setVisibility(View.GONE);

        // Get screen resolution
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        // Set VideoView height based on screen resolution
        ViewGroup.LayoutParams params = videoView.getLayoutParams();
        params.height = width * 9 / 16;
        videoView.setLayoutParams(params);

        // Set up spinner
        final ArrayList<String> speedList = new ArrayList<>();
        speedList.add("Fast");
        speedList.add("Medium");
        speedList.add("Slow");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, speedList);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(1); // Select "Medium" at start
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                speed = speedList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Initialize Http Client
        httpClient = new ControllerHttpClient();

        // Dialog for connecting to a robot
        connectionDialog = new MaterialDialog.Builder(this)
                .title("Connecting")
                .customView(R.layout.client_connect_dialog, false)
                .cancelable(false)
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
                moveRobot("Left");
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveRobot("Right");
            }
        });

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveRobot("Up");
            }
        });

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveRobot("Down");
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
    private void moveRobot(String direction) {
        if (!robotIP.equals("")) {
            httpClient.moveRobot(robotIP, direction, speed, new AsyncHttpResponseHandler() {
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
