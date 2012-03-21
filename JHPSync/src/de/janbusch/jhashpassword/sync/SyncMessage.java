package de.janbusch.jhashpassword.sync;

import java.io.Serializable;

import de.janbusch.jhashpassword.xml.simple.data.HashPassword;



public class SyncMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1184453949917519082L;

	public static enum MsgType { ACCEPT, REJECT, CONNECT }
	public static enum ObserverData { CONNECTION_ESTABLISHED, CONNECTION_DISCONNECTED, MESSAGE_RECEIVED };

	private MsgType myType;
	private HashPassword myHashPassword;;
	
	public SyncMessage(MsgType msgType) {
		myType = msgType;
	}

	public SyncMessage(MsgType msgType, HashPassword hashPassword) {
		myType = msgType;
		myHashPassword = hashPassword;
	}
	
	public MsgType getType() {
		return myType;
	}
	
	public HashPassword getHashPassword() {
		return myHashPassword;
	}

}
