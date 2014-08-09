package future.im.tools;

import future.im.types.GroupInfo;
import future.im.types.GroupMessageInfo;
import future.im.types.MessageInfo;

/*
 * This class can store friendInfo and check userkey and username combination 
 * according to its stored data
 */
public class UploadController 
{
	
	
	private static String friend;
	private static String imagepath;
	private static String username;
	private static String password;
	private static String groupname;
	private static String groupowner;
	private static GroupInfo[] groups;
	
	public static void setgroupname(String groupname)
	{
		UploadController.groupname = groupname;
	}
	
	public static String getgroupname()
	{
		return groupname;
	}
	
	public static void setgroup(GroupInfo[] groups)
	{
		UploadController.groups = groups;
	}
	
	public static GroupInfo[] getgroup()
	{
		return groups;
	}
	
	
	public static void setfriend(String friend)
	{
		UploadController.friend = friend;
	}
	
	public static String getfriend()
	{
		return friend;
	}
	
	public static void setimagepath(String image)
	{
		UploadController.imagepath = image;
	}
	
	public static String getimagepath()
	{
		return imagepath;
	}
	
	public static void setusername(String username)
	{
		UploadController.username = username;
	}
	
	public static String getusername()
	{
		return username;
	}
	
	public static void setpassword(String password)
	{
		UploadController.password = password;
	}
	
	public static String getpassword()
	{
		return password;
	}
	
	
	

}
