package bdmds.recorder.serial;

@FunctionalInterface
public interface DueListener
{
	public void acceptData(String line);
}
