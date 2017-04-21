/*
 * created by Kirill Kultinov
 * code for handling incoming messages from Arduino is obtained from
 * http://stackoverflow.com/questions/10327506/android-arduino-bluetooth-data-transfer
 */
package com.team24.uasdelivery;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Handler;

import static android.content.ContentValues.TAG;



public class DeliveryLogedInFragment extends Fragment {

    View view;
    private FirebaseAuth mAuth;
    private Button pairButton;
    private Button verifyButton;

    private static final UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String bluetoothMACAddress = "00:06:66:8C:D4:94";


    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;

    private boolean connected = false;

    OutputStream outputStream;//new stuff
    InputStream inputStream;//new stuff
    boolean stopWorker; // new stuff
    Thread workerThread; // new stuff
    int readBufferPosition;//new stuff
    byte[] readBuffer;//new stuff

    public DeliveryLogedInFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //intialize view and its components
        view = inflater.inflate(R.layout.fragment_delivery_loged_in, container, false);
        final TextView deliveryStatusTV = (TextView) view.findViewById(R.id.deliveryStatus);
        final TextView droneNameTV = (TextView) view.findViewById(R.id.droneName);
        final TextView verificationCodeTV = (TextView) view.findViewById(R.id.verificationCode);
        final TextView estTime = (TextView) view.findViewById(R.id.estTime);
        pairButton = (Button) view.findViewById(R.id.pairButton);
        verifyButton = (Button) view.findViewById(R.id.verifyButton);

        //set initial appearance for buttons
        disableButton(pairButton);
        disableButton(verifyButton);

        //intialize objects for Firebase to get changes in real time
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();

        //listen for changes in database
        //update data on the screen when changes happen
        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = dataSnapshot.child("deliveryStatus").getValue(String.class);
                String droneName = dataSnapshot.child("droneName").getValue(String.class);
                String verificationCode = dataSnapshot.child("verificationCode").getValue(String.class);
                String deliveryTime = dataSnapshot.child("deliveryTime").getValue(String.class);
                String verificationCodestr = verificationCode.toString();
                deliveryStatusTV.setText("Your package " + status);
                droneNameTV.setText("Drone's name: " + droneName);
                verificationCodeTV.setText("Verification code: " + verificationCode);
                estTime.setText("Estimated Delivery Time:" + deliveryTime);

                if(status.equals("is being delivered")){
                    estTime.setVisibility(view.VISIBLE);
                }
                else{
                    estTime.setVisibility(view.GONE);
                }

                if(status.equals("has been verified") || status.equals("is being processed") ){
                    droneNameTV.setVisibility(view.GONE);
                    verificationCodeTV.setVisibility(view.GONE);
                }else{
                    droneNameTV.setVisibility(view.VISIBLE);
                    verificationCodeTV.setVisibility(view.VISIBLE);
                }

                if(status.equals("has arrived") ||  status.equals("is being delivered")){
                    enableButton(pairButton);

                }
                else{
                    disableButton(pairButton);
                }

                if(status.equals("has been delivered")){
                    disableButton(verifyButton);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "error Happened getting data from realtime database");
            }

        });

        //onClick listener for pairButton
        pairButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //open Bluetooth settigns


                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                checkBluetoothState();
                Intent settings_intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivityForResult(settings_intent, 1);

                verifyButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enableButton(verifyButton);
                    }
                }, 1500);


            }
        });

        //onClick listener for verifyButton
        verifyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // updateDatabaseStatus();

                if(!connected) {
                    sendMessage();
                }
                else{

                    try {
                        sendData("1");

                    } catch (IOException e) {
                        Toast.makeText(getActivity().getBaseContext(), "Error happened while sending data", Toast.LENGTH_LONG).show();
                    }

                }

                updateDatabaseStatus();

            }
        });


        return view;
    }


    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBluetoothState() {

        if(bluetoothAdapter == null) {
            Toast.makeText(getActivity().getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    private void sendMessage(){
        //create device and set the MAC address
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bluetoothMACAddress);

        //create a BT socket
        try {
            bluetoothSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getActivity().getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }

        //try to connect
        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            try {
                bluetoothSocket.close();
            } catch (IOException e2) {
                Toast.makeText(getActivity().getBaseContext(), "Make sure your device is paired with the Drone.", Toast.LENGTH_LONG).show();
            }
        }

        //new stuff
        try {
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        beginListenForData(); // end of new stuff

        connected = true;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendData("1");
                    //updateDatabaseStatus();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 3000);






        //start connection thread
        //connectionThread = new ConnectionThread(bluetoothSocket);
        //connectionThread.start();

        //send the message
        //connectionThread.send("1");
        //Toast.makeText(getActivity().getBaseContext(), " sent data", Toast.LENGTH_LONG).show();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(DEVICE_UUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    private void showAlert(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void enableButton(Button button){
        button.setEnabled(true);
        button.setBackgroundColor(Color.argb(255, 59, 204, 40));
    }

    private void disableButton(Button button){
        button.setEnabled(false);
        button.setBackgroundColor(Color.GRAY);
    }

    //new stuff
    void beginListenForData() {

        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    //Log.d("LISTENING FOR DATA", "INSIDE THREAT");
                    try {
                        int bytesAvailable = inputStream.available();
                        if(bytesAvailable > 0) {
                            Log.d("RECEIVED DATA: ", "number: " + bytesAvailable);
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);

                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }
    //new stuff
    void sendData(String message) throws IOException {
        message += "\n";
        //outputStream.write(message.getBytes());
        outputStream.write('A');

    }



/*
    //create new class for connect thread
    private class ConnectionThread extends Thread {
        private final InputStream inStream;
        private final OutputStream outStream;
        //creation of the connect thread
        public ConnectionThread(BluetoothSocket socket) {
            InputStream in = null;
            OutputStream out = null;

            try {
                //Create I/O streams for connection
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) { }

            inStream = in;
            outStream = out;
        }
        //listen to incoming messages
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            // keep looping to listen to messages
            while (true) {
                Log.d("RUN METHOD", "LISTENING FOR MESSAGES");
                try {
                    bytes = inStream.read(buffer);            //read bytes from input buffer
                    readMessage = new String(buffer, 0, bytes);
                    Toast.makeText(getActivity().getBaseContext(), "receiving " + readMessage, Toast.LENGTH_LONG).show();
                    Log.w("RECEIVED DATA", "received" + readMessage);
                    // Send the obtained bytes to the UI Activity via handler
                    //bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //send data via Bluetooth
        public void send(String input) {
            byte[] msgBuffer = input.getBytes();//convert String into bytes
            try {
                outStream.write(msgBuffer);
                //Toast.makeText(getActivity().getBaseContext(), " sending data", Toast.LENGTH_LONG).show();
            } catch (IOException e) {

                Toast.makeText(getActivity().getBaseContext(), "Connection Failure Writing a Message", Toast.LENGTH_LONG).show();
            }
        }
    }*/


    private void updateDatabaseStatus(){

        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();

        try {
            reference.child(userID).child("deliveryStatus").setValue("has been delivered");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



}
