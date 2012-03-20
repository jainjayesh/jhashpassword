package de.janbusch.jhashpassword.net.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import de.janbusch.jhashpassword.net.common.JHPMessage;
import de.janbusch.jhashpassword.net.common.JHPMessage.MessageType;

public class ProcessConnection extends Thread {
	Socket client;
	ObjectInputStream is;
	ObjectOutputStream os;

	public ProcessConnection(Socket s) { // constructor
		client = s;
		try {
			is = new ObjectInputStream(client.getInputStream());
			os = new ObjectOutputStream(client.getOutputStream());
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}
		this.start(); // Thread starts here...this start() will call run()
	}

	@SuppressWarnings("rawtypes")
	public void run() {
		try {
			// get a request and parse it.
			Object request = is.readObject();
			System.out.println("Request: " + request);
			if (request instanceof JHPMessage) {
				MessageType type = ((JHPMessage) request).getMsgType();
				switch (type) {
				case DISCONNECT:
					@SuppressWarnings("unchecked")
					JHPMessage<String> req = (JHPMessage<String>) request;
					System.out.println(req.getParams().get(0));
					is.close();
					os.close();
					client.close();
					break;
				default:
					System.out.println("Unkown message type " + type.toString());
					break;
				}
			} else {
				JHPMessage<String> msg = new JHPMessage<String>(
						MessageType.ERROR, "Unknown object type "
								+ request.toString());
				os.writeObject(msg);
			}
			client.close();
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
}
