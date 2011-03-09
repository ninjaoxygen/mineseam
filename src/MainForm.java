import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ProgressBar;

public class MainForm implements LineHandler {

	protected Shell shell;
	protected List list;
	protected Button btnBackup;
	protected Button btnCheckMapBefore;
	protected Button btnCheckMapAfter;
	protected ProgressBar progressBar;
	protected Text text;
	
	public String[] maps;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainForm window = new MainForm();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Run MineSeam app when Go button is pressed
	 * @param e
	 */
	public void onBtnGo(SelectionEvent e) {
		// Retrieve selected world from listbox
		int worldIndex = list.getSelectionIndex();
		
		if (worldIndex < 0) {
		    MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
		    messageBox.setText("Error");
		    messageBox.setMessage("Please select a world first!");
		    messageBox.open();
			return;
		}
		
		String worldName = list.getItem(worldIndex);
		
		Options o = Options.getOptions();
		o.doBackup = btnBackup.getSelection(); 
		o.mapBefore = btnCheckMapBefore.getSelection(); 
		o.mapAfter = btnCheckMapAfter.getSelection();
		
		// clear the log
		text.setText("");
				
		LineHandlerOutputStream.redirectStd(this);
		try {
			MineSeam.runApp(worldName);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(584, 437);
		shell.setText("MineSeam v0.1 ALPHA");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Group grpSeamParameters = new Group(shell, SWT.NONE);
		grpSeamParameters.setText("Seam Parameters");
		grpSeamParameters.setLayout(null);
		
		Button btnGo = new Button(grpSeamParameters, SWT.NONE);
		btnGo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) { onBtnGo(e); }
		});
		btnGo.setBounds(10, 339, 264, 50);
		btnGo.setText("Go!");
		
		text = new Text(grpSeamParameters, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		text.setBounds(10, 24, 264, 278);
		
		progressBar = new ProgressBar(grpSeamParameters, SWT.NONE);
		progressBar.setBounds(10, 308, 264, 25);
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.VERTICAL));
		
		Group grpWorld = new Group(composite, SWT.NONE);
		grpWorld.setText("World");
		grpWorld.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		list = new List(grpWorld, SWT.BORDER | SWT.V_SCROLL);

		Group grpMapOptions = new Group(composite, SWT.NONE);
		grpMapOptions.setText("Options");
		grpMapOptions.setLayout(new FillLayout(SWT.VERTICAL));
		
		btnBackup = new Button(grpMapOptions, SWT.CHECK);
		btnBackup.setSelection(true);
		btnBackup.setText("Create backup before modifying map");
		
		btnCheckMapBefore = new Button(grpMapOptions, SWT.CHECK);
		btnCheckMapBefore.setText("Produce a \"Before\" map");
		
		btnCheckMapAfter = new Button(grpMapOptions, SWT.CHECK);
		btnCheckMapAfter.setText("Produce an \"After\" map");
		
		for (String w: MineSeam.getWorldList())
			list.add(w);
	}

	public void handleLine(String s) {
		if (s.contains("%")) {
			try {
				String [] pieces = s.split(" ");
				String numStr = pieces[pieces.length - 1];
				numStr = numStr.substring(0, numStr.indexOf('%'));
				progressBar.setSelection(Integer.parseInt(numStr));
			} catch (Exception e) { // catch anything bad that happens
			}
		} else {
			text.append(s);
		}
	}
}
