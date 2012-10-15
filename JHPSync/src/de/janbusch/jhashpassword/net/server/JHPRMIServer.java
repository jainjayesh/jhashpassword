package de.janbusch.jhashpassword.net.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import de.janbusch.jhashpassword.xml.simple.data.HashPassword;

public class JHPRMIServer {
	public interface IJHPServer extends Remote {
		public HashPassword getHashPassword() throws RemoteException;
		public void authenticate(String user, String password);
		public void disconnect();
	}

	public static class IJHPServerImpl extends UnicastRemoteObject implements
			IJHPServer {
		private static final long serialVersionUID = 698406232805041187L;
		Registry rmiRegistry;

		public IJHPServerImpl() throws RemoteException {
			super();
		}

		public void start() throws Exception {
			rmiRegistry = LocateRegistry.createRegistry(1099);
			rmiRegistry.bind("jhprmiserver", this);
			System.out.println("Server started");
		}

		public void stop() throws Exception {
			rmiRegistry.unbind("jhprmiserver");
			unexportObject(this, true);
			unexportObject(rmiRegistry, true);
			System.out.println("Server stopped");
		}

		@Override
		public HashPassword getHashPassword() throws RemoteException {
			return null;
		}

		@Override
		public void authenticate(String user, String password) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void disconnect() {
			// TODO Auto-generated method stub
			
		}

	}

	public static void main(String[] args) throws Exception {
		IJHPServerImpl server = new IJHPServerImpl();
		server.start();
		Thread.sleep(5 * 60 * 1000); // run for 5 minutes
		server.stop();
	}
}
