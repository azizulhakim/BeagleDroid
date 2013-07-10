package com.vvu.beagledroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.util.Log;

public class ByteMaker {
	private final static String TAG = "ByteMaker";
	
	ByteArrayOutputStream toSend = new ByteArrayOutputStream();
	
	public byte[] converter(final byte[]... arrays) {
		toSend.reset();
		try {
			for(int i=0; i<arrays.length; i++)
				toSend.write(arrays[i]);
		}
		catch(IOException e) {
			Log.d(TAG, "Something wrong: " + e.getMessage());
		}
		return toSend.toByteArray();
	}
	
	public byte[] stripSize(byte[] arr, int size) {
		return Arrays.copyOf(arr, size);
	}
}
