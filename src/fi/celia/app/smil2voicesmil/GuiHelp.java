package fi.celia.app.smil2voicesmil;

import java.awt.*;

import javax.swing.text.Document; 
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.event.AncestorListener;
import java.io.*;

import javax.swing.*;

import java.awt.event.*;
//import java.util.*;

// import javax.swing.event.*;
//import javax.swing.text.*;

/**
 * This class is showing html help file of Lipsync2Daisy app. It is a JFrame.
 * <p>
 * @author Tuomas Kassila
 *
 */
public class GuiHelp extends JFrame implements HyperlinkListener /* implements ActionListener,
DocumentListener */ {
	static final long serialVersionUID = 324343232423434235L;
	 private static final int PREF_W = 900;
    private static final int PREF_H = 700;

	private File fileHelp;

	 HTMLEditorKit kit = new HTMLEditorKit();
	 JEditorPane jEditorPane = new JEditorPane();
    //JTextArea textArea = new JTextArea();
    JButton closeButton;
    JScrollPane jscrollPane;
    private JLabel jlabelWindow = new JLabel();
    private Lipsync2Daisy lipsync2Smil;

    public GuiHelp(Lipsync2Daisy p_lipsync2Smil, String name)
    throws IOException
    {    	
    	super(name);
    	lipsync2Smil = p_lipsync2Smil;
    	init();
    }
        
    protected GuiHelp() throws IOException {
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
        jEditorPane.setEditorKit(kit);
        jEditorPane.setEditable(false);  
        jEditorPane.setContentType("text/html");
    	//textArea.setLineWrap(true);    	      	
        // Add a scrolling text area
        //textArea.setEditable(false);
        //textArea.setText(lipsync2Smil.getHelpText());
    	Document doc = kit.createDefaultDocument();
        jEditorPane.setDocument(doc);
        jEditorPane.setText(lipsync2Smil.getHelpText());
        jEditorPane.addHyperlinkListener(this);
        
        int top = 10;
        int left  = 10;
        int bottom = 10;
        int right  = 10;
        /*textArea.setMargin(new Insets(top, left, bottom, right) );
        textArea.setRows(25);
        textArea.setColumns(200);
        */
        jscrollPane = new JScrollPane(jEditorPane);
        jscrollPane.setAutoscrolls(true);
        
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
        	
       	closeButton    		= new JButton((lipsync2Smil == null ? "" : lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_help)) +"...");
                        	
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
            
       java.awt.BorderLayout jSearchNextValueLayout = new java.awt.BorderLayout();
      Dimension jPanelDimension = new Dimension(50, 50);
       //jMainPanel.add(jscrollPane); 
       getContentPane().add(jscrollPane, BorderLayout.CENTER);
                
        pack();
        
    }

	   @Override
	   public Dimension getPreferredSize() {
	      return new Dimension(PREF_W, PREF_H);
	   }

    public void showWindowsWithHelpText()
    {
    	jEditorPane.setText(lipsync2Smil.getHelpText());
    	getContentPane().remove(jscrollPane);
    	getContentPane().add(jscrollPane);
    	/*
    	try {
    		Thread.sleep(1000);
    	}catch(Exception e){    		
    	}
    	*/
    	jEditorPane.setCaretPosition(0); 
    	//jscrollPane.getViewport().setViewPosition(new Point(0,0));
    	 pack();
    	this.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
    	JComboBox cb = (JComboBox)e.getSource();
        String executeMode = (String)cb.getSelectedItem();
       // executeTypeListChanged(executeMode);
    }
    
    public void hyperlinkUpdate(HyperlinkEvent e) 
    {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JEditorPane pane = (JEditorPane) e.getSource();
            if (e instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                HTMLDocument doc = (HTMLDocument)pane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            } else {
                try {
                	String strUrl = e.getDescription();
                	jEditorPane.setText(lipsync2Smil.getHelpText(strUrl));
                    // pane.setPage(e.getURL());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}