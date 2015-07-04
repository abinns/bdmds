package bdmds.extractor.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.function.Function;

import org.jtransforms.fft.DoubleFFT_1D;

import bdmds.core.backend.SortablePair;
import bdmds.core.storage.DataChannel;
import bdmds.extractor.engine.simple.FeatureExtractor;
import bdmds.extractor.engine.simple.FilterApplicator;
import bdmds.extractor.engine.simple.Params;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;

public class ExtractorEngine
{
	public static double[] extractFeature(String name, DataChannel d, Params p)
	{
		return ExtractorEngine.extractionHandlers.get(name).extractFrom(d, p);
	}

	private static void initExtractors()
	{
		ExtractorEngine.extractionHandlers.put("Peak Frequency Finder", (data, params) -> {
			double[] fft = ExtractorEngine.extractFeature("Fourier Transform", data, null);
			Params p = new Params();
			p.setParam("FreqCount", 6);
			double[] res = ExtractorEngine.extractFeature("Peak Finder", new DataChannel(fft), p);
			return res;
		});

		ExtractorEngine.extractionHandlers.put("Peak Finder", (data, params) -> {
			double[] inputData = data.asArray();
			int freqCount = params.getParam("FreqCount");
			PriorityQueue<SortablePair> rows = new PriorityQueue<SortablePair>();
			int i = 0;
			for (double cur : inputData)
				rows.offer(new SortablePair(cur, i++));

			SortablePair[] pairRes = new SortablePair[freqCount];
			LinkedList<SortablePair> bounds = new LinkedList<SortablePair>();
			for (i = 0; i < pairRes.length; i++)
			{

				SortablePair curPeak = rows.poll();

				Function<Double, Boolean> isWithinPrevThresholds = (input) -> {
					boolean isWithin = false;
					for (SortablePair cur : bounds)
						if (input > cur.getLeft() && input < cur.getRight())
							isWithin = true;
					return isWithin;
				};

				while (isWithinPrevThresholds.apply(curPeak.getSort()))
					curPeak = rows.poll();

				pairRes[i] = curPeak;

				double rLim = curPeak.getVal();
				int rPointer = 0;
				for (rPointer = (int) (curPeak.getSort() + 1); rPointer < inputData.length - 1 && rLim > inputData[rPointer]; rLim = inputData[rPointer++])
					;
				double lLim = curPeak.getVal();
				int lPointer = 0;
				for (lPointer = (int) (curPeak.getSort() - 1); lPointer < inputData.length && lPointer > 0 && lLim > inputData[lPointer]; lLim = inputData[lPointer--])
					;
				bounds.add(new SortablePair(lPointer + 1, rPointer - 1));
			}

			double[] res = new double[pairRes.length];

			for (i = 0; i < res.length; i++)
				res[i] = pairRes[i].getSort();
			return res;
		});
		ExtractorEngine.extractionHandlers.put("Fourier Transform", (data, params) -> {
			DoubleFFT_1D fftDo = new DoubleFFT_1D(data.asArray().length);
			double[] fft = new double[data.asArray().length * 2];
			System.arraycopy(data.asArray(), 0, fft, 0, data.asArray().length);
			fftDo.realForwardFull(fft);
			return fft;
		});
		ExtractorEngine.extractionHandlers.put("Waveform Segmentation", (data, params) -> {
			return null;
			/* data = DampingFilter.dampen(data, 3); */
		});

		ExtractorEngine.extractionHandlers.put("Weighted Dampen", (data, params) -> {
			double[] weights = params.getParam("weights");
			if (weights.length % 2 == 0)
				throw new IllegalArgumentException("Weight array cannot have even number of elements.");
			if (data.asArray().length < weights.length)
				throw new IllegalArgumentException("Data must be at least as big as weights.");

			double weightSum = 0;
			for (double weight : weights)
				weightSum += weight;
			for (int i = 0; i < weights.length; ++i)
				weights[i] /= weightSum;

			double[] output = new double[data.asArray().length - weights.length + 1];

			for (int i = 0; i < output.length; ++i)
			{
				double result = 0;
				for (int j = 0; j < weights.length; ++j)
					result += data.asArray()[i + j - weights.length / 2 + 1] * weights[j];
				output[i] = result;
			}

			/* Just to make it compile, make work. */
			return output;
		});
	}

	private static void initFilters()
	{
		ExtractorEngine.filterAppliers.put("Butter", (data, params) -> {
			DataChannel d = new DataChannel(data);
			IirFilterCoefficients coeffs = new IirFilterCoefficients();
			// A and B are swapped from matlab defaults, due to internal library
			// things.
				coeffs.b = new double[]
				{ 0.00210486906514907, 0, -0.00631460719544722, 0, 0.00631460719544722, 0, -0.00210486906514907 };
				coeffs.a = new double[]
				{ 1, -5.44008635033977, 12.3486561232094, -14.9759435061843, 10.2368669283708, -3.74009573785951, 0.570602543343194 };
				IirFilter filter = new IirFilter(coeffs);
				int i = 0;
				for (double cur : data.asArray())
					d.asArray()[i++] = filter.step(cur);
				return d;
			});

		ExtractorEngine.filterAppliers.put("DC Offset", (data, params) -> {
			DataChannel line = ExtractorEngine.runFilter("Linear Reduction", data, null);
			try
			{
				return data.subtract(line);
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return data;
		});

		ExtractorEngine.filterAppliers.put("Linear Reduction", (data, params) -> {
			int i = 0;

			int sumx = 0;
			double sumy = 0;
			for (double cur : data.asArray())
			{
				sumx += i++;
				sumy += cur;
			}
			double xbar = sumx / i;
			double ybar = sumy / i;

			double xxbar = 0.0, xybar = 0.0;
			for (i = 0; i < data.asArray().length; i++)
			{
				xxbar += (i - xbar) * (i - xbar);
				xybar += (i - xbar) * (data.asArray()[i] - ybar);
			}
			double m = xybar / xxbar;
			double b = ybar - m * xbar;

			return new DataChannel((x) -> {
				return m * x + b;
			}, data.asArray().length);
		});
	}

	public static DataChannel runFilter(String name, DataChannel d, Params p)
	{
		return ExtractorEngine.filterAppliers.get(name).apply(d, p);
	}

	private static HashMap<String, FeatureExtractor>	extractionHandlers;

	private static HashMap<String, FilterApplicator>	filterAppliers;

	static
	{
		ExtractorEngine.extractionHandlers = new HashMap<String, FeatureExtractor>();
		ExtractorEngine.filterAppliers = new HashMap<String, FilterApplicator>();

		ExtractorEngine.initExtractors();
		ExtractorEngine.initFilters();
	}
}
