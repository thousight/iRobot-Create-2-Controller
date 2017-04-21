package www.markwen.space.irobot_create_2_controller.controller;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import www.markwen.space.irobot_create_2_controller.R;

/**
 * Created by markw on 4/17/2017.
 */

public class ControllerActivity extends AppCompatActivity {

    private boolean isConnected = false;
    private MaterialDialog connectionDialog;
    private VideoView videoView;
    private ImageButton recordButton, leftButton, rightButton, upButton, downButton;
    private String robotIP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        videoView = (VideoView)findViewById(R.id.videoView);
        recordButton = (ImageButton)findViewById(R.id.recordButton);
        leftButton = (ImageButton)findViewById(R.id.leftButton);
        upButton = (ImageButton)findViewById(R.id.upButton);
        rightButton = (ImageButton)findViewById(R.id.rightButton);
        downButton = (ImageButton)findViewById(R.id.downButton);

        // Dialog for connecting to a robot
        connectionDialog = new MaterialDialog.Builder(this)
                .title("Connecting")
                .customView(R.layout.client_connect_dialog, false)
                .cancelable(false)
                .positiveText("Connect")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // Get IP address
                        isConnected = true;
                        View connectionDialogView = dialog.getView();
                        EditText robotIpEditText = (EditText) connectionDialogView.findViewById(R.id.client_IP_input);
                        robotIP = robotIpEditText.getText().toString();
                        // Connect to robot
                        ControllerClient myClient = new ControllerClient(robotIP, 2333, ControllerActivity.this);
                        myClient.execute();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ControllerActivity.this.finish();
                    }
                })
                .negativeColor(ResourcesCompat.getColor(getResources(), R.color.colorNegative, null))
                .build();


        if (!isConnected) {
            connectionDialog.show();
        }
    }

    public void setConnected() {
        isConnected = true;
        connectionDialog.dismiss();
        Toast.makeText(this, "Connected to " + robotIP, Toast.LENGTH_SHORT).show();
    }

}
