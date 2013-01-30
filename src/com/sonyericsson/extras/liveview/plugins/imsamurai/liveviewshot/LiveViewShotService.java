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
import com.sonyericsson.extras.liveview.plugins.PluginUtils;
import com.sonyericsson.extras.liveview.plugins.sandbox.R;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class LiveViewShotService extends AbstractPluginService {
    
    private int MSG_TYPE_TIMER = 1;
    private int MSG_TYPE_ROTATOR = 2;
    
    // Our handler.
    private Handler mHandler = null;
    
    // Rotating
    private int mRotationDegrees = 2;
    private Bitmap mRotateBitmap = null;
    private int mDegrees = 0;
    private android.hardware.Camera mCamera = null;
    
    // Workers
//    private Timer mTimer = new Timer();
//    private Rotator mRotator = new Rotator();
    
    // Worker state
    private int mCurrentWorker = 0;
    
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		// Create handler.
		if(mHandler == null) {
		    mHandler = new Handler();
		}
		
		// Rotation
		mRotateBitmap = BitmapFactory.decodeStream(this.getResources().openRawResource(R.drawable.samurai));
		
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
//	    if(!workerRunning()) {
	        mHandler.postDelayed(new Runnable() {
                public void run() {
                    // First message to LiveView
                    try {
                        mLiveViewAdapter.clearDisplay(mPluginId);
                    } catch(Exception e) {
                        Log.e(PluginConstants.LOG_TAG, "Failed to clear display.");
                    }
                    StringBuilder text = new StringBuilder();
                    text.append("imsamurai2!");
                    PluginUtils.sendTextBitmap(mLiveViewAdapter, mPluginId, text.toString(), 128, 15);
                }
            }, 1000);
//        }
	}
	
	/**
	 * Must be implemented. Stops plugin work, if any.
	 */
	protected void stopWork() {
		stopUpdates();
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
		mLiveViewAdapter.screenOn(mPluginId);
	    Log.d(PluginConstants.LOG_TAG, "button - type " + buttonType + ", doublepress " + doublepress + ", longpress " + longpress);
	    mLiveViewAdapter.vibrateControl(mPluginId, 50, 50);
		if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_UP)) {
		    if(longpress) {
		        mLiveViewAdapter.ledControl(mPluginId, 50, 50, 50);
		    } else {
		    	
		    	
		    	
//		    	Log.d(PluginConstants.LOG_TAG, mCamera.toString());
		    	PluginUtils.rotateAndSend(mLiveViewAdapter, mPluginId, mRotateBitmap, 0);
//		        rotate(0);
		    }
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_DOWN)) {
            if(longpress) {
                mLiveViewAdapter.vibrateControl(mPluginId, 50, 50);
            } else {
            	
//                rotate(180);
            }
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_RIGHT)) {
//		    toggleRotate(true);
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_LEFT)) {
//		    toggleRotate(false);
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_SELECT)) {
//		    toggleTimer();
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
        
        if(mode == PluginConstants.LIVE_SCREEN_MODE_ON) {
            startUpdates();
        } else {
            stopUpdates();
        }
    }
    
    private void stopUpdates() {
    }
    
    private void startUpdates() {
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
				
				
				PluginUtils.sendScaledImage(mLiveViewAdapter, mPluginId, image);
				Log.d(PluginConstants.LOG_TAG, "shot sent");
				Log.d(PluginConstants.LOG_TAG, "image h:"+image_full.getHeight()+" w:"+image_full.getWidth());
				mLiveViewAdapter.vibrateControl(mPluginId, 50, 50);
				
				
			}
			
//			public void onPictureTaken(byte[] data, Camera camera) {
//		        FileOutputStream outStream = null;
//		        try {
//		            // write to local sandbox file system
//		            // outStream =
//		            // CameraDemo.this.openFileOutput(String.format("%d.jpg",
//		            // System.currentTimeMillis()), 0);
//		            // Or write to sdcard
//		            outStream = new FileOutputStream(String.format(
//		                    "/sdcard/%d.jpg", System.currentTimeMillis()));
//		            outStream.write(data);
//		            outStream.close();
//		            Log.d(PluginConstants.LOG_TAG, "onPictureTaken - wrote bytes: " + data.length);
//		        } catch (FileNotFoundException e) {
//		            e.printStackTrace();
//		        } catch (IOException e) {
//		            e.printStackTrace();
//		        } finally {
//		        }
//		        Log.d(PluginConstants.LOG_TAG, "onPictureTaken - jpeg");
//		    }
    		
    	}
    	
    	mCamera = safeCameraOpen(0);
    	Camera.Parameters Parameters = mCamera.getParameters();
    	List<Size> Sizes = Parameters.getSupportedPictureSizes();
    	
//    	Iterator<Size> Iterator = Sizes.iterator();
//    	while(Iterator.hasNext()) {
//    		Size Size = Iterator.next();
    	Size Size = Sizes.get(Sizes.size()-1);
    		Log.d(PluginConstants.LOG_TAG, "size - h:"+Size.height+" w:"+Size.width);
    		Parameters.setPictureSize(Size.width, Size.height);
//    	}
    	Log.d(PluginConstants.LOG_TAG, Sizes.toArray().toString());
//    	Log.d(PluginConstants.LOG_TAG, Sizes.toString());
//    	Parameters.setPictureSize(128, 128);
    	Parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
    	mCamera.setParameters(Parameters);
    	mCamera.startPreview();
		mCamera.takePicture(null, null, new JpegCallback() );
//		Log.d(PluginConstants.LOG_TAG, "release camera");
		

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