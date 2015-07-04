package bdmds.extractor.engine.simple;

import bdmds.core.storage.DataChannel;

@FunctionalInterface
public interface FeatureExtractor
{
	public double[] extractFrom(DataChannel data, Params params);
}
