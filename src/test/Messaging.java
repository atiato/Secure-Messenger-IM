/*package test;



import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Messaging extends Activity {
	
	    public void onCreate(Bundle savedInstanceState)
	    {
	        super.onCreate(savedInstanceState);
	        
	        String ecckey="This is just a test";
	        
	        TextView  tv = new TextView(this);
	   
	        
	        tv.setText( EncryptKey(ecckey) );
	        
	        TextView  out = new TextView(this);
	   
	        
	        out.setText( "\nclear txt"+DecryptKey(EncryptKey(ecckey)) );
	        
	        setContentView(tv);
	        setContentView(out);
	    }

	    public native String  EncryptKey(String ecckey);
	    public native String  DecryptKey(String ecckey);

	   
	    static {
	        System.loadLibrary("ecc");
	    }
	
}*/
