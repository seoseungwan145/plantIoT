package org.techtown.plant;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private Socket mmSocket;
    static final int REQUEST_ENABLE_BT = 10;
    BluetoothAdapter mBluetoothAdapter;
    int mPairedDeviceCount = 0;
    Set<BluetoothDevice> mDevices;
    BluetoothDevice mRemoteDevice;
    BluetoothSocket mSocket = null;
    OutputStream mOutputStream = null;
    InputStream mInputStream = null;
    Thread mWorkerThread = null;
    String mStrDelimiter = "\n";
    char mCharDelimiter = '\n';
    byte[] readBuffer;
    int readBufferPosition;
    TextView mMemoEdit = null;
    TextView mEditReceive, mEditSend;
    String result = "";

    void beginListenForData()
    {
        final Handler handler = new Handler();

        readBuffer = new byte[1024];    // 수신 버퍼
        readBufferPosition = 0;     // 버퍼 내 수신 문자 저장 위치

        // 문자열 수신 쓰레드
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        int bytesAvailable = mInputStream.available();  // 수신 데이터 확인
                        if (bytesAvailable > 0) { // 데이터가 수신된 경우
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == mCharDelimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            // 수신된 문자열 데이터에 대한 처리 작업
                                            mEditReceive.setText(mEditReceive.getText().toString()+data+mStrDelimiter);
                                            result = data;
                                            if(!result.matches(".*[A-z].*") && result.length() > 17)
                                                mmSocket.emit("clientMessage", result);
                                            result = "";
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        // 데이터 수신 중 오류 ㅏㅂㄹ생
                        finish();
                    }
                }
            }
        });

        mWorkerThread.start();
    }

    void sendData(String msg){
        msg += mStrDelimiter;   // 문자열 종료 표시
        try{
            mOutputStream.write(msg.getBytes());    // 문자열 전송
        }catch(Exception e){
            // 문자열 전송 도중 오류가 발생한 경우
            finish();   // 어플리케이션 종료
        }
    }

    protected void onDestory(){
        try{
            mWorkerThread.interrupt();  // 데이터 수신 쓰레드 종료
            mInputStream.close();
            mOutputStream.close();
            mSocket.close();
        }catch(Exception e){
            super.onDestroy();
        }
    }

    void connectToSelectedDevice(String selectedDeviceName){
        mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // 소켓 생성
            mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);

            // RFCOMM 채널을 통한 연결
            mSocket.connect();

            // 데이터 송수신을 위한 스트림 얻기
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            // 데이터 수신 준비
            beginListenForData();
        }
        catch(Exception e){
            // 블루투스 연결 중 오류 발생
            finish();   // 어플리케이션 종료
        }
    }

    BluetoothDevice getDeviceFromBondedList(String name){
        BluetoothDevice selectedDevice = null;

        for (BluetoothDevice device : mDevices){
            if(name.equals(device.getName())){
                selectedDevice = device;
                break;
            }
        }
        return selectedDevice;
    }

    void selectDevice(){
        mDevices = mBluetoothAdapter.getBondedDevices();
        mPairedDeviceCount = mDevices.size();

        if(mPairedDeviceCount == 0){
            // 페어링된 장치가 없는 경우
            finish();   // 어플리케이션 종료
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 장치 선택");

        // 페어링된 블루투스 장치의 이름 목록 작성
        List<String> listItems = new ArrayList<String>();
        for(BluetoothDevice device : mDevices){
            listItems.add(device.getName());
        }
        listItems.add("취소");    // 취소 항목 추가

        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                if(item == mPairedDeviceCount){
                    // 연결할 장치를 선택하지 않고 취소를 누른 경우
                    finish();
                }
                else
                {
                    // 연결할 장치를 선택한 경우
                    // 선택한 장치와 연결을 시도함
                    connectToSelectedDevice(items[item].toString());
                }
            }
        });
        builder.setCancelable(false);   // 뒤로 가기 버튼 사용 금지
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK){
                    // 블루투스가 활성 상태로 변경됨
                    selectDevice();
                }
                else if(resultCode == RESULT_CANCELED){
                    // 블루투스가 비활성 상태임
                    finish();   // 어플리케이션 종료
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void checkBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            // 장치가 블루투스를 지원하지 않는 경우
            finish(); // 어플리케이션 종료
        }
        else{
            if(!mBluetoothAdapter.isEnabled())
            {
                // 블루투스를 지원하지만 비활성 상태인 경우
                // 블루투스를 활성 상태로 바꾸기 위해 사용자 동의 요청
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else
            {
                // 블루투스를 지원하며 활성 상태인 경우
                // 페어링된 기기 목록을 보여주고 연결할 장치를 선택
                selectDevice();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditReceive = (TextView)findViewById(R.id.receiveText);
        // mEditSend = (EditText)findViewById(R.id.sendText);

        checkBluetooth();

        try {
            mmSocket = IO.socket("http://192.168.0.7:8000");
            mmSocket.connect();
            mmSocket.on(Socket.EVENT_CONNECT, onConnect);
            mmSocket.on("serverMessage", onMessageReceived);
        } catch(URISyntaxException e) {
            e.printStackTrace();
        }

    }

    // Socket서버에 connect 되면 발생하는 이벤트
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //mmSocket.emit("clientMessage", "hi seungwan");
            // String memoData = result;
            // mmSocket.emit("clientMessage", memoData);
            // result = "";
            // Toast.makeText(this, "송출 완료", Toast.LENGTH_LONG).show();
        }
    };

    // 서버로부터 전달받은 'chat-message' Event 처리.
    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // 전달받은 데이터는 아래와 같이 추출할 수 있습니다.
            try {
                JSONObject receivedData = (JSONObject) args[0];
                Log.d(TAG, receivedData.getString("msg"));
                Log.d(TAG, receivedData.getString("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}