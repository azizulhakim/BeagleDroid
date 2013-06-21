/*
 * Copyright 2013 Vlad V. Ungureanu <ungureanuvladvictor@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this Github repository and wiki except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.vvu.beagledroid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

/**
 * created by vvu on 6/20/13.
 */

public class UsbConnection extends MainActivity {
	private final Context mApplicationContext;
	private final UsbManager myUsbManager;
	private final int VID;
	private final int PID;
	private final String TAG = "UsbConnection";
	protected static final String ACTION_USB_PERMISSION = "com.vvu.USBBoot.USB";

	public UsbConnection(Activity parentActivity,
						 int vid, int pid) {
		mApplicationContext = parentActivity.getApplicationContext();
		myUsbManager = (UsbManager) mApplicationContext
				.getSystemService(Context.USB_SERVICE);
		VID = vid;
		PID = pid;
	}

	public void init() {
		enumerate(new IPermissionListener() {
			@Override
			public void onPermissionDenied(UsbDevice d) {
				UsbManager usbman = (UsbManager) mApplicationContext
						.getSystemService(Context.USB_SERVICE);
				PendingIntent pi = PendingIntent.getBroadcast(
						mApplicationContext, 0, new Intent(
						ACTION_USB_PERMISSION), 0);
				mApplicationContext.registerReceiver(mPermissionReceiver,
						new IntentFilter(ACTION_USB_PERMISSION));
				usbman.requestPermission(d, pi);
			}
		});
	}

	private static interface IPermissionListener {
		void onPermissionDenied(UsbDevice d);
	}

	private void enumerate(IPermissionListener listener) {
		HashMap<String, UsbDevice> devlist = myUsbManager.getDeviceList();
		Iterator<UsbDevice> deviter = devlist.values().iterator();
		while (deviter.hasNext()) {
			UsbDevice d = deviter.next();
			Log.d(TAG, "Found device: "
					+ String.format("%04X:%04X", d.getVendorId(),
					d.getProductId()));
			if (d.getVendorId() == VID && d.getProductId() == PID) {
				if (!myUsbManager.hasPermission(d))
					listener.onPermissionDenied(d);
				else {
					getInfo(d);
					return;
				}
				break;
			}
		}
		Log.d(TAG, "no more devices found");
	}

	private BroadcastReceiver mPermissionReceiver = new PermissionReceiver(
			new IPermissionListener() {
				@Override
				public void onPermissionDenied(UsbDevice d) {
					Log.d(TAG, "Permission denied on " + d.getDeviceId());
				}
			});

	private class PermissionReceiver extends BroadcastReceiver {
		private final IPermissionListener mPermissionListener;

		public PermissionReceiver(IPermissionListener permissionListener) {
			mPermissionListener = permissionListener;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			mApplicationContext.unregisterReceiver(this);
			if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
				if (!intent.getBooleanExtra(
						UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					mPermissionListener.onPermissionDenied((UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE));
				} else {
					Log.d(TAG, "Permission granted");
					UsbDevice dev = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (dev != null) {
						if (dev.getVendorId() == VID
								&& dev.getProductId() == PID) {
							getInfo(dev);
						}
					} else {
						Log.e(TAG, "device not present!");
					}
				}
			}
		}

	}

	private void getInfo(UsbDevice myDevice) {
		Log.d(TAG, "Getting Info!");

		UsbDeviceConnection myConnection = myUsbManager.openDevice(myDevice);

		if (!myConnection.claimInterface(myDevice.getInterface(1), true)) {
			Log.e(TAG, "Cannot claim interface!");
		}

		UsbEndpoint endIN;

		endIN = myDevice.getInterface(1).getEndpoint(0);

		Log.d(TAG, endIN.toString());
		byte[] buffer = new byte[500];
		boolean me = true;
		int tmp = -1;
		while (me) {
			tmp = myConnection.bulkTransfer(endIN, buffer, 500, 0);
			Log.d(TAG, "Received " + tmp + ".");
			HexDump myDumper = new HexDump();
			Log.d(TAG, myDumper.dumpHexString(buffer));
			if (tmp >= 0) me = false;
		}
	}
}

