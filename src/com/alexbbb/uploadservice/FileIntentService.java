package com.alexbbb.uploadservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import future.im.R;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

public class FileIntentService extends IntentService {
	
	 private NotificationManager notificationManager;
	    private Builder notification;
	    String filename;

	private static final String SERVICE_NAME = FileIntentService.class
			.getName();
	File cacheDir = null;

	public FileIntentService() {
		super(SERVICE_NAME);
	}

	public void onCreate() {
		super.onCreate();
		  notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	        notification = new NotificationCompat.Builder(this);
	        
		String tmpLocation = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + "Future_IM";
		cacheDir = new File(tmpLocation);
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
	}

	protected void onHandleIntent(Intent intent) {
		String remoteUrl = intent.getExtras().getString("url");
		String pass = intent.getExtras().getString("pass");
		Log.e("test","password"+pass);
		 

		String location;
		filename = remoteUrl.substring(remoteUrl
				.lastIndexOf(File.separator) + 1);
		
		Log.e("fielname","file name "+filename);
		File tmp = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + "Future_IM" + File.separator + filename);
	/*	if (tmp.exists()) {
			location = tmp.getAbsolutePath();
			notifyFinished(location, remoteUrl);
			Log.e("fielname","file name exsists"+filename);
			stopSelf();
			return;
		}*/
		
		createNotification();		
		try {
			
			int bufferedBytes = 0;
			int count=0;
			ByteArrayOutputStream into = new ByteArrayOutputStream();
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			URLConnection conn = null;
			URL url = new URL(remoteUrl);
			try {
				conn = url.openConnection();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] buffer = new byte[1024];
			Log.e("Testing", "Failed!"+remoteUrl);
			
			HttpURLConnection httpCon = (HttpURLConnection) conn;
			httpCon.setAllowUserInteraction(false);
			httpCon.setInstanceFollowRedirects(true);
			httpCon.setRequestMethod("GET");
			httpCon.connect();
			int totalBytes=conn.getContentLength();
			if (httpCon.getResponseCode() != 200)
				throw new Exception("Failed to connect");
			InputStream is = httpCon.getInputStream();
			OutputStream outputStream = new FileOutputStream(tmp);
			byte[] data1=null;
			int uploadedBytes=0;
			while ((count = is.read(buffer)) > 0) {
				
				into.write(buffer, 0, count);
				uploadedBytes += count;
				broadcastProgress(uploadedBytes, totalBytes);
			//	fos.write(data1, 0, data1.length);
			}
			into.close();
			
			updateNotificationCompleted( Environment.getExternalStorageDirectory()
					.getAbsolutePath() + File.separator + "Future_IM" + File.separator + filename,  remoteUrl);
			
			byte[] key = null;
			if(pass==null)
				pass="";
			try {
				key = generateKey(pass);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				data1 = decodeFile(key, into.toByteArray());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			ByteArrayInputStream inputStream = new ByteArrayInputStream(
					data1);
			
			byteOutputStream.writeTo(outputStream);
			
			bufferedBytes=0;
			
			while ((bufferedBytes = inputStream.read(buffer)) > 0) {
				
				outputStream.write(buffer, 0, bufferedBytes);
			}
			
			
			outputStream.flush();
			outputStream.close();
			byteOutputStream.close();
			
			is.close();
			location = tmp.getAbsolutePath();
			notifyFinished(location, remoteUrl);
		} catch (Exception e) {
			updateNotificationError();
			Log.e("Service", "Failed!", e);
		}
	}

	
	 private void broadcastProgress(final long downloadedBytes, final long totalBytes) {

	        final int progress = (int) (downloadedBytes * 100 / totalBytes);
	     

	        updateNotificationProgress(progress);

	    
	    }
	public static final String TRANSACTION_DONE = "com.alexbbb.uploadservice.TRANSACTION_DONE";

	private void notifyFinished(String location, String remoteUrl) {
		Intent i = new Intent(TRANSACTION_DONE);
		i.putExtra("location", location);
		i.putExtra("url", remoteUrl);
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
				Uri.parse("file://"
						+ Environment.getExternalStorageDirectory())));
		FileIntentService.this.sendBroadcast(i);		
	}
	
	public static byte[] generateKey(String password) throws Exception {
		byte[] keyStart = password.getBytes("UTF-8");

		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
		sr.setSeed(keyStart);
		kgen.init(128, sr);
		SecretKey skey = kgen.generateKey();
		return skey.getEncoded();
	}

	public static byte[] encodeFile(byte[] key, byte[] fileData)
			throws Exception {

		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

		byte[] encrypted = cipher.doFinal(fileData);

		return encrypted;
	}

	public static byte[] decodeFile(byte[] key, byte[] fileData)
			throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);

		byte[] decrypted = cipher.doFinal(fileData);

		return decrypted;
	}
	
	   private void createNotification() {
	        notification.setContentTitle(""+filename)
	                    .setContentText("Downloading..."+filename)
	                    .setSmallIcon(R.drawable.ic_menu_download)
	                    .setProgress(100, 0, true);
	        notificationManager.notify(0, notification.build());
	    }

	    private void updateNotificationProgress(final int progress) {
	    	notification.setContentTitle(""+filename)
            .setContentText("Downloading "+filename).setSmallIcon(R.drawable.ic_menu_download)
	                    .setProgress(100, progress, false);
	        notificationManager.notify(0, notification.build());
	    }

	    private void updateNotificationCompleted(String location, String remoteUrl) {
	  
	    		notification.setContentTitle(""+filename)
            .setContentText("Downloaded in "+Environment.getExternalStorageDirectory()
    				.getAbsolutePath() + File.separator + "Future_IM").setSmallIcon(R.drawable.ic_menu_download)
	            		//	.setContentText("Result ="+Result)
	                       
	                        .setProgress(0, 0, false);
	            notificationManager.notify(0, notification.build());
	            
	        	Intent i = new Intent(TRANSACTION_DONE);
	    		i.putExtra("location", location);
	    		i.putExtra("url", remoteUrl);
	    		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
	    				Uri.parse("file://"
	    						+ Environment.getExternalStorageDirectory())));
	    		FileIntentService.this.sendBroadcast(i);	
	            
	            
	        }
	    

	    private void updateNotificationError() {
	        notification.setContentTitle("Downloading "+filename)
	        		.setContentText("Downloading File Error").setSmallIcon(R.drawable.ic_menu_download)
	                   
	                    .setProgress(0, 0, false);
	        notificationManager.notify(0, notification.build());
	    }
	
}
