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
 * Created by vvu on 6/22/13.
 */
public class UDP {
	private short src_port;
	private short dst_port;
	private short len;
	private short check_sum;

	public UDP(short src_port, short dst_port, short len) {
		this.src_port = src_port;
		this.dst_port = dst_port;
		this.len = (short) (8 + len);
	}

	public short getSrc_port() {
		return src_port;
	}

	public void setSrc_port(short src_port) {
		this.src_port = src_port;
	}

	public short getDst_port() {
		return dst_port;
	}

	public void setDst_port(short dst_port) {
		this.dst_port = dst_port;
	}

	public short getLen() {
		return len;
	}

	public void setLen(short len) {
		this.len = len;
	}

	@Override
	public String toString() {
		return "UDP{" +
				"src_port=" + src_port +
				", dst_port=" + dst_port +
				", len=" + len +
				", check_sum=" + check_sum +
				'}';
	}

	public byte[] getByteArray() {
		byte[] buffer = new byte[8];
		ByteBuffer result = ByteBuffer.wrap(buffer);

		result.putShort(src_port);
		result.putShort(dst_port);
		result.putShort(len);
		result.putShort(check_sum);

		return result.array();
	}
}
