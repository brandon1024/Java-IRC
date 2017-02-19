package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**@author Brandon Richardson
 *@version 1.4.3
 *@since 06/05/2016
 *<p>
 *The {@code Logger} class manages logging information regarding the state of an application, system,
 *or component, be it simple messages or information regarding exceptions thrown.
 *<p>
 *The main purpose of the {@code Logger} class is to aid in application troubleshooting and
 *record keeping. Log files are stored in a desired directory, in plain text files.
 */

public class Logger
{
	/**A String representing the directory to which the text log files will be stored*/
	private String logFileDirectory;
	
	/**The FileWriter stream used to write text to the log files*/
	private FileWriter fileOutput;
	
	/**Boolean variable describing the state of the logger. If false, the FileWriter is unable to
	  *write to write to the directory specified*/
	private boolean isClosed;
	
	/**Construct a new Logger object given a directory to which log files should be stored. The
	  *log file generated by this instance of Logger will have the default file name LOGyyyyMMdd_HHmmss.txt,
	  *where yyyyMMdd_HHmmss represents the time at which the log file was generated.
	  *If the FileWriter stream cannot be established, the logger will be marked as closed.
	 *@param logFileDirectory The directory to which the log files should be stored*/
	public Logger(String logFileDirectory)
	{
		if(logFileDirectory == null)
			throw new NullPointerException("null file directory");
		
		this.logFileDirectory = logFileDirectory;
		this.isClosed = false;
		
		if(!(new File(logFileDirectory)).isDirectory())
		{
			(new File(logFileDirectory)).mkdir();
		}
		
		//Attempt to Build Log File and File Writer
		File logFile = new File(logFileDirectory + "LOG " + Logger.getSystemTimestamp() + ".txt");
		
		try
		{
			if(logFile.exists())
				throw new RuntimeException(logFile.getName() + " already exists");
			else
				logFile.createNewFile();
			
			this.fileOutput = new FileWriter(logFile);
		}
		catch (IOException e)
		{
			this.isClosed = true;
		}
	}
	
	/**Construct a new Logger object given a reference to a FileWriter stream, and log file directory.
	  *@param fileOutput The FileWriter stream used to write text to the log file
	  *@param logFileDirectory The directory to which the log files should be stored*/
	public Logger(FileWriter fileOutput, String logFileDirectory)
	{
		if(fileOutput == null)
			throw new NullPointerException("null FileWriter reference");
		
		if(logFileDirectory == null)
			throw new NullPointerException("null file directory");
		
		this.logFileDirectory = logFileDirectory;
		this.fileOutput = fileOutput;
		this.isClosed = false;
	}
	
	/**Construct a new Logger object given a directory to which log files should be stored and 
	  *a specified file name. If the FileWriter stream cannot be established, the logger will be 
	  *marked as closed.
	 *@param logFileDirectory The directory to which the log files should be stored*/
	public Logger(String logFileDirectory, String fileName)
	{
		this.logFileDirectory = logFileDirectory;
		this.isClosed = false;
		
		if(!(new File(logFileDirectory)).isDirectory())
		{
			(new File(logFileDirectory)).mkdir();
		}
		
		if(logFileDirectory == null)
			throw new NullPointerException("null file directory");
		
		if(fileName == null)
			throw new NullPointerException("null file name");
		
		if(fileName.isEmpty())
			throw new RuntimeException("invalid file name; file name must not be empty");
		
		//Attempt to Build Log File and File Writer
		File logFile = new File(logFileDirectory + fileName + ".txt");
		
		try
		{
			if(logFile.exists())
				throw new RuntimeException(logFile.getName() + " already exists");
			else
				logFile.createNewFile();
			
			this.fileOutput = new FileWriter(logFile);
		}
		catch (IOException e)
		{
			this.isClosed = true;
		}
	}
	
	/**Log an exception. If the logger is closed, the method simply returns.
	  *Format: 
	  *TIMESTAMP: EXCEPTION THROWN
	  *Exception Description
	  *STACKTRACE...
	  *<p>
	  *@param e The Exception to be logged to the log file*/
	public synchronized void logException(Exception e)
	{
		if(this.isClosed)
		{
			return;
		}
		
		try
		{
			String stackTrace = "";
			for(StackTraceElement element : e.getStackTrace())
			{
				stackTrace += "\t" + element.toString() + "\n";
			}
			stackTrace += "\n";
			
			this.fileOutput.write(Logger.getSystemTimestamp() + ": EXCEPTION THROWN" + "\n");
			this.fileOutput.write(e.toString() + "\n" + stackTrace);
			this.fileOutput.flush();
		}
		catch(Exception ex)
		{
			this.isClosed = true;
		}
	}
	
	/**Log a String. If the logger is closed, the method returns. Each String logged to the log file
	  *is preceded by a timestamp.
	  *<p>
	  *Format: 
	  *TIMESTAMP: str...
	  *@param str The String to be logged to the log file*/
	public synchronized void logString(String str)
	{
		if(this.isClosed)
		{
			return;
		}
		
		try
		{
			this.fileOutput.write(Logger.getSystemTimestamp() + ": " + str);
			this.fileOutput.flush();
		}
		catch(Exception e)
		{
			this.isClosed = true;
		}
	}
	
	/** Attempts to delete all log files in the log file directory, i.e. any file with a filename that begins 
	  *with {@code LOG}.*/
	public void clearLogs()
	{
		for(File f : (new File(this.logFileDirectory)).listFiles())
		{
			if(f.getName().startsWith("LOG"))
			{
				f.delete();
			}
		}
	}
	
	/**Returns the state of the logger. If the logger is closed, calls to logException()
	  *and logString() will simply return.
	  *@return the state of the Logger*/
	public boolean isClosed()
	{
		return this.isClosed;
	}
	
	/**Closes the logger and associated file stream.*/
	public void close()
	{
		try
		{
			this.fileOutput.close();
		}
		catch (IOException e){}
		this.isClosed = true;
	}
	
	/**Returns the system timestamp at the time of being called.
	  *Format: yyyyMMdd_HHmmss
	  *@return the system timestamp at the moment of being invoked*/
	private static String getSystemTimestamp()
	{
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	}
}
