package bdmds.recorder.parsing;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import bdmds.core.backend.U;

/**
 * Scanner replacement, handles parsing input serial data, splitting into '~'
 * delimited segments.
 *
 * @author Andrew Binns
 */
public class ByteBuffer
{
	private static final String				SPLIT_STR		= "~";
	private static final char				SPLIT			= ByteBuffer.SPLIT_STR.charAt(0);
	private ConcurrentLinkedQueue<byte[]>	queue;
	private boolean							hasNextSegment	= false;

	private String							leftover		= "";

	/**
	 * Parses incoming byte arrays into '~' delimited segments.
	 */
	public ByteBuffer()
	{
		this.queue = new ConcurrentLinkedQueue<byte[]>();
	}

	/**
	 * gets all current leftovers.
	 *
	 * @return
	 */
	public String flush()
	{
		String res = this.leftover;
		this.leftover = "";
		return res;
	}

	/**
	 * Gets the next segment from the serial interface.
	 *
	 * @return the next segment
	 */
	public String getNextSegment()
	{
		U.d("Checking if we have a next line...", 6);
		if (!this.hasNextSegment)
			throw new NoSuchElementException();
		this.hasNextSegment = false;
		U.d("leftovers, \"" + this.leftover + "\"...", 6);
		if (this.leftover.contains(ByteBuffer.SPLIT_STR))
		{
			int splitPoint = this.leftover.indexOf(ByteBuffer.SPLIT);
			String res = this.leftover.substring(0, splitPoint);
			this.leftover = this.leftover.substring(splitPoint + 1, this.leftover.length());
			this.hasNextSegment = this.leftover.contains(ByteBuffer.SPLIT_STR);
			U.d("Returning1 " + res, 5);
			return res;
		}

		boolean done = false;
		StringBuilder nextSeg = new StringBuilder("");
		nextSeg.append(this.leftover);
		this.leftover = "";
		U.d("Done with leftovers, now looping...", 6);
		while (!done && !this.queue.isEmpty())
			if (!new String(this.queue.peek()).contains(ByteBuffer.SPLIT_STR))
				nextSeg.append(new String(this.queue.poll()));
			else
			{
				done = true;
				String cur = new String(this.queue.poll());
				int splitPoint = cur.indexOf(ByteBuffer.SPLIT);
				this.leftover = cur.substring(splitPoint + 1);
				nextSeg.append(cur.substring(0, splitPoint));
			}
		U.d("Returning2 next seg, " + nextSeg.toString(), 5);
		return nextSeg.toString();
	}

	public boolean hasNextSegment()
	{
		return this.hasNextSegment;
	}

	public void push(byte[] b)
	{
		for (Byte c : b)
			// Commented out for speed
			// U.d("Curr byte: " + (char) (c & 0xFF), 8);
			if (c == 126)
				this.hasNextSegment = true;
		U.d("Currently status of nextline: " + this.hasNextSegment, 6);
		this.queue.add(b);
	}
}
