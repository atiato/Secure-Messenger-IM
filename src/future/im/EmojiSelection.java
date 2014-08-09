package future.im;



import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

public class EmojiSelection extends Activity {
	public static final String TAG = "EmojiSelection";
	//int position=0;
	GridView gridView;
	CustomEmojis customEmojis;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emojis);
		
		initUIElement();
		
		customEmojis = new CustomEmojis(this);
		
		gridView.setAdapter(customEmojis);
		
		
	
	//	 Toast.makeText(getBaseContext(), 
      //              "position" + (position), 
        //            Toast.LENGTH_SHORT).show();
		
		
		
		//dont forgot to commit preference
		
		
		
		
		
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position ,
					long arg3) {
				
				
				
				
	/*			Log.i(TAG, "U are in OnItemSelected");
				SharedPreferences preferences = EmojiSelection.this.getSharedPreferences("pref", MODE_WORLD_READABLE);
				SharedPreferences.Editor editor = preferences.edit();
				
				editor.putInt("smiley", position);
				System.out.println("Selected emojis ---> " + position);*/
			//	 Toast.makeText(getBaseContext(), 
		      //              "position" + (position), 
		        //            Toast.LENGTH_SHORT).show();
				
				
				
				//dont forgot to commit preference
			//	editor.commit();
				
				
				if (position==0)
					cs=":(yy0)";
				else if (position==1)
				{
					cs=":(yy1)";
				}
				else if (position==2)
					cs=":(yy2)";
				else if (position==3)
					cs=":(yy3)";
				else if (position==4)
					cs=":(yy4)";
				else if (position==5)
					cs=":(yy5)";
				else if (position==6	)	cs=":(xx1)"	;
				else if (position==7	)	cs=":(xx2)"	;
				else if (position==8	)	cs=":(xx3)"	;
				else if (position==9	)	cs=":(xx4)"	;
				else if (position==10	)	cs=":(xx5)"	;
				else if (position==11	)	cs=":(xx6)"	;
				else if (position==12	)	cs=":(xx7)"	;
				else if (position==13	)	cs=":(xx8)"	;
				else if (position==14	)	cs=":(xx9)"	;
				else if (position==15	)	cs=":(xx10)"	;
				else if (position==16	)	cs=":(xx11)"	;
				else if (position==17	)	cs=":(xx12)"	;
				else if (position==18	)	cs=":(xx13)"	;
				else if (position==19	)	cs=":(xx14)"	;
				else if (position==20	)	cs=":(xx15)"	;
				else if (position==21	)	cs=":(xx16)"	;
				else if (position==22	)	cs=":(xx17)"	;
				else if (position==23	)	cs=":(xx18)"	;
				else if (position==24	)	cs=":(xx19)"	;
				else if (position==25	)	cs=":(xx20)"	;
				else if (position==26	)	cs=":(xx21)"	;
				else if (position==27	)	cs=":(xx22)"	;
				else if (position==28	)	cs=":(xx23)"	;
				else if (position==29	)	cs=":(xx24)"	;
				else if (position==30	)	cs=":(xx25)"	;
				else if (position==31	)	cs=":(xx26)"	;
				else if (position==32	)	cs=":(xx27)"	;
				else if (position==33	)	cs=":(xx28)"	;
				else if (position==34	)	cs=":(xx29)"	;
				else if (position==35	)	cs=":(xx30)"	;
				else if (position==36	)	cs=":(xx31)"	;
				else if (position==37	)	cs=":(xx32)"	;
				else if (position==38	)	cs=":(xx33)"	;
				else if (position==39	)	cs=":(xx34)"	;
				else if (position==40	)	cs=":(xx35)"	;
				else if (position==41	)	cs=":(xx36)"	;
				else if (position==42	)	cs=":(xx37)"	;
				else if (position==43	)	cs=":(xx38)"	;
				else if (position==44	)	cs=":(xx39)"	;
				else if (position==45	)	cs=":(xx40)"	;
				else if (position==46	)	cs=":(xx41)"	;
				else if (position==47	)	cs=":(xx42)"	;
				else if (position==48	)	cs=":(xx43)"	;
				else if (position==49	)	cs=":(xx44)"	;
				else if (position==50	)	cs=":(xx45)"	;
				else if (position==51	)	cs=":(xx46)"	;
				else if (position==52	)	cs=":(xx47)"	;
				else if (position==53	)	cs=":(xx48)"	;
				else if (position==54	)	cs=":(xx49)"	;
				else if (position==55	)	cs=":(xx50)"	;
				else if (position==56	)	cs=":(xx51)"	;
				else if (position==57	)	cs=":(xx52)"	;
				else if (position==58	)	cs=":(xx53)"	;
				else if (position==59	)	cs=":(xx54)"	;
				else if (position==60	)	cs=":(xx55)"	;
				else if (position==61	)	cs=":(xx56)"	;
				else if (position==62	)	cs=":(xx57)"	;
				else if (position==63	)	cs=":(xx58)"	;
				else if (position==64	)	cs=":(xx59)"	;
				else if (position==65	)	cs=":(xx60)"	;
				else if (position==66	)	cs=":(xx61)"	;
				else if (position==67	)	cs=":(xx62)"	;
				else if (position==68	)	cs=":(xx63)"	;
				else if (position==69	)	cs=":(xx64)"	;
				else if (position==70	)	cs=":(xx65)"	;
				else if (position==71	)	cs=":(xx66)"	;
				else if (position==72	)	cs=":(xx67)"	;
				else if (position==73	)	cs=":(xx68)"	;
				else if (position==74	)	cs=":(xx69)"	;
				else if (position==75	)	cs=":(xx70)"	;
				else if (position==76	)	cs=":(xx71)"	;
				else if (position==77	)	cs=":(xx72)"	;
				else if (position==78	)	cs=":(xx73)"	;
				
				
				
				

				
				
//				cs="<img src ='"+getResources().getDrawable(emojis.images[index])+"'/>";
			//	EditText messageText=(EditText)findViewById(R.id.message);
				
				   SharedPreferences s = getSharedPreferences("P", 0);
		           Editor edt = s.edit();
		           edt.putString("valye", cs.toString());
		           cs="";
		           edt.commit();
		           finish();
				
				//finish();
				
			
			}
		});
	}
	
	private void initUIElement() {
		gridView = (GridView)findViewById(R.id.gridview1);
		
	}
	
	
	CharSequence cs;
	CustomEmojis emojis;
	int index=0;
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
		//super.onRestart();
//		emojis = new CustomEmojis(this);
	/*	SharedPreferences preferences = this.getSharedPreferences("pref",
				this.MODE_WORLD_READABLE);
		index = preferences.getInt("smiley", 0);
		System.out.println("smiley index is---> " + index);*/

	/*	ImageGetter imageGetter = new ImageGetter() {

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
						+ "'/>", imageGetter, null);*/
		
	
	
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        // your code
	    	  SharedPreferences s = getSharedPreferences("P", 0);
	           Editor edt = s.edit();
	           edt.putString("valye", "");
	           
	           edt.commit();
	           finish();
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}

	
}
