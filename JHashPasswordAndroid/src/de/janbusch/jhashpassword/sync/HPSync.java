package de.janbusch.jhashpassword.sync;

import java.io.IOException;
import java.net.InetSocketAddress;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import de.janbusch.jhashpassword.R;
import de.janbusch.jhashpassword.net.ENetCommand;
import de.janbusch.jhashpassword.net.IJHPMsgHandler;
import de.janbusch.jhashpassword.net.JHPServer;
import de.janbusch.jhashpassword.xml.simple.HashPassword;

public class HPSync extends Activity implements IJHPMsgHandler {
	private final String TAG = "de.janbusch.jhashpassword.sync.HPSync";
	private HashPassword hashPassword;
	private JHPServer myJHPServer;
	private TextView txtSyncState;
	private Button btnStartSync;
	private Button btnStopSync;
	private TextView txtLog;
	private ScrollView scroller;
	private WakeLock wl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync);

		final WifiManager wifiM = (WifiManager) getSystemService(WIFI_SERVICE);
		if (!wifiM.isWifiEnabled()) {
			Builder inputDialog = new AlertDialog.Builder(HPSync.this);
			inputDialog.setTitle(R.string.app_name_sync);
			inputDialog.setMessage(R.string.msgWifiNotEnabled);
			inputDialog.setPositiveButton(getString(R.string.Yes),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							wifiM.setWifiEnabled(true);
						}
					});
			inputDialog.setNegativeButton(R.string.No, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			inputDialog.show();
		}

		// Grab views
		txtSyncState = (TextView) findViewById(R.id.txtSyncState);
		btnStartSync = (Button) findViewById(R.id.btnStartSync);
		btnStopSync = (Button) findViewById(R.id.btnStopSync);
		txtLog = (TextView) findViewById(R.id.txtViewLog);
		scroller = (ScrollView) findViewById(R.id.scrollViewLog);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);

		// Get the hashPassword object.
		this.hashPassword = (HashPassword) getIntent().getSerializableExtra(
				getString(R.string.hp));
	}

	/**
	 * This method specifies what to do if a button was clicked.
	 * 
	 * @param btn
	 *            Button that has been clicked as {@link View}.
	 * @return True if the button is known otherwise false.
	 */
	public boolean onButtonClicked(View btn) {
		switch (btn.getId()) {
		case R.id.btnStartSync:
			btnStartSync.setEnabled(false);
			txtSyncState.setText(R.string.txtConnectionPending);
			btnStopSync.setEnabled(true);
			wl.acquire();

			try {
				myJHPServer = new JHPServer(false, this,
						Util.getBroadcastAddress(getApplicationContext()),
						Util.getMacAddressAndroid(getApplicationContext()),
						Util.getOperatingSystemAndroid());
				myJHPServer.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return true;
		case R.id.btnStopSync:
			if (wl != null && wl.isHeld())
				wl.release();

			myJHPServer.killServer();
			btnStopSync.setEnabled(false);
			txtSyncState.setText(R.string.txtNoConnection);
			btnStartSync.setEnabled(true);
			return true;
		default:
			Log.d(this.toString(), "Clicked button has no case.");
			return false;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	public void handleMessage(String msg, final InetSocketAddress from) {
		ENetCommand command = ENetCommand.parse(msg);

		switch (command) {
		case REQ:
			log("Request received from " + from.getAddress() + ", "
					+ command.getParam());
			showReqAckDialog(from, command);
			break;
		case ADVERTISEMENT:
			log("Advertisement received from " + from.getAddress());
			// myJHPServer.stopSolicitation();
			break;
		default:
			log("Unknown command received: " + msg);
		}
	}

	private void showReqAckDialog(final InetSocketAddress from,
			final ENetCommand command) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String msgReqAckDialog = String.format(
						getString(R.string.msgReqAckDialog), from.getAddress()
								+ ", " + command.getParam());

				Builder inputDialog = new AlertDialog.Builder(HPSync.this);
				inputDialog.setTitle(R.string.titleReqAckDialog);
				inputDialog.setMessage(msgReqAckDialog);
				inputDialog.setPositiveButton(getString(R.string.Yes),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								myJHPServer.sendMessage(from,
										ENetCommand.ACK.toString());
							}
						});
				inputDialog.setNegativeButton(getString(R.string.No),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Toast.makeText(
										getBaseContext(),
										getString(R.string.toastReqAckDiscarded),
										Toast.LENGTH_SHORT).show();
							}
						});
				inputDialog.show();
			}
		});
	}

	private void log(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				txtLog.append(msg + "\n");
				scroller.post(new Runnable() {
					public void run() {
						scroller.smoothScrollTo(0, txtLog.getBottom());
					}
				});
			}
		});
	}

	@Override
	protected void onPause() {
		if (wl != null && wl.isHeld())
			wl.release();

		if (myJHPServer != null) {
			myJHPServer.killServer();
		}
		
		super.onPause();
	}

}
