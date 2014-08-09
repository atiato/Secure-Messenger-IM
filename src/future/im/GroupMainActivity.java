package future.im;
 
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.crypto.spec.SecretKeySpec;

import future.im.communication.SocketOperator;
import future.im.interfaces.IAppManager;
import future.im.interfaces.ISocketOperator;
import future.im.services.IMService;
import future.im.tools.LocalStorageHandler;
import future.im.types.FriendInfo;
import future.im.types.GroupInfo;
import future.im.types.GroupMessageInfo;
import future.im.types.MessageInfo;
 
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import future.im.Messaging;
  
public  class GroupMainActivity extends Activity {
     
    private TextView messageText;
    private Button uploadButton, btnselectpic, sendButton;
    private ImageView imageview;
    private int serverResponseCode = 0;
    private ProgressDialog dialog = null;
    private IMService IMKeys = new IMService();
    
    private LocalStorageHandler localstoragehandler;     
    private String upLoadServerUri = null;
    private String imagepath=null;
    private String filenameext=null;
    private IAppManager imService;
    long msgid=0;
    
    
    
private ServiceConnection mConnection = new ServiceConnection() {
      
		
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((IMService.IMBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(GroupMainActivity.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
	
    
    static {
        System.loadLibrary("ecc");
    }
    
    public native String  EncryptKey(String ecckey);
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
         
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        upLoadServerUri = "http://0392710.NETSOLHOST.COM/upload.php";
        uploadButton = (Button)findViewById(R.id.uploadButton);
        sendButton = (Button)findViewById(R.id.SendButton);
        messageText  = (TextView)findViewById(R.id.messageText);
        btnselectpic = (Button)findViewById(R.id.button_selectpic);
        imageview = (ImageView)findViewById(R.id.imageView_pic);
         
        btnselectpic.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) { 	
        	Intent intent = new Intent();
             intent.setType("image/*");
             intent.setAction(Intent.ACTION_GET_CONTENT);
             startActivityForResult(Intent.createChooser(intent, "Complete action using"), 1);
        	}

});
        uploadButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) { 
        		
        	    dialog = ProgressDialog.show(GroupMainActivity.this, "", "Uploading file...", true);
                messageText.setText("uploading started.....");
                new Thread(new Runnable() {
                    public void run() {
                                         
                         uploadFile(imagepath);
                                                  
                    }
                  }).start();     
        		
        		
        	}
        });
        
      //  localstoragehandler = new LocalStorageHandler(this);
		final Bundle extras = this.getIntent().getExtras();
		
        sendButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) { 
        		final Handler handler = new Handler();
				final SecretKeySpec sks=IMKeys.Gen();
				
				
				
				
				
				final String groupname = extras.getString(GroupInfo.GROUPNAME);
				final String username =extras.getString(GroupMessageInfo.MYUSER);
				
			//	System.out.println("friend user name "+friend+"user name"+username+"message text"+messageText.getText().toString()+"IMService"+imService);
				
	//			msgid=	localstoragehandler.insert(username,friend , messageText.getText().toString(),"sent");

			
				
					Thread thread = new Thread(){					
						public void run() {
							try {
								
								
								if (imService.sendGroupMessage(username, groupname, IMKeys.Encrypt(sks,messageText.getText().toString()),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),"notseen") == null)
								{
									
									handler.post(new Runnable(){	

										public void run() {
											
									        Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

											
											//showDialog(MESSAGE_CANNOT_BE_SENT);										
										}
										
									});
								} 
							} 
							
							catch (UnsupportedEncodingException e) {
								Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

								e.printStackTrace();
							}
						}						
					};
					thread.start();
										
				
        	
    
        	}
        });
        
    }
      
     
   

       
             
         
        
         
    
    
    
     
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //Bitmap photo = (Bitmap) data.getData().getPath(); 
           
            Uri selectedImageUri = data.getData();
            imagepath = getPath(selectedImageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
         // will results in a much smaller image than the original
         options.inSampleSize = 8;
            
            Bitmap bitmap=BitmapFactory.decodeFile(imagepath, options);
            imageview.setImageBitmap(bitmap);
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
         
    public int uploadFile(String sourceFileUri) {
           
           
          final String fileName = sourceFileUri;
  
          HttpURLConnection conn = null;
          DataOutputStream dos = null;  
          String lineEnd = "\r\n";
          String twoHyphens = "--";
          String boundary = "*****";
          int bytesRead, bytesAvailable, bufferSize;
          byte[] buffer;
          int maxBufferSize = 1 * 1024 * 1024; 
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
                    
                   dos = new DataOutputStream(conn.getOutputStream());
          
                   dos.writeBytes(twoHyphens + boundary + lineEnd); 
                   dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                             + fileName + "\"" + lineEnd);
                    
                   dos.writeBytes(lineEnd);
          
                   // create a buffer of  maximum size
                   bytesAvailable = fileInputStream.available(); 
          
                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
                   buffer = new byte[bufferSize];
          
                   // read file and write it into form...
                   bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                      
                   while (bytesRead > 0) {
                        
                     dos.write(buffer, 0, bufferSize);
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
                                String msg = "Please Click to Download:\n"
                                      +"http://0392710.NETSOLHOST.COM/uploads/"+sourceFile.getName();
                                messageText.setText(msg);
                                Toast.makeText(GroupMainActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
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
                          Toast.makeText(GroupMainActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                      }
                  });
                   
                  Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  
              } catch (final Exception e) {
                   
                  dialog.dismiss();  
                  e.printStackTrace();
                   
                  runOnUiThread(new Runnable() {
                      public void run() {
                          messageText.setText("Got Exception : see logcat "+e.getMessage());
                          Toast.makeText(GroupMainActivity.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                      }
                  });
                  Log.e("Upload file to server Exception", "Exception : "  + e.getMessage(), e);  
              }
              dialog.dismiss();       
              return serverResponseCode; 
               
           } // End else block 
         }
    
	@Override
	protected void onResume() 
	{
			
		super.onResume();
		bindService(new Intent(GroupMainActivity.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);


		

		

	}
	
	@Override
	protected void onPause() 
	{
		
		unbindService(mConnection);
		super.onPause();
	}
 
     
}