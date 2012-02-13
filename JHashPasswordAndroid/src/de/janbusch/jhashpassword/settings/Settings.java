package de.janbusch.jhashpassword.settings;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import de.janbusch.hashpassword.core.CoreInformation;
import de.janbusch.jhashpassword.R;
import de.janbusch.jhashpassword.impexp.HPImpExp;
import de.janbusch.jhashpassword.sync.HPSync;
import de.janbusch.jhashpassword.xml.SimpleXMLUtil;
import de.janbusch.jhashpassword.xml.simple.HashPassword;

public class Settings extends Activity {
	public final static String ITEM_TITLE = "title";
	public final static String ITEM_CAPTION = "caption";
	private final static int REQUESTCODE_SETTINGSXML = 0;
	private static final int REQUESTCODE_IMPEXP = 1;
	private static final int REQUESTCODE_SYNC = 2;
	private boolean hasChanges;
	private HashPassword hashPassword;

	public Map<String, ?> createItem(String title, String caption) {
		Map<String, String> item = new HashMap<String, String>();
		item.put(ITEM_TITLE, title);
		item.put(ITEM_CAPTION, caption);
		return item;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hpsettings);
		hasChanges = false;

		// Get the hashPassword object.
		this.hashPassword = (HashPassword) getIntent().getSerializableExtra(
				getString(R.string.hp));

		List<Map<String, ?>> programmSettings = new LinkedList<Map<String, ?>>();
		programmSettings.add(createItem(getString(R.string.optmen_Timeout),
				getString(R.string.optmen_TimeoutDesc)));

		List<Map<String, ?>> xmlSettings = new LinkedList<Map<String, ?>>();
		xmlSettings.add(createItem(getString(R.string.optmen_SettingsXML),
				getString(R.string.optmen_SettingsXMLDesc)));
		xmlSettings.add(createItem(getString(R.string.optmen_SettingsIE),
				getString(R.string.optmen_SettingsIEDesc)));
		xmlSettings.add(createItem(getString(R.string.optmen_SettingsSync),
				getString(R.string.optmen_SettingsSyncDesc)));

		List<Map<String, ?>> programmInfo = new LinkedList<Map<String, ?>>();
		programmInfo.add(createItem(getString(R.string.app_name) + " v"
				+ getString(R.string.version),
				CoreInformation.JHASHPASSWORD_COPYRIGHT + "\n\n"
						+ CoreInformation.ICONSET_COPYRIGHT + "\n\n"
						+ "Hash-Core: " + CoreInformation.HASH_VERSION + "\n"
						+ "XML-Core: " + HashPassword.jhpSXMLVersion));

		// create our list and custom adapter
		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		adapter.addSection(getString(R.string.programmSettings),
				new SimpleAdapter(this, programmSettings,
						R.layout.list_complex, new String[] { ITEM_TITLE,
								ITEM_CAPTION }, new int[] {
								R.id.list_complex_title,
								R.id.list_complex_caption }));
		adapter.addSection(getString(R.string.xmlSettings), new SimpleAdapter(
				this, xmlSettings, R.layout.list_complex, new String[] {
						ITEM_TITLE, ITEM_CAPTION }, new int[] {
						R.id.list_complex_title, R.id.list_complex_caption }));
		adapter.addSection(getString(R.string.optmen_Aboutjhp),
				new SimpleAdapter(this, programmInfo, R.layout.list_complex,
						new String[] { ITEM_TITLE, ITEM_CAPTION }, new int[] {
								R.id.list_complex_title,
								R.id.list_complex_caption }));

		ListView list = (ListView) findViewById(R.id.lstSettings);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent;
				switch (position) {
				case 1:
					LayoutInflater factory = LayoutInflater.from(Settings.this);
					final View timeoutView = factory.inflate(
							R.layout.seeker_layout, null);
					SeekBar mSeekBar1 = (SeekBar) timeoutView
							.findViewById(R.id.seekBar);
					TextView txtView = (TextView) timeoutView
							.findViewById(R.id.txtValue);
					mSeekBar1.setProgress(hashPassword.getTimeOut() / 1000 / 60);
					txtView.setText(mSeekBar1.getProgress() + " min.");
					mSeekBar1
							.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
								@Override
								public void onStopTrackingTouch(SeekBar seekBar) {
								}

								@Override
								public void onStartTrackingTouch(SeekBar seekBar) {
								}

								@Override
								public void onProgressChanged(SeekBar seekBar,
										int progress, boolean fromUser) {
									TextView txtView = (TextView) timeoutView
											.findViewById(R.id.txtValue);
									txtView.setText(seekBar.getProgress()
											+ " min.");
									hasChanges = true;
								}
							});
					new AlertDialog.Builder(Settings.this)
							.setTitle(R.string.titleSetTimeout)
							.setView(timeoutView)
							.setPositiveButton(R.string.OK,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											SeekBar mSeekBar1 = (SeekBar) timeoutView
													.findViewById(R.id.seekBar);
											hashPassword.setTimeout(mSeekBar1
													.getProgress() * 1000 * 60);
										}
									})
							.setNegativeButton(R.string.Cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
										}
									}).create().show();
					break;
				case 3:
					intent = new Intent(getBaseContext(), HPSettings.class);
					intent.putExtra(getString(R.string.hp), hashPassword);
					startActivityForResult(intent, REQUESTCODE_SETTINGSXML);
					break;
				case 4:
					intent = new Intent(getBaseContext(), HPImpExp.class);
					startActivityForResult(intent, REQUESTCODE_IMPEXP);
					break;
				case 5:
					intent = new Intent(getBaseContext(), HPSync.class);
					startActivityForResult(intent, REQUESTCODE_SYNC);
					break;
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (hasChanges) {
			Builder inputDialog = new AlertDialog.Builder(this);
			inputDialog.setTitle(R.string.titleSaveSettings);
			inputDialog.setMessage(R.string.msgSaveSettings);
			inputDialog.setPositiveButton(getString(R.string.Yes),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Settings.super.onBackPressed();
							saveXML();
							Toast.makeText(getBaseContext(),
									getString(R.string.settingsSaved),
									Toast.LENGTH_SHORT).show();
							setResult(Activity.RESULT_OK, new Intent());
							finish();
						}
					});
			inputDialog.setNegativeButton(getString(R.string.No),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Settings.super.onBackPressed();
							Toast.makeText(
									getBaseContext(),
									getString(R.string.settingsSavingDiscarded),
									Toast.LENGTH_SHORT).show();
							setResult(Activity.RESULT_CANCELED, new Intent());
							finish();
						}
					});
			inputDialog.show();
		} else {
			setResult(Activity.RESULT_OK, new Intent());
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUESTCODE_SETTINGSXML:
			switch (resultCode) {
			case Activity.RESULT_OK:
				Log.d(this.toString(), "RESULT_OK, reloading!");
				hashPassword = (HashPassword) data
						.getSerializableExtra(getString(R.string.hp));
				hasChanges = data.getBooleanExtra(
						getString(R.string.settingsChanged), false);
			case Activity.RESULT_CANCELED:
				Log.d(this.toString(), "RESULT_CANCELED!");
				break;
			default:
				Log.d(this.toString(), "Unknown resultcode: " + resultCode);
				break;
			}
			break;
		case REQUESTCODE_IMPEXP:
			try {
				hashPassword = SimpleXMLUtil.getXML(getApplicationContext());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.d(this.toString(), "Resultcode: " + resultCode);
			break;
		default:
			Log.d(this.toString(), "Unknown requestcode: " + requestCode);
		}
	}

	private void saveXML() {
		// Serialize that thing!
		try {
			SimpleXMLUtil.writeXML(hashPassword, getBaseContext());
			// Log.d(this.toString(), "HashPassword.xml was saved!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
