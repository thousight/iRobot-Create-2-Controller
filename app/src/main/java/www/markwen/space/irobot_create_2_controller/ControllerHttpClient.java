package www.markwen.space.irobot_create_2_controller;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by Mark Wen on 4/27/2017.
 */

public class ControllerHttpClient {

    private static AsyncHttpClient client = new AsyncHttpClient();

    public void moveRobot(String url, String direction, AsyncHttpResponseHandler res) {
        client.get("http://" + url + "/"+direction, res);
    }

    public void testConnection(String url, AsyncHttpResponseHandler res) {
        client.get("http://" + url, res);
    }

    public void setRobotMode(String url, String mode, AsyncHttpResponseHandler res) {
        client.get("http://" + url + "/"+mode, res);
    }

    public void setRobotSpeed(String url, String speed, AsyncHttpResponseHandler res) {
        client.get("http://" + url + "/"+speed, res);
    }


    public void beepRobot(String url, AsyncHttpResponseHandler res) {
        client.get("http://" + url + "/Beep", res);
    }
}
