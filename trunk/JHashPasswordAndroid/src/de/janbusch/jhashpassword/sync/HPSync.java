package de.janbusch.jhashpassword.sync;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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

		WifiManager wifiM = (WifiManager) getSystemService(WIFI_SERVICE);
		if (!wifiM.isWifiEnabled()) {
			// Pop up an info dialog.
			Toast.makeText(getBaseContext(),
					getString(R.string.msgWifiNotEnabled), Toast.LENGTH_LONG)
					.show();
			finish();
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
						de.janbusch.jhashpassword.net.Util
								.getBroadcastAddress(getApplicationContext()));
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
			log("Request received! User " + command.getParam());
			break;
		case ADVERTISEMENT:
			log("Advertisement received from " + from.getAddress());
			myJHPServer.stopSolicitation();
			break;
		default:
			log("Unknown command received: " + msg);
		}
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

		myJHPServer.killServer();
		super.onPause();
	}

}
