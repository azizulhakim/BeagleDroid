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

/**
 * Created by vvu on 6/22/13.
 */

public class TFTP {
	private short op_code;
	private short blk_numer;

	public TFTP(short op_code, short blk_numer) {
		this.op_code = op_code;
		this.blk_numer = blk_numer;
	}

	public short getOp_code() {
		return op_code;
	}

	public void setOp_code(short op_code) {
		this.op_code = op_code;
	}

	public short getBlk_numer() {
		return blk_numer;
	}

	public void setBlk_numer(short blk_numer) {
		this.blk_numer = blk_numer;
	}

	@Override
	public String toString() {
		return "TFTP{" +
				"op_code=" + op_code +
				", blk_numer=" + blk_numer +
				'}';
	}
}
