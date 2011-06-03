package de.janbusch.jhashpassword;

import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import de.janbusch.hashpassword.core.CoreInformation;
import de.janbusch.hashpassword.core.EHashType;
import de.janbusch.hashpassword.core.HashUtil;
import de.janbusch.jhashpassword.impexp.HPImpExp;
import de.janbusch.jhashpassword.settings.Settings;
import de.janbusch.jhashpassword.xml.SimpleXMLUtil;
import de.janbusch.jhashpassword.xml.simple.HashPassword;
import de.janbusch.jhashpassword.xml.simple.Host;
import de.janbusch.jhashpassword.xml.simple.LoginName;

public class HPHome extends Activity {

	private static final int REQUESTCODE_SETTINGSXML = 0;
	private static final int REQUESTCODE_IMPEXP = 1;
	private static final int REQUESTCODE_CLEAR_CLIPBOARD = 2;

	private static final int NOTIFICATION_ID = 1337;

	private HashPassword hashPassword;
	private Timer hpTimer;
	private Spinner sprHostname;
	private Spinner sprLoginname;
	private ArrayAdapter<CharSequence> sprHostnameAdapter;
	private ClipboardManager clipboard;
	private NotificationManager notifitcationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		setWatcher();

		// Grab the spinners.
		sprHostname = (Spinner) findViewById(R.id.sprHostname);
		sprLoginname = (Spinner) findViewById(R.id.sprLoginname);
		sprHostname.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				loadLoginNames();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// why should I do something?
			}

		});
		sprHostname.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				runOnUiThread(new Runnable() {
					public void run() {
						clipboard.setText((CharSequence) sprHostname
								.getSelectedItem());
						Toast.makeText(getBaseContext(),
								getString(R.string.msgCopiedToClipboard),
								Toast.LENGTH_SHORT).show();
					}
				});
				return true;
			}
		});
		sprLoginname.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				runOnUiThread(new Runnable() {
					public void run() {
						clipboard.setText((CharSequence) sprLoginname
								.getSelectedItem());
						Toast.makeText(getBaseContext(),
								getString(R.string.msgCopiedToClipboard),
								Toast.LENGTH_SHORT).show();
					}
				});
				return true;
			}
		});

		clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		notifitcationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		hpTimer = new Timer();
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadXML();
		loadHostNames();
		loadLoginNames();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (hashPassword != null) {
			saveXML();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadHostNames();
		loadLoginNames();

		Integer clearClipboard = getIntent().getIntExtra(
				"REQUESTCODE_CLEAR_CLIPBOARD", -1);
		if (clearClipboard == REQUESTCODE_CLEAR_CLIPBOARD) {
			clipboard.setText("");
			Toast.makeText(getBaseContext(),
					getString(R.string.msgClipboardCleared), Toast.LENGTH_SHORT)
					.show();
			hpTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					finish();
				}
			}, 100);
		}
	}

	/**
	 * This method reads the XML-File by using the XMLUtil class.
	 */
	private void loadXML() {
		try {
			hashPassword = SimpleXMLUtil.getXML(getBaseContext());
			Log.d(this.toString(), "HashPassword.xml was read!");
		} catch (Exception e) {
			if (e instanceof FileNotFoundException) { // No HashPassword.xml
				// found
				Log.d(this.toString(),
						"XML-File not found."
								+ "Creating new HashPassword.xml with default settings.");

				// Pop up an info dialog.
				AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
				infoDialog.setTitle(R.string.app_name);
				infoDialog.setMessage(R.string.msgXMLNotFound);
				infoDialog.setPositiveButton(getString(R.string.OK), null);
				infoDialog.show();

				// Create new HashPassword.xml
				hashPassword = new HashPassword();
				hashPassword
						.setDefaultCharset(CoreInformation.DEFAULT_CHARACTERSET);
				hashPassword
						.setDefaultHashType(CoreInformation.DEFAULT_HASHTYPE);
				hashPassword
						.setDefaultPasswordLength(CoreInformation.DEFAULT_PASSWORD_LENGTH);
			} else {
				// Something went terribly wrong while reading the XML-File.
				Log.d(this.toString(), "Error while reading HashPassword.xml!");

				// Show error message and close the app without changing the
				// XML-File.
				AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
				errorDialog.setTitle(R.string.titleXMLFile);
				errorDialog.setMessage(R.string.msgXMLFailure);
				errorDialog.setPositiveButton(getString(R.string.Yes),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent impexpIntent = new Intent(
										getBaseContext(), HPImpExp.class);
								startActivityForResult(impexpIntent,
										REQUESTCODE_IMPEXP);
								finish();
							}
						});
				errorDialog.setNegativeButton(getString(R.string.No),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								System.exit(1);
							}
						});
				errorDialog.show();
			}
		}
	}

	/**
	 * This method loads host informations from the {@link HashPassword} object
	 * into the ui components.
	 */
	private void loadHostNames() {
		if (hashPassword == null)
			return;

		// Load hosts into spinner.
		int lastHostPos;
		String lastHostname = hashPassword.getLastHost();
		sprHostnameAdapter = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_item);
		sprHostnameAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (Host host : hashPassword.getHosts().getHost()) {
			sprHostnameAdapter.add(host.getName());
		}
		lastHostPos = sprHostnameAdapter.getPosition(lastHostname);
		sprHostname.setAdapter(sprHostnameAdapter);

		if (lastHostPos == -1) {
			if (sprHostname.getCount() > 0) {
				sprHostname.setSelection(0);
			}
		} else {
			sprHostname.setSelection(lastHostPos);
		}
	}

	/**
	 * This method loads login informations from the {@link HashPassword} object
	 * into the ui components.
	 */
	private void loadLoginNames() {
		if (hashPassword == null)
			return;

		// Load loginnames of the current host into spinner.
		int lastLoginPos;
		if (sprHostname.getSelectedItem() != null) {
			String hostName = sprHostname.getSelectedItem().toString();
			Host currentHost = hashPassword.getHosts().getHostByName(hostName);
			if (currentHost != null) {
				ArrayAdapter<CharSequence> sprLoginAdapter = new ArrayAdapter<CharSequence>(
						this, android.R.layout.simple_spinner_item);
				sprLoginAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				for (LoginName login : currentHost.getLoginNames()
						.getLoginName()) {
					sprLoginAdapter.add(login.getName());
				}
				sprLoginname.setAdapter(sprLoginAdapter);
				lastLoginPos = sprLoginAdapter.getPosition(currentHost
						.getLastLogin());

				if (lastLoginPos == -1) {
					if (sprLoginname.getCount() > 0) {
						sprLoginname.setSelection(0);
					}
				} else {
					sprLoginname.setSelection(lastLoginPos);
				}
			}
		} else {
			ArrayAdapter<CharSequence> sprLoginAdapter = new ArrayAdapter<CharSequence>(
					this, android.R.layout.simple_spinner_item);
			sprLoginAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sprLoginname.setAdapter(sprLoginAdapter);
		}
	}

	/**
	 * This method uses the XMLUtil class to write the object of type
	 * {@link HashPassword} to a file.
	 */
	private void saveXML() {
		// Set last host and last login
		String lastHostName = new String();
		if (sprHostname.getSelectedItem() != null) {
			lastHostName = sprHostname.getSelectedItem().toString();
		}
		Host lastHost = hashPassword.getHosts().getHostByName(lastHostName);
		String lastLoginName = new String();
		Object lastLoginNameObject = sprLoginname.getSelectedItem();

		if (lastLoginNameObject != null) {
			lastLoginName = sprLoginname.getSelectedItem().toString();
		}

		hashPassword.setLastHost(lastHostName);
		if (lastHost != null) {
			lastHost.setLastLogin(lastLoginName);
		}

		// Serialize that thing!
		try {
			SimpleXMLUtil.writeXML(hashPassword, getBaseContext());
			// Log.d(this.toString(), "HashPassword.xml was saved!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets all watchers for this activity.
	 */
	private void setWatcher() {
		final Button btnGenPW = (Button) findViewById(R.id.btnGenPW);
		final EditText txtPassphraseOne = (EditText) findViewById(R.id.etPasswordOne);
		final EditText txtPassphraseTwo = (EditText) findViewById(R.id.etPasswordTwo);

		// Validates the given passphrases and sets the generate button
		// enabled or disabled due to the passphrase validation.
		TextWatcher pwWatcher = new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().toString().length() > 0) {
					btnGenPW.setEnabled(txtPassphraseOne.getText().toString()
							.matches(txtPassphraseTwo.getText().toString()));
				} else {
					btnGenPW.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// Nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Nothing

			}
		};
		txtPassphraseOne.addTextChangedListener(pwWatcher);
		txtPassphraseTwo.addTextChangedListener(pwWatcher);
	}

	private void closeJHP() {
		Builder inputDialog = new AlertDialog.Builder(this);
		inputDialog.setTitle(R.string.btnClearClipboard);
		inputDialog.setMessage(R.string.msgClearClipboardQuestion);
		inputDialog.setPositiveButton(getString(R.string.Yes),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clipboard.setText("");
						hpTimer.cancel();
						notifitcationManager.cancel(NOTIFICATION_ID);

						Toast.makeText(getBaseContext(),
								getString(R.string.msgClipboardCleared),
								Toast.LENGTH_SHORT).show();
						finish();
					}
				});
		inputDialog.setNegativeButton(getString(R.string.No),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		inputDialog.show();
	}

	/**
	 * This method specifies what should be done when a button was clicked.
	 * 
	 * @param btn
	 *            Button that has been clicked as {@link View}.
	 * @return True if the button is known otherwise false.
	 */
	public boolean onButtonClicked(View btn) {
		switch (btn.getId()) {
		case R.id.btnGenPW:
			EditText txtPassphraseOne = (EditText) findViewById(R.id.etPasswordOne);
			EditText txtPassphraseTwo = (EditText) findViewById(R.id.etPasswordTwo);

			String hostname = (String) sprHostname.getSelectedItem();
			String loginname = (String) sprLoginname.getSelectedItem();
			String passphrase = txtPassphraseOne.getText().toString();

			Host currentHost = hashPassword.getHosts().getHostByName(hostname);
			if (currentHost == null) {
				return false;
			}

			LoginName currentLogin = currentHost.getLoginNames()
					.getLoginNameByName(loginname);

			if (hostname == null) {
				hostname = new String();
			}
			if (loginname == null) {
				loginname = new String();
			}
			if (passphrase == null) {
				passphrase = new String();
			}

			String pw = null;
			pw = HashUtil.generatePassword(hostname, loginname, passphrase,
					EHashType.valueOf(currentHost.getHashType()), currentHost
							.getCharset(), Integer.parseInt(currentHost
							.getPasswordLength().replaceAll("[^0-9]", "")));
			if (currentHost.getLoginNames().getLoginName().size() > 1) {
				try {
					pw = HashUtil.generatePassword(hostname, loginname,
							passphrase, EHashType.valueOf(currentLogin
									.getHashType()), currentLogin.getCharset(),
							Integer.parseInt(currentLogin.getPasswordLength()
									.replaceAll("[^0-9]", "")));
				} catch (Exception e) {
					Log.d(this.toString(), e.getMessage());
				}
			}

			clipboard.setText(pw);
			txtPassphraseOne.setText(new String());
			txtPassphraseTwo.setText(new String());

			Notification n = new Notification(R.drawable.icon,
					getString(R.string.notificationClipboard),
					System.currentTimeMillis());
			Intent notificationIntent = new Intent(this, HPHome.class);
			notificationIntent.putExtra("REQUESTCODE_CLEAR_CLIPBOARD",
					REQUESTCODE_CLEAR_CLIPBOARD);
			PendingIntent contentIntent = PendingIntent.getActivity(
					getBaseContext(), 0, notificationIntent, 0);
			n.setLatestEventInfo(getApplicationContext(),
					getString(R.string.app_name),
					getString(R.string.notificationClipboard), contentIntent);

			n.flags |= Notification.FLAG_AUTO_CANCEL;
			notifitcationManager.notify(NOTIFICATION_ID, n);

			Integer timeout = hashPassword.getTimeOut();
			if (timeout > 0) {
				hpTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						clipboard.setText("");
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(
										getBaseContext(),
										getString(R.string.msgClipboardCleared),
										Toast.LENGTH_SHORT).show();
							}
						});
						notifitcationManager.cancel(NOTIFICATION_ID);
						Log.d(this.toString(),
								"Removed password from clipboard!");
					}
				}, timeout);
			}

			Toast.makeText(getBaseContext(),
					getString(R.string.msgPasswordCreated), Toast.LENGTH_LONG)
					.show();

			Log.d(this.toString(), "Password generated, timer started: " + timeout);
			finish();
			return true;
		case R.id.btnShowClipboard:
			AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
			infoDialog.setTitle(R.string.titleClipboard);
			infoDialog.setMessage(clipboard.getText());
			infoDialog.setPositiveButton(getString(R.string.OK), null);
			infoDialog.show();
			return true;
		default:
			Log.d(this.toString(), "Clicked button has no case.");
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			Intent settingsIntent = new Intent(getBaseContext(), Settings.class);
			settingsIntent.putExtra(getString(R.string.hp), hashPassword);
			startActivityForResult(settingsIntent, REQUESTCODE_SETTINGSXML);
			return true;
		case R.id.help:
			Intent helpIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(getString(R.string.myHomepage)
							+ getString(R.string.helpFile)));
			startActivity(helpIntent);
			return true;
		case R.id.goPro:
			Intent donateIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(getString(R.string.donateLink)));
			startActivity(donateIntent);
			return true;
		case R.id.aboutjhp:
			AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
			infoDialog.setTitle(getString(R.string.app_name) + " v"
					+ getString(R.string.version));
			infoDialog.setMessage(CoreInformation.JHASHPASSWORD_COPYRIGHT
					+ "\n\n" + CoreInformation.ICONSET_COPYRIGHT + "\n\n"
					+ "Hash-Core: " + CoreInformation.HASH_VERSION + "\n"
					+ "XML-Core: " + HashPassword.jhpSXMLVersion);
			infoDialog.setPositiveButton(getString(R.string.OK), null);
			infoDialog.show();
			return true;
		case R.id.clearclipb:
			clipboard.setText("");

			Toast.makeText(getBaseContext(),
					getString(R.string.msgClipboardCleared), Toast.LENGTH_SHORT)
					.show();
			return true;
		case R.id.exit:
			closeJHP();
			return true;
		default:
			Log.d(this.toString(),
					"Clicked menu button has no case: " + item.getItemId());
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void onBackPressed() {
		closeJHP();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUESTCODE_SETTINGSXML:
			switch (resultCode) {
			case Activity.RESULT_OK:
				Log.d(this.toString(), "RESULT_OK, reloading!");
				loadXML();
				loadLoginNames();
				loadHostNames();
				break;
			case Activity.RESULT_CANCELED:
				Log.d(this.toString(), "RESULT_CANCELED!");
				break;
			default:
				Log.d(this.toString(), "Unknown resultcode: " + resultCode);
				break;
			}
			break;
		case REQUESTCODE_IMPEXP:
			switch (resultCode) {
			case Activity.RESULT_OK:
				loadXML();
				loadHostNames();
				loadLoginNames();
				break;
			case Activity.RESULT_CANCELED:
				break;
			default:
				Log.d(this.toString(), "Unknown resultcode: " + resultCode);
				break;
			}
			break;
		default:
			Log.d(this.toString(), "Unknown requestcode: " + requestCode);
		}
	}
}