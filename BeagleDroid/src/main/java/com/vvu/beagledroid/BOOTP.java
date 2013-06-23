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
import java.util.Arrays;

/**
 * Created by vvu on 6/22/13.
 */
public class BOOTP {
	private byte opcode = 2;
	private byte hw = 1;
	private byte hw_length = 6;
	private byte hop_count = 64;
	private int xid;
	private short secs = 0;
	private short flag = 0;
	byte[] ciaddr = new byte[4];
	byte[] yiaddr = new byte[4];
	byte[] server_ip = new byte[4];
	byte[] bootp_gw_addr = new byte[4];
	byte[] hw_addr = new byte[16];
	byte[] server_name = new byte[64];
	byte[] boot_file = new byte[128];
	byte[] vendor = new byte[]{(byte) 99, (byte) 130, (byte) 83, (byte) 99,
			(byte) 1, (byte) 4, (byte) 255, (byte) 255, (byte) 255, (byte) 0,
			(byte) 3, (byte) 4, (byte) 192, (byte) 168, (byte) 1, (byte) 9, (byte) 0xFF};

	public BOOTP(byte opcode, byte hw, byte hw_length, byte hop_count, int xid, short secs, short flag, byte[] ciaddr, byte[] yiaddr, byte[] server_ip, byte[] bootp_gw_addr, byte[] hw_addr, byte[] server_name, byte[] boot_file, byte[] vendor) {
		this.opcode = opcode;
		this.hw = hw;
		this.hw_length = hw_length;
		this.hop_count = hop_count;
		this.xid = htonl(xid);
		this.secs = secs;
		this.flag = flag;
		this.ciaddr = ciaddr;
		this.yiaddr = yiaddr;
		this.server_ip = server_ip;
		this.bootp_gw_addr = bootp_gw_addr;
		this.hw_addr = hw_addr;
		this.server_name = server_name;
		this.boot_file = boot_file;
		this.vendor = vendor;
	}

	public BOOTP(short xid, byte[] yiaddr, byte[] server_ip, byte[] bootp_gw_addr, byte[] hw_addr, byte[] server_name, byte[] boot_file, byte[] vendor) {
		this.xid = htonl(xid);
		this.yiaddr = yiaddr;
		this.server_ip = server_ip;
		this.bootp_gw_addr = bootp_gw_addr;
		this.hw_addr = hw_addr;
		this.server_name = server_name;
		this.boot_file = boot_file;
		this.vendor = vendor;
	}

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = htonl(xid);
	}

	public byte[] getCiaddr() {
		return ciaddr;
	}

	public void setCiaddr(byte[] ciaddr) {
		this.ciaddr = ciaddr;
	}

	public byte[] getYiaddr() {
		return yiaddr;
	}

	public void setYiaddr(byte[] yiaddr) {
		this.yiaddr = yiaddr;
	}

	public byte[] getServer_ip() {
		return server_ip;
	}

	public void setServer_ip(byte[] server_ip) {
		this.server_ip = server_ip;
	}

	public byte[] getBootp_gw_addr() {
		return bootp_gw_addr;
	}

	public void setBootp_gw_addr(byte[] bootp_gw_addr) {
		this.bootp_gw_addr = bootp_gw_addr;
	}

	public byte[] getHw_addr() {
		return hw_addr;
	}

	public void setHw_addr(byte[] hw_addr) {
		this.hw_addr = hw_addr;
	}

	public byte[] getServer_name() {
		return server_name;
	}

	public void setServer_name(byte[] server_name) {
		this.server_name = server_name;
	}

	public byte[] getBoot_file() {
		return boot_file;
	}

	public void setBoot_file(byte[] boot_file) {
		this.boot_file = boot_file;
	}

	public byte[] getVendor() {
		return vendor;
	}

	public void setVendor(byte[] vendor) {
		this.vendor = vendor;
	}

	@Override
	public String toString() {
		return "BOOTP{" +
				"opcode=" + opcode +
				", hw=" + hw +
				", hw_length=" + hw_length +
				", hop_count=" + hop_count +
				", xid=" + xid +
				", secs=" + secs +
				", flag=" + flag +
				", ciaddr=" + Arrays.toString(ciaddr) +
				", yiaddr=" + Arrays.toString(yiaddr) +
				", server_ip=" + Arrays.toString(server_ip) +
				", bootp_gw_addr=" + Arrays.toString(bootp_gw_addr) +
				", hw_addr=" + Arrays.toString(hw_addr) +
				", server_name=" + Arrays.toString(server_name) +
				", boot_file=" + Arrays.toString(boot_file) +
				", vendor=" + Arrays.toString(vendor) +
				'}';
	}

	private int htonl(int xid) {
		return ByteBuffer.allocate(4).putInt(xid)
				.order(ByteOrder.nativeOrder()).getInt(0);
	}

	public byte[] getByteArray() {
		byte[] buffer = new byte[300];
		ByteBuffer result = ByteBuffer.wrap(buffer);

		result.put(opcode);
		result.put(hw);
		result.put(hw_length);
		result.put(hop_count);
		result.putInt(xid);
		result.putShort(secs);
		result.putShort(flag);
		result.put(ciaddr);
		result.put(yiaddr);
		result.put(server_ip);
		result.put(bootp_gw_addr);
		result.put(hw_addr);
		result.put(server_name);
		result.put(boot_file);
		result.put(vendor);

		return result.array();
	}
}
