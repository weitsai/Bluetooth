package com.example.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// 藍芽裝置是否開啟
	private final int REQUEST_ENABLE_BT = 1;
	// 藍芽裝置回傳資料
	private final int BluetoothDeviceRequest = 2;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket mBluetoothSocket = null;
	private LinkedHashSet<String> bluetoothDevicesName;
	private LinkedHashSet<BluetoothDevice> bluetoothDevices;
	private ListView mBluetoothList;
	private TextView mBluetoothData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViews();
		mBluetoothAdapter = checkBluetooth();
		if (mBluetoothAdapter == null) {
			return;
		}

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// // 取得目前已經配對過的裝置
		bluetoothDevices = new LinkedHashSet<BluetoothDevice>(mBluetoothAdapter.getBondedDevices());
		bluetoothDevicesName = new LinkedHashSet<String>();
		// 如果已經有配對過的裝置
		if (bluetoothDevices.size() > 0) {
			// 把裝置名稱以及MAC Address印出來
			for (BluetoothDevice device : bluetoothDevices) {
				bluetoothDevicesName.add("[已配對]" + "\n" + device.getName() + "\n" + device.getAddress());
			}
		}

		// searchBluetooth();

		mBluetoothList.setAdapter(new BtAdapter(this, bluetoothDevicesName));
		mBluetoothList.setOnItemClickListener(listListener);

	}

	// 偵測藍芽廣播
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			// 當收尋到裝置時
			if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
				// 取得藍芽裝置這個物件
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				bluetoothDevicesName.add("[搜尋到]" + device.getName() + "\n" + device.getAddress());
			}
			mBluetoothList.setAdapter(new BtAdapter(MainActivity.this, bluetoothDevicesName));
		}
	};

	/**
	 * 停止收尋裝置
	 */
	private void cancelBluetooth() {
		mBluetoothAdapter.cancelDiscovery();
	}

	/**
	 * 搜尋藍芽裝置
	 */
	private void searchBluetooth() {
		// 註冊一個BroadcastReceiver，等等會用來接收搜尋到裝置的消息
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);
		mBluetoothAdapter.startDiscovery(); // 開始搜尋裝置
	}

	/**
	 * 判斷藍芽裝置是否正常及開啟
	 * 
	 * @return 藍芽裝置是否正常及開啟 (true = 沒問題)
	 */
	private BluetoothAdapter checkBluetooth() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// 裝置不支援藍芽
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "本裝置不支援藍芽", Toast.LENGTH_SHORT).show();
			finish();
			return null;
		}

		// 藍芽沒有開啟
		if (!mBluetoothAdapter.isEnabled()) {
			Intent mIntentOpenBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(mIntentOpenBT, REQUEST_ENABLE_BT);
			return null;
		}

		return mBluetoothAdapter;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT) {
			if (!(resultCode == RESULT_OK)) {
				Toast.makeText(this, "不開啟藍芽無法使用，因此關閉程式", Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}

	private void findViews() {
		mBluetoothList = (ListView) findViewById(R.id.Devices_list);
		mBluetoothData = (TextView) findViewById(R.id.Devices_return);
	}

	OnItemClickListener listListener = new OnItemClickListener() {
		@SuppressLint("NewApi")
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			cancelBluetooth();

			Object[] bluetoothDevicesArrays = bluetoothDevices.toArray();
			try {
				System.out.println(((BluetoothDevice) bluetoothDevicesArrays[arg2]).getName());
				mBluetoothSocket = ((BluetoothDevice) bluetoothDevicesArrays[arg2]).createInsecureRfcommSocketToServiceRecord(SPP_UUID);
				mBluetoothSocket.connect();
				Thread t = new Thread() {
					public void run() {
						while (true) {
							try {
								InputStream input = mBluetoothSocket.getInputStream();
								Message mMessage2 = new Message();
								mMessage2.what = 5;
								mHandler.sendMessage(mMessage2);
								int tmp = input.read();
								System.out.println(tmp);
								if (tmp == 255) {
									Message mMessage = new Message();
									Bundle mBundle = new Bundle();
									mMessage.what = BluetoothDeviceRequest;
									int tmp1 = input.read();
									mBundle.putInt("wave", tmp1);
									int tmp2 = input.read();
									mBundle.putInt("w/", tmp2);
									int tmp3 = input.read();
									mBundle.putInt("pulse", tmp3);
									int tmp4 = input.read();
									mBundle.putInt("spo2", tmp4);
									mMessage.setData(mBundle);
									mHandler.sendMessage(mMessage);
								}
								Thread.sleep(250);
							} catch (Exception e) {
							}

						}
					}
				};
				t.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	};

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothDeviceRequest:
				Bundle date = msg.getData();
				mBluetoothData.setText("wave" + date.get("wave").toString() + " w/" + date.get("w/").toString() + "\npulse" + date.get("npulse").toString() + "\nspo2" + date.get("nspo2").toString() + "\n");
				break;
			case 5:
				mBluetoothData.setText("開始接值");
				break;
			}
		}
	};
}
