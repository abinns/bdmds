package bdmds.extractor.engine.simple;

import java.util.HashMap;

public class Params
{
	private HashMap<String, Object>	params	= new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	public <T> T getParam(String name)
	{
		return (T) this.params.get(name);
	}

	public <T> void setParam(String name, T in)
	{
		this.params.put(name, in);
	}

	@Override
	public String toString()
	{
		return this.params.toString();
	}
}
