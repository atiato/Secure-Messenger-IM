package com.alexbbb.uploadservice;

import java.io.File;

import javax.crypto.spec.SecretKeySpec;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;


	public class MainActivity extends Activity {

		
	/*    public void onCreate(Bundle savedInstanceState)
	    {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        
	        String seed="This is just a test";
	        
	
	        
	    
	        
	        TextView  tv = (TextView)findViewById(R.id.test1);
	   
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();

	       	        
	        TextView  out = (TextView)findViewById(R.id.test2);
	   
	
	     
	        out.setText(baseDir+File.separator+"atiato.prop");
	        
	        updateSomething(this);
            
	        tv.setText("Check notifications");

	        
	    }*/
	    
	    public void updateSomething(final Context context) {
	        final UploadRequest request = new UploadRequest(context, "http://0392710.NETSOLHOST.COM/upload.php");

	        request.addFileToUpload(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"mySavedImage.jpg", 
	                                "uploaded_file", 
	                                "mySavedImage.jpg",
	                                "multipart/form-data;boundary=*****"); //You can find many common content types defined as static constants in the ContentType class
/*         conn.setRequestProperty("Connection", "Keep-Alive");
	                       conn.setRequestProperty("ENCTYPE", "multipart/form-data");
	                       conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	                       conn.setRequestProperty("uploaded_file", fileName); */
	        //You can add your own custom headers
	        request.addHeader("your-custom-header", "your-custom-value");

	        request.addParameter("Connection", "Keep-Alive");

	        //If you want to add an array of strings, you can simply to the following:
	        request.addParameter("ENCTYPE", "multipart/form-data");
	        request.addParameter("Content-Type", "multipart/form-data;boundary=*****");
	        request.addParameter("uploaded_file", "atiato.prop");

	        request.setNotificationConfig(
	                android.R.drawable.ic_menu_upload, //Notification icon. You can use your own app's R.drawable.your_resource
	                "notification title", //You can use your string resource with: context.getString(R.string.your_string)
	                "upload in progress text",
	                "upload completed successfully text",
	                "upload error text",
	                false); //Set this to true if you want the notification to be automatically cleared when upload is successful

	        try {
	            //Utility method that creates the intent and starts the upload service in the background
	            //As soon as the service starts, you'll see upload status in Android Notification Center :)
	            UploadService.startUpload(request);
	        } catch (Exception exc) {
	            //You will end up here only if you pass an incomplete UploadRequest
	            Log.e("AndroidUploadService", exc.getLocalizedMessage(), exc);
	        }
	    }


	    private final BroadcastReceiver uploadReceiver = new AbstractUploadServiceReceiver() {

	        @Override
	        public void onProgress(int progress) {
	            Log.e("AndroidUploadService", "The progress is: " + progress);
	        }

	        @Override
	        public void onError(Exception exception) {
	            Log.e("AndroidUploadService", exception.getLocalizedMessage(), exception);
	        }

	        @Override
	        public void onCompleted(int serverResponseCode, String serverResponseMessage) {
	            Log.e("AndroidUploadService", "Upload completed: " + serverResponseCode + ", " + serverResponseMessage);
	        }
	    };

	    @Override
	    protected void onResume() {
	        super.onResume();
	        final IntentFilter intentFilter = new IntentFilter();
	        intentFilter.addAction(UploadService.BROADCAST_ACTION);
	        registerReceiver(uploadReceiver, intentFilter);
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        unregisterReceiver(uploadReceiver);
	    }

	
}
