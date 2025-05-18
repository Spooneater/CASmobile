package com.example.attendancechecker;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
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
import java.util.HashSet;
import java.util.List;

public class AddressesPage extends AppCompatActivity {
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public static final String STOPWIRING = "ASDJlAJDS";
    public static final int WIRED = 123456;
    public static final int NOTWIRED = 654321;
    public static final String STOPUPDATINGF = "adsksaaadk";
    public View overlay;
    UserData userData;
    //devices_data[i] : i - id студента в рамках таблицы(1..n); [0]-адрес устройства (string); [1]-название (String);
    List<Object[]> devicesData = new ArrayList<Object[]>();
    HashSet<String> deviceAddressSet = new HashSet<String>();
    String studentName;
    int studentId;
    //Отображение в которое мы закидываем
    LinearLayout tableLayoutDevice;

    BluetoothAdapter bluetoothAdapter;

    private TextView errorMessageDevice;
    private int deviceId = -1;
    private LinearLayout highlightedDeviceLine;

    protected LinearLayout makeTableInst(int id, String adress, String name, Integer width) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width / 6);

        LinearLayout line = new LinearLayout(this);
        line.setLayoutParams(params);
        TextView id_text = new TextView(this);
        id_text.setText(Integer.toString(id + 1));
        id_text.setGravity(Gravity.CENTER);
        id_text.setWidth(width / 6);
        id_text.setHeight(width / 6);
        id_text.setTextSize(17);
        id_text.setBackground(AppCompatResources.getDrawable(this, R.drawable.scrol_border));
        line.addView(id_text);
        LinearLayout twoRows = new LinearLayout(this);
        twoRows.setOrientation(LinearLayout.VERTICAL);
        //Название девайса
        TextView name_text = new TextView(this);
        name_text.setText(String.format("Имя:%s", name));
        name_text.setPadding(15, 0, 0, 0);
        name_text.setTextSize(17);
        name_text.setHeight(width / 12);
        name_text.setWidth(width - width / 6);
        name_text.setGravity(Gravity.CENTER_VERTICAL);

        twoRows.addView(name_text);
        //Адрес
        TextView adress_text = new TextView(this);
        adress_text.setText(String.format("Адрес:%s", adress));
        adress_text.setPadding(15, 0, 0, 0);
        adress_text.setHeight(width / 12);
        adress_text.setWidth(width - width / 6);
        adress_text.setTextSize(17);
        adress_text.setGravity(Gravity.CENTER_VERTICAL);

        twoRows.addView(adress_text);
        line.addView(twoRows);
        View overlay_view = new View(this);

        overlay_view.setAlpha((float) 0.6);
        overlay_view.setBackgroundColor(getColor(R.color.black));
        line.addView(overlay_view);
        line.setBackground(AppCompatResources.getDrawable(this, R.drawable.scrol_border));
        overlay_view.setVisibility(View.INVISIBLE);
        var context = this;
        line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceId != -1) {
                    highlightedDeviceLine.setBackground(AppCompatResources.getDrawable(context, R.drawable.scrol_border));
                }
                deviceId = id;
                highlightedDeviceLine = line;
                line.setBackground(AppCompatResources.getDrawable(context, R.drawable.border_in_gray));
            }
        });
        return line;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null) {

                    return;
                }
                String deviceName;
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    deviceName = "";
                } else {
                    deviceName = device.getName();
                }
                String deviceHardwareAddress = device.getAddress(); // MAC addres
                synchronized (this) {
                    if (deviceAddressSet.add(deviceHardwareAddress)) {
                        devicesData.add(new Object[]{deviceHardwareAddress, deviceName});// s
                        tableLayoutDevice.addView(makeTableInst(devicesData.size() - 1, deviceHardwareAddress, deviceName, tableLayoutDevice.getWidth()));
                    }
                }
            }
            if (STOPWIRING.equals(action)) {
                overlay.setVisibility(View.INVISIBLE);
                if (userData.wiring_result == 1) {
                    errorMessageDevice.setText("Этот адрес уже привязан к другому студенту");
                    return;
                }
                if (userData.wiring_result == 3) {
                    errorMessageDevice.setText("UNKNOWN ERROR");
                    return;
                }
                if (userData.wiring_result == 0) {
                    overlay.setVisibility(View.VISIBLE);
                    LoginPage.userData.updateData();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (userData.is_updating_data)
                                    sleep(25);
                                Intent intentf = new Intent();
                                intentf.setAction(STOPUPDATINGF);
                                intentf.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                sendBroadcast(intentf);
                            } catch (InterruptedException e) {

                                throw new RuntimeException(e);
                            }
                        }
                    }).start();

                }
            }
            if (STOPUPDATINGF.equals(action)) {
                overlay.setVisibility(View.INVISIBLE);
                finish();
            }
        }
    };


    protected void tryToWire() {
        if (deviceId == -1) {
            errorMessageDevice.setText("Нажмите на выбранное устройство");
            return;
        }
        if (!isNetworkConnected()) {
            errorMessageDevice.setText("Привязывать устройства можно только при подключении к сети");
            return;
        }
        userData.is_wiring = true;
        overlay.setVisibility(View.VISIBLE);
        userData.requestWiring((String) devicesData.get(deviceId)[0], studentId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (userData.is_wiring)
                        sleep(25);
                    Intent intent = new Intent();
                    intent.setAction(STOPWIRING);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(intent);
                } catch (InterruptedException e) {

                    throw new RuntimeException(e);
                }
            }
        }).start();


    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userData = LoginPage.userData;
        setContentView(R.layout.activity_pick_device);
        errorMessageDevice = findViewById(R.id.errorMessageDevice);
        Button confirmBtn = findViewById(R.id.confirmDeviceBtn);
        Button returnBtn = findViewById(R.id.ret_button_dev);
        TextView header = findViewById(R.id.TextThingDevice);
        overlay = findViewById(R.id.deviceOverlay);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(NOTWIRED);
                finish();

            }
        });
        //данные с  прошлой страницы
        Intent lastintent = getIntent();
        studentName = lastintent.getStringExtra("student_name");
        studentId = lastintent.getIntExtra("student_id", -1);
        header.setText(String.format("Выберите устройство для \"%s\"", studentName));
        deviceId = -1;

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToWire();
            }
        });
        tableLayoutDevice = findViewById(R.id.tableLayoutDevice);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
        }

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter intentFilter2 = new IntentFilter(STOPWIRING);
        IntentFilter intentFilter3 = new IntentFilter(STOPUPDATINGF);
        registerReceiver(receiver, intentFilter);
        registerReceiver(receiver, intentFilter2, RECEIVER_EXPORTED);
        registerReceiver(receiver, intentFilter3, RECEIVER_EXPORTED);
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
        unregisterReceiver(receiver);
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.

    }
}