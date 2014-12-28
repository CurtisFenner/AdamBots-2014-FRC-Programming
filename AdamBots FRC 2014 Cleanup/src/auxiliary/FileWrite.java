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

	public static void writeFile(String name, String contents) {
		try {
			FileConnection file = (FileConnection) Connector.open("file:///" + name, Connector.WRITE);
			file.create();
			DataOutputStream stream = file.openDataOutputStream();
			stream.writeChars(contents);
			stream.flush();
			stream.close();
			file.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
