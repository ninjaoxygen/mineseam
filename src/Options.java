
public class Options {
	
	public boolean doBackup = true;
	public boolean mapBefore = true;
	public boolean mapAfter = true;
	static Options o;
	
	public static Options getOptions() {
		if (o == null) {
			o = new Options();
		}
		return o;
	}
	
}
