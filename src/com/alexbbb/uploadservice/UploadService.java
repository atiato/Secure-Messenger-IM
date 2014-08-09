package com.alexbbb.uploadservice;

import java.io.File;
import future.im.tools.LocalStorageHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import future.im.R;
import future.im.UploadFile;
import future.im.interfaces.IAppManager;
import future.im.services.IMService;
import future.im.tools.LocalStorageHandler;
import future.im.tools.UploadController;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

/**
 * Service to upload files as a multi-part form data in background using HTTP POST
 * with notification center progress display.
 *
 * @author alexbbb (Alex Gotev)
 *
 */
public class UploadService extends IntentService {

    private static final String SERVICE_NAME = UploadService.class.getName();

    private static final int BUFFER_SIZE = 4096;
    private static final String NEW_LINE = "\r\n";
    private static final String TWO_HYPHENS = "--";

    protected static final String ACTION_UPLOAD = "com.alexbbb.uploadservice.action.upload";
    protected static final String PARAM_NOTIFICATION_CONFIG = "notificationConfig";
    protected static final String PARAM_URL = "url";
    protected static final String PARAM_FILES = "files";
    protected static final String PARAM_REQUEST_HEADERS = "requestHeaders";
    protected static final String PARAM_REQUEST_PARAMETERS = "requestParameters";

    public static final String BROADCAST_ACTION = "com.alexbbb.uploadservice.broadcast.status";
    public static final String STATUS = "status";
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_ERROR = 3;
    public static final String PROGRESS = "progress";
    public static final String ERROR_EXCEPTION = "errorException";
    public static final String SERVER_RESPONSE_CODE = "serverResponseCode";
    public static final String SERVER_RESPONSE_MESSAGE = "serverResponseMessage";

    private NotificationManager notificationManager;
    private Builder notification;
    private UploadNotificationConfig notificationConfig;
    private int lastPublishedProgress;
    private IAppManager imService;
    private IMService IMKeys = new IMService();
    private LocalStorageHandler localstoragehandler; 
    byte[] bytes = null;
    int maxBufferSize = 10 * 1024 * 1024; 

    
    public native String  EncryptKey(String ecckey);
    /**
     * Utility method that creates the intent that starts the background
     * file upload service.
     *
     * @param task object containing the upload request
     * @throws IllegalArgumentException if one or more arguments passed are invalid
     * @throws MalformedURLException if the server URL is not valid
     */
    
private ServiceConnection mConnection = new ServiceConnection() {
      
		
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((IMService.IMBinder)service).getService();
            Log.w("Debug Log for Service", "Service Connected");
        }
        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(UploadService.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
            Log.w("Debug Log for Service", "Service disConnected");
        }
    };
    public static void startUpload(final UploadRequest task)
            throws IllegalArgumentException,
            MalformedURLException {
        if (task == null) {
            throw new IllegalArgumentException("Can't pass an empty task!");
        } else {
            task.validate();

            final Intent intent = new Intent(UploadService.class.getName());

            intent.setAction(ACTION_UPLOAD);
            intent.putExtra(PARAM_NOTIFICATION_CONFIG, task.getNotificationConfig());
            intent.putExtra(PARAM_URL, task.getServerUrl());
            intent.putParcelableArrayListExtra(PARAM_FILES, task.getFilesToUpload());
            intent.putParcelableArrayListExtra(PARAM_REQUEST_HEADERS, task.getHeaders());
            intent.putParcelableArrayListExtra(PARAM_REQUEST_PARAMETERS, task.getParameters());

            task.getContext().startService(intent);
        }
    }

    public UploadService() {
        super(SERVICE_NAME);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(this);
		bindService(new Intent(UploadService.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);

    }
    
    public void onDestroy() 
	{
		super.onDestroy();
		unbindService(mConnection);
		
	}

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_UPLOAD.equals(action)) {
                notificationConfig = intent.getParcelableExtra(PARAM_NOTIFICATION_CONFIG);
                final String url = intent.getStringExtra(PARAM_URL);
                final ArrayList<FileToUpload> files = intent.getParcelableArrayListExtra(PARAM_FILES);
                final ArrayList<NameValue> headers = intent.getParcelableArrayListExtra(PARAM_REQUEST_HEADERS);
                final ArrayList<NameValue> parameters = intent.getParcelableArrayListExtra(PARAM_REQUEST_PARAMETERS);

                lastPublishedProgress = 0;
                try {
                    createNotification();
                    handleFileUpload(url, files, headers, parameters);
                } catch (Exception exception) {
                    broadcastError(exception);
                }
            }
        }
    }

    private void handleFileUpload(final String url,
                                  final ArrayList<FileToUpload> filesToUpload,
                                  final ArrayList<NameValue> requestHeaders,
                                  final ArrayList<NameValue> requestParameters)
            throws IOException {

        final String boundary = getBoundary();
        final byte[] boundaryBytes = getBoundaryBytes(boundary);

        HttpURLConnection conn = null;
        OutputStream requestStream = null;

        try {
            conn = getMultipartHttpURLConnection(url, boundary);

            setRequestHeaders(conn, requestHeaders);

            requestStream = conn.getOutputStream();
            setRequestParameters(requestStream, requestParameters, boundaryBytes);

            uploadFiles(requestStream, filesToUpload, boundaryBytes);

            final byte[] trailer = getTrailerBytes(boundary);
            requestStream.write(trailer, 0, trailer.length);

            final int serverResponseCode = conn.getResponseCode();
            final String serverResponseMessage = conn.getResponseMessage();
            
            final SecretKeySpec sks=IMKeys.Gen();
    	    
    	    File sourceuri=new File(UploadController.getimagepath());
    	    
    	    Log.e("fuck","image path"+UploadController.getimagepath());
    	    
    	    String msg = "Please Click link to Download the File:\n"
                    +"http://0392710.NETSOLHOST.COM/uploads/"+sourceuri.getName();
    	    
    	    String Result=null;
    		localstoragehandler = new LocalStorageHandler(this);

            
            try {
            	
            	if (imService==null)
            		 Log.e("Fuck IM Service","it is NULLLLLLL"+imService);

    			//	Result = imService.sendMessage(username1, friend1, IMKeys.Encrypt(sks,EncryptKey(messageText.getText().toString())),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),""+msgid,"notseen");
    			
    		if (UploadController.getfriend() != null)
    		{
        		long	msgid=	localstoragehandler.insert(UploadController.getusername(), UploadController.getfriend(), "File Sent :\n"+msg,"sent","yes");

    		
    		Result = imService.sendMessage(UploadController.getusername(), UploadController.getfriend(), IMKeys.Encrypt(sks,msg),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),""+msgid,"notseen");
    		}
    		
    		else 
    		
    		{
			    Log.e("In group message","Result of IMService"+Result);
				localstoragehandler.groupinsert(UploadController.getusername(), UploadController.getgroupname(),"File Sent :\n"+ msg,"sent",UploadController.getusername());

    			Result =imService.sendGroupMessage(UploadController.getusername(), UploadController.getgroupname(), IMKeys.Encrypt(sks,msg),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),"notseen");
    		}					
    			    Log.e("Fuck IM Service","Result of IMService"+Result);

    			    
    							//showDialog(MESSAGE_CANNOT_BE_SENT);										
    				
    	//			messageText.setText("Message Sent"+Result);

    			} catch (Exception e1) {
    				// TODO Auto-generated catch block
    				
    				e1.printStackTrace();
    			}
    	
            

            broadcastCompleted(serverResponseCode, serverResponseMessage);

        } finally {
            closeOutputStream(requestStream);
            closeConnection(conn);
            localstoragehandler.close();
        }
    }

    private String getBoundary() {
        final StringBuilder builder = new StringBuilder();

        builder.append("---------------------------")
               .append(System.currentTimeMillis());

        return builder.toString();
    }

    private byte[] getBoundaryBytes(final String boundary)
            throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();

        builder.append(NEW_LINE)
               .append(TWO_HYPHENS)
               .append(boundary)
               .append(NEW_LINE);

        return builder.toString().getBytes("US-ASCII");
    }

    private byte[] getTrailerBytes(final String boundary)
            throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();

        builder.append(NEW_LINE)
               .append(TWO_HYPHENS)
               .append(boundary)
               .append(TWO_HYPHENS)
               .append(NEW_LINE);

        return builder.toString().getBytes("US-ASCII");
    }

    private HttpURLConnection getMultipartHttpURLConnection(final String url,
                                                            final String boundary)
            throws IOException {
        final HttpURLConnection conn;

        if (url.startsWith("https")) {
            AllCertificatesTruster.trustAllSSLCertificates();
            final HttpsURLConnection https = (HttpsURLConnection) new URL(url).openConnection();
            https.setHostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }

            });
            conn = https;

        } else {
            conn = (HttpURLConnection) new URL(url).openConnection();
        }

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        return conn;
    }

    private void setRequestHeaders(final HttpURLConnection conn,
                                   final ArrayList<NameValue> requestHeaders) {
        if (!requestHeaders.isEmpty()) {
            for (final NameValue param : requestHeaders) {
                conn.setRequestProperty(param.getName(), param.getValue());
            }
        }
    }

    private void setRequestParameters(final OutputStream requestStream,
                                      final ArrayList<NameValue> requestParameters,
                                      final byte[] boundaryBytes)
            throws IOException, UnsupportedEncodingException {
        if (!requestParameters.isEmpty()) {

            for (final NameValue parameter : requestParameters) {
                requestStream.write(boundaryBytes, 0, boundaryBytes.length);
                byte[] formItemBytes = parameter.getBytes();
                requestStream.write(formItemBytes, 0, formItemBytes.length);
            }
        }
        requestStream.write(boundaryBytes, 0, boundaryBytes.length);
    }

    private void uploadFiles(OutputStream requestStream,
                             final ArrayList<FileToUpload> filesToUpload,
                             final byte[] boundaryBytes)
            throws UnsupportedEncodingException,
                   IOException,
                   FileNotFoundException {
    	int bytesAvailable, bufferSize;

        final long totalBytes = getTotalBytes(filesToUpload);
        long uploadedBytes = 0;

        for (FileToUpload file : filesToUpload) {
            byte[] headerBytes = file.getMultipartHeader();
            requestStream.write(headerBytes, 0, headerBytes.length);

            final InputStream stream = file.getStream();
            
            File sourceuri=new File(UploadController.getimagepath());
            FileInputStream fileInputStream = new FileInputStream(sourceuri);

            bytesAvailable = fileInputStream.available(); 
            
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            
           
         //   byte[] buffer = new byte[BUFFER_SIZE];
            long bytesRead;
            byte[] key = null;
   				try {
   					key = generateKey(UploadController.getpassword());
   				} catch (Exception e) {
   					// TODO Auto-generated catch block
   					e.printStackTrace();
   				}
                      
                      
             

            try {
                while ((bytesRead = stream.read(buffer, 0, buffer.length)) > 0) {
                	
               	   try {
            						bytes = encodeFile(key, buffer);
            					} catch (Exception e) {
            						// TODO Auto-generated catch block
            						e.printStackTrace();
            					}
              //      requestStream.write(buffer, 0, buffer.length);
               	requestStream.write(bytes, 0, bytes.length);
                    uploadedBytes += bytesRead;
                    broadcastProgress(uploadedBytes, totalBytes);
                }
            } finally {
                closeInputStream(stream);
            }
            requestStream.write(boundaryBytes, 0, boundaryBytes.length);
           
         
            
        }
        
        
    }

    private long getTotalBytes(final ArrayList<FileToUpload> filesToUpload) {
        long total = 0;

        for (FileToUpload file : filesToUpload) {
            total += file.length();
        }

        return total;
    }

    private void closeInputStream(final InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception exc) { }
        }
    }

    private void closeOutputStream(final OutputStream stream) {
        if (stream != null) {
            try {
                stream.flush();
                stream.close();
            } catch (Exception exc) { }
        }
    }

    private void closeConnection(final HttpURLConnection connection) {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception exc) { }
        }
    }

    private void broadcastProgress(final long uploadedBytes, final long totalBytes) {

        final int progress = (int) (uploadedBytes * 100 / totalBytes);
        if (progress <= lastPublishedProgress) return;
        lastPublishedProgress = progress;

        updateNotificationProgress(progress);

        final Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(STATUS, STATUS_IN_PROGRESS);
        intent.putExtra(PROGRESS, progress);
        sendBroadcast(intent);
    }

    private void broadcastCompleted(final int responseCode, final String responseMessage) {

        final String filteredMessage;
        if (responseMessage == null) {
            filteredMessage = "";
        } else {
            filteredMessage = responseMessage;
        }

        updateNotificationCompleted();
        
  
		//	Log.w("Send Message","String atia "+Result);
     

        final Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(STATUS, STATUS_COMPLETED);
      //  intent.putExtra(SERVER_RESPONSE_CODE, responseCode);
          intent.putExtra(SERVER_RESPONSE_CODE, responseCode);
          intent.putExtra(SERVER_RESPONSE_MESSAGE, filteredMessage);
       // intent.putExtra(SERVER_RESPONSE_MESSAGE, Result);
        sendBroadcast(intent);
    }

    private void broadcastError(final Exception exception) {

        updateNotificationError();

        final Intent intent = new Intent(BROADCAST_ACTION);
        intent.setAction(BROADCAST_ACTION);
        intent.putExtra(STATUS, STATUS_ERROR);
        intent.putExtra(ERROR_EXCEPTION, exception);
        sendBroadcast(intent);
    }

    private void createNotification() {
        notification.setContentTitle(notificationConfig.getTitle())
                    .setContentText(notificationConfig.getMessage())
                    .setSmallIcon(notificationConfig.getIconResourceID())
                    .setProgress(100, 0, true);
        notificationManager.notify(0, notification.build());
    }

    private void updateNotificationProgress(final int progress) {
        notification.setContentTitle(notificationConfig.getTitle())
                    .setContentText(notificationConfig.getMessage())
                    .setSmallIcon(notificationConfig.getIconResourceID())
                    .setProgress(100, progress, false);
        notificationManager.notify(0, notification.build());
    }

    private void updateNotificationCompleted() {
  
    	
    	
    	
    	
	
    	
    	
    	
    	
    	if (notificationConfig.isAutoClearOnSuccess()) {
            notificationManager.cancel(0);
            return;
        } else {
            notification.setContentTitle(notificationConfig.getTitle())
                       .setContentText(notificationConfig.getCompleted())
            		//	.setContentText("Result ="+Result)
                        .setSmallIcon(notificationConfig.getIconResourceID())
                        .setProgress(0, 0, false);
            notificationManager.notify(0, notification.build());
        }
    }

    private void updateNotificationError() {
        notification.setContentTitle(notificationConfig.getTitle())
                    .setContentText(notificationConfig.getError())
                    .setSmallIcon(notificationConfig.getIconResourceID())
                    .setProgress(0, 0, false);
        notificationManager.notify(0, notification.build());
    }
    
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
}
