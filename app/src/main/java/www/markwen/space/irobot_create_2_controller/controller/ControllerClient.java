package www.markwen.space.irobot_create_2_controller.controller;

import android.app.Activity;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Mark Wen on 4/21/2017.
 */

public class ControllerClient extends AsyncTask<Void, Void, String> {
    private String dstAddress, response = "";
    private int dstPort;
    private ControllerActivity activity;

    ControllerClient(String addr, int port, ControllerActivity activity) {
        dstAddress = addr;
        dstPort = port;
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... arg0) {

        Socket socket = null;

        try {
            socket = new Socket(dstAddress, dstPort);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                    1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            InputStream inputStream = socket.getInputStream();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.setConnected();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}
