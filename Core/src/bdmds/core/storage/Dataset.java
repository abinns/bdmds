package bdmds.core.storage;

import java.sql.SQLException;
import java.util.LinkedList;

public class Dataset
{
	private String			name;
	private String			starttime;
	private String			ident;
	private int				datacount;
	private DataChannel[]	res	= null;

	public Dataset(String name, String starttime, String identifier, int datacount)
	{
		this.name = name;
		this.starttime = starttime;
		this.ident = identifier;
		this.datacount = datacount;
	}

	public DataChannel[] getData() throws SQLException
	{
		if (this.res == null)
		{
			LinkedList<DataPacket[]> data = DbUtil.getAllData(this.ident);
			this.res = new DataChannel[data.getFirst()[0].getData().length];
			int len = 0;
			for (DataPacket[] cur : data)
				len += cur.length;
			for (int i = 0; i < this.res.length; i++)
				this.res[i] = new DataChannel(len);
			int j = 0;
			for (DataPacket[] curArr : data)
				for (DataPacket curPacket : curArr)
				{
					for (int i = 0; i < this.res.length; i++)
						this.res[i].asArray()[j] = curPacket.getData()[i];
					j++;
				}
		}
		return this.res;
	}

	public int getDatacount()
	{
		return this.datacount;
	}

	public String getIdent()
	{
		return this.ident;
	}

	public String getName()
	{
		return this.name;
	}

	public String getStarttime()
	{
		return this.starttime;
	}

}
