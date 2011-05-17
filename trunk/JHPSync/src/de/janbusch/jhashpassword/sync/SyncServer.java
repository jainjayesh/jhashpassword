package de.janbusch.jhashpassword.sync;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.EmptyStackException;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.janbusch.jhashpassword.sync.SyncMessage.MsgType;
import de.janbusch.jhashpassword.sync.SyncMessage.ObserverData;
import de.janbusch.jhashpassword.xml.simple.HashPassword;


/**
 * This class implements a SlaveServer. A SlaveServer communicates with a single
 * or all clients.
 * 
 * @author Jan Busch
 */
public class SyncServer extends Thread {
	private static final int socketTimeout = 1000;

	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	private Socket mySocket = null;

	private Stack<Runnable> sendQueue = new Stack<Runnable>();
	private Stack<SyncMessage> receiveQueue = new Stack<SyncMessage>();
	private ExecutorService myExecutor = Executors
			.newSingleThreadExecutor(Executors.defaultThreadFactory());

	private Observable observable;

	/**
	 * Constructor for a SyncServer
	 * 
	 * @param clientSocket
	 * @throws IOException
	 */
	public SyncServer(Observer o, Socket clientSocket) throws IOException {
		observable = new Observable();
		observable.addObserver(o);
		
		mySocket = clientSocket;

		out = new ObjectOutputStream(new BufferedOutputStream(mySocket
				.getOutputStream()));

		mySocket.setSoTimeout(socketTimeout);
		
		this.setName("SyncServerThread");
	}

	/**
	 * Runmethod for the SubServer
	 */
	@Override
	public void run() {
		SyncMessage recMsg = null;

		while (!this.isInterrupted()) {
			// Send message
			while (!sendQueue.isEmpty()) {
				myExecutor.execute(sendQueue.pop());
			}

			// Receive message
			try {
				Object inObject = this.in.readObject();
				if (inObject instanceof SyncMessage) {
					recMsg = (SyncMessage) inObject;
				}
			} catch (SocketTimeoutException e) {
				continue;
			} catch (NullPointerException e) {
				try {
					sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					break;
				}
			} catch (EOFException e) {
				this.interrupt();
				System.out.println(this.getName()
						+ ": Connection was resetted by client!");
				continue;
			} catch (IOException e) {
				continue;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			messageReceived(recMsg);
		}

		try {
			in.close();
			out.close();
			mySocket.close();
		} catch (Exception e) {
			System.out.println(this.getName() + ": Socket already closed.");
		}
		
		observable.notifyObservers(ObserverData.CONNECTION_DISCONNECTED);

		System.out.println(this.getName() + " stopped!");
	}

	/**
	 * Gives in a ReceivedMessage by a SlaveServer to decide
	 */
	public synchronized void messageReceived(final SyncMessage inComing) {
		receiveQueue.add(inComing);
		observable.notifyObservers(ObserverData.MESSAGE_RECEIVED);
	}

	/**
	 * Sends a command to the client
	 * 
	 * @param CmdID
	 * @param HashPassword
	 */
	public synchronized void send(MsgType CmdID, HashPassword hashPassword) {
		SyncMessage outM = new SyncMessage(CmdID, hashPassword);

		try {
			out.writeObject(outM);
			out.flush();

			if (CmdID == MsgType.ACCEPT) {
				in = new ObjectInputStream(new BufferedInputStream(mySocket
						.getInputStream()));
			}
		} catch (IOException e) {
			e.printStackTrace();
			interrupt();
			observable.notifyObservers(ObserverData.CONNECTION_DISCONNECTED);
		}
	}

	public synchronized void requestSend(final MsgType cmdID,
			final HashPassword hashPassword) {
		sendQueue.add(new Runnable() {
			@Override
			public void run() {
				send(cmdID, hashPassword);
			}
		});
	}

	public SyncMessage getMsg() {
		try {
			return receiveQueue.pop();
		} catch (EmptyStackException e) {
			return null;
		}

	}
}
