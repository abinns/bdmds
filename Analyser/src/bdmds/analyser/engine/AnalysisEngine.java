package bdmds.analyser.engine;

import java.util.HashMap;
import java.util.PriorityQueue;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import bdmds.core.backend.SortablePair;
import bdmds.core.backend.U;

public class AnalysisEngine
{
	private static void initAnalysers()
	{
		AnalysisEngine.analysisHandlers.put("Diffusion Map", (data) -> {

			Result evd = AnalysisEngine.runAnalysis("Eigenvalue Decomposition", data);
			double[][] D = evd.getRes("Matrix D");
			double[][] V = evd.getRes("Matrix V");
			Result res = new Result();
			PriorityQueue<SortablePair> values = new PriorityQueue<SortablePair>();
			for (int i = 0; i < D.length; i++)
				values.add(new SortablePair(D[i][i], i));
			int a = (int) values.poll().getVal();
			int b = (int) values.poll().getVal();
			int c = (int) values.poll().getVal();

			res.setRes("X Coords", V[a]);
			res.setRes("Y Coords", V[b]);
			res.setRes("Z Coords", V[c]);
			U.p(V[a]);
			U.p(V[b]);
			U.p(V[c]);
			return res;
		});

		AnalysisEngine.analysisHandlers.put("Markov Matrix", (data) -> {
			BiVectToScalar eculideanDistance = (A, B) -> {
				double sum = 0;
				double t = 0;
				for (int i = 0; i < A.length; i++)
				{
					t = A[i] - B[i];
					sum += t * t;
				}
				return Math.sqrt(sum);
			};
			double maxDist = Double.MIN_VALUE;
			double[][] markov = new double[data.length][data.length];
			for (int x = 0; x < markov.length; x++)
				for (int y = 0; y < markov[0].length; y++)
				{
					markov[x][y] = eculideanDistance.apply(data[x], data[y]);
					if (maxDist < markov[x][y])
						maxDist = markov[x][y];
				}
			Result res = new Result();
			for (int x = 0; x < markov.length; x++)
				for (int y = 0; y < markov[0].length; y++)
					markov[x][y] = 1.0 - markov[x][y] / maxDist;
			res.setRes("Result Matrix", markov);
			return res;
		});
		AnalysisEngine.analysisHandlers.put("Eigenvalue Decomposition", (data) -> {
			Matrix mat = new Matrix(data);
			EigenvalueDecomposition decomp = new EigenvalueDecomposition(mat);
			Result res = new Result();
			res.setRes("Matrix D", decomp.getD().getArrayCopy());
			res.setRes("Matrix V", decomp.getV().getArrayCopy());
			return res;
		});
	}

	public static Result runAnalysis(String name, double[][] input)
	{
		return AnalysisEngine.analysisHandlers.get(name).applyAnalysis(input);
	}

	private static HashMap<String, Analyser>	analysisHandlers;

	static
	{
		AnalysisEngine.analysisHandlers = new HashMap<String, Analyser>();
		AnalysisEngine.initAnalysers();
	}
}
