/*
 * Copyright (c) 2010 Sony Ericsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sonyericsson.extras.liveview.plugins.imsamurai.liveviewshot;

import com.sonyericsson.extras.liveview.plugins.AbstractPluginService;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;
import com.sonyericsson.extras.liveview.plugins.sandbox.R;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextPaint;
import android.util.Log;

import java.util.List;

public class LiveViewShotService extends AbstractPluginService {
    
    
    // Our handler.
    private Handler mHandler = null;
    
    private android.hardware.Camera mCamera = null;
    
    private boolean screenAlwaysOn = false;
    private boolean flashEnabled = false;
    
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		// Create handler.
		if(mHandler == null) {
		    mHandler = new Handler();
		}
		
		// Rotation
//		mRotateBitmap = BitmapFactory.decodeStream(this.getResources().openRawResource(R.drawable.samurai));
		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopWork();
	}
	
    /**
     * Plugin is sandbox.
     */
    protected boolean isSandboxPlugin() {
        return true;
    }
	
	/**
	 * Must be implemented. Starts plugin work, if any.
	 */
	protected void startWork() {
	        mHandler.postDelayed(new Runnable() {
                public void run() {
                    sendInfo();
                }
            }, 1000);
	}
	
	/**
	 * Must be implemented. Stops plugin work, if any.
	 */
	protected void stopWork() {
	}
	
	/**
	 * Must be implemented.
	 * 
	 * PluginService has done connection and registering to the LiveView Service. 
	 * 
	 * If needed, do additional actions here, e.g. 
	 * starting any worker that is needed.
	 */
	protected void onServiceConnectedExtended(ComponentName className, IBinder service) {
		
	}
	
	/**
	 * Must be implemented.
	 * 
	 * PluginService has done disconnection from LiveView and service has been stopped. 
	 * 
	 * Do any additional actions here.
	 */
	protected void onServiceDisconnectedExtended(ComponentName className) {
		
	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has checked if plugin has been enabled/disabled.
	 * 
	 * The shared preferences has been changed. Take actions needed. 
	 */	
	protected void onSharedPreferenceChangedExtended(SharedPreferences prefs, String key) {
		
	}

	protected void startPlugin() {
		Log.d(PluginConstants.LOG_TAG, "startPlugin");
		startWork();
	}
			
	protected void stopPlugin() {
		Log.d(PluginConstants.LOG_TAG, "stopPlugin");
		stopWork();
	}
	
	protected void button(String buttonType, boolean doublepress, boolean longpress) {
		mLiveViewAdapter.vibrateControl(mPluginId, 50, 50);
	    Log.d(PluginConstants.LOG_TAG, "button - type " + buttonType + ", doublepress " + doublepress + ", longpress " + longpress);
		if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_UP)) {
			if (longpress) {
				sendAboutPicture();
			}
			else {
				sendAbout();
			}
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_DOWN)) {
            flashEnabled = !flashEnabled;
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_RIGHT)) {
			sendInfo();
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_LEFT)) {
			if (screenAlwaysOn) {
				mLiveViewAdapter.screenOnAuto(mPluginId);
			}
			else {
				mLiveViewAdapter.screenOn(mPluginId);
			}
			screenAlwaysOn = !screenAlwaysOn;
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_SELECT)) {
			sendShot();
		}
	}

	protected void displayCaps(int displayWidthPx, int displayHeigthPx) {
        Log.d(PluginConstants.LOG_TAG, "displayCaps - width " + displayWidthPx + ", height " + displayHeigthPx);
    }

	protected void onUnregistered() throws RemoteException {
		Log.d(PluginConstants.LOG_TAG, "onUnregistered");
		stopWork();
	}

	protected void openInPhone(String openInPhoneAction) {
		Log.d(PluginConstants.LOG_TAG, "openInPhone: " + openInPhoneAction);
	}
	
    protected void screenMode(int mode) {
        Log.d(PluginConstants.LOG_TAG, "screenMode: screen is now " + ((mode == 0) ? "OFF" : "ON"));
    }
    
    private void sendInfo() {
    	Bitmap bitmap = BitmapFactory.decodeStream(this.getResources().openRawResource(R.drawable.info));
    	PluginUtils.sendScaledImage(mLiveViewAdapter, mPluginId, bitmap);
    }
    
    private void sendAbout() {
    	Bitmap bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.RGB_565);
    	TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(15);
        textPaint.setColor(Color.WHITE);
    	PluginUtils.sendTextBitmap(mLiveViewAdapter, mPluginId, "\nThis is plugin\nfor take shot\nfrom camera\nwith LiveView.\nAuthor imsamurai", bitmap, textPaint);
    }
    
    private void sendAboutPicture() {
    	Bitmap bitmap = BitmapFactory.decodeStream(this.getResources().openRawResource(R.drawable.samurai));
    	PluginUtils.sendScaledImage(mLiveViewAdapter, mPluginId, bitmap);
    }
    
    private void sendShot() {
    	if (mCamera!=null) {
    		return;
    	}
    	
    	class JpegCallback implements PictureCallback {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
				
				
				Bitmap image_full = BitmapFactory.decodeByteArray(data, 0, data.length).copy(Bitmap.Config.RGB_565, false);
				
				Bitmap image = Bitmap.createScaledBitmap(image_full, 128, 128, true);
				
//				mLiveViewAdapter.clearDisplay(mPluginId);
				PluginUtils.sendScaledImage(mLiveViewAdapter, mPluginId, image);
				Log.d(PluginConstants.LOG_TAG, "shot sent");
				Log.d(PluginConstants.LOG_TAG, "image h:"+image_full.getHeight()+" w:"+image_full.getWidth());
				
				
				
			}
    		
    	}
    	
    	mCamera = safeCameraOpen(0);
    	Camera.Parameters Parameters = mCamera.getParameters();
    	List<Size> Sizes = Parameters.getSupportedPictureSizes();
    	Size Size = Sizes.get(Sizes.size()-1);
    	Log.d(PluginConstants.LOG_TAG, "size - h:"+Size.height+" w:"+Size.width);
    	Parameters.setPictureSize(Size.width, Size.height);
    	Log.d(PluginConstants.LOG_TAG, Sizes.toArray().toString());
    	if (flashEnabled) {
    		Parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
    	}
//    	Parameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
    	mCamera.setParameters(Parameters);
    	mCamera.startPreview();
		mCamera.takePicture(null, null, new JpegCallback() );
		

    }
    
    
    private android.hardware.Camera safeCameraOpen(int id) {
    	android.hardware.Camera mCamera = null;
      
        try {
        	mCamera = Camera.open(id);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return mCamera;    
    }
    

}