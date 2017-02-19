package webchatinterface.client.ui.dialog;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import webchatinterface.AbstractIRC;
import webchatinterface.client.util.FrameUtilities;
import webchatinterface.client.util.ResourceLoader;

/**@author Brandon Richardson
 *@version 1.4.3
 *@since 06/05/2016
 *<p>
 *The AboutApplicationDialog class is designed to display a small dialog to show information about
 *the client application.
 */

public class AboutApplicationDialog
{
	private static JFrame frame;
	
	/**Displays the "About" dialog in a new window. The dialog contains the name of the author of
	  *the application, the build version, the release date and a short description of the purpose
	  *of the application.*/
	public static void showAboutDialog()
	{
		AboutApplicationDialog.frame = new JFrame("About Web Chat Interface");
		FrameUtilities.setFrameIcon(AboutApplicationDialog.frame, ResourceLoader.getInstance().getFrameIcon());
		AboutApplicationDialog.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		AboutApplicationDialog.frame.setSize(500,300);  
		AboutApplicationDialog.frame.setResizable(false);
		AboutApplicationDialog.frame.setVisible(false);
		AboutApplicationDialog.buildDialog();
		AboutApplicationDialog.frame.setVisible(true);
	}
	
	private static void buildDialog()
	{
		JTextArea textArea = new JTextArea();
	    textArea.setWrapStyleWord(true);
	    textArea.setLineWrap(true);
	    textArea.setOpaque(false);
	    textArea.setEditable(false);
	    textArea.setFocusable(false);
	    textArea.setText(AboutApplicationDialog.buildAboutString());
	    
	    AboutApplicationDialog.frame.getContentPane().add(textArea);
	    AboutApplicationDialog.frame.getContentPane().validate();
	}
	
	private static String buildAboutString()
	{
		String aboutText = "";
	    
		aboutText += "Author: " + AbstractIRC.AUTHOR + "\n";
	    aboutText += "Build Version: " + AbstractIRC.CLIENT_VERSION + "\n";
	    aboutText += "Release Date: " + AbstractIRC.RELEASE_DATE + "\n\n";
	    
	    aboutText += "Web Chat Interface is an application used for communicating with a group of " 
	    		+ "people in single or private chatrooms. Users are able to share images and files "
	    		+ "among themselves and communicate in a private secured manner." + "\n\n"
	    		+ "Messages that are sent from a client are never stored or cached by the server, "
	    		+ "keeping communication private and untraceable.";
	    
	    return aboutText;
	}
}
