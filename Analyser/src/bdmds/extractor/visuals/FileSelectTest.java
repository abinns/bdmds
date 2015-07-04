package bdmds.extractor.visuals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class FileSelectTest
{

	public static void main(String[] args)
	{
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.open();
		FileDialog dialog = new FileDialog(shell);
		String platform = SWT.getPlatform();
		dialog.setFilterPath(platform.equals("win32") ? "c:\\" : "/");
		System.out.println("RESULT=" + dialog.open());
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
		display.dispose();
	}
}
