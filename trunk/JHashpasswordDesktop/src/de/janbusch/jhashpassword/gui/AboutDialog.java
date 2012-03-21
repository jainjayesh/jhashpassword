package de.janbusch.jhashpassword.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.swtdesigner.SWTResourceManager;

import de.janbusch.hashpassword.core.CoreInformation;
import de.janbusch.jhashpassword.xml.simple.data.HashPassword;

public class AboutDialog extends Dialog {

	protected Object result;
	protected Shell shlberJhashpassword;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 */
	public AboutDialog(Shell parent) {
		super(parent, SWT.DIALOG_TRIM);
		setText(Messages.AboutDialog_0);
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlberJhashpassword.open();
		shlberJhashpassword.layout();
		Display display = getParent().getDisplay();
		while (!shlberJhashpassword.isDisposed()) {
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
		shlberJhashpassword = new Shell(getParent(), SWT.CLOSE
				| SWT.APPLICATION_MODAL);
		shlberJhashpassword
				.setImage(SWTResourceManager
						.getImage(AboutDialog.class,
								"/de/janbusch/jhashpassword/images/32px-Crystal_Clear_action_lock-silver.png"));
		shlberJhashpassword.setText(Messages.AboutDialog_1);
		shlberJhashpassword.setLayout(new GridLayout(1, false));
		{
			Composite group = new Composite(shlberJhashpassword, SWT.BORDER);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
					1, 1));
			group.setLayout(new GridLayout(2, false));
			{
				Label label = new Label(group, SWT.NONE);
				label.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true,
						false, 1, 1));
				label.setImage(SWTResourceManager.getImage(AboutDialog.class,
						"/de/janbusch/jhashpassword/images/JB-Logo.jpg"));
			}
			{
				Label label = new Label(group, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
						false, 1, 1));
				label.setText(CoreInformation.HASHPASSWORD_COPYRIGHT);
			}
			{
				Label label = new Label(group, SWT.NONE);
				label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
						false, 1, 1));
				label.setImage(SWTResourceManager.getImage(AboutDialog.class,
						"/de/janbusch/jhashpassword/images/JanBusch.png"));
			}
			{
				Label label = new Label(group, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
						false, 1, 1));
				label.setText(CoreInformation.JHASHPASSWORD_COPYRIGHT);
			}
			{
				Label label = new Label(group, SWT.WRAP);
				label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true,
						false, 2, 1));
				label.setText(CoreInformation.ICONSET_COPYRIGHT);
			}
		}
		{
			Composite composite = new Composite(shlberJhashpassword, SWT.BORDER);
			composite.setLayout(new GridLayout(1, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					true, 1, 1));
			{
				Label label = new Label(composite, SWT.SHADOW_IN);
				label.setText(MainApplication.APPLICATION_TITLE + ": "
						+ MainApplication.APPLICATION_VERSION + "\n"
						+ "Hash-Core: " + CoreInformation.HASH_VERSION + "\n"
						+ "XML-Core: " + HashPassword.jhpSXMLVersion);
				;
			}
		}
		shlberJhashpassword.pack();
	}
}
