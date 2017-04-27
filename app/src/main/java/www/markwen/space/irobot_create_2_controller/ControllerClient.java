package www.markwen.space.irobot_create_2_controller;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import www.markwen.space.irobot_create_2_controller.ControllerActivity;

/**
 * Created by Mark Wen on 4/21/2017.
 */

public class ControllerClient extends AsyncTask<Void, Void, String> {
    private String dstAddress, response = "", action = "";
    private int dstPort;
    private ControllerActivity activity;

    ControllerClient(String addr, int port, ControllerActivity activity, String command) {
        dstAddress = addr;
        dstPort = port;
        this.activity = activity;
        this.action = command;
    }

    @Override
    protected String doInBackground(Void... arg0) {

        Socket socket = null;
        DataOutputStream dataOutputStream;

        try {
            socket = new Socket(dstAddress, dstPort);
            socket.setSoTimeout(200);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                    1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            InputStream inputStream = socket.getInputStream();
            dataOutputStream.writeUTF(action);

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
