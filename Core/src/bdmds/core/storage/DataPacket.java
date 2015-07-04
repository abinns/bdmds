package bdmds.core.storage;

import java.io.Serializable;
import java.util.Arrays;

import bdmds.core.backend.U;

public class DataPacket implements Serializable
{
	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	private static final int	CHANNEL_COUNT		= 8;
	private int[]				data;

	private String				channel;

	private DataPacket(int[] data, String channel)
	{
		this.data = Arrays.copyOf(data, data.length);
		this.channel = channel;
	}

	public DataPacket(String input) throws NumberFormatException
	{
		U.d(input, 4);
		// Split the incoming string every 6 places, for 24-bit hex decoding
		String[] data = U.splitStringEvery(input, 6);
		if (!data[0].equalsIgnoreCase("C00000"))
		{
			U.e("Error decoding input string " + input);
			throw new NumberFormatException("Error decoding input string " + input);
		}

		this.data = new int[DataPacket.CHANNEL_COUNT];

		for (int i = 1; i < data.length; i++)
			this.data[i - 1] = Integer.valueOf(data[i], 16);
	}

	public DataPacket copy()
	{
		return new DataPacket(this.data, this.channel);
	}

	public String getChannel()
	{
		return this.channel;
	}

	public int[] getData()
	{
		return this.data;
	}

	public DataPacket setChannel(String channel)
	{
		this.channel = channel;
		return this;
	}

	@Override
	public String toString()
	{
		StringBuilder res = new StringBuilder("," + this.channel);
		for (int cur : this.data)
			res.append("," + cur);
		return res.toString();
	}

}
