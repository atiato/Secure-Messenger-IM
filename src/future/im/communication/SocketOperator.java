package future.im.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import future.im.interfaces.IAppManager;
import future.im.interfaces.ISocketOperator;

import android.util.Log;


public class SocketOperator implements ISocketOperator
{
	private static final String AUTHENTICATION_SERVER_ADDRESS = "http://0392710.NETSOLHOST.COM/test_im/"; //TODO change to your WebAPI Address
	
	private int listeningPort = 0;
	
	private static final String HTTP_REQUEST_FAILED = null;
	
	private HashMap<InetAddress, Socket> sockets = new HashMap<InetAddress, Socket>();
	
	private ServerSocket serverSocket = null;

	private boolean listening;

	private class ReceiveConnection extends Thread {
		Socket clientSocket = null;
		public ReceiveConnection(Socket socket) 
		{
			this.clientSocket = socket;
			SocketOperator.this.sockets.put(socket.getInetAddress(), socket);
		}
		
		@Override
		public void run() {
			 try {
	//			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(
						    new InputStreamReader(
						    		clientSocket.getInputStream()));
				String inputLine;
				
				 while ((inputLine = in.readLine()) != null) 
				 {
					 if (inputLine.equals("exit") == false)
					 {
						 //appManager.messageReceived(inputLine);						 
					 }
					 else
					 {
						 clientSocket.shutdownInput();
						 clientSocket.shutdownOutput();
						 clientSocket.close();
						 SocketOperator.this.sockets.remove(clientSocket.getInetAddress());
					 }						 
				 }		
				
			} catch (IOException e) {
				Log.e("ReceiveConnection.run: when receiving connection ","");
			}			
		}	
	}

	public SocketOperator(IAppManager appManager) {	
	}
	
	
	public String sendHttpRequest(String params)
	{		
		URL url;
		String result = new String();
		try 
		{
			url = new URL(AUTHENTICATION_SERVER_ADDRESS);
			
//			HttpsURLConnection connection;
			HttpURLConnection connection;
		//	trustAllHosts();
//			connection = (HttpsURLConnection) url.openConnection();
			connection = (HttpURLConnection) url.openConnection();
//			connection.setHostnameVerifier(DO_NOT_VERIFY);
			
			connection.setDoOutput(true);
			
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			
			out.println(params);
			out.close();

			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							connection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				result = result.concat(inputLine);				
			}
			in.close();			
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
			Log.w("Connection error ","Null will be returned"+e.getMessage());
			return null;
		}			
		
		if (result.length() == 0) {
			result = HTTP_REQUEST_FAILED;
		}
		
		return result;
		
	
	}


	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
 };


	    /**
	     * Trust every server - dont check for any certificate
	     */
	    private static void trustAllHosts() {
	              // Create a trust manager that does not validate certificate chains
	              TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	                      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                              return new java.security.cert.X509Certificate[] {};
	                      }

	                      public void checkClientTrusted(X509Certificate[] chain,
	                                      String authType) throws CertificateException {
	                      }

	                      public void checkServerTrusted(X509Certificate[] chain,
	                                      String authType) throws CertificateException {
	                      }
	              } };

	              // Install the all-trusting trust manager
	              try {
	                      SSLContext sc = SSLContext.getInstance("TLS");
	                      sc.init(null, trustAllCerts, new java.security.SecureRandom());
	                      HttpsURLConnection
	                                      .setDefaultSSLSocketFactory(sc.getSocketFactory());
	              } catch (Exception e) {
	                      e.printStackTrace();
	              }
	      }



	public int startListening(int portNo) 
	{
		listening = true;
		
		try {
			serverSocket = new ServerSocket(portNo);
			this.listeningPort = portNo;
		} catch (IOException e) {			
			
			//e.printStackTrace();
			this.listeningPort = 0;
			return 0;
		}

		while (listening) {
			try {
				new ReceiveConnection(serverSocket.accept()).start();
				
			} catch (IOException e) {
				//e.printStackTrace();				
				return 2;
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {			
			Log.e("Exception server socket", "Exception when closing server socket");
			return 3;
		}
		
		
		return 1;
	}
	
	
	public void stopListening() 
	{
		this.listening = false;
	}
	
	public void exit() 
	{			
		for (Iterator<Socket> iterator = sockets.values().iterator(); iterator.hasNext();) 
		{
			Socket socket = (Socket) iterator.next();
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) 
			{				
			}		
		}
		
		sockets.clear();
		this.stopListening();
	}


	public int getListeningPort() {
		
		return this.listeningPort;
	}	

}
