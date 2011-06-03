package de.janbusch.jhashpassword.sync;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import de.janbusch.jhashpassword.R;
import de.janbusch.jhashpassword.sync.SyncMessage.MsgType;
import de.janbusch.jhashpassword.sync.SyncMessage.ObserverData;
import de.janbusch.jhashpassword.xml.simple.HashPassword;

public class Synchronization implements Observer {
	public static final int CONNECTION_ESTABLISHING_DIALOG = 0;

	private Socket connection;
	private SyncServer syncServer;
	private static final String HAS_CONNECTION = "hasConnection";
	private ProgressThread syncThread;
	private ProgressDialog syncDialog;

	private HashPassword hashPassword;
	private Context context;
	private Activity callingActivity;
	private Observable observable;

	public Synchronization(Activity callingActivity, Context context,
			HashPassword hashPassword) {
		observable = new Observable();
		observable.addObserver((Observer) callingActivity);

		this.callingActivity = callingActivity;
		this.context = context;
		this.hashPassword = hashPassword;
	}

	public void startSync() {
		callingActivity.showDialog(CONNECTION_ESTABLISHING_DIALOG);
	}

	public void stopSync() {
		if (syncServer != null) {
			syncServer.interrupt();
		}
		
		observable.notifyObservers(ObserverData.CONNECTION_DISCONNECTED);
	}

	final Handler connectionRequestHandler = new Handler() {
		public void handleMessage(Message msg) {
			boolean hasConnection = msg.getData().getBoolean(HAS_CONNECTION);
			if (hasConnection) {
				callingActivity.dismissDialog(CONNECTION_ESTABLISHING_DIALOG);
				syncThread.setState(ProgressThread.STATE_DONE);
				try {
					syncServer = new SyncServer(Synchronization.this, connection);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				// Pop up an input dialog.
				Builder inputDialog = new AlertDialog.Builder(callingActivity);
				inputDialog.setTitle(R.string.titleAcceptConnection);
				inputDialog
						.setMessage(context
								.getString(R.string.msgAcceptConnection)
								+ " "
								+ connection.getInetAddress().getHostName()
								+ "?");
				inputDialog.setNeutralButton(R.string.Permanent,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});
				inputDialog.setPositiveButton(context.getString(R.string.Yes),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								syncServer.start();
								observable.notifyObservers(ObserverData.CONNECTION_ESTABLISHED);
							}
						});
				inputDialog.setNegativeButton(context.getString(R.string.No),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									connection.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
								// Pop up an info dialog.
								Toast
										.makeText(
												context,
												context
														.getString(R.string.msgConnectionRejected),
												Toast.LENGTH_LONG).show();
							}
						});
				inputDialog.show();
			}
		}
	};

	private class ProgressThread extends Thread {
		private Handler mHandler;
		final static int STATE_DONE = 0;
		final static int STATE_RUNNING = 1;
		private int mState;
		private ServerSocket serverSocket;

		ProgressThread(Handler h) {
			mHandler = h;
		}

		public void run() {
			mState = STATE_RUNNING;

			try {
				serverSocket = new ServerSocket(1337);
				serverSocket.setSoTimeout(1000);
				Log.d(toString(), "Server starts listening for connection.");
			} catch (IOException e) {
				e.printStackTrace();
			}

			while (mState == STATE_RUNNING) {
				try {
					Thread.sleep(100);
					connection = serverSocket.accept();
					Log.d(toString(), "Connection established from "
							+ connection.getInetAddress());
					serverSocket.close();
					Message msg = mHandler.obtainMessage();
					Bundle b = new Bundle();
					b.putBoolean(HAS_CONNECTION, true);
					msg.setData(b);
					mHandler.sendMessage(msg);
				} catch (SocketTimeoutException ste) {
					continue;
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (InterruptedException e) {
					Log.e("ERROR", "Thread Interrupted");
				}
			}
			Log.d(toString(), "ConnectionHandler stopped!");
		}

		/*
		 * sets the current state for the thread, used to stop the thread
		 */
		public void setState(int state) {
			mState = state;
		}
	}

	public Dialog getProgressDialog() {
		syncDialog = new ProgressDialog(callingActivity);
		syncDialog.setMessage(context.getString(R.string.msgWaitingForSync));
		syncDialog.setCancelable(true);
		syncDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				syncThread.setState(ProgressThread.STATE_DONE);
			}
		});

		syncThread = new ProgressThread(connectionRequestHandler);
		syncThread.start();

		return syncDialog;
	}

	@Override
	public void update(Observable observable, Object data) {
		observable.notifyObservers(data);
		
		ObserverData observerData = (ObserverData) data;

		switch (observerData) {
		case MESSAGE_RECEIVED:
			syncServer.requestSend(MsgType.ACCEPT, null);
			break;
		}

	}

}
