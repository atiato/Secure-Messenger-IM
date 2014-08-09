package future.im.tools;

import java.util.Calendar;
import java.util.Date;

import future.im.services.IMService;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalStorageHandler extends SQLiteOpenHelper {

	private static final String TAG = LocalStorageHandler.class.getSimpleName();
	
	private static final String DATABASE_NAME = "AndroidIM.db";
	private static final int DATABASE_VERSION = 2;
	
	private static final String _ID = "_id";
	private static final String TABLE_NAME_MESSAGES = "androidim_messages";
	private static final String TABLE_GROUP_NAME_MESSAGES = "androidim_group_messages";
	public static final String MESSAGE_RECEIVER = "receiver";
	public static final String MESSAGE_SENDER = "sender";
	private static final String MESSAGE_MESSAGE = "message";
	private static final String FLAG_FLAG = "flag";
	private static final String TIME_TIME = "timeofsms";
	private static final String UNREADSMS = "unreadsms";
	
	
	
	private static final String TABLE_MESSAGE_CREATE
	= "CREATE TABLE " + TABLE_NAME_MESSAGES
	+ " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	+ MESSAGE_RECEIVER + " VARCHAR(25), "
	+ MESSAGE_SENDER + " VARCHAR(25), "
	+ MESSAGE_MESSAGE + " VARCHAR(255)," +
		FLAG_FLAG		+ " VARCHAR(25),"+
		UNREADSMS       + " VARCHAR(25),"
		+TIME_TIME + " VARCHAR(40));";
	
	private static final String TABLE_MESSAGE_DROP = 
			"DROP TABLE IF EXISTS "
			+ TABLE_NAME_MESSAGES;
	
	private static final String TABLE_GROUPMESSAGE_CREATE
	= "CREATE TABLE " + TABLE_GROUP_NAME_MESSAGES
	+ " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	+ "groupowner" + " VARCHAR(25), "
	+ "groupname" + " VARCHAR(25), "
	+ "message" + " VARCHAR(255)," +//FROMUID
	 "fromuid" + " VARCHAR(25)," +
		FLAG_FLAG		+ " VARCHAR(25),"
		+TIME_TIME + " VARCHAR(40));";
	
	private static final String TABLE_GROUP_MESSAGE_DROP = 
			"DROP TABLE IF EXISTS "
			+ TABLE_GROUP_NAME_MESSAGES;
	
	
	
	
	public LocalStorageHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_MESSAGE_CREATE);
		db.execSQL(TABLE_GROUPMESSAGE_CREATE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrade der DB von V: "+ oldVersion + " zu V:" + newVersion + "; Alle Daten werden gelöscht!");
		db.execSQL(TABLE_MESSAGE_DROP);
		db.execSQL(TABLE_GROUP_MESSAGE_DROP);
		onCreate(db);
		
	}
	
	public long insert(String sender, String receiver, String message,String flag,String unreadsms){
		long rowId = -1;
		try{
			
			
			
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(MESSAGE_RECEIVER, receiver);
			values.put(MESSAGE_SENDER, sender);
			values.put(MESSAGE_MESSAGE, message);
			values.put(FLAG_FLAG, flag);
			values.put(UNREADSMS, unreadsms);
			values.put(TIME_TIME, getCurrDate()+":");
			rowId = db.insert(TABLE_NAME_MESSAGES, null, values);
			
			
		} catch (SQLiteException e){
			Log.e(TAG, "insert()", e);
		} finally {
			Log.d(TAG, "insert(): rowId=" + rowId);
		}
		return rowId;
		
	}
	
	public long groupinsert(String owner, String groupname, String message,String flag,String fromuid){
		long rowId = -1;
		try{
			
		//groupinsert(imService.getUsername(), group.groupname, message.toString(),"sent")	
			
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("groupowner", owner);
			values.put("groupname", groupname);
			values.put("message", message);
			values.put(FLAG_FLAG, flag);
			values.put("fromuid", fromuid);
			values.put(TIME_TIME, getCurrDate()+":");
			
			rowId = db.insert(TABLE_GROUP_NAME_MESSAGES, null, values);
			
			
		} catch (SQLiteException e){
			Log.e(TAG, "insert()", e);
		} finally {
			Log.d(TAG, "insert(): rowId=" + rowId);
		}
		return rowId;
		
	}
	
	public String getCurrDate()
	{
	    String dt;
	    Date cal = Calendar.getInstance().getTime();
	    dt = cal.toLocaleString();
	    return dt;
	}
	
	public Cursor getgroup(String groupname, String username) {
			//		dbCursor = localstoragehandler.getgroup(group.groupname, IMService.USERNAME );
		
			SQLiteDatabase db = getWritableDatabase();
			String SELECT_QUERY = "SELECT * FROM " + TABLE_GROUP_NAME_MESSAGES + " WHERE " + "groupname" + " LIKE '" + groupname + "' ORDER BY " + _ID + " DESC LIMIT 0, 15";
			return db.rawQuery(SELECT_QUERY,null);
			
			//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
		
	}
	
	public Cursor getgroupall(String groupname, String username) {
		//		dbCursor = localstoragehandler.getgroup(group.groupname, IMService.USERNAME );
	
		SQLiteDatabase db = getWritableDatabase();
		String SELECT_QUERY = "SELECT * FROM " + TABLE_GROUP_NAME_MESSAGES + " WHERE " + "groupname" + " LIKE '" + groupname + "' ORDER BY " + _ID + " DESC";
		return db.rawQuery(SELECT_QUERY,null);
		
		//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
	
}
	
	public Cursor get(String sender, String receiver) {
		
		SQLiteDatabase db = getWritableDatabase();
		String SELECT_QUERY = "SELECT * FROM " + TABLE_NAME_MESSAGES + " WHERE " + MESSAGE_SENDER + " LIKE '" + sender + "' AND " + MESSAGE_RECEIVER + " LIKE '" + receiver + "' OR " + MESSAGE_SENDER + " LIKE '" + receiver + "' AND " + MESSAGE_RECEIVER + " LIKE '" + sender + "' ORDER BY " + _ID + " DESC LIMIT 0, 15";
		return db.rawQuery(SELECT_QUERY,null);
		
		//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
	
}
	
	public Cursor getall(String sender, String receiver) {
		
		SQLiteDatabase db = getWritableDatabase();
		String SELECT_QUERY = "SELECT * FROM " + TABLE_NAME_MESSAGES + " WHERE " + MESSAGE_SENDER + " LIKE '" + sender + "' AND " + MESSAGE_RECEIVER + " LIKE '" + receiver + "' OR " + MESSAGE_SENDER + " LIKE '" + receiver + "' AND " + MESSAGE_RECEIVER + " LIKE '" + sender + "' ORDER BY " + _ID + " DESC";
		return db.rawQuery(SELECT_QUERY,null);
		
		//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
	
}
	
	public void deleteall(String sender, String receiver) {
		
		SQLiteDatabase db = getWritableDatabase();
		String DELECT_QUERY = "DELETE FROM " + TABLE_NAME_MESSAGES + " WHERE " + MESSAGE_SENDER + " LIKE '" + sender + "' AND " + MESSAGE_RECEIVER + " LIKE '" + receiver + "' OR " + MESSAGE_SENDER + " LIKE '" + receiver + "' AND " + MESSAGE_RECEIVER + " LIKE '" + sender + "'";
		db.execSQL(DELECT_QUERY);
		
		//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
	
}
	
	public void deletegroupall(String groupname, String username) {
		//		dbCursor = localstoragehandler.getgroup(group.groupname, IMService.USERNAME );
	
		SQLiteDatabase db = getWritableDatabase();
		String DELECT_QUERY = "DELETE FROM " + TABLE_GROUP_NAME_MESSAGES + " WHERE " + "groupname" + " LIKE '" + groupname + "'";
		db.execSQL(DELECT_QUERY);
		
		//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
	
}
	
	public void update(String msgid,String flag) {
		
		SQLiteDatabase db = getWritableDatabase();
		ContentValues data=new ContentValues();
		 data.put(FLAG_FLAG,flag);
		 Log.i("Update SQL DB","with seeen");
	try {
		 db.update(TABLE_NAME_MESSAGES, data, "_id=" + msgid, null);
		//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
	}
	 catch (SQLiteException e){
		Log.e("update SQL Lite", "Exception at update ()", e);
	}
	
	}
	
public void updatereadsms(String friendname) {
		
		SQLiteDatabase db = getWritableDatabase();
		ContentValues data=new ContentValues();
		 data.put(UNREADSMS,"yes");
		 Log.i("Update SQL DB","with seeen");
	try {
		 db.update(TABLE_NAME_MESSAGES, data, UNREADSMS+"=" + "'no'"+" AND "+MESSAGE_SENDER+" LIKE "+"'"+friendname+"'", null);
		//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
	}
	 catch (SQLiteException e){
		Log.e("update SQL Lite", "Exception at update ()", e);
	}
	
	
	
	
}
	

/*Cursor c = myDatabase.rawQuery(“SELECT count(*) FROM sqlite_master WHERE \
type = ‘table’ AND name != ‘android_metadata’ AND name != ‘sqlite_sequence’ , null);
int i = 0;
if (c.moveToFirst())
i = c.getInt(0);*/
	
public int getcount(String sender, String receiver) {
	int i=0;
	SQLiteDatabase db = getWritableDatabase();
	String SELECT_QUERY = "SELECT count(*) FROM " + TABLE_NAME_MESSAGES + " WHERE (" + MESSAGE_SENDER + " LIKE '" + sender + "' AND " + MESSAGE_RECEIVER + " LIKE '" + receiver + "' OR " + MESSAGE_SENDER + " LIKE '" + receiver + "' AND " + MESSAGE_RECEIVER + " LIKE '" + sender + "') AND "+UNREADSMS+" ='no'"+" ORDER BY " + _ID + " DESC";
	Cursor c= db.rawQuery(SELECT_QUERY,null);
	if (c.moveToFirst())
		i = c.getInt(0);
	return i;
	
	//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");

}
	

}
