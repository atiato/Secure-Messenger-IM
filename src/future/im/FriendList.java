package future.im;

import java.io.UnsupportedEncodingException;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import future.im.interfaces.IAppManager;
import future.im.services.IMService;
import future.im.tools.FriendController;
import future.im.tools.GroupController;
import future.im.tools.GroupMessageController;
import future.im.tools.LocalStorageHandler;
import future.im.types.FriendInfo;
import future.im.types.GroupInfo;
import future.im.types.STATUS;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import future.im.R;


public class FriendList extends ListActivity 
{
	private static final int ADD_NEW_FRIEND_ID = Menu.FIRST;
	private static final int EXIT_APP_ID = Menu.FIRST + 3;
	private static final int REMOVE_SMS=Menu.FIRST + 2;
	private static final int CREATE_GROUP = Menu.FIRST + 1;
	private AdView adView;

	  /* Your ad unit id. Replace with your actual ad unit id. */
	  private static final String AD_UNIT_ID = "a1532dfd78cf267";
	
	private IAppManager imService = null;
	private FriendListAdapter friendAdapter;
	LocalStorageHandler test= new LocalStorageHandler(this);
	
	public String ownusername = new String();
	public String groupmemberslist=new String();

	private class FriendListAdapter extends BaseAdapter 
	{		
		class ViewHolder {
			TextView text;
			ImageView icon;
			TextView unreadsms;
		}
		private LayoutInflater mInflater;
		private Bitmap mOnlineIcon;
		private Bitmap mOfflineIcon;		

		private FriendInfo[] friends = null;


		public FriendListAdapter(Context context) {
			super();			

			mInflater = LayoutInflater.from(context);

			mOnlineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.greenstar);
			mOfflineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.redstar);

		}

		public void setFriendList(FriendInfo[] friends)
		{
			this.friends = friends;
		}


		public int getCount() {		

			return friends.length;
		}
		

		public FriendInfo getItem(int position) {			

			return friends[position];
		}

		public long getItemId(int position) {

			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;

			// When convertView is not null, we can reuse it directly, there is no need
			// to reinflate it. We only inflate a new View when the convertView supplied
			// by ListView is null.
			if (convertView == null) 
			{
				convertView = mInflater.inflate(R.layout.friend_list_screen, null);

				// Creates a ViewHolder and store references to the two children views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon); 
				holder.unreadsms=(TextView) convertView.findViewById(R.id.unreadsms);
			

				convertView.setTag(holder);
			}   
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
			int i=test.getcount(friends[position].userName,imService.getUsername() );
			// Bind the data efficiently with the holder.
			if (i>0)
			{
				holder.unreadsms.setTypeface(null,Typeface.BOLD);
				holder.unreadsms.setText("You have "+ i +" Unread Messages");
			}
			
			
			holder.text.setText(friends[position].userName);
			holder.icon.setImageBitmap(friends[position].status == STATUS.ONLINE ? mOnlineIcon : mOfflineIcon);

			return convertView;
		}

	}

	public class MessageReceiver extends  BroadcastReceiver  {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.i("Broadcast receiver ", "received a message");
			Bundle extra = intent.getExtras();
			if (extra != null)
			{
				String action = intent.getAction();
				if (action.equals(IMService.FRIEND_LIST_UPDATED))
				{
					// taking friend List from broadcast
					//String rawFriendList = extra.getString(FriendInfo.FRIEND_LIST);
					//FriendList.this.parseFriendInfo(rawFriendList);
					FriendList.this.updateData(FriendController.getFriendsInfo(), 
												FriendController.getUnapprovedFriendsInfo());
					
				}
				
			
			}
		}

	};
	public MessageReceiver messageReceiver = new MessageReceiver();

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((IMService.IMBinder)service).getService();      
			
			FriendInfo[] friends = FriendController.getFriendsInfo(); //imService.getLastRawFriendList();
			if (friends != null) {    			
				FriendList.this.updateData(friends, null); // parseFriendInfo(friendList);
			}    
			Log.w("","CHeck user name"+imService.getUsername()+"friends"+friends);
			setTitle(imService.getUsername() + "'s friend list");
			ownusername = imService.getUsername();
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(FriendList.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
	


	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);

        setContentView(R.layout.list_screen);
        
		friendAdapter = new FriendListAdapter(this);
		
		  adView = new AdView(this);
		    adView.setAdSize(AdSize.BANNER);
		    adView.setAdUnitId(AD_UNIT_ID);

		    // Add the AdView to the view hierarchy. The view will have no size
		    // until the ad is loaded.
		    LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
		    layout.addView(adView);

		    // Create an ad request. Check logcat output for the hashed device ID to
		    // get test ads on a physical device.
		    AdRequest adRequest = new AdRequest.Builder()
		      //  .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		     //   .addTestDevice("JZAYIVGEUG45EA9L")
		        .build();

		    adView.loadAd(adRequest);

	}
	public void updateData(FriendInfo[] friends, FriendInfo[] unApprovedFriends)
	{
		if (friends != null) {
			friendAdapter.setFriendList(friends);	
			setListAdapter(friendAdapter);				
		}
		if (friends != null) {
			String test = new String();
			for (int j = 0; j < friends.length; j++) {
				test = test.concat(friends[j].userName).concat(",");			
			}	
			groupmemberslist=new String (test);
			GroupMessageController.setmemberlist(groupmemberslist);
		}	
		
		
		if (unApprovedFriends != null) 
		{
			NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			if (unApprovedFriends.length > 0)
			{					
				String tmp = new String();
				for (int j = 0; j < unApprovedFriends.length; j++) {
					tmp = tmp.concat(unApprovedFriends[j].userName).concat(",");			
				}
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
		    	.setSmallIcon(R.drawable.stat_sample)
		    	.setContentTitle(getText(R.string.new_friend_request_exist));
				/*Notification notification = new Notification(R.drawable.stat_sample, 
						getText(R.string.new_friend_request_exist),
						System.currentTimeMillis());*/

				Intent i = new Intent(this, UnApprovedFriendList.class);
				i.putExtra(FriendInfo.FRIEND_LIST, tmp);				

				PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
						i, 0);

				mBuilder.setContentText("You have new friend request(s)");
				/*notification.setLatestEventInfo(this, getText(R.string.new_friend_request_exist),
												"You have new friend request(s)", 
												contentIntent);*/
				
				mBuilder.setContentIntent(contentIntent);

				
				NM.notify(R.string.new_friend_request_exist, mBuilder.build());			
			}
			else
			{
				// if any request exists, then cancel it
				NM.cancel(R.string.new_friend_request_exist);			
			}
		}

	}
	
	
	


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);		

		Intent i = new Intent(this, Messaging.class);
		FriendInfo friend = friendAdapter.getItem(position);
		i.putExtra(FriendInfo.USERNAME, friend.userName);
		i.putExtra(FriendInfo.PORT, friend.port);
		i.putExtra(FriendInfo.IP, friend.ip);		
		startActivity(i);
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
	protected void onPause() 
	{
		  if (adView != null) {
		      adView.pause();
		    }
		super.onPause();
//		moveTaskToBack(true);
		unregisterReceiver(messageReceiver);		
		unbindService(mConnection);
		
		if (test != null) {
	    	test.close();
	    }
		
		
	}
	
/*	@Override
	public void onBackPressed() {
	    Log.d("CDA", "onBackPressed Called");
	    Intent intent = new Intent();
	    intent.setAction(Intent.ACTION_MAIN);
	    intent.addCategory(Intent.CATEGORY_HOME);

	    startActivity(intent);
	}*/
	
	/*public boolean onKeyDown(int keyCode, KeyEvent event)  
	{

	     if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
	     {

	        this.moveTaskToBack(true);
	           return true;
	      }

	    return super.onKeyDown(keyCode, event);
	}*/

	@Override
	protected void onResume() 
	{
			
		super.onResume();
		
		Intent x=new Intent(FriendList.this, IMService.class);
		bindService(x, mConnection , Context.BIND_AUTO_CREATE);
	//	startService(x);
		IntentFilter i = new IntentFilter();
		//i.addAction(IMService.TAKE_MESSAGE);	
		i.addAction(IMService.FRIEND_LIST_UPDATED);
		test = new LocalStorageHandler(this);

		registerReceiver(messageReceiver, i);	
		  if (adView != null) {
		      adView.resume();
		    }
		

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);		

		menu.add(0, ADD_NEW_FRIEND_ID, 0, R.string.add_new_friend);
		menu.add(0, CREATE_GROUP, 0, "Group list");
		menu.add(0, REMOVE_SMS, 0, "Delete Remote Messages");	
		
		menu.add(0, EXIT_APP_ID, 0, "Sign Out");		
		
		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{		

		switch(item.getItemId()) 
		{	  
			case ADD_NEW_FRIEND_ID:
			{
				Intent i = new Intent(FriendList.this, AddFriend.class);
				startActivity(i);
				return true;
			}
			
			case CREATE_GROUP:
			{
				Intent i = new Intent(FriendList.this, GroupList.class);
				i.putExtra(GroupInfo.GROUP_MEMEBERS_LIST, groupmemberslist);
				i.putExtra(GroupInfo.GROUPOWNER, imService.getUsername());
				startActivity(i);
				return true;
			}		
			
			case REMOVE_SMS:
			{
				String result;
				
				Thread loginThread = new Thread(){
					private Handler handler = new Handler();
					@Override
					public void run() {
						String result = null;
						try {
							
							result = imService.deletemessages();
						} catch (UnsupportedEncodingException e) {
							
							e.printStackTrace();
						}
						if (result == null) 
						{
							/*
							 * Authenticatin failed, inform the user
							 */
							handler.post(new Runnable(){
								public void run() {	
									Toast.makeText(getApplicationContext(),"Problem while deleting", Toast.LENGTH_LONG).show();

									//showDialog(MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT);
								}									
							});
													
						}else if (result.equals("1"))
						
						{
							handler.post(new Runnable(){
								public void run() {	
									Toast.makeText(getApplicationContext(),"Your Messages have been Successfully Deleted from Server -:)", Toast.LENGTH_LONG).show();

									//showDialog(MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT);
								}									
							});
							
						}
							
							
							
							else {
						
							/*
							 * if result not equal to authentication failed,
							 * result is equal to friend list of the user
							 */		
								handler.post(new Runnable(){
									public void run() {	
										Toast.makeText(getApplicationContext(),"Unknown Error", Toast.LENGTH_LONG).show();

										//showDialog(MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT);
									}									
								});
							
						}
						
					}
				};
				loginThread.start();
			
				
				
				
				return true;
			}		
			
			case EXIT_APP_ID:
			{
				imService.exit();
				finish();
				return true;
			}			
		}

		return super.onMenuItemSelected(featureId, item);		
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
	
		
		
	}
}
