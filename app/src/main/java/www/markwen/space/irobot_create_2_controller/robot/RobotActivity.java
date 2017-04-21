package www.markwen.space.irobot_create_2_controller.robot;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView IPText, SSIDText, ConnectedDevice;
    private ServerSocket serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot);

        IPText = (TextView)findViewById(R.id.IP);
        SSIDText = (TextView)findViewById(R.id.networkName);
        ConnectedDevice = (TextView)findViewById(R.id.ConnectedDevice);

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
                        ip += inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
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
                    dataInputStream = new DataInputStream(
                            socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    final Socket finalSocket = socket;
//                    final String finalMessageFromClient = dataInputStream.readUTF();

                    // Set UI
                    RobotActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ConnectedDevice.setText("Connected Device IP: " + finalSocket.getInetAddress().getHostAddress());
//                            Toast.makeText(RobotActivity.this, finalMessageFromClient, Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Reply confirm connection
                    PrintStream printStream = new PrintStream(dataOutputStream);
                    printStream.print(true);
                    printStream.close();
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