import java.sql.SQLException;

import bdmds.analyser.engine.AnalysisEngine;
import bdmds.analyser.engine.Result;
import bdmds.core.backend.U;
import bdmds.core.storage.DataChannel;
import bdmds.core.storage.DbUtil;
import bdmds.extractor.engine.ExtractorEngine;

public class Driver
{
	public static void main(String... CHEESE) throws SQLException
	{
		// for (Dataset d : DbUtil.getDatasets())
		// U.p(d.getName() + " with ident " + d.getIdent() + " has " +
		// d.getDatacount() + " rows from time " + d.getStarttime());
		// double[] input = new double[] { 0, 0.2, 0.2, 0.2, 0.4, 0.5, 0.6, 0.7,
		// 0.8, 0.9, 1};

		double[][] val = new double[][]
		{
		{ 1, 2, 33 },
		{ 2, 13, 6 },
		{ 33, 6, 4 } };
		// double[] res =
		// ExtractorEngine.extractFeature("Peak Frequency Finder", new
		// DataChannel(input), null);
		// double[] res = ExtractorEngine.runFilter("DC Offset", new
		// DataChannel(input), null).asArray();

		// double[][] res = AnalysisEngine.runAnalysis("Markov Matrix",
		// input).getRes("Result Matrix");

		Result evd = AnalysisEngine.runAnalysis("Eigenvalue Decomposition", val);
		double[][] D = evd.getRes("Matrix D");
		double[][] V = evd.getRes("Matrix V");

		U.p(D[0]);
		U.p(D[1]);
		U.p(D[2]);
		U.p(V[0]);
		U.p(V[1]);
		U.p(V[2]);

		if (true)
			return;

		U.p("Retrieving dataset...");
		DataChannel[] input = DbUtil.getDatasets().getLast().getData();

		U.p("Running Butterworth Filter");
		for (int i = 0; i < 6; i++)
			input[i] = ExtractorEngine.runFilter("Butter", input[i], null);
		U.p("Running DC Offset");
		for (int i = 0; i < 6; i++)
			input[i] = ExtractorEngine.runFilter("DC Offset", input[i], null);

		double[][] features = new double[6][];
		U.p("Running Peak Freq Finder");
		for (int i = 0; i < 6; i++)
			features[i] = ExtractorEngine.extractFeature("Peak Frequency Finder", input[i], null);
		U.p("Getting Markov Matrix");
		double[][] markov = AnalysisEngine.runAnalysis("Markov Matrix", features).getRes("Result Matrix");
		U.p("Doing diffusion map...");
		Result evd1 = AnalysisEngine.runAnalysis("Diffusion Map", markov);
	}
}
