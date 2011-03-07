import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ZipWorld {
	public static void doZip(String savesDir, String worldToZip) throws Exception {
		String dateString;
		
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'_'HH.mm.ss");
        dateString = sdf.format( now );
        
	    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(savesDir + "/" + worldToZip + "_" + dateString + ".zip")); 
	    zipDir(savesDir, worldToZip, zos); 
	    zos.close(); 
	}
	
	public static void zipDir(String dirRoot, String dir2zip, ZipOutputStream zos) throws Exception 
	{ 
        //create a new File object based on the directory we have to zip File    
    	File zipDir = new File(dirRoot + "/" + dir2zip);
    	
        //get a listing of the directory content 
        String[] dirList = zipDir.list();
        
        byte[] readBuffer = new byte[8192]; 
        int bytesIn = 0; 
        //loop through dirList, and zip the files 
        for(int i=0; i<dirList.length; i++) { 
            File f = new File(zipDir, dirList[i]);
            
	        if(f.isDirectory()) 
	        {
	        	// recurse
	            zipDir(dirRoot, dir2zip + "/" + f.getName(), zos); 
	        } else { // is a file
	            FileInputStream fis = new FileInputStream(f);
	            
	            // create a new zip entry 
	            ZipEntry anEntry = new ZipEntry(dir2zip + "/" + f.getName());
	            
	            //place the zip entry in the ZipOutputStream object 
	            zos.putNextEntry(anEntry);
	            
	            //now write the content of the file to the ZipOutputStream 
	            while((bytesIn = fis.read(readBuffer)) != -1) 
	            { 
	                zos.write(readBuffer, 0, bytesIn); 
	            }
	            
				//close the Stream 
				fis.close(); 
	        }
	    } 
	}
}
