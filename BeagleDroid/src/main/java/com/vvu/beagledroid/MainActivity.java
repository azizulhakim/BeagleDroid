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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	static final String TAG = "MainActivity";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		Button myMagic = (Button) findViewById(R.id.button);
		myMagic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				/*UsbConnection myConn = new UsbConnection(MainActivity.this, 0x0451, 0x6141);
				myConn.init();*/
				HexDump util = new HexDump();
				byte[] dst_mac = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,};
				byte[] src_mac = new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06,};
				short h_proto = 0x0800;
				Ether2 eth = new Ether2(dst_mac, src_mac, h_proto);
				Log.d(TAG, eth.toString());
				Log.d(TAG, util.dumpHexString(eth.getH_dest()));
				Log.d(TAG, util.dumpHexString(eth.getH_source()));
				Log.d(TAG, "" + eth.getH_proto());
			}
		});
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
