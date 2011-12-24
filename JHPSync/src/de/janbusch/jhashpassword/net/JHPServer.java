package de.janbusch.jhashpassword.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JHPServer extends Thread {
	public enum ServerState {
		LISTEN_SOLICITATION, IDLE, SENDING_SOLICITATION, SHUTTING_DOWN
	};

	private ServerState myState;
	private ExecutorService executor;
	private ScheduledExecutorService scheduledExecutor;
	private DatagramSocket serverSocket;
	private boolean isServer;
	private IJHPMsgHandler msgHandler;
	public static final int serverPort = 4832;
	public static final int clientPort = 4833;
	private static final int servertimeout = 1000;
	private static final int clienttimeout = 1000;
	private static int length = 512;
	private InetAddress broadcast;
	private String macAddress;
	private String operatingSystem;

	public JHPServer(boolean isServer, IJHPMsgHandler msgHandler,
			InetAddress inetAddress, String macAddress, String operatingSystem)
			throws IOException {
		this.isServer = isServer;
		this.msgHandler = msgHandler;
		this.executor = Executors.newSingleThreadExecutor();
		this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		this.broadcast = inetAddress;
		this.setName("JHashPassword Server");
		this.operatingSystem = operatingSystem;
		this.macAddress = macAddress;
		this.myState = ServerState.IDLE;

		if (this.isServer) {
			serverSocket = new DatagramSocket(serverPort);
			serverSocket.setSoTimeout(servertimeout);
			myState = ServerState.LISTEN_SOLICITATION;
		} else {
			serverSocket = new DatagramSocket(clientPort);
			serverSocket.setSoTimeout(clienttimeout);
			myState = ServerState.SENDING_SOLICITATION;
			
			scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					ENetCommand req = ENetCommand.SOLICITATION;
					req.setParameter(JHPServer.this.macAddress + "|"
							+ JHPServer.this.operatingSystem);

					sendMessage(new InetSocketAddress(broadcast,
							JHPServer.serverPort), req.toString());

					System.out.println("Sending solicitation...");
				}
			}, 0, 3, TimeUnit.SECONDS);
		}

		System.out.println(this.getName() + ": ready! Running as server: "
				+ isServer);
	}

	public void run() {
		System.out.println(this.getName() + ": starts listening on port: "
				+ serverSocket.getLocalPort());

		DatagramPacket packet;

		while (!this.isInterrupted()) {
			switch (myState) {
			case LISTEN_SOLICITATION:
			case SENDING_SOLICITATION:
				packet = new DatagramPacket(new byte[length], length);
				try {
					serverSocket.receive(packet);
					InetSocketAddress receivedFrom = (InetSocketAddress) packet
							.getSocketAddress();
					String msg = new String(packet.getData(), 0,
							packet.getLength()).trim();
					if (msg != null && msg.length() > 0) {
						msgHandler.handleMessage(msg, receivedFrom);
					}
				} catch (SocketTimeoutException e) {
					continue;
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case SHUTTING_DOWN:
				this.interrupt();
				break;
			case IDLE:
			default:
				try {
					JHPServer.sleep(100);
				} catch (InterruptedException e1) {
					// Interrupted
				}
				break;
			}
		}
		
		this.serverSocket.close();
		System.out.println(this.getName() + ": stopped!");
	}

	public void sendMessage(final InetSocketAddress recipient, final String msg) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg
						.getBytes().length);
				packet.setSocketAddress(recipient);
				try {
					serverSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void killServer() {
		myState = ServerState.SHUTTING_DOWN;
		executor.shutdown();
		scheduledExecutor.shutdown();
		
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
			scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void stopSolicitation() {
		this.scheduledExecutor.shutdown();
	}
	
	public void startListeningForSolicitations() {
		this.myState = ServerState.LISTEN_SOLICITATION;
	}

	public void stopListeningForSolicitations() {
		this.myState = ServerState.IDLE;
	}

}
