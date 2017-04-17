package www.markwen.space.irobot_create_2_controller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import www.markwen.space.irobot_create_2_controller.controller.ControllerActivity;
import www.markwen.space.irobot_create_2_controller.robot.RobotActivity;

public class MainActivity extends AppCompatActivity {

    CardView robotCard, controllerCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        robotCard = (CardView)findViewById(R.id.robotCard);
        controllerCard = (CardView)findViewById(R.id.controllerCard);

        controllerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ControllerActivity.class));
            }
        });
        robotCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RobotActivity.class));
            }
        });
    }
}
