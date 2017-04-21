package www.markwen.space.irobot_create_2_controller.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import www.markwen.space.irobot_create_2_controller.R;

/**
 * Created by markw on 4/17/2017.
 */

public class ControllerActivity extends AppCompatActivity {

    private boolean isConnected = false;
    private MaterialDialog connectionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        connectionDialog = new MaterialDialog.Builder(this)
                .title("Connecting")
                .content("Looking for a robot socket server...")
//                .cancelable(false)
                .progress(true, 100, false)
                .build();
        if (!isConnected) {
            connectionDialog.show();
        }
    }

}
