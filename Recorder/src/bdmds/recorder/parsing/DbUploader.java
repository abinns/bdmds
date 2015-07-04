package bdmds.recorder.parsing;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

import bdmds.core.backend.U;
import bdmds.core.storage.DataPacket;
import bdmds.core.storage.DbUtil;

/**
 * Database uploading class. Handles uploading data to the specified dataset.
 * Implemented with its own internal thread to handle incoming data.
 *
 * @author Andrew Binns
 */
public class DbUploader
{
	private ConcurrentLinkedQueue<DataPacket>	queue;
	private Thread								handler;
	private String								datasetIdent;
	private String								channel;
	private int									rowCount	= 0;

	/**
	 * Initializes a database uploader for the specified dataset. Starts its own
	 * listener, so merely offer datapackets as time goes on.
	 *
	 * @param datasetIdent
	 * @param channel
	 */
	public DbUploader(String datasetIdent, String channel)
	{
		this.datasetIdent = datasetIdent;
		this.channel = channel;
		this.queue = new ConcurrentLinkedQueue<DataPacket>();
		this.initHandler();
		this.handler.setDaemon(true);
		this.handler.start();
	}

	/**
	 * Private function that initializes the internal handler thread to help
	 * keep constructor clean.
	 */
	private void initHandler()
	{
		this.handler = new Thread(() -> {
			while (true)
			{
				if (this.queue.isEmpty())
				{
					U.sleep(100);
					continue;
				}
				int count = this.queue.size() - 1;
				DataPacket[] arr = new DataPacket[count];
				for (int i = 0; i < count; i++)
					arr[i] = this.queue.poll().setChannel(this.channel);
				if (count != 0)
					try
					{
						DbUtil.saveRows(this.datasetIdent, this.rowCount, this.rowCount + count, arr);
						this.rowCount += count;
					} catch (SQLException e)
					{
						U.e("Error writing", e);
					}
			}
		});
	}

	/**
	 * Adds a datapacket to the uploading queue.
	 *
	 * @param in
	 */
	public void offer(DataPacket in)
	{
		this.queue.offer(in);
	}

	public void updateSize()
	{
		U.p("Uploaded " + this.rowCount + " to " + this.datasetIdent);
		try
		{
			DbUtil.updateDataset(this.rowCount, this.datasetIdent);
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
