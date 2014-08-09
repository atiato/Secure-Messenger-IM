package future.im.interfaces;

import java.io.UnsupportedEncodingException;


public interface IAppManager {
	
	public String getUsername();
	public String sendMessage(String username,String tousername, String message,String key,String MSGID,String Seen) throws UnsupportedEncodingException;
	public String authenticateUser(String usernameText, String passwordText) throws UnsupportedEncodingException; 
	public String CreateGroup(String usernameText,String passwordText,String groupmemberslist,String groupname, String groupowner) throws UnsupportedEncodingException;
	public String sendGroupMessage(String  username,String  groupname,String message,String key,String Seen) throws UnsupportedEncodingException;
	public String changepassword(String usernameText, String oldpasswordText,String newpasswordText) throws UnsupportedEncodingException; 
	public String deletemessages() throws UnsupportedEncodingException;
	public String createBroadCast() throws UnsupportedEncodingException; 
	public String removeme(String groupname) throws UnsupportedEncodingException ;
	public String removegroup(String groupname) throws UnsupportedEncodingException ;
	public String getmembers(String groupname) throws UnsupportedEncodingException ;
	public String RemoveMembers(String usernameText,String passwordText,String groupmemberslist,String groupname, String groupowner) throws UnsupportedEncodingException; 


	public void messageReceived(String username, String message ,String key,String msgid,String flag);
//	public void setUserKey(String value);
	public boolean isNetworkConnected();
	public boolean isUserAuthenticated();
	public String getLastRawFriendList();
	public void exit();
	public String signUpUser(String usernameText, String passwordText, String email);
	public String addNewFriendRequest(String friendUsername);
	public String sendFriendsReqsResponse(String approvedFriendNames,
			String discardedFriendNames);
	public void setAsForeground();

	
}
