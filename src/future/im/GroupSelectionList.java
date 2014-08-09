package future.im;

import java.io.UnsupportedEncodingException;

import future.im.interfaces.IAppManager;
import future.im.services.IMService;
import future.im.types.FriendInfo;
import future.im.types.GroupInfo;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import future.im.R;


public class GroupSelectionList extends ListActivity {
	
	private static final int APPROVE_SELECTED_FRIENDS_ID = Menu.FIRST;
	 public static final int SELECTALL_ID = Menu.FIRST + 1;
	    public static final int UNSELECTALL_ID = Menu.FIRST + 2;
//	private static final int DISCARD_ID = 1;
	private String[] friendUsernames;
	String result;
	private IAppManager imService;
	String SelctedGroupmembers = new String(); // comma separated
	String username = new String(); // comma separated
	String groupname = new String();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		
		String names = extras.getString(GroupInfo.GROUP_MEMEBERS_LIST);
		
		friendUsernames = names.split(",");
		
		 username =extras.getString(GroupInfo.GROUPOWNER);
		 groupname=extras.getString(GroupInfo.GROUPNAME);
		
		setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, friendUsernames));			
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);	
		
		// canceling friend request notification
	//	NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	//	NM.cancel(R.string.new_friend_request_exist);					
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);		

		menu.add(0, APPROVE_SELECTED_FRIENDS_ID, 0, R.string.CreateGroup);
		menu.add(0, SELECTALL_ID, 0, "Check All");
		menu.add(0, UNSELECTALL_ID, 0, "UnCheck All");
		
		//UNSELECTALL_ID
		//SELECTALL_ID
		
		return result;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{		

		switch(item.getItemId()) 
		{	  
			case APPROVE_SELECTED_FRIENDS_ID:
			{
				int reqlength = getListAdapter().getCount();
				
				for (int i = 0; i < reqlength ; i++) 
				{
					if (getListView().isItemChecked(i)) {
						SelctedGroupmembers = SelctedGroupmembers.concat(friendUsernames[i]).concat(",");
					}
			//		else {
			//			discardedFriendNames = discardedFriendNames.concat(friendUsernames[i]).concat(",");						
			//		}					
				} 
				
				
				
				Thread thread = new Thread(){
					@Override
					public void run() {
						if ( SelctedGroupmembers.length() > 0 //|| 
							// discardedFriendNames.length() > 0 
							) 
						{
							try {
							result=	imService.CreateGroup(username,"password",SelctedGroupmembers, groupname,username);
						
							if (result.equals("5"))
						{
								runOnUiThread(new Runnable() {
									public void run() {	
								
								Toast.makeText(GroupSelectionList.this, "Group already exist so please select another name", Toast.LENGTH_SHORT).show();	
									}
									});
									
									}	else if (result.equals("1"))
						{
										
										runOnUiThread(new Runnable() {
											public void run() {	
										
							Toast.makeText(GroupSelectionList.this, "Group Creation Success", Toast.LENGTH_SHORT).show();	
											}
										});
						}else if (result.equals("0"))
						{
							runOnUiThread(new Runnable() {
								public void run() {	
								
							Toast.makeText(GroupSelectionList.this, "Unable to Create Group Please check connection", Toast.LENGTH_SHORT).show();	
								}
							});
							
						}	
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}											
					}
				};
				thread.start();

		//		Toast.makeText(GroupSelectionList.this, R.string.request_sent, Toast.LENGTH_SHORT).show();
			
						
				return true;
			}			
			
			case SELECTALL_ID:	
			{
				
				for ( int i=0; i< getListAdapter().getCount(); i++ ) {
			        getListView().setItemChecked(i, true);
																}
				return true;
			}
			
			case UNSELECTALL_ID:
			
			{
				
				for ( int i=0; i< getListAdapter().getCount(); i++ ) {
			        getListView().setItemChecked(i, false);
																}
				return true;
			}
			
		}

		return super.onMenuItemSelected(featureId, item);		
	}

	@Override
	protected void onPause() 
	{
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		bindService(new Intent(GroupSelectionList.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((IMService.IMBinder)service).getService();      

			
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(GroupSelectionList.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
}
