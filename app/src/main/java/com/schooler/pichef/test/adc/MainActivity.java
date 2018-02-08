/*
	Copyright 2018 Jonathan O. Schooler

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */

package com.schooler.pichef.test.adc;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends Activity {

    private Handler mHandler;
	private SpiAdcDevice mAdc;
	private long mStartTimeMillis;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHandler = new Handler(Looper.getMainLooper());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mHandler = null;
	}

	@Override
	protected void onStart() {
		super.onStart();

		mStartTimeMillis = System.currentTimeMillis();

		try {
			mAdc = SpiAdcDevice.acquireOpenSharedDevice();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mHandler.post(mSampleRunnable);
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (mAdc != null) {
			try {
				SpiAdcDevice.releaseSharedDevice();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mAdc = null;
		}

		mHandler.removeCallbacksAndMessages(null);
	}

	private void getSample() throws IOException {
		if (mAdc == null) {
			return;
		}

		final double adcValue = mAdc.getSampledValue(0, 10);

		final long time = System.currentTimeMillis() - mStartTimeMillis;
		Log.d("ADC", time + "\t" + adcValue);
	}

	private Runnable mSampleRunnable = new Runnable() {
		@Override
		public void run() {
			mHandler.removeCallbacks(this);

			try {
				getSample();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mHandler.post(this);
		}
	};
}
