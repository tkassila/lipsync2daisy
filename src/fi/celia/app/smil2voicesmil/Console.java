package fi.celia.app.smil2voicesmil;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.border.Border;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.nio.channels.FileChannel;
//import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.*;

// import javax.swing.event.*;
//import javax.swing.text.*;

/**
 * This Console class is collecting the output of none graphic classes 
 * into a main graphical-conmponent, window. JFrame. This 
 * class contains all graphic ui of the app. The size of window done
 * with a code: 
 * <code>
 * textArea.setRows(25);
   textArea.setColumns(100);
   </code>
 * 
 * Eräänlainen konsole jfrpropButtonPressedame luokka, joka kerää out.println tulostukset ikkunaan. Suljetaan oikeasta kulmasta.
 * Ikkunan kokoa voi säätää: 
 *  textArea.setRows(25);
    textArea.setColumns(100);
 * <p>
 * @author Tuomas Kassila
 *
 */
public class Console extends JFrame implements ActionListener {
	static final long serialVersionUID = 324343232423434234L;

	private static  Properties justsavedprop = null;
	private String strCurrentLanguage = null;
	static Console thiswindow = null;
	private Dimension windowoldsize;
	public JPanel mainContentPanel;
	private boolean bUnderInit = false;
	public static int iDividerLocation = 280;
			
	ConsoleEditCfg consoleEditCfg = new ConsoleEditCfg();
	GuiHelp help = null;
	
	private JLabel jLabelCorrrectTimes = null;
	private JLabel jlabelApp = null;
	private LipsyncWindowAdapter lipsyncWindowAdapter = null;
	private JTabbedPane jtbConversionTab = null;
	private JPanelEarlierSetups jpanelEarlierSetups = null;
	private static int SIZE = 20;
	
	ReaderThread outThread;
    ReaderThread errThread; 
    PipedInputStream piOut;
    PipedInputStream piErr;
    PipedOutputStream poOut;
    PipedOutputStream poErr;
    JTextArea textArea = new JTextArea();
    boolean bLipsync2smilButtons = false;    
    JButton cfgButton, cfgDirButton, inputPathBuffon, outputPathBuffon, executeButton, 
            propButton, savePropButton, editCfgButton, copyCfgButton, newProjectButton, 
            copyconfigfilesButton, copytextfieldvalueFromConfigButton, helpButton;
    public JTextField textCfg, textCfgPath, textInputPath, textOutputPath, correctTimeText;
    
    boolean bexecuteTypeListUnderRemove = false;
    String[] comboStrings = { "Luo Daisy 2 tiedostot", "Luo Daisy 3 tiedostot" };
    String[] comboSentenceStrings = { "Lipsync-lauseet", "Kappaleen vierek. lauseet", "Dokumentin vierek. lauseet" };
    String[] comboLanguaegStrings = { "Finland", "Englang", "Sweden" };

    private JComboBox<String> languageCombo = new JComboBox<String>(comboLanguaegStrings);
    private JComboBox<String> sentenceCombo = new JComboBox<String>(comboSentenceStrings); 
    JComboBox<String> executeTypeList = new JComboBox<String>(comboStrings);
    JCheckBox lenthMP3Button = new JCheckBox();
    JCheckBox checkBoxXmlValidation = new JCheckBox();

   // private final static String newline = "\n";
    private Lipsync2Daisy lipsync2Smil;
    private final JFileChooser fc = new JFileChooser();
    private boolean bExecutionButtonPressed = false;
    private JSplitPane splitPane;
    private JScrollPane jscrollPaneTextArea;
    
	private class IndexedFocusTraversalPolicy extends 
	  FocusTraversalPolicy {

	   private ArrayList<Component> components = 
	      new ArrayList<Component>();

	   public void addIndexedComponent(Component component) {
	        components.add(component);
	   }

	   @Override
	   public Component getComponentAfter(Container aContainer, 
	               Component aComponent) {
	        int atIndex = components.indexOf(aComponent);
	        int nextIndex = (atIndex + 1) % components.size();
	        return components.get(nextIndex);
	   }

	   @Override
	   public Component getComponentBefore(Container aContainer,
	         Component aComponent) {
	        int atIndex = components.indexOf(aComponent);
	        int nextIndex = (atIndex + components.size() - 1) %
	                components.size();
	        return components.get(nextIndex);
	   }

	   @Override
	   public Component getFirstComponent(Container aContainer) {
	        return components.get(0);
	   }
	   
	   @Override
	   public Component getDefaultComponent(Container aContainer){
		   return components.get(0);
	   }
	   
	   @Override
	   public Component getLastComponent(Container aContainer)
	   {
		   int max = components.size();
		   if (max > 0)
			   return components.get(max-1);
		   return null;
	   }
	}
	
    public Console(String name, boolean p_bLipsync2smilButtons, Lipsync2Daisy p_lipsync2Smil)
    throws IOException
    {    	
    	super(name);
    	lipsync2Smil = p_lipsync2Smil;
    	bLipsync2smilButtons = p_bLipsync2smilButtons;
    	/* an another close listener is in a class LipsyncWindowListerner.java
    	addWindowListener(new WindowAdapter() {
    	  public void windowClosing(WindowEvent evt) {
    	     onExit();
    	  }
    	});	
    	*/
    	init();
    }
    
    /*
    private void onExit()
    {
    	// lipsync2Smil.saveUserProperties();
    	//lipsync2Smil.saveUserPropertiesFromGui(getCfg(), getInputPath(), 
       		// getOutputPath(), getCfgPath(), getExecuteType(), 
       		// getCorrectTime(), getCalculateLengthOfMP3Files());
    }
    */
    
    public Console(String name)
    throws IOException
    {
    	super(name);
    	init();
    }
    
    public Console() throws IOException {
    	super();
    	init();
    }

    private static JPanel getFixedPanel(JComponent jcomp)
    {    	
    	return getFixedPanelWithPossibleEmptyBorder(jcomp, false);
    }

    private static JPanel getFixedPanelWithPossibleEmptyBorder(JComponent jcomp, boolean bNoBoder)
    {
    	JPanel fieldPanel = new JPanel(new BorderLayout());
    	if (jcomp != null)
    	{
    		if (bNoBoder)
    		{
    			Border emptyBorder = BorderFactory.createEmptyBorder();
    			fieldPanel.setBorder(emptyBorder);
    		}    				
    		fieldPanel.add(jcomp, BorderLayout.CENTER);
    	}
    	return fieldPanel;
    }
    
    public void setErrorColorOn()
    {
    	textArea.setForeground(Color.RED);
    }
    
    public void setErrorColorOff()
    {
    	textArea.setForeground(Color.BLACK);
    }
    
    private int getLocaleLanguage(Locale currentlocale)
    {
    	int ret = 0;
    	if (currentlocale != null)
    	{
        	String currentlocalelang = currentlocale.getLanguage();
        	if (currentlocalelang != null)
        	{
	        	if (currentlocalelang.toLowerCase() == "fi")
	        		return 0;
	        	if (currentlocalelang.toLowerCase() == "sw")
	        		return 1;
	        	if (currentlocalelang.toLowerCase() == "en")
	        		return 2;
        	}    		
    	}
    	return ret;
    }
    
    private boolean isDifferentLanguage(String currentLanguage, Locale currentlocale)
    {
    	boolean ret = false;
    	if (currentLanguage == null)
    		return true;
    	if (currentlocale == null)
    		return true;
    	String currentlocalelang = currentlocale.getLanguage();
    	if (currentlocalelang == null)
    		return true;
    	if (currentlocalelang.toLowerCase() == "fi" && currentLanguage == "Finland")
    		return false;
    	if (currentlocalelang.toLowerCase() == "sw" && currentLanguage == "Sweden")
    		return false;
    	if (currentlocalelang.toLowerCase() == "en" && currentLanguage == "England")
    		return false;
    	return true;
    }
    
    private void init()
    throws IOException
    {
    	bUnderInit = true;
    	
    	lipsync2Smil.loadi18Resourcies(lipsync2Smil.getCurrentDir());
    	
    	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	splitPane.setOneTouchExpandable(true);
    	help = new GuiHelp(lipsync2Smil, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_help));
    	// splitPane.setDividerLocation(160);
    	
    	/*
        // Set up System.out
        piOut = new PipedInputStream();
        poOut = new PipedOutputStream(piOut);
        System.setOut(new PrintStream(poOut, true));

        // Set up System.err
        piErr = new PipedInputStream();
        poErr = new PipedOutputStream(piErr);
        System.setErr(new PrintStream(poErr, true));
        */

        int top = 10;
        int left  = 10;
        int bottom = 10;
        int right  = 10;
        textArea.setMargin(new Insets(top, left, bottom, right) );
        textArea.setRows(15);
        textArea.setColumns(100);
        jscrollPaneTextArea = new JScrollPane(textArea);
        /*
        if (!bLipsync2smilButtons)
        {
            getContentPane().add(jscrollPaneTextArea, BorderLayout.CENTER);
        }
        else
        {
        */
        
        	JPanel jPane = new JPanel();
        	GridLayout layout = new GridLayout(6,5);
        	int iHorGap = 4;
        	layout.setHgap(iHorGap);
        	layout.setVgap(iHorGap);
        	layout.setColumns(2);
        	// Border empty = BorderFactory.createEmptyBorder();        	
        	jPane.setLayout(layout);
        	jPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 0));
        	
        	jtbConversionTab = new JTabbedPane();        	
        	helpButton        = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_help) +"...");
        	help.setVisible(false);
        	help = new GuiHelp(lipsync2Smil, helpButton.getText());
            propButton        = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ReadTextFieldValueFromFile) +"...");
        	savePropButton    = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_SaveTextFieldValueIntoFile) +"...");
        	cfgButton        = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ChangeCfgFile) +"...");
        	editCfgButton    = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_EditCfgFile) +"...");
        	copyCfgButton    = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_CopyCfgFile) +"...");
        	newProjectButton    = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_NewProject) +"...");
        	copyconfigfilesButton = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_Copyconfigfiles) +"...");
        	copytextfieldvalueFromConfigButton = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_copytextfieldvalueFromConfig) +"...");
        	inputPathBuffon  = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_changeintputdir) +"...");
        	cfgDirButton  = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_changecfgdir) +"...");
        	outputPathBuffon = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_changeoutputdir) +"...");
        	executeButton = new JButton(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_executeconvert));
            executeButton.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_executeButtontip));
        	textCfg = new JTextField(25);
        	textCfgPath = new JTextField(25);
        	textInputPath = new JTextField(25);
        	textOutputPath = new JTextField(25);
        	correctTimeText = new JTextField(5);
        	correctTimeText.setText("0.0");
        	           
            //Add Components to this panel.
        	/*
            GridBagConstraints c = new GridBagConstraints();
            // c.gridwidth = GridBagConstraints.REMAINDER;
            // c.gridheight = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            // c.fill = GridBagConstraints.VERTICAL;
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            jPane.add(addFieldPanel(cfgButton, textCfg), c);
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.weightx = 0.5;
            c.gridx = 1;
            c.gridy = 0;
            jPane.add(addFieldPanel(inputPathBuffon, textInputPath), c);
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 1;
            jPane.add(addFieldPanel(outputPathBuffon, textOutputPath), c);
            c.weightx = 0.1;
            c.gridx = 1;
            c.gridy = 1;
            jPane.add(executeButton, c); 
            */
        	
        	// FlowLayout experimentLayout = new FlowLayout( FlowLayout.LEFT, 2, 2 );
        	//jPane.setLayout(new BoxLayout(jPane, BoxLayout.LINE_AXIS));
        	// jPane.setLayout(experimentLayout);
        	// experimentLayout.setAlignment(FlowLayout.TRAILING);
        	/*
            jPane.add(addFieldPanel(cfgButton, textCfg));
            jPane.add(addFieldPanel(inputPathBuffon, textInputPath));
            jPane.add(addFieldPanel(outputPathBuffon, textOutputPath));
            jPane.add(executeButton);
            */ 

        	// executeTypeList.setSelectedIndex(1);
            executeTypeList.addActionListener(this);
            sentenceCombo.addActionListener(this);
            languageCombo.addActionListener(this);
            helpButton.addActionListener(this);
        	// executeTypeList.addActionListener(this);
            JPanel propPanel = getFixedPanel(null);
            jpanelEarlierSetups = new JPanelEarlierSetups(this, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_delete));
            JPanel jpanelearlier = getFixedPanel(jpanelEarlierSetups);
            propPanel.add(newProjectButton, BorderLayout.WEST);
            propPanel.add(copyconfigfilesButton, BorderLayout.CENTER);
            // propPanel.add(propButton, BorderLayout.EAST);            
            jPane.add(propPanel);
            // jPane.add(propButton);
            
            // jPane.add(propButton, BorderLayout.EAST);
            JPanel savePropPanel = getFixedPanel(null);
            savePropPanel.add(propButton, BorderLayout.WEST);
            savePropPanel.add(savePropButton, BorderLayout.CENTER);
            jPane.add(savePropPanel);
            //jPane.add(getFixedPanel(savePropButton));
            jPane.add(getFixedPanel(textCfg));
            JPanel panelCfg = getFixedPanel(null);
            panelCfg.add(cfgButton, BorderLayout.WEST);
            panelCfg.add(editCfgButton, BorderLayout.CENTER);
            panelCfg.add(copyCfgButton, BorderLayout.EAST);
            jPane.add(getFixedPanel(panelCfg));
            jPane.add(getFixedPanel(textCfgPath));            
            JPanel panelCfgPathButton = getFixedPanel(null);
            panelCfgPathButton.add(cfgDirButton, BorderLayout.WEST);
            panelCfgPathButton.add(copytextfieldvalueFromConfigButton, BorderLayout.CENTER);
            // jPane.add(getFixedPanel(cfgDirButton));
            jPane.add(panelCfgPathButton);
            // jPane.add(getFixedPanel(cfgDirButton));
            jPane.add(getFixedPanel(textInputPath));
            jPane.add(getFixedPanel(inputPathBuffon));
            jPane.add(getFixedPanel(textOutputPath));
            jPane.add(getFixedPanel(outputPathBuffon));
            JPanel panel = new JPanel();
        	GridLayout layout2 = new GridLayout(1,1);
        	// layout2.setAutoCreateGaps(true);
        	int iHorGap2 = 1;
        	layout2.setHgap(iHorGap2);
        	layout2.setVgap(iHorGap2);
        	layout2.setColumns(2);
        	// panel.setLayout(layout2);        
            panel.add(executeTypeList);
            jLabelCorrrectTimes = new JLabel(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_correctimes));
            panel.add(jLabelCorrrectTimes);
            panel.add(correctTimeText);
            /*
            Border emptyBorder = BorderFactory.createEmptyBorder();
            */
            JPanel p = new JPanel();
            // p.setBorder(emptyBorder);
            p.add(sentenceCombo);
            panel.add(p);
            JPanel checkBoxXmlValidationPanel = new JPanel();
            checkBoxXmlValidationPanel.add(lenthMP3Button, BorderLayout.NORTH);
            checkBoxXmlValidationPanel.add(checkBoxXmlValidation, BorderLayout.SOUTH);
            // panel.add(checkBoxXmlValidationPanel);
            // panel2.setLayout(layout2);
            // panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));            
            jPane.add(getFixedPanel(panel));
            JPanel execPanel = this.getFixedPanel(null);
            execPanel.add(checkBoxXmlValidationPanel, BorderLayout.WEST);
            execPanel.add(executeButton, BorderLayout.CENTER);
            Border emptyBorder2 = BorderFactory.createEmptyBorder();
            execPanel.setBorder(emptyBorder2);
            jPane.add(getFixedPanelWithPossibleEmptyBorder(execPanel, true));
    
            getContentPane().setLayout(new BorderLayout());
            
            JPanel labelPanel = new JPanel();
            ((JComponent)getContentPane()).setBorder(   
                    BorderFactory.createEmptyBorder( 10, 5, 10, 5 ) );  
            JPanel jMainPanel = new JPanel();
        	GridLayout layoutMain = new GridLayout(1,1);
        	// int iHorGap = 4;
        	layoutMain.setHgap(iHorGap);
        	layoutMain.setVgap(iHorGap);
        	//layoutMain.setColumns(2);

            jMainPanel.setLayout(layoutMain);
            JPanel helppanel = new JPanel ();
            helppanel.setLayout(layoutMain);
            helppanel.add(new JLabel("Language:"), BorderLayout.WEST);
            // helppanel.add(new JLabel(" "), BorderLayout.EAST);            
            helppanel.add(languageCombo, BorderLayout.CENTER);
            labelPanel.add(helppanel, BorderLayout.WEST);
            labelPanel.add(helpButton, BorderLayout.EAST);
            // labelPanel.add(new JLabel(" "), BorderLayout.CENTER);
            JPanel mainlabelPanel = new JPanel();
            mainlabelPanel.setLayout(new BorderLayout());
            JLabel labelApp = new JLabel("           Lipsync2Daisy        ");
            labelApp.setFont(new Font("Helvetica", Font.BOLD, 20));
            mainlabelPanel.add(labelApp, BorderLayout.WEST);
            jlabelApp = new JLabel(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_appdescription));
            mainlabelPanel.add(jlabelApp, BorderLayout.CENTER);
            // mainlabelPanel.add(new JLabel("                 "), BorderLayout.CENTER);
            mainlabelPanel.add(labelPanel, BorderLayout.EAST);
            mainContentPanel = new JPanel();
            mainContentPanel.add(mainlabelPanel, BorderLayout.NORTH);
            // getContentPane().add(jPane, BorderLayout.NORTH);
            Dimension jPanelDimension = new Dimension(150, 150);
            /*
            Box box = new Box(BoxLayout.Y_AXIS);
            box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            box.add(Box.createVerticalGlue());
            box.add(jPane);
            box.add(Box.createVerticalGlue());
            */
            jPane.setPreferredSize(jPanelDimension);
            jPane.setMaximumSize(jPanelDimension);
            jPane.setMinimumSize(jPanelDimension);
            /*
            box.setPreferredSize(jPanelDimension);
            box.setMaximumSize(jPanelDimension);
            box.setMinimumSize(jPanelDimension);
            */
            
            // getContentPane().add(jPane, BorderLayout.CENTER);
            // getContentPane().add(box, BorderLayout.CENTER);
            
            // getContentPane().add(new JSeparator(), BorderLayout.CENTER);
            // getContentPane().add(jscrollPaneTextArea, BorderLayout.SOUTH);
            
            /*
            jMainPanel.add(labelPanel, BorderLayout.NORTH);
            jMainPanel.add(jPane, BorderLayout.CENTER);
            */
            // jMainPanel.add(new JSeparator(), BorderLayout.CENTER);
            
            // jMainPanel.setMinimumSize(jMainPanel.getPreferredSize());
            // getContentPane().add(jMainPanel, BorderLayout.CENTER);
            JPanel jscroll = new JPanel();
        	/// GridLayout layoutScroll = new GridLayout(1,1);
        	// int iHorGap = 4;
            //layoutScroll.setHgap(iHorGap);
        	//layoutScroll.setVgap(iHorGap);
            // jscroll.setLayout(layoutScroll);
            /*
            jscroll.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            jscroll.add(jscrollPaneTextArea);
            */	
            // getContentPane().add(jscroll, BorderLayout.SOUTH);
            
            /*
            JPanel apperPanel = new JPanel();
            apperPanel.setLayout(new BorderLayout());
            apperPanel.add(jPane, BorderLayout.NORTH);
            */
            
            jtbConversionTab.addTab(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_convertionrun), null, jPane, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_convertionrun_helptext));
            jtbConversionTab.addTab(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_earlierconvertruns), null, jpanelearlier, "");
            jtbConversionTab.setSelectedIndex(0);

            // apperPanel.add(new JLabel(" "), BorderLayout.SOUTH);
          //Create a split pane with the two scroll panes in it.
            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            		jtbConversionTab, /* jscroll */ jscrollPaneTextArea);
            splitPane.setOneTouchExpandable(true);
            splitPane.setContinuousLayout(true);
            splitPane.setDividerLocation(iDividerLocation);
            splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            	@Override
            	public void propertyChange(PropertyChangeEvent pce) {
            		/*
            		jscroll.getSize().getWidth()
            		jscroll.getSize().getHeight()
            		*/
            	}
           	});
            
            jpanelEarlierSetups.changeButtonsTextInto(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_delete));
            
            mainlabelPanel.add(splitPane, BorderLayout.SOUTH);
            mainContentPanel.add(mainlabelPanel, BorderLayout.SOUTH);
            getContentPane().add(mainContentPanel, BorderLayout.CENTER);

            //splitPane.setLeftComponent(mainlabelPanel);
            //splitPane.setLeftComponent(jMainPanel);
            //splitPane.setRightComponent(jscroll);
            //splitPane.setRightComponent(this.textArea);
            // getContentPane().add(splitPane, BorderLayout.SOUTH);

            //Provide minimum sizes for the two components in the split pane
            Dimension minimumSize = new Dimension(100, 47);
            // listScrollPane.setMinimumSize(minimumSize);
            // pictureScrollPane.setMinimumSize(minimumSize);
            /*
            jMainPanel.add(jPane, BorderLayout.CENTER);
            // splitPane.setResizeWeight(0.5);
            splitPane.setLeftComponent(jMainPanel);
            splitPane.setRightComponent(jscroll);
            getContentPane().add(splitPane, BorderLayout.SOUTH);
			*/
                        
            jPane.paint(this.getGraphics());
            // jMainPanel.setBackground(Color.BLACK);
           
            //this.lipsync2Smil.setUILanguage(null);
            // this.lipsync2Smil.readUserProperties();
            
            /*
            thiswindow = this;
            this.getRootPane().addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    // This is only called when the user releases the mouse button.
                	Dimension newdim = thiswindow.getSize();
                	if (windowoldsize != null && newdim != null && !newdim.equals(windowoldsize))
                	{
                		?*
                		getContentPane().remove(thiswindow.mainContentPanel);
                		getContentPane().add(thiswindow.mainContentPanel, BorderLayout.CENTER);
                		/* thiswindow.getContentPane().setSize(newdim);
                		thiswindow.getContentPane().setPreferredSize(newdim);
                		*?
	                	// thiswindow.getContentPane().update(thiswindow.getContentPane().getGraphics());
                		if ((thiswindow.getWidth() % SIZE != 0) || (thiswindow.getHeight() % SIZE != 0)) {
                            int screenWidth = ((thiswindow.getWidth() + SIZE) / SIZE) * SIZE;
                            int screenHeight = ((thiswindow.getHeight() + SIZE) / SIZE) * SIZE;
                            thiswindow.setSize(screenWidth, screenHeight);
                            thiswindow.getContentPane().update(thiswindow.getContentPane().getGraphics());
                        }
                	}
                }
            });
            */
               
            executeButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		executeButtonPressed(e);
            	}
            });
            
            newProjectButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		newProjectPressed(e);
            	}
            });
                        
            copytextfieldvalueFromConfigButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		copytextfieldvalueFromConfigButtonPressed(e);
            	}
            });
            
                        
            copyconfigfilesButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		copyconfigfilesButtonPressed(e);
            	}
            });
            
            propButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		propButtonPressed(e);
            	}
            });

            savePropButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		savePropButtonPressed(e);
            	}
            });

            cfgButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		cfgButtonPressed(e);
            	}
            });
            
            editCfgButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		editCfgButton(e);
            	}
            });
            
            copyCfgButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		copyCfgButton(e);
            	}
            });
            
            inputPathBuffon.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		inputPathBuffonPressed(e);
            	}
            });
            
            cfgDirButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		cfgPathBuffonPressed(e);
            	}
            });
            
            outputPathBuffon.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		outputPathBuffonPressed(e);
            	}
            });
            
        // }
            
            IndexedFocusTraversalPolicy policy = new IndexedFocusTraversalPolicy();
            
            // policy.addIndexedComponent(cfgButton);
            policy.addIndexedComponent(textCfg);
            // policy.addIndexedComponent(inputPathBuffon);
            policy.addIndexedComponent(textInputPath);
            // policy.addIndexedComponent(outputPathBuffon);
            policy.addIndexedComponent(textOutputPath);
            // policy.addIndexedComponent(executeButton);
            setFocusTraversalPolicy(policy);
                                
        lipsyncWindowAdapter = new LipsyncWindowAdapter(this, this.lipsync2Smil);
        // setVisible(true);

        languageUIChanged();
        
 		String strInstallDir = System.getProperty("user.dir"); // first value is installdir
 		File installdir = new File(strInstallDir);
 		boolean bSetValuesAllways = false;
 		try {
 			boolean underStartedExecution = true;
 			setMissingTextFieldValues(installdir, this.lipsync2Smil.getoutputpathname(), this.lipsync2Smil.getinputpathname(), bSetValuesAllways, underStartedExecution);
    	} catch(Exception e2){
    		try {
    			JOptionPane.showMessageDialog (this, 
     	 			e2.getMessage(), "Missing textfield values", JOptionPane.OK_OPTION);
   			 }catch(Exception e3){	     	 				    				 
   			 }
    	} 		    		
        bUnderInit = false;
        addWindowListener(lipsyncWindowAdapter);   
        windowoldsize = this.getSize();
    //    pack();
      //  this.setVisible(true);
    }
    
    private String [] getSearchTemplaFilesInSubs(File fparent)
    {
    	// { ["daisy2templates", "daisy3templates"], File.exists, "*/*.tmpl"
    	String [] ret = null;
    	    	
    	if (!fparent.exists())
    		return null;

    	String [] arrfchildren = fparent.list();
    	if (arrfchildren == null || arrfchildren.length == 0)
    	{
    		return null;
    	}

    	File f = new File(fparent +File.separator +"config");
    	if (!f.exists())
    		return null;
    	
    	arrfchildren = f.list();
    	if (arrfchildren == null || arrfchildren.length == 0)
    	{
    		return null;
    	}
    	
    	File f2 = new File(f +File.separator +"daisy2templates");
    	if (!f2.exists())
    		return null;

       	arrfchildren = f2.list();
    	if (arrfchildren == null || arrfchildren.length == 0)
    	{
    		return null;
    	}
    
    	File f3 = new File(f +File.separator +"daisy3templates");
    	if (!f3.exists())
    		return null;
    	    	
       	arrfchildren = f3.list();
    	if (arrfchildren == null || arrfchildren.length == 0)
    	{
    		return null;
    	}

    	boolean founded_daisy2templates = false;
    	for(File f4 : f2.listFiles())
    	{
    		if (f4 == null || !f4.exists() || f4.isDirectory())
    			continue;
    		if (f4.getName().endsWith(".tmpl"))
    			founded_daisy2templates = true;
    	}
    	
    	boolean founded_daisy3templates = false;
    	for(File f4 : f3.listFiles())
    	{
    		if (f4 == null || !f4.exists() || f4.isDirectory())
    			continue;
    		if (f4.getName().endsWith(".tmpl"))
    			founded_daisy3templates = true;
    	}
    	
    	if (founded_daisy2templates && founded_daisy3templates)
    	{
    		ret = new String[2];
    		ret[0] = f2.getAbsolutePath();
    		ret[1] = f3.getAbsolutePath();
    		return ret;
    	}
    	
        Locale currentlocale = Locale.getDefault();
        String currentLanguage = (String)languageCombo.getSelectedItem();
        if (currentLanguage == null || isDifferentLanguage(currentLanguage, currentlocale))
        {
        	languageCombo.setSelectedIndex(getLocaleLanguage(currentlocale));
        }
        
        this.invalidate();
        this.validate();
        this.repaint();
        
    	return ret;
    }
    
    private boolean isLipSyncXmlDir(File f)
    {
    	boolean ret = false;
    	if (f == null || !f.exists() || f.isFile())
    		return false;
    		
    	int iXmlCounter = 0;
    	int iNoneXmlCounter = 0;
    	try {
    		// System.out.println ("f " +f.getAbsolutePath());
    	String [] arrfchildren = f.list();
    	if (arrfchildren == null || arrfchildren.length == 0)
    	{
    		return false;
    	}
    	
    	for(File f4 : f.listFiles())
    	{
    		if (f4 == null)
    			continue;
    		
    		// System.out.println ("f4 " +f4.getAbsolutePath());
    		
    		if (f4.isDirectory())
    			continue;
    		if (f4.getName().toLowerCase().endsWith(".xml~") || f4.getName().toLowerCase().endsWith(".bak"))
    			continue;
    		if (!f4.getName().toLowerCase().endsWith(".xml"))
    		{
    			iNoneXmlCounter++;
    			continue;
    		}
    		iXmlCounter++;
    	}
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	if (iNoneXmlCounter < 3 && iXmlCounter > 0)
    		ret = true;

    	return ret;
    }
    
    private String [] getSearchLipsyncFilesInSubs(File fparent)
    {
    	String [] ret = null;
    	  	
		if (fparent == null || !fparent.exists())
			return null;
		
    	String [] arrfchildren = fparent.list();
    	if (arrfchildren == null || arrfchildren.length == 0)
    	{
    		return null;
    	}
		
    	List<String> lipsyncdirs = new ArrayList<String>(); 
    	for(File f4 : fparent.listFiles())
    	{
    		if (f4 == null || !f4.exists() || f4.isFile())
    			continue;
    		if (isLipSyncXmlDir(f4))
    			lipsyncdirs.add(f4.getAbsolutePath());
    	}
    	    	    	
    	if (lipsyncdirs.size() > 0)
    	{
    		ret = new String[lipsyncdirs.size()];
    		int i = 0;
    		for(String s : lipsyncdirs)
    			ret[i++] = s;
    	}
    	
    	return ret;
    }
    
    private boolean isOutputDir(File f)
    {
    	boolean ret = false;
    	if (f == null || !f.exists() || f.isFile())
    		return false;
    		
    	int iOutputDirCounter = 0;
    	int iNoneOutputDirCounter = 0;
    	String [] arrfchildren = f.list();
    	if (arrfchildren == null || arrfchildren.length == 0)
    	{
    		return false;
    	}

    	for(File f4 : f.listFiles())
    	{
    		if (f4.isDirectory())
    			continue;
    		if (!f4.getName().toLowerCase().endsWith(".mp3"))
    		{
    			iNoneOutputDirCounter++;
    			continue;
    		}
    		iOutputDirCounter++;
    	}
    	
    	if (iOutputDirCounter > 0)
    		ret = true;

    	return ret;
    }
    
    
    private String [] getSearchOutputFilesInSubs(File fparent)
    {
    	String [] ret = null;
    	  	
		if (!fparent.exists())
			return null;
		
    	String [] arrfchildren = fparent.list();
    	if (arrfchildren == null || arrfchildren.length == 0)
    	{
    		return null;
    	}

		
    	List<String> outputdirs = new ArrayList<String>(); 
    	for(File f4 : fparent.listFiles())
    	{
    		if (f4 == null || !f4.exists() || f4.isFile())
    			continue;
    		if (isOutputDir(f4))
    			outputdirs.add(f4.getAbsolutePath());
    	}
    	    	    	
    	if (outputdirs.size() > 0)
    	{
    		ret = new String[outputdirs.size()];
    		int i = 0;
    		for(String s : outputdirs)
    			ret[i++] = s;
    	}
    	
    	return ret;
    }
    
    private void setMissingTextFieldValues(File bookdir, String outputpathname, String inputpathname, boolean bSetValuesAllways, boolean bUnderStartedExecution)
    throws Exception
    {
    	if (outputpathname == null || bookdir == null || !bookdir.exists())
    		return;
    	
 		String strInstallDir = System.getProperty("user.dir"); // first value is installdir
 		File installdir = new File(strInstallDir);

    	// lipsyn2smil3.cfg
    	// C:\java\project\celia\smil2voicesmil\config
    	// C:\java\project\celia\smil2voicesmil\20130825\Lipsync_export
    	// C:\java\project\celia\smil2voicesmil\20130825\tulos
      	String strTextOutputPath = textOutputPath.getText();
      	String strTextCfg = textCfg.getText();
      	String strTextCfgPath = textCfgPath.getText();
      	String strTextInputPath = textInputPath.getText();
    	
      	boolean isEmptytextOutputPath = (strTextOutputPath == null || strTextOutputPath.trim().length() == 0);
    	boolean isEmptytextCfg = (strTextCfg == null || strTextCfg.trim().length() == 0);
    	boolean isEmptytextCfgPath = (strTextCfgPath == null || strTextCfgPath.trim().length() == 0);
    	boolean isEmptytextInputPath = (strTextInputPath == null || strTextInputPath.trim().length() == 0);

    	String value;
  		String [] templatedirs = getSearchTemplaFilesInSubs(bookdir);
		String [] lipsyncdirs = getSearchLipsyncFilesInSubs(bookdir);
		String [] outputcdirs = getSearchOutputFilesInSubs(bookdir);

		/*
		if (templatedirs == null) // if config does not exists 
		{
    		value = bookdir.getAbsolutePath() +File.separator +"config";    			
    		File f = new File(value);
			throw new Exception(lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_configdir_does_not_exist) +": " +f.getAbsolutePath());
		}
		*/
	
    	if (bSetValuesAllways || isEmptytextOutputPath)
    	{
    		// textCfg, textCfgPath, textInputPath, textOutputPath
    		value = bookdir.getAbsolutePath() +File.separator +outputpathname;
    		File f = new File(value);    
    		
    		if (outputcdirs != null)
    		{
    			if (outputcdirs.length > 1)
    			{
    				if (!bUnderStartedExecution)
    				{
	    			    String[] options = outputcdirs;
	    			    JComboBox<String> optionList = new JComboBox<String>(outputcdirs);
	                    optionList.setSelectedIndex(0);
	                    JPanel jpan = new JPanel ();
	                    jpan.add(new JLabel(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_selectfromoutputdirs) +":"));
	                    jpan.add(optionList);
	                    /*
	    			    int n = JOptionPane.showOptionDialog(this, lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_selectfromoutputdirs) +":",
	    			    	 lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_selectfiles),
	    				     JOptionPane.DEFAULT_OPTION,
	    				     JOptionPane.QUESTION_MESSAGE,
	    				     null,
	    				     options,
	    				     options[0]);
	    				 */
	    			    int n = JOptionPane.showOptionDialog(this, jpan,
		    			    	 lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_selectfiles),
		    				     JOptionPane.DEFAULT_OPTION,
		    				     JOptionPane.QUESTION_MESSAGE,
		    				     null,
		    				     null,
		    				     null);
	    			    if (n != -1)
	    			    	n = optionList.getSelectedIndex();
	    			    if (n == -1)
	    			    {
	    			    	StringBuffer sb = new StringBuffer ();
	    			    	for(String n2 : outputcdirs)
	    			    		sb.append(n2 +" ");
	    			    	throw new Exception(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_noselection) +": " +sb.toString());
	    			    }
					 	value = outputcdirs[n]; 
					 	f = new File(value);
    				}
    				else
    				{
    					;
    				}
    			}
    			else
    			{
    				value = outputcdirs[0];
    				f = new File(value);
    			}
    		}
    		if (!f.exists())
    		{
    			if (!f.mkdir())
    				throw new Exception(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cannotcreatedir) +": " +f.getAbsolutePath());
    		}
    		textOutputPath.setText(value);
    	}
    	if (bSetValuesAllways || isEmptytextCfgPath)
    	{
    		// textCfg, textCfgPath, textInputPath, textOutputPath
    		value = bookdir.getAbsolutePath() +File.separator +"config";    			
    		textCfgPath.setText(value);
    	}    	
    	if (bSetValuesAllways || isEmptytextInputPath)
    	{
    		// textCfg, textCfgPath, textInputPath, textOutputPath
    		value = bookdir.getAbsolutePath() +File.separator +inputpathname;
    		File f = new File(value);
    		
    		if (lipsyncdirs != null)
    		{
    			if (lipsyncdirs.length > 1)
    			{
    				if (!bUnderStartedExecution)
    				{
	    			    String[] options = lipsyncdirs;
	    			    JComboBox<String> optionList = new JComboBox<String>(outputcdirs);
	                    optionList.setSelectedIndex(0);
	                    JPanel jpan = new JPanel ();
	                    jpan.add(new JLabel(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_selectfrominputdirs) +":"));
	                    jpan.add(optionList);
	                    /*
	    			    int n = JOptionPane.showOptionDialog(this, lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_selectfrominputdirs) +":",
	    			    	 lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_selectfiles),
	    				     JOptionPane.DEFAULT_OPTION,
	    				     JOptionPane.QUESTION_MESSAGE,
	    				     null,
	    				     options,
	    				     options[0]);
	    				 */
	    			    int n = JOptionPane.showOptionDialog(this, jpan,
		    			    	 lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_selectfiles),
		    				     JOptionPane.DEFAULT_OPTION,
		    				     JOptionPane.QUESTION_MESSAGE,
		    				     null,
		    				     null,
		    				     null);
	    			    if (n != -1)
	    			    	n = optionList.getSelectedIndex();
	    			    if (n == -1)
	    			    {
	    			    	StringBuffer sb = new StringBuffer ();
	    			    	for(String n2 : lipsyncdirs)
	    			    		sb.append(n2 +" ");
	    			    	throw new Exception(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_noselection) +": " +sb.toString());
	    			    }
					 	value = lipsyncdirs[n]; 
					 	f = new File(value);
    				}
    				else
    				{
    					;
    				}
    			}
    			else
    			{
    				value = lipsyncdirs[0];
    				f = new File(value);
    			}
    		}
    		
    		if (!f.exists())
    			if (!f.mkdir())
    				throw new Exception(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cannotcreatedir) +": " +f.getAbsolutePath());
    		textInputPath.setText(value);
    		textInputPath.paint(textInputPath.getGraphics());
    	}
    	if (bSetValuesAllways || isEmptytextCfg)
    	{
    		// textCfg, textCfgPath, textInputPath, textOutputPath
    		value = "lipsyn2smil3.cfg";
    		if (executeTypeList.getSelectedIndex() == 0)
    			value = "lipsyn2smil2.cfg";
    		textCfg.setText(value);
    		textCfg.getParent().paint(textCfg.getGraphics());
    	}
    	// textCfg, textCfgPath, textInputPath, textOutputPath
    }

    private void languageUIChanged()
    {    	
    	int ilanguageComboSelected = languageCombo.getSelectedIndex();
    	if (strCurrentLanguage == null)
    		strCurrentLanguage = "England";
    	String tmp_strUILanguage = lipsync2Smil.getUILanguage();
    	if (tmp_strUILanguage != null)
    		strCurrentLanguage = tmp_strUILanguage;
    	else
    		lipsync2Smil.setUILanguage(strCurrentLanguage);
    	if (strCurrentLanguage != null)
    	{
    		String currLang = (String)languageCombo.getSelectedItem();
    		if (!strCurrentLanguage.equals(currLang))
    			languageCombo.setSelectedItem(strCurrentLanguage);		
    	}    		
    
    	comboStrings = lipsync2Smil.getComboStrings();
    	comboSentenceStrings = lipsync2Smil.getComboSentenceStrings();    	

    	int iSentenceComboSelected = sentenceCombo.getSelectedIndex();
    	int iExecuteTypeListSelected = executeTypeList.getSelectedIndex();
    	
    	bexecuteTypeListUnderRemove = true;
    	executeTypeList.removeAllItems();
    	for(String item : comboStrings)
    		executeTypeList.addItem(item);    	
    	bexecuteTypeListUnderRemove = false;
    	
    	sentenceCombo.removeAllItems();
    	for(String item : this.comboSentenceStrings)
    		sentenceCombo.addItem(item);
    	
    	sentenceCombo.setSelectedIndex(iSentenceComboSelected);
    	
    	int tmpExecutetype = this.lipsync2Smil.getExecutetype();
    	if (tmpExecutetype > -1)
    	{
    		executeTypeList.setSelectedIndex(tmpExecutetype -2);
    	}
    	else
    		executeTypeList.setSelectedIndex(1);
    	
    	jlabelApp.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_appdescription));
    	jLabelCorrrectTimes.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_correctimes));    	
    	checkBoxXmlValidation.setSelected(true);
    		checkBoxXmlValidation.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_xml_validationtip));
        lenthMP3Button.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_usemp3lengthtip));
        executeTypeList.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_converttypetip));
        lenthMP3Button.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_usemp3length));
        /*
        jtbConversionTab.addTab(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_convertionrun), null, jPane, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_convertionrun_helptext));
        jtbConversionTab.addTab(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_earlierconvertruns), null, jpanelearlier, "");
        */
        jtbConversionTab.setTitleAt(0, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_convertionrun));
        jtbConversionTab.setTitleAt(1, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_earlierconvertruns));
        jtbConversionTab.setToolTipTextAt(0, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_convertionrun_helptext));
        
        sentenceCombo.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_sentenceCombotip));
        checkBoxXmlValidation.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_xml_validation));
        // Add a scrolling text area
        textArea.setEditable(false);

        helpButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_help) +"...");
    	propButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ReadTextFieldValueFromFile) +"...");
    	savePropButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_SaveTextFieldValueIntoFile) +"...");
    	cfgButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ChangeCfgFile) +"...");
    	editCfgButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_EditCfgFile) +"...");
    	copyCfgButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_CopyCfgFile) +"...");
    	newProjectButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_NewProject) +"...");
    	copyconfigfilesButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_Copyconfigfiles) +"...");
    	inputPathBuffon.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_changeintputdir) +"...");
    	cfgDirButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_changecfgdir) +"...");
    	outputPathBuffon.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_changeoutputdir) +"...");
    	executeButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_executeconvert));
        executeButton.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_executeButtontip));
    	correctTimeText.setText("0.0");
    	
        correctTimeText.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_correctimestip));
        textCfg.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfgfiletip));        
        textCfgPath.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_templatepathtip));
        textInputPath.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_inputpathtip));
        textOutputPath.setToolTipText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_outputpathtip));

    	help.setVisible(false);
    	try {
    	help = new GuiHelp(lipsync2Smil, helpButton.getText());
    	}catch(Exception e)
    	{
    		
    	}
      
        if (this.consoleEditCfg != null)
        {
        	consoleEditCfg.setVisible(false);
        }
        if (this.help != null)
        {
        	help.setVisible(false);
        }
        jpanelEarlierSetups.changeButtonsTextInto(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_delete));
        this.update(this.getGraphics());
    }
    
    private void executeTypeListChanged(String executeMode)
    {
    	if (bexecuteTypeListUnderRemove)
    		return;
    	
    	Object console = lipsync2Smil.getConsole();
    	if (executeMode != null && console != null)
    	{
    		lipsync2Smil.setExecutetype(executeMode);
    		String fcfgfname = textCfg.getText();
    		if (fcfgfname != null && fcfgfname.trim().length() > 0)
    		{
    			if ("lipsyn2smil2.cfg".equals(fcfgfname))
    			{
    				if (lipsync2Smil.getExecutetype() == Lipsync2Daisy.cnstExecuteDaisy3)
    					textCfg.setText("lipsyn2smil3.cfg");
    			}
    			else
       			if ("lipsyn2smil3.cfg".equals(fcfgfname))
       			{
       				if (lipsync2Smil.getExecutetype() == Lipsync2Daisy.cnstExecuteDaisy2)
       					textCfg.setText("lipsyn2smil2.cfg");
       			}
    		}
    		else
    		{
    			if (lipsync2Smil.getExecutetype() == Lipsync2Daisy.cnstExecuteDaisy2)
   					textCfg.setText("lipsyn2smil2.cfg");
    			else
    			if (lipsync2Smil.getExecutetype() == Lipsync2Daisy.cnstExecuteDaisy3)
    				textCfg.setText("lipsyn2smil3.cfg");
    				
    		}
    	}
    }
    
    public String getCfg() { return textCfg.getText(); }
    public String getCfgPath() { return textCfgPath.getText(); }
    public String getInputPath() { return textInputPath.getText(); }
    public String getOutputPath() { return textOutputPath.getText(); }
    public String getExecuteType() {
    	if (executeTypeList.getSelectedItem() != null)
    		return executeTypeList.getSelectedItem().toString();
    	return null;
    }    
    
    public boolean getCalculateLengthOfMP3Files() { return lenthMP3Button.isSelected(); }
    public boolean getXmlValidation() { return checkBoxXmlValidation.isSelected(); }
    public String getCorrectTime() { return correctTimeText.getText(); }
    private String strSmilTemplateDir;

    private void copyconfigfilesButtonPressed(ActionEvent e)
    {
    	String strNewConfigCopyTitle = lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_copybookdirconfig);
    	String strSelectCopyIntoConfigCopyTitle = lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_selectconfigdirtocopy);
    	String strCopyIntoConfigCopyTitle = lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_NewProject) +" " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_copyed);
    	boolean bCreateNewSubDir = false;
    	copyConfigFiles(strNewConfigCopyTitle, strSelectCopyIntoConfigCopyTitle, strCopyIntoConfigCopyTitle, bCreateNewSubDir);
    }
    
    private void copytextfieldvalueFromConfigButtonPressed(ActionEvent e)
    {
    	String strNewConfigCopyTitle = "Set text field values";
    	try {
	 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	 		fc.setDialogTitle(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_selectdirwherenextfieldsareset));
	 		fc.setApproveButtonText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
	 		int returnVal = fc.showOpenDialog(this);
	 		 if (returnVal == JFileChooser.APPROVE_OPTION) 
	    	 { 			  			
	 			String selecteddir = fc.getSelectedFile().getAbsolutePath();
	 		    if (selecteddir != null)
	 		    {    		
		    		boolean bSetValuesAllways = true;
		    		File fCopyToDir = new File(selecteddir);
		    		boolean underStartedExecution = false;
		    		setMissingTextFieldValues(fCopyToDir, this.lipsync2Smil.getoutputpathname(), this.lipsync2Smil.getinputpathname(), bSetValuesAllways, underStartedExecution);
	 		    }
	    	 }
    	} catch(Exception e2){
    		try {
    		JOptionPane.showMessageDialog (this, 
	 					e2.getMessage(), strNewConfigCopyTitle, JOptionPane.OK_OPTION);
	 			 }catch(Exception e3){	     	 				 
	 			 }
    		return ;
    	} 		    		

    }
    
    private void newProjectPressed(ActionEvent e)
    {
    	String strNewConfigCopyTitle = lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_newbookdirectory);
    	String strSelectCopyIntoConfigCopyTitle = lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_selectdirwherenewdircreatead);
    	String strCopyIntoConfigCopyTitle = lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_NewProject) +" " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_created);
    	boolean bCreateNewSubDir = true;
    	copyConfigFiles(strNewConfigCopyTitle, strSelectCopyIntoConfigCopyTitle, strCopyIntoConfigCopyTitle, bCreateNewSubDir);
    }
  
    private void copyConfigFiles(String strNewConfigCopyTitle, String strSelectCopyIntoConfigCopyTitle, String strCopyIntoConfigCopyTitle, boolean bCreateNewSubDir)
    {
      	// fffff
    	/*
   	 Object[] options = { lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_yes),
			 lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_no) };
	*/
       Object[] options = { lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_fromconfigdir), lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_fromanotherconfigdir) };
       int n = JOptionPane.showOptionDialog(this, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_selectoptiondirtocopy),
	     strNewConfigCopyTitle,
	     JOptionPane.DEFAULT_OPTION,
	     JOptionPane.QUESTION_MESSAGE,
	     null,
	     options,
	     options[0]);
       
	 	 String strCopyFrom = System.getProperty("user.dir"); // first value is installdir
	 	 File fCopyFrom = new File(strCopyFrom);
	 	if (n == 1)
	 	{
	 		strCopyFrom = "no-install";
	 		// fc.setSelectedFile(f);
	 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	 		fc.setDialogTitle(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_selectdirtocopy));
	 		fc.setApproveButtonText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
	 		int returnVal = fc.showOpenDialog(this);
	 		 if (returnVal == JFileChooser.APPROVE_OPTION) 
	    	 { 			  			
	 			String selecteddir = fc.getSelectedFile().getAbsolutePath();
	 		    if (selecteddir != null)
	 		    {
	 		    	strCopyFrom = selecteddir; 
	 		    	fCopyFrom = new File(strCopyFrom);
	 		    }
	    	 }
	 	}
	 	
	 	if (!fCopyFrom.exists())
	 	{ // no file
	 		 try {
  	 			JOptionPane.showMessageDialog (this, 
  	 					lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +fCopyFrom.getCanonicalPath(), strNewConfigCopyTitle, JOptionPane.OK_OPTION);
  	 			 }catch(Exception e1){
  	 				 
  	 			 }
         	 return ;
	 	}
	 	
	 	File fCopyFromConfig = new File(fCopyFrom.getAbsolutePath() +File.separator +"config");
	 	if (!fCopyFromConfig.exists())
	 	{ // no config dir
	 		 try {
  	 			JOptionPane.showMessageDialog (this, 
  	 					lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_configdir_does_not_exist) +": " +fCopyFromConfig.getCanonicalPath(), strNewConfigCopyTitle, JOptionPane.OK_OPTION);
  	 			 }catch(Exception e1){
  	 				 
  	 			 }
         	 return ;
	 	}
	 	
	 	File fCopyToDir = null;
 		// fc.setSelectedFile(f);
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		fc.setDialogTitle(strSelectCopyIntoConfigCopyTitle);
 		fc.setApproveButtonText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
 		int returnVal = fc.showOpenDialog(this);
 		if (returnVal == -1) // cancel is pressed
 		{
 			return ;
 		}
 		 		
 		 if (returnVal == JFileChooser.APPROVE_OPTION) 
    	 { 			  			
 			String selecteddir = fc.getSelectedFile().getAbsolutePath();
 			File fselecteddir = new File(selecteddir);
 			if (!fselecteddir.exists())
 			{ // did not exists
 				 try {
      	 			JOptionPane.showMessageDialog (this, 
      	 					lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +fselecteddir.getCanonicalPath(), strNewConfigCopyTitle, JOptionPane.OK_OPTION);
      	 			 }catch(Exception e1){
      	 				 
      	 			 }
             	 return ;
 			}
 			
 			fCopyToDir = new File(selecteddir);
 			
 			if (bCreateNewSubDir)
 			{
	 		    Object[] message = { lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_dirnametobecreated), "(" + lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_underdir) +" " +selecteddir +")"};//send text of filename
	 		    String dirname = JOptionPane.showInputDialog(this, message, strNewConfigCopyTitle, JOptionPane.OK_CANCEL_OPTION);
	 		    if (dirname == null) // cancel is pressed
	 		    {
	 		    	return ;
	 		    }
	 		    
	 		    if (dirname != null)
	 		    {
	 		    	System.out.println(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_newdir) +": " +dirname);
	 		    	fCopyToDir = new File(selecteddir +File.separator +dirname);
	 		    	if (fCopyToDir.exists())
	 		    	{ // did not exists
	 		    		 try {
	 	     	 			JOptionPane.showMessageDialog (this, 
	 	     	 					lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_diirallreadyexists) +": " +fCopyToDir.getCanonicalPath(), strNewConfigCopyTitle, JOptionPane.OK_OPTION);
	 	     	 			 }catch(Exception e1){
	 	     	 				 
	 	     	 			 }
	 	            	 return ;
	 		    	}
	 		    	if (!fCopyToDir.mkdir())
	 		    	{ // cannot create a dir
	 		    		 try {
	 	     	 			JOptionPane.showMessageDialog (this, 
	 	     	 					lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cannotcreatedir) +": " +fCopyToDir.getCanonicalPath(), strNewConfigCopyTitle, JOptionPane.OK_OPTION);
	 	     	 			 }catch(Exception e1){
	 	     	 				 
	 	     	 			 }
	 	            	 return ;
	 		    	} 		    	 	
	 		    }
 			}
 			else
 			{
 				if (!fCopyToDir.exists())
 		    	{ // did not exists
 		    		 try {
 	     	 			JOptionPane.showMessageDialog (this, 
 	     	 					lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +fCopyToDir.getCanonicalPath(), strNewConfigCopyTitle, JOptionPane.OK_OPTION);
 	     	 			 }catch(Exception e1){
 	     	 				 
 	     	 			 }
 	            	 return ;
 		    	}
 			}
 		    
	    	try {
		    		lipsync2Smil.copyConfigFilesIntoBookdirs(fCopyFrom, fCopyToDir);
		    		List<String> listSubDirs = lipsync2Smil.getBookSubDirectories();
		    		lipsync2Smil.createNewBookdirs(fCopyToDir, listSubDirs);
		    		boolean bSetValuesAllways = true;
		    		boolean underStartedExecution = false;
		    		setMissingTextFieldValues(fCopyToDir, this.lipsync2Smil.getoutputpathname(), this.lipsync2Smil.getinputpathname(), bSetValuesAllways, underStartedExecution);
		    		JOptionPane.showMessageDialog (this, 
		    				strCopyIntoConfigCopyTitle, strNewConfigCopyTitle, JOptionPane.OK_OPTION);     	 
		    	} catch(Exception e2){
		    		e2.printStackTrace();
		    		try {
		    		JOptionPane.showMessageDialog (this, 
     	 					e2.getMessage(), strNewConfigCopyTitle, JOptionPane.OK_OPTION);
     	 			 }catch(Exception e3){	     	 				 
     	 			 }
		    		return ;
		    	} 		    		
    	 }
    }
    
    public Properties getUserFileProperty(int ind)
    {
    	return this.lipsync2Smil.getUserFileProperty(ind);
    }
    
    private void executeButtonPressed(ActionEvent e)
    {
    	String fieldvalue = textCfg.getText();
    	if ( fieldvalue == null || fieldvalue.trim().length() == 0 )
    	{
    		int selind = languageCombo.getSelectedIndex();
    		if (selind == 1)
    		{
    			textCfg.setText("lipsyn2smil2.cfg");
    		}
    		else
    		{
    			textCfg.setText("lipsyn2smil3.cfg");
    		}
    	}
    	if (checkTextFieldValues()==false)
    		return;
    	/*
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        */
            	textArea.setText(null);
            	textArea.setCaretPosition(textArea.getDocument().getLength());
            	splitPane.updateUI();
        /*    }
        });
		*/

    	try {
    		startReadThreads();
    		Thread.sleep(1000);
    	} catch(Exception e2){
    		System.out.println(e2.getMessage());
			Lipsync2Daisy.severe(e2);
    		setEditIntoTrue();
    	}

		int iSentenceComboSelected = sentenceCombo.getSelectedIndex();
    	int iExecuteTypeListSelected = executeTypeList.getSelectedIndex();
 
    	strSmilTemplateDir = lipsync2Smil.getStrSmilTemplateDir();
    	if (bExecutionButtonPressed) 
    	{    		
    		boolean bBRemoveNeigthBornSentencesInPElement = false;
    		boolean bBRemoveNeigthBornSentencesInAllElement = false;
       	
    		ResourceBundle messages = lipsync2Smil.getMessages(); 
    		int executetype_tmp = lipsync2Smil.getExecutetype();
    		Properties prop3 = lipsync2Smil.getProp3();
    		Properties uiprop = lipsync2Smil.getUiProp();
    		String uiLanguage = lipsync2Smil.getUILanguage();
    		String currDir = lipsync2Smil.getM_currentDir();
    		if (lipsync2Smil != null)
    			bBRemoveNeigthBornSentencesInPElement = lipsync2Smil.getBRemoveNeigthBornSentencesInPElements();
    		if (lipsync2Smil != null)
    			bBRemoveNeigthBornSentencesInAllElement = lipsync2Smil.getBRemoveNeigthBornSentencesInAllElements();
    		lipsync2Smil = new Lipsync2Daisy();
    		if (Lipsync2Daisy.static_messages != null)
    			lipsync2Smil.setMessages(Lipsync2Daisy.static_messages);
    		lipsyncWindowAdapter.setLipsync2Smil(lipsync2Smil); 
    		lipsync2Smil.setConsole(this);
    		lipsync2Smil.setBRemoveNeigthBornSentencesInPElements(bBRemoveNeigthBornSentencesInPElement);
    		lipsync2Smil.setBRemoveNeigthBornSentencesInAllElements(bBRemoveNeigthBornSentencesInAllElement);
    		
    		lipsync2Smil.setUILanguage(uiLanguage);
    		lipsync2Smil.setMessages(Lipsync2Daisy.static_messages);
    		lipsync2Smil.setProp3(prop3);
    		lipsync2Smil.setUiProp(uiprop);
    		lipsync2Smil.setbGui(true);
    		if (iExecuteTypeListSelected == -1)
    			lipsync2Smil.setExecutetype("2");
    		else
    		{
    			String exetype = (String)executeTypeList.getSelectedItem();
    			lipsync2Smil.setExecutetype("" +(iExecuteTypeListSelected +2));
    		}
    		if (currDir != null)
    			lipsync2Smil.setM_currentDir(currDir);    		
    		lipsync2Smil.loadi18Resourcies(currDir);
    		lipsync2Smil.loadi18Properties(Lipsync2Daisy.getFProp(), Lipsync2Daisy.getLocale());
    	}    	
    	if (textCfgPath.getText().trim().length() > 0)
    		strSmilTemplateDir = textCfgPath.getText();
    	// lipsync2Smil.setConsole(this); // bugi?: kun convertointi tyyppiä vaihdetaan
    	// toisesta toiseen => muuttuja lipsync2Smil.console == null !!!
    	// ilmeisesti jossain tilanteessa syntyy uusi instanssi ohjelmasta!
    	
    	try {
    		setEditIntoFalse();
    		if (languageCombo.getSelectedIndex() > -1 && strCurrentLanguage == null)
    		{
    			strCurrentLanguage = (String)languageCombo.getSelectedItem();
    		}
        	if (sentenceCombo.getSelectedIndex() > -1) 
        		lipsync2Smil.removeNeigthBornSentencesInPElements(sentenceCombo.getSelectedIndex());   		
        	
        	// executeTypeList.setSelectedIndex(iExecuteTypeListSelected);
        	justsavedprop = null;
        	Thread convertThread = new Thread() {
      	      public void run() {
      	    	justsavedprop = lipsync2Smil.convertFromGui(textCfg.getText(), textInputPath.getText(), textOutputPath.getText(), strSmilTemplateDir, "" +(executeTypeList.getSelectedIndex() +2), lenthMP3Button.isSelected(), correctTimeText.getText(), getXmlValidation(), (String)languageCombo.getSelectedItem());
    	    	bExecutionButtonPressed = true;
    	    	if (justsavedprop != null )
            	{
                	jpanelEarlierSetups.addNewButtonText(textInputPath.getText(), justsavedprop);
            	}
    	    	setEditIntoTrue();
      	      }
      	    };
      	    convertThread.start();        	

    	} catch(Exception e3){
    		System.out.println(e3.getMessage());
			Lipsync2Daisy.severe(e3);
    		setEditIntoTrue();
    	}
    	finally {
    		// setEditIntoTrue();
    	}
    }
    
    public Properties [] getGuiConversionPathSettingsArray()
    {
    	return this.jpanelEarlierSetups.getGuiConversionPathSettingsArray();
    }
    
    private boolean checkCfgPath()
    {
    	String value = this.textCfgPath.getText();
    	boolean bMakeDir = false;
    	boolean bCheckIsDir = true;
    	boolean ret = checkPath(value, bMakeDir, bCheckIsDir, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfgfilesofpath));
    	if (ret)
    	{
    		this.lipsync2Smil.setStrSmilTemplateDir(value);
    	}
    	return ret;
    }
    
    private boolean checkCfgFile()
    {
    	String value = this.textCfg.getText();
    	if (value!= null && !value.contains(File.separator))
    	{
    		value = lipsync2Smil.getConsoleAbsolutePathOfCfgFile(value);
    	}
    		// value = this.textCfgPath.getText() +File.separator + this.textCfg.getText();
    	boolean bMakeDir = false;
    	boolean bCheckIsDir = false;
    	return checkPath(value, bMakeDir, bCheckIsDir, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfgfile));	
    }
    
    private boolean checkInputPath()
    {
    	String value = this.textInputPath.getText();
    	boolean bMakeDir = false;
    	boolean bCheckIsDir = true;
    	return checkPath(value, bMakeDir, bCheckIsDir, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_lipsyncfilepath));	
    }
    
    private boolean checkTextFieldValues()
    {
    	if (!checkCfgPath())
    		return false;    	
    	if (!checkInputPath())
    		return false;
    	if (!checkCfgFile())
    		return false;    	
    	if (!checkOutputPath())
    		return false;
    	return true;
    }
    
    private boolean checkOutputPath()
    {    	
    	String value = this.textOutputPath.getText();
    	boolean bMakeDir = true;
    	boolean bCheckIsDir = true;
    	return checkPath(value, bMakeDir, bCheckIsDir, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_outputpath));	
    }
    
    private boolean checkPath(String value, boolean bMakeDir, boolean bCheckIsDir, String strHeader)
    {
    	File f = null;
 		if (value.trim().length() > 0)
	 	{
	 		f = new File(value);
	 		if (!f.exists())
	 		{
	 			if (bMakeDir)
	 			{
	 	   	 		int reply = JOptionPane.showConfirmDialog(
	 	    	 	            this, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_dirnotexists) +": " +f.getAbsolutePath() +" " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_shoulddircreated) +"?",
	 	    	 	            strHeader,
	 	    	 	            JOptionPane.YES_NO_OPTION);
	 	    	 	if (reply == JOptionPane.YES_OPTION)
	 	    	 	 {
	 	    	 		if (!f.mkdir())
	 	    	 		{
	 	    	 			try {
	 		 				JOptionPane.showMessageDialog (this, 
	 		 						lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cannotcreatedir)+ ": " +f.getAbsolutePath(), strHeader, JOptionPane.OK_OPTION);	   	 				
	 			 			}catch(Exception e1){   	 					
	 			 			}
	 			 			return false;
	 	    	 		}
	 	    	 	 }
	 	    	 	else
	 	    	 		return false;
	 			}
	 			else
	 			if (bCheckIsDir)
	 			{
		 			try {
		 				JOptionPane.showMessageDialog (this, 
		 						lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_dirdontexists) + ": " +f.getAbsolutePath(), strHeader, JOptionPane.OK_OPTION);	   	 				
		 			}catch(Exception e1){   	 					
		 			}
		 			return false;
	 			}
	 			else
	 			{
		 			try {
		 				JOptionPane.showMessageDialog (this, 
		 						lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +f.getCanonicalPath(), strHeader, JOptionPane.OK_OPTION);	   	 				
		 			}catch(Exception e1){   	 					
		 			}
		 			return false;
		 		}
	 		}
	 		else	 		
	 		if (bCheckIsDir && f.isFile())
	 		{
	 			try {
	 				JOptionPane.showMessageDialog (this, 
	 						lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_fileisdir) +": " +f.getCanonicalPath(), strHeader, JOptionPane.OK_OPTION);	   	 				
	 			}catch(Exception e1){   	 					
	 			}
	 			return false;
	 		}
	 		else
	 		if (!bCheckIsDir && f.isDirectory())
		 	{
		 		try {
		 			JOptionPane.showMessageDialog (this, 
		 					lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_dirisfile) +": " +f.getCanonicalPath(), strHeader, JOptionPane.OK_OPTION);	   	 				
	 			}catch(Exception e1){   	 					
	 			}
	 			return false;
	 		}
	 	}
	 	return true;
	 }
	 		
    private void cfgButtonPressed(ActionEvent e)
    {    	
    	File f = null;
    	String value = this.textCfg.getText();
   	 	if (value.trim().length() > 0)
   	 	{
   	 		if (value.contains(File.separator))
   	 		{
   	 			f = new File(value.trim());
   	 			fc.setSelectedFile(f);
   	 		}
   	 		else   	 	
   	 		{
   	 			f = new File(textCfgPath.getText().trim() +File.separator +value.trim());
   	 			/*
   	 			if (!f.exists())
   	 			{
   	 				try {
   	 				JOptionPane.showMessageDialog (this, 
   	 						lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_file_does_not_exist) +": " +f.getCanonicalPath(), lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_warning), JOptionPane.OK_OPTION);	   	 				
   	 				}catch(Exception e1){   	 					
   	 				}
   	 				return ;
   	 			}
   	 			*/
   	 			if (f.exists())
   	 				fc.setSelectedFile(f);
   	 			else
   	 			{   	 				   	 			
   	 				String subdir = Lipsync2Daisy.cnstExecuteDaisy3CfgSubDir;
   	 				if (lipsync2Smil.executetype == Lipsync2Daisy.cnstExecuteDaisy2)
   	 					subdir = Lipsync2Daisy.cnstExecuteDaisy2CfgSubDir;
   	 				f = new File(textCfgPath.getText().trim() +File.separator + subdir +File.separator +value.trim());
   	 				if (!f.exists())
   	 				{
   	 					/*
	   	 				try {
	   	 				JOptionPane.showMessageDialog (this, 
	   	 						lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_file_does_not_exist) +": " +f.getCanonicalPath(), lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_warning), JOptionPane.OK_OPTION);	   	 				
	   	 				}catch(Exception e1){
	   	 					
	   	 				}
	   	 				*/
   	 				}
   	 				else
   	 					fc.setSelectedFile(f);
   	 			}
   	 		}
   	 	}
   	 	
   	 	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
   	    fc.setDialogTitle(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
   	    fc.setApproveButtonText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
    	int returnVal = fc.showOpenDialog(this);

    	 if (returnVal == JFileChooser.APPROVE_OPTION) 
    	 {
             File file = fc.getSelectedFile();
             if (file == null)
            	 return ;
             
             if (!file.exists())
             {
            	 try {
     	 			JOptionPane.showMessageDialog (this, 
     	 					lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +file.getCanonicalPath(), lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfgfile), JOptionPane.OK_OPTION);
     	 			 }catch(Exception e1){
     	 				 
     	 			 }
            	 return ;
             }
             if (!file.getAbsolutePath().equals(f.getAbsolutePath()))
             {
                 File dir  = file.getParentFile();
                 if (file != null && file.exists())
    	 		 {
                	 if (!f.getParentFile().getAbsolutePath().equals(dir.getAbsolutePath()))
                	 {                		  
        	 			  String subdir = Lipsync2Daisy.cnstExecuteDaisy3CfgSubDir;
           	 			  if (lipsync2Smil.executetype == Lipsync2Daisy.cnstExecuteDaisy2)
           	 				 subdir = Lipsync2Daisy.cnstExecuteDaisy2CfgSubDir;
           	 			  if (f.getParentFile().getAbsolutePath().equals(dir.getAbsolutePath()))
           	 			  {
           	 				  textCfgPath.setText(dir.getParentFile().getAbsolutePath());
           	 				  textCfg.setText(file.getName());
           	 			  }
           	 			  else
           	 			  {
           	 				if (f.getParent().equals(dir))
           	 					textCfg.setText(file.getName());
           	 				else
           	 				{
           	 					String fparentpath = file.getParent();
           	 					String strtextCfgPath = this.textCfgPath.getText();
           	 					String dirparentpath = dir.getParent();
           	 					if (fparentpath.endsWith(subdir) && strtextCfgPath.equals(dirparentpath) )
           	 						textCfg.setText(file.getName());
           	 					else
           	 					if (strtextCfgPath.equals(dirparentpath))
           	 						textCfg.setText(file.getName());
           	 					else
           	 						textCfg.setText(file.getAbsolutePath());
           	 				}
           	 			  }
                	 }
                	 else
                		 textCfg.setText(file.getName());
    	 		 }
    	 		 else
    	 		 {
    	 			 try {
    	 			JOptionPane.showMessageDialog (this, 
    	 					lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +f.getCanonicalPath(), lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfgfile), JOptionPane.OK_OPTION);
    	 			 }catch(Exception e1){
    	 				 
    	 			 }
    	 		 } 		  	                         	 
             }
         } else {
             
         }      
    }
    
    private void editCfgButton(ActionEvent e)
    {
    	if (!checkCfgFile())
    		return ;
    	
    	String value = this.textCfg.getText();
    	String value2 = null;
   	 	if (value.trim().length() > 0)
   	 	{
   	 		File f2 = new File(value);
   	 		File f = null; 
   	 		if (!f2.exists() ||  !f2.isFile())
   	 		{
   	 			value2 = this.lipsync2Smil.getConsoleAbsolutePathOfCfgFile(value);
   	 			f = new File(value2);
   	 		}
   	 		else
   	 			f = f2;
   	 			
	   	 	try {
		   	   	 	if (!f.exists())
		   	   	 	{
		   	   	 		JOptionPane.showMessageDialog(this, 
		   	   	 			lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +f.getCanonicalPath(), lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfgfile), JOptionPane.WARNING_MESSAGE);
		   	   	 		return ;
		   	   	 	}
		  	 			
		   	 		if (consoleEditCfg.isShowing())
		   	 		{
		   	 				String feditcfgfname = consoleEditCfg.getCfgFileFame();
		   	 				if (feditcfgfname != null)
		   	 				{
		   	 					if (feditcfgfname.equals(f.getAbsolutePath()))
		   	 					{
		   	 						consoleEditCfg.setVisible(true);
		   	 						return ;
		   	 					}
		   	 				}
		   	 				consoleEditCfg.setVisible(false);
							consoleEditCfg.setCfgFileName(f.getAbsolutePath());
							consoleEditCfg.setfInputPath(this.textInputPath.getText());
		   	 		}
		   	 		else
		   	 		{
		   	 			consoleEditCfg = new ConsoleEditCfg(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_save_into_cfg_file), this.lipsync2Smil);
		   	 			String path = f.getParent();
		   	 			consoleEditCfg.setfInputPath(this.textInputPath.getText());
			   	 		consoleEditCfg.setCfgFileName(f.getAbsolutePath());
			   	 		// consoleEditCfg.setInputPath(this.textInputPath.getText());
		   	 		}
			   	 	consoleEditCfg.setVisible(true);
	   	 	}catch(Exception e1){
		 		System.out.println("Error: " +e1.getMessage());
		 			e1.getStackTrace();   	 			
		 	}
   	 	}
    }
    
    private static void copyFileUsingChannel(File source, File dest)
    throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
           }finally{
               sourceChannel.close();
               destChannel.close();
           }
    }
    
    private void copyCfgButton (ActionEvent e)
    {
    	if (!checkCfgFile())
    		return;
    	
    	String value = this.textCfg.getText();
    	File oldCfgFile;
   	 	if (value != null && value.trim().length() > 0)
   	 	{
   	 		if (!value.contains(File.separator))
   	 			value = this.lipsync2Smil.getConsoleAbsolutePathOfCfgFile(value);
   	 		oldCfgFile = new File(value.trim());
 			if (!oldCfgFile.exists())
 			{
 				try {
 					JOptionPane.showMessageDialog (this, 
 							lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +oldCfgFile.getCanonicalPath(), lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfgfile), JOptionPane.OK_OPTION);	   	 				
 				}catch(Exception e1){   	 					
 				}
 				return ;
 			}

   	 		fc.setSelectedFile(oldCfgFile);
   	 		
   	 		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
   	 		fc.setDialogTitle(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_copy));
   	 		fc.setApproveButtonText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_copy));
   	 		int returnVal = fc.showOpenDialog(this);

   	 		if (returnVal == JFileChooser.APPROVE_OPTION) {
   	 			File fileNew = fc.getSelectedFile();
   	 			if (fileNew == null)
   	 				return ;
   	 			if (oldCfgFile.getAbsolutePath().equals(fileNew.getAbsolutePath()))
   	 			{
   	 				try {
   	 					JOptionPane.showMessageDialog (this, 
   	 						lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_filesareequal) + ": " +oldCfgFile.getCanonicalPath(), lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfgfile), JOptionPane.OK_OPTION);	   	 				
   	 				}catch(Exception e1){   	 					
   	 				}
   	 				return ;   	 				
   	 			}
   	 			
   	 		  String msgDialog = lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_willyoucopyfile);
   	 		  if (fileNew.exists())
   	 			msgDialog = lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_fileexists_willyoucopyfile);
   	 		  
   	 		  int reply = JOptionPane.showConfirmDialog(
   	 	            this,
   	 	            msgDialog + ": " +oldCfgFile.getName() +"\n"
   	 	            +fileNew.getAbsolutePath() +" " + lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_intofile) +"?",
   	 	            lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_copyfile),
   	 	            JOptionPane.YES_NO_OPTION);
   	 		  if (reply == JOptionPane.YES_OPTION)
   	 		  {
   	 			  try {
   	 				  copyFileUsingChannel(oldCfgFile, fileNew);
   	 			  } catch(Exception e1){
   	 				  System.err.println (lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_errorinfilecopy));
   	 				  System.err.println (e1.getMessage()); 
   	 				  return ;
   	 			  }
   	 			  this.textCfg.setText(fileNew.getAbsolutePath());
   	 		  }
   	 		}
   	 	}
    }
    
    private void savePropButtonPressed(ActionEvent e)
    {
  	 	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);    
  	 	fc.setDialogTitle(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_save));
  	 	fc.setApproveButtonText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_save)); 
  	 	fc.setSelectedFile(null);
    	int returnVal = fc.showOpenDialog(this);

    	if (returnVal == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             boolean bSaveFile = true;
             if (file.exists())
             {
            	//Custom button text
            	 Object[] options = { lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_yes),
            			 lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_no) };
            	 int n = JOptionPane.showOptionDialog(this,
            	     lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_file) +" " +file.getAbsolutePath() +" " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_fileexists_overwrite) +"?",          	     
            	     lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_shouldoverwrite),
            	     JOptionPane.YES_NO_CANCEL_OPTION,
            	     JOptionPane.QUESTION_MESSAGE,
            	     null,
            	     options,
            	     options[1]);
            	 if (n == 1)
            		 bSaveFile = false;
             }
             
             if (bSaveFile)
            	 lipsync2Smil.saveUserPropertiesFromGui(getCfg(), getInputPath(), 
            		 getOutputPath(), getCfgPath(), getExecuteType(), 
            		 getCorrectTime(), getCalculateLengthOfMP3Files(), file);
        } else {
             
        }
    }
    
    private void propButtonPressed(ActionEvent e)
    {
     	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
     	fc.setDialogTitle(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
     	fc.setApproveButtonText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
     	fc.setSelectedFile(null);
    	int returnVal = fc.showOpenDialog(this);

    	if (returnVal == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             //This is where a real application would open the file.
             lipsync2Smil.readUserProperties(new File(file.getAbsolutePath()));
        } else {
             
        }
   }
    
    private void inputPathBuffonPressed(ActionEvent e)
    {
    	String value = this.textInputPath.getText();
   	 	if (value.trim().length() > 0)
   	 		fc.setSelectedFile(new File(value.trim()));
   	 	fc.setDialogTitle(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
   	   	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
   	   	fc.setApproveButtonText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
   	   	int returnVal = fc.showOpenDialog(this);

     if (returnVal == JFileChooser.APPROVE_OPTION) {
         File file = fc.getSelectedFile();
         //This is where a real application would open the file.
         this.textInputPath.setText(file.getAbsolutePath());
     } else {
         
     }
  }

    private void cfgPathBuffonPressed(ActionEvent e)
    {
    	String value = this.textCfgPath.getText();
   	 	if (value.trim().length() > 0)
   	 		fc.setSelectedFile(new File(value.trim()));
   	 	fc.setDialogTitle(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
   	   	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
   	   	fc.setApproveButtonText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
   	   	int returnVal = fc.showOpenDialog(this);

     if (returnVal == JFileChooser.APPROVE_OPTION) {
         File file = fc.getSelectedFile();
         //This is where a real application would open the file.
         this.textCfgPath.setText(file.getAbsolutePath());
     } else {
         
     }
  }

    private void outputPathBuffonPressed(ActionEvent e)
    {
    	String value = this.textOutputPath.getText();
   	 	if (value.trim().length() > 0)
   	 		fc.setSelectedFile(new File(value.trim()));
   	 	fc.setDialogTitle(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
     	 fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
     	 fc.setApproveButtonText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
      	 int returnVal = fc.showOpenDialog(this);

         if (returnVal == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             //This is where a real application would open the file.
             this.textOutputPath.setText(file.getAbsolutePath());
         } else {
             
         }
   }
  
    public void startReadThreads()
    throws IOException
    {
        // Set up System.out
        piOut = new PipedInputStream();
        poOut = new PipedOutputStream(piOut);
        System.setOut(new PrintStream(poOut, true));

        // Set up System.err
        piErr = new PipedInputStream();
        poErr = new PipedOutputStream(piErr);
        System.setErr(new PrintStream(poErr, true));

        // Create reader threads
        if (outThread != null)
        	outThread = null;
        outThread = new ReaderThread(piOut); // .start();
        outThread.start();
        if (errThread != null)
        	errThread = null;
        errThread = new ReaderThread(piErr); // .start();
        errThread.start();
        setVisible(true);
    }
    
    public void setEditIntoTrue()
    {
    	en_endableOrDisableControls(true);
    }
    
    public void setEditIntoFalse()
    {
    	en_endableOrDisableControls(false);
    }
    
    private void en_endableOrDisableControls(boolean value)
    {
        //  if (!bLipsync2smilButtons)
        //  {
    		jtbConversionTab.setEnabled(value);
    		sentenceCombo.setEnabled(value);
    		languageCombo.setEnabled(value);
    		executeTypeList.setEnabled(value);
    		textCfgPath.setEnabled(value);
    		newProjectButton.setEnabled(value);
    		copytextfieldvalueFromConfigButton.setEnabled(value);
    		copyconfigfilesButton.setEnabled(value);
    		cfgDirButton.setEnabled(value);
          	cfgButton.setEnabled(value);
          	editCfgButton.setEnabled(value);
          	copyCfgButton.setEnabled(value);
          	propButton.setEnabled(value);
          	savePropButton.setEnabled(value);
          	textCfg.setEnabled(value);
          	inputPathBuffon.setEnabled(value);
          	textInputPath.setEnabled(value);
          	outputPathBuffon.setEnabled(value);
          	textOutputPath.setEnabled(value);
          	executeButton.setEnabled(value); 
          	correctTimeText.setEnabled(value);
          	lenthMP3Button.setEnabled(value);
          	checkBoxXmlValidation.setEnabled(value);
          	helpButton.setEnabled(value);
         // }

    }
    
    private JPanel addFieldPanel(JButton jbutton, JTextField jtextField)
    throws NullPointerException
    {
    	if (jbutton == null)
    		throw new NullPointerException("jbutton is null!");
    	if (jtextField == null)
    		throw new NullPointerException("jtextField is null!");
    		
        JPanel jpanel = new JPanel();
        jpanel.add (jbutton, BorderLayout.NORTH);
        jpanel.add (jtextField, BorderLayout.SOUTH);
    	return jpanel;
    }
    	
    class ReaderThread extends Thread {
        PipedInputStream pi;

        ReaderThread(PipedInputStream pi) {
            this.pi = pi;
        }

        public void run() {
            final byte[] buf = new byte[1024*200];
            try {
                while (true) {
                    final int len = pi.read(buf);
                    if (len == 0)
                    	continue;
                    if (len == -1) {
                        break;
                    }
                    textArea.append(new String(buf, 0, len));
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    /*
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            textArea.append(new String(buf, 0, len));

                            // Make sure the last line is always visible
                            textArea.setCaretPosition(textArea.getDocument().getLength());

                            ?*
                            // Keep the text area down to a certain character size
                            int idealSize = 1000;
                            int maxExcess = 500;
                            int excess = textArea.getDocument().getLength() - idealSize;
                            if (excess >= maxExcess) {
                                textArea.replaceRange("", 0, excess);
                            }
                            *?
                        }
                    });
                    */
                }
            } catch (IOException e) {
            	// e.printStackTrace();
            }
        }
    }
    
    public void actionPerformed(ActionEvent e) {
    	Object obj = e.getSource();
    	JComboBox cb = null;
    	String executeMode = null;
    	if (obj instanceof JComboBox)
    	{
    		cb = (JComboBox)obj;
        	executeMode = (String)cb.getSelectedItem();
    	}
        if (obj == helpButton)
        {
        	help.showWindowsWithHelpText();
        }
        else
        if (cb != null && cb == sentenceCombo)
        	sentenceComboChanged(executeMode);
        else
        if (cb != null && cb == languageCombo)
        	languageComboChanged(languageCombo.getSelectedIndex());
        else
        {        	
        	executeTypeListChanged("" +(executeTypeList.getSelectedIndex() +2));
        }        	
    }
    
    private void languageComboChanged(int selvalue)
    {    	
   		lipsync2Smil.setUILanguage(selvalue);
    	try {
    		languageUIChanged();
    	} catch(Exception e){
    		System.out.println("Error: " +e.getMessage());
    		e.printStackTrace();	
    	}
    }
    
    public void setUILanguage(String value)
    {
    	this.strCurrentLanguage = value;
    	if (strCurrentLanguage != null && strCurrentLanguage.equals("Finland"))
    		languageCombo.setSelectedIndex(0);
    	else
       	if (strCurrentLanguage != null && strCurrentLanguage.equals("England"))
       		languageCombo.setSelectedIndex(1);
    	else
       	if (strCurrentLanguage != null && strCurrentLanguage.equals("Sweden"))
       		languageCombo.setSelectedIndex(2);

    }
    
    private void sentenceComboChanged(String selvalue)
    {    	
    	if (sentenceCombo.getSelectedIndex() > -1) 
    		lipsync2Smil.removeNeigthBornSentencesInPElements(sentenceCombo.getSelectedIndex());   		
    }
    
    public String setNewConfigsOfConsole(int index, Properties selectedprop)
    {    	
    	if (selectedprop == null)
    		return "";
    	String ret = "";
    	
  	  	String propVariablePostExt = "";
		String strexecutetype = selectedprop.getProperty("executetype");
		if (strexecutetype != null && strexecutetype.contains("3"))
		  	propVariablePostExt = "3";
				  
		String strfTimeshiftintovoicedatas = selectedprop.getProperty("fTimeshiftintovoicedatas");
		String strcalculatemp3filelength = selectedprop.getProperty("calculatemp3filelength");
		boolean bstrcalculatemp3filelength = Boolean.parseBoolean(strcalculatemp3filelength);
		lenthMP3Button.setSelected(bstrcalculatemp3filelength);
		String uilanguage = selectedprop.getProperty("uilanguage");			  
			  
	  	String strlocale_prop_field_cfgfile = selectedprop.getProperty(Lipsync2Daisy.cnst_locale_prop_field_cfgfile +propVariablePostExt);
			  // settprop.setProperty(cnst_locale_prop_field_templatedir, this.strParTemplate)
	  	String strSmilTemplateDir = null;
		// this.ch xxxxyyy
		if (this.strCurrentLanguage == null)
			this.strCurrentLanguage = (String)this.languageCombo.getSelectedItem();
		
		strSmilTemplateDir = selectedprop.getProperty(Lipsync2Daisy.cnst_locale_prop_field_templatedir +propVariablePostExt);
		// strSmilTemplateDir
		
		// correctTimeText ??
		 String strfLipsyncDataDir = selectedprop.getProperty("readdir" +propVariablePostExt);
		 textInputPath.setText(strfLipsyncDataDir);
		 String strcfgpath = selectedprop.getProperty("cfgpath" +propVariablePostExt);
		 textCfgPath.setText(strcfgpath);
		 String stroutputdir = selectedprop.getProperty("outputdir" +propVariablePostExt);
		 textOutputPath.setText(stroutputdir);
		 String strcfgfile = selectedprop.getProperty("cfgfile" +propVariablePostExt);	
		 textCfg.setText(strcfgfile);
		 int index2 = (strexecutetype.equals("2") ? 0 : 1);
		 int langIndex = languageCombo.getSelectedIndex();
		 languageCombo.setSelectedItem(uilanguage);;
		 executeTypeList.setSelectedIndex(index2);
				  
		 this.update(this.getGraphics());
		 
		  return ret;
    }
}