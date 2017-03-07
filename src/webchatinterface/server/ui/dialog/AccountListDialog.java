package webchatinterface.server.ui.dialog;

import webchatinterface.server.AbstractServer;
import webchatinterface.server.account.AccountManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

public class AccountListDialog
{
	public static void displayAccountList()
	{
		JFrame accountDialog = new JFrame();
		accountDialog.setTitle("User Accounts");
		accountDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		accountDialog.setResizable(true);
		accountDialog.setSize(600,150);
		
		try
		{
			accountDialog.setIconImage(ImageIO.read(AccountManager.class.getResource("/webchatinterface/server/resources/SERVERICON.png")));
		}
		catch(IOException | IllegalArgumentException e)
		{
			AbstractServer.logException(e);
		}
		
		Container masterPane = accountDialog.getContentPane();
		
		DefaultTableModel tableModel = new DefaultTableModel();
		tableModel.addColumn("Username");
		tableModel.addColumn("Email Address");
		
		JTable userTable = new JTable(tableModel);
		userTable.setPreferredScrollableViewportSize(new Dimension(600,150));
        
        JScrollPane scrollPane = new JScrollPane(userTable);
		masterPane.add(scrollPane);
			
		userTable.setColumnSelectionAllowed(false);
		userTable.setRowSelectionAllowed(true);
		
		
		try
		{
			
			String[][] accountList = AccountManager.retrieveBasicAccountList();
			for(int n = 0; n < accountList[0].length; n++)
			{
				String[] row = new String[2];
				row[0] = accountList[0][n];
				row[1] = accountList[1][n];
				
				tableModel.addRow(row);
			}
		}
		catch (Exception e){}
		
		accountDialog.setVisible(true);
	}
}
