package bdmds.core.storage;

import java.util.Arrays;

import bdmds.core.storage.simple.DataSizeMismatchError;
import bdmds.core.storage.simple.Generator;

public class DataChannel
{
	private double[]	data;

	/**
	 * Initializes this data element to match the size of another data element.
	 * NOTE: DOES NOT MATCH DATA
	 *
	 * @param data
	 */
	public DataChannel(DataChannel other)
	{
		this.data = new double[other.data.length];
	}

	public DataChannel(double[] input)
	{
		this.data = input;
	}

	public DataChannel(Generator g, int size)
	{
		this.data = new double[size];
		for (int i = 0; i < size; i++)
			this.data[i] = g.gen(i);
	}

	public DataChannel(int len)
	{
		this.data = new double[len];
	}

	public double[] asArray()
	{
		return this.data;
	}

	// TODO Make this useful, makes itself from dataset ident perhaps, from
	// provided data, etc, etc, etc

	public DataChannel copy()
	{
		return new DataChannel(Arrays.copyOf(this.data, this.data.length));
	}

	/**
	 * Returns a new Data object with the result of 'this dataset' - 'other
	 * dataset' as its data. Requires provided data to be same size as current
	 * dataset.
	 *
	 * @param other
	 *            the data to be subtracted from this one's data
	 * @return the subtraction of the two datasets
	 * @throws DataSizeMismatchError
	 *             provided data is of the wrong size
	 */
	public DataChannel subtract(DataChannel other) throws DataSizeMismatchError
	{
		if (other.asArray().length != this.data.length)
			throw new DataSizeMismatchError();
		DataChannel res = new DataChannel(this);
		int i = 0;
		for (double d : this.data)
			res.asArray()[i] = d - other.asArray()[i++];
		return res;
	}

	@Override
	public String toString()
	{
		return Arrays.toString(this.data);
	}
}
