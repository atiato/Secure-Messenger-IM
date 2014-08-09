package future.im.tools;

import future.im.types.GroupMessageInfo;
import future.im.types.MessageInfo;

/*
 * This class can store friendInfo and check userkey and username combination 
 * according to its stored data
 */
public class GroupMessageController 
{
	
	private static GroupMessageInfo[] groupmessagesInfo = null;
	private static String mem;
	
	
	public static void setGroupMessagesInfo(GroupMessageInfo[] groupmessageInfo)
	{
		GroupMessageController.groupmessagesInfo = groupmessageInfo;
	}
	
	public static void setmemberlist(String members)
	{
		GroupMessageController.mem = members;
	}
	
	public static String getmemberlist()
	{
		return mem;
	}
	
	
	
	public static GroupMessageInfo checkMessage(String username)
	{
		GroupMessageInfo result = null;
		if (groupmessagesInfo != null) 
		{
			for (int i = 0; i < groupmessagesInfo.length;) 
			{
				
					result = groupmessagesInfo[i];
					break;
								
			}			
		}		
		return result;
	}
	
	



	public static GroupMessageInfo getGroupMessageInfo(String username) 
	{
		GroupMessageInfo result = null;
		if (groupmessagesInfo != null) 
		{
			for (int i = 0; i < groupmessagesInfo.length;) 
			{
					result = groupmessagesInfo[i];
					break;
							
			}			
		}		
		return result;
	}






	public static GroupMessageInfo[] getGroupMessagesInfo() {
		return groupmessagesInfo;
	}



	
	
	

}
