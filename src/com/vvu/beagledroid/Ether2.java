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
import java.util.Arrays;

/**
 * Created by vvu on 6/21/13.
 */
public class Ether2 {
	private byte[] h_dest = new byte[6];
	private byte[] h_source = new byte[6];
	private short h_proto;

	public Ether2(byte[] h_dest, byte[] h_source, short h_proto) {
		this.h_dest = h_dest;
		this.h_source = h_source;
		this.h_proto = h_proto;
	}

	public byte[] getH_dest() {
		return h_dest;
	}

	public void setH_dest(byte[] h_dest) {
		this.h_dest = h_dest;
	}

	public byte[] getH_source() {
		return h_source;
	}

	public void setH_source(byte[] h_source) {
		this.h_source = h_source;
	}

	public short getH_proto() {
		return h_proto;
	}

	public void setH_proto(short h_proto) {
		this.h_proto = h_proto;
	}

	@Override
	public String toString() {
		return "Ether2{" +
				"h_dest=" + Arrays.toString(h_dest) +
				", h_source=" + Arrays.toString(h_source) +
				", h_proto=" + h_proto +
				'}';
	}

	public byte[] getByteArray() {
		byte[] buffer = new byte[6 + 6 + 2];
		ByteBuffer target = ByteBuffer.wrap(buffer);

		target.put(h_dest);
		target.put(h_source);
		target.putShort(h_proto);

		return target.array();
	}
}
