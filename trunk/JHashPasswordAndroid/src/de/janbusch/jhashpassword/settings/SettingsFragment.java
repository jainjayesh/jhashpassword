package de.janbusch.jhashpassword.settings;

import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.janbusch.jhashpassword.HPHomeFragment;
import de.janbusch.jhashpassword.R;
import de.janbusch.jhashpassword.xml.simple.data.HashPassword;

public class SettingsFragment extends Fragment {
	public final static String ITEM_TITLE = "title";
	public final static String ITEM_CAPTION = "caption";
	private final static int REQUESTCODE_SETTINGSXML = 0;
	private static final int REQUESTCODE_IMPEXP = 1;
	private static final int REQUESTCODE_SYNC = 2;
	private boolean hasChanges;
	private HashPassword hashPassword;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.settings, container, false);
		return view;
	}
	
	public Map<String, ?> createItem(String title, String caption) {
		Map<String, String> item = new HashMap<String, String>();
		item.put(ITEM_TITLE, title);
		item.put(ITEM_CAPTION, caption);
		
		return item;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		hasChanges = false;
	}
}
