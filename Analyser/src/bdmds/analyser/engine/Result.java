package bdmds.analyser.engine;

import java.util.HashMap;

public class Result
{
	private HashMap<String, Object>	results	= new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	public <T> T getRes(String name)
	{
		return (T) this.results.get(name);
	}

	public <T> void setRes(String name, T in)
	{
		this.results.put(name, in);
	}

	@Override
	public String toString()
	{
		return this.results.toString();
	}
}
