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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserDialog;


public class MainActivity extends Activity {
	
	static final String TAG = "BBB";
	String downloadedFile;
	static final byte[] AndroidMac = {(byte)0x9A, (byte)0x1F, (byte)0x85, (byte)0x1C, (byte)0x3D, (byte)0x0E};
	static final byte[] BBBIP = {(byte)0xC0, (byte)0xA8, (byte)0x01, (byte)0x03};
	static final byte[] AndroidIP = {(byte)0xC0, (byte)0xA8, (byte)0x01, (byte)0x09};
	static final byte[] serverName = {(byte)'A', (byte)'n', (byte)'d', (byte)'r', (byte)'o', (byte)'i', (byte)'d', (byte)'\0'};
	static final byte[] fileName = {(byte)'M', (byte)'L', (byte)'O', (byte)'\0'};
	static final byte[] END = {(byte)'F', (byte)'I', (byte)'N'};
	static final byte protocolUDP = (byte) 0x11;
	static final short ethARP = (short) 0x0806;
	static final short ethIP = (short) 0x0800;
	static final short BOOTPS = (short) 67;
	static final short BOOTPC = (short) 68;
	static final short romxID = 1;
	static final int RNDISSize = 44;
	static final int ETHSize = 14;
	static final int IPSize = 20;
	static final int ARPSize = 28;
	static final int UDPSize = 8;
	static final int BOOTPSize = 300;
	static final int TFTPSize = 4;
	long soFar = 0;
	long downloadID;
	int downloadCheck;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button flasher = (Button) findViewById(R.id.button1);
        Button downloader = (Button) findViewById(R.id.button3);
        Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
    	
        flasher.setOnClickListener(flashListener);
        spinner1.setAdapter(setupSpinner());
    	spinner1.setOnItemSelectedListener(spinnerListener);
        
    	checkRunningDownloads();
        setupDir();
        
        downloader.setOnClickListener(downloadListener);
        
        IntentFilter downloadIntent = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, downloadIntent);
       }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
    	DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    	manager.remove(downloadID);
    	Log.d(TAG, "Destroy");
    }
	
	/**
	 * Sets up the working folder for the app /sdcard/BBB
	 */
	public void setupDir() {
		File f = new File(Environment.getExternalStorageDirectory() + "/BBB");
		if(!f.exists()){
			String newFolder = "/BBB";
			String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
			File myNewFolder = new File(extStorageDirectory + newFolder);
			myNewFolder.mkdir();	
		}
	}
	
	/**
	 * Sends the MLO to the AM335x
	 * 
	 * @param myDev UsbDevice that represents the AM335x in USB Boot mode
	 */
	public void runRom(final UsbDevice myDev) {
		new Thread(new Runnable() {
			public void run() {
				byte[] buffer = new byte[450];
				UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
				
				UsbInterface intf = myDev.getInterface(1);
				UsbEndpoint readEP = intf.getEndpoint(0);
				UsbEndpoint writeEP = intf.getEndpoint(1);
				UsbDeviceConnection connection = null;
				while (!manager.hasPermission(myDev)){}
				if (manager.hasPermission(myDev)) {
					connection = manager.openDevice(myDev);
				} 
				else Log.d(TAG, "No permissions on ROM!");
				
				connection.claimInterface(intf, true);
				int tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				while (tmp < 0) tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				byte[] BBBMac = Arrays.copyOfRange(Arrays.copyOfRange(buffer, 
						RNDISSize, RNDISSize + ETHSize),6,12);
				BOOTP bootpAnswer = new BOOTP(romxID, BBBIP, AndroidIP, AndroidIP, BBBMac, serverName, fileName);
				UDP udpAnswer = new UDP(BOOTPS, BOOTPC, (short)BOOTPSize);
				IPv4 ipAnswer = new IPv4((short) (udpAnswer.getLen() + (short)IPSize), (short)0, protocolUDP, AndroidIP, BBBIP);
				Ether2 etherAnswer = new Ether2(BBBMac, AndroidMac, ethIP);
				RNDIS rndisAnswer = new RNDIS(ETHSize + IPSize + UDPSize + BOOTPSize);
				ByteMaker send = new ByteMaker();
				byte[] output = send.converter(rndisAnswer.getByteArray(), etherAnswer.getByteArray(), ipAnswer.getByteArray(), udpAnswer.getByteArray(), bootpAnswer.getByteArray());
				tmp = connection.bulkTransfer(writeEP, output, output.length, 10);
				tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				while (tmp < 0) tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				ARP arpAnswer = new ARP((short)2, AndroidMac, AndroidIP, BBBMac, BBBIP);
				etherAnswer.setH_proto(ethARP);
				rndisAnswer.updateRNDIS(ETHSize+ARPSize);
				
				output = send.converter(rndisAnswer.getByteArray(), etherAnswer.getByteArray(), arpAnswer.getByteArray());
				tmp = connection.bulkTransfer(writeEP, output, output.length, 10);
				tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				while (tmp < 0) tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				
				try {
					AssetManager assetManager = getAssets();
					InputStream fIn = assetManager.open("u-boot-spl.bin");
					byte[] temporary = new byte[512];
					int block = 1;
					int count = fIn.read(temporary, 0, 512);
					while(count != -1) {
						TFTP tftpAnswer = new TFTP((short)3, (short)block);
						udpAnswer = new UDP((short)0x45, (short)0x4D2, (short) (TFTPSize + count));
						ipAnswer = new IPv4((short) (udpAnswer.getLen() + (short)IPSize), (short)0, protocolUDP, AndroidIP, BBBIP);
						etherAnswer = new Ether2(BBBMac, AndroidMac, ethIP);
						rndisAnswer = new RNDIS(ETHSize + IPSize + UDPSize + TFTPSize + count);
						tftpAnswer.setBlk_numer((short)block);
						
						output = send.converter(rndisAnswer.getByteArray(), etherAnswer.getByteArray(), ipAnswer.getByteArray(), udpAnswer.getByteArray(), tftpAnswer.getByteArray(), temporary);
						
						byte[] result = send.stripSize(output, RNDISSize + ETHSize + IPSize + UDPSize + TFTPSize + count);
						tmp = connection.bulkTransfer(writeEP, result, result.length, 200);
						buffer = new byte[450];
						tmp = connection.bulkTransfer(readEP, buffer, 450, 200);
						temporary = new byte[512];
						block++;
						count = fIn.read(temporary, 0, 512);
					}
					fIn.close();
					connection.close();
				} catch (Exception e) {
					Log.d(TAG, e.getMessage());
					return;
				}

			}
		}).start();
	}
	
	/**
	 * Sends the U-Boot to the MLO
	 * 
	 * @param myDev UsbDevice that represents the MLO in USB Boot mode
	 */
	public void runUBoot(final UsbDevice myDev) {
		new Thread(new Runnable() {
			public void run() {
				byte[] buffer = new byte[450];
				UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
				
				UsbInterface intf = myDev.getInterface(1);
				UsbEndpoint readEP = intf.getEndpoint(0);
				UsbEndpoint writeEP = intf.getEndpoint(1);
				UsbDeviceConnection connection = null;
				
				while (!manager.hasPermission(myDev)){}
				if (manager.hasPermission(myDev)) {
					connection = manager.openDevice(myDev);
				} 
				else Log.d(TAG, "No permissions on device running MLO!");
				
				connection.claimInterface(intf, true);
				
				int tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				while (tmp < 0) tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				byte[] BBBMac = Arrays.copyOfRange(Arrays.copyOfRange(buffer, 
						RNDISSize, RNDISSize + ETHSize),6,12);
				byte[] bootp = Arrays.copyOfRange(buffer, RNDISSize + ETHSize + 
												IPSize + UDPSize, RNDISSize + ETHSize + 
												IPSize + UDPSize + BOOTPSize);
				byte[] xid = Arrays.copyOfRange(bootp, 4, 8);
				BOOTP bootpAnswer = new BOOTP(ByteBuffer.wrap(xid).getInt(), BBBIP, AndroidIP, 
												AndroidIP, BBBMac, serverName, fileName);
				UDP udpAnswer = new UDP(BOOTPS, BOOTPC, (short)BOOTPSize);
				IPv4 ipAnswer = new IPv4((short) (udpAnswer.getLen() + (short)IPSize), (short)0, 
													protocolUDP, AndroidIP, BBBIP);
				Ether2 etherAnswer = new Ether2(BBBMac, AndroidMac, ethIP);
				RNDIS rndisAnswer = new RNDIS(ETHSize + IPSize + UDPSize + BOOTPSize);
				ByteMaker send = new ByteMaker();
				byte[] output = send.converter(rndisAnswer.getByteArray(), etherAnswer.getByteArray(), 
												ipAnswer.getByteArray(), udpAnswer.getByteArray(), 
												bootpAnswer.getByteArray());
				tmp = connection.bulkTransfer(writeEP, output, output.length, 10);
				tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				while (tmp < 0) tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				ARP arpAnswer = new ARP((short)2, AndroidMac, AndroidIP, BBBMac, BBBIP);
				etherAnswer.setH_proto(ethARP);
				rndisAnswer.updateRNDIS(ETHSize+ARPSize);
				
				output = send.converter(rndisAnswer.getByteArray(), etherAnswer.getByteArray(), arpAnswer.getByteArray());
				tmp = connection.bulkTransfer(writeEP, output, output.length, 10);
				tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				while (tmp < 0) tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				byte[] udp = Arrays.copyOfRange(buffer, RNDISSize +ETHSize + IPSize, RNDISSize +ETHSize + IPSize + UDPSize);
				byte[] dstPort = Arrays.copyOfRange(udp, 0, 2);
				byte[] srcPort = Arrays.copyOfRange(udp, 2, 4);
				short sPort = ByteBuffer.wrap(srcPort).getShort();
				short dPort = ByteBuffer.wrap(dstPort).getShort();
				
				try {
					AssetManager assetManager = getAssets();
					InputStream fIn = assetManager.open("u-boot.img");
					byte[] temporary = new byte[512];
					int block = 1;
					int count = fIn.read(temporary, 0, 512);
					while(count != -1) {
						TFTP tftpAnswer = new TFTP((short)3, (short)block);
						udpAnswer = new UDP(sPort, dPort, (short) (TFTPSize + count));
						ipAnswer = new IPv4((short) (udpAnswer.getLen() + (short)IPSize), (short)0, protocolUDP, AndroidIP, BBBIP);
						etherAnswer = new Ether2(BBBMac, AndroidMac, ethIP);
						rndisAnswer = new RNDIS(ETHSize + IPSize + UDPSize + TFTPSize + count);
						tftpAnswer.setBlk_numer((short)block);
						
						output = send.converter(rndisAnswer.getByteArray(), etherAnswer.getByteArray(), 
												ipAnswer.getByteArray(), udpAnswer.getByteArray(), 
												tftpAnswer.getByteArray(), temporary);
						
						byte[] result = send.stripSize(output, RNDISSize + ETHSize + IPSize + UDPSize + TFTPSize + count);
						tmp = connection.bulkTransfer(writeEP, result, result.length, 200);
						buffer = new byte[450];
						tmp = connection.bulkTransfer(readEP, buffer, 450, 200);
						temporary = new byte[512];
						block++;
						count = fIn.read(temporary, 0, 512);
					}
					fIn.close();
					connection.close();
				} catch (Exception e) {
					Log.d(TAG, e.getMessage());
					return;
				}
			}
		}).start();
	}
	
	/**
	 * Sends the FIT(Flatten Image Tree) to U-Boot that is doing TFTP Boot via RNDIS
	 * 
	 * @param myDev UsbDevice that represents the U-Boot in TFTP/RNDIS Boot mode
	 */
	public void runFIT(final UsbDevice myDev) {
		new Thread(new Runnable() {
			public void run() {
				byte[] buffer = new byte[450];
				UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
				
				UsbInterface intf = myDev.getInterface(1);
				UsbEndpoint readEP = intf.getEndpoint(0);
				UsbEndpoint writeEP = intf.getEndpoint(1);
				UsbDeviceConnection connection = null;
				
				while (!manager.hasPermission(myDev)){}
				if (manager.hasPermission(myDev)) {
					connection = manager.openDevice(myDev);
				} 
				
				connection.claimInterface(intf, true);
				int tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				while (tmp < 0) tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				byte[] BBBMac = Arrays.copyOfRange(Arrays.copyOfRange(buffer, 
						RNDISSize, RNDISSize + ETHSize),6,12);
				
				UDP udpAnswer = new UDP(BOOTPS, BOOTPC, (short)BOOTPSize);
				IPv4 ipAnswer = new IPv4((short) (udpAnswer.getLen() + (short)IPSize), (short)0, 
													protocolUDP, AndroidIP, BBBIP);
				Ether2 etherAnswer = new Ether2(BBBMac, AndroidMac, ethIP);
				RNDIS rndisAnswer = new RNDIS(ETHSize + IPSize + UDPSize + BOOTPSize);
				ByteMaker send = new ByteMaker();
				ARP arpAnswer = new ARP((short)2, AndroidMac, AndroidIP, BBBMac, BBBIP);
				etherAnswer.setH_proto(ethARP);
				rndisAnswer.updateRNDIS(ETHSize+ARPSize);
				
				byte[] output = send.converter(rndisAnswer.getByteArray(), etherAnswer.getByteArray(), arpAnswer.getByteArray());
				tmp = connection.bulkTransfer(writeEP, output, output.length, 10);
				tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				while (tmp < 0) tmp = connection.bulkTransfer(readEP, buffer, 450, 10);
				byte[] udp = Arrays.copyOfRange(buffer, RNDISSize +ETHSize + IPSize, RNDISSize +ETHSize + IPSize + UDPSize);
				byte[] dstPort = Arrays.copyOfRange(udp, 0, 2);
				byte[] srcPort = Arrays.copyOfRange(udp, 2, 4);
				short sPort = ByteBuffer.wrap(srcPort).getShort();
				short dPort = ByteBuffer.wrap(dstPort).getShort();
				
				try {
					AssetManager assetManager = getAssets();
					InputStream fIn = assetManager.open("maker.itb");
					byte[] temporary = new byte[512];
					int block = 1;
					int count = fIn.read(temporary, 0, 512);
					while(count != -1) {
						TFTP tftpAnswer = new TFTP((short)3, (short)block);
						udpAnswer = new UDP(sPort, dPort, (short) (TFTPSize + count));
						ipAnswer = new IPv4((short) (udpAnswer.getLen() + (short)IPSize), (short)0, protocolUDP, AndroidIP, BBBIP);
						etherAnswer = new Ether2(BBBMac, AndroidMac, ethIP);
						rndisAnswer = new RNDIS(ETHSize + IPSize + UDPSize + TFTPSize + count);
						tftpAnswer.setBlk_numer((short)block);
						
						output = send.converter(rndisAnswer.getByteArray(), etherAnswer.getByteArray(), 
												ipAnswer.getByteArray(), udpAnswer.getByteArray(), 
												tftpAnswer.getByteArray(), temporary);
						
						byte[] result = send.stripSize(output, RNDISSize + ETHSize + IPSize + UDPSize + TFTPSize + count);
						tmp = connection.bulkTransfer(writeEP, result, result.length, 200);
						buffer = new byte[450];
						tmp = connection.bulkTransfer(readEP, buffer, 450, 200);
						temporary = new byte[512];
						block++;
						count = fIn.read(temporary, 0, 512);
					}
					fIn.close();
					connection.close();
				} catch (Exception e) {
					Log.d(TAG, e.getMessage());
					return;
				}
			}
		}).start();
	}
	
	/**
	 * Sends the image which will be flashed on the uSD or eMMC
	 * 
	 * @param fileName String that is the name of the image sent to the board for flashing
	 * @param myDev UsbDevice that represents the g_serial device from the BBB kernel
	 */
	public void runWrite(final String fileName, final UsbDevice myDev) {
		new Thread(new Runnable() {
			public void run() {
				UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
				
				UsbInterface intf = myDev.getInterface(1);
				UsbEndpoint writeEP = intf.getEndpoint(1);
				UsbEndpoint readEP = intf.getEndpoint(0);
				UsbDeviceConnection connection = null;
				
				while (!manager.hasPermission(myDev)){}
				if (manager.hasPermission(myDev)) {
					connection = manager.openDevice(myDev);
				} 
				
				if (connection == null) {
					Log.d(TAG, "conn null");
					return;
				}
				try {
					connection.claimInterface(intf, true);
				}
				catch (Exception e) {
					Log.d(TAG, e.getMessage());
					return;
				}
				int tmp;
				try {
					CheckBox sdCard = (CheckBox)findViewById(R.id.checkBox1);
					if (sdCard.isChecked()) {
						byte makeSD[] = {(byte)'y',(byte)'s', (byte)'d'};
						tmp = -1;
						while(tmp<0) tmp = connection.bulkTransfer(writeEP, makeSD, makeSD.length, 10);
					}
					else {
						byte makeSD[] = {(byte)'n', (byte)'s', (byte)'d'};
						tmp = -1;
						while(tmp<0) tmp = connection.bulkTransfer(writeEP, makeSD, makeSD.length, 10);
					}
					updateProgress(0);
					File myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/BBB/" + fileName);
					FileInputStream fIn = new FileInputStream(myFile);
					byte[] name = myFile.getName().getBytes();
					tmp = -1;
					while(tmp<0) tmp = connection.bulkTransfer(writeEP, name, name.length, 10);
					byte[] size = ByteBuffer.allocate(8).putLong(fIn.available()).array();
					tmp = -1;
					while(tmp < 0) tmp = connection.bulkTransfer(writeEP, size, size.length, 10);
					byte[] temporary = new byte[512];
					int count = fIn.read(temporary, 0, 512);
					byte[] reader = new byte[10];
					tmp = connection.bulkTransfer(readEP, reader, 5, 200);
					while (tmp < 0) tmp = connection.bulkTransfer(readEP, reader, 5, 10);
					int newVal = 0;
					int fullSize = fIn.available();
					while(count != -1) {
						int tmP = connection.bulkTransfer(writeEP, temporary, count, 0);
						while(tmP < 0) tmP = connection.bulkTransfer(writeEP, temporary, count, 10);
						tmp = connection.bulkTransfer(readEP, reader, 3, 0);
						while (tmp < 0) tmp = connection.bulkTransfer(readEP, reader, 3, 10);
						if (tmP >0) soFar += tmP;
						temporary = new byte[512];
						count = fIn.read(temporary, 0, 512);
						long temp = soFar*100/(long)fullSize;
						int value = (int)temp;
						if(value != newVal) {
							updateProgress(value);
							newVal = value;
						}
					}
					SystemClock.sleep(2000);
					tmp = -1;
					while(tmp < 0) tmp = connection.bulkTransfer(writeEP, END, 3, 10);
					fIn.close();
					soFar = 0;
					showToast("Done sending image");
					connection.close();
				} 
				catch (Exception e) {
					Log.d(TAG, e.getMessage());
					return;
				}
			}
		}).start();
	}
	
	/**
	 * Downloads a file and saves it locally
	 * 
	 * @param url String HTTP url with the file
	 * @param title String title which is set in the notification 
	 * @param description String description which is set in the notification
	 * @param fileName String local path where to save the file
	 * @return long ID of the download
	 */
	public long downloadFile(String url, String title, String description, String fileName) {
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setDescription(description);
		request.setTitle(title);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		request.setDestinationInExternalPublicDir("BBB/", fileName);
		DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		return manager.enqueue(request);
	}
	
	/**
	 * Converts an InputStream to String
	 * 
	 * @param is InputStream that will be converted to String
	 * @return String the text from the stream
	 * @throws Exception
	 */
	public static String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	        sb.append(line);
	    }
	    is.close();
	    return sb.toString();
	}
	
	/**
	 * Check if you are connected to the Internet
	 * 
	 * @return boolean status of the network connection
	 */
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if (ni == null) {
		   return false;
		  } else
		   return true;
		 }
	
	/**
	 * Downloads an image
	 * 
	 * @param item String the image which will be downloaded
	 * @throws Exception
	 */
	public void downloadImage(String item) throws Exception {
		if (isNetworkConnected()) {
			downloadCheck = 1;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Button downloader = (Button)findViewById(R.id.button3);
					String buttonText = (String)downloader.getText();
					if(buttonText.compareTo((String)"Download Image") == 0) {
						downloader.setText("Cancel download");
					}
					if(buttonText.compareTo((String)"Cancel download") == 0){
						downloader.setText("Download Image");
						DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
						manager.remove(downloadID);
						downloadCheck = 0;
					}
				}
			});
			SystemClock.sleep(1000);
			if(downloadCheck == 0 )return;
			if (item.equals("rPI-NOOBS_v1_2")) {
			Log.d(TAG, "NOOBS_v1_2");
			downloadID = downloadFile("http://raspberry.mythic-beasts.com/raspberry/images/NOOBS/NOOBS_v1_2_1/NOOBS_v1_2_1.zip", 
					"NOOBS_v1_2", "rPI", "NOOBS_v1_2_1.zip");
			downloadedFile = "NOOBS_v1_2_1.zip";
			}
		else if (item.equals("rPI-Raspbian 'wheezy'")) {
			Log.d(TAG, "Raspbian 'wheezy'");
			downloadID = downloadFile("http://raspberry.mythic-beasts.com/raspberry/images/raspbian/2013-07-26-wheezy-raspbian/2013-07-26-wheezy-raspbian.zip", 
					item, "rPI", "2013-07-26-wheezy-raspbian.zip");
			downloadedFile = "2013-07-26-wheezy-raspbian.zip";
			}
		else if (item.equals("rPI-Soft-float Debian 'wheezy'")) {
			Log.d(TAG, "Soft-float Debian 'wheezy'");
			downloadID = downloadFile("http://raspberry.mythic-beasts.com/raspberry/images/debian/7/2013-05-29-wheezy-armel/2013-05-29-wheezy-armel.zip",
					item, "rPI", "2013-05-29-wheezy-armel.zip");
			downloadedFile = "2013-05-29-wheezy-armel.zip";
			}
		else if (item.equals("rPI-Pidora")) {
			Log.d(TAG, "Pidora");
			downloadID = downloadFile("http://raspberry.mythic-beasts.com/raspberry/images/pidora/pidora-18-r1c/pidora-18-r1c.zip",
					item, "rPI", "pidora-18-r1c.zip");
			downloadedFile = "pidora-18-r1c.zip";
			}
		else if (item.equals("rPI-Arch Linux ARM")) {
			Log.d(TAG, "Arch Linux ARM");
			downloadID = downloadFile("http://raspberry.mythic-beasts.com/raspberry/images/archlinuxarm/archlinux-hf-2013-07-22/archlinux-hf-2013-07-22.img.zip",
					item, "rPI", "archlinux-hf-2013-07-22.img.zip");
			downloadedFile = "archlinux-hf-2013-07-22.img.zip";
			}
		else if (item.equals("rPI-RISC OS")) {
			Log.d(TAG, "RISC OS");
			downloadID = downloadFile("http://raspberry.mythic-beasts.com/raspberry/images/riscos/riscos-2013-07-10-RC11/riscos-2013-07-10-RC11.zip",
					item, "rPI", "riscos-2013-07-10-RC11.zip");
			downloadedFile = "riscos-2013-07-10-RC11.zip";
			}
		else if (item.equals("BBB-Angstrom")) {
			downloadID = downloadAngstrom();
		}
		else if (item.equals("Ubuntu Raring 13.04")) {
			Log.d(TAG, "Ubuntu Raring 13.04");
			downloadID = downloadFile("http://s3.armhf.com/debian/raring/bone/ubuntu-raring-13.04-armhf-3.8.13-bone20.img.xz", item, "Ubuntu", "ubuntu-raring-13.04-armhf-3.8.13-bone20.img.xz");
			downloadedFile = "ubuntu-raring-13.04-armhf-3.8.13-bone20.img.xz";
		}
		else if (item.equals("Ubuntu Precise 12.04.2 LTS")) {
			Log.d(TAG, "Ubuntu Precise 12.04.2 LTS");
			downloadID = downloadFile("http://s3.armhf.com/debian/precise/bone/ubuntu-precise-12.04.2-armhf-3.8.13-bone20.img.xz", item, "Ubuntu", "ubuntu-precise-12.04.2-armhf-3.8.13-bone20.img.xz");
			downloadedFile = "ubuntu-precise-12.04.2-armhf-3.8.13-bone20.img.xz";
		}
		}
		else {
			showToast("No internet connection!");
		}
	}
	
	/**
	 * Downloads the latest Angstrom image for the BBB/BBW
	 * 
	 * @return long the ID of the download
	 * @throws Exception
	 */
	public long downloadAngstrom() throws Exception {
		if(isNetworkConnected())
			try {
				URL input = new URL("http://beagleboard.org/latest-images");
				URLConnection conn = input.openConnection();
				int contentLength = conn.getContentLength();
				byte[] buffer = new byte[contentLength];
				DataInputStream stream = new DataInputStream(input.openStream());
				
				stream.readFully(buffer);
				stream.close();
				
				DataOutputStream output = new DataOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory() + "/BBB/page.html"));
				output.write(buffer);
				output.flush();
				output.close();
				
				File fl = new File(Environment.getExternalStorageDirectory() + "/BBB/page.html");
			    FileInputStream fin = new FileInputStream(fl);
			    StringBuffer fileContent = new StringBuffer("");
			    File checkExist = new File(Environment.getExternalStorageDirectory() + "/BBB/Angstrom.img.xz");
			    if(checkExist.exists()) checkExist.delete();
			    
			    byte[] buff = new byte[fin.available()];
			    while (fin.read(buff) != -1) {
			        fileContent.append(new String(buff));
			    }
			    
			    int start = fileContent.toString().indexOf("https://s3.");
			    int end = fileContent.toString().indexOf(".img.xz");
			    
			    fin.close();        
			    String filename = fileContent.toString().substring(start, end+7);
			    downloadedFile = "Angstrom.img.xz";
			    
			    fl.delete();
			    
			    return downloadFile(fileContent.toString().substring(start, end+7),
			    		"Angstrom", filename, "Angstrom.img.xz");
			    } catch(FileNotFoundException e) {
				
				} catch (IOException e) {
					Log.d(TAG, e.getMessage());
					}
		else {
			showToast("No internet connection!");
		}
		return -1;
	}
	
	private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		 @Override
		 public void onReceive(Context arg0, Intent arg1) {
			 checkDwnloadStatus();
		 }
		};
	
	/**
	 * Checks if there are any running downloads at the moment. Does not let you to download multiple images at th
	static final int RNDISSize = 44;
	static final int ETHSize = 14;
	static final int IPSize = 20;
	static final int ARPSize = 28;
	static final int UDPSize = 8;e same time.
	 */
	private void checkRunningDownloads() {
		DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterByStatus(DownloadManager.STATUS_RUNNING);
		Cursor cursor = downloadManager.query(query);
		if(cursor.moveToFirst()) {
			int index= cursor.getColumnIndex(DownloadManager.COLUMN_ID);
			downloadID = cursor.getLong(index);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Button downloader = (Button)findViewById(R.id.button3);
					String buttonText = (String)downloader.getText();
					if(buttonText.compareTo((String)"Download Image") == 0) {
						downloader.setText("Cancel download");
					}
				}
			});
		}
	}
		
	/**
	 * Checks the status of the enqueued download.
	 */
	private void checkDwnloadStatus(){
			DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			 DownloadManager.Query query = new DownloadManager.Query();
			 query.setFilterById(downloadID);
			 Cursor cursor = downloadManager.query(query);
			 if(cursor.moveToFirst()){
			  int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
			  int status = cursor.getInt(columnIndex);
			  int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
			  int reason = cursor.getInt(columnReason);
			  int nameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
			  String name = cursor.getString(nameIndex);
			 
			  switch(status){
			  case DownloadManager.STATUS_FAILED:
			   String failedReason = "";
			   switch(reason){
			   case DownloadManager.ERROR_CANNOT_RESUME:
			    failedReason = "ERROR_CANNOT_RESUME";
			    break;
			   case DownloadManager.ERROR_DEVICE_NOT_FOUND:
			    failedReason = "ERROR_DEVICE_NOT_FOUND";
			    break;
			   case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
			    failedReason = "ERROR_FILE_ALREADY_EXISTS";
			    break;
			   case DownloadManager.ERROR_FILE_ERROR:
			    failedReason = "ERROR_FILE_ERROR";
			    break;
			   case DownloadManager.ERROR_HTTP_DATA_ERROR:
			    failedReason = "ERROR_HTTP_DATA_ERROR";
			    break;
			   case DownloadManager.ERROR_INSUFFICIENT_SPACE:
			    failedReason = "ERROR_INSUFFICIENT_SPACE";
			    break;
			   case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
			    failedReason = "ERROR_TOO_MANY_REDIRECTS";
			    break;
			   case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
			    failedReason = "ERROR_UNHANDLED_HTTP_CODE";
			    break;
			   case DownloadManager.ERROR_UNKNOWN:
			    failedReason = "ERROR_UNKNOWN";
			    break;
			   }

			   Toast.makeText(getApplicationContext(),
			     "Failed: " + failedReason,
			     Toast.LENGTH_LONG).show();
			   break;
			  case DownloadManager.STATUS_SUCCESSFUL:
				  Toast.makeText(getApplicationContext(),
						  "Finished downloading image " + name + "!",
						  Toast.LENGTH_LONG).show();
				  runOnUiThread(new Runnable() {
					  public void run() {
						  Button downloader = (Button)findViewById(R.id.button3);
						  downloader.setText("Download Image");
						  }
					  }
				  );
				  break;
				  }
			  }
			 }	
	
	/**
	 * Checks if you have in the local storage the selected image.
	 * 
	 * @return boolean true if the image is found, false else
	 */
	private boolean checkImage() {
		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		String spinnerSelected = (String)spinner1.getSelectedItem();
		if(spinnerSelected.compareTo("BBB-Angstrom") == 0) {
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "/BBB/Angstrom.img.xz" );
			if(file.exists())
				downloadedFile = "Angstrom.img.xz";
			else {
				showToast("Please download the BBB-Angstrom image!");
				return false;
			}
		}
		else if(spinnerSelected.compareTo("rPI-NOOBS_v1_2") == 0){
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "/BBB/NOOBS_v1_2_1.zip" );
			if(file.exists())
				downloadedFile = "NOOBS_v1_2_1.zip";
			else {
				showToast("Please download the rPI-NOOBS_v1_2 image!");
				return false;
			}
		}
		else if(spinnerSelected.compareTo("rPI-Raspbian 'wheezy'") == 0){
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "/BBB/2013-07-26-wheezy-raspbian.zip" );
			if(file.exists())
				downloadedFile = "2013-07-26-wheezy-raspbian.zip";
			else {
				showToast("Please download the rPI-Raspbian 'wheezy' image!");
				return false;
			}
		}
		else if(spinnerSelected.compareTo("rPI-Soft-float Debian 'wheezy'") == 0){
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "/BBB/2013-05-29-wheezy-armel.zip" );
			if(file.exists())
				downloadedFile = "2013-05-29-wheezy-armel.zip";
			else {
				showToast("Please download the rPI-Soft-float Debian 'wheezy' image!");
				return false;
			}
		}
		else if(spinnerSelected.compareTo("rPI-Pidora") == 0){
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "/BBB/pidora-18-r1c.zip" );
			if(file.exists())
				downloadedFile = "pidora-18-r1c.zip";
			else {
				showToast("Please download the rPI-Pidora image!");
				return false;
			}
		}
		else if(spinnerSelected.compareTo("rPI-Arch Linux ARM") == 0){
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "/BBB/archlinux-hf-2013-07-22.img.zip" );
			if(file.exists())
				downloadedFile = "archlinux-hf-2013-07-22.img.zip";
			else {
				showToast("Please download the rPI-Arch Linux ARM image!");
				return false; 
			}
		}
		else if(spinnerSelected.compareTo("rPI-RISC OS") == 0){
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "/BBB/riscos-2013-07-10-RC11.zip" );
			if(file.exists())
				downloadedFile = "riscos-2013-07-10-RC11.zip";
			else {
				showToast("Please download the rPI-RISC OS image!");
				return false;
			}
		}
		else if(spinnerSelected.compareTo("Ubuntu Precise 12.04.2 LTS") == 0){
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "/BBB/ubuntu-precise-12.04.2-armhf-3.8.13-bone20.img.xz" );
			if(file.exists())
				downloadedFile = "ubuntu-precise-12.04.2-armhf-3.8.13-bone20.img.xz";
			else {
				showToast("Please download the Ubuntu Precise 12.04.2 LTS image!");
				return false;
			}
		}
		else if(spinnerSelected.compareTo("Ubuntu Raring 13.04") == 0){
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "/BBB/ubuntu-raring-13.04-armhf-3.8.13-bone20.img.xz" );
			if(file.exists())
				downloadedFile = "ubuntu-raring-13.04-armhf-3.8.13-bone20.img.xz";
			else {
				showToast("Please download the Ubuntu Raring 13.04 image!");
				return false;
			}
		}		
		else if(spinnerSelected.compareTo("Local File") == 0) return true;
		return true;
	}
	
	/**
	 * Shows the user a Toast message
	 * 
	 * @param message String the Toast text
	 */
	private void showToast(final String message) {
		runOnUiThread(new Runnable() {
			public void run() {
				Context context = getApplicationContext();
				CharSequence text = message;
				int duration = Toast.LENGTH_LONG;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
				}
			}
		);
	}
	    
	/**
	 * Searches for an UsbDevice with matching vID/pID
	 * 
	 * @param vID int Vendor ID
	 * @param pID int Product ID
	 * @param failMessage String message if the device is not found
	 * @param manager UsbManager System-Wide USB Manager
	 * @return UsbDevice the attached device which has the vID/pID specified
	 */
	private UsbDevice checkUsbDevice(int vID, int pID, String failMessage, UsbManager manager, long diffToCheckTo) {
		UsbDevice myDev = null;
		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		boolean found = false;
		Time start = new Time();
        start.setToNow();
		while(found == false) {
			deviceList = manager.getDeviceList();
			deviceIterator = deviceList.values().iterator();
			while(deviceIterator.hasNext()) {
				UsbDevice dev = deviceIterator.next();
				if(dev.getVendorId() == vID && dev.getProductId() == pID){
					myDev = dev;
					found = true;
				}
			}
			Time end = new Time();
            end.setToNow();
            long diff = TimeUnit.MILLISECONDS.toSeconds(end.toMillis(true)-start.toMillis(true));
            if(diff >= diffToCheckTo) {
            	myDev = null;
            	found = true;
            }
		}
		if (myDev == null) {
			Log.d(TAG, "MLO did not started correctly and it`s not found in the device list!");
			showToast(failMessage);
			return null;
		}
		return myDev;
	}
	
	/**
	 * @param value int Value to be set for the progress bar
	 */
	private void updateProgress(final int value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBar1);
				pBar.setProgress(value);
			}
		});
	}
	
    OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        	Button downloader = (Button) findViewById(R.id.button3);
    		if (parentView.getItemAtPosition(position).toString().compareTo("Local File") == 0) {
        		downloader.setEnabled(false);
        		FileChooserDialog dialog = new FileChooserDialog(parentView.getContext());
        		dialog.setFilter(".*zip|.*tar.gz|.*tar|.*img.tar.gz|.*img.xz|.*img|.*img.zip");
                dialog.loadFolder(Environment.getExternalStorageDirectory() + "/BBB/");
                dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
                    public void onFileSelected(Dialog source, File file) {
                        source.hide();
                        downloadedFile = file.getName();
                		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
                		spinner1.setSelection(spinner1.getCount()-1);
                        Toast toast = Toast.makeText(source.getContext(), "Image selected: " + file.getName(), Toast.LENGTH_LONG);
                        toast.show();
                    }
                    public void onFileSelected(Dialog source, File folder, String name) {
                        source.hide();
                    }
                });
                dialog.show();
        	}
        	else{
        		downloader.setEnabled(true);
        	}
        }
        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
        	Log.d(TAG, "DID NOT SELECT");
        }

    };
	
    OnClickListener flashListener = new OnClickListener() {
		public void onClick(View v) {
			new Thread(new Runnable() {
				public void run() {
					if (checkImage() == false) return;
					UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
					
					UsbDevice myDev = checkUsbDevice(1105, 24897, "Please connect the BeagleBone Black in USB boot mode!", manager, 0);
					if (myDev == null) {
						Log.d(TAG, "AM335x not in USB Boot mode!");
						return;
					}
					while (!manager.hasPermission(myDev)){}
					
					runRom(myDev);
					showToast("MLO started!");
					
					myDev = checkUsbDevice(1317, 42146, "U-Boot SPL did not started correctly!", manager, 5);
					if (myDev == null) {
						Log.d(TAG, "MLO error!");
						return;
					}
					while (!manager.hasPermission(myDev)){}
					
					runUBoot(myDev);
					showToast("U-Boot started");
					myDev = checkUsbDevice(1317, 42149, "U-Boot did not started corectly!", manager, 10);
					if(myDev == null) {
						Log.d(TAG, "U-boot error");
						return ;
					}
					runFIT(myDev);
					
					showToast("Booting kernel!");
					myDev = checkUsbDevice(1317, 42151, "The kenernel did not started corectly!", manager, 30);
					if(myDev == null) {
						Log.d(TAG, "Kernel error");
						return ;
					}
					while (!manager.hasPermission(myDev)){}
					
					showToast("Starting to send image to the board!");
					SystemClock.sleep(1500);
					
					runWrite(downloadedFile, myDev);
				}
			}).start();
		}
	};
    
	OnClickListener downloadListener = new OnClickListener() {		
		public void onClick(View v) {
			new Thread(new Runnable() {
				public void run() {
					try {
						Spinner selection = (Spinner) findViewById(R.id.spinner1);
						downloadImage(selection.getSelectedItem().toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	};
	
	/**
	 * @return ArrayAdapter<String> Values for the spinner
	 */
	ArrayAdapter<String> setupSpinner() {
		List<String> list = new ArrayList<String>();
		list.add("BBB-Angstrom");
		list.add("Ubuntu Precise 12.04.2 LTS");
		list.add("Ubuntu Raring 13.04");
    	list.add("rPI-NOOBS_v1_2");
    	list.add("rPI-Raspbian 'wheezy'");
    	list.add("rPI-Soft-float Debian 'wheezy'");
    	list.add("rPI-Pidora");
    	list.add("rPI-Arch Linux ARM");
    	list.add("rPI-RISC OS");
    	list.add("Local File");
    	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
    		android.R.layout.simple_spinner_item, list);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	return dataAdapter;
	}
	
	/**
	 * @param device UsbDevice device which needs to be debugged
	 */
	public void debugDevice(UsbDevice device) {
        Log.i(TAG,"Model: " + device.getDeviceName());
        Log.i(TAG,"ID: " + device.getDeviceId());
        Log.i(TAG,"Class: " + device.getDeviceClass());
        Log.i(TAG,"Protocol: " + device.getDeviceProtocol());
        Log.i(TAG,"Vendor ID " + device.getVendorId());
        Log.i(TAG,"Product ID: " + device.getProductId());
        Log.i(TAG,"Interface count: " + device.getInterfaceCount());
        Log.i(TAG,"---------------------------------------");
   // Get interface details
        for(int index = 0; index < device.getInterfaceCount(); index++) {
        	UsbInterface mUsbInterface = device.getInterface(index);
        	Log.i(TAG,"  *****     *****");
        	Log.i(TAG,"  Interface index: " + index);
        	Log.i(TAG,"  Interface ID: " + mUsbInterface.getId());
        	Log.i(TAG,"  Inteface class: " + mUsbInterface.getInterfaceClass());
        	Log.i(TAG,"  Interface protocol: " + mUsbInterface.getInterfaceProtocol());
        	Log.i(TAG,"  Endpoint count: " + mUsbInterface.getEndpointCount());
    // Get endpoint details 
            for(int epi = 0; epi < mUsbInterface.getEndpointCount(); epi++) {
	            UsbEndpoint mEndpoint = mUsbInterface.getEndpoint(epi);
	            Log.i(TAG,"    ++++   ++++   ++++");
	            Log.i(TAG,"    Endpoint index: " + epi);
	            Log.i(TAG,"    Attributes: " + mEndpoint.getAttributes());
	            Log.i(TAG,"    Direction: " + mEndpoint.getDirection());
	            Log.i(TAG,"    Number: " + mEndpoint.getEndpointNumber());
	            Log.i(TAG,"    Interval: " + mEndpoint.getInterval());
	            Log.i(TAG,"    Packet size: " + mEndpoint.getMaxPacketSize());
	            Log.i(TAG,"    Type: " + mEndpoint.getType());
	            }
            }
        }

	}