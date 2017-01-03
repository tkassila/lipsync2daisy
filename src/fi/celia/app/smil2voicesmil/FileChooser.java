package fi.celia.app.smil2voicesmil;

import java.awt.BorderLayout;
//import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
//dimport javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
//import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * This class is a custom file chooser, which extends JFileChooser. It has its own label text fields
 * and some special content than its the parent class has.
 *  
 * @author tk
 *
 */
public class FileChooser extends JFileChooser /* JDialog */ implements ActionListener 
{
    private JPanel panel = new JPanel();
    private JFileChooser fc = new JFileChooser();
    private JButton unselectedButton = new JButton(); 
    private boolean isUnselectedButtonPressed = false;
    private boolean isButtonPressed = false;
    private Integer returnValue = null;
    private File [] selectedFiles = null;
    enum SEMANTIC_OPTIONS { CANCEL_SELECTED, OPEN_SELECTED, UNSELECTALL_SELECTED };
    private SEMANTIC_OPTIONS selected = null;
    
    public SEMANTIC_OPTIONS getSelected() { return selected; }
    public JFileChooser getFileChooser() { return fc; }
    public boolean isUnselectedButtonPressed() { return isUnselectedButtonPressed; }
    public boolean isButtonPressed() { return isButtonPressed; }

    public FileChooser(JFrame owner, String strHeader, String strApproveButtonText, String unselectedButtonText)
    {
    	super();
    	
        // super(owner, strHeader, true);
        // this.setTitle(strHeader);
        // fc.addActionListener(this);
        addActionListener(this);
        // fc.setApproveButtonText(strApproveButtonText);
        setApproveButtonText(strApproveButtonText);
        unselectedButton.setLayout(new BorderLayout());
        String [] arrText = unselectedButtonText.split("\n");
        JLabel label1 = new JLabel(arrText[0]);
        JLabel label2 = new JLabel(arrText[1]);
        JPanel jp = new JPanel(); 
        jp.setLayout(new BorderLayout());
        jp.add(BorderLayout.NORTH,label1);
        jp.add(BorderLayout.CENTER,label2);
        unselectedButton.add(BorderLayout.NORTH,jp);
        // unselectedButton.setText(unselectedButtonText);
        unselectedButton.addActionListener(this);
        // panel.setLayout(new BorderLayout());
        // panel.add(fc, BorderLayout.CENTER);
        // panel.add(unselectedButton, BorderLayout.SOUTH);
        // panel.setVisible(true);
        // getContentPane().add(panel);
        setAccessory(unselectedButton);
    }

    public void setVisible()
    {
        // pack();
        // setVisible(true);
    }
    
    /*
    public int showOpenDialog()
    {
        // setVisible(true);
        System.out.println("1");
        while(!isButtonPressed)
            sleep();
        System.out.println("2");
        setVisible(false);
        if (isUnselectedButtonPressed)
            return -1000;
        System.out.println("3");
        return returnValue.intValue();
    }
*/
    
    private void sleep()
    {
        try {
            Thread.sleep(1000);
        }catch(Exception e){            
        }
    }
    
    public void actionPerformed(ActionEvent e)
    {
        //System.out.println(e.toString());
        Object source = e.getSource();
        String command = e.getActionCommand();
        selectedFiles = null;
        isButtonPressed = true;
        isUnselectedButtonPressed = false;
        if (source == unselectedButton)
        {
            this.isUnselectedButtonPressed = true;
            selected = SEMANTIC_OPTIONS.UNSELECTALL_SELECTED;
            selectedFiles = null;
            this.setSelectedFiles(null);
            this.updateUI();
            this.approveSelection();
        }
        else
        if (source == this)
        {
            if (command.equals(JFileChooser.APPROVE_SELECTION))
            {
            	selectedFiles = getSelectedFiles();
            	// returnValue = new Integer(JFileChooser.APPROVE_SELECTION);
            	selected = SEMANTIC_OPTIONS.OPEN_SELECTED;
            }
            else
            if (command.equals(JFileChooser.CANCEL_SELECTION))
            {
             	// returnValue = new Integer(FileChooser.CANCEL_SELECTION);
             	selected = SEMANTIC_OPTIONS.CANCEL_SELECTED;
            }
        }
        // super.setVisible(false);
    }
}
