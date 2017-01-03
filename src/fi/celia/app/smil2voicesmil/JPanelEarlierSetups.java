/**
 * 
 */
package fi.celia.app.smil2voicesmil;

import java.awt.LayoutManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
// import javax.swing.DefaultListModel;
import java.awt.BorderLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JSplitPane;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.io.File;

/**
 * This class contains buttons, if they have a value, to selected earlier convert settings 
 * into gui fields (=another tab). And when this application is closing these Properties arrays, when not
 * null, are stored into user home directory as .properties files. A user can also delete
 *  button settings. After every successfully convert, saved Properties instance is
 * added or at least updated into this panel. Before a save, all earlier correspond .properties
 * files are deleted.
 * 
 * @author tkassila2
 *
 */
public class JPanelEarlierSetups extends JPanel implements ActionListener {

	JButton [] arrayJButtons;
	JButton [] arrayRemoveJButtons;
	Properties [] arraygcps;
	Console console;
	public static final int maxbuttons = 8;
	
	/**
	 * 
	 */
	public JPanelEarlierSetups(Console p_console, String strDelete) {
		super();
		init(p_console, strDelete);
	}
	
	private void init(Console p_console, String strDelete)
	{
		console = p_console;
		GridBagConstraints c = new GridBagConstraints();
		GridLayout grid = new GridLayout(maxbuttons,1);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setOneTouchExpandable(false);
		/*
		FlowLayout fl = new FlowLayout();
		fl.
		*/
		splitPane.setDividerLocation(1000);
		JPanel rigthlist = new JPanel ();
		rigthlist.setLayout(grid);
		JPanel leftlist = new JPanel ();
		leftlist.setLayout(grid);

		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		// setLayout(grid);
		arrayJButtons = new JButton [maxbuttons];
		arrayRemoveJButtons = new JButton [maxbuttons];
		arraygcps = new Properties [maxbuttons];
		
		for(int i = 0; i < maxbuttons; i++)
		{
			arrayJButtons[i] = new JButton(""+i);
			arrayJButtons[i].setActionCommand("config " +i);
			arrayJButtons[i].addActionListener(this);
			c.anchor = GridBagConstraints.PAGE_START; //bottom of space
			c.gridx = i;
			c.gridy = 1;
			c.gridwidth = 2;
			c.weighty = 1.5;
			// add(arrayJButtons[i], c);
			leftlist.add(arrayJButtons[i]);
			arrayRemoveJButtons[i] = new JButton(strDelete);
			arrayRemoveJButtons[i].setActionCommand("clean " +i);
			arrayRemoveJButtons[i].addActionListener(this);			
			c.gridy = 2;
			c.gridwidth = 1;
			c.weighty = 0.1;
			c.anchor = GridBagConstraints.PAGE_END; //bottom of space
			arraygcps[i] = console.getUserFileProperty(i);
			if (arraygcps[i] != null)
			{
				String strexecutetype = arraygcps[i].getProperty("executetype");
				String strAdd = strexecutetype;
				if (strexecutetype == null || strexecutetype.equals("2"))
					strAdd = "";
				arrayJButtons[i].setText(arraygcps[i].getProperty("readdir" +strAdd));
			}
			//add(arrayRemoveJButtons[i], c);
			rigthlist.add(arrayRemoveJButtons[i]);
		}

		splitPane.setLeftComponent(leftlist);
	    splitPane.setRightComponent(rigthlist);

	    this.setLayout(new BorderLayout());
		add(splitPane, BorderLayout.NORTH);
		//setLayout(grid);
	}

	public Properties [] getGuiConversionPathSettingsArray()
	{
		return this.arraygcps;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String strcmd = e.getActionCommand();
		if (strcmd == null)
			return;
		if (strcmd.startsWith("config"))
			handleConfigButtonPressed(e);
		else
		if (strcmd.startsWith("clean"))
			handleRemoveButtonPressed(e);
	}
	
	private void handleConfigButtonPressed(ActionEvent e)
	{
		JButton pressed = (JButton)e.getSource();
		if (pressed == null)
			return;
		String visulavalue = pressed.getText();
		if (visulavalue == null || visulavalue.trim().length() == 0)
			return;
		String strcmd = e.getActionCommand();
		String []arraValues = strcmd.split(" ");
		String strIndex = arraValues[1];
		int iIndex = Integer.parseInt(strIndex);
		setSelectedConfigsIntoConversionTab(iIndex);
	}
	
	private void setSelectedConfigsIntoConversionTab(int ind)
	{
		if (ind < 0 || ind >= maxbuttons)
			return ;
		String oldtext = arrayJButtons[ind].getText(); 
		if (oldtext == null || oldtext.trim().length() == 0 || arraygcps[ind] == null)
			return;
		 String ret = console.setNewConfigsOfConsole(ind, arraygcps[ind]);
		 if (ret != null && ret.trim().length() > 0)
 			JOptionPane.showMessageDialog (null, 
 					ret, "Possible Error", JOptionPane.OK_OPTION);
	}
	
	private boolean isAllReadyInButtonTextValues(String newtext, Properties jbuttonProp)
	{
		if (newtext == null || newtext.trim().length() == 0)
			return true;
		
		boolean ret = false;
		String buttonvalue ;
		for(int i = 0 ; i < this.maxbuttons; i++)
		{
			buttonvalue = arrayJButtons[i].getText();
			if (buttonvalue == null || buttonvalue.trim().length() == 0)
				continue;
			if (buttonvalue.equals(newtext))
			{
				arraygcps	[i] = jbuttonProp;
				return true;
			}
		}
		return ret;
	}
	
	public void addNewButtonText(String newtext, Properties jbuttonProp)
	{		
		if (newtext == null || newtext.trim().length() == 0)
			return;
		if (isAllReadyInButtonTextValues(newtext, jbuttonProp))
			return ;
		String oldtext = arrayJButtons[0].getText(); 
		arrayJButtons[0].setText(newtext);
		Properties oldgcps = arraygcps[0];
		arraygcps[0] = jbuttonProp;
		if (oldtext == null || oldtext.trim().length() == 0)
			return;
		setThisTextIntoNextButton(1, oldtext, oldgcps);
	}
	
	private void setThisTextIntoNextButton(int ind, String newtext, Properties oldg)
	{
		if (ind < 0 || ind >= maxbuttons)
			return ;
		String oldtext = arrayJButtons[ind].getText();
		arrayJButtons[ind].setText(newtext);
		Properties oldgcps = arraygcps[ind];
		arraygcps[ind] = oldg;
		if (oldtext == null || oldtext.trim().length() == 0)
			return;
		setThisTextIntoNextButton(++ind, oldtext, oldgcps);
	}
	
	private void handleRemoveButtonPressed(ActionEvent e)
	{
		JButton pressed = (JButton)e.getSource();
		if (pressed == null)
			return;
		String strcmd = e.getActionCommand();
		String []arraValues = strcmd.split(" ");
		String strIndex = arraValues[1];
		int iIndex = Integer.parseInt(strIndex);
		cleanConfigButton(iIndex);
	}

	private void cleanConfigButton(int ind)
	{
		if (ind < 0 || ind >= maxbuttons)
			return ;
		String oldtext = arrayJButtons[ind].getText();
		if (oldtext == null || oldtext.trim().length() == 0)
			return;
		try {
			// if config button contains only a number:
			Double.parseDouble(oldtext);
			return;
		}catch(NumberFormatException  e){
		}
		int nextInd = ind +1;
		List newvalueList = getNextConfigButtonText(nextInd);
		arrayJButtons[ind].setText(newvalueList.get(0).toString());
		arraygcps[ind] = (Properties)newvalueList.get(1);
	}
	
	private List getNextConfigButtonText(int ind)
	{
		if (ind < 0 || ind >= maxbuttons)
		{	
			List mylist = new ArrayList();
			mylist.add("");
			mylist.add(null);
			return mylist;
		}
		int nextInd = ind +1;
		Properties oldprop = arraygcps[ind]; 
		String newvalue = arrayJButtons[ind].getText();
		List newvalue2List = getNextConfigButtonText(nextInd);
		arrayJButtons[ind].setText(newvalue2List.get(0).toString());
		arraygcps[ind] = (Properties)newvalue2List.get(1);
		List mylist = new ArrayList();
		mylist.add(newvalue);
		mylist.add(oldprop);
		return mylist;
	}
	
	/**
	 * @param layout
	 */
	public JPanelEarlierSetups(Console p_console, String strDelete, LayoutManager layout) {
		super(layout);
		init(p_console, strDelete);
	}

	/**
	 * @param isDoubleBuffered
	 */
	public JPanelEarlierSetups(Console p_console, String strDelete, boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		init(p_console, strDelete);
	}

	/**
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public JPanelEarlierSetups(Console p_console, String strDelete, LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		init(p_console, strDelete);
	}
	
	public void changeButtonsTextInto(String newbuttontext)
	{
		for(int i = 0; i < maxbuttons; i++)
		{
			arrayRemoveJButtons[i].setText(newbuttontext);
		}
		this.paint(this.getGraphics());
	}
}
