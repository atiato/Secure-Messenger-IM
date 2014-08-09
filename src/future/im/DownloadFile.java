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
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.alexbbb.uploadservice.FileIntentService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import future.im.services.IMService;
import future.im.tools.FriendController;
import future.im.tools.GroupController;
import future.im.types.MessageInfo;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadFile extends Activity {

	TextView img;
	Button GetDownload;
	EditText zebra;
	private ProgressDialog pDialog;
	int count;
	String httpurls;
	ProgressDialog pd=null;
	
	private AdView adView;

	  /* Your ad unit id. Replace with your actual ad unit id. */
	  private static final String AD_UNIT_ID = "a1532dfd78cf267";

	// Progress dialog type (0 - for Horizontal progress bar)
	public static final int progress_bar_type = 0;

	public native byte[] DecryptBinFile(byte[] message);

	static {
		System.loadLibrary("ecc");
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.download_file);

		final Bundle extras = this.getIntent().getExtras();
		String httptext = extras.getString(MessageInfo.HTTPTEXT);
		int index = httptext
				.indexOf("\nPlease Click link to Download the File:\n");
		final String httpurl = httptext.substring(index + 41);
		Log.i("URL:::", "" + httpurl);
		httpurls = "" + httpurl;

		img = (TextView) findViewById(R.id.Textfiledownload);
		GetDownload = (Button) findViewById(R.id.getdownload);
		zebra = (EditText) findViewById(R.id.putPassword);
		
		

		 adView = new AdView(this);
		    adView.setAdSize(AdSize.BANNER);
		    adView.setAdUnitId(AD_UNIT_ID);

		    // Add the AdView to the view hierarchy. The view will have no size
		    // until the ad is loaded.
		    LinearLayout layout = (LinearLayout) findViewById(R.id.dolinearLayout);
		    layout.addView(adView);

		    // Create an ad request. Check logcat output for the hashed device ID to
		    // get test ads on a physical device.
		    AdRequest adRequest = new AdRequest.Builder()
		      //  .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		     //   .addTestDevice("JZAYIVGEUG45EA9L")
		        .build();

		    adView.loadAd(adRequest);

		GetDownload.setOnClickListener(new OnClickListener() {
			CharSequence message;

			Handler handler = new Handler();

			public void onClick(View arg0) {
				// Drawable d = img.getDrawable();
				// Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(),
				// d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
				// Canvas canvas = new Canvas(bitmap);
				// d.draw(canvas);
				// new SaveImageAsync(DownloadFile.this,
				// bitmap,httpurl).execute();

		//		new HttpAsyncTask(DownloadFile.this, zebra.getText().toString())
			//			.execute(httpurl);
				
				
			    Intent i = new Intent(DownloadFile.this, FileIntentService.class);
			    i.putExtra("pass",zebra.getText().toString());
			    i.putExtra("url",httpurls);
			    startService(i);
	//		    pd = ProgressDialog.show(DownloadFile.this, "Fetching Image", 
	//		"Go intent service go!"); 

			}

		});

	}
	
	private BroadcastReceiver imageReceiver = new BroadcastReceiver() {
		@Override
		    public void onReceive(Context context, Intent intent) {
		        String location = intent.getExtras().getString("location");
		        if(location == null || location.length() ==0){
		          
		        	runOnUiThread(new Runnable() {
						public void run() {
							
							Toast.makeText(DownloadFile.this,
									"Failed to download image",
									Toast.LENGTH_SHORT).show();
						}
					});
		        }
		        File imageFile = new File(location);
		        if(!imageFile.exists()){
		 //           pd.dismiss();
		            
		            
		            runOnUiThread(new Runnable() {
						public void run() {
							
							Toast.makeText(DownloadFile.this,
									"Unable to Download file :-(",
									Toast.LENGTH_SHORT).show();
						}
					});
		            return;
		        }else 
		        {
		        	   runOnUiThread(new Runnable() {
							public void run() {
								
								Toast.makeText(DownloadFile.this,
										"Downloaded Successfully",
										Toast.LENGTH_SHORT).show();
							}
						});
		        	
		        }
		       
		//        pd.dismiss();
		    }
		};

	/*
	 * private InputStream OpenHttpConnection(String urlString) throws
	 * IOException { InputStream in = null; int response = -1;
	 * 
	 * URL url = new URL(urlString); URLConnection conn = url.openConnection();
	 * 
	 * if (!(conn instanceof HttpURLConnection)) throw new
	 * IOException("Not an HTTP connection");
	 * 
	 * try{ HttpURLConnection httpConn = (HttpURLConnection) conn;
	 * httpConn.setAllowUserInteraction(false);
	 * httpConn.setInstanceFollowRedirects(true);
	 * httpConn.setRequestMethod("GET"); httpConn.connect(); int lenghtOfFile =
	 * httpConn.getContentLength(); byte data[] = new byte[1024]; long total =
	 * 0;
	 * 
	 * response = httpConn.getResponseCode(); if (response ==
	 * HttpURLConnection.HTTP_OK) {
	 * System.out.print("response           bbbbbbbbbbbbb"+response); in =
	 * httpConn.getInputStream(); while ((count = in.read(data)) != -1) { total
	 * += count;
	 * 
	 * publishProgress(""+(int)((total*100)/lenghtOfFile));
	 * 
	 * 
	 * 
	 * } } } catch (Exception ex) { ex.printStackTrace(); } return in; }
	 */
	/*
	 * private Bitmap DownloadImage(String URL) { Bitmap bitmap = null;
	 * InputStream in = null; try { in = OpenHttpConnection(URL); bitmap =
	 * BitmapFactory.decodeStream(in); in.close(); } catch (IOException e1) {
	 * 
	 * e1.printStackTrace(); } return bitmap; }
	 */
		
		 @Override
		  public void onDestroy() {
		    // Destroy the AdView.
		    if (adView != null) {
		      adView.destroy();
		    }
		    super.onDestroy();
		  }
		
		@Override
		protected void onPause() {
			  if (adView != null) {
			      adView.pause();
			    }
			
			super.onPause();
			unregisterReceiver(imageReceiver);
		//	unbindService(mConnection);
			
			GroupController.setActiveGroup(null);
			
		}
		
		@Override
		protected void onResume() 
		{		
			super.onResume();
			  if (adView != null) {
			      adView.resume();
			    }
			
			 IntentFilter intentFilter = new IntentFilter();
		        intentFilter.addAction(FileIntentService.TRANSACTION_DONE);
		    registerReceiver(imageReceiver, intentFilter);
			
			
			
			
			
		}

	private class HttpAsyncTask extends AsyncTask<String, String, byte[]> {
		private Context mContext;
		private ProgressDialog mProgressDialog;
		private String httpurlss;
		private String text;

		public HttpAsyncTask(Context context, String text1) {
			mContext = context;
			text = text1;

		}

		@Override
		protected byte[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			// return DownloadImage(params[0]);

			InputStream in = null;
			int response = -1;
			Bitmap bitmap = null;
			byte[] data1 = null;
			// byte data1[]=null;

			URL url = null;
			try {
				url = new URL(params[0].replace(" ", "%20"));
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

			try {
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
					System.out.print("response           bbbbbbbbbbbbb"
							+ response);
					// in = httpConn.getInputStream();

					// input = new BufferedInputStream(url.openStream(), 8192);
					ByteArrayOutputStream into = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];

					input = httpConn.getInputStream();

					// bitmap = BitmapFactory.decodeStream(input);
					while ((count = input.read(data)) != -1) {
						total += count;
						into.write(data, 0, count);
						// publishing the progress....
						// After this onProgressUpdate will be called
						publishProgress(""
								+ (int) ((total * 100) / lenghtOfFile));

						// writing data to file

					}
					into.close();
					// data1=new byte[into.toByteArray().length];
					byte[] key = null;
					try {
						key = generateKey(text);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						data1 = decodeFile(key, into.toByteArray());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// newStream = new ByteArrayInputStream(data1);
					// System.out.print("newstream          aaaaaa"+newStream);
				}

				// bitmap = BitmapFactory.decodeStream(newStream);
				// System.out.print("bitmap          bbbbbbbbbbbbb"+bitmap);
				input.close();

			} catch (final IOException ex) {
				mProgressDialog.dismiss();

				ex.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						img.setText("Got Exception : see logcat "
								+ ex.getMessage());
						Toast.makeText(DownloadFile.this,
								"Got Exception : see logcat ",
								Toast.LENGTH_SHORT).show();
					}
				});
				Log.w("Download file from server Exception", "Exception : "
						+ ex.getMessage(), ex);

				return null;
			}
			// return in;
			return data1;
		}

		/*
		 * @Override protected void onPreExecute() { super.onPreExecute();
		 * showDialog(progress_bar_type); }
		 */

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
		protected void onPostExecute(byte[] data1) {
			// dismissDialog(progress_bar_type);
			mProgressDialog.dismiss();
			mProgressDialog = null;
			if (data1 != null) {
				img.setVisibility(View.VISIBLE);
				Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG)
						.show();
				new SaveImageAsync(DownloadFile.this, data1, httpurls)
						.execute();
				// System.out.println("ya jama3et el khair"+bitmap);
				img.setText("Received");
			} else {
				Toast.makeText(getBaseContext(),
						"Please make sure you have connection!",
						Toast.LENGTH_LONG).show();

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
	/*
	 * @Override protected Dialog onCreateDialog(int id) { switch (id) { case
	 * progress_bar_type: pDialog = new ProgressDialog(this);
	 * pDialog.setMessage("Downloading file. Please wait...");
	 * pDialog.setIndeterminate(false); pDialog.setMax(100);
	 * pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	 * pDialog.setCancelable(true); pDialog.show(); return pDialog; default:
	 * return null; } }
	 */

	public class SaveImageAsync extends AsyncTask<Void, String, String> {

		private Context mContext;
		private Bitmap bitmaps;
		private String httpurls;
		private byte[] data;
		String Savedat = null;

		private ProgressDialog mProgressDialog;

		public SaveImageAsync(Context context, byte[] data1, String httpurl) {
			mContext = context;
			// bitmaps = bitmap;
			httpurls = httpurl;
			data = data1.clone();
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
		protected String doInBackground(Void... filePath) {
			try {
				// Bitmap bitmap =bitmaps;//;
				// BitmapFactory.decodeResource(mContext.getResources(),
				// imageResourceID);
				final File sourceFile = new File(httpurls);
				ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
				Log.i("Check if data is null", "Check if data :::::" + data);
				// bitmap.compress(CompressFormat.JPEG, 100, byteOutputStream);
				byte[] mbitmapdata = data.clone();// byteOutputStream.toByteArray();
				ByteArrayInputStream inputStream = new ByteArrayInputStream(
						mbitmapdata);

				String baseDir = Environment.getExternalStorageDirectory()
						.getAbsolutePath();
				String fileName = sourceFile.getName();
				createDirIfNotExists("Future_IM");
				OutputStream outputStream = new FileOutputStream(baseDir
						+ File.separator + "Future_IM" + File.separator
						+ fileName);
				Savedat = baseDir + File.separator + "Future_IM"
						+ File.separator + fileName;
				byteOutputStream.writeTo(outputStream);

				byte[] buffer = new byte[128]; // Use 1024 for better
												// performance
				int lenghtOfFile = mbitmapdata.length;
				int totalWritten = 0;
				int bufferedBytes = 0;

				while ((bufferedBytes = inputStream.read(buffer)) > 0) {
					totalWritten += bufferedBytes;
					publishProgress(Integer
							.toString((int) ((totalWritten * 100) / lenghtOfFile)));
					outputStream.write(buffer, 0, bufferedBytes);
				}

			} catch (IOException e) {
				mProgressDialog.dismiss();
				e.printStackTrace();

			}

			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://"
							+ Environment.getExternalStorageDirectory())));

			return Savedat;

		}

		protected void onProgressUpdate(String... progress) {
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String Saved) {
			mProgressDialog.dismiss();
			mProgressDialog = null;

			img.setText("Received and Saved at" + Saved);

		}

		public boolean createDirIfNotExists(String path) {
			boolean ret = true;

			File file = new File(Environment.getExternalStorageDirectory(),
					path);
			if (!file.exists()) {
				if (!file.mkdirs()) {
					Log.e("TravellerLog :: ", "Problem creating Image folder");
					ret = false;
				}
			}
			return ret;
		}

	}

	public static byte[] generateKey(String password) throws Exception {
		byte[] keyStart = password.getBytes("UTF-8");

		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
		sr.setSeed(keyStart);
		kgen.init(128, sr);
		SecretKey skey = kgen.generateKey();
		return skey.getEncoded();
	}

	public static byte[] encodeFile(byte[] key, byte[] fileData)
			throws Exception {

		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

		byte[] encrypted = cipher.doFinal(fileData);

		return encrypted;
	}

	public static byte[] decodeFile(byte[] key, byte[] fileData)
			throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);

		byte[] decrypted = cipher.doFinal(fileData);

		return decrypted;
	}

}