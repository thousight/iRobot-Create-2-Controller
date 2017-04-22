package www.markwen.space.irobot_create_2_controller.robot;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import www.markwen.space.irobot_create_2_controller.R;

/**
 * Created by markw on 4/17/2017.
 */

public class RobotActivity extends AppCompatActivity {
    private TextView IPText, SSIDText, ConnectedDevice, ConnectedToRobot;
    private VideoView robotCameraView;
    private MaterialDialog connectionDialog;
    private ServerSocket serverSocket;
    private boolean isConnectedToRobot = false, isConnectedToClient = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot);

        robotCameraView = (VideoView)findViewById(R.id.robotCameraView);

        connectionDialog = new MaterialDialog.Builder(this)
                .title("Connecting")
                .customView(R.layout.server_connect_dialog, false)
                .cancelable(false)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        RobotActivity.this.finish();
                    }
                })
                .negativeText("Cancel")
                .negativeColor(ResourcesCompat.getColor(getResources(), R.color.colorNegative, null))
                .build();
        View connectionDialogView = connectionDialog.getView();
        IPText = (TextView)connectionDialogView.findViewById(R.id.IP);
        SSIDText = (TextView)connectionDialogView.findViewById(R.id.networkName);
        ConnectedDevice = (TextView)connectionDialogView.findViewById(R.id.ConnectedDevice);
        ConnectedToRobot = (TextView)connectionDialogView.findViewById(R.id.ConnectedToRobot);
        connectionDialog.show();

        setNetworkNameText(((WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE)).getConnectionInfo().getSSID());
        setIPAddressText(getIpAddress());

        // Start server thread
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public void setIPAddressText(String IP) {
        IPText.setText("IP address: " + IP);
    }

    public void setNetworkNameText(String SSID) {
        SSIDText.setText("Wifi name: " + SSID);
    }

    public void setConnectedToRobotText(boolean connected) {
        if (connected) {
            SSIDText.setText("Robot is connected");
        } else {
            SSIDText.setText("Please connect to a robot");
        }
    }

    /*
    TODO: Code to connect to robot
     */
    public void connectToRobot() {
        // TODO: connect to robot

        isConnectedToRobot = true;
        setFinishConnection();
    }

    /*
    TODO: Code to move robot to direction
     */
    public void moveRobot(String direction) {

    }

    /*
    TODO: Call this function when client and robot are connected to dismiss the dialog and proceed
     */
    public void setFinishConnection() {
        if (isConnectedToClient && isConnectedToRobot) {
            connectionDialog.dismiss();
        }
    }

    // Getting IP address of the device
    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip = "Something Wrong! " + e.toString();
        }

        return ip;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread {

        private Socket socket;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;


        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(2333);

                while (true) {
                    socket = serverSocket.accept();
                    socket.setSoTimeout(200);
                    dataInputStream = new DataInputStream(
                            socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    final Socket finalSocket = socket;
                    final String finalMessageFromClient = dataInputStream.readUTF(); // Receive command from client

                    // Actions in UI thread after receiving commands from client
                    RobotActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (finalMessageFromClient.equals("Connect")) {
                                ConnectedDevice.setText("Connected Device IP: " + finalSocket.getInetAddress().getHostAddress());
                                isConnectedToClient = true;
                                setFinishConnection();
                            } else {
                                moveRobot(finalMessageFromClient);
                            }

                            // Show received command from client
                            Toast.makeText(RobotActivity.this, finalMessageFromClient, Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Reply confirm connection at the beginning
                    if (finalMessageFromClient.equals("Connect")) {
                        PrintStream printStream = new PrintStream(dataOutputStream);
                        printStream.print(true);
                        printStream.close();
                    }
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

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}

