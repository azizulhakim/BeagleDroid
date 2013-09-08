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
