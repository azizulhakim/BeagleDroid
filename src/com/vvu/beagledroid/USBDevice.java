package com.vvu.beagledroid;

import java.util.HashMap;
import java.util.Iterator;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class USBDevice {
	final static String TAG = "USBDevice";
	
	UsbDevice myDev = null;
	UsbManager manager = null;
	UsbInterface intf = null;
	UsbEndpoint readEP = null;
	UsbEndpoint writeEP = null;
	UsbDeviceConnection connection = null;
	int vID;
	int pID;

	USBDevice(int vID, int pID, Object context) {
		this.manager = (UsbManager) context;
		this.vID = vID;
		this.pID = pID;
	}
	
	public void init() {
		HashMap<String, UsbDevice> deviceList = this.manager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while (deviceIterator.hasNext()) {
			UsbDevice dev = deviceIterator.next();
			if (dev.getProductId() == this.pID && dev.getVendorId() == this.vID)
				this.myDev = dev;
		}
		if (this.myDev == null) {
			Log.d(TAG, "Please connect the BBW/BBB");
			return;
		}
		this.intf = this.myDev.getInterface(1);
		this.readEP = this.intf.getEndpoint(0);
		this.writeEP = this.intf.getEndpoint(1);
		if (this.manager.hasPermission(this.myDev)) {
			this.connection = this.manager.openDevice(this.myDev);
		} 
		else Log.d(TAG, "no permissions");
		if ( this.connection.claimInterface(this.intf, true) == true) Log.d(TAG, "claim ok!");
		else Log.d(TAG, "Claim not ok!");
	}
	
	public int read(byte[] buffer, int len) {
		int tmp = this.connection.bulkTransfer(this.readEP, buffer, len, 10);
		while (tmp < 0) tmp = this.connection.bulkTransfer(this.readEP, buffer, len, 10);
		return tmp;
	}
	
	public int write(byte[] buffer, int len) {
		int tmp = this.connection.bulkTransfer(this.writeEP, buffer, len, 70);
		while (tmp < 0) tmp = this.connection.bulkTransfer(this.writeEP, buffer, len, 70);
		return tmp;
	}
}
