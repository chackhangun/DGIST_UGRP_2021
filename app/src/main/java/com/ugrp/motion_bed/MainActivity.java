package com.ugrp.motion_bed;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity<test> extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 100; //0보다 커야함. 블루투스 활성화 요청시 사용됨.
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int BT_MESSAGE_READ = 1;
    private static final int REQUEST_CODE = 0;
    private static final int BT_CONNECTING_STATUS = 2;

    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private FragmentManager fragmentManager;
    private Home_fragment home_fragment;
    private Setting_fragment setting_fragment;
    private FragmentTransaction fragmentTransaction;
    private List<String> list_of_devices;

    BluetoothAdapter btAdapter; //블루투스를 사용하려면 BluetoothAdapter 필요.//
    Set<BluetoothDevice> pairedDevices;
    BluetoothDevice bluetoothDevice_ar;
    BluetoothDevice bluetoothDevice_bed;
    BluetoothSocket btsocket_ar;
    BluetoothSocket btsocket_bed;
    ConnectedThread connectedThread_ar;
    ConnectedThread connectedThread_bed;
    Handler bt_Handler;
    boolean thread_check = false;
    private ImageView baby_image_view;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        home_fragment = new Home_fragment();
        setting_fragment = new Setting_fragment();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, home_fragment).commitAllowingStateLoss();

        baby_image_view = findViewById(R.id.baby_photo);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        bt_Handler = new Handler() {

            public void handleMessage(android.os.Message msg) {
                if (msg.what == BT_MESSAGE_READ) {
                    String arduinoMsg = msg.obj.toString();
                    System.out.println(arduinoMsg);
                    switch (arduinoMsg.toLowerCase()) {
                        case "warning":
                            System.out.println("warning\n");
                            break;
                        case "stable!":
                            System.out.println("stable\n");
                            break;
                    }
                    //connectedThread_bed.write("1")
                    /*
                    TextView textView = home_fragment.status_box;
                    textView.setText("warning");
                    textView.setTextColor(Color.parseColor("#CD1039"));

                    //connectedThread_bed.write("1");

                     */
                }

            }
        }; //read가 올 때 write를 해주게 된다 -> 1을 한번만 보내게 된다. ///test
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //------------------------------------------------------블루투스 설정------------------------------------------------------------//
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (btAdapter.isEnabled()) {///만약 블루투스가 꺼져있다면 활성화 대화창이 나타난다.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//intent는 화면전환 시 필요한 클래
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Toast.makeText(getApplicationContext(), "블루투스를 활성화가 필요합니다", Toast.LENGTH_LONG).show();
        }
        //--------------------------------------------------블루투스 on/off check-------------------------------------------------------//
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                String MACaddress = device.getAddress();
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    public void clickHandler(View view) {
        fragmentTransaction = fragmentManager.beginTransaction();
        switch (view.getId()) {
            case R.id.Home_btn:
                fragmentTransaction.replace(R.id.frame_layout, home_fragment).commitAllowingStateLoss();
                break;
            case R.id.Setting_btn:
                fragmentTransaction.replace(R.id.frame_layout, setting_fragment).commitAllowingStateLoss();
                break;
        }
    }

    public void choose_photo(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void awaken(View view) {
        if (connectedThread_bed != null) {
            connectedThread_bed.write("1");
        }
    }

    public void showlistdevice(View view) {
        if (btAdapter.isEnabled()) {
            pairedDevices = btAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("bluetooth 장치목록 ");

                list_of_devices = new ArrayList();
                for (BluetoothDevice device : pairedDevices) {
                    list_of_devices.add(device.getName());
                }
                final CharSequence[] items = list_of_devices.toArray(new CharSequence[list_of_devices.size()]);
                list_of_devices.toArray(new CharSequence[list_of_devices.size()]);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        for (BluetoothDevice device : pairedDevices) {
                            if (items[item].toString().equals(device.getName())) {//bluetooth device 이름 자체를 아두이노로 바꾸기
                                // 자이로센서 쪽 아두이노부터 연결.
                                if (bluetoothDevice_ar == null) {
                                    bluetoothDevice_ar = device;
                                } else {
                                    bluetoothDevice_bed = device;
                                }
                                break;
                            }
                        }
                        try {
                            if (btsocket_ar == null) {
                                btsocket_ar = bluetoothDevice_ar.createRfcommSocketToServiceRecord(BT_UUID);
                                btsocket_ar.connect();
                                connectedThread_ar = new ConnectedThread(btsocket_ar);
                                connectedThread_ar.start();
                                thread_check = true;
                            } else {
                                btsocket_bed = bluetoothDevice_bed.createRfcommSocketToServiceRecord(BT_UUID);
                                btsocket_bed.connect();
                                connectedThread_bed = new ConnectedThread(btsocket_bed);
                                connectedThread_bed.start();
                            }
                            bt_Handler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {///블루투스 디바이스의 action found를 위해서 적는
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra((BluetoothDevice.EXTRA_DEVICE));
                String deviceName = device.getName();
                String deviceMACaddress = device.getAddress(); //macaddress를 리턴해주는 메소드//
            }
        }
    };

    /*
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    try {
                        InputStream in = getContentResolver().openInputStream(data.getData());
                        Bitmap img = BitmapFactory.decodeStream(in);
                        in.close();

                        baby_image_view.setImageBitmap(img);
                    } catch (Exception e) {

                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
                }
            }
        }*/
    @Override
    public void onDestroy() { //기기 검색할때는 블루투스 어댑터의 리소스가 많이 소모. 따라서 검색 중단하고 연결해야함.
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {
        public static BluetoothSocket mmSocket;
        public static Handler handler;
        private final static int CONNECTING_STATUS = 1;

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final static int MESSAGE_READ = 2;
        public static Handler handler;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}

