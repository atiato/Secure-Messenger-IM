package future.im;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;



import future.im.interfaces.IAppManager;
import future.im.services.IMService;
import future.im.tools.FriendController;
import future.im.tools.GroupController;
import future.im.tools.LocalStorageHandler;
import future.im.types.FriendInfo;
import future.im.types.GroupInfo;
import future.im.types.GroupMessageInfo;
import future.im.types.MessageInfo;

import future.im.R;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.Log;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

public class GroupMessaging extends Activity {

	private static final int MESSAGE_CANNOT_BE_SENT = 0;
	public String username;
	private EditText messageText;
//	private EditText usermessageHistoryText;
	private EditText friendmessageHistoryText;
	private AdView adView;

	  /* Your ad unit id. Replace with your actual ad unit id. */
	  private static final String AD_UNIT_ID = "a1532dfd78cf267";
	private long msgid;
	private String pos;
	private int poose;
	private String in="";
	String user0="";
	public static final int HISTORY_ID = Menu.FIRST;
    public static final int UPlOADFILE_ID = Menu.FIRST + 1;
    public static final int DELETE_ID = Menu.FIRST + 2;
    public static final int REMOVEME_ID = Menu.FIRST + 3;
    public static final int REMOVEGROUP_ID = Menu.FIRST + 4;
    public static final int REMOVEMEMBER_ID = Menu.FIRST + 5;
    public static final int GETMEMBER_ID = Menu.FIRST + 6;

	private ImageButton sendMessageButton;
	private Button btnButton;
	private IAppManager imService;
	private IMService IMKeys = new IMService();
	private GroupInfo group = new GroupInfo();
	private LocalStorageHandler localstoragehandler; 
	private Cursor dbCursor;
	private List<MyMessageView> ListedView = new ArrayList<MyMessageView>();
	private List<MyMessageView> UpdateListedView=new ArrayList<MyMessageView>();
	String friendlist = new String();
	String [] members;
	Hashtable<String, String> source = new Hashtable<String,String>();
	HashMap<String, String>  map = new HashMap(source);
	
	 private static final Factory spannableFactory = Spannable.Factory
		        .getInstance();

		    private static final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

		static {
			//:(bye)
			addPattern(emoticons, ":(yy0)", R.drawable.e2);
			addPattern(emoticons, ":(yy1)", R.drawable.e1);
		    addPattern(emoticons, ":(yy2)", R.drawable.e2);
		    addPattern(emoticons, ":(yy3)", R.drawable.e3);
		    addPattern(emoticons, ":(yy4)", R.drawable.e4);
		    
		    addPattern(emoticons, ":(yy5)", R.drawable.e5);
		    addPattern(emoticons, ":(xx1)" ,R.drawable.e6	);
		    addPattern(emoticons, ":(xx2)" ,R.drawable.e7	);
		    addPattern(emoticons, ":(xx3)" ,R.drawable.e8	);
		    addPattern(emoticons, ":(xx4)" ,R.drawable.e9	);
		    addPattern(emoticons, ":(xx5)" ,R.drawable.e10	);
		    addPattern(emoticons, ":(xx6)" ,R.drawable.e11	);
		    addPattern(emoticons, ":(xx7)" ,R.drawable.e12	);
		    addPattern(emoticons, ":(xx8)" ,R.drawable.e13	);
		    addPattern(emoticons, ":(xx9)" ,R.drawable.e14	);
		    addPattern(emoticons, ":(xx10)" ,R.drawable.e15	);
		    addPattern(emoticons, ":(xx11)" ,R.drawable.e16	);
		    addPattern(emoticons, ":(xx12)" ,R.drawable.e17	);
		    addPattern(emoticons, ":(xx13)" ,R.drawable.e18	);
		    addPattern(emoticons, ":(xx14)" ,R.drawable.e19	);
		    addPattern(emoticons, ":(xx15)" ,R.drawable.e20	);
		    addPattern(emoticons, ":(xx16)" ,R.drawable.e21	);
		    addPattern(emoticons, ":(xx17)" ,R.drawable.e22	);
		    addPattern(emoticons, ":(xx18)" ,R.drawable.e23	);
		    addPattern(emoticons, ":(xx19)" ,R.drawable.e24	);
		    addPattern(emoticons, ":(xx20)" ,R.drawable.e25	);
		    addPattern(emoticons, ":(xx21)" ,R.drawable.e26	);
		    addPattern(emoticons, ":(xx22)" ,R.drawable.e27	);
		    addPattern(emoticons, ":(xx23)" ,R.drawable.e28	);
		    addPattern(emoticons, ":(xx24)" ,R.drawable.e29	);
		    addPattern(emoticons, ":(xx25)" ,R.drawable.e30	);
		    addPattern(emoticons, ":(xx26)" ,R.drawable.e31	);
		    addPattern(emoticons, ":(xx27)" ,R.drawable.e32	);
		    addPattern(emoticons, ":(xx28)" ,R.drawable.e33	);
		    addPattern(emoticons, ":(xx29)" ,R.drawable.e34	);
		    addPattern(emoticons, ":(xx30)" ,R.drawable.e35	);
		    addPattern(emoticons, ":(xx31)" ,R.drawable.e36	);
		    addPattern(emoticons, ":(xx32)" ,R.drawable.e37	);
		    addPattern(emoticons, ":(xx33)" ,R.drawable.e38	);
		    addPattern(emoticons, ":(xx34)" ,R.drawable.e39	);
		    addPattern(emoticons, ":(xx35)" ,R.drawable.e40	);
		    addPattern(emoticons, ":(xx36)" ,R.drawable.e41	);
		    addPattern(emoticons, ":(xx37)" ,R.drawable.e42	);
		    addPattern(emoticons, ":(xx38)" ,R.drawable.e43	);
		    addPattern(emoticons, ":(xx39)" ,R.drawable.e44	);
		    addPattern(emoticons, ":(xx40)" ,R.drawable.e45	);
		    addPattern(emoticons, ":(xx41)" ,R.drawable.e46	);
		    addPattern(emoticons, ":(xx42)" ,R.drawable.e47	);
		    addPattern(emoticons, ":(xx43)" ,R.drawable.e48	);
		    addPattern(emoticons, ":(xx44)" ,R.drawable.e49	);
		    addPattern(emoticons, ":(xx45)" ,R.drawable.e50	);
		    addPattern(emoticons, ":(xx46)" ,R.drawable.e51	);
		    addPattern(emoticons, ":(xx47)" ,R.drawable.e52	);
		    addPattern(emoticons, ":(xx48)" ,R.drawable.e53	);
		    addPattern(emoticons, ":(xx49)" ,R.drawable.e54	);
		    addPattern(emoticons, ":(xx50)" ,R.drawable.e55	);
		    addPattern(emoticons, ":(xx51)" ,R.drawable.e56	);
		    addPattern(emoticons, ":(xx52)" ,R.drawable.e57	);
		    addPattern(emoticons, ":(xx53)" ,R.drawable.e58	);
		    addPattern(emoticons, ":(xx54)" ,R.drawable.e59	);
		    addPattern(emoticons, ":(xx55)" ,R.drawable.e60	);
		    addPattern(emoticons, ":(xx56)" ,R.drawable.e61	);
		    addPattern(emoticons, ":(xx57)" ,R.drawable.e62	);
		    addPattern(emoticons, ":(xx58)" ,R.drawable.e63	);
		    addPattern(emoticons, ":(xx59)" ,R.drawable.e64	);
		    addPattern(emoticons, ":(xx60)" ,R.drawable.e65	);
		    addPattern(emoticons, ":(xx61)" ,R.drawable.e66	);
		    addPattern(emoticons, ":(xx62)" ,R.drawable.e67	);
		    addPattern(emoticons, ":(xx63)" ,R.drawable.e68	);
		    addPattern(emoticons, ":(xx64)" ,R.drawable.e69	);
		    addPattern(emoticons, ":(xx65)" ,R.drawable.e70	);
		    addPattern(emoticons, ":(xx66)" ,R.drawable.e71	);
		    addPattern(emoticons, ":(xx67)" ,R.drawable.e72	);
		    addPattern(emoticons, ":(xx68)" ,R.drawable.e73	);
		    addPattern(emoticons, ":(xx69)" ,R.drawable.e74	);
		    addPattern(emoticons, ":(xx70)" ,R.drawable.e75	);
		    addPattern(emoticons, ":(xx71)" ,R.drawable.e76	);
		    addPattern(emoticons, ":(xx72)" ,R.drawable.e77	);
		    addPattern(emoticons, ":(xx73)" ,R.drawable.e78	);
		    addPattern(emoticons, ":(azreal)" ,R.drawable.azreal);
		    addPattern(emoticons, ":(bigmouth)" ,R.drawable.bigmouth);
		    addPattern(emoticons, ":(brainy)" ,R.drawable.brainy);
		    addPattern(emoticons, ":(gargamel)" ,R.drawable.gargamel);
		    addPattern(emoticons, ":(farmer)" ,R.drawable.farmer);
		    addPattern(emoticons, ":(papasmurf)" ,R.drawable.papasmurf);
		    addPattern(emoticons, ":(handy)" ,R.drawable.handy);
		    addPattern(emoticons, ":(smurfgirl)" ,R.drawable.sassette);
		    addPattern(emoticons, ":(future)" ,R.drawable.future);
		    // ...add more patterns as you want
		}
		
		private static void addPattern(Map<Pattern, Integer> map, String smile,
		        int resource) {
		    map.put(Pattern.compile(Pattern.quote(smile)), resource);
		}
		
		
		public static boolean addSmiles(Context context, Spannable spannable) {
		    boolean hasChanges = false;
		    for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
		        Matcher matcher = entry.getKey().matcher(spannable);
		        while (matcher.find()) {
		            boolean set = true;
		            for (ImageSpan span : spannable.getSpans(matcher.start(),
		                    matcher.end(), ImageSpan.class))
		                if (spannable.getSpanStart(span) >= matcher.start()
		                        && spannable.getSpanEnd(span) <= matcher.end())
		                    spannable.removeSpan(span);
		                else {
		                    set = false;
		                    break;
		                }
		            if (set) {
		                hasChanges = true;
		                spannable.setSpan(new ImageSpan(context, entry.getValue()),
		                        matcher.start(), matcher.end(),
		                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		            }
		        }
		    }
		    return hasChanges;
		}

		public static Spannable getSmiledText(Context context, CharSequence text) {
		    Spannable spannable = spannableFactory.newSpannable(text);
		    addSmiles(context, spannable);
		    return spannable;
		}
	
	private ServiceConnection mConnection = new ServiceConnection() {
      
		
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((IMService.IMBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(GroupMessaging.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
    
    static {
        System.loadLibrary("ecc");
    }
    
    public native String  EncryptKey(String ecckey);
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	   
		
		setContentView(R.layout.messaging_screen); //messaging_screen);
				
	//	usermessageHistoryText = (EditText) findViewById(R.id.usermessageHistory);
	//	friendmessageHistoryText = (EditText) findViewById(R.id.friendmessageHistory);
		
//		usermessageHistoryText.setTextColor(Color.BLUE);
	//	usermessageHistoryText.setTextColor(Color.GRAY);
		
		 adView = new AdView(this);
		    adView.setAdSize(AdSize.BANNER);
		    adView.setAdUnitId(AD_UNIT_ID);

		    // Add the AdView to the view hierarchy. The view will have no size
		    // until the ad is loaded.
		    LinearLayout layout = (LinearLayout) findViewById(R.id.smslinearLayout);
		    layout.addView(adView);

		    // Create an ad request. Check logcat output for the hashed device ID to
		    // get test ads on a physical device.
		    AdRequest adRequest = new AdRequest.Builder()
		      //  .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		     //   .addTestDevice("JZAYIVGEUG45EA9L")
		        .build();

		    adView.loadAd(adRequest);
		
		messageText = (EditText) findViewById(R.id.message);
		
		messageText.requestFocus();			
		
		sendMessageButton = (ImageButton) findViewById(R.id.sendMessageButton);
		btnButton = (Button) findViewById(R.id.btnselect);
		
		Bundle extras = this.getIntent().getExtras();
		
		
		
		
		group.groupname = extras.getString(GroupInfo.GROUPNAME);
		group.groupowner=extras.getString(GroupInfo.GROUPOWNER);
		 user0=extras.getString(GroupInfo.USER);
		friendlist=extras.getString(GroupInfo.GROUP_MEMEBERS_LIST);
		members=friendlist.split(",");
		
		int j=0;
		int n=1;
		for (int i=0;i<members.length;i++)
		{	
			j++;
		map.put(members[i], ""+j);
		
		if (i==5*n)
		{
			j=0;
			n++;
			
		}
		
		}
		
		for (int i=0;i<members.length;i++)
		{	
			String friend0=map.get(members[i]);
			Log.e("mapped friends","value of the key: " + members[i]+"key"+friend0);
			
		}
		
		
		
		
		
		
	//	group.groupowner = extras.getString(GroupInfo.GROUPOWNER);
	//	friend.port = extras.getString(FriendInfo.PORT);
	//	String msg = extras.getString(MessageInfo.MESSAGETEXT);
	//	final String msgid1 = extras.getString(MessageInfo.MSGID);
	//	final String flag="";
		
		
		
		setTitle("" + group.groupname);
		
		Log.e("Group owner","xxx"+group.groupowner+"user name"+username);
		
	
	
		
		
	//	EditText friendUserName = (EditText) findViewById(R.id.friendUserName);
	//	friendUserName.setText(friend.userName);
		
		
		localstoragehandler = new LocalStorageHandler(this);
		updateview();
	
		final ListView lv = (ListView) findViewById(R.id.MessageView);
		
		
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

            @SuppressWarnings("deprecation")
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    int pos, long id) {
                // TODO Auto-generated method stub

                Log.i("long clicked","pos: " + pos);
                
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
                clipboard.setText(UpdateListedView.get(pos).getmessagetxt());
              
                Toast.makeText(GroupMessaging.this, "Copied", Toast.LENGTH_SHORT).show();
       		 

                return true;
            }
        }); 
		
		
		lv.setOnItemClickListener(new OnItemClickListener() {

		    @Override
		    public void onItemClick(AdapterView<?> parent, View view,
		            int position, long id) {
		    if(UpdateListedView.get(position).getmessagetxt().contains("Please Click to Download:"))
		    {
		    	Intent i = new Intent(GroupMessaging.this, DownloadImageActivity.class);
	    		
	    		i.putExtra(MessageInfo.HTTPTEXT, UpdateListedView.get(position).getmessagetxt());
		      
	    		Log.i("Hello!", "Y u no see me?");
	    		
	    		startActivity(i);
		    }
		    else if(UpdateListedView.get(position).getmessagetxt().contains("Please Click link to Download the File:"))
		    {
		    	Intent i = new Intent(GroupMessaging.this, DownloadFile.class);
	    		
	    		i.putExtra(MessageInfo.HTTPTEXT, UpdateListedView.get(position).getmessagetxt());
		      
	    		Log.i("Hello!", "Download a File?");
	    		
	    		startActivity(i);
		    }else {
		    	
		    	int index = UpdateListedView.get(position).getmessagetxt().indexOf("\nhttp");
				final String httpurl = UpdateListedView.get(position).getmessagetxt().substring(index+1);
				if (UpdateListedView.get(position).getmessagetxt().substring(index+1).contains("http")||UpdateListedView.get(position).getmessagetxt().substring(index+1).contains("HTTP"))
				{
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(httpurl));
					startActivity(i);
				}
		    }
		    //else to call URL
		    //Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.vogella.com"));
		    //startActivity(i); 
		    }

		});
		
dbCursor = localstoragehandler.getgroup(group.groupname, IMService.USERNAME );
		
		if (dbCursor.getCount() > 0){
		int noOfScorer = 0;
	//	dbCursor.is.moveToFirst();
		dbCursor.moveToLast();
	//	    while ((!dbCursor.isAfterLast())&&noOfScorer<dbCursor.getCount()) 
	    while ((!dbCursor.isBeforeFirst())&&noOfScorer<dbCursor.getCount())
		    {
		        noOfScorer++;
		    
					this.firedappendToMessageHistory(dbCursor.getString(1) ,dbCursor.getString(2),dbCursor.getString(3),dbCursor.getString(4),dbCursor.getString(5),dbCursor.getString(6));
					/*values.put("groupowner", owner);
			values.put("groupname", groupname);
			values.put("message", message);
			values.put(FLAG_FLAG, flag);
			values.put(TIME_TIME, getCurrDate()+":");*/
			
					
			//		this.friendappendToMessageHistory(dbCursor.getString(2) , dbCursor.getString(3));
  
		    //  dbCursor.moveToNext();
					dbCursor.moveToPrevious();
		    }
		}
		
	/*	dbCursor = localstoragehandler.get(friend.userName, IMService.USERNAME );
		
		if (dbCursor.getCount() > 0){
		
		int noOfScorer = 0;
		dbCursor.moveToFirst();
		    while ((!dbCursor.isAfterLast())&&noOfScorer<dbCursor.getCount()) 
		    {
		        noOfScorer++;
		    
					this.userappendToMessageHistory(dbCursor.getString(2) ,dbCursor.getString(3),dbCursor.getString(4),""+dbCursor.getString(4));
		        
				
					
			//		this.friendappendToMessageHistory(dbCursor.getString(2) , dbCursor.getString(3));
  
		      dbCursor.moveToNext();
		    }
		}
		localstoragehandler.close();
		*/
	//	if (msg != null) 
	//	{
			
			
	//		this.userappendToMessageHistory(friend.userName , msg,flag,msgid1);
	//		populateupdatedListView("CLEAR");
	//		updateview();
		
	//		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((friend.userName).hashCode());
	//	}
		
		sendMessageButton.setOnClickListener(new OnClickListener(){
			CharSequence message;
			
			Handler handler = new Handler();
			public void onClick(View arg0) {
				final SecretKeySpec sks=IMKeys.Gen();
				message = messageText.getText();
				if (message.length()>0 && message.length()<10023) 
				{	
				
				//	userappendToMessageHistory(imService.getUsername(), message.toString(),flag,msgid1);
				
					
						
			//	msgid=	localstoragehandler.insert(imService.getUsername(), friend.userName, message.toString(),"sent");
				//Log.w("Message ID","Ya jama3et el khair"+msgid);
			//	populateupdatedListView("CLEAR");	
			//	updateview();
					messageText.setText("");
					Thread thread = new Thread(){					
						public void run() {
							try {
								
								msgid=	localstoragehandler.groupinsert(imService.getUsername(), group.groupname, message.toString(),"sent",imService.getUsername());
								Log.w("Message ID","Ya jama3et el khair"+msgid);
								
								 runOnUiThread(new Runnable() {
			                            public void run() {
			                            	populateupdatedListView("CLEAR");	
			            					updateview();
			                            }
			                        });      
								//								if (imService.sendMessage(imService.getUsername(), friend.userName, IMKeys.Encrypt(sks,EncryptKey(message.toString())),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),""+msgid,"notseen") == null)

								if (imService.sendGroupMessage(imService.getUsername(), group.groupname, IMKeys.Encrypt(sks,message.toString()),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),"notseen") == null)
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
										
				
					
				
				
				
				} else if (message.length()>10023) {
					
					 AlertDialog alertDialog = new AlertDialog.Builder(GroupMessaging.this).create();

			            // Setting Dialog Title
			            alertDialog.setTitle("Alert Dialog");

			            // Setting Dialog Message
			            alertDialog.setMessage("Message is too Big Unable to Send "+message.toString().length()+" Character");

			            // Setting Icon to Dialog
			            

			            // Setting OK Button
			            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

			                        public void onClick(DialogInterface dialog,int which) 
			                        {
			                            // Write your code here to execute after dialog closed
			                        Toast.makeText(getApplicationContext(),"Please shorten the Message", Toast.LENGTH_SHORT).show();
			                        }
			                    });

			            // Showing Alert Message
			            alertDialog.show();
				}
				
			//	populateupdatedListView("CLEAR");	
			//	updateview();
				
				
			
				
			} 
			
		
		});
		

btnButton.setOnClickListener(new OnClickListener(){
			CharSequence message;
			
			Handler handler = new Handler();
			public void onClick(View arg0) {
				
				
				Intent i = new Intent(GroupMessaging.this, EmojiSelection.class);
			//	startActivity(i);
				startActivityForResult(i, 0);
				
			}					
					




});


		
		
		
		
	messageText.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) 
			{
				if (keyCode == 66){
					sendMessageButton.performClick();
					return true;
				}
				return false;
			}
			
			
		});
				
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		int message = -1;
		switch (id)
		{
		case MESSAGE_CANNOT_BE_SENT:
			message = R.string.message_cannot_be_sent;
		break;
		}
		
		if (message == -1)
		{
			return null;
		}
		else
		{
			return new AlertDialog.Builder(GroupMessaging.this)       
			.setMessage(message)
			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked OK so do some stuff */
				}
			})        
			.create();
		}
	}

	

	 
	@Override
	protected void onPause() {
		  if (adView != null) {
		      adView.pause();
		    }
		super.onPause();
		unregisterReceiver(messageReceiver);
		unbindService(mConnection);
		
		GroupController.setActiveGroup(null);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // TODO Auto-generated method stub
	    super.onActivityResult(requestCode, resultCode, data);

	    if (resultCode == 0) {
	        SharedPreferences s = getSharedPreferences("P", 0);
	        String m = s.getString("valye", "");
	        messageText.setText(messageText.getText().toString()+""+m);

	    }
	}

	@Override
	protected void onResume() 
	{		
		super.onResume();
		 if (adView != null) {
		      adView.resume();
		    }
		bindService(new Intent(GroupMessaging.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
				
		IntentFilter i = new IntentFilter();
		i.addAction(IMService.TAKE_GROUP_MESSAGE);
		
		registerReceiver(messageReceiver, i);
		
		GroupController.setActiveGroup(group.groupname);
		
		populateupdatedListView("CLEAR");
		updateview();
		
	
		
		
	}
	
	
	public class  MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{		
			Bundle extra = intent.getExtras();
			String username = extra.getString(GroupMessageInfo.USERID);			
			String message = extra.getString(GroupMessageInfo.MESSAGETEXT);
			String flag= extra.getString(GroupMessageInfo.SEENFLAG);
			String from= extra.getString(GroupMessageInfo.FROM);
			Log.i("messageid", "from xxxxxxxxxxxxxxxxxx"+from);
		//	String msgid=extra.getString(MessageInfo.MSGID);
	//		String key=extra.getString(MessageInfo.KEY);
		    Log.i("messageid", "Message id wloooooo"+msgid);
	        
			if (username != null && message != null && !message.equals("") )
			{
		/*					byte[]	keyBytes=key.getBytes();
							byte[]	decode = Base64.decode(keyBytes, Base64.DEFAULT);
							byte[] encryptedmessage=Base64.decode(message, Base64.DEFAULT);
							
							 byte[] clearmessage = null;
						        try {
						            Cipher c = Cipher.getInstance("AES");
						            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decode, "AES"));
						            clearmessage = c.doFinal(encryptedmessage);
						        } catch (Exception e) {
						            Log.e("Error", "AES decryption error");
						        }*/
				
				

				if (group.groupname.equals(username)) {
				
				//	appendToMessageHistory(username, message);
				//	localstoragehandler.insert(username,imService.getUsername(), message);
					
				//	userappendToMessageHistory(username, message,"",msgid);
					
					localstoragehandler.groupinsert(username,username, message,"",from);
					populateupdatedListView("CLEAR");
					updateview();
				}
				else {
					if (message.length() > 15) {
					//	message = message.substring(0, 15);
						message = message.substring(0, 15);
					}
					Toast.makeText(GroupMessaging.this, "Group ("+username+")::" +from + " says '"+
													message + "'",
													Toast.LENGTH_SHORT).show();		
				}
				
			} 
			
	
			
			
				

				
				
			
			
			
		}
		
	};
	private MessageReceiver messageReceiver = new MessageReceiver();
	
	public  void userappendToMessageHistory(String username, String message,String flag,String msgid,String datetime) {
		if (username != null && message != null) {
	//		usermessageHistoryText.append(username + ":\n");								
	//		usermessageHistoryText.append(message + "\n");
			ListedView.add(new MyMessageView(username+": \n"+message,flag,msgid,datetime));
			
			populateListView();
		}
	}
	
	
	
	
	public  void updateview() {
	
		
		dbCursor = localstoragehandler.getgroup(group.groupname, IMService.USERNAME );
		
		if (dbCursor.getCount() > 0){
		int noOfScorer = 0;
	//	dbCursor.is.moveToFirst();
		dbCursor.moveToLast();
	//	    while ((!dbCursor.isAfterLast())&&noOfScorer<dbCursor.getCount()) 
	    while ((!dbCursor.isBeforeFirst())&&noOfScorer<dbCursor.getCount())
		    {
		        noOfScorer++;
		    
					this.firedappendToMessageHistory(dbCursor.getString(1) ,dbCursor.getString(2),dbCursor.getString(3),dbCursor.getString(4),dbCursor.getString(5),dbCursor.getString(6));
					/*values.put("groupowner", owner);
			values.put("groupname", groupname);
			values.put("message", message);
			values.put(FLAG_FLAG, flag);
			values.put(TIME_TIME, getCurrDate()+":");*/
			
					
			//		this.friendappendToMessageHistory(dbCursor.getString(2) , dbCursor.getString(3));
  
		    //  dbCursor.moveToNext();
					dbCursor.moveToPrevious();
		    }
		}
		localstoragehandler.close();
	
	    
	   
		
		
		
				
		/*		Log.i("I'm inside","i====="+i);
				View v = list.getChildAt(i - 
			            list.getFirstVisiblePosition());
				
				TextView someText = (TextView) v.findViewById(R.id.item_txtreceived);
				someText.setText(flag);*/
				
				
				
				
				
			
			
			
			
		
	}
	
	
public  void updateallgroupview() {
	
		
		dbCursor = localstoragehandler.getgroupall(group.groupname, IMService.USERNAME );
		
		if (dbCursor.getCount() > 0){
		int noOfScorer = 0;
	//	dbCursor.is.moveToFirst();
		dbCursor.moveToLast();
	//	    while ((!dbCursor.isAfterLast())&&noOfScorer<dbCursor.getCount()) 
	    while ((!dbCursor.isBeforeFirst())&&noOfScorer<dbCursor.getCount())
		    {
		        noOfScorer++;
		    
					this.firedappendToMessageHistory(dbCursor.getString(1) ,dbCursor.getString(2),dbCursor.getString(3),dbCursor.getString(4),dbCursor.getString(5),dbCursor.getString(6));
					/*values.put("groupowner", owner);
			values.put("groupname", groupname);
			values.put("message", message);
			values.put(FLAG_FLAG, flag);
			values.put(TIME_TIME, getCurrDate()+":");*/
			
					
			//		this.friendappendToMessageHistory(dbCursor.getString(2) , dbCursor.getString(3));
  
		    //  dbCursor.moveToNext();
					dbCursor.moveToPrevious();
		    }
		}
		localstoragehandler.close();
	
	    
	   
		
		
		
				
		/*		Log.i("I'm inside","i====="+i);
				View v = list.getChildAt(i - 
			            list.getFirstVisiblePosition());
				
				TextView someText = (TextView) v.findViewById(R.id.item_txtreceived);
				someText.setText(flag);*/
				
				
				
				
				
			
			
			
			
		
	}
	
	
	private class MyListAdapter extends ArrayAdapter<MyMessageView> {
		public MyListAdapter() {
			super(GroupMessaging.this, R.layout.item_view, ListedView);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Make sure we have a view to work with (may have been given null)
			View itemView = convertView;
			if (itemView == null) {
				itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
			}
			
			// Find the car to work with.
			MyMessageView currentCar = ListedView.get(position);
			
			
			
			
			// Fill the view
			
			
			// Make:
			TextView message = (TextView) itemView.findViewById(R.id.item_txtMessageText);
			message.setText(currentCar.getmessagetxt());

			// Year:
			TextView recText = (TextView) itemView.findViewById(R.id.item_txtreceived);
			recText.setText(currentCar.getreceived());
			
			// Condition:
		
			return itemView;
		}				
	}
	
	private LinearLayout wrapper;
	
	private class MyUpdatedListAdapter extends ArrayAdapter<MyMessageView> {
		public MyUpdatedListAdapter() {
			super(GroupMessaging.this, R.layout.item_view, UpdateListedView);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Make sure we have a view to work with (may have been given null)
			View itemView = convertView;
			if (itemView == null) {
				itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
			}
			
			// Find the car to work with.
			MyMessageView currentCar = UpdateListedView.get(position);
			
			// Fill the view
			wrapper = (LinearLayout) itemView.findViewById(R.id.wrapper);
			
			String user=IMService.USERNAME+":";
			String result =currentCar.getmessagetxt().substring(0,currentCar.getmessagetxt().indexOf(": \n"));
			
			String key=map.get(result);
			if (key==null)
				key="";
	//		Log.e("Key valyes",key);
			String out=currentCar.getmessagetxt().substring(currentCar.getmessagetxt().indexOf(": \n")+3);
		//	Log.i("inside adaptor","user"+user+"result"+result);
			// Make:
			if (IMService.USERNAME.equals(result))
			{
			TextView message = (TextView) itemView.findViewById(R.id.item_txtMessageText);
			
		//	message.setText(getSmiledText(Messaging.this,currentCar.getmessagetxt()));
			message.setText(getSmiledText(GroupMessaging.this,currentCar.getmessagetxt()));
			message.setTextColor(Color.BLUE);
			message.setBackgroundResource(R.drawable.bubble_yellow);
			//wrapper.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			//itemView.setLayoutParams(wrapper);
			//message.setGravity(Gravity.LEFT);
			wrapper.setGravity(Gravity.LEFT);
			}else if (key.equals("1")){
			
				TextView message = (TextView) itemView.findViewById(R.id.item_txtMessageText);

									


				//	CharSequence spanned = Html.fromHtml("<img src ='"
				//		+ "a.png"
					//	+ "'/>");
	//			message.setText(getSmiledText(Messaging.this,currentCar.getmessagetxt()));
				//
				message.setText(getSmiledText(GroupMessaging.this,currentCar.getmessagetxt()));
			message.setTextColor(Color.BLACK);
			message.setBackgroundResource(R.drawable.bubble_green);
			wrapper.setGravity(Gravity.RIGHT);
		//	wrapper.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		//	itemView.setLayoutParams(wrapper);
			
			}else if (key.equals("2")){
			
				TextView message = (TextView) itemView.findViewById(R.id.item_txtMessageText);

									


				//	CharSequence spanned = Html.fromHtml("<img src ='"
				//		+ "a.png"
					//	+ "'/>");
	//			message.setText(getSmiledText(Messaging.this,currentCar.getmessagetxt()));
				//
				message.setText(getSmiledText(GroupMessaging.this,currentCar.getmessagetxt()));
			message.setTextColor(Color.MAGENTA);
			message.setBackgroundResource(R.drawable.bubble_green);
			wrapper.setGravity(Gravity.RIGHT);
		//	wrapper.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		//	itemView.setLayoutParams(wrapper);
			
			}else if (key.equals("3")){
			
				TextView message = (TextView) itemView.findViewById(R.id.item_txtMessageText);

									


				//	CharSequence spanned = Html.fromHtml("<img src ='"
				//		+ "a.png"
					//	+ "'/>");
	//			message.setText(getSmiledText(Messaging.this,currentCar.getmessagetxt()));
				//
				message.setText(getSmiledText(GroupMessaging.this,currentCar.getmessagetxt()));
			message.setTextColor(Color.RED);
			message.setBackgroundResource(R.drawable.bubble_green);
			wrapper.setGravity(Gravity.RIGHT);
		//	wrapper.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		//	itemView.setLayoutParams(wrapper);
			
			}
			else if (key.equals("4")){
				
				TextView message = (TextView) itemView.findViewById(R.id.item_txtMessageText);

									


				//	CharSequence spanned = Html.fromHtml("<img src ='"
				//		+ "a.png"
					//	+ "'/>");
	//			message.setText(getSmiledText(Messaging.this,currentCar.getmessagetxt()));
				//
				message.setText(getSmiledText(GroupMessaging.this,currentCar.getmessagetxt()));
			message.setTextColor(Color.DKGRAY);
			message.setBackgroundResource(R.drawable.bubble_green);
			wrapper.setGravity(Gravity.RIGHT);
		//	wrapper.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		//	itemView.setLayoutParams(wrapper);
			
			}else if (key.equals("5")){
			
				TextView message = (TextView) itemView.findViewById(R.id.item_txtMessageText);

									


				//	CharSequence spanned = Html.fromHtml("<img src ='"
				//		+ "a.png"
					//	+ "'/>");
	//			message.setText(getSmiledText(Messaging.this,currentCar.getmessagetxt()));
				//
				message.setText(getSmiledText(GroupMessaging.this,currentCar.getmessagetxt()));
			message.setTextColor(Color.WHITE);
			message.setBackgroundResource(R.drawable.bubble_green);
			wrapper.setGravity(Gravity.RIGHT);
		//	wrapper.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		//	itemView.setLayoutParams(wrapper);
			
			}
			else if (key.equals("6")){
				
				TextView message = (TextView) itemView.findViewById(R.id.item_txtMessageText);

									


				//	CharSequence spanned = Html.fromHtml("<img src ='"
				//		+ "a.png"
					//	+ "'/>");
	//			message.setText(getSmiledText(Messaging.this,currentCar.getmessagetxt()));
				//
				message.setText(getSmiledText(GroupMessaging.this,currentCar.getmessagetxt()));
			message.setTextColor(Color.YELLOW);
			message.setBackgroundResource(R.drawable.bubble_green);
			wrapper.setGravity(Gravity.RIGHT);
		//	wrapper.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		//	itemView.setLayoutParams(wrapper);
			
			}
else {
				
				TextView message = (TextView) itemView.findViewById(R.id.item_txtMessageText);

									


				//	CharSequence spanned = Html.fromHtml("<img src ='"
				//		+ "a.png"
					//	+ "'/>");
	//			message.setText(getSmiledText(Messaging.this,currentCar.getmessagetxt()));
				//
				message.setText(getSmiledText(GroupMessaging.this,currentCar.getmessagetxt()));
			message.setTextColor(Color.BLACK);
			message.setBackgroundResource(R.drawable.bubble_green);
			wrapper.setGravity(Gravity.RIGHT);
		//	wrapper.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		//	itemView.setLayoutParams(wrapper);
			
			}






				
			// Year:
			TextView recText = (TextView) itemView.findViewById(R.id.item_txtreceived);
			recText.setText(currentCar.getreceived());
			recText.setTextColor(Color.CYAN);
			recText.setVisibility(recText.VISIBLE);
			
			
			TextView timeText = (TextView) itemView.findViewById(R.id.item_time);
			timeText.setText(currentCar.getdatatime());
			
			// Condition:
		
			return itemView;
		}				
	}
	
	
	
	CharSequence cs;
	CustomEmojis emojis;
	int index;
	
/*@Override
	protected void onRestart() {
		
		super.onRestart();
		emojis = new CustomEmojis(this);

		SharedPreferences preferences = this.getSharedPreferences("pref",
				this.MODE_WORLD_READABLE);
		index = preferences.getInt("smiley", 0);
		System.out.println("smiley index is---> " + index);

		ImageGetter imageGetter = new ImageGetter() {

			@Override
			public Drawable getDrawable(String source) {
				Drawable d = getResources().getDrawable(emojis.images[index]);
				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
				return d;
			}
		};
		cs = Html.fromHtml(
				"<img src ='"
						+ getResources().getDrawable(emojis.images[index])
						+ "'/>", imageGetter, null);
		if (index==0)
		{
			cs=":)";
		}
		else if (index==1)
			cs=":(";
		else if (index==2)
			cs=":D";
		
*///		cs="<img src ='"+getResources().getDrawable(emojis.images[index])+"'/>";
	/*	CharSequence m=messageText.getText();
		messageText.setText(m+""+cs);
		int position = messageText.length();
		Editable editable = messageText.getText();
		Selection.setSelection(editable, position);
		
	}*/



	private void populateListView() {
		ArrayAdapter<MyMessageView> adapter = new MyListAdapter();
		ListView list = (ListView) findViewById(R.id.MessageView);
		list.getLastVisiblePosition();
		list.setAdapter(adapter);
		
	}
	
	public void populateupdatedListView(String clear) {
		
		
		ArrayAdapter<MyMessageView> adapter = new MyUpdatedListAdapter();
		ListView list = (ListView) findViewById(R.id.MessageView);
	if (!clear.equals("CLEAR"))	
	{
		list.setAdapter(adapter);
	}
	else
		{
		adapter.clear();
		
		list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		}
		
	}
	
	public  void firedappendToMessageHistory(String groupowner, String groupname,String message,String from,String flag,String datatime)  {
		if (groupowner != null && message != null) {
		//	friendmessageHistoryText.append(username + ":\n");								
UpdateListedView.add(new MyMessageView(from+": \n"+message,flag,"",datatime));
//UpdateListedView.add(new MyMessageView(message,flag,msgid));
		/*	/*values.put("groupowner", owner);
			values.put("groupname", groupname);
			values.put("message", message);
			values.put(FLAG_FLAG, flag);
			values.put(TIME_TIME, getCurrDate()+":");*/
			populateupdatedListView("NOTCLEAR");
			
		}
	}
	
	
	@Override
	protected void onDestroy() {
		  if (adView != null) {
		      adView.destroy();
		    }
	    super.onDestroy();
	    if (localstoragehandler != null) {
	    	localstoragehandler.close();
	    }
	    if (dbCursor != null) {
	    	dbCursor.close();
	    }
	    
	  
	}
	
	MenuItem removeme=null;
	MenuItem removegroup=null;
	MenuItem removemember=null;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);
		
		 menu.add(0, HISTORY_ID, 0, R.string.upload_up);
		 menu.add(0, UPlOADFILE_ID, 0, R.string.uploadfile_up);
		 menu.add(0, DELETE_ID, 0, "Delete Messages");
	removeme=	 menu.add(0, REMOVEME_ID, 0, "Exit Group");
	removegroup=	 menu.add(0, REMOVEGROUP_ID, 0, "Delete "+group.groupname+" Group");
//	removemember =	 menu.add(0, REMOVEMEMBER_ID, 0, "Remove "+group.groupname+" Members").setEnabled(false);
		 menu.add(0, GETMEMBER_ID, 0, "Get "+group.groupname+" Members");
		 
	if(group.groupowner.equals(user0))
	{
		removeme.setEnabled(false);
	}
	
	else
	{
		removegroup.setEnabled(false);
	}
		
	


		return result;
	}
	
	String output=new String();
	String res= new String();
	String out= new String();
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    
		switch(item.getItemId()) 
	    {
	    	case HISTORY_ID:
	    	{
	    /*		Intent i = new Intent(GroupMessaging.this, GroupMainActivity.class);
	    		i.putExtra(GroupInfo.GROUPNAME, group.groupname);
	    		i.putExtra(GroupMessageInfo.MYUSER, imService.getUsername());
	    		
	  		startActivity(i);*/
	    		populateupdatedListView("CLEAR");
				updateallgroupview();
	    		
	    		return true;
	    	}
	    	
	    	//UPlOADFILE_ID
	    	
	    	case UPlOADFILE_ID:
	    	{
	    		Intent i = new Intent(GroupMessaging.this, GroupUploadFile.class);
	    		i.putExtra(GroupInfo.GROUPNAME, group.groupname);
	    		i.putExtra(GroupMessageInfo.MYUSER, imService.getUsername());
	    		
	  		startActivity(i);
	    		return true;
	    	}
	    	
	    	case DELETE_ID:
	    	{
	    		localstoragehandler.deletegroupall(group.groupname, IMService.USERNAME);
	    		populateupdatedListView("CLEAR");
				updateview();
	    		return true;
	    	}
	    	
	    	case REMOVEME_ID:
			{
				
	//			String groupmemb=extras.getString(GroupInfo.GROUP_MEMEBERS_LIST);
		
				Thread thread = new Thread(){					
					public void run() {
						try {
							
					
							//								if (imService.sendMessage(imService.getUsername(), friend.userName, IMKeys.Encrypt(sks,EncryptKey(message.toString())),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),""+msgid,"notseen") == null)

							output=imService.removeme(group.groupname);
							
							if (output==null)
							{
								 runOnUiThread(new Runnable() {
			                            public void run() {
			    							Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

			                            }
			                        });     
							
							} else if (output.equals("1"))
							{
								
								 runOnUiThread(new Runnable() {
			                            public void run() {
			    							Toast.makeText(getApplicationContext(),"Successfully Removed from Group!", Toast.LENGTH_LONG).show();

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
			
	
				return true;
			}		
	    	
			
	    	
	    	case REMOVEGROUP_ID:
			{
				
	//			String groupmemb=extras.getString(GroupInfo.GROUP_MEMEBERS_LIST);
		
				Thread thread = new Thread(){					
					public void run() {
						try {
							
					
							//								if (imService.sendMessage(imService.getUsername(), friend.userName, IMKeys.Encrypt(sks,EncryptKey(message.toString())),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),""+msgid,"notseen") == null)

							res=imService.removegroup(group.groupname);
							
							if (res==null)
							{
								 runOnUiThread(new Runnable() {
			                            public void run() {
			    							Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

			                            }
			                        });     
							
							} else if (res.equals("1"))
							{
								
								 runOnUiThread(new Runnable() {
			                            public void run() {
			    							Toast.makeText(getApplicationContext(),"Group has been deleted!", Toast.LENGTH_LONG).show();

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
			
	
				return true;
			}		
	    	
			
	   // 	case REMOVEMEMBER_ID:
		//	{
				
	//			String groupmemb=extras.getString(GroupInfo.GROUP_MEMEBERS_LIST);
				
				
		/*		Intent i = new Intent(GroupMessaging.this,
						RemoveMembersSelectionList.class);
				i.putExtra(GroupInfo.GROUPNAME, group.groupname);
				i.putExtra(GroupInfo.GROUP_MEMEBERS_LIST,
						out);
				i.putExtra(GroupInfo.GROUPOWNER, group.groupowner);
				i.putExtra(GroupInfo.USER, user0);
				startActivity(i);
				
	
				return true;
			}		*/
	    	
	    	case GETMEMBER_ID:
			{
				
	//			String groupmemb=extras.getString(GroupInfo.GROUP_MEMEBERS_LIST);
		
				Thread thread = new Thread(){					
					public void run() {
						try {
							
					
							//								if (imService.sendMessage(imService.getUsername(), friend.userName, IMKeys.Encrypt(sks,EncryptKey(message.toString())),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),""+msgid,"notseen") == null)

							out=imService.getmembers(group.groupname);
							
							if (out==null)
							{
								 runOnUiThread(new Runnable() {
			                            public void run() {
			    							Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

			                            }
			                        });     
							
							} else 
							{
								
								 runOnUiThread(new Runnable() {
			                            public void run() {
			    							Toast.makeText(getApplicationContext(),"Members are available to add or remove!", Toast.LENGTH_LONG).show();
			    							
			    							Intent i = new Intent(GroupMessaging.this,
			    									RemoveMembersSelectionList.class);
			    							
			    							i.putExtra(GroupInfo.GROUPNAME, group.groupname);
			    							i.putExtra(GroupInfo.GROUP_MEMEBERS_LIST,
			    									out);
			    							i.putExtra(GroupInfo.GROUPOWNER, group.groupowner);
			    							i.putExtra(GroupInfo.USER, user0);
			    							startActivity(i);
			    							
			    						//	removemember.setEnabled(true);
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
			
	
				return true;
			}		
	    	
	    	
	    }
	       
	    return super.onMenuItemSelected(featureId, item);
	}

	

}
