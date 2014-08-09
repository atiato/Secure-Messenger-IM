package future.im;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import future.im.types.MessageInfo;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;
  
public class DownloadImageActivity extends Activity {
	
	ImageView img;
	 private ProgressDialog pDialog;
	 int count;
	 
	    // Progress dialog type (0 - for Horizontal progress bar)
	    public static final int progress_bar_type = 0; 
	 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	
    	    
    	 
        super.onCreate(savedInstanceState);
 
        setContentView(R.layout.main_download);
        
        final Bundle extras = this.getIntent().getExtras();
        String httptext=extras.getString(MessageInfo.HTTPTEXT);
        int index=httptext.indexOf("\nPlease Click to Download:\n");
        final String httpurl=httptext.substring(index+26);
        Log.i("URL:::",""+httpurl);
         
 
			
       img = (ImageView) findViewById(R.id.img);
        
    
      
                	
       new HttpAsyncTask(DownloadImageActivity.this).execute(httpurl);
       
       img.setOnClickListener(new OnClickListener(){
			CharSequence message;
			
			Handler handler = new Handler();
			public void onClick(View arg0) {
				
				Drawable d = img.getDrawable();
				Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);
				d.draw(canvas);
				new SaveImageAsync(DownloadImageActivity.this, bitmap,httpurl).execute();
			
			}					
					




}); 
       
  		 
    
      
    }
    
 /*  private InputStream OpenHttpConnection(String urlString)
    throws IOException
    {
        InputStream in = null;
        int response = -1;
                 
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
                   
        if (!(conn instanceof HttpURLConnection))                    
            throw new IOException("Not an HTTP connection");
          
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            int lenghtOfFile = httpConn.getContentLength();
            byte data[] = new byte[1024];
            long total = 0;
  
            response = httpConn.getResponseCode();                
            if (response == HttpURLConnection.HTTP_OK) {
            	System.out.print("response           bbbbbbbbbbbbb"+response);
                in = httpConn.getInputStream();  
                while ((count = in.read(data)) != -1) {
                    total += count;
                    
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
     
                   
                   
                }
            }                    
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();           
        }
        return in;    
    }*/
/*    private Bitmap DownloadImage(String URL)
    {       
        Bitmap bitmap = null;
        InputStream in = null;       
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e1) {
           
            e1.printStackTrace();
        }
        return bitmap;               
    }*/
    
  
    
    private  class HttpAsyncTask extends AsyncTask<String, String, Bitmap> {
    	 private Context mContext;
    	 private ProgressDialog mProgressDialog;
    	 
    	 public HttpAsyncTask(Context context) {
             mContext = context;
            
         }
    	@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
		//	return DownloadImage(params[0]);
    		
    	    InputStream in = null;
            int response = -1;
            Bitmap bitmap=null;
                     
            URL url = null;
			try {
				url = new URL(params[0]);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            URLConnection conn = null;
			try {
				conn = url.openConnection();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
                       
            if (!(conn instanceof HttpURLConnection))
				try {
					throw new IOException("Not an HTTP connection");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              
            try{
                HttpURLConnection httpConn = (HttpURLConnection) conn;
                httpConn.setAllowUserInteraction(false);
                httpConn.setInstanceFollowRedirects(true);
                httpConn.setRequestMethod("GET");
                httpConn.connect();
            	
                int lenghtOfFile = conn.getContentLength();
                byte data[] = new byte[1024];
                long total = 0;
                InputStream input = null;
                ByteArrayInputStream newStream = null;
                response = httpConn.getResponseCode();                
                if (response == HttpURLConnection.HTTP_OK) {
                	System.out.print("response           bbbbbbbbbbbbb"+response);
               //     in = httpConn.getInputStream();  
                	
                	//input = new BufferedInputStream(url.openStream(), 8192);
                	ByteArrayOutputStream into = new ByteArrayOutputStream();
                	byte[] buf = new byte[4096];
                	
                	input = httpConn.getInputStream();
                	
               // 	bitmap = BitmapFactory.decodeStream(input);
                   while ((count = input.read(data)) != -1) {
                        total += count;
                        into.write(data, 0, count);
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress(""+(int)((total*100)/lenghtOfFile));
                        
                        // writing data to file
                       
                    }
                   into.close();
                   byte[] data1 = into.toByteArray();
                    newStream = new ByteArrayInputStream(data1);
                   
                }   
                BitmapFactory.Options options = new BitmapFactory.Options();
                // will results in a much smaller image than the original
                options.inSampleSize = 8;
                
                 bitmap = BitmapFactory.decodeStream(newStream,null,options);
                 System.out.print("bitmap          bbbbbbbbbbbbb"+bitmap);
                input.close();
                
            }
            catch (final IOException ex)
            {
            	mProgressDialog.dismiss();
            	ex.printStackTrace();    
             	 runOnUiThread(new Runnable() {
                     public void run() {
                       //  img.setText("Got Exception : see logcat "+ex.getMessage());
                         Toast.makeText(DownloadImageActivity.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                     }
                 });
                 Log.w("Download file from server Exception", "Exception : "  + ex.getMessage(), ex);  
            return null;
            }
          //  return in;  
            return bitmap;
		}
    	
    /*	  @Override
    	    protected void onPreExecute() {
    	        super.onPreExecute();
    	        showDialog(progress_bar_type);
    	    }*/
    	
    	 @Override
         protected void onPreExecute() {
             super.onPreExecute();
              mProgressDialog = new ProgressDialog(mContext);
             mProgressDialog.setMessage("Downloading");
             mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             mProgressDialog.setIndeterminate(true);
             mProgressDialog.setCancelable(false);
             mProgressDialog.show();
         }
    	 
	
        // onPostExecute displays the results of the AsyncTask.
        protected void onPostExecute(Bitmap bitmap) {
       //	dismissDialog(progress_bar_type);
            mProgressDialog.dismiss();
            mProgressDialog = null;
            img.setVisibility(View.VISIBLE);
            if (bitmap!=null)
            {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            System.out.println("ya jama3et el khair"+bitmap);
            img.setImageBitmap(bitmap);
            }else 
            {
            	
                Toast.makeText(getBaseContext(), "Make Sure you have connection!", Toast.LENGTH_LONG).show();

            }
            
       }
        
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }
        
        
		
    }
    
    /**
     * Showing Dialog
     * */
 /*   @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case progress_bar_type:
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Downloading file. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(true);
            pDialog.show();
            return pDialog;
        default:
            return null;
        }
    }*/
    
    
    public class SaveImageAsync extends AsyncTask<Void, String, Void> {

        private Context mContext;
        private Bitmap bitmaps;
        private String httpurls;

        private ProgressDialog mProgressDialog;

        public SaveImageAsync(Context context, Bitmap bitmap ,String httpurl) {
            mContext = context;
            bitmaps = bitmap;
            httpurls=httpurl;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Saving Image to SD Card");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... filePath) {
            try {
                Bitmap bitmap =bitmaps;//; BitmapFactory.decodeResource(mContext.getResources(), imageResourceID);
                final File sourceFile = new File(httpurls);
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(); 
                bitmap.compress(CompressFormat.JPEG, 100, byteOutputStream); 
                byte[] mbitmapdata = byteOutputStream.toByteArray();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(mbitmapdata);

                String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                String fileName = sourceFile.getName();
                createDirIfNotExists("Future_IM");
                OutputStream outputStream = new FileOutputStream(baseDir + File.separator+"Future_IM"+File.separator + fileName);
                byteOutputStream.writeTo(outputStream);

                byte[] buffer = new byte[128]; //Use 1024 for better performance
                int lenghtOfFile = mbitmapdata.length;
                int totalWritten = 0;
                int bufferedBytes = 0;

                while ((bufferedBytes = inputStream.read(buffer)) > 0) {
                    totalWritten += bufferedBytes;
                    publishProgress(Integer.toString((int) ((totalWritten * 100) / lenghtOfFile)));
                    outputStream.write(buffer, 0, bufferedBytes);
                }

            } catch (IOException e) { e.printStackTrace(); }
            
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
          
            return null;

        }

        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(Void filename) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        
        public boolean createDirIfNotExists(String path) {
            boolean ret = true;

            File file = new File(Environment.getExternalStorageDirectory(), path);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e("TravellerLog :: ", "Problem creating Image folder");
                    ret = false;
                }
            }
            return ret;
        }
        
        
    }
    
  

}