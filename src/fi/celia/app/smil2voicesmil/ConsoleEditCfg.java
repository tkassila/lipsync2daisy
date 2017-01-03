package fi.celia.app.smil2voicesmil;

import java.awt.*;
//import java.awt.List;

//import javax.swing.text.BadLocationException;
//import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;

import javax.swing.text.Highlighter;
import javax.swing.text.DefaultHighlighter;

import java.nio.channels.FileChannel;
//import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.awt.event.*;
//import java.util.*;

// import javax.swing.event.*;
//import javax.swing.text.*;

/**
 * This Console class is showing and editing a cfg file of Lipsync2Daisy app. It is a JFrame.
 * <p>
 * @author Tuomas Kassila
 *
 */
public class ConsoleEditCfg extends JFrame implements ActionListener,
DocumentListener {
	static final long serialVersionUID = 324343232423434235L;
	
	private boolean bTextAreaChanged = false;
	// private String strInputPath;
	private String cfgFilename;
	private File fileCgf;
	private FileNameExtensionFilter xmlfilter;
    private FileChooser fc = null; 
    private File fCfgDir;
    private File fInputPath;
    private SearchNextCfgVariableItem item;
    private DefaultHighlighter.DefaultHighlightPainter cyanPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.cyan);
    
    private class SearchNextCfgVariableItem
    {
    	public SearchNextCfgVariableItem(final String p_variablename, final String p_variableDescription)
    	{
    		variablename = p_variablename; 
    		variableDescription = p_variableDescription; 
    	}
    	
    	private final String variablename;
    	private final String variableDescription;
    }

    private java.util.List<SearchNextCfgVariableItem> listSearchValues = new ArrayList<SearchNextCfgVariableItem>();
    private int iCurrentIndexOflistSearchValues = 0;
    private int iMaxItems_listSearchValues = 0;
	public String getCfgFileFame() { return cfgFilename; }
	
	public void setfInputPath(String path)
	{
		fInputPath = new File(path);
	}

    @Override
    public void removeUpdate(DocumentEvent e) {
    	bTextAreaChanged = ! textArea.getText().equals(lipsync2Smil.getTextOfFile(cfgFilename));
    	if (bTextAreaChanged)
    		savePropButton.setEnabled(true);
    	else
       		savePropButton.setEnabled(false);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
    	bTextAreaChanged = ! textArea.getText().equals(lipsync2Smil.getTextOfFile(cfgFilename));
    	if (bTextAreaChanged)
    		savePropButton.setEnabled(true);
    	else
       		savePropButton.setEnabled(false);
    }

    @Override
    public void changedUpdate(DocumentEvent arg0) {

    }
    
	public void setCfgFileName(String fname)
	{
		cfgFilename = fname;
		fileCgf = new File(cfgFilename);
		bTextAreaChanged = false;
		if (fileCgf.exists())
		{
			textArea.setText(lipsync2Smil.getTextOfFile(cfgFilename));
			textArea.getDocument().addDocumentListener(this);			
			savePropButton.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_save) +" " +fileCgf.getAbsolutePath());
			savePropButton.setEnabled(false);
		}
		else
		{
			textArea.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +fileCgf.getAbsolutePath());
			savePropButton.setEnabled(false);
			textArea.setEnabled(false);
			savePropButton.setEnabled(false);
			editXmlFileListButton.setEnabled(false);
			searchNextValueButton.setEnabled(false);
			return ;
		}
		// jscrollPane.setLocation(1, 1);
		textArea.setCaretPosition(0);
		// jscrollPane.getViewport().setViewPosition(new Point(0,0));
		
	}
	
    JTextArea textArea = new JTextArea();
    JButton savePropButton, editXmlFileListButton, searchNextValueButton;
    JScrollPane jscrollPane;
    
   // private final static String newline = "\n";
    private Lipsync2Daisy lipsync2Smil;
    private JSplitPane splitPane;
    private JTextArea jNextValueText = new JTextArea(/* 2, 180 */);
    private JScrollPane areaScrollPane;
    private JLabel jlabelWindow = new JLabel();

    public ConsoleEditCfg(String name, Lipsync2Daisy p_lipsync2Smil)
    throws IOException
    {    	
    	super(name);
    	lipsync2Smil = p_lipsync2Smil;
    	xmlfilter = new FileNameExtensionFilter(
    			lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_selectxmlfiles), "xml");
    	init();
    	initVariableArray();
    }
    
    private void initVariableArray()
    {
    	listSearchValues.add(new SearchNextCfgVariableItem("dctitle", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_header)));
    	listSearchValues.add(new SearchNextCfgVariableItem("page_lipsync_time_on_off", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_page_onoff)));    	
    	listSearchValues.add(new SearchNextCfgVariableItem("change_xmlelements_startendtimes_between_wordelements", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_change_xmlelements_startendtimes_between_wordelements)));
    	listSearchValues.add(new SearchNextCfgVariableItem("doctitle_on_off", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_css_file)));

    	int executetype = lipsync2Smil.getExecutetype();
    	if (executetype == Lipsync2Daisy.cnstExecuteDaisy3)
    	{
        	listSearchValues.add(new SearchNextCfgVariableItem("dtbookoutputfilename", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_dtbookoutputfilename)));
        	listSearchValues.add(new SearchNextCfgVariableItem("css_file", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_css_file)));    	
        	listSearchValues.add(new SearchNextCfgVariableItem("dtbook.listAddDaisy3SmillRef", "????"));    	
        	listSearchValues.add(new SearchNextCfgVariableItem("dtbook.listNoDaisy3SmillRefIfManySentencies", "????"));
        	listSearchValues.add(new SearchNextCfgVariableItem("dtbook.pagenum_onoff", "????"));
        	listSearchValues.add(new SearchNextCfgVariableItem("picture_exts", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_img_file_extensions)));
        	listSearchValues.add(new SearchNextCfgVariableItem("picture_media_types", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_img_file_extensions)));    		
        	listSearchValues.add(new SearchNextCfgVariableItem("audio_exts", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_audio_file_extensions)));
        	listSearchValues.add(new SearchNextCfgVariableItem("customtest_elements", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_customtest_elements)));
        	listSearchValues.add(new SearchNextCfgVariableItem("customtest_elements_removed_from_head_element", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_customtest_elements_removed_from_head_element)));    	
    	}
    	else
    	{
        	listSearchValues.add(new SearchNextCfgVariableItem("dc_authrows", lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfg_variable_dc_authrows)));    		
        	// listSearchValues.add(new SearchNextCfgVariableItem("customtest_elements_removed_from_head_element", "Kirjailijoiden nimet, jotka on tekijöinä."));    		
    	}
    	
    	
    	iMaxItems_listSearchValues = listSearchValues.size(); 

    }
    
    protected ConsoleEditCfg(String name)
    throws IOException
    {
    	super(name);
    	init();
    }
    
    protected ConsoleEditCfg() throws IOException {
    	super();
    	init();
    }

    private static JPanel getFixedPanel(JComponent jcomp)
    {
    	JPanel fieldPanel = new JPanel(new BorderLayout());
    	if (jcomp != null)
    		fieldPanel.add(jcomp, BorderLayout.CENTER);
    	return fieldPanel;
    }
    
    private void init()
    throws IOException
    {
    	if (lipsync2Smil != null)
    	{
    		fc = new FileChooser(this, lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_readedfiles), lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_selectfiles), 
    			lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_removefileselections));
    	}
    	
    	textArea.setLineWrap(false);    	      	
    	
    	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	splitPane.setOneTouchExpandable(true);
    	// splitPane.setDividerLocation(160);
    	
    	if (lipsync2Smil != null)
    	{
    		jlabelWindow.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_config_window));
    		jNextValueText.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cfgedit_description));
    	}
        // Add a scrolling text area
        textArea.setEditable(true);
        int top = 10;
        int left  = 10;
        int bottom = 10;
        int right  = 10;
        textArea.setMargin(new Insets(top, left, bottom, right) );
        textArea.setRows(25);
        textArea.setColumns(200);
        jscrollPane = new JScrollPane(textArea);
        
        JPanel jButtonPanel = new JPanel();
        GridLayout layout = new GridLayout(2,1);
        	int iHorGap = 4;
        	/*
        	layout.setHgap(iHorGap);
        	layout.setVgap(iHorGap);
        	*/
        	//layout.setColumns(2);
        	// Border empty = BorderFactory.createEmptyBorder();        	
        	// jPane.setLayout(layout);
       	// jButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        	
       	savePropButton    		= new JButton((lipsync2Smil == null ? "" : lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_save_into_cfg_file)) +"...");
       	editXmlFileListButton   = new JButton((lipsync2Smil == null ? "" : lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_select_lipsync2_files)) +"...");
       	searchNextValueButton   = new JButton((lipsync2Smil == null ? "" : lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_search_next_usually_changing_value)));
                        	
       	// executeTypeList.addActionListener(this);

       	/*
        jButtonPanel.add(getFixedPanel(savePropButton));
        //jButtonPanel.add(getFixedPanel(searchNextValueButton));            
        jButtonPanel.add(getFixedPanel(editXmlFileListButton));            
        */    
        // jPane.add(getFixedPanel());
        JPanel labelPanel = new JPanel();
        // mainPanel.add(jPane, BorderLayout.NORTH);
        // ((JComponent)getContentPane()).setBorder(   
        //     BorderFactory.createMatteBorder( 10, 10, 10, 10, Color.blue ) );  
        ((JComponent)getContentPane()).setBorder(   
             BorderFactory.createEmptyBorder( 10, 5, 10, 5 ) );  
        JPanel jMainPanel = new JPanel();
       	GridLayout layoutMain = new GridLayout(2,1);
       	// int iHorGap = 4;
       	layoutMain.setHgap(iHorGap);
       	layoutMain.setVgap(iHorGap);
       	//layoutMain.setColumns(2);

        jMainPanel.setLayout(layoutMain);
            
            /*
                         JPanel nextSearchValuePanel = new JPanel();            
            nextSearchValuePanel.add(new JLabel(" cat"), BorderLayout.CENTER);            
            getContentPane().add(nextSearchValuePanel, BorderLayout.CENTER);
            

             */

            // jMainPanel.add(new JLabel("Ohjelmalla muutetaan Lipsync-ohjelman tuottamia .xml tiedostoja Daisy 2- ja Daisy 3 tiedostoiksi"), BorderLayout.NORTH);
            // jMainPanel.add(jPane, BorderLayout.CENTER);            
        	labelPanel.add(jlabelWindow, BorderLayout.NORTH);
       // labelPanel.add(new JLabel(" "), BorderLayout.CENTER);
            
       // getContentPane().add(labelPanel, BorderLayout.NORTH);
            
            // GridLayout
       java.awt.BorderLayout jSearchNextValueLayout = new java.awt.BorderLayout();
           // jSearchNextValueLayout.setHgap(10);
           // jSearchNextValueLayout.setVgap(0);
   	   GridLayout layoutjSearchNextValuePanel = new GridLayout(5,1);
   	   layoutjSearchNextValuePanel.setHgap(5);
   	   layoutjSearchNextValuePanel.setVgap(5);
   	   
       JPanel jSearchNextValuePanel = new JPanel(/* layoutjSearchNextValuePanel */ /* jSearchNextValueLayout */);
       // jSearchNextValuePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
       // jSearchNextValuePanel.add(searchNextValueButton /*, BorderLayout.NORTH */);       
       jNextValueText.setRows(3);
       jNextValueText.setColumns(200);       
       jNextValueText.setWrapStyleWord(true);
       jNextValueText.setLineWrap(true);
       jNextValueText.setEditable(false);
            /*
            Dimension jNextValueTextDimension = new Dimension(50, 50);
            jNextValueText.setPreferredSize(jNextValueTextDimension);            
            jNextValueText.setMaximumSize(jNextValueTextDimension);
            jNextValueText.setMinimumSize(jNextValueTextDimension);
            */
            
            /* jSearchNextValuePanel.setPreferredSize(jNextValueTextDimension);            
            jSearchNextValuePanel.setMaximumSize(jNextValueTextDimension);
            jSearchNextValuePanel.setMinimumSize(jNextValueTextDimension);
            */
       areaScrollPane = new JScrollPane(jNextValueText);
       //areaScrollPane.setVerticalScrollBarPolicy(
         //   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED & JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       // areaScrollPane.setPreferredSize(new Dimension(250, 250));
       //jSearchNextValuePanel.add(areaScrollPane /*, BorderLayout.SOUTH */);
            
            // jSearchNextValuePanel.add(comp);
       JPanel jUpperPanel = new JPanel(/* new BorderLayout() */);
       jUpperPanel.setLayout(layoutjSearchNextValuePanel);
       // jUpperPanel.setBackground(Color.BLACK);
       //jUpperPanel.setLayout(layout);     
       jUpperPanel.add(labelPanel);
       jUpperPanel.add(savePropButton);
       jUpperPanel.add(getFixedPanel(editXmlFileListButton));
       // searchNextValueButton.setPreferredSize(jButtonPanel.getPreferredSize());
       jUpperPanel.add(searchNextValueButton);
       jUpperPanel.add(areaScrollPane/* , BorderLayout.SOUTH */);

      // getContentPane().add(jPane, BorderLayout.CENTER);
       Dimension jPanelDimension = new Dimension(50, 50);
       /*
       jButtonPanel.setPreferredSize(jPanelDimension);            
       jButtonPanel.setMaximumSize(jPanelDimension);
       jButtonPanel.setMinimumSize(jPanelDimension);
       */
       JPanel jscroll = new JPanel();
          //Create a split pane with the two scroll panes in it.
       JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            										jUpperPanel, /* jscroll */ jscrollPane);
       splitPane.setOneTouchExpandable(true);
       splitPane.setContinuousLayout(true);
       splitPane.setDividerLocation(190);
       splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
        	@Override
            	public void propertyChange(PropertyChangeEvent pce) {
            		/*
            		jscrollPane.getSize().getWidth()
            		jscroll.getSize().getHeight()
            		*/
            	}
       });            
            
            // getContentPane().add(splitPane, BorderLayout.SOUTH);
       // getContentPane().add(jscroll, BorderLayout.SOUTH);

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
       getContentPane().add(splitPane, BorderLayout.SOUTH);
            
       jButtonPanel.paint(this.getGraphics());
            // jMainPanel.setBackground(Color.BLACK);
                        
            savePropButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		savePropButtonPressed(e);
            	}
            });

            editXmlFileListButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		editXmlFileListPressed(e);
            	}
            });

                                 
            searchNextValueButton.addActionListener(new ActionListener()
            {
            	public void actionPerformed(ActionEvent e)
            	{
            		searchNextValueButtonPressed(e);
            	}
            });

        pack();
        
        // addWindowListener(new LipsyncWindowAdapter(this, this.lipsync2Smil));
        // setVisible(true);

  
    }

    private void executeTypeListChanged(String executeMode)
    {
    	if (executeMode != null && lipsync2Smil.getConsole() != null)
    		lipsync2Smil.setExecutetype(executeMode);
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
    
   
    private void savePropButtonPressed(ActionEvent e)
    {  // it's easier to save file content on groovy side of code:
    	
    	String value = this.textArea.getText();
    	String ret = this.lipsync2Smil.saveCfgText(cfgFilename, value);
    	if (ret == null || ret.trim().length() == 0)
    	{           
        	bTextAreaChanged = false;
        	if (bTextAreaChanged)
        		savePropButton.setEnabled(true);
        	else
           		savePropButton.setEnabled(false);  
        	jNextValueText.setText(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_savedfile) +": " +cfgFilename.toString());
    	}
    	else
    		jNextValueText.setText(ret.toString());    	
     }
    
    private void editXmlFileListPressed(ActionEvent e)
    {
    	String text = this.textArea.getText();
    	Properties prop = new Properties(); 
    	StringReader sr = new StringReader(text);
    	try {
    		prop.load(sr);
    		String xmlfilelist = prop.getProperty(lipsync2Smil.cnst_locale_prop_field_xmlfles, null);
    		fc.setDialogTitle(lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_open));
    		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    		fc.setFileFilter(xmlfilter);
    		fc.setMultiSelectionEnabled(true);
    		fc.setCurrentDirectory(new File(fInputPath.getAbsolutePath()));
			fc.setSelectedFiles(null);
			fc.setAutoscrolls(true);
    		
    		if (xmlfilelist != null && xmlfilelist.trim().length() != 0)
    		{
    			String [] fnames = xmlfilelist.split(",");
    			ArrayList<File> listFile = new ArrayList<File>();
    			for(String fname : fnames)
    			{
    				listFile.add(new File(fInputPath.getAbsolutePath(), fname.trim()));
    			}
    			File [] arrFiles = new File [listFile.size()];
    			int i = 0;
    			for(File f : listFile)
    			{
    				arrFiles[i++] = f;
    			}
    			fc.setSelectedFiles(arrFiles);
    		}
    		
    		// fc.setVisible();
    		
           	StringBuffer sb = new StringBuffer();
    		int returnVal = fc.showOpenDialog(this);
    		if (returnVal == JFileChooser.CANCEL_OPTION && !fc.isUnselectedButtonPressed())
    			return ;
    		else
       	 	if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = fc.getSelectedFiles();
                if (files == null || files.length == 0)
           	 	{
           	 		sb.append("");
           	 		xmlfilelist = "";
           	 	}
                else
                {
	                fInputPath = files[0].getParentFile();
	               	String [] arrvalues = new String [files.length]; 
	            	int i = 0;
	                for(File f : files)
	                {
	                  	arrvalues[i++] = f.getName();
	                }
	               	java.util.List<String> listvalues = lipsync2Smil.getSortedFileNameList(arrvalues, xmlfilter.getExtensions());
	               	i = 0;
	                for(String fname : listvalues)
	                {
	                   	// System.out.println ("" +fname);
	                   	if (i++ != 0)
	                   		sb.append(", ");
	                   	sb.append(fname.toString());
	                }
	               	if (sb.toString().length() > 0)
	               	{
	                }
                }
            }
       	 	else
       	 	{
       	 		sb.append("");
       	 		xmlfilelist = "";
       	 	}
       		// prop.setProperty(lipsync2Smil.cnst_locale_prop_field_xmlfles, sb.toString());
       		// StringWriter sw = new StringWriter();
       		//prop.store(sw, "");
       		//textArea.setText(sw.toString());
       		int ind = text.indexOf("\n" +lipsync2Smil.cnst_locale_prop_field_xmlfles);
       		if (ind > -1)
       		{
       			String substr = text.substring(ind +1 +lipsync2Smil.cnst_locale_prop_field_xmlfles.length());
      			String before = text.substring(0, ind);
       			int ind2 = text.indexOf("=", ind);
       			if (ind2 > -1)
       			{
       				String value = text.substring(ind2+1);
       				if (value != null)
       				{
       					int ind3 = text.indexOf("\n", ind2);
       					if (ind3 == -1)
       						ind3 = text.length() -1;
       					if (ind3 > -1)
       					{
       						String var_value = text.substring(ind2, ind3);
       						if (var_value != null)
       						{
       							String after = text.substring(ind3+1);
       							text = before +"\n" +lipsync2Smil.cnst_locale_prop_field_xmlfles +"="+ sb.toString() /* var_value */ + after;
       						}
       					}
       				}
        		}
        		textArea.setText(text);
        	}
    	}catch(Exception e1){
    		System.out.println(e1.getMessage());    		
    	}
    }
    
    private int lastIndexOf(final String search, final String strtextArea, final int ind)
    {
    	if (search == null)
    		return -1;
    	if (strtextArea == null)
    		return -1;
    	if (ind == -1)
    		return -1;
    	int ind2 = strtextArea.indexOf(search);
    	int iPrevInd = -1;
    	while(ind2 != -1 && ind2 < ind)
    	{
    		iPrevInd = ind2;
    		ind2 = strtextArea.indexOf(search, ind2+1);
    	}
    	return iPrevInd;
    }
    
    private int noCommentLine(final String variablename, final String strtextArea, final int ind)
    {
    	if (variablename == null)
    		return -1;
    	if (strtextArea == null)
    		return -1;
    	if (ind == -1)
    		return -1;
    	String before = strtextArea.substring(0, ind);
    	String foundAndAfter = strtextArea.substring(ind);
    	if (before == null || before.trim().length() == 0)
    		return ind;
    	if (!before.contains("#"))
    		return ind;
    	int indLF = lastIndexOf("\n", strtextArea, ind);
    	if (indLF == -1) // # char but no LF; that is comment line
    	{
    		int indNew = strtextArea.indexOf(variablename, ind+variablename.length());
    		if (indNew == -1)
    			return ind;
    		return noCommentLine(variablename, strtextArea, indNew);
    	}
    	else
    	{
    		int indComment = strtextArea.indexOf("#", indLF);
    		if (indComment == -1 || indComment > ind)
    			return ind; // ok search
    		return noCommentLine(variablename, strtextArea, ind+1);
    	}
    	// return ind;
    }
    
    private void searchNextValueButtonPressed(ActionEvent e)
    {
    	item = listSearchValues.get(iCurrentIndexOflistSearchValues++);
    	if (iCurrentIndexOflistSearchValues == iMaxItems_listSearchValues)
    		iCurrentIndexOflistSearchValues = 0;
    	if (item == null)
    		return ;
    	
    	jNextValueText.setText("");
    	
    	/*
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
         */
           	 Font curr  = jNextValueText.getFont();  
             Font bold  = null;  
             int  style = curr.getStyle();  
             if (style == Font.PLAIN) {  
                bold = curr.deriveFont(Font.BOLD);  
             } else if (style == Font.ITALIC) {  
                bold = curr.deriveFont(Font.ITALIC | Font.BOLD);  
             }
             /*
             if (bold != null) {  
            	 jNextValueText.setFont(bold);          	 
             } 
             */ 
             // jNextValueText.setForeground(Color.BLUE);
         	 jNextValueText.append(item.variablename);
         	 // try  {
         		int len = jNextValueText.getDocument().getLength();
         		Highlighter hlighter = jNextValueText.getHighlighter();
        		int posOfTextToScroll = -1;
        		if (len > 0)
        		{
        			posOfTextToScroll = 0;
    	    		try {
    	    			hlighter.removeAllHighlights();
    	    			hlighter.addHighlight(posOfTextToScroll, len, DefaultHighlighter.DefaultPainter);	    				
    	    				// textArea.setSelectedTextColor(Color.CYAN);
    	    			jNextValueText.setCaretPosition(posOfTextToScroll);
    	    		} catch(Exception e2){
    	    			try {
    		    				hlighter.addHighlight(posOfTextToScroll, len-1, DefaultHighlighter.DefaultPainter);
    		    				jNextValueText.setCaretPosition(posOfTextToScroll);
    		    			} catch(Exception e3){		    				
    		    			}
    	    			}    	    			    			
        		}

         		 /// jNextValueText.getHighlighter().addHighlight(0, len -1, cyanPainter);
    		//  } catch (BadLocationException ble) {
         		 // System.out.println(ble.getMessage());
             // }
         	 // jNextValueText.setFont(curr);
         	 // jNextValueText.setForeground(Color.BLACK);
         	 jNextValueText.append(": " +item.variableDescription);
         	 jNextValueText.updateUI();
         	 areaScrollPane.updateUI();
         	 /*
          }
        });
        */
    	
     	String strtextArea = textArea.getText();
    	if (strtextArea == null ||strtextArea.trim().length() == 0)
    		return;
    	int ind = strtextArea.indexOf(item.variablename);
    	if (ind > -1)
    	{
    		hlighter = textArea.getHighlighter();
    		posOfTextToScroll = -1;
    		int ind2 = strtextArea.indexOf("=", ind);
    		if (ind2 > -1)
    		{
    			ind2 = noCommentLine(item.variablename, strtextArea, ind);
    			int indLF = strtextArea.indexOf("\n", ind2);
	    		if (indLF > ind2) // ok = character
	    		{
	    			posOfTextToScroll = ind2;
	    			try {
	    				hlighter.removeAllHighlights();
	    				hlighter.addHighlight(posOfTextToScroll, indLF, DefaultHighlighter.DefaultPainter);	    				
	    				// textArea.setSelectedTextColor(Color.CYAN);
	    				textArea.setCaretPosition(posOfTextToScroll);
	    			} catch(Exception e2){
		    			try {
		    				hlighter.addHighlight(posOfTextToScroll, ind2, DefaultHighlighter.DefaultPainter);
		    				textArea.setCaretPosition(posOfTextToScroll);
		    			} catch(Exception e3){		    				
		    			}
	    			}
	    		}	    			
    		}
    	}
    	
    }
    
    public void startReadThreads()
    throws IOException
    {
        // Set up System.out
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
          	savePropButton.setEnabled(value);          	          	
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
    	
    /*
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
                    ??*
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            textArea.append(new String(buf, 0, len));

                            // Make sure the last line is always visible
                            // textArea.setCaretPosition(textArea.getDocument().getLength());

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
                    *??
                }
            } catch (IOException e) {
            }
        }
    }
    */
    
    public void actionPerformed(ActionEvent e) {
    	JComboBox cb = (JComboBox)e.getSource();
        String executeMode = (String)cb.getSelectedItem();
        executeTypeListChanged(executeMode);
    }
}