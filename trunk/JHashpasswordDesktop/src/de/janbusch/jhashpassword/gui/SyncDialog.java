package de.janbusch.jhashpassword.gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class SyncDialog extends Dialog {

	protected Object result;
	protected Shell shlSynchronisation;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public SyncDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlSynchronisation.open();
		shlSynchronisation.layout();
		Display display = getParent().getDisplay();
		while (!shlSynchronisation.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlSynchronisation = new Shell(getParent(), getStyle());
		shlSynchronisation.setSize(764, 507);
		shlSynchronisation.setText("Synchronisation");
		shlSynchronisation.setLayout(new GridLayout(2, false));
		
		Group grpConnection = new Group(shlSynchronisation, SWT.NONE);
		grpConnection.setText("Connection");
		grpConnection.setLayout(new GridLayout(3, false));
		grpConnection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
		
		Label imgMyOS = new Label(grpConnection, SWT.NONE);
		imgMyOS.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		imgMyOS.setImage(SWTResourceManager.getImage(SyncDialog.class, "/de/janbusch/jhashpassword/images/Crystal_Clear_app_kblackbox.png"));
		
		Label imgArrow = new Label(grpConnection, SWT.NONE);
		imgArrow.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
		imgArrow.setImage(com.swtdesigner.SWTResourceManager.getImage(SyncDialog.class, "/de/janbusch/jhashpassword/images/Crystal_Clear_app_package_network.png"));

		Label imgOtherOS = new Label(grpConnection, SWT.NONE);
		imgOtherOS.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
		imgOtherOS.setImage(com.swtdesigner.SWTResourceManager.getImage(SyncDialog.class, "/de/janbusch/jhashpassword/images/Crystal_Clear_app_kblackbox.png"));
		
		Group grpStatus = new Group(shlSynchronisation, SWT.NONE);
		grpStatus.setText("Status");
		grpStatus.setLayout(new GridLayout(2, false));
		grpStatus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Label lblState = new Label(grpStatus, SWT.NONE);
		lblState.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblState.setText("State:");
		
		Label lblConnection = new Label(grpStatus, SWT.NONE);
		lblConnection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblConnection.setText("No connection.");
		
		Group grpControl = new Group(shlSynchronisation, SWT.NONE);
		grpControl.setText("Control");
		grpControl.setLayout(new GridLayout(2, false));
		grpControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Button btnAutorefresh = new Button(grpControl, SWT.TOGGLE);
		btnAutorefresh.setToolTipText("Autosearch for JHashPassword on Android devices or other computers.");
		btnAutorefresh.setSelection(true);
		btnAutorefresh.setText("Autorefresh");
		
		Button btnDisconnect = new Button(grpControl, SWT.NONE);
		btnDisconnect.setEnabled(false);
		btnDisconnect.setText("Disconnect");
		
		Composite group = new Composite(shlSynchronisation, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		TabFolder tabFolder = new TabFolder(group, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TabItem tbtmConnection = new TabItem(tabFolder, SWT.NONE);
		tbtmConnection.setText("Connection");
		
		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmConnection.setControl(composite_1);
		composite_1.setLayout(new GridLayout(1, false));
		
		TabItem tbtmSynchronisation = new TabItem(tabFolder, SWT.NONE);
		tbtmSynchronisation.setText("Synchronisation");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmSynchronisation.setControl(composite);
		composite.setLayout(new GridLayout(3, false));
		
		Group grpYourSettings = new Group(composite, SWT.NONE);
		grpYourSettings.setText("Your Settings");
		grpYourSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpYourSettings.setSize(332, 196);
		
		Group grpControls = new Group(composite, SWT.NONE);
		grpControls.setText("Controls");
		grpControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group grpRemoteSettings = new Group(composite, SWT.NONE);
		grpRemoteSettings.setText("Remote Settings");
		grpRemoteSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
}
}
