package de.janbusch.jhashpassword.gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import de.janbusch.jhashpassword.net.ENetCommand;
import de.janbusch.jhashpassword.net.IJHPMsgHandler;
import de.janbusch.jhashpassword.net.JHPServer;
import de.janbusch.jhashpassword.net.Partner;
import de.janbusch.jhashpassword.net.Util;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class SyncDialog extends Dialog implements IJHPMsgHandler {

	private JHPServer myServer;
	private Object result;
	private Shell shlSynchronisation;
	private Map<String, Partner> availablePartners;
	private Table tblAvailableClients;
	private TableColumn tblclmnIpaddress;
	private TableColumn tblclmnMacaddress;
	private TableColumn tblclmnOperatingSystem;
	private Label imgMyOS;
	private ProgressBar progressBar;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public SyncDialog(Shell parent, int style) {
		super(parent, style | SWT.CLOSE | SWT.APPLICATION_MODAL);
		setText("Synchronisation");

		try {
			availablePartners = new HashMap<String, Partner>();
			myServer = new JHPServer(true, SyncDialog.this, null,
					Util.getMacAddress(InetAddress.getLocalHost()),
					Util.getOperatingSystem());
			myServer.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlSynchronisation.open();
		shlSynchronisation.layout();
		shlSynchronisation.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				myServer.killServer();
			}
		});

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
		shlSynchronisation.setLayout(new GridLayout(3, false));

		Group grpConnection = new Group(shlSynchronisation, SWT.NONE);
		grpConnection.setText("Connection");
		grpConnection.setLayout(new GridLayout(5, false));
		grpConnection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 2));

		imgMyOS = new Label(grpConnection, SWT.NONE);
		imgMyOS.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1,
				1));
		imgMyOS.setImage(SWTResourceManager
				.getImage(SyncDialog.class,
						"/de/janbusch/jhashpassword/images/Crystal_Clear_app_kblackbox.png"));

		Label label = new Label(grpConnection, SWT.NONE);
		label.setText("----");

		Label imgArrow = new Label(grpConnection, SWT.NONE);
		imgArrow.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false,
				1, 1));
		imgArrow.setImage(com.swtdesigner.SWTResourceManager
				.getImage(SyncDialog.class,
						"/de/janbusch/jhashpassword/images/Crystal_Clear_app_package_network.png"));

		Label label_1 = new Label(grpConnection, SWT.NONE);
		label_1.setText("----");

		Label imgOtherOS = new Label(grpConnection, SWT.NONE);
		imgOtherOS.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false,
				1, 1));
		imgOtherOS
				.setImage(com.swtdesigner.SWTResourceManager
						.getImage(SyncDialog.class,
								"/de/janbusch/jhashpassword/images/Crystal_Clear_app_kblackbox.png"));

		Group grpStatus = new Group(shlSynchronisation, SWT.NONE);
		grpStatus.setText("Status");
		grpStatus.setLayout(new GridLayout(2, false));
		grpStatus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				2, 2));

		Label lblState = new Label(grpStatus, SWT.NONE);
		lblState.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				1, 1));
		lblState.setText("State:");

		Label lblConnection = new Label(grpStatus, SWT.NONE);
		lblConnection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		lblConnection.setText("No connection.");

		Composite group = new Composite(shlSynchronisation, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		TabFolder tabFolder = new TabFolder(group, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		TabItem tbtmConnection = new TabItem(tabFolder, SWT.NONE);
		tbtmConnection.setText("Connection");

		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmConnection.setControl(composite_1);
		composite_1.setLayout(new GridLayout(2, false));

		Group grpAvailableSyncpartners = new Group(composite_1, SWT.NONE);
		grpAvailableSyncpartners.setText("Available Syncpartners");
		grpAvailableSyncpartners.setLayout(new GridLayout(2, false));
		grpAvailableSyncpartners.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 1));

		tblAvailableClients = new Table(grpAvailableSyncpartners, SWT.BORDER
				| SWT.FULL_SELECTION);
		tblAvailableClients.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 2, 1));
		tblAvailableClients.setHeaderVisible(true);
		tblAvailableClients.setLinesVisible(true);

		tblclmnIpaddress = new TableColumn(tblAvailableClients, SWT.NONE);
		tblclmnIpaddress.setWidth(176);
		tblclmnIpaddress.setText("IP-Address");

		tblclmnMacaddress = new TableColumn(tblAvailableClients, SWT.NONE);
		tblclmnMacaddress.setWidth(176);
		tblclmnMacaddress.setText("MAC-Address");

		tblclmnOperatingSystem = new TableColumn(tblAvailableClients, SWT.NONE);
		tblclmnOperatingSystem.setWidth(231);
		tblclmnOperatingSystem.setText("Operating System");

		progressBar = new ProgressBar(grpAvailableSyncpartners,
				SWT.INDETERMINATE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 2, 1));

		Group grpControls_1 = new Group(composite_1, SWT.NONE);
		grpControls_1.setText("Controls");
		grpControls_1.setLayout(new GridLayout(1, false));
		grpControls_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				true, 1, 1));

		Button btnAutorefresh = new Button(grpControls_1, SWT.TOGGLE);
		btnAutorefresh.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		btnAutorefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Object sender = arg0.getSource();

				if (sender instanceof Button) {
					Button toggle = (Button) sender;

					if (toggle.getSelection()) {
						myServer.startListeningForSolicitations();
						progressBar.setState(SWT.NORMAL);
					} else {
						myServer.stopListeningForSolicitations();
						progressBar.setState(SWT.PAUSED);
					}
				}
			}
		});
		btnAutorefresh
				.setToolTipText("Autosearch for JHashPassword in sync mode on Android devices or other computers.");
		btnAutorefresh.setSelection(true);
		btnAutorefresh.setText("Autorefresh");

		Button btnconnect = new Button(grpControls_1, SWT.NONE);
		btnconnect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false,
				1, 1));
		btnconnect.setText("&Connect");

		TabItem tbtmSynchronisation = new TabItem(tabFolder, SWT.NONE);
		tbtmSynchronisation.setText("Synchronisation");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmSynchronisation.setControl(composite);
		composite.setLayout(new GridLayout(3, false));

		Group grpYourSettings = new Group(composite, SWT.NONE);
		grpYourSettings.setText("Your Settings");
		grpYourSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		grpYourSettings.setSize(332, 196);

		Group grpControls = new Group(composite, SWT.NONE);
		grpControls.setText("Controls");
		grpControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));

		Group grpRemoteSettings = new Group(composite, SWT.NONE);
		grpRemoteSettings.setText("Remote Settings");
		grpRemoteSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));

		String os = Util.getOperatingSystem();

		if (os.contains("Windows")) {
			imgMyOS.setImage(SWTResourceManager.getImage(SyncDialog.class,
					"/de/janbusch/jhashpassword/images/windows-logo.png"));
		} else if (os.contains("Linux")) {
			imgMyOS.setImage(SWTResourceManager.getImage(SyncDialog.class,
					"/de/janbusch/jhashpassword/images/Linux_tux.png"));
		}
	}

	@Override
	public void handleMessage(String msg, InetSocketAddress from) {
		ENetCommand command = ENetCommand.parse(msg);

		System.out.println("Received msg from " + from.getAddress()
				+ " with content: " + msg);

		switch (command) {
		case REQ:
			System.out.println("Request received! User " + command.getParam());
			break;
		case SOLICITATION:
			System.out.println("Solicitation received!");
			String[] params = command.getParam().split("[|]");
			Partner p = new Partner(from, params[0], params[1]);

			myServer.sendMessage(from, ENetCommand.ADVERTISEMENT.toString());

			if (!availablePartners.containsKey(from.getHostName())) {
				availablePartners.put(from.getHostName(), p);
			}
			break;
		default:
			System.out.println("Unknown command received.");
		}

		updateUI();
	}

	private void updateUI() {
		shlSynchronisation.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				rebuildTable();
			}

			private void rebuildTable() {
				Partner[] addresses = availablePartners.values().toArray(
						new Partner[availablePartners.values().size()]);
				final TableItem[] items = new TableItem[availablePartners
						.size()];

				tblAvailableClients.removeAll();

				for (int i = 0; i < items.length; i++) {
					items[i] = new TableItem(tblAvailableClients, SWT.NONE);
					items[i].setText(new String[] {
							addresses[i].getMyAddress().getHostName(),
							addresses[i].getMacAddress(),
							addresses[i].getOperatingSystem() });
				}
			}
		});
	}
}
