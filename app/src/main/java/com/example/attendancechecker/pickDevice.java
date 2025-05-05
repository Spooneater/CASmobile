package com.example.attendancechecker;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class pickDevice extends AppCompatActivity {

    public static final int WIRED = 123456;
    public static final int NOTWIRED = 654321;
    //devices_data[i] : i - id студента в рамках таблицы(1..n); [0]-адрес устройства (string); [1]-название (String);
    List<Object[]> devices_data = new ArrayList<Object[]>();
    String student_name;
    long student_id;
    //Отображение в которое мы закидываем
    LinearLayout tableLayoutDevice;

    BluetoothAdapter bluetoothAdapter;

    private TextView errorMessageDevice;
    private int device_id = -1;
    private LinearLayout highlightedDeviceLine;
    protected LinearLayout makeTableInst(int id,  String adress, String name, Integer width){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,width/6);

        LinearLayout line = new LinearLayout(this);
        line.setLayoutParams(params);
        TextView id_text = new TextView(this);
        id_text.setText(Integer.toString(id+1));
        id_text.setGravity(Gravity.CENTER);
        id_text.setWidth(width / 6);
        id_text.setHeight(width / 6);
        id_text.setTextSize(17);
        id_text.setBackground(AppCompatResources.getDrawable(this,R.drawable.scrol_border));
        line.addView(id_text);
        LinearLayout twoRows = new LinearLayout(this);
        twoRows.setOrientation(LinearLayout.VERTICAL);
        //Название девайса
        TextView name_text = new TextView(this);
        name_text.setText(String.format("Имя:%s",name));
        name_text.setPadding(15,0,0,0);
        name_text.setTextSize(17);
        name_text.setHeight(width / 12);
        name_text.setWidth(width-width/6);
        name_text.setGravity(Gravity.CENTER_VERTICAL);

        twoRows.addView(name_text);
        //Адрес
        TextView adress_text = new TextView(this);
        adress_text.setText(String.format("Адрес:%s", adress));
        adress_text.setPadding(15,0,0,0);
        adress_text.setHeight(width / 12);
        adress_text.setWidth(width-width/6);
        adress_text.setTextSize(17);
        adress_text.setGravity(Gravity.CENTER_VERTICAL);

        twoRows.addView(adress_text);
        line.addView(twoRows);
        View overlay_view = new View(this);

        overlay_view.setAlpha((float)0.6);
        overlay_view.setBackgroundColor(getColor(R.color.black));
        line.addView(overlay_view);
        line.setBackground(AppCompatResources.getDrawable(this,R.drawable.scrol_border));
        overlay_view.setVisibility(View.INVISIBLE);
        var context = this;
        line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (device_id !=-1){
                    highlightedDeviceLine.setBackground(AppCompatResources.getDrawable(context,R.drawable.scrol_border));
                }
                device_id = id;
                highlightedDeviceLine = line;
                line.setBackground(AppCompatResources.getDrawable(context,R.drawable.border_in_gray));
            }
        });
        return line;
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("aaa","bbb");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null){

                    return;
                }
                String deviceName;
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    deviceName = "";
                }
                else{
                    deviceName = device.getName();
                }
                String deviceHardwareAddress = device.getAddress(); // MAC addres
                synchronized (this) {
                    devices_data.add(new Object[]{deviceHardwareAddress, deviceName});// s
                    tableLayoutDevice.addView(makeTableInst(devices_data.size()-1 ,deviceHardwareAddress,deviceName, tableLayoutDevice.getWidth()));
                }
            }
        }
    };

    //TODO
    protected boolean isConnected(){
        return true;
    }
    //TODO Собственно запрос. return true if получилось
    private boolean requestWiring()
    {
        return (student_id!=-1);
    }
    protected void tryToWire(){
        if (device_id == -1){
            errorMessageDevice.setText("Нажмите на выбранное устройство");
            return;
        }
        if (!isConnected()){
            errorMessageDevice.setText("Привязывать устройства можно только при подключении к сети");
            return;
        }
        //TODO Добавить запрос для проверки привязки устройства
        if ((boolean)devices_data.get(device_id)[2]){
            errorMessageDevice.setText("Этот адрес уже привязан к другому студенту");
            return;
        }
        //Попытка привязки. индекс в devices_data лежит в device_id подефолту -1
        if (requestWiring()){
            setResult(WIRED);
            finish();
            return;
        }
        errorMessageDevice.setText("UNKNOWN ERROR");

    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_device);
        errorMessageDevice = findViewById(R.id.errorMessageDevice);
        Button confirmBtn = findViewById(R.id.confirmDeviceBtn);
        Button returnBtn =  findViewById(R.id.ret_button_dev);
        TextView header = findViewById(R.id.TextThingDevice);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(NOTWIRED);
                finish();

            }
        });
        //данные с  прошлой страницы
        Intent lastintent = getIntent();
        student_name = lastintent.getStringExtra("student_name");
        student_id = lastintent.getLongExtra("student_id",-1);
        header.setText(String.format("Выберите устройство для \"%s\"", student_name));
        device_id = -1;

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToWire();
            }
        });
        tableLayoutDevice = findViewById(R.id.tableLayoutDevice);
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH_CONNECT},2);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH_SCAN},2);
        }

        if (!bluetoothAdapter.getBondedDevices().isEmpty()) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                String deviceName = device.getName();

                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }



    }
    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,intentFilter);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothAdapter.startDiscovery();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
}