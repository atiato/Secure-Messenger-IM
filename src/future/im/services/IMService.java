/* 
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package future.im.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import future.im.GroupMessaging;
import future.im.Login;
import future.im.Messaging;
import future.im.Messaging.MessageReceiver;
import future.im.communication.SocketOperator;
import future.im.interfaces.IAppManager;
import future.im.interfaces.ISocketOperator;
import future.im.interfaces.IUpdateData;
import future.im.tools.FriendController;
import future.im.tools.GroupController;
import future.im.tools.GroupMessageController;
import future.im.tools.LocalStorageHandler;
import future.im.tools.MessageController;
import future.im.tools.UploadController;
import future.im.tools.XMLHandler;
import future.im.types.FriendInfo;
import future.im.types.GroupInfo;
import future.im.types.GroupMessageInfo;
import future.im.types.MessageInfo;

import android.app.AlertDialog;
import android.app.Notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import future.im.R;


/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The {@link LocalServiceController}
 * and {@link LocalServiceBinding} classes show how to interact with the
 * service.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
public class IMService extends Service implements IAppManager, IUpdateData {
//	private NotificationManager mNM;
	
	public static String USERNAME;
	public static final String TAKE_MESSAGE = "Take_Message";
	public static final String FRIEND_LIST_UPDATED = "Take Friend List";
	public static final String MESSAGE_LIST_UPDATED = "Take Message List";
	public static final String GROUP_LIST_UPDATED = "Take Group List";
	public static final String TAKE_GROUP_MESSAGE = "Take_Group_Message";
	public ConnectivityManager conManager = null; 
	private final int UPDATE_TIME_PERIOD = 15000;
//	private static final INT LISTENING_PORT_NO = 8956;
	private String rawFriendList = new String();
	private String rawMessageList = new String();
	private String rawGroupList = new String();
	private String rawGroupMessageList = new String();
	private int notificationID = 100;
	   private int numMessages = 0;
	   private String msgapp="";
	ISocketOperator socketOperator = new SocketOperator(this);

	private final IBinder mBinder = new IMBinder();
	private String username;
	private String password;
	private boolean authenticatedUser = false;
	 // timer to take the updated data from server
	private Timer timer;
	
	  static {
	        System.loadLibrary("ecc");
	    }
	    
	    public native String  DecryptKey(String ecckey);
	    public native String readfromFile(String path);

	private LocalStorageHandler localstoragehandler; 
	
	private NotificationManager mNM;

	public class IMBinder extends Binder {
		public IAppManager getService() {
			return IMService.this;
		}
		
	}
	   
    @Override
    public void onCreate() 
    {   	
         mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

         localstoragehandler = new LocalStorageHandler(this);
        // Display a notification about us starting.  We put an icon in the status bar.
     //   showNotification();
    	conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	new LocalStorageHandler(this);
    	
    	// Timer is used to take the friendList info every UPDATE_TIME_PERIOD;
		timer = new Timer();   
		
		Thread thread = new Thread()
		{
			@Override
			public void run() {			
				
				//socketOperator.startListening(LISTENING_PORT_NO);
				Random random = new Random();
				int tryCount = 0;
				while (socketOperator.startListening(10000 + random.nextInt(20000))  == 0 )
				{		
					tryCount++; 
					if (tryCount > 10)
					{
						// if it can't listen a port after trying 10 times, give up...
						break;
					}
					
				}
			}
		};		
		thread.start();
    
    }

/*
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.local_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }
*/	

	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}




	/**
	 * Show a notification while this service is running.
	 * @param msg 
	 **/
    @SuppressWarnings({ "static-access", "deprecation" })
	private void showNotification(String username, String msg) 
	{       
        // Set the icon, scrolling text and TIMESTAMP
    	String title = "New Message! (" + username + ")";
 				
    	String text = username + ": " + 
     				((msg.length() < 15) ? msg : msg.substring(0, 15)+ "...");
    
    	Uri sound = Uri.parse("android.resource://" + "future.im" + "/" + R.raw.siren);

    //	private int notificationCounter;
    //	int notificationNumber = notificationCounter.incrementAndGet();
    //	final int myNotificationId = 1;
    	//NotificationCompat.Builder notification = new NotificationCompat.Builder(R.drawable.stat_sample, title,System.currentTimeMillis());
    	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
    	.setSmallIcon(R.drawable.stat_sample)
    	.setContentTitle(title)
    	.setContentText(text).setNumber(++numMessages); 
    	
    //	Notification notification = new Notification.InboxStyle().addLine("line1 text").addLine("line2 text").addLine("line3 text").build();
    
    		       //  .setLargeIcon(aBitmap))
    		  

        Intent i = new Intent(this, Messaging.class);
        i.putExtra(FriendInfo.USERNAME, username);
       i.putExtra(MessageInfo.MESSAGETEXT, msg);
        i.putExtra(FriendInfo.PORT, "12345");
		i.putExtra(FriendInfo.IP, "1111");	
        i.setFlags(i.FLAG_ACTIVITY_CLEAR_TOP);
        i.setAction("future.im"+System.currentTimeMillis());//unique per contact

        // |i.FLAG_ACTIVITY_SINGLE_TOP The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(),
                i, PendingIntent.FLAG_UPDATE_CURRENT);
	//	startActivity(i);
        

        // Set the info for the views that show in the notification panel.
        // msg.length()>15 ? MSG : msg.substring(0, 15);
        mBuilder.setContentIntent(contentIntent); 
        
     //   mBuilder.setContentText("New message from " + username + ": " + msg);
        mBuilder.setAutoCancel(true);
        mBuilder.setSound(sound);
        //TODO: it can be improved, for instance message coming from same user may be concatenated 
        // next version
        
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
   //    mNM.notify((username+msg).hashCode(), mBuilder.build());
        //myNotificationId
        
        mNM.notify((username).hashCode(), mBuilder.build());
    }
	 
    
    @SuppressWarnings({ "static-access", "deprecation" })
   	private void showGroupNotification(String username, String msg, String fromuid) 
   	{       
           // Set the icon, scrolling text and TIMESTAMP
       	String title = "New Group Message! (" + fromuid + ")";
    			
       	String groupowner =new String();
       	String text = fromuid + ": " + 
        				((msg.length() < 15) ? msg : msg.substring(0, 15)+ "...");
       
       	Uri sound = Uri.parse("android.resource://" + "future.im" + "/" + R.raw.siren);
       	GroupInfo [] group=GroupController.getGroupsInfo();
       	int z=0;
       	while (z<group.length)
       	{
       		if (username.equals(group[z].groupname))
       			{
       			groupowner= group[z].groupowner;
       			break;
       			}
       	z++;	
       	}
       //	private int notificationCounter;
       //	int notificationNumber = notificationCounter.incrementAndGet();
       //	final int myNotificationId = 1;
       	//NotificationCompat.Builder notification = new NotificationCompat.Builder(R.drawable.stat_sample, title,System.currentTimeMillis());
       	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
       	.setSmallIcon(R.drawable.stat_sample)
       	.setContentTitle(title)
       	.setContentText(text).setNumber(++numMessages); 
       	
       //	Notification notification = new Notification.InboxStyle().addLine("line1 text").addLine("line2 text").addLine("line3 text").build();
       
       		       //  .setLargeIcon(aBitmap))
       		  
       	
           Intent i = new Intent(this, GroupMessaging.class);
           i.putExtra(GroupInfo.GROUPOWNER,groupowner);
           i.putExtra(GroupInfo.GROUPNAME, username);
           i.putExtra(GroupInfo.USER, this.username);
          i.putExtra(GroupMessageInfo.MESSAGETEXT, msg);
          i.putExtra(GroupInfo.GROUP_MEMEBERS_LIST, GroupMessageController.getmemberlist());
           i.putExtra(FriendInfo.PORT, "12345");
   		i.putExtra(FriendInfo.IP, "1111");	
           i.setFlags(i.FLAG_ACTIVITY_CLEAR_TOP);
           i.setAction("future.im"+System.currentTimeMillis());//unique per contact

           // |i.FLAG_ACTIVITY_SINGLE_TOP The PendingIntent to launch our activity if the user selects this notification
           PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(),
                   i, PendingIntent.FLAG_UPDATE_CURRENT);
   	//	startActivity(i);
           

           // Set the info for the views that show in the notification panel.
           // msg.length()>15 ? MSG : msg.substring(0, 15);
           mBuilder.setContentIntent(contentIntent); 
           
        //   mBuilder.setContentText("New message from " + username + ": " + msg);
           mBuilder.setAutoCancel(true);
           mBuilder.setSound(sound);
           //TODO: it can be improved, for instance message coming from same user may be concatenated 
           // next version
           
           // Send the notification.
           // We use a layout id because it is a unique number.  We use it later to cancel.
      //    mNM.notify((username+msg).hashCode(), mBuilder.build());
           //myNotificationId
           
           mNM.notify((username).hashCode(), mBuilder.build());
       }

	public String getUsername() {
		return this.username;
	}

	
	public String sendMessage(String  username, String  tousername, String message,String key,String MSGID,String Seen) throws UnsupportedEncodingException 
	{	
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	//	String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		String params = "username="+ URLEncoder.encode(this.username,"UTF-8") +
						"&password="+ URLEncoder.encode(this.password,"UTF-8") +
						"&to=" + URLEncoder.encode(tousername,"UTF-8") +
						"&message="+ URLEncoder.encode(message,"UTF-8") +
						"&action="  + URLEncoder.encode("sendMessage","UTF-8")+
						"&key=" + URLEncoder.encode(key,"UTF-8")+
						"&msgid="+URLEncoder.encode(MSGID,"UTF-8")+
						"&seen="+URLEncoder.encode(Seen,"UTF-8")+
						"&userKey=" + URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8")+
						"&";		
		Log.i("PARAMS", params);
		 Log.w("Debug Log for socket", "Socket param"+socketOperator);
		return socketOperator.sendHttpRequest(params);		
	}
	
	//if (imService.sendGroupMessage(imService.getUsername(), group.groupname, IMKeys.Encrypt(sks,message.toString()),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),"notseen") == null)

	public String sendGroupMessage(String  username,String  groupname,String message,String key,String Seen) throws UnsupportedEncodingException 
	{	
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	//	String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		String params = "username="+ URLEncoder.encode(this.username,"UTF-8") +
						"&password="+ URLEncoder.encode(this.password,"UTF-8") +
						"&to=" + URLEncoder.encode(groupname,"UTF-8") +
						"&message="+ URLEncoder.encode(message,"UTF-8") +
						"&action="  + URLEncoder.encode("sendGroupMessage","UTF-8")+
						"&key=" + URLEncoder.encode(key,"UTF-8")+
						//"&msgid="+URLEncoder.encode(MSGID,"UTF-8")+
						"&seen="+URLEncoder.encode(Seen,"UTF-8")+
						"&userKey=" + URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8")+
						"&";		
		Log.i("PARAMS", params);
		 Log.w("Debug Log for socket", "Socket param"+socketOperator);
		return socketOperator.sendHttpRequest(params);		
	}
	
	
	public SecretKeySpec Gen()
	{
		
		 SecretKeySpec sks = null;
	        try {
	            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
	            Time t = new Time();
	            t.setToNow();
	            Random rand=new Random(t.toMillis(false));
	            
	            
	            sr.setSeed(rand.toString().getBytes());
	            KeyGenerator kg = KeyGenerator.getInstance("AES");
	            kg.init(128, sr);
	            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
	            return sks;
	        } catch (Exception e) {
	            Log.e("Error generating Keys", "AES secret key spec error");
	        }	
		
		
		return sks;
		
		
	}
	
public String Encrypt(SecretKeySpec sks,String message)
	
	{
		
		 byte[] encodedBytes = null;
	        try {
	            Cipher c = Cipher.getInstance("AES");
	            c.init(Cipher.ENCRYPT_MODE, sks);
	            encodedBytes = c.doFinal(message.getBytes());
	        } catch (Exception e) {
	            Log.e("AES", "AES encryption error");
	        }
	        
	        Log.e("[ENCODED]:\n", 
	            Base64.encodeToString(encodedBytes, Base64.DEFAULT) + "\n");
	        
		return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
		
		
	}
	

	
	private String getFriendList() throws UnsupportedEncodingException 	{		
		// after authentication, server replies with friendList xml
		
		 rawFriendList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		 if (rawFriendList != null) {
			 this.parseFriendInfo(rawFriendList);
		 }
		 return rawFriendList;
	}
	
	private String getGroupList() throws UnsupportedEncodingException 	{		
		// after authentication, server replies with friendList xml
		
		 rawGroupList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		 if (rawGroupList != null) {
			 this.parseGroupInfo(rawGroupList);
		 }
		 return rawGroupList;
	}
	
	private String getMessageList() throws UnsupportedEncodingException 	{		
		// after authentication, server replies with friendList xml
		
		 rawMessageList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		 if (rawMessageList != null) {
			 this.parseMessageInfo(rawMessageList);
		 }
		 return rawMessageList;
	}
	
	private String getGroupMessageList() throws UnsupportedEncodingException 	{		
		// after authentication, server replies with friendList xml
		
		 rawGroupMessageList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		 if (rawGroupMessageList != null) {
			 this.parseGroupMessageInfo(rawGroupMessageList);
		 }
		 return rawGroupMessageList;
	}
	
	

	/**
	 * authenticateUser: it authenticates the user and if succesful
	 * it returns the friend list or if authentication is failed 
	 * it returns the "0" in string type
	 * @throws UnsupportedEncodingException 
	 * */
	public String authenticateUser(String usernameText, String passwordText) throws UnsupportedEncodingException 
	{
		this.username = usernameText;
		this.password = passwordText;	
		
		this.authenticatedUser = false;
		String result=null;
		
		while (result == null)
		{
		result = this.getFriendList(); //socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		if (result!=null) break;
		}
		if (result != null && !result.equals(Login.AUTHENTICATION_FAILED)) 
		{			
			// if user is authenticated then return string from server is not equal to AUTHENTICATION_FAILED
			this.authenticatedUser = true;
			rawFriendList = result;
			USERNAME = this.username;
			Intent i = new Intent(FRIEND_LIST_UPDATED);					
			i.putExtra(FriendInfo.FRIEND_LIST, rawFriendList);
			sendBroadcast(i);
			
			timer.schedule(new TimerTask()
			{			
				public void run() 
				{
					try {					
						//rawFriendList = IMService.this.getFriendList();
						// sending friend list 
						Intent i = new Intent(FRIEND_LIST_UPDATED);
						Intent i2 = new Intent(MESSAGE_LIST_UPDATED);
						Intent i3 = new Intent(GROUP_LIST_UPDATED);
						Intent i4 = new Intent(TAKE_GROUP_MESSAGE);
				
						String tmp=null;
						String tmp2=null;
						String tmp3=null;
						String tmp4=null;
						
						while (tmp==null)
					{
						tmp = IMService.this.getFriendList();
						tmp2 = IMService.this.getMessageList();
						tmp3 = IMService.this.getGroupList();
						tmp4=IMService.this.getGroupMessageList();
						if (tmp != null) break;
					}	
						
						if (tmp != null) {
							i.putExtra(FriendInfo.FRIEND_LIST, tmp);
							sendBroadcast(i);	
							Log.i("friend list broadcast sent ", "");
						
						if (tmp2 != null) {
							i2.putExtra(MessageInfo.MESSAGE_LIST, tmp2);
							sendBroadcast(i2);	
							Log.i("friend list broadcast sent ", "");
										}
						if (tmp3 != null) {
							i3.putExtra(GroupInfo.GROUP_LIST, tmp3);
							sendBroadcast(i3);	
							Log.i("Group list broadcast sent ", "");
										}
						if (tmp4 != null) {
							i4.putExtra(GroupMessageInfo.MESSAGE_LIST, tmp4);
							sendBroadcast(i4);	
							Log.i("Group Message list broadcast sent ", "");
										}
						}
						
						else {
							Log.i("friend list returned null", "");
							
							
						}
					}
					catch (Exception e) {
						
					
						e.printStackTrace();
					}					
				}			
			}, UPDATE_TIME_PERIOD, UPDATE_TIME_PERIOD);
		}
		
		
		return result;		
	}

	public void messageReceived(String username, String message, String key,String msgid,String flag) 
	{				
		
		//FriendInfo friend = FriendController.getFriendInfo(username);
		MessageInfo msg = MessageController.checkMessage(username);
		 byte[] clearmessage = null;
		 String message1="";	
		if ( !message.equals("") && message!=null){
			
	//	String message1=DecryptKey(message);	
			
		byte[]	keyBytes=DecryptKey(key).getBytes();
		byte[]	decode = Base64.decode(keyBytes, Base64.DEFAULT);
		byte[] encryptedmessage=Base64.decode(message, Base64.DEFAULT);
		
	        try {
	            Cipher c = Cipher.getInstance("AES");
	            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decode, "AES"));
	            clearmessage = c.doFinal(encryptedmessage);
	            Log.i("Success", "AES decryption Success");
	        } catch (Exception e) {
	            Log.e("Error", "AES decryption error");
	        }
	    
	        message1=new String(clearmessage);
		}
		if ( msg != null)
		{			
			Intent i = new Intent(TAKE_MESSAGE);
		//	if (clearmessage!=null)
	/*		if (!message1.equals(""))
			{
				
			}
			else
			{
				
				message1="SEENSMS"+msgid;
			}*/
				i.putExtra(MessageInfo.USERID, msg.userid);			
		//	i.putExtra(MessageInfo.MESSAGETEXT, msg.messagetext);
		//	i.putExtra(MessageInfo.MESSAGETEXT, new String(clearmessage));
				i.putExtra(MessageInfo.MESSAGETEXT, message1);
				Log.i("Receving message before Broadcasting","msg.msgid"+msgid);
			i.putExtra(MessageInfo.MSGID, msgid);
			i.putExtra(MessageInfo.SEENFLAG, flag);
			Log.i("Receving message before Broadcasting","msg.msgid"+msgid+"flag"+flag+"messaeg1"+message1);
			this.sendBroadcast(i);
			//sendStickyBroadcast(i);
			String activeFriend = FriendController.getActiveFriend();
			if (activeFriend == null || activeFriend.equals(username) == false) 
			{
			//	localstoragehandler.insert(username,this.getUsername(), message.toString());
			//	showNotification(username, message);
			if(!flag.equals("delivered") && !flag.equals("seen") && !message1.equals("") ) {
	//			localstoragehandler.insert(username,this.getUsername(), new String(clearmessage),"");
	//			showNotification(username, new String(clearmessage));
				localstoragehandler.insert(username,this.getUsername(), message1,"","no");
							showNotification(username, message1);
					
			}
			else if (flag.equals("delivered"))
			{
				localstoragehandler.update(""+msgid,flag);
				Log.i("Updating Successfully delivered","msgid "+msgid+"flag "+flag);
			}
			
			
			
			}
			
			Log.i("TAKE_MESSAGE broadcast sent by im service", "");
		}	
		
	}  
	
	
	public void GroupmessageReceived(String username, String message, String key,String msgid,String flag,String fromuid) 
	{				
		Log.i("after receving in group message","username"+username+"message"+message+"flag"+flag+"key"+key+"from uid"+fromuid);
		//FriendInfo friend = FriendController.getFriendInfo(username);
		GroupMessageInfo msg = GroupMessageController.checkMessage(username);
		 byte[] clearmessage = null;
		 String message1="";	
		if ( !message.equals("") && message!=null){
			
	//	String message1=DecryptKey(message);	
			
		byte[]	keyBytes=DecryptKey(key).getBytes();
		byte[]	decode = Base64.decode(keyBytes, Base64.DEFAULT);
		byte[] encryptedmessage=Base64.decode(message, Base64.DEFAULT);
		
	        try {
	            Cipher c = Cipher.getInstance("AES");
	            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decode, "AES"));
	            clearmessage = c.doFinal(encryptedmessage);
	            Log.i("Success", "AES decryption Success");
	        } catch (Exception e) {
	            Log.e("Error", "AES decryption error");
	        }
	    
	        message1=new String(clearmessage);
		}
	//	if ( msg != null)
		
		if (!message.equals("") && message!=null)
		{			
			Intent i = new Intent(TAKE_GROUP_MESSAGE);
		//	if (clearmessage!=null)
	/*		if (!message1.equals(""))
			{
				
			}
			else
			{
				
				message1="SEENSMS"+msgid;
			}*/
				i.putExtra(GroupMessageInfo.USERID, msg.userid);			
		//	i.putExtra(MessageInfo.MESSAGETEXT, msg.messagetext);
		//	i.putExtra(MessageInfo.MESSAGETEXT, new String(clearmessage));
				i.putExtra(GroupMessageInfo.MESSAGETEXT, message1);
				Log.i("Receving message before Broadcasting","msg.msgid"+msgid);
		//	i.putExtra(MessageInfo.MSGID, msgid);
			i.putExtra(GroupMessageInfo.SEENFLAG, flag);
			i.putExtra(GroupMessageInfo.FROM, fromuid);
			Log.i("Receving message before Broadcasting","msg.msgid"+""+"flag"+flag+"messaeg1"+message1);
			this.sendBroadcast(i);
			//sendStickyBroadcast(i);
			String activegroup = GroupController.getActiveGroup();
			if (activegroup == null || activegroup.equals(username) == false) 
			{
			//	localstoragehandler.insert(username,this.getUsername(), message.toString());
			//	showNotification(username, message);
			if(!message1.equals("") ) {
	//			localstoragehandler.insert(username,this.getUsername(), new String(clearmessage),"");
	//			showNotification(username, new String(clearmessage));
				localstoragehandler.groupinsert(username,username, message1,"",fromuid);
							showGroupNotification(username, message1,fromuid);
					
			}
		
			
			
			
			}
			
			Log.i("TAKE_Group_MESSAGE broadcast sent by im service", "");
		}	
		
	} 
	
	private String getAuthenticateUserParams(String usernameText, String passwordText) throws UnsupportedEncodingException 
	{		
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		//String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		
		String params = "username=" + URLEncoder.encode(usernameText,"UTF-8") +
						"&password="+ URLEncoder.encode(passwordText,"UTF-8") +
						"&action="  + URLEncoder.encode("authenticateUser","UTF-8")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&userKey="+ URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8") +
						"&";		
		
		return params;		
	}
	
	//changepassword(usernameText.getText().toString(), oldpassword.getText().toString(),newpassword.getText().toString())
	
	
	public String changepassword(String usernameText, String oldpasswordText,String newpasswordText) throws UnsupportedEncodingException 
	{		
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		//String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		
		String params = "username=" + URLEncoder.encode(usernameText,"UTF-8") +
						"&password="+ URLEncoder.encode(oldpasswordText,"UTF-8") +
						"&old="+ URLEncoder.encode(oldpasswordText,"UTF-8") +
						"&new="+ URLEncoder.encode(newpasswordText,"UTF-8") +
						"&action="  + URLEncoder.encode("ChangePassword","UTF-8")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&userKey="+ URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8") +
						"&";		
		
		return socketOperator.sendHttpRequest(params);		
	}
	
	
	public String deletemessages() throws UnsupportedEncodingException 
	{		
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		//String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		
		String params = "username=" + URLEncoder.encode(this.username,"UTF-8") +
						"&password="+ URLEncoder.encode(this.password,"UTF-8") +
						"&action="  + URLEncoder.encode("DeleteMessages","UTF-8")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&userKey="+ URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8") +
						"&";		
		
		return socketOperator.sendHttpRequest(params);		
	}
	
	
	public String removeme(String groupname) throws UnsupportedEncodingException 
	{		
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		//String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		
		String params = "username=" + URLEncoder.encode(this.username,"UTF-8") +
						"&password="+ URLEncoder.encode(this.password,"UTF-8") +
						"&groupname="  + URLEncoder.encode(groupname,"UTF-8")+
						"&action="  + URLEncoder.encode("RemovemeGroup","UTF-8")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&userKey="+ URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8") +
						"&";		
		
		return socketOperator.sendHttpRequest(params);		
	}
	
	
	public String removegroup(String groupname) throws UnsupportedEncodingException 
	{		
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		//String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		
		String params = "username=" + URLEncoder.encode(this.username,"UTF-8") +
						"&password="+ URLEncoder.encode(this.password,"UTF-8") +
						"&groupname="  + URLEncoder.encode(groupname,"UTF-8")+
						"&action="  + URLEncoder.encode("RemoveGroup","UTF-8")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&userKey="+ URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8") +
						"&";		
		
		return socketOperator.sendHttpRequest(params);		
	}
	
	public String getmembers(String groupname) throws UnsupportedEncodingException 
	{		
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		//String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		
		String params = "username=" + URLEncoder.encode(this.username,"UTF-8") +
						"&password="+ URLEncoder.encode(this.password,"UTF-8") +
						"&groupname="  + URLEncoder.encode(groupname,"UTF-8")+
						"&action="  + URLEncoder.encode("GetMembers","UTF-8")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&userKey="+ URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8") +
						"&";		
		
		return socketOperator.sendHttpRequest(params);		
	}
	
//RemoveMembers	
	
	public String RemoveMembers(String usernameText,String passwordText,String groupmemberslist,String groupname, String groupowner) throws UnsupportedEncodingException 
	{		
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		//String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		
		
		String params = "username=" + usernameText +
							"&password=" + passwordText +
						"&groupname="  + URLEncoder.encode(groupname,"UTF-8")+
				
						"&groupmemberslist="  + URLEncoder.encode(groupmemberslist,"UTF-8")+
						"&groupowner="  + URLEncoder.encode(groupowner,"UTF-8")+
						"&action="  + URLEncoder.encode("RemoveMembers","UTF-8")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&userKey="+ URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8") +
						"&";		
		
		Log.i("PARAMS", params);
		return socketOperator.sendHttpRequest(params);		
	}
	public String CreateGroup(String usernameText,String passwordText,String groupmemberslist,String groupname, String groupowner) throws UnsupportedEncodingException 
	{		
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		//String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		
		
		String params = "username=" + usernameText +
							"&password=" + passwordText +
						"&groupname="  + URLEncoder.encode(groupname,"UTF-8")+
				
						"&groupmemberslist="  + URLEncoder.encode(groupmemberslist,"UTF-8")+
						"&groupowner="  + URLEncoder.encode(groupowner,"UTF-8")+
						"&action="  + URLEncoder.encode("CreateGroup","UTF-8")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&userKey="+ URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8") +
						"&";		
		
		Log.i("PARAMS", params);
		return socketOperator.sendHttpRequest(params);		
	}

	public String createBroadCast() throws UnsupportedEncodingException 
	{		
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		//String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		
		
		String params = "username=" + this.username +
							"&password=" + this.password +
						"&action="  + URLEncoder.encode("CreateBroadCast","UTF-8")+
						"&userKey="+ URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8") +
						"&";		
		
		Log.e("PARAMS", params);
		
		
		 String test=socketOperator.sendHttpRequest(params);		
		 Log.e("response back", "Allah "+test);
		 
		 return test;
		 
	}

	
	
	
	public void setUserKey(String value) 
	{		
	}

	public boolean isNetworkConnected() {
		return conManager.getActiveNetworkInfo().isConnected();
	}
	
	public boolean isUserAuthenticated(){
		return authenticatedUser;
	}
	
	public String getLastRawFriendList() {		
		return this.rawFriendList;
	}
	
	@Override
	public void onDestroy() {
		Log.i("IMService is being destroyed", "...");
		super.onDestroy();
	}
	
	public void exit() 
	{
		timer.cancel();
		socketOperator.exit(); 
		socketOperator = null;
		this.stopSelf();
	}
	
	public String signUpUser(String usernameText, String passwordText,
			String emailText) 
	{
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	//	String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		String params = null;
		try {
			params = "username=" + usernameText +
							"&password=" + passwordText +
							"&action=" + "signUpUser"+
							"&email=" + emailText+
							"&userKey=" + URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8")+
							"&";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8")
		
		String result = socketOperator.sendHttpRequest(params);	
		
		return result;
	}

	public String addNewFriendRequest(String friendUsername) 
	{
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	//	String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		String params = null;
		
			try {
				params = "username=" + this.username +
				"&password=" + this.password +
				"&action=" + "addNewFriend" +
				"&friendUserName=" + friendUsername +
				"&userKey=" + URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8")+
				"&";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		

		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
	}

	public String sendFriendsReqsResponse(String approvedFriendNames,
			String discardedFriendNames) 
	{
		
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	//	String readoutput=readfromFile(baseDir+File.separator+"atiato.prop");
		String readoutput=readfromFile(baseDir);
		
		String params = null;
		try {
			params = "username=" + this.username +
			"&password=" + this.password +
			"&action=" + "responseOfFriendReqs"+
			"&approvedFriends=" + approvedFriendNames +
			"&discardedFriends=" +discardedFriendNames +
			"&userKey=" + URLEncoder.encode(readoutput.replace("\n", ""),"UTF-8")+
			"&";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
		
	} 
	
	private void parseFriendInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XMLHandler(IMService.this));		
		} 
		catch (ParserConfigurationException e) {			
			e.printStackTrace();
		}
		catch (SAXException e) {			
			e.printStackTrace();
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}	
	}
	private void parseMessageInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XMLHandler(IMService.this));		
		} 
		catch (ParserConfigurationException e) {			
			e.printStackTrace();
		}
		catch (SAXException e) {			
			e.printStackTrace();
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}	
	}
	
	
	private void parseGroupMessageInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XMLHandler(IMService.this));		
		} 
		catch (ParserConfigurationException e) {			
			e.printStackTrace();
		}
		catch (SAXException e) {			
			e.printStackTrace();
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}	
	}
	
	
	
	private void parseGroupInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XMLHandler(IMService.this));		
		} 
		catch (ParserConfigurationException e) {			
			e.printStackTrace();
		}
		catch (SAXException e) {			
			e.printStackTrace();
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}	
	}

	public void updateData(MessageInfo[] messages,FriendInfo[] friends,
			FriendInfo[] unApprovedFriends, String userKey ,GroupInfo[] groups ,GroupMessageInfo[] groupmessages) 
	{
		this.setUserKey(userKey);
		//FriendController.	
		MessageController.setMessagesInfo(messages);
		//Log.i("MESSAGEIMSERVICE","messages.length="+messages.length);
		GroupMessageController.setGroupMessagesInfo(groupmessages);
		UploadController.setgroup(groups);
		
		int i = 0;
		while (i < messages.length){
			messageReceived(messages[i].userid,messages[i].messagetext,messages[i].key,messages[i].msgid,messages[i].flag);
			Log.i("in the receiver","messages IDSsssss" + messages[i].msgid);
			//appManager.messageReceived(messages[i].userid,messages[i].messagetext);
			i++;
		}
		
		int k = 0;
		while (k < groupmessages.length){
			GroupmessageReceived(groupmessages[k].userid,groupmessages[k].messagetext,groupmessages[k].key,"",groupmessages[k].flag,groupmessages[k].from);
			//appManager.messageReceived(messages[i].userid,messages[i].messagetext);
			k++;
		}
		
		
		FriendController.setFriendsInfo(friends);
		FriendController.setUnapprovedFriendsInfo(unApprovedFriends);
		GroupController.setGroupsInfo(groups);
		
		
	}


	 @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
	        Log.d("Start", "onStartCommand");

	        return START_STICKY;
	    }

	

	    public void setAsForeground() {
	        startForeground(future.im.Notif.notifId, future.im.Notif.getNotification(getApplicationContext()));
	    }

	    public void setAsBackground() {
	        stopForeground(true);
	    }
	
	
	
}