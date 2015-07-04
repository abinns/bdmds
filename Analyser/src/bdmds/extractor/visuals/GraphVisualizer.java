package bdmds.extractor.visuals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GraphVisualizer
{

	private Shell	shell;

	public GraphVisualizer(Display display)
	{

		this.shell = new Shell(display);

		this.shell.setText("BDMD Extraction Visualizer");

		this.initUI();

		this.shell.setSize(1024, 768);

		this.shell.open();

		while (!this.shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	public void initUI()
	{
		this.shell.setLayout(new FillLayout());

		Canvas canvas = new Canvas(this.shell, SWT.NONE);
		Color black = canvas.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		Color white = canvas.getDisplay().getSystemColor(SWT.COLOR_WHITE);

		canvas.addPaintListener((e) -> {
			Rectangle rect = canvas.getBounds();
			e.gc.setBackground(black);
			e.gc.setForeground(black);
			e.gc.fillRectangle(rect);

			e.gc.setForeground(white);

			for (int x = 0, y = 0; x < rect.width; x++, y = (int) (rect.height * Math.sin((double) x / 360)))
				e.gc.drawPoint(x, y);
		});
	}
}
