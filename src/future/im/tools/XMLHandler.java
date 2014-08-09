package future.im.tools;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import future.im.interfaces.IUpdateData;
import future.im.types.FriendInfo;
import future.im.types.GroupInfo;
import future.im.types.GroupMessageInfo;
import future.im.types.MessageInfo;
import future.im.types.STATUS;

import android.util.Log;

/*
 * Parses the xml data to FriendInfo array
 * XML Structure 
 * <?xml version="1.0" encoding="UTF-8"?>
 * 
 * <friends>
 * 		<user key="..." />
 * 		<friend username="..." status="..." IP="..." port="..." key="..." expire="..." />
 * 		<friend username="..." status="..." IP="..." port="..." key="..." expire="..." />
 * </friends>
 *
 *
 *status == online || status == unApproved
 * */

public class XMLHandler extends DefaultHandler
{
		private String userKey = new String();
		private IUpdateData updater;
		
		public XMLHandler(IUpdateData updater) {
			super();
			this.updater = updater;
		}

		private Vector<FriendInfo> mFriends = new Vector<FriendInfo>();
		private Vector<FriendInfo> mOnlineFriends = new Vector<FriendInfo>();
		private Vector<FriendInfo> mUnapprovedFriends = new Vector<FriendInfo>();
		private Vector<GroupInfo> mGroups = new Vector<GroupInfo>();
		
		private Vector<MessageInfo> mUnreadMessages = new Vector<MessageInfo>();
		
		private Vector<GroupMessageInfo> mUnreadGroupMessages = new Vector<GroupMessageInfo>();


		
		public void endDocument() throws SAXException 
		{
			FriendInfo[] friends = new FriendInfo[mFriends.size() + mOnlineFriends.size()];
			MessageInfo[] messages = new MessageInfo[mUnreadMessages.size()];
			GroupInfo[] groups=new GroupInfo[mGroups.size()];
			GroupMessageInfo[] groupmessages = new GroupMessageInfo[mUnreadGroupMessages.size()];
			
			int onlineFriendCount = mOnlineFriends.size();			
			for (int i = 0; i < onlineFriendCount; i++) 
			{				
				friends[i] = mOnlineFriends.get(i);
			}
			
						
			int offlineFriendCount = mFriends.size();			
			for (int i = 0; i < offlineFriendCount; i++) 
			{
				friends[i + onlineFriendCount] = mFriends.get(i);
			}
			
			int unApprovedFriendCount = mUnapprovedFriends.size();
			FriendInfo[] unApprovedFriends = new FriendInfo[unApprovedFriendCount];
			
			for (int i = 0; i < unApprovedFriends.length; i++) {
				unApprovedFriends[i] = mUnapprovedFriends.get(i);
			}
			
			int unreadMessagecount = mUnreadMessages.size();
			//Log.i("MessageLOG", "mUnreadMessages="+unreadMessagecount );
			for (int i = 0; i < unreadMessagecount; i++) 
			{
				messages[i] = mUnreadMessages.get(i);
				Log.i("MessageLOG", "i="+i );
			}
			
			int mGroupscount = mGroups.size();
			//Log.i("MessageLOG", "mUnreadMessages="+unreadMessagecount );
			for (int i = 0; i < mGroupscount; i++) 
			{
				groups[i] = mGroups.get(i);
			//	Log.i("Groups", "i="+i );
			}
			
			int unreadGroupMessagecount = mUnreadGroupMessages.size();
			Log.i("MessageLOG", "mUnreadMessages="+unreadGroupMessagecount );
			for (int i = 0; i < unreadGroupMessagecount; i++) 
			{
				groupmessages[i] = mUnreadGroupMessages.get(i);
				Log.i("MessageLOG", " Group messages i="+i +" "+groupmessages[i].messagetext+" "+groupmessages[i].key);
			}
			
			this.updater.updateData(messages, friends, unApprovedFriends, userKey,groups,groupmessages);
			super.endDocument();
		}		
		
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException 
		{				
			if (localName == "friend")
			{
				FriendInfo friend = new FriendInfo();
				friend.userName = attributes.getValue(FriendInfo.USERNAME);
				String status = attributes.getValue(FriendInfo.STATUS);
				friend.ip = attributes.getValue(FriendInfo.IP);
				friend.port = attributes.getValue(FriendInfo.PORT);
				friend.userKey = attributes.getValue(FriendInfo.USER_KEY);
				//friend.expire = attributes.getValue("expire");
				
				if (status != null && status.equals("online"))
				{					
					friend.status = STATUS.ONLINE;
					mOnlineFriends.add(friend);
				}
				else if (status.equals("unApproved"))
				{
					friend.status = STATUS.UNAPPROVED;
					mUnapprovedFriends.add(friend);
				}	
				else
				{
					friend.status = STATUS.OFFLINE;
					mFriends.add(friend);	
				}											
			}
			else if (localName == "user") {
				this.userKey = attributes.getValue(FriendInfo.USER_KEY);
			}
			else if (localName == "message") {
				MessageInfo message = new MessageInfo();
				message.userid = attributes.getValue(MessageInfo.USERID);
				message.sendt = attributes.getValue(MessageInfo.SENDT);
				message.messagetext = attributes.getValue(MessageInfo.MESSAGETEXT);
				message.key=attributes.getValue(MessageInfo.KEY);
				message.msgid=attributes.getValue(MessageInfo.MSGID);
			//	message.pos=attributes.getValue(MessageInfo.POS);
				message.flag=attributes.getValue(MessageInfo.SEENFLAG);
				Log.i("MessageLOG", "user name "+message.userid + "Sent to"+message.sendt + "Text message"+message.messagetext +"Key  "+ message.key+"mesgid "+message.msgid+"message flag"+message.flag);
				mUnreadMessages.add(message);
			}
			else if (localName == "group") {
				GroupInfo group = new GroupInfo();
				group.groupname = attributes.getValue(GroupInfo.GROUPNAME);
				group.groupowner = attributes.getValue(GroupInfo.GROUPOWNER);
			//	message.messagetext = attributes.getValue(MessageInfo.MESSAGETEXT);
			
			//	message.pos=attributes.getValue(MessageInfo.POS);
			
		//		Log.i("GroupLOG", "group.groupname "+group.groupname + "group.groupowner"+group.groupowner);
				mGroups.add(group);
			}
			
			else if (localName == "groupmessage") {
				GroupMessageInfo groupmessage = new GroupMessageInfo();
				groupmessage.userid = attributes.getValue(GroupMessageInfo.USERID);
				groupmessage.from=attributes.getValue(GroupMessageInfo.FROM);
				groupmessage.sendt = attributes.getValue(GroupMessageInfo.SENDT);
				groupmessage.messagetext = attributes.getValue(GroupMessageInfo.MESSAGETEXT);
				groupmessage.key=attributes.getValue(GroupMessageInfo.KEY);
				groupmessage.msgid=attributes.getValue(GroupMessageInfo.MSGID);
			//	groupmessage.pos=attributes.getValue(MessageInfo.POS);
				groupmessage.flag=attributes.getValue(GroupMessageInfo.SEENFLAG);
				
				Log.i("MessageLOG", "groupmessage.from "+groupmessage.from + "Sent to"+groupmessage.sendt + "Text message"+groupmessage.messagetext +"Key  "+ groupmessage.key+"mesgid "+groupmessage.msgid+"message flag"+groupmessage.flag);
				mUnreadGroupMessages.add(groupmessage);
			}
			
			
			
			super.startElement(uri, localName, name, attributes);
		}

		@Override
		public void startDocument() throws SAXException {			
			this.mFriends.clear();
			this.mOnlineFriends.clear();
			this.mUnreadMessages.clear();
			this.mGroups.clear();
			this.mUnreadGroupMessages.clear();
			super.startDocument();
		}
		
		
}

