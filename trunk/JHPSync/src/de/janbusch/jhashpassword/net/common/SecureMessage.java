package de.janbusch.jhashpassword.net.common;

import java.io.NotSerializableException;
import java.io.Serializable;

public class SecureMessage implements Serializable {
	public enum MessageType {
		REC_HASHPASSWORD_XML, REQ_DISCONNECT, REQ_HASHPASSWORD_XML
	}

	private static final long serialVersionUID = -2719059139890954358L;
	private MessageType type;
	private Object payload;

	public SecureMessage(MessageType messageType) {
		this.type = messageType;
	}

	public MessageType getType() {
		return this.type;
	}

	public void setPayload(Object object) throws NotSerializableException {
		if (object instanceof Serializable) {
			this.payload = object;
		} else {
			throw new NotSerializableException();
		}
	}

	public Object getPayload() {
		return payload;
	}
}
