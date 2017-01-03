package fi.celia.app.smil2voicesmil;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This class is listen "close" event from the application and it will then store gui user
 * field values into a user_home file of the application.
 * <p> 
 * Tämä luokka kuuntelee jframe close event:ia, jolloin ennen ohjelman sulkeutumista 
 * tallennetaan käyttäjän asetukset.
 * 
 * @author Tuomas Kassila
 *
 */
public class LipsyncWindowAdapter extends WindowAdapter
{
	private Lipsync2Daisy lipsync2Smil;
	private Console console;
	
	public LipsyncWindowAdapter(Console p_console, Lipsync2Daisy p_lipsync2Smil)
	{
		console = p_console;
		lipsync2Smil = p_lipsync2Smil;
	}
	
	public void windowClosing(WindowEvent we) {
		saveUserProperties();
	    System.exit(0);
	}
	
	/**
	 * This method is called when Console class is creating a new Lipsync2Smil instance.
	 * 
	 * @param p_lipsync2Smil
	 */
	public void setLipsync2Smil(Lipsync2Daisy p_lipsync2Smil)
	{
		lipsync2Smil = p_lipsync2Smil;
	}
	
	public void saveUserProperties()
	{
		lipsync2Smil.saveUserPropertiesFromGui(console.getCfg(), console.getInputPath(), 
				console.getOutputPath(), console.getCfgPath(), console.getExecuteType(), 
				console.getCorrectTime(), console.getCalculateLengthOfMP3Files());
		lipsync2Smil.saveGuiConversionPathSettings();
	}
}