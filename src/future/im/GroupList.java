package future.im;

import java.io.UnsupportedEncodingException;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import future.im.interfaces.IAppManager;
import future.im.services.IMService;
import future.im.tools.FriendController;
import future.im.tools.GroupController;
import future.im.tools.UploadController;
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
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
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


public class GroupList extends ListActivity 
{
	
	private static final int CREATE_GROUP = Menu.FIRST;
	private static final int CREATE_BROADCAST = Menu.FIRST+1;
	private static final int GETUSERS_BROADCAST = Menu.FIRST+2;
	private AdView adView;

	  /* Your ad unit id. Replace with your actual ad unit id. */
	  private static final String AD_UNIT_ID = "a1532dfd78cf267";
	
	private IAppManager imService = null;
	private GroupListAdapter GroupAdapter;
	String groupmemba = null;
	
	public String ownusername = new String();
	public String groupmemberslist=new String();

	private class GroupListAdapter extends BaseAdapter 
	{		
		class ViewHolder {
			TextView text;
			ImageView icon;
		}
		private LayoutInflater mInflater;
		private Bitmap mOnlineIcon;
		private Bitmap mOfflineIcon;		

		private GroupInfo[] groups = null;


		public GroupListAdapter(Context context) {
			super();			

			mInflater = LayoutInflater.from(context);

			mOnlineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.group_icon);
			mOfflineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.redstar);

		}

		public void setGroupList(GroupInfo[] groups)
		{
			this.groups = groups;
		}


		public int getCount() {		

			return groups.length;
		}
		

		public GroupInfo getItem(int position) {			

			return groups[position];
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
				convertView = mInflater.inflate(R.layout.group_list_screen, null);

				// Creates a ViewHolder and store references to the two children views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.grouptext);
				holder.icon = (ImageView) convertView.findViewById(R.id.groupicon);                                       

				convertView.setTag(holder);
			}   
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.
			holder.text.setText("  "+groups[position].groupname+"  Owner : "+groups[position].groupowner);
			holder.icon.setImageBitmap(mOnlineIcon);

			return convertView;
		}

	}

	public class MessageReceiver extends  BroadcastReceiver  {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.i("Broadcast receiver ", "received a group message");
			Bundle extra = intent.getExtras();
			if (extra != null)
			{
				String action = intent.getAction();
				if (action.equals(IMService.GROUP_LIST_UPDATED))
				{
					// taking friend List from broadcast
					//String rawFriendList = extra.getString(FriendInfo.FRIEND_LIST);
					//FriendList.this.parseFriendInfo(rawFriendList);
					GroupList.this.updateData(GroupController.getGroupsInfo()
												);
					
				}
			}
		}

	};
	public MessageReceiver messageReceiver = new MessageReceiver();

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((IMService.IMBinder)service).getService();      
			
			GroupInfo[] groups = GroupController.getGroupsInfo(); //imService.getLastRawFriendList();
			if (groups != null) {    			
				GroupList.this.updateData(groups); // parseFriendInfo(friendList);
			}    
			Log.w("","CHeck user name"+imService.getUsername()+"groups"+groups);
			setTitle(imService.getUsername() + "'s Group list");
			ownusername = imService.getUsername();
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(GroupList.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
	


	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);

        setContentView(R.layout.glist_screen);
        
		GroupAdapter = new GroupListAdapter(this);
		
		 adView = new AdView(this);
		    adView.setAdSize(AdSize.BANNER);
		    adView.setAdUnitId(AD_UNIT_ID);

		    // Add the AdView to the view hierarchy. The view will have no size
		    // until the ad is loaded.
		    LinearLayout layout = (LinearLayout) findViewById(R.id.glinearLayout);
		    layout.addView(adView);

		    // Create an ad request. Check logcat output for the hashed device ID to
		    // get test ads on a physical device.
		    AdRequest adRequest = new AdRequest.Builder()
		      //  .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		     //   .addTestDevice("JZAYIVGEUG45EA9L")
		        .build();

		    adView.loadAd(adRequest);


	}
	public void updateData(GroupInfo[] groups)
	{
		if (groups != null) {
			GroupAdapter.setGroupList(groups);	
			setListAdapter(GroupAdapter);				
		}
	/*	if (groups != null) {
			String test = new String();
			for (int j = 0; j < groups.length; j++) {
				test = test.concat(groups[j].groupname).concat(",");			
			}	
			groupmemberslist=new String (test);
		}*/	
		
		
		
		

	}
	
	
	


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);		

		Intent i = new Intent(this, GroupMessaging.class);
		GroupInfo group = GroupAdapter.getItem(position);
		Bundle extras = getIntent().getExtras();
		String groupmemb=extras.getString(GroupInfo.GROUP_MEMEBERS_LIST);
		String username=extras.getString(GroupInfo.GROUPOWNER);
		i.putExtra(GroupInfo.USER, username);
		i.putExtra(GroupInfo.GROUP_MEMEBERS_LIST, groupmemb);
		i.putExtra(GroupInfo.GROUPNAME, group.groupname);
		i.putExtra(GroupInfo.GROUPOWNER, group.groupowner);
		
		
	//	i.putExtra(FriendInfo.PORT, friend.port);
	//	i.putExtra(FriendInfo.IP, friend.ip);		
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
		
		Intent x=new Intent(GroupList.this, IMService.class);
		bindService(x, mConnection , Context.BIND_AUTO_CREATE);
	//	startService(x);
		IntentFilter i = new IntentFilter();
		//i.addAction(IMService.TAKE_MESSAGE);	
		i.addAction(IMService.GROUP_LIST_UPDATED);

		registerReceiver(messageReceiver, i);			
		
		 if (adView != null) {
		      adView.resume();
		    }
	}
	
	MenuItem me=null;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);		

		
		menu.add(0, CREATE_GROUP, 0, "New Group");
	//	me=menu.add(0, CREATE_BROADCAST, 0, "New Broadcast").setEnabled(false);
		menu.add(0, GETUSERS_BROADCAST, 0, "New BroadCast").setEnabled(true);	
		
		
		
		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{		

		switch(item.getItemId()) 
		{	  
		
			
			case CREATE_GROUP:
			{
				Intent i = new Intent(GroupList.this, GroupNameSelection.class);
				Bundle extras = getIntent().getExtras();
				String groupmemb=extras.getString(GroupInfo.GROUP_MEMEBERS_LIST);
			
				i.putExtra(GroupInfo.GROUP_MEMEBERS_LIST, groupmemb);
				i.putExtra(GroupInfo.GROUPOWNER, imService.getUsername());
				startActivity(i);
				return true;
			}		
			
/*			case CREATE_BROADCAST:
			{
				Intent i = new Intent(GroupList.this, GroupNameSelection.class);
				Bundle extras = getIntent().getExtras();
		
				
			
				i.putExtra(GroupInfo.GROUP_MEMEBERS_LIST, groupmemba);
				i.putExtra(GroupInfo.GROUPOWNER, imService.getUsername());
				startActivity(i);
				return true;
			}	*/	
			
			
			case GETUSERS_BROADCAST:
			{
				
	//			String groupmemb=extras.getString(GroupInfo.GROUP_MEMEBERS_LIST);
		
				Thread thread = new Thread(){					
					public void run() {
						try {
							
					
							//								if (imService.sendMessage(imService.getUsername(), friend.userName, IMKeys.Encrypt(sks,EncryptKey(message.toString())),EncryptKey(Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT)),""+msgid,"notseen") == null)

							groupmemba=imService.createBroadCast();
							
							if (groupmemba==null)
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
			                            	Intent i = new Intent(GroupList.this, GroupNameSelection.class);
			                				Bundle extras = getIntent().getExtras();
			                	//			String groupmemb=extras.getString(GroupInfo.GROUP_MEMEBERS_LIST);
			                		
			                				
			                			
			                				i.putExtra(GroupInfo.GROUP_MEMEBERS_LIST, groupmemba);
			                				i.putExtra(GroupInfo.GROUPOWNER, imService.getUsername());
			                				startActivity(i);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
	
		
		
	}
}
