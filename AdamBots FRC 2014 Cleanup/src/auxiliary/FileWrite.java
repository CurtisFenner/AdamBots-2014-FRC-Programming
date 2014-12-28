/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package auxiliary;

import com.sun.squawk.microedition.io.FileConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;

/**
 *
 * @author Nathan
 */
public class FileWrite {

	/**
	 * Writes a string to a file, overwriting current contents.
	 * @param filename The file name. Writes directly to the  ftp://10.2.45.2/
	 * directory.
	 * @param contents The contents of the file. The character encoding ought
	 * to be ASCII, however, we have experience quirks with encoding before.
	 */
	public static void writeFile(String filename, String contents) {
		try {
			FileConnection file = (FileConnection) Connector.open("file:///" + filename, Connector.WRITE);
			file.create();
			DataOutputStream stream = file.openDataOutputStream();
			stream.writeChars(contents);
			stream.flush();
			stream.close();
			file.close();
		} catch (IOException exception) {
			System.out.println("writeFile(): " + exception.getMessage());
		}
	}
}
