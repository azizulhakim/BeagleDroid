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
import java.nio.ByteOrder;

/**
 * Created by vvu on 6/22/13.
 */
public class ARP {
	private short hw_type = htons((short) 1);
	private short proto_type = htons((short) 0x0800);
	private byte hw_len = 6;
	private byte proto_len = 4;
	private short op_code;
	private byte[] hw_source = new byte[6];
	private byte[] ip_source = new byte[4];
	private byte[] hw_dest = new byte[6];
	private byte[] ip_dest = new byte[4];

	public ARP(short op_code, byte[] hw_source, byte[] ip_source, byte[] hw_dest, byte[] ip_dest) {
		this.op_code = htons(op_code);
		this.hw_source = hw_source;
		this.ip_source = ip_source;
		this.hw_dest = hw_dest;
		this.ip_dest = ip_dest;
	}

	public short getOp_code() {
		return op_code;
	}

	public void setOp_code(short op_code) {
		this.op_code = htons(op_code);
	}

	public byte[] getHw_source() {
		return hw_source;
	}

	public void setHw_source(byte[] hw_source) {
		this.hw_source = hw_source;
	}

	public byte[] getIp_source() {
		return ip_source;
	}

	public void setIp_source(byte[] ip_source) {
		this.ip_source = ip_source;
	}

	public byte[] getHw_dest() {
		return hw_dest;
	}

	public void setHw_dest(byte[] hw_dest) {
		this.hw_dest = hw_dest;
	}

	public byte[] getIp_dest() {
		return ip_dest;
	}

	public void setIp_dest(byte[] ip_dest) {
		this.ip_dest = ip_dest;
	}

	private short htons(short value) {
		return ByteBuffer.allocate(2).putShort(value)
				.order(ByteOrder.nativeOrder()).getShort(0);
	}
}
