package bdmds.recorder.display;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import bdmds.core.storage.DataPacket;

public class ViewableData implements Updateable, Renderable
{
	private DataPacket	recent;
	private int			hisLen;
	private int[][]		data;
	private int[]		pointers;
	private int			winWidth;
	private int			hisHeight;

	public ViewableData(int width, int height, int perRow)
	{
		this.hisLen = width / perRow;
		this.hisHeight = height / (8 / perRow);
		this.winWidth = width;
		this.data = new int[8][this.hisLen];
		this.pointers = new int[8];
		for (int[] cur : this.data)
			for (int i = 0; i < cur.length; i++)
				cur[i] = 0;
		for (int i = 0; i < this.pointers.length; i++)
			this.pointers[i] = 0;
	}

	private void incPointers()
	{
		for (int i = 0; i < this.pointers.length; i++)
		{
			this.pointers[i]++;
			if (this.pointers[i] >= this.hisLen)
				this.pointers[i] = 0;
		}
	}

	public void offer(DataPacket p)
	{
		this.recent = p;
	}

	private void put(DataPacket p)
	{
		int i = 0;
		for (int[] cur : this.data)
			cur[this.pointers[i]] = p.getData()[i++];
	}

	@Override
	public void render(GameContainer gc, Graphics g)
	{
		float x = 0;
		float y = 0;
		g.setColor(Color.white);
		for (int[] cur : this.data)
		{
			this.show(cur, x, y, g);
			x += this.hisLen;
			if (x >= this.winWidth)
			{
				y += this.hisHeight;
				x = 0;
			}
		}
	}

	private float scale(int val, int range)
	{
		return (float) val / range * this.hisHeight;
	}

	private void show(int[] cur, float x, float y, Graphics g)
	{
		int max = 0;
		int min = 16777216;
		for (int val : cur)
		{
			if (val > max)
				max = val;
			if (val < min)
				min = val;
		}
		int range = max - min;
		for (int val : cur)
			g.drawRect(x++, y + this.scale(val - min, range), 1, 1);
	}

	@Override
	public void update(GameContainer gc, int i)
	{
		if (this.recent == null)
			return;
		DataPacket cur = this.recent.copy();
		this.put(cur);
		this.incPointers();
	}
}
