package future.im;

public class MyMessageView {
	

	/**
	 * Store information about a car.
	 */
	
		private String messagetxt;
		private String received;
		private String msgid;
		private String datatime;
		
		
		public MyMessageView(String messagetxt, String received, String msgid ,String datatime) {
			super();
			this.messagetxt = messagetxt;
			this.received = received;
			this.msgid = msgid;
			this.datatime = datatime;
			
		}
		
		public void setMessagetxt(String messagetxt) {
			this.messagetxt = messagetxt;
		}
		
		public void setdatatime(String datatime) {
			this.datatime = datatime;
		}

		public void setReceived(String received) {
			this.received = received;
		}

		public void setmsgid(String msgid) {
			this.msgid = msgid;
		}

		public String getmessagetxt() {
			return messagetxt;
		}
		public String getreceived() {
			return received;
		}
		public String getmsgid() {
			return msgid;
		}
		
		public String getdatatime() {
			return datatime;
		}
		
	}


