package bdmds.extractor.visuals;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import bdmds.core.backend.U;
import bdmds.core.storage.DataChannel;
import bdmds.core.storage.Dataset;
import bdmds.core.storage.DbUtil;

public class Visualizer
{
	/**
	 * Launch the application.
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			Visualizer window = new Visualizer();
			window.open();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private Dataset	leadData;
	private int		max		= 0;
	private int		min		= 0;

	private boolean	loaded	= false;
	protected Shell	shell;
	private Label	txtAvailableDatasets;
	private Canvas	dataCanvas;

	private List	datasetList;

	private int clamp(int in, int max, int min)
	{
		if (in < min)
			in = min;
		if (in > max)
			in = max;
		return in;
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents()
	{
		this.shell = new Shell();
		this.shell.setSize(1000, 700);
		this.shell.setText("BDMDS Visulization Manager");
		GridLayout gl_shell = new GridLayout(2, false);
		gl_shell.verticalSpacing = 0;
		gl_shell.horizontalSpacing = 0;
		gl_shell.marginHeight = 0;
		gl_shell.marginWidth = 0;
		this.shell.setLayout(gl_shell);

		Composite composite = new Composite(this.shell, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_composite.widthHint = 200;
		gd_composite.minimumWidth = 200;
		composite.setLayoutData(gd_composite);
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		composite.setLayout(gl_composite);

		this.txtAvailableDatasets = new Label(composite, SWT.BORDER | SWT.CENTER);
		this.txtAvailableDatasets.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		this.txtAvailableDatasets.setText("Available Datasets");

		this.datasetList = new List(composite, SWT.BORDER);
		this.datasetList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		this.datasetList.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String[] sel = Visualizer.this.datasetList.getSelection();
				if (sel.length == 0)
					Visualizer.this.min = Visualizer.this.max;
				else if (Visualizer.this.leadData == null || !sel[0].equals(Visualizer.this.leadData.getName()))
					try
					{
						Visualizer.this.setDataset(DbUtil.getDatasetByName(sel[0]));
					} catch (SQLException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}

		});

		Thread datasetUpdater = new Thread(() -> {
			while (true)
			{
				Display.getDefault().syncExec(() -> {
					try
					{
						String[] selection = this.datasetList.getSelection();
						String sel = selection.length > 0 ? selection[0] : "";
						this.datasetList.removeAll();
						for (Dataset dataset : DbUtil.getDatasets())
							this.datasetList.add(dataset.getName());
						int index = this.datasetList.indexOf(sel);
						if (index >= 0)
							this.datasetList.select(index);
					} catch (Exception e1)
					{
						e1.printStackTrace();
					}
				});
				try
				{
					Thread.sleep(30 * 1000);
				} catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});
		datasetUpdater.setDaemon(true);
		datasetUpdater.start();

		TabFolder tabFolder = new TabFolder(this.shell, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TabItem tbtmCollect = new TabItem(tabFolder, SWT.NONE);
		tbtmCollect.setText(" Collect ");

		TabItem tbtmExtract = new TabItem(tabFolder, SWT.NONE);
		tbtmExtract.setText(" Extract ");

		Composite extractComposite = new Composite(tabFolder, SWT.NONE);
		tbtmExtract.setControl(extractComposite);
		GridLayout gl_extractComposite = new GridLayout(1, true);
		gl_extractComposite.horizontalSpacing = 0;
		gl_extractComposite.verticalSpacing = 0;
		gl_extractComposite.marginHeight = 0;
		gl_extractComposite.marginWidth = 0;
		extractComposite.setLayout(gl_extractComposite);

		Composite composite_4 = new Composite(extractComposite, SWT.NONE);
		GridLayout gl_composite_4 = new GridLayout(2, false);
		gl_composite_4.marginWidth = 0;
		gl_composite_4.verticalSpacing = 0;
		gl_composite_4.horizontalSpacing = 0;
		gl_composite_4.marginHeight = 0;
		composite_4.setLayout(gl_composite_4);
		GridData gd_composite_4 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_4.heightHint = 300;
		composite_4.setLayoutData(gd_composite_4);

		Scale canvasScale = new Scale(composite_4, SWT.VERTICAL);
		canvasScale.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		canvasScale.setMaximum(100);
		canvasScale.setIncrement(1);
		canvasScale.setPageIncrement(10);

		Composite canvasComposite = new Composite(composite_4, SWT.NONE);
		GridLayout gl_composite_3 = new GridLayout(1, false);
		gl_composite_3.marginWidth = 0;
		gl_composite_3.marginHeight = 0;
		gl_composite_3.verticalSpacing = 0;
		gl_composite_3.horizontalSpacing = 0;
		canvasComposite.setLayout(gl_composite_3);
		canvasComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		this.setupLeadDisplay(canvasComposite, canvasScale);

		Composite composite_1 = new Composite(extractComposite, SWT.NONE);
		GridLayout gl_composite_1 = new GridLayout(5, true);
		gl_composite_1.verticalSpacing = 0;
		gl_composite_1.horizontalSpacing = 10;
		gl_composite_1.marginWidth = 0;
		composite_1.setLayout(gl_composite_1);
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_1.heightHint = 35;
		composite_1.setLayoutData(gd_composite_1);
		new Label(composite_1, SWT.NONE);

		Button btnStartExtraction = new Button(composite_1, SWT.NONE);
		btnStartExtraction.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnStartExtraction.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
			}
		});
		btnStartExtraction.setText("Start Extraction");

		Button btnStopExtraction = new Button(composite_1, SWT.NONE);
		btnStopExtraction.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnStopExtraction.setText("Stop Extraction");

		Button btnExportFeatures = new Button(composite_1, SWT.NONE);
		btnExportFeatures.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnExportFeatures.setText("Export Features");
		new Label(composite_1, SWT.NONE);

		Composite composite_2 = new Composite(extractComposite, SWT.NONE);
		composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_composite_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_2.heightHint = 200;
		composite_2.setLayoutData(gd_composite_2);

		StyledText styledText = new StyledText(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);

		TabItem tbtmAnalyze = new TabItem(tabFolder, SWT.NONE);
		tbtmAnalyze.setText(" Analyze ");

		Composite analyzeComposite = new Composite(tabFolder, SWT.NONE);
		tbtmAnalyze.setControl(analyzeComposite);
		analyzeComposite.setLayout(new GridLayout(1, false));
		new Label(this.shell, SWT.NONE);
		new Label(this.shell, SWT.NONE);

	}

	private int logChk(int x, int scale)
	{
		return (int) (scale / 2 * Math.log((double) x / 360));
	}

	/**
	 * Open the window.
	 */
	public void open()
	{
		Display display = Display.getDefault();
		this.createContents();
		this.shell.open();
		this.shell.layout();
		while (!this.shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	private int[] plotData(int streamIndex, int offset, int scale, int height, int width) throws SQLException
	{
		double[] data = this.leadData.getData()[streamIndex].asArray();
		int[] output = new int[width];
		int winWidth = scale * 10 + 200;
		int start = offset * (data.length - winWidth) / 100;
		int end = start + winWidth;

		// Scales from 1 to 6?
		int pointsperbin = this.clamp(winWidth, 999999999, width) / width;

		int k = 0;
		for (int i = start; i < end && k < output.length; i += pointsperbin)
			output[k++] = (int) ((data[i] - this.min) / ((double) this.max - (double) this.min) * height * this.min);
		U.p("pnts:" + pointsperbin + ", max:" + this.max + ", min:" + this.min + ", off:" + offset + ", scl:" + scale + ", hgt:" + height + ", wdth:" + width + ", start:" + start + ", end:" + end
				+ ", win:" + winWidth);
		U.p("Original: " + Arrays.toString(Arrays.copyOfRange(data, start, end)));
		U.p("Output: " + Arrays.toString(output));

		return output;
	}

	public void setDataset(Dataset dataset) throws SQLException
	{
		this.leadData = dataset;
		this.loaded = false;
		this.dataCanvas.redraw();

		Thread thread = new Thread(() -> {
			try
			{
				System.out.println("Test 1");
				PriorityQueue<Double> queue = new PriorityQueue<>();
				for (DataChannel data : dataset.getData())
					for (double val : data.asArray())
						queue.add(val);

				int dataPoints = queue.size();
				for (int i = 0; i < dataPoints * .05; ++i)
					queue.poll();
				this.min = (int) (double) queue.peek();
				for (int i = (int) Math.ceil(dataPoints * .05); i < dataPoints * .95; ++i)
					queue.poll();
				this.max = (int) (double) queue.peek();
				this.loaded = true;
				System.out.println("Test 2");
				Display.getDefault().syncExec(() -> {
					this.dataCanvas.redraw();
				});
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void setupLeadDisplay(Composite parent, Scale scale)
	{
		final AtomicBoolean maximized = new AtomicBoolean(false);
		final AtomicInteger selectedCanvas = new AtomicInteger(0);
		this.dataCanvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		this.dataCanvas.setLayout(new GridLayout(1, false));
		this.dataCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Slider canvasSlider = new Slider(parent, SWT.NONE);
		canvasSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		canvasSlider.setValues(0, 0, 110, 10, 1, 10);

		Color black = this.dataCanvas.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		Color white = this.dataCanvas.getDisplay().getSystemColor(SWT.COLOR_WHITE);

		SelectionAdapter listener = new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Visualizer.this.dataCanvas.redraw();
			}
		};
		scale.addSelectionListener(listener);
		canvasSlider.addSelectionListener(listener);

		this.dataCanvas.addPaintListener((e) -> {
			try
			{
				Rectangle rect = this.dataCanvas.getBounds();
				int offset = canvasSlider.getSelection();
				e.gc.setBackground(black);
				e.gc.setForeground(black);
				e.gc.fillRectangle(rect);

				e.gc.setForeground(white);

				if (this.max == this.min)
				{
					e.gc.drawString("No Dataset Selected.", 10, 10 / 2, true);
					return;
				}

				if (maximized.get())
				{
					if (!this.loaded)
					{
						e.gc.drawString("Loading Lead " + (selectedCanvas.get() + 1) + "...", 0, 0);
						return;
					}
					e.gc.drawString("Lead " + (selectedCanvas.get() + 1), 0, 0);
					int[] data = this.plotData(selectedCanvas.get(), offset, scale.getSelection(), rect.height, rect.width);

					for (int x = 0; x < rect.width; x++)
						e.gc.drawPoint(x, data[x]);
					return;
				}

				for (int cell = 0; cell < 8; ++cell)
				{
					int xOffset = rect.width / 4 * (cell % 4);
					int yOffset = rect.height / 2 * (cell / 4);
					if (!this.loaded)
					{
						e.gc.drawString("Loading Lead " + (cell + 1) + "...", xOffset, yOffset);
						continue;
					}
					e.gc.drawString("Lead " + (cell + 1), xOffset, yOffset);

					int[] data = this.plotData(cell, offset, scale.getSelection(), rect.height / 2, rect.width / 4);
					for (int x = 0; x < data.length; x++)
						e.gc.drawPoint(x + xOffset, data[x] + yOffset);
				}
			} catch (Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		this.dataCanvas.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				Rectangle rect = Visualizer.this.dataCanvas.getBounds();

				maximized.set(!maximized.get());
				int xVal = e.x * 4 / rect.width;
				int yVal = e.y * 2 / rect.height;
				int selection = xVal + 4 * yVal;
				// selection = e.x * 4 / (rect.width) + 4 * e.y * 2 /
				// (rect.height);
				selectedCanvas.set(selection);
				Visualizer.this.dataCanvas.redraw();
			}

			@Override
			public void mouseDown(MouseEvent e)
			{
			}

			@Override
			public void mouseUp(MouseEvent e)
			{
			}
		});
	}
}
