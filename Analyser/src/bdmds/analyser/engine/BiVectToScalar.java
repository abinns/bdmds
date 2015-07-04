package bdmds.analyser.engine;

@FunctionalInterface
public interface BiVectToScalar
{
	public double apply(double[] A, double[] B);
}
