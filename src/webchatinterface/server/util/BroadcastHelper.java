package webchatinterface.server.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import webchatinterface.AbstractIRC;
import webchatinterface.helpers.TimeHelper;
import webchatinterface.server.AbstractServer;
import webchatinterface.server.WebChatServerInstance;
import webchatinterface.server.ui.ConsoleManager;
import webchatinterface.util.TransferBuffer;
import webchatinterface.util.Command;
import webchatinterface.util.Message;

/**@author Brandon Richardson
 *@version 1.4.3
 *@since 06/05/2016
  *<p>
  *The {@code BroadcastHelper} class is designed to simply broadcasting Message
  *and Command objects to clients connected to the server. It also facilitates
  *scheduled server message broadcasting.
  */

public class BroadcastHelper implements Runnable
{
	/**ArrayList object representing a collection of all scheduled server messages*/
	private ArrayList<ScheduledServerMessage> scheduledServerMessage;
	
	/**The console manager for the graphical user interface. The console manager 
	  *is used to append messages and status to the console.
	  *@see ConsoleManager*/
	private ConsoleManager consoleMng;
	
	/**Variable used to close the {@code BroadcastHelper} thread*/
	private volatile boolean RUN = true;
	
	/**Builds a {@code BroadcastHelper} instance. Assigns parameters to instance variables, and attemps
	  *to load all saved ScheduledServerMessages.
	  *@param consoleMng The server console manager. Used to append messages
	  *and status to the graphical user interface console.*/
	public BroadcastHelper(ConsoleManager consoleMng)
	{
		this.consoleMng = consoleMng;
		this.scheduledServerMessage = new ArrayList<ScheduledServerMessage>();
	}
	
	/**Attempt to load saved ScheduledServerMessage objects from SCHEDULED_MESSAGES.dat, and populate
	  *scheduledServerMessage ArrayList.*/
	private void loadScheduledMessages()
	{
		try(ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(AbstractIRC.SERVER_APPLCATION_DIRECTORY + "SCHEDULED_MESSAGES.dat")))
		{
			while(true)
			{
				try
				{
					this.scheduledServerMessage.add((ScheduledServerMessage)objectIn.readObject());
				}
				catch(EOFException e)
				{
					break;
				}
				catch (IOException | ClassNotFoundException e)
				{
					AbstractServer.logException(e);
					break;
				}
			}
		}
		catch (IOException e)
		{
			AbstractServer.logException(e);
		}
	}
	
	/**Attempt to save ScheduledServerMessage objects from scheduledServerMessage ArrayList to SCHEDULED_MESSAGES.dat.
	  *Items in ArrayList are not removed from the Collection, simply accessed and written to file.*/
	private void saveScheduledMessages()
	{
		try(ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(AbstractIRC.SERVER_APPLCATION_DIRECTORY + "SCHEDULED_MESSAGES.dat", false)))
		{
			for(ScheduledServerMessage message : this.scheduledServerMessage)
			{
				try
				{
					objectOut.writeObject(message);
				}
				catch (IOException e)
				{
					AbstractServer.logException(e);
				}
			}
		}
		catch (IOException e)
		{
			AbstractServer.logException(e);
		}
	}
	
	/**Invoked when start() is invoked. Periodically iterates the automatedServerMessages ArrayList
	  *and broadcasts any messages that fit the message frequency criteria. Will continue until
	  *{@code stop()} is invoked.*/
	@Override
	public void run()
	{
		int minutes = 0;
		
		while(this.RUN)
		{
			for(ScheduledServerMessage message : this.scheduledServerMessage)
			{
				if(message.repeatDaily)
				{
					String time = TimeHelper.formatTimestamp(Calendar.getInstance(), "HHmm");
					int hour = Integer.parseInt(time.substring(0,2));
					int minute = Integer.parseInt(time.substring(2));
					
					if(message.dailyHour == hour && message.dailyMinute == minute)
					{
						this.broadcastMessage(new Message(message.message, "SERVER", "0"), ChatRoom.publicRoom);
						this.consoleMng.printConsole("Scheduled Message Broadcasted from Server", false);
					}
				}
				else
				{
					if(minutes % message.everyMinutes == 0)
					{
						this.broadcastMessage(new Message(message.message, "SERVER", "0"), ChatRoom.publicRoom);
						this.consoleMng.printConsole("Scheduled Message Broadcasted from Server", false);
					}
				}
			}
			
			
			try
			{
				Thread.sleep(60000);
			}
			catch(InterruptedException e)
			{
				AbstractServer.logException(e);
			}
			
			minutes++;
			if(minutes == 1440)
			{
				minutes = 0;
			}
		}
	}
	
	/**Runs the BroadcastHelper thread. Attempts to load saved ScheduledServerMessages.*/
	public void start()
	{
		if(this.RUN)
		{
			return;
		}
		
		this.scheduledServerMessage.clear();
		
		this.loadScheduledMessages();
		(new Thread(this)).start();
	}
	
	/**Stops the BroadcastHelper thread and attempt to save ScheduledServerMessages from
	  *scheduledServerMessage ArrayList*/
	public void stop()
	{
		this.RUN = false;
		this.saveScheduledMessages();
		
		synchronized(this)
		{
			this.scheduledServerMessage.clear();
		}
	}
	
	/**Displays an input dialog creating and managing scheduled server messages.*/
	public void showBroadcastMessageDialog()
	{
		//Build Component Panels and Frames
		JFrame frame = new JFrame();
		JPanel dialogPanel = new JPanel();
		JPanel messagePanel = new JPanel();
		JPanel freqencyPanel = new JPanel();
		
		//Set Panel Layout Managers
		dialogPanel.setLayout(new BorderLayout(5,5));
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS));
		freqencyPanel.setLayout(new BoxLayout(freqencyPanel, BoxLayout.PAGE_AXIS));
		
		//Build JComboBox and Populate with AutomatedServerMessages
		JComboBox<ScheduledServerMessage> automatedMessages = new JComboBox<ScheduledServerMessage>();
		ScheduledServerMessage defaultMessage = new ScheduledServerMessage("New Message", 0);
		automatedMessages.addItem(defaultMessage);
		
		synchronized(this)
		{
			for(ScheduledServerMessage message : scheduledServerMessage)
			{
				automatedMessages.addItem(message);
			}
		}
		automatedMessages.setSelectedIndex(0);
		
		//Build Message JTextArea and Make Scrollable
		JTextArea messageField = new JTextArea(3,50);
		messageField.setWrapStyleWord(true);
		JScrollPane scroll = new JScrollPane(messageField);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		//Add to Message Panel
		messagePanel.add(new JLabel("Enter Message to Broadcast:"));
		messagePanel.add(scroll);
		
		//Build Custom Frequency TextFiels
		JTextField customEveryMinutes = new JTextField();
		customEveryMinutes.setEnabled(false);
		JTextField customDailyMinutes = new JTextField();
		customDailyMinutes.setEnabled(false);
		JTextField customDailyHours = new JTextField();
		customDailyHours.setEnabled(false);
		
		//Build RadioButton Frequency Options
		JRadioButton oneTime = new JRadioButton("One Time", true);
		JRadioButton every1Min = new JRadioButton("Every 1 Minute");
		JRadioButton every15Min = new JRadioButton("Every 15 Minute");
		JRadioButton every1Hour = new JRadioButton("Every 1 Hour");
		JRadioButton custom = new JRadioButton("Custom:");
		
		//Hide Custom Frequency TextFields when Custom Radiobutton Not Selected
		custom.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
			    if (e.getStateChange() == ItemEvent.SELECTED)
			    {
			    	customEveryMinutes.setEnabled(true);
			    	customDailyMinutes.setEnabled(true);
			    	customDailyHours.setEnabled(true);
			    }
			    else if (e.getStateChange() == ItemEvent.DESELECTED)
			    {
			    	customEveryMinutes.setEnabled(false);
			    	customDailyMinutes.setEnabled(false);
			    	customDailyHours.setEnabled(false);
			    }
			}
		});
		
		//Add RadioButtons to Group
		ButtonGroup group = new ButtonGroup();
		group.add(oneTime);
		group.add(every1Min);
		group.add(every15Min);
		group.add(every1Hour);
		group.add(custom);
		
		//Build and Populate Inner Panel
		JPanel customEveryMinutesPanel = new JPanel();
		customEveryMinutesPanel.setLayout(new BoxLayout(customEveryMinutesPanel, BoxLayout.LINE_AXIS));
		customEveryMinutesPanel.add(customEveryMinutes);
		customEveryMinutesPanel.add(new JLabel(" minutes"));
		
		//Build and Populate Inner Panel
		JPanel customDailyPanel = new JPanel();
		customDailyPanel.setLayout(new BoxLayout(customDailyPanel, BoxLayout.LINE_AXIS));
		customDailyPanel.add(customDailyHours);
		customDailyPanel.add(new JLabel(" : "));
		customDailyPanel.add(customDailyMinutes);
		customDailyPanel.add(new JLabel(" daily"));
		
		//Set Component Alignment
		oneTime.setAlignmentX(Component.LEFT_ALIGNMENT);
		every1Min.setAlignmentX(Component.LEFT_ALIGNMENT);
		every15Min.setAlignmentX(Component.LEFT_ALIGNMENT);
		every1Hour.setAlignmentX(Component.LEFT_ALIGNMENT);
		custom.setAlignmentX(Component.LEFT_ALIGNMENT);
		customEveryMinutesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		customDailyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		customEveryMinutes.setAlignmentX(Component.LEFT_ALIGNMENT);
		customDailyHours.setAlignmentX(Component.LEFT_ALIGNMENT);
		customDailyMinutes.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		//Populate Frequency Panel
		freqencyPanel.add(new JLabel(" Frequency:"));
		freqencyPanel.add(oneTime);
		freqencyPanel.add(every1Min);
		freqencyPanel.add(every15Min);
		freqencyPanel.add(every1Hour);
		freqencyPanel.add(custom);
		freqencyPanel.add(customEveryMinutesPanel);
		freqencyPanel.add(new JLabel("or"));
		freqencyPanel.add(customDailyPanel);
		
		//Populate Master Panel
		dialogPanel.add(automatedMessages, BorderLayout.PAGE_START);
		dialogPanel.add(messagePanel, BorderLayout.CENTER);
		dialogPanel.add(freqencyPanel, BorderLayout.LINE_END);
		
		//Show Dialog Window
		String[] options = {"OK", "Delete", "Clear All", "Cancel"};
		int returnValue = JOptionPane.showOptionDialog(frame, dialogPanel, 
				"Broadcast Message", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		
		//if "OK" button pressed
		if(returnValue == 0)
		{
			if(oneTime.isSelected())
			{
				if(!messageField.getText().isEmpty())
				{
					this.broadcastMessage(new Message(messageField.getText(), "SERVER", "0"), ChatRoom.publicRoom);
					this.consoleMng.printConsole("Successfully Broadcasted Server Message", false);
				}
			}
			else if(every1Min.isSelected())
			{
				ScheduledServerMessage newMessage = new ScheduledServerMessage(messageField.getText(), 1);
				synchronized(this)
				{
					this.scheduledServerMessage.add(newMessage);
				}
				this.consoleMng.printConsole("Successfully Scheduled Server Message", false);
			}
			else if(every15Min.isSelected())
			{
				ScheduledServerMessage newMessage = new ScheduledServerMessage(messageField.getText(), 15);
				synchronized(this)
				{
					this.scheduledServerMessage.add(newMessage);
				}
				this.consoleMng.printConsole("Successfully Scheduled Server Message", false);
			}
			else if(every1Hour.isSelected())
			{
				ScheduledServerMessage newMessage = new ScheduledServerMessage(messageField.getText(), 60);
				synchronized(this)
				{
					this.scheduledServerMessage.add(newMessage);
				}
				this.consoleMng.printConsole("Successfully Scheduled Server Message", false);
			}
			else if(custom.isSelected())
			{
				try
				{
					if(!customEveryMinutes.getText().isEmpty())
					{
						int min = Integer.parseInt(customEveryMinutes.getText());
						ScheduledServerMessage newMessage = new ScheduledServerMessage(messageField.getText(), min);
						synchronized(this)
						{
							this.scheduledServerMessage.add(newMessage);
						}
						this.consoleMng.printConsole("Successfully Scheduled Server Message", false);
					}
					else
					{
						int dailyHour = Integer.parseInt(customDailyHours.getText());
						int dailyMin = Integer.parseInt(customDailyMinutes.getText());
						ScheduledServerMessage newMessage = new ScheduledServerMessage(messageField.getText(), dailyHour, dailyMin);
						synchronized(this)
						{
							this.scheduledServerMessage.add(newMessage);
						}
						this.consoleMng.printConsole("Successfully Scheduled Server Message", false);
					}
				}
				catch(Exception e)
				{
					this.consoleMng.printConsole("Invalid Scheduled Message Frequency", true);
					AbstractServer.logException(e);
				}
			}
		}
		//if "delete" button pressed
		else if(returnValue == 1)
		{
			ScheduledServerMessage message = (ScheduledServerMessage)automatedMessages.getSelectedItem();
			if(!message.equals(defaultMessage))
			{
				synchronized(this)
				{
					for(ScheduledServerMessage m : scheduledServerMessage)
					{
						if(m.equals(message))
						{
							scheduledServerMessage.remove(m);
							break;
						}
					}
				}
			}
			this.consoleMng.printConsole("Successfully Deleted Saved Server Message", false);
		}
		//if "delete all" button pressed
		else if(returnValue == 2)
		{
			synchronized(this)
			{
				this.scheduledServerMessage.clear();
			}

			this.consoleMng.printConsole("Successfully Deleted All Saved Server Message", false);
		}
	}
	
	/**Broadcast a Message object to all clients connected to the server within the
	  *specified chatroom.
	  *@param message The message object to broadcast to all clients
	  *@param room The room to broadcast the message to*/
	public synchronized void broadcastMessage(Message message, ChatRoom room)
	{
		//for each instance in ConnectionArray
		WebChatServerInstance[] clients = room.getConnectedClients();
			
		for(WebChatServerInstance client : clients)
		{
			try
			{
				client.send(message);
			}
			catch (IOException e)
			{
				this.consoleMng.printConsole("Unable to Broadcast Message to: " + client.toString(), true);
				AbstractServer.logException(e);
			}
		}
	}
	
	/**Broadcast a Message object to all clients connected to the server within the
	  *specified chatroom.
	  *@param message The message object to broadcast to all clients
	  *@param room The room to broadcast the message to*/
	public synchronized void broadcastMessage(TransferBuffer message, ChatRoom room)
	{
		//for each instance in ConnectionArray
		WebChatServerInstance[] clients = room.getConnectedClients();
			
		for(WebChatServerInstance client : clients)
		{
			try
			{
				client.send(message);
			}
			catch (IOException e)
			{
				this.consoleMng.printConsole("Unable to Broadcast Message to: " + client.toString(), true);
				AbstractServer.logException(e);
			}
		}
	}
	
	/**Broadcast a Command object to all clients connected to the server within the
	  *specified chatroom.
	  @param com The Command object to broadcast to all clients
	  *@param room The room to broadcast the message to*/
	public synchronized void broadcastCommand(Command com, ChatRoom room)
	{
		//for each instance in ConnectionArray
		WebChatServerInstance[] clients = room.getConnectedClients();
			
		for(WebChatServerInstance client : clients)
		{
			try
			{
				client.send(com);
			}
			catch (IOException e)
			{
				this.consoleMng.printConsole("Unable to Broadcast Message to: " + client.toString(), true);
				AbstractServer.logException(e);
			}
		}
	}
	
	/**Broadcast a Message object to all clients connected to the server.
	  @param message The message object to broadcast to all clients*/
	public synchronized void broadcastMessage(Message message)
	{
		//for each instance in ConnectionArray
		WebChatServerInstance[] clients = ChatRoom.getGlobalMembers();
			
		for(WebChatServerInstance client : clients)
		{
			try
			{
				client.send(message);
			}
			catch (IOException e)
			{
				this.consoleMng.printConsole("Unable to Broadcast Message to: " + client.toString(), true);
				AbstractServer.logException(e);
			}
		}
	}
	
	/**Broadcast a Command object to all clients connected to the server.
	  @param com The command object to broadcast to all clients*/
	public synchronized void broadcastCommand(Command com)
	{
		//for each instance in ConnectionArray
		WebChatServerInstance[] clients = ChatRoom.getGlobalMembers();
			
		for(WebChatServerInstance client : clients)
		{
			try
			{
				client.send(com);
			}
			catch (IOException e)
			{
				this.consoleMng.printConsole("Unable to Broadcast Message to: " + client.toString(), true);
				AbstractServer.logException(e);
			}
		}
	}
}
