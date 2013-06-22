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

import java.nio.ByteBuffer;

/**
 * Created by vvu on 6/21/13.
 */

public class RNDIS {
	private int msg_type;
	private int msg_len;
	private int data_offset;
	private int data_len;
	private int band_offset;
	private int band_len;
	private int out_band_elements;
	private int packet_offset;
	private int packet_info_len;
	private int reserved_first;
	private int reserved_second;

	RNDIS(int data_len) {
		this.data_len = data_len;
		this.msg_len = data_len + 44;
		this.msg_type = 1;
		this.data_offset = 36;
		this.band_len = 0;
		this.band_offset = 0;
		this.out_band_elements = 0;
		this.packet_offset = 0;
		this.packet_info_len = 0;
		this.reserved_first = 0;
		this.reserved_second = 0;
	}

	void updateRNDIS(int data_len) {
		this.data_len = data_len;
		this.msg_len = data_len + 44;
	}

	public int getData_len() {
		return data_len;
	}

	public void setData_len(int data_len) {
		this.data_len = data_len;
	}

	@Override
	public String toString() {
		return "RNDIS{" +
				"msg_type=" + msg_type +
				", msg_len=" + msg_len +
				", data_offset=" + data_offset +
				", data_len=" + data_len +
				'}';
	}

	public byte[] getByteArray() {
		byte[] buffer = new byte[44];
		ByteBuffer result = ByteBuffer.wrap(buffer);

		result.putInt(msg_type);
		result.putInt(msg_len);
		result.putInt(data_offset);
		result.putInt(data_len);
		result.putInt(band_offset);
		result.putInt(band_len);
		result.putInt(out_band_elements);
		result.putInt(packet_offset);
		result.putInt(packet_info_len);
		result.putInt(reserved_first);
		result.putInt(reserved_second);

		return result.array();
	}
}
