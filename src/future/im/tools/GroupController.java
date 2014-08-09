package future.im.tools;

import future.im.types.FriendInfo;
import future.im.types.GroupInfo;

/*
 * This class can store friendInfo and check userkey and username combination 
 * according to its stored data
 */
public class GroupController 
{
	
	private static GroupInfo[] groupsInfo = null;
	
	private static String activeGroup;
	
	public static void setGroupsInfo(GroupInfo[] groupInfo)
	{
		GroupController.groupsInfo = groupInfo;
	}
	
	
	
	
	
	public static void setActiveGroup(String groupname){
		activeGroup = groupname;
	}
	
	public static String getActiveGroup()
	{
		return activeGroup;
	}



	public static GroupInfo getGroupsInfo(String username) 
	{
		GroupInfo result = null;
		if (groupsInfo != null) 
		{
			for (int i = 0; i < groupsInfo.length; i++) 
			{
				
					result = groupsInfo[i];
					break;
						
			}			
		}		
		return result;
	}



	



	public static GroupInfo[] getGroupsInfo() {
		return groupsInfo;
	}



	
	

}
