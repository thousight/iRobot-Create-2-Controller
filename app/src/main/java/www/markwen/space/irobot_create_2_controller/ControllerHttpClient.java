package www.markwen.space.irobot_create_2_controller;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by Mark Wen on 4/27/2017.
 */

public class ControllerHttpClient {

    private static AsyncHttpClient client = new AsyncHttpClient();

    public void moveRobot(String url, String direction, String speed, AsyncHttpResponseHandler res) {
        RequestParams requestParams = new RequestParams();
        requestParams.add("Direction", direction);
        requestParams.add("Speed", speed);
        client.post("http://" + url + "/moveRobot", requestParams, res);
    }

    public void testConnection(String url, AsyncHttpResponseHandler res) {
        RequestParams requestParams = new RequestParams();
        requestParams.add("TestConnection", "Test");
        client.post("http://" + url + "/testConnection", requestParams, res);
    }

    public void setRobotMode(String url, String mode, AsyncHttpResponseHandler res) {
        RequestParams requestParams = new RequestParams();
        requestParams.add("Mode", mode);
        client.post("http://" + url + "/setRobotMode", requestParams, res);
    }

    public void beepRobot(String url, AsyncHttpResponseHandler res) {
        client.post("http://" + url + "/beepRobot", new RequestParams(), res);
    }
}
