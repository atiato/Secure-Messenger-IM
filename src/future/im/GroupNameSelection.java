package future.im;

import java.io.UnsupportedEncodingException;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import future.im.interfaces.IAppManager;
import future.im.services.IMService;
import future.im.tools.GroupController;
import future.im.types.GroupInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import future.im.R;

public class GroupNameSelection extends Activity {

	private EditText groupnameText;
	private Button cancelButton;
	private AdView adView;

	  /* Your ad unit id. Replace with your actual ad unit id. */
	  private static final String AD_UNIT_ID = "a1532dfd78cf267";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Start and bind the imService
		 */


		setContentView(R.layout.group_name);
		setTitle("Group Name");

		Button NextButton = (Button) findViewById(R.id.next);
		cancelButton = (Button) findViewById(R.id.cancel_groupname);
		groupnameText = (EditText) findViewById(R.id.groupname);
		
	      adView = new AdView(this);
		    adView.setAdSize(AdSize.BANNER);
		    adView.setAdUnitId(AD_UNIT_ID);

		    // Add the AdView to the view hierarchy. The view will have no size
		    // until the ad is loaded.
		    LinearLayout layout = (LinearLayout) findViewById(R.id.gslinearLayout);
		    layout.addView(adView);

		    // Create an ad request. Check logcat output for the hashed device ID to
		    // get test ads on a physical device.
		    AdRequest adRequest = new AdRequest.Builder()
		      //  .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		     //   .addTestDevice("JZAYIVGEUG45EA9L")
		        .build();

		    adView.loadAd(adRequest);

		NextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {

				if (groupnameText.length() > 0) {

					
					
				
					
							Bundle extras = getIntent().getExtras();

							String groupmemberslist = extras
									.getString(GroupInfo.GROUP_MEMEBERS_LIST);

							String username = extras
									.getString(GroupInfo.GROUPOWNER);
							Intent i = new Intent(GroupNameSelection.this,
									GroupSelectionList.class);
							i.putExtra(GroupInfo.GROUPNAME, groupnameText.getText().toString());
							i.putExtra(GroupInfo.GROUP_MEMEBERS_LIST,
									groupmemberslist);
							i.putExtra(GroupInfo.GROUPOWNER, username);
							startActivity(i);
							

			

				}
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				finish();

			}

		});

	}
	
	 @Override
	  public void onDestroy() {
	    // Destroy the AdView.
	    if (adView != null) {
	      adView.destroy();
	    }
	    super.onDestroy();
	  }
	
	protected void onPause() {
		  if (adView != null) {
		      adView.pause();
		    }
		super.onPause();
		
	}

}