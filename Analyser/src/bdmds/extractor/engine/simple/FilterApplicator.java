package bdmds.extractor.engine.simple;

import bdmds.core.storage.DataChannel;

@FunctionalInterface
public interface FilterApplicator
{
	public DataChannel apply(DataChannel data, Params params);
}
