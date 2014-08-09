package future.im.interfaces;
import future.im.types.FriendInfo;
import future.im.types.GroupInfo;
import future.im.types.GroupMessageInfo;
import future.im.types.MessageInfo;


public interface IUpdateData {
	public void updateData(MessageInfo[] messages, FriendInfo[] friends, FriendInfo[] unApprovedFriends, String userKey,GroupInfo[] groups,GroupMessageInfo[] groupmessages);

}
