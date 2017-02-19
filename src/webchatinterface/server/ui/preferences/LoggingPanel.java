package webchatinterface.server.ui.preferences;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import webchatinterface.AbstractIRC;
import webchatinterface.server.AbstractServer;

public class LoggingPanel extends PreferencePanel
{
	private static final long serialVersionUID = -8912986255346052099L;

	private JCheckBox enableLoggingCheckBox;
	
	private JRadioButton logOnlyWarningsExceptionsRadioButton;
	
	private JRadioButton logOnlyServerActivityRadioButton;
	
	private JRadioButton logAllRadioButton;
	
	private JCheckBox logAllToSingleFileCheckBox;
	
	private JCheckBox logFileFortmatCheckBox;
	
	private JCheckBox limitFileSizeCheckBox;
	
	private JCheckBox deleteOldLogFilesCheckBox;
	
	private JCheckBox showTimestampsCheckBox;
	
	private JTextField limitFileSizeField;
	
	private JTextField deleteOldLogFilesField;
	
	private JTextField logFileFormatField;
	
	private JButton showLogFolderButton;
	
	private JButton deleteAllLogFilesButton;
	
	public LoggingPanel(String header)
	{
		super(header);
		this.enableLoggingCheckBox = new JCheckBox("Enable Logging to File");
		this.logOnlyWarningsExceptionsRadioButton = new JRadioButton("Log Only Warnings and Exceptions");
		this.logOnlyServerActivityRadioButton = new JRadioButton("Log Only Server Activity");
		this.logAllRadioButton = new JRadioButton("Log All Activity");
		this.logAllToSingleFileCheckBox = new JCheckBox("Log All to Single File \'WebChatServer.LOG\'");
		this.logFileFortmatCheckBox = new JCheckBox("Use File Format: ");
		this.limitFileSizeCheckBox = new JCheckBox("Limit Log File Size");
		this.deleteOldLogFilesCheckBox = new JCheckBox("Delete Old Log Files");
		this.showTimestampsCheckBox = new JCheckBox("Show Timestamps In Log Files");
		this.limitFileSizeField = new JTextField(15);
		this.deleteOldLogFilesField = new JTextField(15);
		this.logFileFormatField = new JTextField(15);
		this.showLogFolderButton = new JButton("Open Logs in Windows Explorer");
		this.deleteAllLogFilesButton = new JButton("Delete All Logs");
		
		this.showLogFolderButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				try
				{
					if(Desktop.isDesktopSupported())
					{
						Desktop.getDesktop().open(new File(AbstractIRC.SERVER_APPLCATION_DIRECTORY + "LOGS"));
					}
					else
					{
						JTextArea info = (JTextArea)LoggingPanel.this.createInformationPanel("This feature is not supported on this platform.\n"
								+ "Use Windows Explorer to open " + AbstractIRC.SERVER_APPLCATION_DIRECTORY + "LOGS");
						info.setFocusable(true);
						info.setRows(3);
						info.setColumns(50);
						JOptionPane.showMessageDialog(LoggingPanel.this, info);
					}
				}
				catch(Exception e){}
			}
		});
		
		this.deleteAllLogFilesButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				AbstractServer.clearLogs();
			}
		});
		
		ButtonGroup group = new ButtonGroup();
		group.add(this.logOnlyWarningsExceptionsRadioButton);
		group.add(this.logOnlyServerActivityRadioButton);
		group.add(this.logAllRadioButton);
		
		ActionListener action = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				if(((JCheckBox)event.getSource()).isSelected())
				{
					LoggingPanel.this.logOnlyWarningsExceptionsRadioButton.setEnabled(true);
					LoggingPanel.this.logOnlyServerActivityRadioButton.setEnabled(true);
					LoggingPanel.this.logAllRadioButton.setEnabled(true);
					LoggingPanel.this.logAllToSingleFileCheckBox.setEnabled(true);
					LoggingPanel.this.logFileFortmatCheckBox.setEnabled(true);
					LoggingPanel.this.limitFileSizeCheckBox.setEnabled(true);
					LoggingPanel.this.deleteOldLogFilesCheckBox.setEnabled(true);
					LoggingPanel.this.showTimestampsCheckBox.setEnabled(true);
					
					LoggingPanel.this.limitFileSizeField.setEnabled(true);
					LoggingPanel.this.limitFileSizeField.setEditable(true);
					
					LoggingPanel.this.deleteOldLogFilesField.setEnabled(true);
					LoggingPanel.this.deleteOldLogFilesField.setEditable(true);
					
					LoggingPanel.this.logFileFormatField.setEnabled(true);
					LoggingPanel.this.logFileFormatField.setEditable(true);
				}
				else
				{
					LoggingPanel.this.logOnlyWarningsExceptionsRadioButton.setEnabled(false);
					LoggingPanel.this.logOnlyServerActivityRadioButton.setEnabled(false);
					LoggingPanel.this.logAllRadioButton.setEnabled(false);
					LoggingPanel.this.logAllToSingleFileCheckBox.setEnabled(false);
					LoggingPanel.this.logFileFortmatCheckBox.setEnabled(false);
					LoggingPanel.this.limitFileSizeCheckBox.setEnabled(false);
					LoggingPanel.this.deleteOldLogFilesCheckBox.setEnabled(false);
					LoggingPanel.this.showTimestampsCheckBox.setEnabled(false);
					
					LoggingPanel.this.limitFileSizeField.setEnabled(false);
					LoggingPanel.this.limitFileSizeField.setEditable(false);
					
					LoggingPanel.this.deleteOldLogFilesField.setEnabled(false);
					LoggingPanel.this.deleteOldLogFilesField.setEditable(false);
					
					LoggingPanel.this.logFileFormatField.setEnabled(false);
					LoggingPanel.this.logFileFormatField.setEditable(false);
				}
			}
		};
		
		ItemListener item = new ItemListener()
		{
			public void itemStateChanged(ItemEvent event)
			{
				if(event.getStateChange() == ItemEvent.SELECTED)
				{
					LoggingPanel.this.logOnlyWarningsExceptionsRadioButton.setEnabled(true);
					LoggingPanel.this.logOnlyServerActivityRadioButton.setEnabled(true);
					LoggingPanel.this.logAllRadioButton.setEnabled(true);
					LoggingPanel.this.logAllToSingleFileCheckBox.setEnabled(true);
					LoggingPanel.this.logFileFortmatCheckBox.setEnabled(true);
					LoggingPanel.this.limitFileSizeCheckBox.setEnabled(true);
					LoggingPanel.this.deleteOldLogFilesCheckBox.setEnabled(true);
					LoggingPanel.this.showTimestampsCheckBox.setEnabled(true);
					
					LoggingPanel.this.limitFileSizeField.setEnabled(true);
					LoggingPanel.this.limitFileSizeField.setEditable(true);
					
					LoggingPanel.this.deleteOldLogFilesField.setEnabled(true);
					LoggingPanel.this.deleteOldLogFilesField.setEditable(true);
					
					LoggingPanel.this.logFileFormatField.setEnabled(true);
					LoggingPanel.this.logFileFormatField.setEditable(true);
				}
				else
				{
					LoggingPanel.this.logOnlyWarningsExceptionsRadioButton.setEnabled(false);
					LoggingPanel.this.logOnlyServerActivityRadioButton.setEnabled(false);
					LoggingPanel.this.logAllRadioButton.setEnabled(false);
					LoggingPanel.this.logAllToSingleFileCheckBox.setEnabled(false);
					LoggingPanel.this.logFileFortmatCheckBox.setEnabled(false);
					LoggingPanel.this.limitFileSizeCheckBox.setEnabled(false);
					LoggingPanel.this.deleteOldLogFilesCheckBox.setEnabled(false);
					LoggingPanel.this.showTimestampsCheckBox.setEnabled(false);
					
					LoggingPanel.this.limitFileSizeField.setEnabled(false);
					LoggingPanel.this.limitFileSizeField.setEditable(false);
					
					LoggingPanel.this.deleteOldLogFilesField.setEnabled(false);
					LoggingPanel.this.deleteOldLogFilesField.setEditable(false);
					
					LoggingPanel.this.logFileFormatField.setEnabled(false);
					LoggingPanel.this.logFileFormatField.setEditable(false);
				}
			}
		};
		
		this.enableLoggingCheckBox.addActionListener(action);
		this.enableLoggingCheckBox.addItemListener(item);
		
		action = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				if(((JCheckBox)event.getSource()).isSelected())
				{
					LoggingPanel.this.logFileFormatField.setEnabled(true);
					LoggingPanel.this.logFileFormatField.setEditable(true);
				}
				else
				{
					LoggingPanel.this.logFileFormatField.setEnabled(false);
					LoggingPanel.this.logFileFormatField.setEditable(false);
				}
			}
		};
		
		item = new ItemListener()
		{
			public void itemStateChanged(ItemEvent event)
			{
				if(((JCheckBox)event.getSource()).isSelected())
				{
					LoggingPanel.this.logFileFormatField.setEnabled(true);
					LoggingPanel.this.logFileFormatField.setEditable(true);
				}
				else
				{
					LoggingPanel.this.logFileFormatField.setEnabled(false);
					LoggingPanel.this.logFileFormatField.setEditable(false);
				}
			}
		};
		this.logFileFortmatCheckBox.addActionListener(action);
		this.logFileFortmatCheckBox.addItemListener(item);
		
		action = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				if(((JCheckBox)event.getSource()).isSelected())
				{
					LoggingPanel.this.limitFileSizeField.setEnabled(true);
					LoggingPanel.this.limitFileSizeField.setEditable(true);
				}
				else
				{
					LoggingPanel.this.limitFileSizeField.setEnabled(false);
					LoggingPanel.this.limitFileSizeField.setEditable(false);
				}
			}
		};
		
		item = new ItemListener()
		{
			public void itemStateChanged(ItemEvent event)
			{
				if(((JCheckBox)event.getSource()).isSelected())
				{
					LoggingPanel.this.limitFileSizeField.setEnabled(true);
					LoggingPanel.this.limitFileSizeField.setEditable(true);
				}
				else
				{
					LoggingPanel.this.limitFileSizeField.setEnabled(false);
					LoggingPanel.this.limitFileSizeField.setEditable(false);
				}
			}
		};
		this.limitFileSizeCheckBox.addActionListener(action);
		this.limitFileSizeCheckBox.addItemListener(item);
		
		action = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				if(((JCheckBox)event.getSource()).isSelected())
				{
					LoggingPanel.this.deleteOldLogFilesField.setEnabled(true);
					LoggingPanel.this.deleteOldLogFilesField.setEditable(true);
				}
				else
				{
					LoggingPanel.this.deleteOldLogFilesField.setEnabled(false);
					LoggingPanel.this.deleteOldLogFilesField.setEditable(false);
				}
			}
		};
		
		item = new ItemListener()
		{
			public void itemStateChanged(ItemEvent event)
			{
				if(((JCheckBox)event.getSource()).isSelected())
				{
					LoggingPanel.this.deleteOldLogFilesField.setEnabled(true);
					LoggingPanel.this.deleteOldLogFilesField.setEditable(true);
				}
				else
				{
					LoggingPanel.this.deleteOldLogFilesField.setEnabled(false);
					LoggingPanel.this.deleteOldLogFilesField.setEditable(false);
				}
			}
		};
		this.deleteOldLogFilesCheckBox.addActionListener(action);
		this.deleteOldLogFilesCheckBox.addItemListener(item);
		
		this.populatePanel();
		
		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.PAGE_AXIS));
		body.setBorder(BorderFactory.createEmptyBorder());
		body.add(this.buildLogSettingsPanel());
		body.add(this.buildFileManagementPanel());
		
		super.add(body, BorderLayout.CENTER);
		super.add(Box.createRigidArea(new Dimension(0,15)), BorderLayout.PAGE_END);
	}

	protected JPanel buildLogSettingsPanel()
	{
		JPanel logSettings = new JPanel();
		logSettings.setLayout(new BoxLayout(logSettings, BoxLayout.PAGE_AXIS));
		logSettings.setBorder(BorderFactory.createTitledBorder("Logging"));
		
		String info = "Change Server Logging Settings:\nEnabling server logging will allow you to examine the server "
				+ "activity for suspicious client activity or troubleshooting issues with the server.";
		logSettings.add(super.createInformationPanel(info));
		
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		innerPanel.add(this.enableLoggingCheckBox);
		logSettings.add(innerPanel);
		
		innerPanel = new JPanel();
		innerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		innerPanel.add(Box.createRigidArea(new Dimension(20,0)));
		innerPanel.add(this.logOnlyWarningsExceptionsRadioButton);
		innerPanel.add(this.logOnlyServerActivityRadioButton);
		innerPanel.add(this.logAllRadioButton);
		logSettings.add(innerPanel);
		
		innerPanel = new JPanel();
		innerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		innerPanel.add(Box.createRigidArea(new Dimension(20,0)));
		innerPanel.add(this.logAllToSingleFileCheckBox);
		logSettings.add(innerPanel);
		
		innerPanel = new JPanel();
		innerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		innerPanel.add(Box.createRigidArea(new Dimension(20,0)));
		innerPanel.add(this.logFileFortmatCheckBox);
		innerPanel.add(this.logFileFormatField);
		logSettings.add(innerPanel);
		
		innerPanel = new JPanel();
		innerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		innerPanel.add(Box.createRigidArea(new Dimension(20,0)));
		innerPanel.add(this.limitFileSizeCheckBox);
		innerPanel.add(new JLabel("Limit: "));
		innerPanel.add(this.limitFileSizeField);
		innerPanel.add(new JLabel("KB"));
		logSettings.add(innerPanel);
		
		innerPanel = new JPanel();
		innerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		innerPanel.add(Box.createRigidArea(new Dimension(20,0)));
		innerPanel.add(this.deleteOldLogFilesCheckBox);
		innerPanel.add(new JLabel("Delete After "));
		innerPanel.add(this.deleteOldLogFilesField);
		innerPanel.add(new JLabel("Sessions"));
		logSettings.add(innerPanel);
		
		innerPanel = new JPanel();
		innerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		innerPanel.add(Box.createRigidArea(new Dimension(20,0)));
		innerPanel.add(this.showTimestampsCheckBox);
		logSettings.add(innerPanel);
		
		innerPanel = new JPanel();
		innerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		innerPanel.add(new JLabel(AbstractIRC.SERVER_APPLCATION_DIRECTORY + "LOGS"));
		logSettings.add(innerPanel);
		
		return logSettings;
	}
	
	protected JPanel buildFileManagementPanel()
	{
		JPanel manageLogFilesPanel = new JPanel();
		manageLogFilesPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		manageLogFilesPanel.setBorder(BorderFactory.createTitledBorder("Manage Log Files"));
		
		manageLogFilesPanel.add(this.showLogFolderButton);
		manageLogFilesPanel.add(this.deleteAllLogFilesButton);
		
		return manageLogFilesPanel;
	}
	
	public String[] requestChangedFields()
	{
		ArrayList<String> changedFields = new ArrayList<String>();
		
		if(this.enableLoggingCheckBox.isSelected() != AbstractServer.loggingEnabled)
		{
			changedFields.add("Logging Enabled/Disabled");
		}
		
		if(this.logOnlyWarningsExceptionsRadioButton.isSelected() == AbstractServer.logOnlyWarningsExceptions){}
		else if(this.logOnlyServerActivityRadioButton.isSelected() == AbstractServer.logOnlyServerActivity){}
		else if(this.logAllRadioButton.isSelected() == AbstractServer.logAllActivity){}
		else
		{
			if(this.logOnlyWarningsExceptionsRadioButton.isSelected())
			{
				changedFields.add("Log Only Warnings and Exceptions");
			}
			else if(this.logOnlyServerActivityRadioButton.isSelected())
			{
				changedFields.add("Log Only Server Activity");
			}
			else if(this.logAllRadioButton.isSelected())
			{
				changedFields.add("Log All Activity");
			}
		}
		
		if(this.logAllToSingleFileCheckBox.isSelected() != AbstractServer.logAllToSingleFile)
		{
			changedFields.add("Log All to Single File Enabled/Disabled");
		}
		
		if(this.logFileFortmatCheckBox.isSelected() && !this.logFileFormatField.getText().equals(AbstractServer.logFileFormat))
		{
			changedFields.add("Log File Format");
		}
		
		if(this.limitFileSizeCheckBox.isSelected() && !(Integer.parseInt(this.limitFileSizeField.getText()) != AbstractServer.logFileSizeLimit))
		{
			changedFields.add("Log File Size Limit");
		}
		
		if(this.deleteOldLogFilesCheckBox.isSelected() && !(Integer.parseInt(this.deleteOldLogFilesField.getText()) != AbstractServer.deleteLogAfterSessions))
		{
			changedFields.add("Delete Old Log Files");
		}
		
		if(this.showTimestampsCheckBox.isSelected() != AbstractServer.showTimestampsInLogFiles)
		{
			changedFields.add("Show/Hide Timestamps in Log Files");
		}
		
		return changedFields.toArray(new String[0]);
	}
	
	public void save()
	{
		AbstractServer.loggingEnabled = this.enableLoggingCheckBox.isSelected();
		AbstractServer.logOnlyWarningsExceptions = this.logOnlyWarningsExceptionsRadioButton.isSelected();
		AbstractServer.logOnlyServerActivity = this.logOnlyServerActivityRadioButton.isSelected();
		AbstractServer.logAllActivity = this.logAllRadioButton.isSelected();
		AbstractServer.logAllToSingleFile = this.logAllToSingleFileCheckBox.isSelected();
		AbstractServer.showTimestampsInLogFiles = this.showTimestampsCheckBox.isSelected();
		
		if(this.logFileFortmatCheckBox.isSelected())
		{
			AbstractServer.logFileFormat = this.logFileFormatField.getText();
		}
		
		if(this.limitFileSizeCheckBox.isSelected())
		{
			AbstractServer.logFileSizeLimit = Integer.parseInt(this.limitFileSizeField.getText());
		}
		
		if(this.deleteOldLogFilesCheckBox.isSelected())
		{
			AbstractServer.deleteLogAfterSessions = Integer.parseInt(this.deleteOldLogFilesField.getText());
		}
	}

	protected void populatePanel()
	{
		this.enableLoggingCheckBox.setSelected(AbstractServer.loggingEnabled);
		this.logOnlyWarningsExceptionsRadioButton.setSelected(AbstractServer.logOnlyWarningsExceptions);
		this.logOnlyServerActivityRadioButton.setSelected(AbstractServer.logOnlyServerActivity);
		this.logAllRadioButton.setSelected(AbstractServer.logAllActivity);
		this.logAllToSingleFileCheckBox.setSelected(AbstractServer.logAllToSingleFile);
		this.logFileFortmatCheckBox.setSelected((AbstractServer.logFileFormat.equals("LOG") ? false : true));
		this.limitFileSizeCheckBox.setSelected((AbstractServer.logFileSizeLimit == 0 ? false : true));
		this.deleteOldLogFilesCheckBox.setSelected((AbstractServer.deleteLogAfterSessions == 0 ? false : true));
		this.showTimestampsCheckBox.setSelected(AbstractServer.showTimestampsInLogFiles);
		this.limitFileSizeField.setText(Integer.toString(AbstractServer.logFileSizeLimit));
		this.deleteOldLogFilesField.setText(Integer.toString(AbstractServer.deleteLogAfterSessions));
		this.logFileFormatField.setText(AbstractServer.logFileFormat);
		
		if(this.enableLoggingCheckBox.isSelected())
		{
			this.logOnlyWarningsExceptionsRadioButton.setEnabled(true);
			this.logOnlyServerActivityRadioButton.setEnabled(true);
			this.logAllRadioButton.setEnabled(true);
			this.logAllToSingleFileCheckBox.setEnabled(true);
			this.logFileFortmatCheckBox.setEnabled(true);
			this.limitFileSizeCheckBox.setEnabled(true);
			this.deleteOldLogFilesCheckBox.setEnabled(true);
			this.showTimestampsCheckBox.setEnabled(true);
			
			if(this.logFileFortmatCheckBox.isSelected())
			{
				this.limitFileSizeField.setEnabled(true);
			}
			else
			{
				this.limitFileSizeField.setEnabled(false);
			}
			
			if(this.deleteOldLogFilesCheckBox.isSelected())
			{
				this.deleteOldLogFilesField.setEnabled(true);
			}
			else
			{
				this.deleteOldLogFilesField.setEnabled(false);
			}
			
			if(this.logFileFortmatCheckBox.isSelected())
			{
				this.logFileFormatField.setEnabled(true);
			}
			else
			{
				this.logFileFormatField.setEnabled(false);
			}
		}
		else
		{
			this.logOnlyWarningsExceptionsRadioButton.setEnabled(false);
			this.logOnlyServerActivityRadioButton.setEnabled(false);
			this.logAllRadioButton.setEnabled(false);
			this.logAllToSingleFileCheckBox.setEnabled(false);
			this.logFileFortmatCheckBox.setEnabled(false);
			this.limitFileSizeCheckBox.setEnabled(false);
			this.deleteOldLogFilesCheckBox.setEnabled(false);
			this.showTimestampsCheckBox.setEnabled(false);
			this.limitFileSizeField.setEnabled(false);
			this.deleteOldLogFilesField.setEnabled(false);
			this.logFileFormatField.setEnabled(false);
		}
	}
}
