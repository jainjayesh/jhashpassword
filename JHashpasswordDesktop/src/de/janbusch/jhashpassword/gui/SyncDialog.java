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
		shlSynchronisation.setLayout(new GridLayout(1, false));
		
		Group group = new Group(shlSynchronisation, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Label lblNewLabel = new Label(group, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		lblNewLabel.setImage(SWTResourceManager.getImage(SyncDialog.class, "/de/janbusch/jhashpassword/images/Crystal_Clear_app_kblackbox.png"));

	}
}
