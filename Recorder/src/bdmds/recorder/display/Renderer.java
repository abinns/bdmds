package bdmds.recorder.display;

import java.util.LinkedList;

import org.newdawn.slick.Game;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class Renderer implements Game
{

	private CloseHandler			onclose;
	private LinkedList<Updateable>	updateables;
	private LinkedList<Renderable>	renderables;

	public Renderer(CloseHandler onclose, ViewableData data)
	{
		this.updateables = new LinkedList<Updateable>();
		this.renderables = new LinkedList<Renderable>();
		this.onclose = onclose;
		this.renderables.add(data);
		this.updateables.add(data);
	}

	@Override
	public boolean closeRequested()
	{
		this.onclose.close();
		return true;
	}

	@Override
	public String getTitle()
	{
		return "BDMDS Recorder Data Viewer";
	}

	@Override
	public void init(GameContainer arg0) throws SlickException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException
	{
		for (Renderable r : this.renderables)
			r.render(gc, g);
	}

	@Override
	public void update(GameContainer gc, int i) throws SlickException
	{
		for (Updateable u : this.updateables)
			u.update(gc, i);
	}

}
