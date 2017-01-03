/**
 * 
 */

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * @author tk
 *
 */
public class TestSwing {

	/**
	 * 
	 */
	public TestSwing() {
		// TODO Auto-generated constructor stub
		JPanel theButtonPanel = new JPanel(new BorderLayout());
		JButton button1 = new JButton("Fire");
		JButton button2 = new JButton("Pass");
		JButton button3 = new JButton("Forfiet 2");
		JButton button4 = new JButton("Fire 2");
		JButton button5 = new JButton("Pass2");
		JButton button6 = new JButton("Forfiet2");
	
		JPanel innerButtonContainer = new JPanel(new GridLayout(2, 3, 8, 8));
		innerButtonContainer.add(button1);
		innerButtonContainer.add(button2);
		innerButtonContainer.add(button3);
		innerButtonContainer.add(button4);
		innerButtonContainer.add(button5);
		innerButtonContainer.add(button6);
		
		Dimension jPanelDimension = new Dimension(200, 200);
		innerButtonContainer.setPreferredSize(jPanelDimension);
		innerButtonContainer.setMaximumSize(jPanelDimension);
		innerButtonContainer.setMinimumSize(jPanelDimension);
		

		theButtonPanel.add(innerButtonContainer);
		JFrame jframe = new JFrame("kissa");
		jframe.getContentPane().add(theButtonPanel, BorderLayout.CENTER);
		
		jframe.pack()
		jframe.setVisible(true)
		
	}

	static main(args) {
		TestSwing ts = new TestSwing();
	}

}
