package com.vvu.beagledroid;

import java.nio.ByteBuffer;

public class IPv4 {
	byte ihl_version = (byte)0x45;
	byte tos;
	short tot_len;
	short id;
	short frag_off;
	byte ttl = 64;
	byte protocol;
	short check;
	byte[] saddr = new byte[4];
	byte[] daddr = new byte[4];
	
	
	/**
	 * @param tot_len
	 * @param id
	 * @param protocol
	 * @param saddr
	 * @param dadr
	 */
	public IPv4(short tot_len, short id, byte protocol, byte[] saddr,
			byte[] dadr) {
		super();
		this.tot_len = tot_len;
		this.id = id;
		this.protocol = protocol;
		this.saddr = saddr;
		this.daddr = dadr;
		this.check = calculateChecksum(getByteArray());
	}


	/**
	 * @return the tot_len
	 */
	public short getTot_len() {
		return tot_len;
	}


	/**
	 * @param tot_len the tot_len to set
	 */
	public void setTot_len(short tot_len) {
		this.tot_len = tot_len;
	}


	/**
	 * @return the id
	 */
	public short getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(short id) {
		this.id = id;
	}


	/**
	 * @return the protocol
	 */
	public byte getProtocol() {
		return protocol;
	}


	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(byte protocol) {
		this.protocol = protocol;
	}


	/**
	 * @return the saddr
	 */
	public byte[] getSaddr() {
		return saddr;
	}


	/**
	 * @param saddr the saddr to set
	 */
	public void setSaddr(byte[] saddr) {
		this.saddr = saddr;
	}


	/**
	 * @return the dadr
	 */
	public byte[] getDaddr() {
		return daddr;
	}


	/**
	 * @param dadr the dadr to set
	 */
	public void setDaddr(byte[] daddr) {
		this.daddr = daddr;
	}
	
	public byte[] getByteArray() {
		byte[] buffer = new byte[20];
		ByteBuffer result = ByteBuffer.wrap(buffer);
		
		result.put(this.ihl_version);
		result.put(this.tos);
		result.putShort(this.tot_len);
		result.putShort(this.id);
		result.putShort(this.frag_off);
		result.put(this.ttl);
		result.put(this.protocol);
		result.putShort(this.check);
		result.put(this.saddr);
		result.put(this.daddr);
		
		return result.array();
	}
	
	public short calculateChecksum(byte[] buf) {
		 int length = buf.length;
		    int i = 0;

		    long sum = 0;
		    long data;

		    // Handle all pairs
		    while (length > 1) {
		      // Corrected to include @Andy's edits and various comments on Stack Overflow
		      data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
		      sum += data;
		      // 1's complement carry bit correction in 16-bits (detecting sign extension)
		      if ((sum & 0xFFFF0000) > 0) {
		        sum = sum & 0xFFFF;
		        sum += 1;
		      }

		      i += 2;
		      length -= 2;
		    }

		    // Handle remaining byte in odd length buffers
		    if (length > 0) {
		      // Corrected to include @Andy's edits and various comments on Stack Overflow
		      sum += (buf[i] << 8 & 0xFF00);
		      // 1's complement carry bit correction in 16-bits (detecting sign extension)
		      if ((sum & 0xFFFF0000) > 0) {
		        sum = sum & 0xFFFF;
		        sum += 1;
		      }
		    }

		    // Final 1's complement value correction to 16-bits
		    sum = ~sum;
		    sum = sum & 0xFFFF;
		    return (short)sum;

		  }

}
