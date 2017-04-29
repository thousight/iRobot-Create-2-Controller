package www.markwen.space.irobot_create_2_controller;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import cz.msebera.android.httpclient.Header;
import tcking.github.com.giraffeplayer.GiraffePlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by markw on 4/17/2017.
 */

public class ControllerActivity extends AppCompatActivity {

    private boolean isConnected = false;
    private MaterialDialog connectionDialog, connectingDialog;
//    private VideoView videoView;
    private ImageButton recordButton, stopRecordButton, leftButton, rightButton, upButton, downButton, beepButton;
    private AppCompatSpinner speedSpinner, modeSpinner;
    private String robotIP = "", speed = "Medium";
    private static ControllerHttpClient httpClient;
    private GiraffePlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

//        videoView = (VideoView)findViewById(R.id.videoView);
        recordButton = (ImageButton)findViewById(R.id.recordButton);
        stopRecordButton = (ImageButton)findViewById(R.id.stopRecordButton);
        leftButton = (ImageButton)findViewById(R.id.leftButton);
        upButton = (ImageButton)findViewById(R.id.upButton);
        rightButton = (ImageButton)findViewById(R.id.rightButton);
        downButton = (ImageButton)findViewById(R.id.downButton);
        beepButton = (ImageButton)findViewById(R.id.beepButton);
        speedSpinner = (AppCompatSpinner)findViewById(R.id.speedSpinner);
        modeSpinner = (AppCompatSpinner)findViewById(R.id.modeSpinner);
        stopRecordButton.setVisibility(View.GONE);

        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", "0");

        player = new GiraffePlayer(this);

        // Get screen resolution
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        // Set VideoView height based on screen resolution
//        ViewGroup.LayoutParams params = player.getLayoutParams();
//        params.height = width * 9 / 16;
//        player.setLayoutParams(params);

        // Set up speedSpinner
        final ArrayList<String> speedList = new ArrayList<>();
        speedList.add("Fast");
        speedList.add("Medium");
        speedList.add("Slow");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, speedList);
        speedSpinner.setAdapter(spinnerAdapter);
        speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                speed = speedList.get(i);
                if (!robotIP.equals("")) {
                    httpClient.setRobotSpeed(robotIP, speed, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            setDisconnected(error);
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Set up modeSpinner
        final ArrayList<String> modeList = new ArrayList<>();
        modeList.add("Passive");
        modeList.add("Safe");
        modeList.add("Full");
        modeList.add("Clean");
        modeList.add("Doc");
        modeList.add("Reset");
        final ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, modeList);
        modeSpinner.setAdapter(modeAdapter);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!robotIP.equals("")) {
                    httpClient.setRobotMode(robotIP, modeAdapter.getItem(i), new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            setDisconnected(error);
                        }
                    });
                }
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
                                setDisconnected(error);
                            }
                        });

                        // Set up and show connecting dialog
                        connectingDialog = new MaterialDialog.Builder(ControllerActivity.this)
                                .title("Connecting")
                                .content("Connecting to " + robotIP)
                                .progress(true, 100, false)
                                .cancelable(false)
                                .negativeText("Cancel")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        connectingDialog.dismiss();
                                        robotIP = "";
                                        isConnected = false;
                                        connectionDialog.show();
                                    }
                                })
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
//                recordButton.setVisibility(View.GONE);
                initVideoView("http://"+robotIP+":8090");
//                stopRecordButton.setVisibility(View.VISIBLE);
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

        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    moveRobot("Left");
                }else{
                    moveRobot("0");
                }
                return true;
            }
        });

        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    moveRobot("Down");
                }else{
                    moveRobot("0");
                }
                return true;
            }
        });
        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    moveRobot("Up");
                }else{
                    moveRobot("0");
                }
                return true;
            }
        });
        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    moveRobot("Right");
                }else{
                    moveRobot("0");
                }
                return true;
            }
        });


        beepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                httpClient.beepRobot(robotIP, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        setDisconnected(error);
                    }
                });
            }
        });
    }

    public void setConnected() {
        isConnected = true;
        connectionDialog.dismiss();
        connectingDialog.dismiss();
        Toast.makeText(this, "Connected to " + robotIP, Toast.LENGTH_SHORT).show();
    }

    public void setDisconnected(Throwable error) {
        Toast.makeText(ControllerActivity.this, "Connection failed, please reconnect: " + error.toString(), Toast.LENGTH_LONG).show();
        isConnected = false;
        connectionDialog.show();
    }
    /*
    Command list:
    "Connect" to initially connect to robot
    "Up" / "Down" / "Left" / "Right" to send robot directions command
     */
    private void moveRobot(String direction) {
        if (!robotIP.equals("")) {
            httpClient.moveRobot(robotIP, direction,  new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    // Purposely left empty
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    setDisconnected(error);
                }
            });
        }
    }

    private void initVideoView(String url){
        Uri uri = Uri.parse(url);
        try {
            player.play(url);
        }catch (Exception ex){
            Log.e("VideoView", ex.toString());
        }
    }
}
