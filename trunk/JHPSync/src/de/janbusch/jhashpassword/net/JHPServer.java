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
	ExecutorService executor;
	ScheduledExecutorService scheduledExecutor;
	private DatagramSocket serverSocket;
	private boolean isServer;
	private IJHPMsgHandler msgHandler;
	public static final int serverPort = 4832;
	public static final int clientPort = 4833;
	private static final int servertimeout = 1000;
	private static final int clienttimeout = 1000;
	private static int length = 512;
	private InetAddress broadcast;

	public JHPServer(boolean isServer, IJHPMsgHandler msgHandler,
			InetAddress inetAddress) throws IOException {
		this.isServer = isServer;
		this.msgHandler = msgHandler;
		this.executor = Executors.newSingleThreadExecutor();
		this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		this.broadcast = inetAddress;
		this.setName("JHashPassword Server");

		if (this.isServer) {
			serverSocket = new DatagramSocket(serverPort);
			serverSocket.setSoTimeout(servertimeout);
		} else {
			serverSocket = new DatagramSocket(clientPort);
			serverSocket.setSoTimeout(clienttimeout);
			scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					ENetCommand req = ENetCommand.SOLICITATION;
					sendMessage(new InetSocketAddress(broadcast,
							JHPServer.serverPort), req.toString());
				}
			}, 0, 3, TimeUnit.SECONDS);
		}

		System.out.println(this.getName() + ": ready! Running as server: "
				+ isServer);
	}

	public void run() {
		System.out.println(this.getName() + ": starts listening on port: "
				+ serverSocket.getLocalPort());

		DatagramPacket packet = new DatagramPacket(new byte[length], length);
		while (!this.isInterrupted()) {
			try {
				serverSocket.receive(packet);
				InetSocketAddress receivedFrom = (InetSocketAddress) packet
						.getSocketAddress();
				String msg = new String(packet.getData(), 0, packet.getLength())
						.trim();
				if (msg != null && msg.length() > 0) {
					msgHandler.handleMessage(msg, receivedFrom);
				}
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				e.printStackTrace();
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
		executor.shutdown();
		JHPServer.this.interrupt();
	}

	public void stopSolicitation() {
		this.scheduledExecutor.shutdown();
	}

}
