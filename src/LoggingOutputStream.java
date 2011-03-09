import java.io.ByteArrayOutputStream; 
import java.io.IOException; 
import java.io.PrintStream;

interface LineHandler
{
	public void handleLine(String s);
}

/** 
 * An OutputStream that writes contents to a LineHandler upon each call to flush() 
 */ 
class LineHandlerOutputStream extends ByteArrayOutputStream { 

	private LineHandler lineHandler; 

	public static void redirectStd(LineHandler lh) {
	    // preserve old stdout/stderr streams      
//	    PrintStream stdout = System.out;                                       
//	    PrintStream stderr = System.err;                                       
	
	    // rebind stdout/stderr to logger                                  
	    LineHandlerOutputStream lhos;                                               
	    lhos = new LineHandlerOutputStream(lh);          
	    System.setOut(new PrintStream(lhos, true));
	    
	    lhos = new LineHandlerOutputStream(lh);           
	    System.setErr(new PrintStream(lhos, true)); 		
	}
	
	/** 
	 * Constructor 
	 * @param lineHandler LineHandler to write to 
	 */ 
	public LineHandlerOutputStream(LineHandler lineHandler) { 
		super(); 
		this.lineHandler = lineHandler; 
	} 

	/** 
	 * upon flush() write the existing contents of the OutputStream to the lineHandler 
	 * @throws java.io.IOException in case of error 
	 */ 
	public void flush() throws IOException { 

		String text; 
		synchronized(this) { 
			super.flush(); 
			text = this.toString(); 
			super.reset(); 

			if (text.length() == 0) { 
				return; 
			}

			lineHandler.handleLine(text); 
		} 
	} 
}
