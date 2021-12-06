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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 100; //0보다 커야함. 블루투스 활성화 요청시 사용됨.
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int BT_MESSAGE_READ = 1;
    private static final int REQUEST_CODE = 0;
    private static final int BT_CONNECTING_STATUS = 2;

    public static ConnectedThread connectedThread;
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
    private ImageView baby_image_view;
    TextView sensor_textbox;
    TextView bed_textbox;
    TextView status_textbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        baby_image_view = findViewById(R.id.baby_photo);
        sensor_textbox = (TextView) findViewById(R.id.sensor_status);
        bed_textbox = (TextView) findViewById(R.id.bed_status);
        status_textbox = (TextView) findViewById(R.id.status_box);
        btAdapter = BluetoothAdapter.getDefaultAdapter();


         bt_Handler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    System.out.println(readMessage);
                    if(readMessage.indexOf("w") == 0){
                        System.out.println("드디어성공?");
                        status_textbox.setText(readMessage);
                        status_textbox.setTextColor(Color.RED);
                        //connectedThread_bed.write("1");
                        connectedThread_ar.write("1\n");

                    }
                    if(readMessage.indexOf("s") == 0){
                        status_textbox.setText(readMessage);
                        status_textbox.setTextColor(Color.BLUE);
                        //connectedThread_bed.write("0");
                        connectedThread_ar.write("0\n");
                    }
                }
                /*
                if(msg.what == BT_CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + (String)(msg.obj));
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }*/
            }
        };

                //read가 올 때 write를 해주게 된다 -> 1을 한번만 보내게 된다. ///test
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //------------------------------------------------------블루투스 설정------------------------------------------------------------//
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (btAdapter.isEnabled()) {///만약 블루투스가 꺼져있다면 활성화 대화창이 나타난다.
            Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어있습니", Toast.LENGTH_LONG).show();
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
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);
            pairedDevices = btAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("BLUETOOTH 장치목록 ");

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
                        boolean ar_flag = true;
                        boolean bed_flag = true;
                        if(btsocket_ar == null){
                            try {
                                btsocket_ar = bluetoothDevice_ar.createRfcommSocketToServiceRecord(BT_UUID);
                                btsocket_ar.connect();
                            }catch (IOException E){
                                ar_flag = false;
                                sensor_textbox.setText("connection failed!");
                                sensor_textbox.setTextColor(Color.RED);
                                E.printStackTrace();
                            }

                            if(ar_flag){
                                sensor_textbox.setText("Connected!");
                                sensor_textbox.setTextColor(Color.BLUE);
                                connectedThread_ar = new ConnectedThread(btsocket_ar);
                                connectedThread_ar.start();
                            }
                        }
                        else{
                            try {
                                btsocket_bed = bluetoothDevice_bed.createRfcommSocketToServiceRecord(BT_UUID);
                                btsocket_bed.connect();
                            }catch (IOException E){
                                bed_flag = false;
                                bed_textbox.setText("connection failed!");
                                bed_textbox.setTextColor(Color.RED);
                                E.printStackTrace();
                            }

                            if(bed_flag){
                                bed_textbox.setText("Connected!");
                                bed_textbox.setTextColor(Color.BLUE);
                                connectedThread_bed = new ConnectedThread(btsocket_bed);
                                connectedThread_bed.start();
                            }
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어있습니", Toast.LENGTH_LONG).show();
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

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
    }

    @Override
    public void onDestroy() { //기기 검색할때는 블루투스 어댑터의 리소스가 많이 소모. 따라서 검색 중단하고 연결해야함.
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        buffer = new byte[10];
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        bt_Handler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

}

