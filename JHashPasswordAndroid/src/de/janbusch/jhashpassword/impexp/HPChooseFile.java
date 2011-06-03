package de.janbusch.jhashpassword.impexp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.janbusch.jhashpassword.R;

public class HPChooseFile extends Activity {
	private static final String BACK = "..";
	private static final String PATH_SDCARD = "/sdcard";
	private ListView lstFiles;
	private TextView txtCurrentDir;
	private List<String> dirEntries;
	private File currentDir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imp_exp_chooser);

		lstFiles = (ListView) findViewById(R.id.lstFiles);
		txtCurrentDir = (TextView) findViewById(R.id.txtCurrentDir);
		dirEntries = new ArrayList<String>();

		lstFiles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				String selection = lstFiles.getItemAtPosition(position)
						.toString();
				Log.d(HPChooseFile.this.toString(), "Selected dir: "
						+ selection);

				if (selection.matches(BACK)) {
					setDir(currentDir.getParent());
				} else {
					setDir(currentDir.getPath() + "/" + selection);
				}
				showFiles();
			}
		});

		setDir(PATH_SDCARD);
		showFiles();
	}

	private void setDir(String path) {
		File f = new File(path);
		if (f.canRead()) {
			if (f.isDirectory()) {
				currentDir = f;
				dirEntries.clear();

				if (currentDir.getParent() != null
						&& !currentDir.getPath().matches(PATH_SDCARD)) {
					dirEntries.add(BACK);
				}

				for (File file : currentDir.listFiles()) {
					if (file.isFile()) {
						if (file.getName().toLowerCase().endsWith(".xml")) {
							dirEntries.add(file.getName());
						}
					} else {
						dirEntries.add(file.getName());
					}
				}
			} else if (f.isFile()) {
				Log.d(this.toString(), "Changes saved!");
				setResult(Activity.RESULT_OK, new Intent().putExtra(
						getString(R.string.selectedFile), f.getPath()));

				Toast.makeText(
						getBaseContext(),
						getString(R.string.msgSelectedFile) + " " + f.getName(),
						Toast.LENGTH_SHORT).show();

				finish();
			}
		}
		Collections.sort(dirEntries, String.CASE_INSENSITIVE_ORDER);
	}

	private void showFiles() {
		txtCurrentDir.setText(currentDir.getPath());

		ListAdapter adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dirEntries);
		lstFiles.setAdapter(adapter);
	}
}
