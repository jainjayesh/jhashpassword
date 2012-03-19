package de.janbusch.jhashpassword.net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JHPMessage<E> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1094767824297592710L;
	
	private List<E> params;
	private MessageType msgType;

	public enum MessageType {
		DISCONNECT, REQ_XML, SEND_XML, ACK, REFUSE, ERROR
	};

	public JHPMessage(MessageType type) {
		this.setMsgType(type);
	}

	public JHPMessage(MessageType type, E param) {
		this.setMsgType(type);

		this.params = new ArrayList<E>();
		this.params.add(param);
	}

	public JHPMessage(MessageType type, List<E> params) {
		this.setMsgType(type);
		this.params = params;
	}

	public MessageType getMsgType() {
		return msgType;
	}

	public void setMsgType(MessageType msgType) {
		this.msgType = msgType;
	}
	
	public List<E> getParams() {
		return params;
	}
}
