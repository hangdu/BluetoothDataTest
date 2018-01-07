package com.example.hang.bluetoothdatatest;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by hang on 2017/12/31.
 */
public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "MYAPP";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;
    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private static BluetoothConnectionService instance;
    public static BluetoothConnectionService getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothConnectionService(context);
        }
        return instance;
    }

    public static BluetoothConnectionService getInstance() {
        return instance;
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread:Setting up Server using" + MY_UUID_INSECURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");
            BluetoothSocket socket = null;

            try {
                Log.d(TAG, "run: RFCOM server socket start......");
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accept connection..");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (socket != null) {
                connected(socket, mmDevice);
            }
            Log.i(TAG, "End mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Cancelling AcceptThread");

            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread");

            //Get a BluetoothSocket for a connection with the given BluetoothDevice
            try{
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
            //always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                //This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected");
                connected(mmSocket, mmDevice);
            } catch (IOException e) {
                //close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "Closed socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: could not close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: mConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);
            }
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client socket.");
                mmSocket.close();
            } catch (IOException e) {
               Log.e(TAG, "cancel: close() of mmSocket in ConnectThread failed. " + e.getMessage());
            }
        }
    }


    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    //AcceptThread starts adn sits waiting for a connection
    //Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread

    //I will call this method.
    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: started");

        //Initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please Wait...", true);
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            mProgressDialog.dismiss();

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[10];
            int bytes;
            List<Byte> list = new ArrayList<>();

            while (true) {
                //Read from inputStream
                try {
                    bytes = mmInStream.read(buffer);
                    for (int i = 0; i < bytes; i++) {
                        list.add(buffer[i]);
                    }
                    if ((buffer[bytes-1] & 0xFF) == 0xFF) {
                        //end of this packet
                        int size = list.size();
                        byte[] res = new byte[size];
                        for (int i = 0; i < list.size(); i++) {
                            res[i] = list.get(i);
                        }
                        Intent incomingMessageIntent = new Intent("incomingMessage");
                        incomingMessageIntent.putExtra("MessageByteArray", res);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
                        list.clear();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "write : Error reading inputStream.." + e.getMessage());
                    break;
                }
            }
        }

        //call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputStream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write : Error wriring to outputstream." + e.getMessage());
            }
        }

        //call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: starting");
        //Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void write(byte[] out) {
        Log.d(TAG, "write: Write called.");
        //Perform the write
        mConnectedThread.write(out);
    }
}
