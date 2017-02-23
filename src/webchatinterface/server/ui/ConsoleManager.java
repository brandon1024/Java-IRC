package webchatinterface.server.ui;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import util.DynamicQueue;
import webchatinterface.AbstractIRC;
import webchatinterface.server.AbstractServer;

/**@author Brandon Richardson
  *@version 1.4.3
  *@since 06/05/2016
  *<p>
  *The {@code ConsoleManager} class manages the display of messages to the console and exception 
  *logging. The purpose of the Console Manager is to prevent messages from displaying incorrectly 
  *in high volume reception situations. It also allows for the user or developer to view a log of 
  *exceptions thrown throughout the execution of the server application, which can aid in 
  *application troubleshooting.
  *<p>
  *Messages and warnings are distinguished by color in the console. The Console Manager class
  *displays warning messages in red.
  *<p>
  *The console manager extends JTextPane, so it may be used as a component in the GUI. However, the 
  *Console Manager uses a print queue to correctly print messages to the console. Therefore, using 
  *methods defined  in JTextPane are strongly discouraged.
  */

public class ConsoleManager extends JTextPane implements Runnable, AbstractIRC
{
	/**Serial Version UID is used as a version control for the class that implements
	 *the serializable interface.*/
	private static final long serialVersionUID = -2782518871912013243L;
	
	/**The queue of messages to be appended to JTextPane*/
	private DynamicQueue<ConsoleMessage> printQueue;
	
	private ArrayList<ConsoleMessage> printCache;
	
	/**The StyledDocument for the underlying JTextPane*/
	private StyledDocument doc;
	
	/**Attribute Set for generic messages*/
	private MutableAttributeSet messageAttribute;
	
	/**Attribute Set for warning messages*/
	private MutableAttributeSet warningAttribute;
	
	/**Constructs a {@code ConsoleManager} object. Constructs the underlying JTextPane 
	  *framework and print queues.*/
	public ConsoleManager()
	{
		//Build JTextPane
		super();
		super.setEditable(false);
		
		//Provide reference to JTextPane StyledDocument
		this.doc = this.getStyledDocument();
		
		this.setBackground(AbstractServer.backgroundColor);
		
		//Style Attribute Set for Generic Messages
		this.messageAttribute = new SimpleAttributeSet();
		StyleConstants.setForeground(this.messageAttribute, AbstractServer.foregroundColor);
		StyleConstants.setFontSize(this.messageAttribute, AbstractServer.textFont.getSize());
		StyleConstants.setFontFamily(this.messageAttribute, AbstractServer.textFont.getFontName());
		
		this.warningAttribute = new SimpleAttributeSet();
		StyleConstants.setForeground(this.warningAttribute, Color.red);
		StyleConstants.setFontSize(this.warningAttribute, AbstractServer.textFont.getSize());
		StyleConstants.setFontFamily(this.warningAttribute, AbstractServer.textFont.getFontName());
		
		//Build Console Print Queue
		this.printQueue = new DynamicQueue<ConsoleMessage>();
		this.printCache = new ArrayList<ConsoleMessage>();
	}
	
	/**Runs the {@code ConsoleManager}. Periodically polls the print queue 
	  *and appends any stored messages to the underlying JTextPane in a 
	  *sequential manner. Also logs messages displayed in the console
	  *to the log file via the Logger.
	  *<p>
	  *Poll time: 100ms
	  *<p>
	  *Invoked when the thread starts.
	  */
	@Override
	public void run()
	{
		while(true)
		{
			while(!this.printQueue.isEmpty())
			{
				ConsoleMessage message = this.printQueue.dequeue();
				AbstractServer.logString(message.getMessage() + "\n");
				
				try
				{
					if(message.isWarning())
					{
						this.doc.setParagraphAttributes(doc.getLength(), 1, this.warningAttribute, false);
						this.doc.insertString(this.doc.getLength(), message.getMessage() + "\n", this.warningAttribute);
					}
					else
					{
						this.doc.setParagraphAttributes(doc.getLength(), 1, this.messageAttribute, false);
						this.doc.insertString(this.doc.getLength(), message.getMessage() + "\n", this.messageAttribute);
					}
				}
				catch(BadLocationException e)
				{
					AbstractServer.logException(e);
				}
			}
			
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				AbstractServer.logException(e);
			}
		}
	}
	
	/**Enqueue a new {@code String} object as either a message or a warning.
	  *@param message the string to enqueue to the console queue
	  *@param warning true if {@code message} is a warning message to be displayed in red, false if
	  *{@code message} is a generic message to be displayed in black
	  *@see ConsoleManager#run()
	  */
	public void printConsole(String message, boolean warning)
	{
		synchronized(this)
		{
			ConsoleMessage consoleMessage = new ConsoleMessage(this.getSystemTimestamp() + ": " + message, warning);
			this.printQueue.enqueue(consoleMessage);
			this.printCache.add(consoleMessage);
		}
	}
	
	/**Enqueue a new line character.
	  *@see ConsoleManager#run()
	  */
	public void printConsole()
	{
		synchronized(this)
		{
			ConsoleMessage consoleMessage = new ConsoleMessage("\n", false);
			this.printQueue.enqueue(consoleMessage);
			this.printCache.add(consoleMessage);
		}
	}
	
	/**Method used to retrieve the system time at the instance of being invoked. 
	  @return a string of format yyyyMMdd_HHmmss
	  */
	private String getSystemTimestamp()
	{
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	}
	
	/**Removes all stored objects in the print queue, and clears all the text in the 
	  *JTextPane.
	  */
	private void clearConsole(boolean clearCache)
	{
		synchronized(this)
		{
			this.printQueue.removeAll();
			if(clearCache)
			{
				this.printCache.clear();
			}
		}
		
		super.setText("");
	}
	
	/***/
	public void validateSettings()
	{
		this.setBackground(AbstractServer.backgroundColor);
		
		this.messageAttribute = new SimpleAttributeSet();
		StyleConstants.setForeground(this.messageAttribute, AbstractServer.foregroundColor);
		StyleConstants.setFontSize(this.messageAttribute, AbstractServer.textFont.getSize());
		StyleConstants.setFontFamily(this.messageAttribute, AbstractServer.textFont.getFontName());
		
		this.warningAttribute = new SimpleAttributeSet();
		StyleConstants.setForeground(this.warningAttribute, Color.red);
		StyleConstants.setFontSize(this.warningAttribute, AbstractServer.textFont.getSize());
		StyleConstants.setFontFamily(this.warningAttribute, AbstractServer.textFont.getFontName());
		
		this.clearConsole(false);
		for(ConsoleMessage message : this.printCache)
		{
			this.printQueue.enqueue(message);
		}
	}
	
	private class ConsoleMessage
	{
		private String message;
		
		private boolean warning;

		public ConsoleMessage(String message, boolean warning)
		{
			this.message = message;
			this.warning = warning;
		}

		public String getMessage()
		{
			return message;
		}

		boolean isWarning()
		{
			return warning;
		}
	}
}