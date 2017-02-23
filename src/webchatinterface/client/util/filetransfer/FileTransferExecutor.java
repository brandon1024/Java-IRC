package webchatinterface.client.util.filetransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import util.KeyGenerator;
import util.DynamicQueue;
import webchatinterface.AbstractIRC;
import webchatinterface.client.AbstractClient;
import webchatinterface.client.communication.WebChatClient;
import webchatinterface.client.ui.WebChatClientGUI;
import webchatinterface.client.ui.dialog.TransferProgressDialog;
import webchatinterface.util.TransferBuffer;
import webchatinterface.util.ClientUser;
import webchatinterface.util.Command;

/**@author Brandon Richardson
 *@version 1.4.3
 *@since 06/05/2016
  *<p>
  *The {@code FileTransferExecutor} class is used to handle orderly file transfer between
  *the client and the server applications.
  *<p>
  *Files are read sequentially and split into small byte buffers. Each buffer is sent
  *to the server wrapped in a {@code TransferBuffer} object. Each buffer is relayed to all the clients
  *in the chatroom by the server in accordance with its specification.
  *<p>
  *Each transfer is marked with a unique transfer identification key, used to distinguish 
  *concurrent transfers and prevent buffer scrambling. This key, along with various information regarding
  *the transfer, is sent to the server and clients in a file transfer manifest command. This notifies
  *the server of a transfer, and allows clients to initiate a FileTransferExecutor in MODE_RECEIVE.
  *<p>
  *The FileTransferExecutor class has two standard modes. When {@code start(File file)} is invoked, 
  *the FileTransferExecutor thread establishes the transfer and handles splitting the file into buffers to
  *send to the server. If {@code start(Command transferManifest)} is invoked, the FileTransferExecutor thread
  *handles receiving the buffers, and reassembling the file to be displayed in the client UI.
  */

public class FileTransferExecutor implements Runnable
{
	private static final int MODE_SEND = 0;
	private static final int MODE_RECEIVE = 1;
	private TransferProgressDialog dialog;
	private WebChatClientGUI userInterface;
	private WebChatClient client;
	private ClientUser clientUser;
	private File file;
	private String transferID;
	private DynamicQueue<TransferBuffer> queuedBuffers = new DynamicQueue<TransferBuffer>();
	private volatile boolean transferRunning;
	private int mode;
	private Command transferManifest;
	
	public FileTransferExecutor(WebChatClientGUI userInterface, WebChatClient client)
	{
		this.userInterface = userInterface;
		this.dialog = null;
		this.client = client;
		this.clientUser = AbstractClient.getClientUser();
		this.mode = -1;
		this.file = null;
		this.transferRunning = false;
		this.transferID = null;
		this.transferManifest = null;
	}
	
	public void start(File file) throws RuntimeException
	{
		if(this.transferRunning)
		{
			RuntimeException e = new RuntimeException("Concurrent file transfers are not supported on a single instance of FileTransferExecutor");
			AbstractClient.logException(e);
			throw e;
		}
		
		this.transferRunning = true;
		this.mode = FileTransferExecutor.MODE_SEND;
		this.file = file;
		this.transferID = KeyGenerator.generateKey64(KeyGenerator.ALPHANUMERIC_MIXED_CASE);
		this.dialog = new TransferProgressDialog();
		(new Thread(this)).start();
	}
	
	public void start(Command transferManifest) throws RuntimeException
	{
		if(this.transferRunning)
		{
			RuntimeException e = new RuntimeException("Concurrent file transfers are not supported on a single instance of FileTransferExecutor");
			AbstractClient.logException(e);
			throw e;
		}
		
		this.transferRunning = true;
		this.transferManifest = transferManifest;
		this.transferID = (String)((Object[]) this.transferManifest.getMessage())[3];
		this.mode = FileTransferExecutor.MODE_RECEIVE;
		(new Thread(this)).start();
	}
	
	public void run() throws RuntimeException
	{
		if(this.mode == FileTransferExecutor.MODE_SEND)
		{
			this.send();
		}
		else if(this.mode == FileTransferExecutor.MODE_RECEIVE)
		{
			this.receive();
		}
		else if(this.mode == -1)
		{
			RuntimeException e = new RuntimeException("FileTransferExecutor thread was not initialized properly; use start(file, mode) to start thread");
			AbstractClient.logException(e);
			throw e;
		}
	}
	
	private void send()
	{
		try
		{
			//Define Variables
			final long bufferSize = 4096L;
			final long bytesTotal = this.file.length();
			long bytesRead = 0L;
			long bytesRemaining = bytesTotal;
			long numberOfPackets = bytesTotal / bufferSize;
			numberOfPackets += (bytesTotal % bufferSize > 0) ? 1 : 0;
			
			//Update Dialog
			this.updateTransferDialog(0, 0, bytesTotal, 0, this.file.getName());
			
			//Open Streams
			FileInputStream fos = new FileInputStream(file);
			
			//Initiate File Transfer with Transfer Manifest Command
			Object[] transferData = {numberOfPackets, bytesTotal, bufferSize, this.transferID, file.getName()};
			this.transferManifest = new Command(Command.FILE_TRANSFER, transferData, this.clientUser.getUsername(), this.clientUser.getUserID());
			this.client.send(this.transferManifest);
			
			//Send Buffers
			while(bytesRemaining > 0)
			{
				int time = TransferUtilities.getSystemTimestamp();
				
				byte[] array = (bytesRemaining < bufferSize) ? new byte[(int) bytesRemaining] : new byte[(int)bufferSize];
				
				fos.read(array);
				bytesRead += array.length;
				bytesRemaining -= array.length;
				
				//Create Byte Array and Message
				TransferBuffer message = new TransferBuffer(array, file.getName(), this.transferID, this.clientUser.getUsername(), this.clientUser.getUserID());
				
				//Send Message and Close Streams
				this.client.send(message);
				
				long timeElapsedMillis = (TransferUtilities.getSystemTimestamp() - time) / 1000;
				this.updateTransferDialog(bytesRead, array.length, bytesTotal, timeElapsedMillis, this.file.getName());
			}
			
			//Close Stream
			fos.close();
			
			//Wait for Confirmation from Server
			this.updateTransferDialogComplete();
		}
		catch(IOException e)
		{
			AbstractClient.logException(e);
			this.updateTransferDialogError();
		}
		
		this.transferRunning = false;
	}
	
	private void receive()
	{
		Object[] transferData = (Object[]) this.transferManifest.getMessage();
		long bytesTotal = (Long) transferData[1];
		long bytesRemaining = bytesTotal;
		String fileName = (String) transferData[4];
		File file = new File(AbstractIRC.CLIENT_APPLCATION_DIRECTORY + fileName);
		file.deleteOnExit();
		
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			int timeoutCounter = 0;
			
			while(bytesRemaining > 0)
			{
				if(this.queuedBuffers.size() != 0)
				{
					TransferBuffer buffer = this.queuedBuffers.dequeue();
					byte[] byteArray = buffer.getByteArray();
					bytesRemaining -= byteArray.length;
					fos.write(byteArray);
					timeoutCounter = 0;
				}
				else
				{
					try
					{
						timeoutCounter++;
						Thread.sleep(100);
					}
					catch(InterruptedException e)
					{
						AbstractClient.logException(e);
					}
				}
				
				if(timeoutCounter == 120)
				{
					fos.close();
					throw new TransferTimedOutException("File Transfer Timed Out (120 seconds); bytes remaining " + bytesRemaining + "B of total " + bytesTotal + "B");
				}
			}
			
			fos.close();
			this.userInterface.displayFile(file, this.transferManifest);
		}
		catch(TransferTimedOutException | IOException e)
		{
			AbstractClient.logException(e);
			file.delete();
		}
		
		this.transferRunning = false;
	}
	
	public synchronized void bufferReceived(TransferBuffer buffer)
	{
		this.queuedBuffers.enqueue(buffer);
	}
	
	private void updateTransferDialog(long bytesRead, long arraySize, long bytesTotal, long timeElapsedMillis, String filename)
	{
		this.dialog.setProgressColor(TransferProgressDialog.PROGRESS_GREEN);
		this.dialog.setTitle("Filename: " + filename);
		this.dialog.setInformationLabelText(filename);
		this.dialog.setSpeedLabelText(TransferUtilities.computeTransferSpeedText(arraySize, timeElapsedMillis));
		this.dialog.setProgressValue(TransferUtilities.progressPercentageInt(bytesRead, bytesTotal, 0));
		this.dialog.setProgressString(TransferUtilities.computePercentCompletionText(bytesRead, bytesTotal));
		this.dialog.setProgressLabelText(TransferUtilities.computeProgressText(bytesRead, bytesTotal));	
	}
	
	private void updateTransferDialogComplete()
	{
		this.dialog.setWindowTitleBarText("Complete");
		this.dialog.setProgressLabelText("File Transfer Complete");
		this.dialog.setProgressValue(100);
		this.dialog.setProgressColor(TransferProgressDialog.PROGRESS_BLUE);
		
		for(int i = 5; i >= 0; i--)
		{
			this.dialog.setProgressString("Dismissed in " + i + "seconds");
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException e)
			{
				AbstractClient.logException(e);
			}
		}
		
		this.dialog.dispose();
	}
	
	private void updateTransferDialogError()
	{
		this.dialog.setProgressColor(TransferProgressDialog.PROGRESS_RED);
		this.dialog.setProgressString("ERROR OCCURED");
		this.dialog.setProgressLabelText("ERROR OCCURED");
		this.dialog.setWindowTitleBarText("ERROR OCCURED");
	}
	
	public String getTransferID()
	{
		return this.transferID;
	}
	
	public boolean isRunning()
	{
		return this.transferRunning;
	}
}
