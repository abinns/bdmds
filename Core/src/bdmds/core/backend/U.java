package bdmds.core.backend;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Scanner;

/**
 * Utility class, handles printing output nicely, as well as multiple levels of
 * debug logging.
 *
 * @author Andrew Binns
 */
public class U
{
	/**
	 * Calls the specified method with the specified parameters, checking to
	 * make sure passed arguments match required ones before attempting an
	 * invocation.
	 *
	 * @param <T>
	 * @param method
	 *            is the method which is being attempted to be invoked.
	 * @param input
	 *            is a var-args (any number of options, or an array) for the
	 *            parameters.
	 * @throws InvalidParameterException
	 *             if the passed options don't match with the arguments required
	 *             by the method.
	 */

	@SuppressWarnings("unchecked")
	public static <T> T carefulCall(Method method, Object target, Object... params) throws InvalidParameterException
	{
		Object res = null;
		// int i = 0;
		// for (Class<?> c : method.getParameterTypes())
		// if (!c.equals(U.getUnBoxedType(params[i++].getClass())))
		// U.w("Input item " + params[i - 1].getClass().getName() +
		// " may be incompatible with " + c.getName() + " for method " +
		// method.getName());

		try
		{
			res = method.invoke(target, params);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			U.e("Error, could not properly call method " + method.getName(), e);
		}
		return (T) res;
	}

	/**
	 * Casting wrapper, centralizes the "unchecked" warning needs.
	 *
	 * @param cur
	 *            the object to cast
	 * @return the object casted to the specified type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cleanCast(Object cur)
	{
		return (T) cur;
	}

	/**
	 * Simple confirmation function, returns a true or false in response to the
	 * passed message.
	 *
	 * @param message
	 *            the question to ask the person.
	 * @return true or false depending on the user response.
	 */
	public static boolean confirm(String message)
	{
		U.p(message);
		String in = U.input.next();
		while (!in.equalsIgnoreCase("yes") && !in.equalsIgnoreCase("y") && !in.equalsIgnoreCase("no") && !in.equalsIgnoreCase("n"))
		{
			U.p("Invalid response, please input 'yes' or 'no'");
			in = U.input.next();
		}
		return in.equalsIgnoreCase("yes") || in.equalsIgnoreCase("y");
	}

	/**
	 * Prints out simple integers as strings, with the appropriate debug level.
	 *
	 * @param in
	 *            the int to print out
	 * @param level
	 *            the debugging level to use
	 */
	public static void d(int in, int level)
	{
		U.d(in + "", level);
	}

	/**
	 * Prints out a general object as a string, with the appropriate debug
	 * level.
	 *
	 * @param in
	 *            the object to print out.
	 * @param level
	 *            the debugging level to use
	 */
	public static void d(Object in, int level)
	{
		U.d(in.toString(), level);
	}

	/**
	 * Prints the specified string if the debug level specified is greater or
	 * equal to the current debugging level specified in this class.
	 *
	 * @param in
	 *            the string to print.
	 * @param level
	 *            the debugging level to print this message out at.
	 */
	public static void d(String in, int level)
	{
		if (U.debugging >= level)
			U.printWithTag(in, "DEBUG");
	}

	/**
	 * Takes a character and count, and returns a string which is that character
	 * duplicated count times.
	 *
	 * @param count
	 *            the number of duplications
	 * @param c
	 *            the character to duplicate
	 * @return a string made up of the duplicated characters
	 */
	public static String dupChar(int count, char c)
	{
		char[] res = new char[count];
		for (int i = 0; i < count; i++)
			res[i] = c;
		return new String(res);
	}

	/**
	 * Prints this string as an error
	 *
	 * @param in
	 *            the string to print
	 */
	public static void e(String in)
	{
		U.printWithTag(in, "ERROR");
	}

	/**
	 * Prints the specified string and exception, for error logging.
	 *
	 * @param in
	 *            the string to print
	 * @param E
	 *            the exception to also print.
	 */
	public static void e(String in, Exception E)
	{
		U.printWithTag(in + " - " + E, "ERROR");
		E.printStackTrace();
	}

	/**
	 * Prints the specified string and exception, for error logging.
	 *
	 * @param in
	 *            the string to print
	 * @param E
	 *            the exception to also print.
	 */
	public static void e(String in, Throwable E)
	{
		U.printWithTag(in + " - " + E, "ERROR");
	}

	public static String getTimestamp()
	{
		return U.timeStampFormatter.format(Calendar.getInstance().getTime());
	}

	/**
	 * <p>
	 * Returns the unboxed version of this class if this is boxed.
	 * </p>
	 * <p>
	 * Example: given Double.class this will return double.class
	 * </p>
	 * <p>
	 * Returns input if the passed type isn't a wrapper type.
	 *
	 * @param input
	 *            the type to unbox
	 * @return the unboxed type
	 */
	public static Class<?> getUnBoxedType(Class<?> input)
	{
		if (U.WRAPPER_TYPES.containsKey(input))
			return U.WRAPPER_TYPES.get(input);
		return input;
	}

	private static LinkedHashMap<Class<?>, Class<?>> getWrapperTypes()
	{
		LinkedHashMap<Class<?>, Class<?>> ret = new LinkedHashMap<Class<?>, Class<?>>();
		ret.put(Boolean.class, boolean.class);
		ret.put(Character.class, char.class);
		ret.put(Byte.class, byte.class);
		ret.put(Short.class, short.class);
		ret.put(Integer.class, int.class);
		ret.put(Long.class, long.class);
		ret.put(Float.class, float.class);
		ret.put(Double.class, double.class);
		ret.put(Void.class, void.class);
		return ret;
	}

	/**
	 * Checks if the passed class is a wrapper type (Double, Integer, etc)
	 *
	 * @param clazz
	 *            the class to check
	 * @return true of this is a wrapper class, false otherwise.
	 */
	public static boolean isWrapperType(Class<?> clazz)
	{
		return U.WRAPPER_TYPES.containsKey(clazz);
	}

	public static void p(double[] arr)
	{
		U.p(Arrays.toString(arr));
	}

	public static void p(double[][] arr)
	{
		int i;
		StringBuilder[] grid = new StringBuilder[arr[0].length];
		for (i = 0; i < grid.length; i++)
			grid[i] = new StringBuilder();
		for (double[] col : arr)
		{
			i = 0;
			for (double cur : col)
				grid[i++].append("," + cur);
		}
		for (StringBuilder cur : grid)
			cur.deleteCharAt(0);
		StringBuilder res = new StringBuilder("\n");
		for (StringBuilder cur : grid)
		{
			res.append(cur);
			res.append('\n');
		}
		U.p(res.toString());
	}

	public static void p(float[][] arr)
	{
		int i;
		StringBuilder[] grid = new StringBuilder[arr[0].length];
		for (i = 0; i < grid.length; i++)
			grid[i] = new StringBuilder();
		for (float[] col : arr)
		{
			i = 0;
			for (float cur : col)
				grid[i++].append("," + cur);
		}
		for (StringBuilder cur : grid)
			cur.deleteCharAt(0);
		StringBuilder res = new StringBuilder("\n");
		for (StringBuilder cur : grid)
		{
			res.append(cur);
			res.append('\n');
		}
		U.p(res.toString());
	}

	/**
	 * Prints the specified integer as simple output
	 *
	 * @param in
	 *            the score to print out.
	 */
	public static void p(int in)
	{
		U.p(in + "");

	}

	/**
	 * Prints the specified object as general output.
	 *
	 * @param in
	 *            the object to print.
	 */
	public static void p(Object in)
	{
		U.p(in.toString());
	}

	/**
	 * Prints the specified string as general output.
	 *
	 * @param in
	 *            the string to print.
	 */
	public static void p(String in)
	{
		U.printWithTag(in, "OUTPUT");
	}

	/**
	 * A private method which prints the specified message to the output
	 * printstream, with the specified string as a tag, as well as the current
	 * date and time.
	 *
	 * @param in
	 *            the string to print
	 * @param tag
	 *            the string to tag it with
	 */
	private static void printWithTag(String in, String tag)
	{
		StringBuilder res = new StringBuilder();
		res.append(U.outputFormatter.format(Calendar.getInstance().getTime()));
		res.append("[");
		res.append(tag);
		res.append("] ");
		res.append(in);
		U.output.println(res.toString().trim());
	}

	/**
	 * Sets the debugging level to something new. Used so that the debugging
	 * level is set in the main, and not forgotten about...
	 *
	 * @param newLevel
	 *            the new debuglevel to use.
	 */
	public static void setDebugLevel(int newLevel)
	{
		U.debugging = newLevel;
	}

	/**
	 * A helper method which cleans up code, handles the try-catch so the
	 * calling method doesn't have to.
	 *
	 * @param millis
	 *            the number of milliseconds to sleep.
	 */
	public static void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		} catch (InterruptedException e)
		{
			U.e("Error sleeping", e);
		}
	}

	/**
	 * Splits the given string every nth element and returns the elements in an
	 * array.
	 *
	 * @param input
	 *            the string to split
	 * @param interval
	 *            how often to split it
	 * @return an array with the the elements after the split
	 */
	public static String[] splitStringEvery(String input, int interval)
	{
		int arrayLength = (int) Math.ceil(input.length() / (double) interval);
		String[] result = new String[arrayLength];

		int j = 0;
		int lastIndex = result.length - 1;
		for (int i = 0; i < lastIndex; i++, j += interval)
			result[i] = input.substring(j, j + interval);

		// Add the last bit
		result[lastIndex] = input.substring(j);

		return result;
	}

	/**
	 * Tries to close the passed stream, if it is not null. If it encounters an
	 * exception, it logs it and continues.
	 *
	 * @param message
	 *            the message to log on IO exception
	 * @param stream
	 *            the stream to try and close
	 */

	public static void tryCloseStream(String message, Closeable stream)
	{
		if (stream != null)
			try
			{
				stream.close();
			} catch (IOException e)
			{
				U.e(message, e);
			}
	}

	/**
	 * Prints the specified warning
	 *
	 * @param input
	 *            some warning text
	 */
	public static void w(String input)
	{
		U.printWithTag(input, "WARNING");
	}

	/**
	 * Given a file and contents, attempts to write the entire string provided
	 * to the file. By default, uses the UTF_8 charset. If the file does not
	 * exist, it creates it. If the file exists, it overwrites the contents,
	 * truncating prior to any writing.
	 *
	 * @param path
	 *            the path of the file to write to
	 * @param contents
	 *            the data to be written to the file
	 */
	public static void writeToFile(String path, String contents)
	{
		U.writeToFile(path, contents, StandardCharsets.UTF_8);
	}

	/**
	 * Given a file and contents, and Charset, attempts to write the entire
	 * string provided to the file using the specified encoding. If the file
	 * does not exist, it creates it. If the file exists, it overwrites the
	 * contents, truncating prior to any writing.
	 *
	 * @param path
	 *            the path of the file to write to
	 * @param contents
	 *            the data to be written to the file
	 * @param encoding
	 *            the string encoding method to use
	 */
	public static void writeToFile(String path, String contents, Charset encoding)
	{
		try
		{
			Files.write(Paths.get(path), contents.getBytes(encoding));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static PrintStream								output;

	private static int										debugging			= 0;

	private static SimpleDateFormat							outputFormatter		= new SimpleDateFormat("HH:mm:ss.SSS");

	private static SimpleDateFormat							timeStampFormatter	= new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss.SSS");

	private static Scanner									input;

	private static Random									rand;

	/**
	 * Static constructor, used to initialize output to the system.out
	 * printstream, could be used to init it to different things to add native
	 * support for logging to a file, etc.
	 */
	static
	{
		U.output = System.out;
		U.input = new Scanner(System.in);
		U.rand = new Random();
	}

	private static final LinkedHashMap<Class<?>, Class<?>>	WRAPPER_TYPES		= U.getWrapperTypes();

	/**
	 * Gives a random float between the specified min and max.
	 *
	 * @param min
	 *            the minimum value for the result
	 * @param max
	 *            the maximum value for the result
	 * @return a random number bewteen the specified min and max
	 */

	public float rand(float min, float max)
	{
		return U.rand.nextFloat() * (max - min) + min;
	}
}
