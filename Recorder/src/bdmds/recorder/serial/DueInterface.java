package bdmds.recorder.serial;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import bdmds.core.backend.U;
import bdmds.recorder.parsing.ByteBuffer;

public class DueInterface implements SerialPortEventListener
{
	public static String[] getSerialPortNames()
	{
		return SerialPortList.getPortNames();
	}

	private SerialPort	port;
	private DueListener	listener;
	private ByteBuffer	input;
	private boolean		dataStreaming;

	public DueInterface(String serialPortName)
	{
		this.port = new SerialPort(serialPortName);
		this.input = new ByteBuffer();
		try
		{
			this.port.openPort();
			this.port.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			int mask = SerialPort.MASK_RXCHAR;
			this.port.setEventsMask(mask);// Set mask
			this.port.addEventListener(this);// Add SerialPortEventListener
		} catch (SerialPortException e)
		{
			U.e("Error opening port:", e);
		}
	}

	public void close()
	{
		try
		{
			U.p(this.port.closePort());
		} catch (SerialPortException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getSegments(int segments)
	{
		U.d("Getting " + segments + " lines.", 3);
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < segments; i++)
		{
			Long timeout = System.nanoTime() + 100000000;
			while (!this.input.hasNextSegment() && timeout < System.nanoTime())
				;
			if (this.input.hasNextSegment())
				res.append(this.input.getNextSegment() + "\n");
			else
				res.append(this.input.flush() + "\n");
			U.d("getting segs, currently on " + res.toString(), 4);
		}
		return res.toString();
	}

	public String getStatus()
	{
		this.send("status");
		String res = this.getSegments(1);
		return res;
	}

	private void purge(int time)
	{
		U.sleep(time);
		while (this.input.hasNextSegment())
			U.d("Purging : " + this.input.getNextSegment(), 4);
	}

	private void send(String command)
	{
		U.d("Sending command \"" + command + "\"", 3);
		try
		{
			if (!this.port.writeString(command + "~"))
				U.e("ERROR WRITING COMMAND");
		} catch (SerialPortException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event)
	{
		// If data is available
		if (event.isRXCHAR())
			try
			{
				byte[] buffer = this.port.readBytes();
				if (buffer != null)
					this.input.push(buffer);
			} catch (SerialPortException ex)
			{
				System.out.println(ex);
			}
	}

	public void setListener(DueListener listener)
	{
		this.listener = listener;
	}

	public void startDataStream()
	{
		this.send("start");
		this.send("hex");
		U.d(this.getSegments(3), 2);
		this.dataStreaming = true;
		Thread piper = new Thread(() -> {
			while (this.dataStreaming)
				if (this.listener != null)
					if (this.input.hasNextSegment())
					{
						String nextSeg = this.input.getNextSegment();
						if (nextSeg.trim() != "")
							this.listener.acceptData(nextSeg);
					}
		});
		piper.setDaemon(true);
		piper.start();
	}

	public void stopDataStream()
	{
		this.send("stop");
		this.dataStreaming = false;
		this.purge(500);
	}
}
