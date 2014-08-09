package future.im;



import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.alexbbb.uploadservice.AbstractUploadServiceReceiver;
import com.alexbbb.uploadservice.UploadRequest;
import com.alexbbb.uploadservice.UploadService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import future.im.UploadFile.HttpAsyncTask;
import future.im.DownloadFile.SaveImageAsync;
import future.im.communication.SocketOperator;
import future.im.interfaces.IAppManager;
import future.im.interfaces.ISocketOperator;
import future.im.services.IMService;
import future.im.tools.FriendController;
import future.im.tools.LocalStorageHandler;
import future.im.tools.UploadController;
import future.im.types.FriendInfo;
import future.im.types.MessageInfo;
 
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import future.im.Messaging;
  
public  class UploadFile extends Activity {
     
    private TextView messageText;
    private EditText text;
    private Button uploadButton, btnselectpic, sendButton;
  //  private ImageView imageview;
    private int serverResponseCode = 0;
    private ProgressDialog dialog = null;
    private IMService IMKeys = new IMService();
    
    private LocalStorageHandler localstoragehandler;     
    private String upLoadServerUri = null;
    private String imagepath=null;
    private String filenameext=null;
    private IAppManager imService;
    private Context context;
    long msgid=0;
    public static final int DIALOG_ALERT=1;
    private AdView adView;

	  /* Your ad unit id. Replace with your actual ad unit id. */
	  private static final String AD_UNIT_ID = "a1532dfd78cf267";
    
    public native byte []  EncryptBinfile(byte [] message);
    public native String  EncryptKey(String ecckey);
    
   
   /* static {
        System.loadLibrary("ecc");
    }*/
    
    
    
private ServiceConnection mConnection = new ServiceConnection() {
      
		
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((IMService.IMBinder)service).getService();
            Log.w("Debug Log for Service", "Service Connected");
        }
        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(UploadFile.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
            Log.w("Debug Log for Service", "Service disConnected");
        }
    };
	
    
  
    
   
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
         
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_file);
        upLoadServerUri = "http://0392710.NETSOLHOST.COM/upload.php";
        uploadButton = (Button)findViewById(R.id.uploadfileButton);
    //    sendButton = (Button)findViewById(R.id.SendfileButton);
        messageText  = (TextView)findViewById(R.id.messageText1);
        btnselectpic = (Button)findViewById(R.id.button_selectfile);
        text=(EditText)findViewById(R.id.putPasswordupload);
        
        adView = new AdView(this);
	    adView.setAdSize(AdSize.BANNER);
	    adView.setAdUnitId(AD_UNIT_ID);

	    // Add the AdView to the view hierarchy. The view will have no size
	    // until the ad is loaded.
	    LinearLayout layout = (LinearLayout) findViewById(R.id.uplinearLayout);
	    layout.addView(adView);

	    // Create an ad request. Check logcat output for the hashed device ID to
	    // get test ads on a physical device.
	    AdRequest adRequest = new AdRequest.Builder()
	      //  .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
	     //   .addTestDevice("JZAYIVGEUG45EA9L")
	        .build();

	    adView.loadAd(adRequest);
      //  imageview = (ImageView)findViewById(R.id.imageView_pic);
         context=this;
        btnselectpic.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) { 	
        	Intent intent = new Intent();
          //   intent.setType("image/*");
        	intent.setType("*/*");
        //    startActivityForResult(intent,PICKFILE_RESULT_CODE);
             intent.setAction(Intent.ACTION_GET_CONTENT);
         //    startActivityForResult(Intent.createChooser(intent, "Complete action using"), 1);
             startActivityForResult(
                     Intent.createChooser(intent, "Select a File to Upload"),
                     1);
        	}

});
        
        final Bundle extras = this.getIntent().getExtras();
        
        final String friend = extras.getString(FriendInfo.USERNAME);
		final String username =extras.getString(MessageInfo.MYUSER);
	//	UploadController test=new UploadController();
		
        
        uploadButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) { 
        		
        	//    dialog = ProgressDialog.show(UploadFile.this, "", "Uploading file...", true);
                messageText.setText("uploading started as Service in the background");
       
		//		new HttpAsyncTask(UploadFile.this,imagepath,text.getText().toString(),friend,username).execute();
                UploadController.setfriend(friend);
        		Log.e("File upload"," "+friend+"  "+imagepath);
        		UploadController.setimagepath(imagepath);
        		UploadController.setusername(username);
        		UploadController.setpassword(text.getText().toString());
          
              updateSomething(context);
        		
        	}
        });
        
		
		

        
    }
    
    
    
    
    
      
     
   

       
             
         
        
         
    
    
    
     
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //Bitmap photo = (Bitmap) data.getData().getPath(); 
           
           Uri selectedImageUri = data.getData();
         	try {
				imagepath = getPath(this,selectedImageUri);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//   imagepath = getPath(selectedImageUri);
       //     Bitmap bitmap=BitmapFactory.decodeFile(imagepath);
       //     imageview.setImageBitmap(bitmap);
            messageText.setText("Uploading file path:" +imagepath);
             
        }
    }
         public String getPath(Uri uri) {
                String[] projection = { MediaStore.Images.Media.DATA };
                Cursor cursor = managedQuery(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
         
         public static String getPath(Context context, Uri uri) throws URISyntaxException {
             if ("content".equalsIgnoreCase(uri.getScheme())) {
                 String[] projection = { "_data" };
                 Cursor cursor = null;

                 try {
                     cursor = context.getContentResolver().query(uri, projection, null, null, null);
                     int column_index = cursor
                     .getColumnIndexOrThrow("_data");
                     if (cursor.moveToFirst()) {
                         return cursor.getString(column_index);
                     }
                 } catch (Exception e) {
                     // Eat it
                 }
             }

             else if ("file".equalsIgnoreCase(uri.getScheme())) {
                 return uri.getPath();
             }

             return null;
         }
         
    public int uploadFile(String sourceFileUri,String passtext) {
           
           
          final String fileName = sourceFileUri;
  
          HttpURLConnection conn = null;
          DataOutputStream dos = null;  
          String lineEnd = "\r\n";
          String twoHyphens = "--";
          String boundary = "*****";
          int bytesRead, bytesAvailable, bufferSize;
          byte[] buffer = null;
          int maxBufferSize = 10 * 1024 * 1024; 
          byte[] bytes = null;
          final File sourceFile = new File(sourceFileUri); 
           
          if (!sourceFile.isFile()) {
               
               dialog.dismiss(); 
                
               Log.e("uploadFile", "Source File not exist :"+imagepath);
                
               runOnUiThread(new Runnable() {
                   public void run() {
                       messageText.setText("Source File not exist :"+ imagepath);
                   }
               }); 
                
               return 0;
            
          }
          else
          {
               try { 
                     // open a URL connection to the Servlet
                   FileInputStream fileInputStream = new FileInputStream(sourceFile);
                   URL url = new URL(upLoadServerUri);
                    
                   // Open a HTTP  connection to  the URL
                   conn = (HttpURLConnection) url.openConnection(); 
                   conn.setDoInput(true); // Allow Inputs
                   conn.setDoOutput(true); // Allow Outputs
                   conn.setUseCaches(false); // Don't use a Cached Copy
                   conn.setRequestMethod("POST");
                  conn.setRequestProperty("Connection", "Keep-Alive");
                   conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                   conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                   conn.setRequestProperty("uploaded_file", fileName); 
                   conn.setConnectTimeout(30000); 
                    
                   dos = new DataOutputStream(conn.getOutputStream());
          
                   dos.writeBytes(twoHyphens + boundary + lineEnd); 
                   dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                             + fileName + "\"" + lineEnd);
                    
                   dos.writeBytes(lineEnd);
          
          
                   bytesAvailable = fileInputStream.available(); 
                   
                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
                   buffer = new byte[bufferSize];
             
                   bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                   
                   byte[] key = null;
				try {
					key = generateKey(passtext);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                   
                   while (bytesRead > 0) {
                	   try {
						bytes = encodeFile(key, buffer);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	   dos.write(bytes, 0, bytes.length);
          //           dos.write(buffer, 0, bufferSize);
                     bytesAvailable = fileInputStream.available();
                     bufferSize = Math.min(bytesAvailable, maxBufferSize);
                     bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
                      
                    }
        
                
          
                   // send multipart form data necesssary after file data...
                   dos.writeBytes(lineEnd);
                   dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
          
                   // Responses from the server (code and message)
                   serverResponseCode = conn.getResponseCode();

                   String serverResponseMessage = conn.getResponseMessage();
                     
                   Log.i("uploadFile", "HTTP Response is : "
                           + serverResponseMessage + ": " + serverResponseCode);
                    
                   if(serverResponseCode == 200){
                        
                       runOnUiThread(new Runnable() {
                            public void run() {
                                String msg = "Please Click link to Download the File:\n"
                                      +"http://0392710.NETSOLHOST.COM/uploads/"+sourceFile.getName();
                                messageText.setText(msg);
                                Toast.makeText(UploadFile.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                            }
                        });                
                   }    
                    
                   //close the streams //
                   fileInputStream.close();
                   dos.flush();
                   dos.close();
                     
              } catch (MalformedURLException ex) {
                   
                  dialog.dismiss();  
                  ex.printStackTrace();
                   
                  runOnUiThread(new Runnable() {
                      public void run() {
                          messageText.setText("MalformedURLException Exception : check script url.");
                          Toast.makeText(UploadFile.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                      }
                  });
                   
                  Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  
              } catch (final IOException e) {
                   
                  dialog.dismiss();  
                  e.printStackTrace();
                   
                  runOnUiThread(new Runnable() {
                      public void run() {
                          messageText.setText("Got Exception : see logcat "+e.getMessage());
                          Toast.makeText(UploadFile.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                      }
                  });
                  Log.e("Upload file to server Exception", "Exception : "  + e.getMessage(), e);  
                return 0;
              }
              dialog.dismiss();       
              return serverResponseCode; 
               
           } // End else block 
         }
    
/*	@Override
	protected void onResume() 
	{
			
		super.onResume();
		bindService(new Intent(UploadFile.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
	
		
	

	}
*/	
	
/*	protected void onPause() 
	{
		super.onPause();
		unbindService(mConnection);
		
	}*/
	
	public static byte[] generateKey(String password) throws Exception
	{
	    byte[] keyStart = password.getBytes("UTF-8");

	    KeyGenerator kgen = KeyGenerator.getInstance("AES");
	    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
	    sr.setSeed(keyStart);
	    kgen.init(128, sr);
	    SecretKey skey = kgen.generateKey();
	    return skey.getEncoded();
	}

	    public static byte[] encodeFile(byte[] key, byte[] fileData) throws Exception
	    {

	        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
	        Cipher cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

	        byte[] encrypted = cipher.doFinal(fileData);

	        return encrypted;
	    }

	    public static byte[] decodeFile(byte[] key, byte[] fileData) throws Exception
	    {
	        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
	        Cipher cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

	        byte[] decrypted = cipher.doFinal(fileData);

	        return decrypted;
	    }
	    
	    public  class HttpAsyncTask extends AsyncTask<String, String, String> {
	    	 private Context mContext;
	    	 private ProgressDialog mProgressDialog;
	    	 private String httpurlss;
	    	 private String text;
	    	 private String friend1;
	    	 private String username1;
	    	 
	    	 public HttpAsyncTask(Context context,String httpurl,String text1,String friend,String username) {
	             mContext = context;
	             text=text1;
	             httpurlss=httpurl;
	             friend1=friend;
	             username1=username;
	            
	         }
	    	@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
			//	return DownloadImage(params[0]);
	    		
	    		  final String fileName = httpurlss;
	    		  
	              HttpURLConnection conn = null;
	              DataOutputStream dos = null;  
	              String lineEnd = "\r\n";
	              String twoHyphens = "--";
	              String boundary = "*****";
	              int bytesRead, bytesAvailable, bufferSize;
	              byte[] buffer = null;
	              int maxBufferSize = 10 * 1024 * 1024; 
	              byte[] bytes = null;
	              final File sourceFile = new File(httpurlss); 
	               
	              if (!sourceFile.isFile()) {
	                   
	                   dialog.dismiss(); 
	                    
	                   Log.e("uploadFile", "Source File not exist :"+imagepath);
	                    
	                   runOnUiThread(new Runnable() {
	                       public void run() {
	                           messageText.setText("Source File not exist :"+ imagepath);
	                       }
	                   }); 
	                    
	                   return ""+0;
	                
	              }
	              else
	              {
	                   try { 
	                         // open a URL connection to the Servlet
	                       FileInputStream fileInputStream = new FileInputStream(sourceFile);
	                       URL url = new URL(upLoadServerUri);
	                        
	                       // Open a HTTP  connection to  the URL
	                       conn = (HttpURLConnection) url.openConnection(); 
	                       conn.setDoInput(true); // Allow Inputs
	                       conn.setDoOutput(true); // Allow Outputs
	                       conn.setUseCaches(false); // Don't use a Cached Copy
	                       conn.setRequestMethod("POST");
	                      conn.setRequestProperty("Connection", "Keep-Alive");
	                       conn.setRequestProperty("ENCTYPE", "multipart/form-data");
	                       conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	                       conn.setRequestProperty("uploaded_file", fileName); 
	                       conn.setConnectTimeout(30000); 
	                        
	                       dos = new DataOutputStream(conn.getOutputStream());
	              
	                       dos.writeBytes(twoHyphens + boundary + lineEnd); 
	                       dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
	                                                 + fileName + "\"" + lineEnd);
	                        
	                       dos.writeBytes(lineEnd);
	              
	              
	                       bytesAvailable = fileInputStream.available(); 
	                       
	                       bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                       buffer = new byte[bufferSize];
	                 
	                       bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
	                       
	                       byte[] key = null;
						try {
							key = generateKey(text);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                       int total=0;
	                       while (bytesRead > 0) {
	                    	   try {
								bytes = encodeFile(key, buffer);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                    	   dos.write(bytes, 0, bytes.length);
	                    	   
	                    	   total += bytesRead;
	                         //  into.write(data, 0, count);
	                           // publishing the progress....
	                           // After this onProgressUpdate will be called
	                           publishProgress(""+(int)((total*100)/bytesAvailable));
	              //           dos.write(buffer, 0, bufferSize);
	                         bytesAvailable = fileInputStream.available();
	                         bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                         bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
	                          
	                        }
	            
	                    
	              
	                       // send multipart form data necesssary after file data...
	                       dos.writeBytes(lineEnd);
	                       dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	              
	                       // Responses from the server (code and message)
	                       serverResponseCode = conn.getResponseCode();

	                       String serverResponseMessage = conn.getResponseMessage();
	                         
	                       Log.i("uploadFile", "HTTP Response is : "
	                               + serverResponseMessage + ": " + serverResponseCode);
	                        
	                       if(serverResponseCode == 200){
	                            
	                           runOnUiThread(new Runnable() {
	                                public void run() {
	                                    String msg = "Please Click link to Download the File:\n"
	                                          +"http://0392710.NETSOLHOST.COM/uploads/"+sourceFile.getName();
	                                    messageText.setText(msg);
	                                    Toast.makeText(UploadFile.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
	                                }
	                            });                
	                       }    
	                        
	                       //close the streams //
	                       fileInputStream.close();
	                       dos.flush();
	                       dos.close();
	                         
	                  } catch (MalformedURLException ex) {
	                       
	                	  mProgressDialog.dismiss();
	                      ex.printStackTrace();
	                       
	                      runOnUiThread(new Runnable() {
	                          public void run() {
	                              messageText.setText("MalformedURLException Exception : check script url.");
	                              Toast.makeText(UploadFile.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
	                          }
	                      });
	                       
	                      Log.e("Upload file to server", "error: " + ex.getMessage(), ex); 
	                      return null;
	                      
	                  } catch (final IOException e) {
	                	  mProgressDialog.dismiss();
	    //                  dialog.dismiss();  
	                      e.printStackTrace();
	                       
	                     runOnUiThread(new Runnable() {
	                          public void run() {
	                              messageText.setText("Got Exception : see logcat "+e.getMessage());
	                              Toast.makeText(UploadFile.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
	                          }
	                      });
	                      Log.w("Upload file to server Exception", "Exception : "  + e.getMessage(), e);  
	                 
	                      return null;
	                  }
	            //      dialog.dismiss();       
	                  return ""+serverResponseCode; 
	                   
	               } // End else block 
	    		
	    	
			}
	    	
	    /*	  @Override
	    	    protected void onPreExecute() {
	    	        super.onPreExecute();
	    	        showDialog(progress_bar_type);
	    	    }*/
	    	
	    	 @Override
	         protected void onPreExecute() {
	             super.onPreExecute();
	              mProgressDialog = new ProgressDialog(mContext);
	             mProgressDialog.setMessage("Uploading");
	             mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	             mProgressDialog.setIndeterminate(true);
	             mProgressDialog.setCancelable(false);
	             mProgressDialog.show();
	         }
	    	 
		
	        // onPostExecute displays the results of the AsyncTask.
	        protected void onPostExecute(String data1) {
	       //	dismissDialog(progress_bar_type);
	            mProgressDialog.dismiss();
	            mProgressDialog = null;
	         
	       if (data1!=null) {    
	    	    Log.w("Message Text","messageText.getText().toString()"+messageText.getText().toString() +" "+friend1+" "+username1);

				new SendMessageAsyncTask(UploadFile.this,imagepath,text,friend1,username1).execute();

	           // img.setVisibility(View.VISIBLE);
	            Toast.makeText(getBaseContext(), "Uploaded!", Toast.LENGTH_LONG).show();
	       }
	       else 
	       {
	    	   Toast.makeText(getBaseContext(), "Please make sure you have connection!", Toast.LENGTH_LONG).show();

	       }	
				//   System.out.println("ya jama3et el khair"+bitmap);
	         //   img.setText("Received");
	       }
	        
	        protected void onProgressUpdate(String... progress) {
	            mProgressDialog.setIndeterminate(false);
	            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
	        }
	        
	        
			
	    }	
	    
	    
	    public  class SendMessageAsyncTask extends AsyncTask<String, String, String> {
	    	 private Context mContext;
	    	 private ProgressDialog mProgressDialog;
	    	 private String httpurlss;
	    	 private String text;
	    	 private String friend1;
	    	 private String username1;
	    	 
	    	 public SendMessageAsyncTask(Context context,String httpurl,String text1,String friend,String username) {
	             mContext = context;
	             text=text1;
	             httpurlss=httpurl;
	             friend1=friend;
	             username1=username;
	            
	         }
	    	@Override
			protected String doInBackground(String... params) {
	    		
	    	    final SecretKeySpec sks=IMKeys.Gen();
	    		
	    	    String messagetext1=messageText.getText().toString();
	    	    String test=IMKeys.Encrypt(sks,EncryptKey(messageText.getText().toString()))+""+EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT));
	    	    
	    	    Log.w("Test Result all ciphered",""+test+"messgae Text"+messagetext1 +" "+friend1+" "+username1);
	    	    
	    		String Result = null;
				try {
				//	Result = imService.sendMessage(username1, friend1, IMKeys.Encrypt(sks,EncryptKey(messageText.getText().toString())),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),""+msgid,"notseen");
					Result = imService.sendMessage(username1, friend1, IMKeys.Encrypt(sks,messageText.getText().toString()),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),""+msgid,"notseen");
					if (Result==null)
													
						  //      Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();
					{
						Toast.makeText(getBaseContext(), R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();
					}
								
								//showDialog(MESSAGE_CANNOT_BE_SENT);										
					
		//			messageText.setText("Message Sent"+Result);

				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

					e1.printStackTrace();
				}
				Log.w("Send Message","String atia "+Result);
	    		
				return Result;
				// TODO Auto-generated method stub
			//	return DownloadImage(params[0]);
	    		
	    				}
	    	
	    /*	  @Override
	    	    protected void onPreExecute() {
	    	        super.onPreExecute();
	    	        showDialog(progress_bar_type);
	    	    }*/
	    	
	    	 @Override
	         protected void onPreExecute() {
	             super.onPreExecute();
	              mProgressDialog = new ProgressDialog(mContext);
	             mProgressDialog.setMessage("Sending URL Message");
	             mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	             mProgressDialog.setIndeterminate(true);
	             mProgressDialog.setCancelable(false);
	          //   mProgressDialog.show();
	         }
	    	 
		
	        // onPostExecute displays the results of the AsyncTask.
	        protected void onPostExecute(String data1) {
	       //	dismissDialog(progress_bar_type);
	        //    mProgressDialog.dismiss();
	            mProgressDialog = null;
	        
	            
	           // img.setVisibility(View.VISIBLE);
	            Toast.makeText(getBaseContext(), "URL Sent Successfully"+data1, Toast.LENGTH_LONG).show();
	            
	        
				//   System.out.println("ya jama3et el khair"+bitmap);
	         //   img.setText("Received");
	       }
	        
	        protected void onProgressUpdate(String... progress) {
	            mProgressDialog.setIndeterminate(false);
	            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
	        }
	        
	        
			
	    }	
	    
	    
	    public void updateSomething(final Context context) {
	        final UploadRequest request = new UploadRequest(context, "http://0392710.NETSOLHOST.COM/upload.php");
	        final File sourceFile = new File(UploadController.getimagepath());
	        request.addFileToUpload(UploadController.getimagepath(), 
	                                "uploaded_file", 
	                                sourceFile.getName(),
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
	        request.addParameter("uploaded_file", sourceFile.getName());

	        request.setNotificationConfig(
	                android.R.drawable.ic_menu_upload, //Notification icon. You can use your own app's R.drawable.your_resource
	                "Uploading File "+sourceFile.getName() , //You can use your string resource with: context.getString(R.string.your_string)
	                "upload in progress",
	                "upload completed successfully",
	                "upload error",
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
	        if (adView != null) {
			      adView.resume();
			    }

	    }

	    
	    @Override
	    public void onDestroy() {
	      // Destroy the AdView.
	      if (adView != null) {
	        adView.destroy();
	      }
	      super.onDestroy();
	    }
	    
	    @Override
	    protected void onPause() {
	    	  if (adView != null) {
			      adView.pause();
			    }
	        super.onPause();
	        unregisterReceiver(uploadReceiver);
	    }

	    
	    
	    
	   

}