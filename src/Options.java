
public class Options {
	
	public boolean doBackup = true;
	public boolean doModify = true;
	public boolean mapBefore = false;
	public boolean mapAfter = false;
	
	private static Options o;
	
	public static Options getOptions() {
		if (o == null) {
			o = new Options();
		}
		return o;
	}
	
}
