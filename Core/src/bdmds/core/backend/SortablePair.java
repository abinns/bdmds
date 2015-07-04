package bdmds.core.backend;

public class SortablePair implements Comparable<SortablePair>
{
	private double	sort;
	private double	val;

	public SortablePair(double sort, double val)
	{
		this.sort = sort;
		this.val = val;
	}

	@Override
	public int compareTo(SortablePair o)
	{
		return (int) Math.signum(o.sort - this.sort);
	}

	public double getLeft()
	{
		return this.sort;
	}

	public double getRight()
	{
		return this.val;
	}

	public double getSort()
	{
		return this.sort;
	}

	public double getVal()
	{
		return this.val;
	}

	@Override
	public String toString()
	{
		return this.sort + " - " + this.val;
	}
}
