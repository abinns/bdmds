import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import bdmds.core.backend.U;
import bdmds.core.storage.DataPacket;
import bdmds.core.storage.DbUtil;
import bdmds.recorder.display.CloseHandler;
import bdmds.recorder.display.Renderer;
import bdmds.recorder.display.ViewableData;
import bdmds.recorder.parsing.DbUploader;
import bdmds.recorder.serial.DueInterface;

public class Driver
{
	private static String initDataset()
	{
		U.p("Initializing dataset");
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		String name = "Data Stream Test" + sdf.format(cal.getTime());
		String ident = "id" + name.hashCode();
		try
		{
			DbUtil.setupDataset(name, ident);
		} catch (SQLException e1)
		{
			e1.printStackTrace();
		}
		return ident;
	}

	private static void initDisplay(CloseHandler onclose, ViewableData dataviewer)
	{
		AppGameContainer appgc;

		int winWidth = 1024;
		int winHeight = 600;
		Renderer renderer = new Renderer(onclose, dataviewer);
		try
		{
			appgc = new AppGameContainer(renderer);
			appgc.setDisplayMode(winWidth, winHeight, false);
			appgc.setShowFPS(true);
			appgc.setTargetFrameRate(60);
			appgc.setMaximumLogicUpdateInterval(40);
			appgc.setAlwaysRender(true);
			appgc.setMultiSample(0);
			appgc.start();
		} catch (SlickException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static DueInterface[] initInterface()
	{
		String[] serialPortNames = DueInterface.getSerialPortNames();
		DueInterface[] res = new DueInterface[serialPortNames.length];
		int i = 0;
		for (String cur : serialPortNames)
			res[i++] = new DueInterface(cur);
		if (res.length == 0)
		{
			U.e("Error, no serial ports detected. Please remember to plug in the device.");
			System.exit(42);
		}
		U.p("Opening serial ports " + Arrays.toString(serialPortNames) + ".");
		return res;
	}

	private static DbUploader initUploader(DueInterface conn, String ident, String channel, ViewableData dataviewer)
	{
		DbUploader uploader = new DbUploader(ident, channel);

		U.p("Setting up listener");
		conn.setListener((data) -> {
			try
			{
				DataPacket curPacket = new DataPacket(data);
				uploader.offer(curPacket);
				dataviewer.offer(curPacket);
			} catch (NumberFormatException e)
			{
				// e.printStackTrace();
			}
		});
		return uploader;
	}

	public static void main(String... CHEESE)
	{
		U.setDebugLevel(0);
		DueInterface[] conns = Driver.initInterface();

		String ident = Driver.initDataset();

		U.p("Initializing display");
		int winWidth = 1024;
		int winHeight = 600;
		int colCount = 4;
		ViewableData dataviewer = new ViewableData(winWidth, winHeight, colCount);

		U.p("Initializing uploader");
		int i = 0;
		DbUploader[] uploaders = new DbUploader[conns.length];
		for (DueInterface cur : conns)
			uploaders[i] = Driver.initUploader(cur, ident, "Channel " + i++, dataviewer);
		U.p("Starting Datastream");
		U.p("Streaming...");

		long start = System.nanoTime();
		for (DueInterface cur : conns)
			cur.startDataStream();

		new Thread(() -> {
			Driver.initDisplay(() -> {
				long end = System.nanoTime();
				for (DueInterface cur : conns)
					cur.stopDataStream();
				U.p("Stopped streaming. Took " + (end - start) + "ns");

				for (DbUploader cur : uploaders)
					cur.updateSize();

				U.p("Dumping to file");
				Driver.printDatasetToFile(ident);

				U.p("Cleaning up...");
				for (DueInterface cur : conns)

					cur.close();
				U.p("All done!");
			}, dataviewer);
		}).start();
	}

	private static void printDatasetToFile(String ident)
	{
		U.p("Retrieving values from database...");
		LinkedList<DataPacket[]> data = null;
		try
		{
			data = DbUtil.getAllData(ident);
		} catch (SQLException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		U.p("Now dumping recieved packets to file...");
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter("output.csv");
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println("Data packets from ident " + ident);
		int i = 0;
		for (DataPacket[] arr : data)
			for (DataPacket p : arr)
				writer.println("" + i++ + p);
		writer.close();
	}
}
