package fi.celia.app.smil2voicesmil;

import java.util.Properties;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


// call our own println implementation before normall System.println method:

import groovy.runtime.metaclass.java.lang.SystemMetaClass

import org.codehaus.groovy.tools.RootLoader
import org.apache.xml.resolver.tools.CatalogResolver;
import org.apache.xml.resolver.CatalogManager
// import groovy.runtime.metaclass.java.lang.StringMetaClass
import org.xml.sax.SAXParseException

import fi.celia.app.smil2voicesmil.daisy3.*
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.*;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle
import java.util.logging.Logger;
import java.io.InputStreamReader;
import java.awt.Toolkit
import java.math.RoundingMode
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Clipboard
import java.text.DecimalFormat
import java.text.SimpleDateFormat

import javax.swing.JFrame
import javax.swing.JOptionPane;


/**
 * Lipsync2Daisy Application main class:
 * <p>
 * This application has gui and command line interface. It is reading config files and directoriers,
 * input Lipsync xml files and at last generates Daisy 2 or 3 files as its output. Gui interface
 * has implemnted with the Java Swing framework. Mostly source files are writen with Groovy programming
 * language, some of those files with Java programming language. The application can execute with JRE 7 or 8
 * under Windows, Linux (not tested) and Macintosh (not tested) OS. Lauch script si done only for
 * Windows Os, that is .bat file. And sample command line .bat files, when staring a run as batch job
 * (=command line run).  
 * <p>
 * 
 * <p>
 * 25.1.14 Changed cfg: Cfig files etc are now located under base cfg directory (config): daisy2templates 
 * or daisy3templates directories. The daisy3templates directory is partly reading also the daisy2templates 
 * directory at first. Removed paths from template and cfg files names. Paths are added into readed files
 * after base cfg directory. And they are either daisy3templates or daisy2templates dirrectory name as
 * a subdirectory.
 * 
 * 17.4.14 If xml file is empty, then every input files (<number>.xml) are readed automatic
 * in a order of numbers of base file name. Anotherwise in the order of readed lipsync xml files
 * are deffined by a user. 
 * <p>
 * 18.10.14 ui/i18 text added. 8.11.14 added userprofile variables and logger.
 * <p>
 * 13.06.2015 Changed application name from Lipsync2Smil into Lipsync2Daisy, becuase application 
 * generates Daisy 2 or 3 files from Lipsync application xml files. Not only .smil files. Side effect
 * is that many especially config files names are after Lipsyn2smil old applaiciaton name.
 *  
 *   * This main class of the application:
 * - this class is open UI classes (Console.groovy and ConsoleCfg.groovy inside of Console.groovy)
 * - From UI it is handling conversion event (call), either daisy 2 or daisy 3 generate request to
 * lipsync xml files after configuration files.
 * 
 * - In the covnersion this class is using VoiceDataFile.groovy, which is reading one lipsynce xml file.
 * VoiceDataFile instance is producing and contains sequences of VoiceData items, which each holds usually
 * one lipsynce data xml element. In the special cases it in can hold more data, like an sentence. An
 * sentence is collected from lipsync words (start and end times in voice).
 * <p>
 * After collected data, it is manipulated different ways to be ready to produce all generated daisy 2
 * or 3 files into user selected directory. UI contains text components for input files or directories.
 * In the bottom there is one big textarea, which will print different conversion messages.
 *  <p>
 *  Below is a planuml picture (showing by an eclipse plugin):
 *  Eclipse: menu Window -> Show -> Other -> PlanUml -> see below code panel: PlanUML
 *  - there is also a picture file (.png) of this application, which is generated from
 *  below planuml statements:
 * 
 *  @startuml
 
 title __Application main logic and classes:__ **generate daisy file from lipsync xml files** 
actor "Application User" as User
actor "Command line shell" as Command_line_shell 
box "Lipsyn2Daisy application" #LightBlue
boundary Console << (C,#ADD1B2) JFrame >>
control Lipsync2Daisy  << (C,#ADD1B2) contains readed data >>

note right of Lipsync2Daisy #aqua
main method
end note

entity VoiceDataFile  << (C,#ADD1B2) contains VoiceData >>

entity VoiceData  << (C,#ADD1B2) corresponds one lipsync xml element, except sentencies >>
database File

User -> Console : << 'Generate' Button push >>
activate Lipsync2Daisy
Command_line_shell -> Lipsync2Daisy : << command line parameters >>
Console -> Lipsync2Daisy : << Start to generate daisy files after text field values >> 
Lipsync2Daisy -> Lipsync2Daisy : << Generate daisy files >>
activate Lipsync2Daisy #DarkSalmon
Lipsync2Daisy -> File : << read config and template files >>
File -> Lipsync2Daisy : << content of files >>
Lipsync2Daisy -> Console  : println  
== To read input files and dirs ==
Lipsync2Daisy -> VoiceDataFile : << To read one lipsync xml file >>
activate VoiceDataFile #DarkSalmon
VoiceDataFile -> Console : println 
VoiceDataFile -> File : << read config and template files >>
File -> VoiceDataFile : << content of files >>
VoiceDataFile -> VoiceData : << create VoiceData instancies >>
VoiceDataFile -> Lipsync2Daisy : << one VoiceDataFile instance >>
deactivate VoiceDataFile
Lipsync2Daisy -> Lipsync2Daisy : << all files read >>
VoiceData -> Console : println 

== Modify and produce voice memory data after template files ==
Lipsync2Daisy -> File : << Write daisy 2 or 3 files into user defined directory >>
Lipsync2Daisy -> Console : println 'Done'
deactivate Lipsync2Daisy
deactivate Lipsync2Daisy
end box 
@enduml

 * <p>
 * Tämä luokka eli ohjelma lukee annetusta asetustiedostosta muuttujien arvoja, lukuhakemiston,
 * ajohakemisto sekä kirjoitushakemisoton. Ohjelma on tehty lukemaan Lipsync-ohjelman tuottamia
 * .xml tiedostoja lukuhakemistosta, jotka se asetustiedostossa saatujen template-tiedostojen
 * mukaisesti muuttaa kirjoitushakemiston pipeline-ohjelman tuottamien kaltaisiksi .smil,
 * contennt.html sekä ncc.html tiedostoiksi (daisy2). Kirjoitetteassa daisy3 tieodostoja
 * edelliset saman hakemiston daisy tiedostot poistetaan. Kirjoitushakemiston tiedostot syntyvät siten,
 * että lukutiedostoista luetaan ja kirjoitetaan lauseittain tulostustiedostoiksi.
 * Yhden lauseen punct-tyyppinen piste tms merkki lopettaa lauseen.
 * <p> 
 * 4.6.12 Ohjelman käynnistyskomentorivi sekä .jar tehty.
 * <p>
 * 
 * 8.10.12 Daisy 3 formaatti aloitettu. Guita muutettu lisäkentillä (templatedir sekä ajotyyppi 
 * [+cfg tiedostoon muuttuja]).
 * 22.12.16 Korjattu help ikkunan nimi kielen mukaan jne.
 * 
 * @author Tuomas Kassila
 *
 * 
 */
// @groovy.transform.TypeChecked
public class Lipsync2Daisy {
	
	def boolean bXmlValidation = true
	def String strUILanguage = null
	def static public String smilbasefilename = "speechgen"
	
	def static logfilename
	// assumes the current class is called logger	
	private final static Logger LOGGER = Logger.getLogger(Lipsync2Daisy.class.getName());

	def listComboStrings = []
	def listSentenceComboStrings = []
	def String cnstUI_Combobox_daisy2uitext
	def String cnstUI_Combobox_daisy3uitext
	def public static final String cnst_lasrtarticlesofuser = "lipsync2daisy_lastarticlesofuser"
	def public static final String constUI_ReadTextFieldValueFromFile = "ui_readtextfieldvaluefromfile"
	def public static final String constUI_SaveTextFieldValueIntoFile = "ui_savetextfieldvalueinfofile"
	def public static final String constUI_ChangeCfgFile = "ui_changecfgfile"
	def public static final String constUI_EditCfgFile = "ui_editcfgfile"
	def public static final String constUI_CopyCfgFile = "ui_copycfgfile"
	def public static final String constUI_NewProject = "ui_newproject"
	def public static final String constUI_copytextfieldvalueFromConfig = "ui_copytextfieldvaluefromconfig"
	def public static final String constUI_Copyconfigfiles = "ui_copyconfigfiles"
	def public static final String constUI_changeintputdir = "ui_changeintputdir"
	def public static final String constUI_changecfgdir = "ui_changecfgdir"
	def public static final String constUI_changeoutputdir = "ui_changeoutputdir"
	def public static final String constUI_executeconvert = "ui_executeconvert"
	def public static final String constUI_usemp3length = "ui_usemp3length"
	def public static final String constUI_sentenceCombotip = "ui_sentencecombo"
	def public static final String constUI_executeButtontip = "ui_executebuttontip"
	def public static final String constUI_correctimes = "ui_correctimes"
	def public static final String constUI_usemp3lengthtip = "ui_usemp3lengthtip"
	def public static final String constUI_ui_xml_validation = "ui_xml_validation"
	def public static final String constUI_ui_delete = "ui_delete"
	// def public static final String constUI_checkboxxmlvalidationtip = "ui_checkboxxmlvalidationtip"	
	def public static final String constUI_converttypetip = "ui_converttypetip"
	def public static final String constUI_convertionrun = "ui_convertionrun"
	def public static final String constUI_convertionrun_helptext = "ui_convertionrun_helptext"
	def public static final String constUI_earlierconvertruns = "ui_earlierconvertruns"
	def public static final String constUI_appdescription = "ui_appdescription"
	def public static final String constUI_selectxmlfiles = "ui_selectxmlfiles"
	def public static final String constUI_ui_cfgfiletip = "ui_cfgfiletip"
	def public static final String constUI_ui_correctimestip = "ui_correctimestip"
	def public static final String constUI_ui_templatepathtip = "ui_templatepathtip"
	def public static final String constUI_ui_inputpathtip = "ui_inputpathtip"
	def public static final String constUI_ui_outputpathtip = "ui_outputpathtip"
	def public static final String constUI_ui_xml_validationtip = "ui_xml_validationtip"
	def public static final String constUI_ui_config_window = "ui_config_window"
	
	def public static final String constUI_ui_cfg_variable_header = "ui_cfg_variable_header"
	def public static final String constUI_ui_cfg_variable_page_onoff = "ui_cfg_variable_page_onoff"
	def public static final String constUI_ui_cfg_variable_change_xmlelements_startendtimes_between_wordelements = "ui_cfg_variable_change_xmlelements_startendtimes_between_wordelements"
	def public static final String constUI_ui_cfg_variable_doctitle_on_off = "ui_cfg_variable_doctitle_on_off"
	def public static final String constUI_ui_cfg_variable_dtbookoutputfilename = "ui_cfg_variable_dtbookoutputfilename"
	def public static final String constUI_ui_cfg_variable_css_file = "ui_cfg_variable_css_file"
	def public static final String constUI_ui_cfg_variable_img_file_extensions = "ui_cfg_variable_img_file_extensions"
	def public static final String constUI_ui_cfg_variable_audio_file_extensions = "ui_cfg_variable_audio_file_extensions"
	def public static final String constUI_ui_cfg_variable_customtest_elements = "ui_cfg_variable_customtest_elements"
	def public static final String constUI_ui_cfg_variable_customtest_elements_removed_from_head_element = "ui_cfg_variable_customtest_elements_removed_from_head_element"
	def public static final String constUI_ui_cfg_variable_dc_authrows = "ui_cfg_variable_dc_authrows"
	
	def public static final String constUI_ui_save_into_cfg_file = "ui_save_into_cfg_file"
	def public static final String constUI_ui_select_lipsync2_files = "ui_select_lipsync2_files"
	def public static final String constUI_ui_search_next_usually_changing_value = "ui_search_next_usually_changing_value"
	def public static final String constUI_ui_cfgfilesofpath = "ui_cfgfilesofpath"
	def public static final String constUI_ui_cfgfile = "ui_cfgfile"
	def public static final String constUI_ui_lipsyncfilepath = "ui_lipsyncfilepath"
	def public static final String constUI_ui_outputpath = "ui_outputpath"
	def public static final String constUI_ui_dirnotexists = "ui_dirnotexists"
	def public static final String constUI_ui_diirallreadyexists = "ui_dirallreadyexists"
	def public static final String constUI_ui_configdir_does_not_exist = "ui_configdir_does_not_exist"	
	def public static final String constUI_ui_shoulddircreated = "ui_shoulddircreated"
	def public static final String constUI_ui_created = "ui_created"
	def public static final String constUI_ui_cannotcreatedir = "ui_cannotcreatedir"
	def public static final String constUI_ui_dirdontexists = "ui_dirdontexists"
	def public static final String constUI_ui_fileisdir = "ui_fileisdir"
	def public static final String constUI_ui_dirisfile = "ui_dirisfile"
	def public static final String constUI_ui_filesareequal = "ui_filesareequal"
	def public static final String constUI_ui_willyoucopyfile = "ui_willyoucopyfile"
	def public static final String constUI_ui_fileexists_willyoucopyfile = "ui_fileexists_willyoucopyfile"
	def public static final String constUI_ui_intofile = "ui_intofile"
	def public static final String constUI_ui_copyfile = "ui_copyfile"
	def public static final String constUI_ui_errorinfilecopy = "ui_errorinfilecopy"
	def public static final String constUI_ui_yes = "ui_yes"
	def public static final String constUI_ui_no = "ui_no"
	def public static final String constUI_ui_file = "ui_file"
	def public static final String constUI_ui_fileexists_overwrite = "ui_fileexists_overwrite"
	def public static final String constUI_ui_shouldoverwrite = "ui_shouldoverwrite"
	def public static final String constUI_ui_warning = "ui_warning"
	def public static final String constUI_ui_readedfiles = "ui_readedfiles"
	def public static final String constUI_ui_selectfiles = "ui_selectfiles"
	def public static final String constUI_ui_removefileselections = "ui_removefileselections"
	def public static final String constUI_ui_error_in_calculating_file_length = "ui_error_in_calculating_file_length"
	def public static final String constUI_ui_xmlerror_row = "ui_xmlerror_row"
	def public static final String constUI_ui_cannotfound = "ui_cannotfound"
	def public static final String constUI_ui_pagenum_error_notnumber = "ui_pagenum_error_notnumber"
	def public static final String constUI_ui_paagenum_error_punct = "ui_paagenum_error_punct"
	def public static final String constUI_ui_paagenum_error_punct2 = "ui_paagenum_error_punct2"
	def public static final String constUI_ui_row = "ui_row"
	def public static final String constUI_ui_readed_line = "ui_readed_line"
	def public static final String constUI_ui_skip_this_line = "ui_skip_this_line"
	def public static final String constUI_ui_pagenumber_error_in_row = "ui_pagenumber_error_in_row"
	def public static final String constUI_ui_readed_autio_elements = "ui_readed_autio_elements"
	def public static final String constUI_ui_getnextglobalregistercounter_wrong_value = "ui_getnextglobalregistercounter_wrong_value"
	def public static final String constUI_ui_is_under_zero = "ui_is_under_zero"
	def public static final String constUI_ui_handlingofparameters = "ui_handlingofparameters"
	
	def public static final String constUI_ui_savedfile = "ui_savedfile"
	def public static final String constUI_ui_cfgedit_description = "ui_cfgedit_description"
	def public static final String constUI_ui_file_does_not_exist = "ui_file_does_not_exist"
	def public static final String constUI_ui_open = "ui_open"
	def public static final String constUI_ui_save = "ui_save"
	def public static final String constUI_ui_copy = "ui_copy"
	def public static final String constUI_ui_copyed = "ui_copyed"
	def public static final String constUI_ui_wrong_executetype = "ui_wrong_executetype"
	def public static final String constUI_ui_conversion_ready = "ui_conversion_ready"
	def public static final String constUI_ui_cannot_find_file = "ui_cannot_find_file"
	def public static final String constUI_ui_reading_cfgfile_of_user = "ui_reading_cfgfile_of_user"
	def public static final String constUI_ui_error = "ui_error"
	def public static final String constUI_ui_validation = "ui_validation"
	def public static final String constUI_ui_defcharacterset = "ui_defcharacterset"
	def public static final String constUI_ui_closeappafterconv = "ui_closeappafterconv"
	def public static final String constUI_ui_confpath_is_missing = "ui_confpath_is_missing"
	def public static final String constUI_ui_is_not_file = "ui_is_not_file"
	def public static final String constUI_ui_input_data_is_missing = "ui_input_data_is_missing"
	def public static final String constUI_ui_is_not_dir = "ui_is_not_dir"
	def public static final String constUI_ui_dir = "ui_dir"
	def public static final String constUI_ui_templatedir_is_missing = "ui_templatedir_is_missing"
	def public static final String constUI_ui_inputdir_is_missing = "ui_inputdir_is_missing"
	def public static final String constUI_ui_outputdir = "ui_outputdir"
	def public static final String constUI_ui_reading_cfg_and_input_files = "ui_reading_cfg_and_input_files"
	def public static final String constUI_ui_logfile = "ui_logfile"
	
	def public static final String constUI_ui_selectfromoutputdirs = "ui_selectfromoutputdirs"
	def public static final String constUI_ui_selectfrominputdirs = "ui_selectfrominputdirs"
	def public static final String constUI_ui_selectdirwherenextfieldsareset = "ui_selectdirwherenextfieldsareset"
	def public static final String constUI_ui_newbookdirectory = "ui_newbookdirectory"
	def public static final String constUI_ui_selectdirwherenewdircreatead = "ui_selectdirwherenewdircreatead"
	def public static final String constUI_ui_fromconfigdir = "ui_fromconfigdir"
	def public static final String constUI_ui_fromanotherconfigdir = "ui_fromanotherconfigdir"
	def public static final String constUI_ui_selectoptiondirtocopy = "ui_selectoptiondirtocopy"
	def public static final String constUI_ui_selectdirtocopy = "ui_selectdirtocopy"
	def public static final String constUI_ui_dirnametobecreated  = "ui_dirnametobecreated"
	def public static final String constUI_ui_underdir = "ui_underdir"
	def public static final String constUI_ui_newdir = "ui_newdir"
	def public static final String constUI_ui_copybookdirconfig = "ui_copybookdirconfig"
	def public static final String constUI_ui_selectconfigdirtocopy = "ui_selectconfigdirtocopy"
	def public static final String constUI_ui_xmlerrorrow = "ui_xmlerrorrow"
	def public static final String constUI_ui_missingcfgvalue = "ui_missingcfgvalue"
	def public static final String constUI_ui_dtbookoutputfilenamehasemptyvalue = "ui_dtbookoutputfilenamehasemptyvalue"
	def public static final String constUI_ui_missgininputfile = "ui_missgininputfile"
	def public static final String constUI_ui_areinputfilesreadedautomatic = "ui_areinputfilesreadedautomatic"
	def public static final String constUI_ui_wrongvalue = "ui_wrongvalue"
	def public static final String constUI_ui_invariable = "ui_invariable"
	def public static final String constUI_ui_missing = "ui_missing"
	def public static final String constUI_ui_content = "ui_content"
	def public static final String constUI_ui_file2 = "ui_file2"
	def public static final String constUI_ui_file3 = "ui_file3"
	def public static final String constUI_ui_empty = "ui_empty"
	def public static final String constUI_ui_input2filesincfgfile = "ui_input2filesincfgfile"
	def public static final String constUI_ui_missinginputfiles = "ui_missinginputfiles"
	def public static final String constUI_ui_severalsameinputfile = "ui_severalsameinputfile"
	def public static final String constUI_ui_readinglipsyncinputfiles = "ui_readinglipsyncinputfiles"
	def public static final String constUI_ui_lipsyncinclipboard = "ui_lipsyncinclipboard"
	def public static final String constUI_ui_xmlerrormissinglinenumber = "ui_xmlerrormissinglinenumber"
	def public static final String constUI_ui_rowdata = "ui_rowdata"
	def public static final String constUI_ui_column = "ui_column"
	def public static final String constUI_ui_wrong = "ui_wrong"
	def public static final String constUI_ui_index = "ui_index"
	def public static final String constUI_ui_character = "ui_character"
	def public static final String constUI_ui_lipsyncvalidationerror = "ui_lipsyncvalidationerror"
	def public static final String constUI_ui_cfgvariablemusthavevalue = "ui_cfgvariablemusthavevalue"
	def public static final String constUI_ui_value = "ui_value"
	def public static final String constUI_ui_writingfile = "ui_writingfile"
	def public static final String constUI_ui_readingfile = "ui_readingfile"
	def public static final String constUI_ui_staringintodaisy = "ui_staringintodaisy"
	def public static final String constUI_ui_and = "ui_and"
	def public static final String constUI_ui_cannotbeempty = "ui_cannotbeempty"
	def public static final String constUI_ui_cannotdeletefile = "ui_cannotdeletefile"
	def public static final String constUI_ui_deleted = "ui_deleted"
	def public static final String constUI_ui_previous = "ui_previous"
	def public static final String constUI_ui_modifyneighbornsentencies = "ui_modifyneighbornsentencies"
	def public static final String constUI_ui_old = "ui_old"
	def public static final String constUI_ui_userhomecfgfileisdir = "ui_userhomecfgfileisdir"
	def public static final String constUI_ui_cannotfinduserhomecfgfile = "ui_cannotfinduserhomecfgfile"
	def public static final String constUI_ui_name = "ui_name"
	def public static final String constUI_ui_type = "ui_type"
	def public static final String constUI_ui_userhomecfgfilecomment = "ui_userhomecfgfilecomment"
	def public static final String constUI_ui_noselection = "ui_noselection"
	def public static final String constUI_ui_writing = "ui_writing"
	def public static final String constUI_ui_cannotdelete= "ui_cannotdelete"
	def public static final String constUI_ui_variable = "ui_variable"
	def public static final String constUI_ui_configfile = "ui_configfile"
	def public static final String constUI_ui_putdtbookelements_file_name = "ui_putdtbookelements_file_name"
	def public static final String constUI_ui_givetextfieldvalue = "ui_givetextfieldvalue"
	def public static final String constUI_ui_cannotcreateoutputdir = "ui_cannotcreateoutputdir"
	def public static final String constUI_ui_deletingprevgenfiles = "ui_deletingprevgenfiles"
	def public static final String constUI_ui_new = "ui_new"
	def public static final String constUI_ui_exectypewrongvalue = "ui_exectypewrongvalue"
	def public static final String constUI_ui_exectypevaluesmustbevalues = "ui_exectypevaluesmustbevalues"
	def public static final String constUI_ui_moment = "ui_moment"
	def public static final String constUI_ui_modifying = "ui_modifying"
	def public static final String constUI_ui_documentselements = "ui_documentselements"
	def public static final String constUI_ui_warningemptycfgfile = "ui_warningemptycfgfile"
	def public static final String constUI_ui_warningemptycfgpath = "ui_warningemptycfgpath"
	def public static final String constUI_ui_warningemptyinputpath = "ui_warningemptyinputpath"
	def public static final String constUI_ui_warningemptyoutputpath = "ui_warningemptyoutputpath"
	def public static final String constUI_ui_warningsamediroutputinput = "ui_warningsamediroutputinput"
	def public static final String constUI_ui_wrongtimefieldvalue = "ui_wrongtimefieldvalue"
	def public static final String constUI_ui_isnotfloatpointnumber = "ui_isnotfloatpointnumber"
	def public static final String constUI_ui_returned = "ui_returned"
	def public static final String constUI_ui_help = "ui_help"
	
	/*
	def public static final String constUI_
	*/	
	
	def public static final String constUI_ui_reading_cfgs = "ui_reading_cfgs"
	
	def private String m_boldetcxmlelementsinmodifiedneighboursentencies
	def static Locale m_locale
	def static File m_fProp
	
	def public static Locale getLocale() { return m_locale }
	def public static File getFProp() { return m_fProp }
	
	// if something will be printed with method println this currentxmlfilename
	// will be printed at first time and variable currentxmlfilenameprinted is false: 
	def static currentxmlfilename = ""
	def static currentxmlfilenameprinted = false
	// def static oldprintln
	def static new_pritln_method // new redefined println method for Lipsync2Daisy app
	// enstead of Systme.out.println (groovy language)
	
	def bRemoveNeigthBornSentencesInPElements = false
	def bRemoveNeigthBornSentencesInAllElements = false

	public String getoutputpathname() { "generated_daisyfiles" }
	public String getinputpathname() { "lipsync2_xml_files" }

	def static File currentDir = new File(".")
	def static ap = currentDir.getAbsolutePath().replace(File.separator +".", "")
	def static cmpropfilename = /* "file:///" + */ ap /* +File.separator +"lib" */ +File.separator +"CatalogManager.properties"
	def static cm = new CatalogManager() // new CatalogManager(cmpropfilename)
	static CatalogResolver cr = new CatalogResolver(cm);
	def donotAddTheseFilesIntoOPF = ["tpbnarrator.res", "tpbnarrator_res.mp3"]
	def Properties uiProp = null
	def ResourceBundle messages
	def public static ResourceBundle static_messages
	def static Locale currentLocale = Locale.getDefault(); 
	String lang = currentLocale.getDisplayLanguage();
	String country = currentLocale.getDisplayCountry();
	
	def nav_template
	def page_template
	def String strClassName
	def String str_user_home
	def static boolean bGui = false // gui-userinterface, use in usage()
	def strAppName
	def bDebug = true
	def String strLipsyncCfg
	def static String m_currentDir
	def static String strLipsyncBaseCfgDir
	def String strSmilTemplateDir
	def String strSmilTemplateDir3, strSmilTemplateDir2
	def String strDtbookoutputfilename
	def Properties prop3
	def String strLipsyncDataDir
	def String strOutputDir
	def File   fLipsyncCfg
	def File   fOutputDir
	def File   fSmilTemplateDir
	def File   fLipsyncDataDir
	def String str_css_file
	def xmlVoiceDataFiles = [:]

	def listVDFs = []
	def public final static int cnstExecuteDaisy2 = 2
	def public final static int cnstExecuteDaisy3 = 3
	// unused: def final static String cnstSelectionTextExecuteDaisy2 // = "Luo Daisy 2 tiedostot"
	// unused: def final static String cnstSelectionTextExecuteDaisy3 // = "Luo Daisy 3 tiedostot"
	def public final static String cnstExecuteDaisy2CfgSubDir = "daisy2templates"
	def public final static String cnstExecuteDaisy3CfgSubDir = "daisy3templates"
	def hmDaisy3Templates = [:]
	def public final static cnstOPF_SmilPrefix = "smil-"
	def public final static cnstOPF_Prefix = "opf-"
	def static bCalculateMP3FileLengths = true
	def bookStructfilename
	def hmBookStruct = [:]
	def public final static String cnst_locale_prop_field_cfgpath = "cfgpath"
	def public final static String cnst_locale_prop_field_xmlfles = "xmlfles"
	def public final static String cnst_locale_prop_field_templatedir = "templatedir"
	def public final static String cnst_locale_prop_field_cfgfile = "cfgfile"
	
	def public static int executetype = cnstExecuteDaisy2
	/**
	 * used dtbook template file name, later use
	 */
	def dtbook_templatefilename 
	
	/**
	 * käytetään daisy3 tulostuksessa
	 */
	def templatefiles
	/**
	 * smil-tiedoston ylin template
	 */
	def strSmilTemplate
	/**
	 * smil-tiedoston par template, joka toistuu smil-templatessa
	 */
	def strParTemplate
	def strH1Template
	def content_file_name = "content.html"
	// def xhtmlDoctype
	def contenttype
	def ncc_file_name = "ncc.html"
	def strSeqTemplate
	def strSeqTemplate2	
	
	/**
	 * celian dtbookin id-tunnus
	 */
	def dc_identifier
	def dc_title
	def region_id
	/**
	 * käytetäänkö doc:ssa doctitlen alussa
	 */
	def static doctitle_on_off = true
	/**
	 * id arvoja:
	 */
	def doctitle
	def VoiceData vdTitle
	def listAuthors = []
	def docauthors
	def doctitle_par_id
	def doctitle_text_id
	def doctitle_audio_src_id
	def doctitle_content_id
	def doctitle_audio_id
	def par_id_base
	def text_id_base
	def content_id_base
	def audio_id_base
	def smil_tmpl_file_name
	def smil_meta_tmpl_file_name
	def strSmilMetaTemplate
	def par_tmpl_file_name
	def seq_tmpl_file_name
	def ncc_tmpl_file_name
	def content_tmpl_file_name
	def dtbookelements_file_name
	def File fDtbookelements_file
	def String strfDtbookelements
	def strNccTemplate
	def strContentTemplate
	def Template nccTemplate
	def Template contentTemplate
	def h1_tmpl_file_name
	def cntGlobalRegisterCounter = 0
	def cntNccRegisterCounter = 80000
	def cntSmilFileCounter = 0
	def static SimpleTemplateEngine engine = new SimpleTemplateEngine()	
	def xmlfiles
	def dtbooksmilreftemplatefile
	def listAddDaisy3SmillRef = []
	def listNoDaisy3SmillRefIfManySentencies = []
	DaisyIDs daisyIDs
	// def cfgFilePairs = [:]
	def static int totalpage = 0
	def static listNCXCustomTestElements = []
	def sbNCXCustomTestElements = new StringBuffer()
	def static int dtblevel = 1
	def static int maxpage = 0
	def static int ncx_total_pages = 0
	
	def static dtbook_pagenum_on_off = "on"
	def static String speechgen_smil_meta_tmpl_file_name
	def static String speechgen_ncx_meta_tmpl_file_name
	/**
	 * kulloisenkin, lähinnä VoiceDataFile objektin arvo
	 */
	VoiceDataFile 	currentVoiceDataFile	
	/**
	 * käyttämätön	
	 */
	VoiceData 		currentVoiceData
	/**
	 * kunkin VoiceDataXXX objektin arvo ja sitä vastaavan objektin map (muuttuja-arvo parien) arvot
	 */
	def voiceObjectMapValues = [:]
	/**
	 * tekijät, jos on useita. Tai yksi.
	 */
	def dc_authrows
	def dc_date
	def pages
	def tocItems
	def static bSentenceWordMode = true
	def page_lipsync_time_on_off = true
	def static double totaltime = 0.0
	def static double old_totaltime = 0.0
	boolean change_xmlelements_startendtimes_between_wordelements = false
	/** daisy 3:n dtbook tiedostonimi
	 */
	def dtbookfilename = "daisy3.xml"
	/**
	 * mahdollinen ajon aika siirtymä konfauksesta,
	 * mahdollisesti myöhemmin gui:sta
	 */
	def timeshift_into_voicedatas
	/**
	 * asetetaan dtbook:n viittauksista ja käytetään ncx:ssä
	 */
	def static iPlayOrder = 0
	/**
	 * gui:n poikkeama
	 */
	def correctTimeText
	def fi.celia.app.smil2voicesmil.Console console
	/**
	 * hashmappi: xml:n validoinnissa käytetyt rivit: alkup. riviindeksi/ alkup.rivi 
	 * @return
	 */
	def validateXmlRows = [:], arrayXmlRows
	/**
	 * elementit, joillee lisätään custemplate-attribuutti arvo, kun generoidaan par-elementtiä
	 * .smil tiedostoihin
	 */	
	def static listCustomTestAttributeNames = []
	def strCustomTestAttributeNames
	def static hmTypeAttributeOfDtbook  = [:]
	def strTypeAttributeOfDtbooks
	def static bWriteReadedDataAsText = false
	def page_sb, nav_sb, collectNCXPageList
	/**
	 * elementit, jotka esim lisätty customtest attribuutteina, ne,
	 * jotka kuitenkin poistetaan .smil tiedoston headeristä ja ei
	 * laiteta myöskään .ncx tiedostoon.
	 */
	def static listRemovedCustomTestAttributeNames = []
	/**
	 * VoiceTreeNode list items from voicedata list
	 */
	def ncxitemList
	/**
	 * hash variable is vd list for xml parsed correspond xml node
	 */
	def hmVdlist
	def boolean bExecuteGarbageCollector = true
	/**
	 * if this list is populated from cfg file, then in daisy 2 run, span page elements
	 * are moved into these elements. Like: </p> and </li>. In this case, correspond elementnames
	 * are: p and li 
	 */
	def movespanpage_elementnames = []

	/**
	 * This private class is used for to collect sequential VoiceData
	 * instancies into hierarchial view. A view is same kind structure
	 * than xml docbook has as an xml data. (An xml parser is used to
	 * define order of VDTreeNode instancies.) This classs is used
	 * to produce structure and content of NCX file.
	 */
	def class VDTreeNode {
		def VoiceData vd
		def VoiceData prev_vd
		def xmlnode
		def playorder = 0
		def data
		def VDTreeNode parent
		def VDTreeNode prev
		def children = []
		def last_children
		def noaddtreenodelist = true
		def nav_sb
		def page_sb
		def VDTreeNode currentheader, currentlevelstart
		def isLevelStarting = false
		
		def static int iTest = 0
		
		/**
		 * set (relplace) last nav_sb navPoint value
		 * @param navPoint
		 */
		def void setNavPoint(String navPoint)
		{
			if (nav_sb == null)
				nav_sb = ""
			nav_sb =  nav_sb.toString().replace("\$navPoint", navPoint)
		}
		
		def void correctLocalVoiceTreeNodeNCXPageString()
		{
			// TODO: IMPLEMENTATION!
		}

		def void collectLocalVoiceTreeNodeNCXPageString()
		{
			// TODO: IMPLEMENTATION!
		}

		def void setLocalVoiceTreeNodeNCXPageString()
		{
				iTest++
				// println "iTest=" +iTest
				/*
				if (this.vd.iPage > 0)
				{
					println "test"
				}
				*/

			  if (vd == null)
				   return
			  int ichildren = 0
			  if (children)
				  ichildren = children.size()
			  vd.isNCXItem = isNCXItem(vd)
			  if (vd.iH_level > 0 && (VoiceDataFile.depth == null ||
				  VoiceDataFile.depth < vd.iH_level ))
			  VoiceDataFile.depth = vd.iH_level
									  
			  if (ichildren == null ||
				  (ichildren == 0 && !vd.isNCXItem))
					  return
		
			  def StringBuffer local_nav_sb	 = new StringBuffer()
			  def StringBuffer local_page_sb = new StringBuffer()
				
			  if (vd.text.toString().toLowerCase().startsWith("<level"))
				   isLevelStarting = true
				  
			  def newchildren = []
			  
			  for(VDTreeNode chi in children)
			  {
				  chi.setLocalVoiceTreeNodeNCXPageString()
				  if (isLevelStarting && currentheader == null)
					  currentheader = chi
				  
				  if (/* (chi.isLevelStarting || vd.isNCXItem) && */
					  currentheader != chi && chi.nav_sb)
				  {
					 local_nav_sb << chi.nav_sb.toString().replaceAll("\$navPoint", "")
				  }
				  
				  if (chi.page_sb)
				  {
					  local_page_sb << chi.page_sb
					  // chi.page_sb = null
				  }
				  newchildren.add(chi)
			  }
			  
			  //if (vd.iPage > 0)
				  // println "test"
			  if (isLevelStarting && parent && parent.currentheader)
			  {
				  if (local_nav_sb && local_nav_sb.toString())
					   parent.currentheader.setNav_sb(local_nav_sb.toString())
				  else
					   parent.currentheader.setNav_sb("")
			  }

			  children = newchildren
			  setNavSbAandPageSb(local_nav_sb, local_page_sb)
		}
		
		def void collectVoiceTreeNodeNCXPageString_old()
		{
			  iTest++
				// println "iTest=" +iTest
								
			  if (vd == null)
				   return
			  int ichildren = 0
			  if (children)
				  ichildren = children.size()
			  /*
			  if (vd.isNCXItem)
					  println "dddd"
			   */
									  
			  if (ichildren == null ||
				  (ichildren == 0 && !Lipsync2Daisy.isNCXItem(vd)))
					  return
			
			  if (vd.text.toString().toLowerCase().startsWith("<level"))
				   isLevelStarting = true
				  
			  def StringBuffer local_nav_sb
			  def StringBuffer local_page_sb
			  for(VDTreeNode chi in children)
			  {
				  chi.collectVoiceTreeNodeNCXPageString()
				  if (isLevelStarting && chi.vd.iH_level > 0 && currentheader == null)
					  currentheader = chi

				  if (chi.isLevelStarting)
					  currentlevelstart = chi

				  if (isLevelStarting && currentheader.nav_sb)
				  {
					 if (local_nav_sb == null)
						 local_nav_sb = new StringBuffer()
					 local_nav_sb << currentheader.nav_sb.toString().replaceAll("\$navPoint", "")
				  }

				  /*
				 if (currentheader != chi && currentlevelstart != chi)
				  {
					  if (isLevelStarting && chi.vd.iH_level < 1
						  && vd.text.toString().toLowerCase().startsWith("</level") && currentheader)
					  {
						  currentheader.setNavPoint(( (currentlevelstart && currentlevelstart.nav_sb) ? currentlevelstart.nav_sb.toString() : ""))
					  }
					  else
					  // if this is not  <level or <level element and it's not one of same level vd children, then
					  // collect children data into
					  // if (!isLevelStarting || (isLevelStarting && (chi.vd.iH_level < 1 || chi.vd.iH_level > 0 && currentheader != chi && chi.nav_sb))
					  if (chi.page_sb && (!isLevelStarting ||
						  (isLevelStarting && (chi.vd.iH_level < 1 || (chi.vd.iH_level > 0 && currentheader != chi && chi.nav_sb)) )
						  ) )
					  {
						  if (nav_sb == null)
							  nav_sb = ""
						  if (local_nav_sb == null)
							  local_nav_sb = new StringBuffer()
						  local_nav_sb << chi.nav_sb
						  chi.nav_sb = null
					  }
				  }
				  */

				  // if this is <level element and it's not one of same level vd children, then
				  // collect children data into
				  /*
				  if (chi.nav_sb && (!isLevelStarting ||
					  (isLevelStarting && (chi.vd.iH_level < 1 || (chi.vd.iH_level > 0 && currentheader != chi)) )
					  ))
					 */
									
				  if (chi.page_sb)
				  {
					  if (page_sb == null)
						  page_sb = new StringBuffer()
					  local_page_sb << chi.page_sb
					  chi.page_sb = null
				  }
			  }

			  setNavSbAandPageSb(local_nav_sb, local_page_sb)
		}

		/*
		 *
				  if (currentheader != chi)
				  {
					  if (isLevelStarting && chi.vd.iH_level < 1
						  && vd.text.toString().toLowerCase().startsWith("</level") && currentheader)
					  {
						  currentheader.setNavPoint((local_nav_sb ? local_nav_sb.toString() : ""))
						  local_nav_sb = new StringBuffer()
					  }
					  else
					  // if this is not  <level or <level element and it's not one of same level vd children, then
					  // collect children data into
					  // if (!isLevelStarting || (isLevelStarting && (chi.vd.iH_level < 1 || chi.vd.iH_level > 0 && currentheader != chi && chi.nav_sb))
					  if (chi.page_sb && (!isLevelStarting ||
						  (isLevelStarting && (chi.vd.iH_level < 1 || (chi.vd.iH_level > 0 && currentheader != chi && chi.nav_sb)) )
						  ) )
					  {
						  if (nav_sb == null)
							  nav_sb = ""
						  if (local_nav_sb == null)
							  local_nav_sb = new StringBuffer()
						  local_nav_sb << chi.nav_sb.toString().replaceAll("\$navPoint", "")
						  chi.nav_sb = null
					  }
				  }
		
		 */
		
		def private void setNavSbAandPageSb(local_nav_sb, local_page_sb)
		{
			// TODO: add possible string into sb
			// TODO: collect data
			// def list = [], tmp_chi, local_nav_sb = new StringBuffer ()
			// def local_page_sb = new StringBuffer ()
			
			if (!Lipsync2Daisy.isNCXItem(vd))
			{
				if (local_nav_sb && local_nav_sb.toString().length() > 0)
				{
					if (currentheader ==  null)
						nav_sb = local_nav_sb.toString().replaceAll("\$navPoint", "")
					else
					{
						if (currentheader)
						{
							if (currentheader.nav_sb && currentheader.nav_sb.toString().length() > 0)
							{
								nav_sb = currentheader.nav_sb.toString().replace("\$navPoint", local_nav_sb.toString().replaceAll("\$navPoint", ""))
							}
							else
								nav_sb = local_nav_sb.toString().replaceAll("\$navPoint", "")
						}
					}
				}
				else
				{
					if (currentheader && currentheader.nav_sb)
						nav_sb = currentheader.nav_sb.toString().replace("\$navPoint", "")
				}
				
				if (local_page_sb && local_page_sb.toString().length() > 0)
				{
					page_sb = local_page_sb.toString().replaceAll("\$pagePoint", "")
				}
				return
			}
				
			// if this vd contains header data, then add $navPoint again to later
			// possible replace when collecting haeader and sub-header nav_sb strings
			// after latest navPoint replace, removePossibleNavPoint() methods is called
			// to alla vds:
			def strNavPointReplaceAdd = ""
			if (vd.iH_level > 0)
				strNavPointReplaceAdd = "\$navPoint"
	
			/*
			if (this.vd.iPage > 0)			{
				println "test"
			}
			*/
				
			def tmp_nav_sb, tmp_page_sb
			def map = collectNCXPageString2(vd, vd.previous, null)
			if (map)
			{
				this.vd = map.get "vd"
				tmp_nav_sb = map.get "nav_sb"
				if (tmp_nav_sb == null)
				{
					if (local_nav_sb == null)
						nav_sb = null
					else
						nav_sb = local_nav_sb.toString().replace("\$navPoint", "" +strNavPointReplaceAdd);
				}
				else
				{
					if (local_nav_sb)
						nav_sb = tmp_nav_sb.toString().replace("\$navPoint", local_nav_sb.toString()  +strNavPointReplaceAdd);
					else
						nav_sb = tmp_nav_sb.toString().replace("\$navPoint", ""  +strNavPointReplaceAdd);
				}


				tmp_page_sb = map.get "page_sb"
				if (tmp_page_sb == null)
				{
					if (local_page_sb == null || !local_page_sb)
					{
						if (!page_sb)
							page_sb = null
					}
					else
					{
						if (page_sb)
							page_sb += local_page_sb.toString().replace("\$pagePoint", "");
						else
							page_sb = local_page_sb.toString().replace("\$pagePoint", "");
					}
				}
				else
				{
					if (local_page_sb)
					{
						if (page_sb)
							page_sb += tmp_page_sb.toString().replace("\$pagePoint", local_page_sb.toString());
						else
							page_sb = tmp_page_sb.toString().replace("\$pagePoint", local_page_sb.toString());
					}
					else
					{
						if (page_sb)
							page_sb += tmp_page_sb.toString().replace("\$pagePoint", "");
						else
							page_sb = tmp_page_sb.toString().replace("\$pagePoint", "");
					}
				}
			}
			else
			{
				if (local_nav_sb)
				{
					if (nav_sb)
						nav_sb += local_nav_sb.toString().replace("\$navPoint", ""  +strNavPointReplaceAdd)
					else
						nav_sb = local_nav_sb.toString().replace("\$navPoint", ""  +strNavPointReplaceAdd)
				}
				if (local_page_sb)
				{
					if (page_sb)
						page_sb += local_page_sb.toString().replace("\$pagePoint", "")
					else
						 page_sb = local_page_sb.toString().replace("\$pagePoint", "")
				}
			}
		}
		
		def void removePossibleNavPoint()
		{
			if (nav_sb && nav_sb.toString().contains("\$navPoint"))
				nav_sb = nav_sb.toString().replace("\$navPoint", "")
		}
		
		def int countitems()
		{
			iTest++
			// println "iTest=" +iTest
			
			if (children == null || children.size() == 0)
				return 1
			int count = 0
			for(item  in children)
			{
				count += item.countitems()
			}
			(count + children.size() +1)
		}
		
		/*
		def private getPageSb(local_page_sb)
		{
			// TODO: add possible string into sb
			// TODO: collect data
			// def list = [], tmp_chi, local_nav_sb = new StringBuffer ()
			// def local_page_sb = new StringBuffer ()
			
			if (!Lipsync2Smil.isNCXItem(vd))
				return null
				
			def tmp_nav_sb
			def map = collectNCXPageString2(this.vd, vd.previous, null)
			if (map)
				tmp_nav_sb = map.get "nav_sb"
		
			?*
			// map.put "prev_hlevel_vd", prev_hlevel_vd
			// map.put "page_sb", page_sb
			map.put "nav_sb", local_nav_sb.toString()
			// map.put "page_sb", local_page_sb.toString()
			// map.put "bPrev_HLevel_vd_higher_than_this_vd", bPrev_HLevel_vd_higher_than_this_vd
			map.put("VDTreeNode", vdtree)
			map
			*?
			tmp_nav_sb
		}
	*/
	}
	
	public List<String> getBookSubDirectories()
	{
		def list = [ getinputpathname(), getoutputpathname() ]
		list
	}
	
	public void createNewBookdirs(File fCopyToDir, List listSubDirs)
	throws Exception
	{
		if (!fCopyToDir && !fCopyToDir.exists())
			return;
		if (listSubDirs == null)
			return;
			
		File fnew
		for(String subdir in listSubDirs)
		{
			fnew = new File(fCopyToDir.absolutePath +File.separator +subdir)
			if (!fnew.exists())
				if (!fnew.mkdir())
				{
					throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_cannotcreatedir) +": " +fnew)
				}
		}
	}
	
	public void copyConfigFilesIntoBookdirs(File fCopyFrom, File fCopyToDir)
	throws Exception
	{
		File fCopyFromConfig = new File(fCopyFrom.absolutePath +File.separator +"config")
		if (!fCopyFromConfig.exists())
		{
			throw new Exception("Copy config directory does not exists! " +fCopyFromConfig)
		}
	
		// copy lanaugage files at first:
		for(File f2 in fCopyFrom.listFiles( { d, f-> f ==~ /.*\.properties/ } as FilenameFilter))
			new File(fCopyToDir.getAbsolutePath() +File.separator +f2.name).bytes = f2.bytes
		
		// create target config file if needed:
		File fconfig = new File(fCopyToDir.getAbsolutePath() +File.separator +"config")
		if (!fconfig.exists())
			if (!fconfig.mkdir())
			{
				throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_cannotcreatedir) +": " +fconfig)
			}
			
		// copy all config files at into target config directory:
		File fCopyFromConfigNewDir
		for(File f3 in fCopyFromConfig.listFiles())
		{
			if (f3.isFile())
				new File(fconfig.getAbsolutePath() +File.separator +f3.name).bytes = f3.bytes
			else
			{
				fCopyFromConfigNewDir = new File(fconfig.getAbsolutePath() +File.separator +f3.name)
				if (!fCopyFromConfigNewDir.exists() && !fCopyFromConfigNewDir.mkdir())
				{
					throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_cannotcreatedir) +": " +fCopyFromConfigNewDir)
				}
				
				for(File f4 in f3.listFiles())
				{
					new File(fCopyFromConfigNewDir.getAbsolutePath() +File.separator +f4.name).bytes = f4.bytes
				}
			}
		}
	}

	
	public void removeNeigthBornSentencesInPElements(int selvalue)
	{
		setBRemoveNeigthBornSentencesInPElements(false)
		setbRemoveNeigthBornSentencesInAllElements(false)
		if (selvalue == 1)
			setBRemoveNeigthBornSentencesInPElements(true)
		else
		if (selvalue == 2)
			setBRemoveNeigthBornSentencesInAllElements(true)
	}
	
	static {
		// define new static println method for this object:
		new_pritln_method = { Object args ->
			if (Lipsync2Daisy.currentxmlfilename != "" && !Lipsync2Daisy.currentxmlfilenameprinted && Lipsync2Daisy.currentxmlfilenameprinted != args.toString())
			{
				System.out.println Lipsync2Daisy.currentxmlfilename
				Lipsync2Daisy.currentxmlfilenameprinted = true
			}
			if (Lipsync2Daisy.currentxmlfilenameprinted != args.toString())
				System.out.println(args )
		}
	}
	def static void info(String msg)
	{
		LOGGER.info(msg)
	}
	def static void severe(String msg)
	{
		LOGGER.severe(msg)
	}
	def static void warning(String msg)
	{
		LOGGER.warning(msg)
	}

	def static void severe(Exception e)
	{
		def msg = exceptionStacktraceToString(e)
		LOGGER.severe(msg)
	}
	
	public static String exceptionStacktraceToString(Exception e)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		return baos.toString();
	}
	
	public void setBRemoveNeigthBornSentencesInPElements(boolean value)
	{
		bRemoveNeigthBornSentencesInPElements = value
	}

	public void setBRemoveNeigthBornSentencesInAllElements(boolean value)
	{
		bRemoveNeigthBornSentencesInAllElements = value
	}
	
	public boolean getBRemoveNeigthBornSentencesInAllElements()
	{
		bRemoveNeigthBornSentencesInAllElements
	}
	
	public boolean getBRemoveNeigthBornSentencesInPElements()
	{
		bRemoveNeigthBornSentencesInPElements
	}
	
	public String getUILanguage()
	{
		if (strUILanguage == null)
			readUserProperties()
		strUILanguage
	}
	
	public void setUILanguage(ivalue)
	{
		String value = null;
		switch(ivalue)
		{
			case "Finland":
			case 0:
				value = "Finland"
				break
			case "England":
			case 1:
				value = "England"
				break
			case "Sweden":
			case 2:
				value = "Sweden"
				break
			default:
				value = "England"
				break
		}
		strUILanguage = value
		if (/* bChanged && */ strUILanguage)
		{
			//loadi18Properties(this.m_fProp, this.m_locale)
			loadi18Resourcies(this.m_currentDir)
		}
	}
	
	public static List getListCustomTestAttributeNames()
	{
		listCustomTestAttributeNames		
	}
	
	public static int getPlayOrder()
	{
		return ++iPlayOrder
	}
	
	/*
	 static {
		 oldprintln = System.metaClass.getMetaMethod("println")
		 println "oldprintln: " +oldprintln.name
		 System.out.metaClass.println = { Object value ->
			 oldprintln.invoke "new printed called:"
			 if (!currentxmlfilenameprinted && scurrentxmlfilename)
				 oldprintln.invoke( scurrentxmlfilename)
			 return oldprintln.invoke( value )
		 }
	 }
	 */
	 
	 public Lipsync2Daisy()
	 {
		 strClassName = this.class.getName().replace(this.class.getPackage().getName(), "").substring(1)
		 strAppName = strClassName +" v. 1.0 (c) Celia & Tuomas Kassila (2015,2016)\n"
		 // meta set a new earlier defined println method for this object:
		 Object.metaClass.'static'.println = new_pritln_method // also classes VoiceData, VoiceDataFiel has same definion!
		 VoiceDataFile.metaClass.'static'.println = new_pritln_method
		 VoiceDataFile.register = this
		 VoiceDataFile.lipsync2Smil = this
		 VoiceData.register = this
		 
		 /* tka added 29.3.2015: initiale static fields: */
		 totalpage = 0
		 listNCXCustomTestElements = []
		 dtblevel = 1
		 maxpage = 0
		 ncx_total_pages = 0
		 dtbook_pagenum_on_off = "on"
		 bSentenceWordMode = true
		 totaltime = 0.0
		 old_totaltime = 0.0
		 iPlayOrder = 0
		 listCustomTestAttributeNames = []
		 hmTypeAttributeOfDtbook  = [:]
		 bWriteReadedDataAsText = false
		 listRemovedCustomTestAttributeNames = []
	 }
	 
	 public String saveCfgText(String fname, String text)
	 {
		 def matcher = text =~ /(?s)executetype\s*=\s*(daisy3uitext|daisy2uitext|2|3)\n/
		 if (!matcher.find())
		 {
			 def msg = getMessages().getString(Lipsync2Daisy.constUI_ui_wrong_executetype)
			 return msg
		 }
		 File f = new File(fname);
		 f.setText(text)
		 ""  // ok
	 }
 
	 /**
	  * This method is removing <meta name="track:...." .../> or <meta name="track:...."...>...</meta>
	  * from opt data before a file write.
	  */
	 public String removeMetaOptText(String text)
	 {
		 /*
		  * This method is tested with following data:
		  *
		  *  <meta name="track:Supplier"  content="Contentra"/>
 <meta test='ddd' name="track:SuppliedDate" content="2013-11-19"/>
 <meta name="track:Signum" content=""/>
 <meta name="track:Guidelines" content="2011-2" ddd>cat</meta>
		  */
		 String regEx = "(?s)<(meta|META)\\s+.*?(name|NAME)=('|\")track:.+?('|\")\\s*.*?(/>|(>.*?</(meta|META)>))";
		  Pattern pattern = Pattern.compile(regEx);
		 Matcher m = pattern.matcher(text);
	 
		 StringBuffer sb = new StringBuffer();
		 boolean founded = false
		 while (m.find()) {
		   m.appendReplacement(sb, "");
		   founded = true
		 }
		 if (!founded)
			 return text
			 
		 m.appendTail(sb);
		 sb.toString()
	 }
 
	 public String getTextOfFile(String fname)
	 {
		 File f = new File(fname)
		 if (!f.exists())
			 return null;
		 f.getText()
	 }
		 
	 def public void setStrSmilTemplateDir(String value)
	 {
		 if (value)
			 strSmilTemplateDir = value;
	 }
	 
	 def private String getSmilTemplateDir()
	 {
		 String ret = strSmilTemplateDir2
		 if (executetype == cnstExecuteDaisy3)
			 ret = strSmilTemplateDir3
		 return ret
	 }
	
	public void setConsole(fi.celia.app.smil2voicesmil.Console p_console)
	{
		this.console = p_console
	}
	
	def private String getCfgAbsolutePathOf(String fname)
	{
		getCfgAbsolutePathOf(executetype, fname)
	}

	/**
	 * possible call from Console to get absolute path of cfg file
	 * before copying a cfg content into a (new) another cfg file
	 * 
	 * @param fname
	 * @return
	 */
	def public String getConsoleAbsolutePathOfCfgFile(String fname)
	{
		getCfgAbsolutePathOf(fname)
	}
	
	def private String getCfgAbsolutePathOf(int iExecutetype, String fname)
	{
		if (fname != null && fname.contains(File.separator))
			return fname;
			
		if (iExecutetype == cnstExecuteDaisy3)
			return strLipsyncBaseCfgDir + File.separator + cnstExecuteDaisy3CfgSubDir +File.separator +fname
		if (iExecutetype == cnstExecuteDaisy2)
			return strLipsyncBaseCfgDir +File.separator + cnstExecuteDaisy2CfgSubDir +File.separator +fname
		return null
	}
	
	public void setErrorColorOff()
	{
		console.setErrorColorOff()
	}

	public void setErrorColorOn()
	{
		console.setErrorColorOn()
	}

	def void loadi18Properties(File fProp, Locale locale)
	{
		// if (m_locale == null)
		if (locale != null)
			m_locale = locale;
		// if (m_fProp == null)
		if (fProp != null)
			m_fProp = fProp;
		uiProp = new Properties()
		if (locale != null)
			currentLocale = locale
		 
		FileInputStream stream = new FileInputStream("lipsync2smil_" +currentLocale.getLanguage() +"_" +currentLocale.getCountry() +".properties");
		messages =  new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
		if (messages != null)
			static_messages = messages
		// messages = ResourceBundle.getBundle("lipsync2smil", currentLocale, new UTF8Control());
		uiProp.load(new StringReader(fProp.getText("UTF-8")))
		setUIValues()
	}
	
	def void setUIValues()
	{
		listComboStrings = []
		cnstUI_Combobox_daisy2uitext = messages.getString("daisy2uitext")
		cnstUI_Combobox_daisy3uitext = messages.getString("daisy3uitext")
		listComboStrings.add cnstUI_Combobox_daisy2uitext
		listComboStrings.add cnstUI_Combobox_daisy3uitext
		
		listSentenceComboStrings = []
		def value1 = messages.getString("ui_unmodifiedsentencies")
		def value2 = messages.getString("ui_modifiedneighboursentencies")
		def value3 = messages.getString("ui_allmodifiedneighboursentencies")		
		if (value1)
		listSentenceComboStrings.add value1
		if (value2)
			listSentenceComboStrings.add value2
		if (value3)
			listSentenceComboStrings.add value3			
	}
	
	public static File getCurrentDir() {
		return currentDir;
	}
	
	public static void setDefaultSize(int size) {
		
				Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
				Object[] keys = keySet.toArray(new Object[keySet.size()]);
		
				for (Object key : keys) {
		
					if (key != null && key.toString().toLowerCase().contains("font")) {
		
						System.out.println(key);
						Font font = UIManager.getDefaults().getFont(key);
						if (font != null) {
							font = font.deriveFont((float)size);
							UIManager.put(key, font);
						}
		
					}
		
				}
		
			}
	
	public static void setAddToDefaultSize(int size) {
		
				Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
				Object[] keys = keySet.toArray(new Object[keySet.size()]);
		
				for (Object key : keys) {
		
					if (key != null && key.toString().toLowerCase().contains("font")) {
		
						Font font = UIManager.getDefaults().getFont(key);
						if (font != null) {
							font = font.deriveFont((float)(font.getSize() +size));
							// System.out.print(key +" ");
							// System.out.println(font.getSize());
							UIManager.put(key, font);
						}						
					}
				}
				System.out.println("Has been set.");				
			}
	/**
	 * Ohjelman käynnistys. Komentoparametrit: ks usage() funtio.
	 * @param args
	 */
	public static void main(String [] args) 
	{
		// if screen resolution is high, you can add font sizes:
		File fProp = new File("lipsync2smil.properties")
		Properties sysprop = System.getenv().getProperties()
		def str_user_home = System.getProperty("user.home")
		if (str_user_home)
		{
			File fUserProp = new File(str_user_home +File.separator +"lipsync2smil.properties")
			if (fUserProp.exists())
			fProp = fUserProp
		} 
		
		if (fProp.exists())
		{
			FileReader fr = new FileReader(fProp)
			Properties prop = new Properties()
			prop.load(fr)
			def addIntoFontSizes = prop.getProperty("addintofontsizes", null)
			if (addIntoFontSizes)
			{
				try {
					int iaddIntoFontSizes = Integer.parseInt(addIntoFontSizes)
					if (iaddIntoFontSizes && iaddIntoFontSizes > 0)
					{
						println "Trying set add a number into font sizes: " +addIntoFontSizes						
						setAddToDefaultSize(iaddIntoFontSizes)
						def strDividerLocation = prop.getProperty("dividerlocation", null)
						if (strDividerLocation)
						{
							println "Trying set divider location size: " +strDividerLocation
							int iDividerLocation = Integer.parseInt(strDividerLocation)
							if (iDividerLocation && iDividerLocation > 0)
							{
								Console.iDividerLocation = iDividerLocation
								println "Has been set."
							}
						}
					}
				}catch(Exception e1){
					println "Error in add default font size: " +e1
				}
			}
		}
		
		System.setProperty("file.encoding", "UTF-8")
		//System.setOut(new PrintStream(System.out, true, "UTF-8"))
		System.setOut(new PrintStream(System.out, true, "CP1252"))
		
		Lipsync2Daisy smil2VoiceSmil = new Lipsync2Daisy()
		if (smil2VoiceSmil.executetype == null)
			smil2VoiceSmil.executetype = cnstExecuteDaisy3
			
		try {
			// def handlers = Logger.emptyHandlers()
			// for(hd in handlers)
				// if (hd)
					// Logger.removeHandler(hd)
			Lipsync2Daisy.LOGGER.setUseParentHandlers(false)
			// create a TXT formatter
		  	// SimpleFormatter formatterTxt = new SimpleFormatter();
			 // "MMM dd,yyyy HH:mm"
			def logfilename = getUserHome() +File.separator + "lipsync2smil.log"
			Handler fh = new FileHandler();
			CustomRecordFormatter formatter = new CustomRecordFormatter();
			// fh.setFormatter(formatterTxt);
			fh.setFormatter(formatter);
			Lipsync2Daisy.logfilename = logfilename
			Lipsync2Daisy.LOGGER.addHandler(fh);
			Lipsync2Daisy.LOGGER.setLevel(Level.FINEST);
		} catch (IOException e) {
			println e.getMessage()
			throw new RuntimeException("Problems with creating the log files");
		}
		  
		def File currentDir = new File(".")		
		Lipsync2Daisy.strLipsyncBaseCfgDir = currentDir.getAbsolutePath().toString().replace(".", "") +"config"
		Lipsync2Daisy.m_currentDir = currentDir.getAbsolutePath()		
		fi.celia.app.smil2voicesmil.Console console = new fi.celia.app.smil2voicesmil.Console(smil2VoiceSmil.strAppName, true, smil2VoiceSmil)
		console.setName(smil2VoiceSmil.strAppName);
		// console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		console.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		if (args.length ==  4 || args.length == 2|| args.length == 0)
		{
			smil2VoiceSmil.bGui = true
			smil2VoiceSmil.console = console
		}
		
		if (smil2VoiceSmil.bGui)
		{
			console.visible = true
			console.startReadThreads()
		}
		
		try {
			
			/*
			System.out.println(System.getProperty("file.encoding"));
			System.out.println(
			new java.io.OutputStreamWriter(
			new java.io.ByteArrayOutputStream()).getEncoding()
			);
			System.out.println(java.nio.charset.Charset.defaultCharset().name());
			println()
			println()
			*/
			
			String consoleEncoding = "Cp1252";
			// BufferedReader testin = new BufferedReader(new InputStreamReader(System.in, consoleEncoding))
		    // BufferedWriter testout = new BufferedWriter(new OutputStreamWriter(System.out, consoleEncoding));
			
			// fi.celia.app.smil2voicesmil.Console console = new fi.celia.app.smil2voicesmil.Console(smil2VoiceSmil.strAppName)
			console.setName(smil2VoiceSmil.strAppName);
			// console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			console.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			// console.visible = true
			
			smil2VoiceSmil.readcommandlineparameters(args)
			if (args.length > 3)
			{
				if (args.length > 4 )
				{
					smil2VoiceSmil.bGui = false
					
				}
				else
				{
				    console.setEditIntoFalse()
					try {
						Thread.sleep(500);
					} catch(Exception e){
						
					}
					if (smil2VoiceSmil.strLipsyncCfg)
						console.textCfg.setText(smil2VoiceSmil.strLipsyncCfg)
					console.textInputPath.setText(smil2VoiceSmil.strLipsyncDataDir)
					console.textOutputPath.setText(smil2VoiceSmil.strOutputDir)
					console.textCfgPath.setText(smil2VoiceSmil.strSmilTemplateDir)					 
					def value = (VoiceData.fTimeshift_into_voicedatas != null ? VoiceData.fTimeshift_into_voicedatas.toString() :"0.0")
					// console.correctTimeText.setEditable(true)
					console.correctTimeText.setText(value)
					// console.correctTimeText.setText("k1")
				}
				
				smil2VoiceSmil.generateDaisyFiles()
				
				/*
				if (!smil2VoiceSmil.readAllFiles())
				{
					println "readAllFiles() returned: false!"
				}
				else
				{
					smil2VoiceSmil.listNCXCustomTestElements = []
					if (smil2VoiceSmil.executetype == cnstExecuteDaisy2)
						smil2VoiceSmil.convertLipsync2SmilContentAfterDaisy2()
					else
					if (smil2VoiceSmil.executetype == cnstExecuteDaisy3)
					{
						VoiceData.cnstTime = ""
						VoiceData.clipStrinEndValue = ""
						Lipsync2Smil.ncx_total_pages = 0
						smil2VoiceSmil.convertLipsync2SmilContentAfterDaisy2()
						smil2VoiceSmil.convertLipsync2SmilContentAfterDaisy3()
					}
						
					println "\n===================="
					println smil2VoiceSmil.getMessages().getString(Lipsync2Smil.constUI_ui_conversion_ready) +"."
					println "====================\n"					
						
					smil2VoiceSmil.saveUserProperties()
				}
				*/
			}
			else
			{
				smil2VoiceSmil.readUserProperties()
				console.lenthMP3Button.selected = smil2VoiceSmil.bCalculateMP3FileLengths
				if (console.textCfg && smil2VoiceSmil.strLipsyncCfg)
					console.textCfg.setText(smil2VoiceSmil.strLipsyncCfg)
			}
			// console.visible = false
			
			console.pack();
			console.setVisible(true);
			if (!smil2VoiceSmil.bGui)
				System.exit(0)
		} catch(Exception e){
		    println e.getMessage()
			def strStackTrace = exceptionStacktraceToString(e)
			Lipsync2Daisy.severe(e.getMessage())
			Lipsync2Daisy.severe(strStackTrace)
		}
	}
	
	def Locale getLanguageLocale()
	{
		def locale = Locale.getDefault();
		if (strUILanguage)
		{
			switch(strUILanguage)
			{
				case "Finland" :
					locale = new Locale("fi","FI")
					break
				case "Englang" :
					locale = new Locale("en","EN")
					break
				case "Sweden" :
					locale = new Locale("sw","SW")
					break
				default:
					locale = new Locale("en","EN")
					break
			}
		}
		else
		{
			if (!locale)
			{
				locale = new Locale("fi","FI")
			}
			else
			{
				def lang = locale.country
				if (!(lang in ["Finland", "Englang", "Sweden"]))
					locale = new Locale("en","EN")
			}
		}
		locale
	}
	
	def public String getHelpText(String anotherfilename = null)
	{
		def localelang = locale.getLanguage()
		def File fHelp = new File(m_currentDir +File.separator +"lipsync2daisy_help_" +locale.getLanguage() +"_" +locale.getCountry() +".html")
		/*
		if (localelang == "en")
		{
			fHelp = new File(m_currentDir +File.separator +"makedoc" +File.separator +"Lipsync2daisy instructions_pics.html")
			if (!fHelp.exists())
				fHelp = new File("'" +fHelp.absolutePath +"'")
		}
		*/
		if (anotherfilename)
		{
			fHelp = new File(anotherfilename)
			//if (!fHelp.exists())
			//{
				fHelp = new File(anotherfilename.replaceAll("/", "\\\\"))
				if (!fHelp.exists())
					fHelp = new File("'" +fHelp.absolutePath +"'")
			//}
		}
		
		if (!fHelp.exists())
		{			
			return """
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">

<head>
</head>

<body>
<p/>
<p/>
<h1>Error in loading help file</h1>
<p>
<h3>Help text file is missing: ${fHelp.getAbsolutePath().replace('.'+File.separator, '') }</p>
<p>Add the missing file!</h3>
<p/>
</body></html>
"""
		}
		if (anotherfilename)
		{
			def ret = fHelp.getText()
			int ind = ret.indexOf("<body")
			if (ind == -1)
				ind = ret.indexOf("<BODY")
			if (ind > -1)
			{
				int ind2 = ret.indexOf(">", ind)
				if (ind2 > -1)
				{
					int indEnd = ret.indexOf("</body")
					if (indEnd == -1)
						indEnd = ret.indexOf("</BODY")
					if (indEnd > -1)
					{
						ret = ret.substring(ind2+1, indEnd)
						return ret
					}
				}
			}
			
			ret = ret.toString().replaceAll("(?s)<(body|BODY).*?>(.*?)</\\1>", "\$2")
			return ret
		}
		return fHelp.getText("UTF-8")
	}
	
	
	
	def void loadi18Resourcies(currentDir)
	{
		m_currentDir = currentDir
		if (strUILanguage == null)
			readUserProperties()
		def locale = getLanguageLocale()
		def File fProp = new File(m_currentDir +File.separator +"lipsync2smil_" +locale.getLanguage() +"_" +locale.getCountry() +".properties")
		if (!fProp.exists())
		{
			locale = new Locale("fi","FI")
			fProp = new File(m_currentDir +File.separator +"lipsync2smil_" +locale.getLanguage() +"_" +locale.getCountry() +".properties")
		}
		if (!fProp.exists())
		{
			println getMessages().getString(Lipsync2Daisy.constUI_ui_cannot_find_file) +fProp.getAbsolutePath()
			Thread.sleep(4000)
			System.exit 1
		}
		loadi18Properties(fProp, locale)
	}
	
	def void readUserProperties()
	{
		str_user_home = System.getProperty("user.home")
		if (str_user_home)
		{
			def File fsettings = new File(str_user_home +File.separator +strClassName +".properties")
			if (fsettings.exists())
				readUserProperties(fsettings)
		}
	}

	def static String getUserHome()
	{
		def ret = System.getProperty("user.home")
		ret
	}
	
	def static File getUserHomeFile(String fname)
	{
		def str_user_home = getUserHome()
		if (str_user_home)
		{
			def File f = new File(str_user_home +File.separator +fname)
			return f
		}
		File f = new File("." +File.separator +fname)
		f
	}
	
	def String [] getComboStrings()
	{
		return (String [])listComboStrings.toArray();	
	}
	
	def String [] getComboSentenceStrings()
	{
		return (String [])listSentenceComboStrings.toArray();
	}
	
	public Properties getPropertiesOf(String textCfg, String textInputPath, 
						String textOutputPath, String strSmilTemplateDir, 
						String executeTypeList, String correctTimeText, 
						boolean lenthMP3Button, String languageCombo)
	{
		Properties prop = new Properties()
		return prop
	}
	
	public Properties getUserFileProperty(int ind)
	{
		if (ind < 0 || ind >= JPanelEarlierSetups.maxbuttons)
			return null;
		File fprop = new File(this.getUserHome() +File.separator +cnst_lasrtarticlesofuser +(ind +1) +".properties")
		boolean bPrintMissingMsg = false;
		readFileProperties(fprop, bPrintMissingMsg)
	}
	
	def Properties readFileProperties(File fsettings, boolean bPrintMissingMsg = true)
	{
		if (!fsettings.isDirectory())
		{
			FileReader fr = null
			try {
				Properties usersettprop = new Properties ()
				if (!fsettings.exists())
				{
					if (bPrintMissingMsg)
						println getMessages().getString(Lipsync2Daisy.constUI_ui_cannot_find_file) +": " +fsettings
					return null
				}
				else
				{
					fr = new FileReader(fsettings)
					usersettprop.load(fr)
				}
				return usersettprop
			} catch(Exception e){
				return null;			
			}
		}
		null
	}
	
	def void readUserProperties(File fsettings)
	{
		if (!fsettings.isDirectory())
		{
			FileReader fr = null
			try {
				String propVariablePostExt = "";
				Properties usersettprop = new Properties ()
				if (!fsettings.exists())
				{
					println getMessages().getString(Lipsync2Daisy.constUI_ui_cannot_find_file) +": " +fsettings
					return
				}
				else
				{
					fr = new FileReader(fsettings)
					usersettprop.load(fr)
				}
				
				def tmp_strExecutetype = usersettprop.getProperty("executetype", null)
				if (tmp_strExecutetype)
				{
					if (tmp_strExecutetype == "3")
						propVariablePostExt = "3";
					this.setExecutetype(tmp_strExecutetype);
				}
				
				def tmp_strUILanguage = usersettprop.getProperty("uilanguage", null)
				if (tmp_strUILanguage)
				{
					strUILanguage = tmp_strUILanguage 
					if (this.console)
						this.console.setUILanguage(strUILanguage)
				}
				
				def tmp_strCalculateMP3Length = usersettprop.getProperty("calculatemp3filelength", null)
				if (tmp_strCalculateMP3Length)
				{
					bCalculateMP3FileLengths = false;
					if (tmp_strCalculateMP3Length == "true")
						bCalculateMP3FileLengths = true;
				}
				// comboStrings
					
				def strParTemplate = usersettprop.getProperty(cnst_locale_prop_field_cfgpath +propVariablePostExt, null)
				if (strParTemplate)
				{
					if (console?.textCfgPath)
						console.textCfgPath.setText(strParTemplate)
					strLipsyncBaseCfgDir = strParTemplate
					strParTemplate = getCfgAbsolutePathOf("")
				}
				def tmp_strLipsyncCfg = usersettprop.getProperty(cnst_locale_prop_field_cfgfile +propVariablePostExt, null)
				/*
				if (tmp_strLipsyncCfg && !tmp_strLipsyncCfg.contains(File.separator))
					strLipsyncCfg = getCfgAbsolutePathOf(tmp_strLipsyncCfg)
				else
				*/
				if (tmp_strLipsyncCfg)
					strLipsyncCfg = tmp_strLipsyncCfg
				if (strLipsyncCfg && console?.textCfg)
					console.textCfg.setText(strLipsyncCfg)
					
				int iComboSelectedIndex = (this.executetype -2);
				if (console /* console?.executeTypeList */ && iComboSelectedIndex > -1)
					console.executeTypeList.setSelectedIndex(iComboSelectedIndex);

				def tmp_fTimeshift_into_voicedatas = usersettprop.getProperty("fTimeshiftintovoicedatas", null)
				if (tmp_fTimeshift_into_voicedatas)
				{
					try {
						VoiceData.fTimeshift_into_voicedatas = Float.parseFloat(tmp_fTimeshift_into_voicedatas)
					} catch(Exception e){
					}
				}
				 
				def strLipsyncDataDir = usersettprop.getProperty("readdir" +propVariablePostExt, null)
				if (strLipsyncDataDir && console?.textInputPath)
					console.textInputPath.setText(strLipsyncDataDir)
				def strOutputDir = usersettprop.getProperty("outputdir" +propVariablePostExt, null)
				if (strOutputDir && console?.textOutputPath)
					console.textOutputPath.setText(strOutputDir)
				if (console?.correctTimeText)
					console.correctTimeText.setText((VoiceData.fTimeshift_into_voicedatas == null ? "0.0" : VoiceData.fTimeshift_into_voicedatas.toString()))
				
				def tmp_user_locale = usersettprop.getProperty("userlocale", null)
				if (tmp_user_locale)
				{
					def arrUserLocale = tmp_user_locale.split("_")
					if (arrUserLocale && arrUserLocale.size() > 1)
					{
						def lang = arrUserLocale[0].toString()
						def country = arrUserLocale[1].toString()
						if (lang && country)
						{
							this.currentLocale = new Locale(lang, country)
							if (console)
								this.setUIValues()
						}
					}
				}
				
				if (fr)
					fr.close()
			} catch(Exception e2){
				def msg = getMessages().getString(Lipsync2Daisy.constUI_ui_error)+ ": " +getMessages().getString(Lipsync2Daisy.constUI_ui_reading_cfgfile_of_user) +": " +fsettings
				println msg
				info msg
				println e2.getMessage()
				Lipsync2Daisy.severe(e2)
			}
		}
	}
	
	/**
	* Ohjelman käynnistysparametrit.
	*/
   def private void usage()
   {
	   System.err.println this.getClass().getName() +" lipsync2smil.cfg-polku lipsyncxml_hakemisto smiltemplate_hakemisto tuloshakemisto [daisy2|daisy3]"
	   System.err.println " - tuottaa sekä asetustiedon mukaan että lipsync-hakemiston mukaan tulostushakemstoon Daisy 2 tai 3 tiedostot."
	   System.err.println " - Jos parametri daisy2 tai daisy3 on annettu, ajetaan ilman graafista käyttöliittymää. Se määrää mitä tiedostoja tuotetaan."
	   System.err.println this.getClass().getName() +" lipsync2smil.cfg-path lipsyncxml_dir smiltemplate_dir outputdir [daisy2|daisy3]"
	   System.err.println " - generates after configuration file and Lipsync input directory Daisy 2 or 3 files into output directory."
	   System.err.println " - If a param daisy2 or daisy3 given, the application is executetd wihtout GUI. It defines also which kind of files will be generated."
	   if (!bGui)
	   		System.exit(1)
   }
   
   /**
   * Virheen sattuessa.
   *
   * @param msg
   */
  def private void error(String msg)
  {
	  System.err.println msg
	  System.exit(2)
  }
  
  def private void setCommandLineCorrectTimesOnOrOff(String value)
  {
  	  try {
		  if (!value || value.trim() == "0" || value.trim() == "0.0" || value.trim() == "0,0")
	  	     return
		  def parsedvalue = Double.parseDouble(value)
		  if (!parsedvalue.toString().endsWith(".0"))
		  {
			  println "Correct cfg correctlipsynctimes variable; error: variable value must to end: .0 or without desimals at all!: " +correctTimeText
			  System.exit(6)
		  }

		  VoiceData.fTimeshift_into_voicedatas = parsedvalue
	  } catch(Exception e){
	    info "command line cfg file: correctlipsynctimes: wrong value: " +e.getMessage()
	  	Lipsync2Daisy.severe(e)
		System.exit(6)
	  }
  }
  
  def private void setCommandLineMergelipsyncsentencies(String value)
  {
	  if (!value || value.trim() == "1")
	  	   return // default
	  if (value.trim() == "2")
	  	  removeNeigthBornSentencesInPElements(1)
	  else
	  if (value.trim() == "3")
	  	  removeNeigthBornSentencesInPElements(2)
	  else
	  	error("wrong command line cfg-file mergelipsyncsentencies value: " +value)
  }
  
  def private void setCommandLineXmldalidationonoff(String value)
  {
	  if (!value)
	  	   return // default
	  if(!(value.trim() in ["on","off"]))
	  	  error("wrong command line cfg-file xmldalidationonoff value: " +value)
	  if (value == "on")
	  	bXmlValidation = true
	  else
	  	bXmlValidation = false
  }
  
  def private void setCommandLineUsemp3lenghtonoff(String value)
  {
	  if (!value)
	  	return // default
	  if(!(value.trim() in ["on","off"]))
	  	 error("wrong command line cfg-file usemp3lenghtonoff value: " +value)
	  if (value == "on")
	  	bCalculateMP3FileLengths = true
	  else
	  	bCalculateMP3FileLengths = false
  }
  
  def private void readCommandlineExtraParameterFile(String extracommanlinecfgfilepath)
  throws Exception
  {
	  if (extracommanlinecfgfilepath == null)
	  	 throw new NullPointerException("extracommanlinecfgfilepath is null!")
	  if (!extracommanlinecfgfilepath)
	  	 throw new NullPointerException("extracommanlinecfgfilepath is empty!")
	  def File file = new File(extracommanlinecfgfilepath)
	  if (!file.exists())
		   throw new FileNotFoundException("Cannot find: " +file)
	  if (file.isDirectory())
		   throw new FileNotFoundException("parameter is a directory: " +file)
		   
	  Properties clProp = new Properties()
	  clProp.load(new StringReader(file.getText("UTF-8")))
	    
	  String tmp_correcttimes = clProp.getProperty("correctlipsynctimes", null)
	  if (tmp_correcttimes)
	  	setCommandLineCorrectTimesOnOrOff(tmp_correcttimes)
	  String tmp_mergelipsyncsentencies = clProp.getProperty("mergelipsyncsentencies", null)
	  if (tmp_mergelipsyncsentencies)
	  	 setCommandLineMergelipsyncsentencies(tmp_mergelipsyncsentencies)
	  String tmp_xmldalidationonoff = clProp.getProperty("xmldalidationonoff", null)
	  if (tmp_xmldalidationonoff)
	  	 setCommandLineXmldalidationonoff(tmp_xmldalidationonoff)
	  String tmp_usemp3lenghtonoff = clProp.getProperty("usemp3lenghtonoff", null)
	  if (tmp_usemp3lenghtonoff)
	  	 setCommandLineUsemp3lenghtonoff(tmp_usemp3lenghtonoff)

  }
  
  /**
  * Lue komentoparaemtrit ja cfg tiedosto.
  * Read command line parameters and cfg files.
  *
  * @param args
  * @return
  * @throws Exception
  */
 def private readcommandlineparameters(String [] args)
 throws Exception
 {
	 println strAppName
	 
	 println  this.getMessages().getString(Lipsync2Daisy.constUI_ui_handlingofparameters)
	 
	 if (!bGui)
	 println "Command line parameters: " +args

	 /*
	 if (!bGui && args.length < 1)
	 {
	  	usage()
		System.exit 1
	 }
	 else
	 */
	 if ((bGui && args.length != 0 && args.length != 2) && (args.length !=  4) && (args.length !=  5))
	 {
	  	usage()
		System.exit 1
	 }
	 	 
	 if (args.length > 0)
	 {
		if (!bGui && args.length == 0)
		{
			println "Done nothing!"
			if (!bGui)
				System.exit(2)
			return
		}
			
		strLipsyncCfg 	= args[0]
		// old: strSmilTemplateDir	= args[1]
		strLipsyncBaseCfgDir = args[1]
		if (args.length == 0 || (bGui && args.length == 2))
		{
			if (!bGui)
				System.exit(3)
			return
		}
			
		strLipsyncDataDir   = args[2]
		strOutputDir 		= args[3]
		VoiceDataFile.strOutputDir = strOutputDir
		
		if (args.length > 4) // has been called from command line
		{
			def executemode = args[4]
			if (!(executemode in ["daisy2", "daisy3"]))
			{
				println "Wrong comand line parameter: '" +executemode +"'"
				if (!bGui)
					System.exit(4)
				return
			}
			if (executemode == "daisy2")
			{
				this.executetype = cnstExecuteDaisy2
				// strSmilTemplateDir =				
			}
			else
			if (executemode == "daisy3")
			{
				this.executetype = cnstExecuteDaisy3
				// strSmilTemplateDir = 				
			}
			strSmilTemplateDir =strLipsyncBaseCfgDir
			
			if (args.length > 5)
			{
				readCommandlineExtraParameterFile(args[5])
			}
		}
				
		if (!bGui)
			println "Command line paramters are readed."
		// readAllFiles()
	}
 }
 
 def File getNewFile(String path, String filename, boolean bSkipExcistence = false)
 throws NullPointerException, FileNotFoundException
 {
	 if (filename == null)
	 	throw new NullPointerException("filename is null!")
	 def File f = new File(filename)
	 if (!bSkipExcistence && f.exists() && filename.contains(File.separator))
	 	return f
	 if (path == null)
	 	throw new NullPointerException("path is null!")
	 f = new File(path, filename)
	 if (f.exists())
	 	return f
	 throw new FileNotFoundException(getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +f)
 }
 
  def private boolean validateXml(String xml)
  throws Exception
  {		  
	  def File fText
	  try {
		  ValidationErrorHandler errorHandler = new ValidationErrorHandler()
		  println "\n====================================================="		  
		  println()
		  println "Xml parser validation:"
		  str_user_home = System.getProperty("user.home")
		  def validatexmlfilename = (str_user_home ? str_user_home +File.separator : "") +"lipsync_xml_validate.xml" 
		  fText = new File(validatexmlfilename)
		  // println "File: " +fText.absolutePath
		  if (fText.exists())
			 fText.delete()
			 
			 def FileWriter fw= new FileWriter(fText)
		try {
		  def final cnstXmlStart = "<\\?xml\\s+version\\s*=\\s*(\\\"|')1.0(\\\"|')\\s+encoding\\s*=\\s*(\\\"|')(UTF|utf)-8(\\\"|')\\s*\\?>"
		  def match = xml =~ /$cnstXmlStart/
		  def search = cnstXmlStart
		  if (!xml.contains('<!DOCTYPE') && !xml.contains('<!doctype')  /* && !xml.contains('<!DOCTYPE dtbook PUBLIC "-//NISO//DTD dtbook 2005-3//EN" "http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd">') */)
			  // xml = "" + xml.replace('<\\?xml\\sversion="1.0"\\sencoding="UTF-8"\\?>[\\s\t\r\n]*<dtbook', '<\\?xml\\sversion="1.0"\\sencoding="UTF-8"\\?>\n<!DOCTYPE dtbook PUBLIC "-//NISO//DTD dtbook 2005-3//EN" "http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd">\n<dtbook')
		  {
			  /*
			  int ind = xml.indexOf(search)
			  if (ind == -1)
			  {
				  search = cnstXmlStart.replace("\"", "'")
				  ind = xml.indexOf(search)
			  }
			  */
			  
			  def strAdd =	"""\n<!DOCTYPE dtbook PUBLIC "-//NISO//DTD dtbook 2005-3//EN" "http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd">\n"""
			  if (match.find())
		  	  	xml = "" + xml.substring(0, match.start() +match.end()) + strAdd + xml.substring(match.end())
              else
			    xml = cnstXmlStart +strAdd + xml.toString()								
		  }
		  
		  if (!xml.contains('<dtbook') && !xml.contains('<DTBOOK'))
		  {
			  match = xml =~ /(?<!(<dtbook\sxmlns=("|')http:\/\/www.daisy.org\/z3986\/2005\/dtbook\/("|')\sversion=("|')2005-3("|')\sxml:lang=("|')fi("|')>\s))<head>/
			  if (match.find())
			  	xml = xml.substring(0, match.start()) + "<dtbook xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\" version=\"2005-3\" xml:lang=\"fi\">" +xml.substring(match.start(), match.end()) + xml.substring(match.end())
		  } 
		  // take possible bad characters away:
		  /*		  
		  int ind = xml.indexOf(search)
		  if (ind > -1)
		  	xml = xml.indexOf(ind)
	      else
		  {
			  if (ind == -1)
			  {
				  search = cnstXmlStart.replace("\"", "'")
				  ind = xml.indexOf(search)
				  if (ind > -1)
				  	xml = xml.indexOf(ind)
			  }
		  }
		  */
			  
		  // fText.setText(xml)
		  fw.write(xml +"\n")
		  fw.close();
		  Thread.sleep(500)
		  fText = new File(validatexmlfilename)
		  def fname = fText.absolutePath
		  println "\nLipsync " +getMessages().getString(Lipsync2Daisy.constUI_ui_validation)+ " dtbook -" +getMessages().getString(Lipsync2Daisy.constUI_ui_file)+ ": " +fname +"\n"		  
		} catch(Exception e33){
			e33.printStackTrace()
		}	
		// fText = new File("lipsync_xml_validate.xml")
		  		  
	  // def data = new XmlParser().parseText(xml)
		  def validating = true        // default is false
		  def namespaceAware = false   // default is true
		  // def data = new XmlParser(validating, namespaceAware).parseText(xml) // sax parse
		  def parser = new XmlParser(validating, namespaceAware)
		  parser.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", false);
		  
		  parser.setEntityResolver(cr);
		  parser.setErrorHandler(errorHandler);
		  // def data = groovy.xml.DOMBuilder.parse(new StringReader(xml), validating, namespaceAware) //dom parser
		  // def data = parser.parse(new StringReader(xml)) //sax parser
		  def data = parser.parse(fText)
		  // def data = new XmlSlurper(validating, namespaceAware).parseText(xml)
		  
		  if (errorHandler.noWarnings && errorHandler.noExceptions && errorHandler.noFatalErrors)
		  		println "Xml parser: OK"
		  else
		  {
			  if (!errorHandler.noExceptions)
			  {
				 println "Xml errors:"				  
				 for (ex in errorHandler.listExceptions)
			  		println ex
			  }
			  if (!errorHandler.noFatalErrors)
			  {
				 println "Xml no fatal errors:"
				 for (fe in errorHandler.listFatalErrors)
			  		println fe
			  }
			  if (!errorHandler.noWarnings)
			  {
				 println "Xml warnings:"
				 for (w in errorHandler.listWarnings)
			  		println w
			  }
		  }
		  // println()
		  println "\n====================================================="
		  println()
		  
		  return true
	  } catch (Exception e){
	  	Lipsync2Daisy.severe(e)		  
	  	throw e
	  }
	  return false
  }
  
  def static public parseXml(String xml, boolean bValidate)
  throws Exception
  {
	  try {
		  ValidationErrorHandler errorHandler = new ValidationErrorHandler()
		  println()
		  println "Xml parser validation:"
		  def File fText = new File("lipsync_xml_validate.xml")
		  if (fText.exists())
			 fText.delete()
		  fText << xml
		  println "\nLipsync dtbook xml " + static_messages.getString(Lipsync2Daisy.constUI_ui_file) + " : " +fText.absolutePath +"\n"
		  
	  // def data = new XmlParser().parseText(xml)
		  def namespaceAware = false   // default is true
		  // def data = new XmlParser(validating, namespaceAware).parseText(xml) // sax parse
		  def parser = new XmlParser(bValidate, namespaceAware)
		  parser.setEntityResolver(cr);		  
		  parser.setErrorHandler(errorHandler);
		  // def data = groovy.xml.DOMBuilder.parse(new StringReader(xml), validating, namespaceAware) //dom parser
		  def data = parser.parse(new StringReader(xml)) //sax parser
		  // def data = new XmlSlurper(validating, namespaceAware).parseText(xml)
		  
		  if (errorHandler.noWarnings && errorHandler.noExceptions && errorHandler.noFatalErrors)
				  println "Xml parser: OK"
		  else
		  {
			  if (!errorHandler.noExceptions)
			  {
				 for (ex in errorHandler.listExceptions)
					  println ex
			  }
			  if (!errorHandler.noFatalErrors)
			  {
				 for (fe in errorHandler.listFatalErrors)
					  println fe
			  }
			  if (!errorHandler.noWarnings)
			  {
				 for (w in errorHandler.listWarnings)
					  println w
			  }
		  }
		  println()
		  return data
	  } catch (Exception e){
	  	   Lipsync2Daisy.severe(e)
		   throw e
	  }
	  return null
  }
  
  def public List<String> getSortedFileNameList(String [] listBaseFileNames, String [] removeFromEnds)
  {
	  def ret = listBaseFileNames.collect{
		  for(String ext in removeFromEnds)
		  {
			  if (it.toString().contains(("." +ext)))
			  {
				  try  {
					  return (it.toString() - ("." +ext)).toInteger()
				  }catch(Exception e)
				  {
					  return it.toString() - ("." +ext)
				  }			
			  }
		 }
		 return it.toString()
	  }.sort()
	  List<String> retnew = new ArrayList<String>();
	  for(v in ret)
	  {
		  for(String ext in removeFromEnds)
		  {
			  if (v.toString().endsWith(("." +ext)))
			  	retnew.add v.toString()
			  else
			  	retnew.add v.toString() +("." +ext)
		  }
	  } 
	  return retnew
  }
  
  /**
   * Lue tiedostot, cfg tiedosto +.xml:t.
   * @throws Exception
   */
  def private boolean readAllFiles()
  throws Exception
  {	  
	  // println strAppName
	  def bSkipExcistence = true	  
	  
	  iPlayOrder = 0
	  println getMessages().getString(Lipsync2Daisy.constUI_ui_defcharacterset)+": " +System.getProperty("file.encoding")	 
	  // System.console().println "console: " +System.getProperty("client.encoding.override")
	  
	  if (bGui)
	  {
		  println getMessages().getString(Lipsync2Daisy.constUI_ui_closeappafterconv)
		  println()
	  }
	  	  
	  if (!strLipsyncCfg)
	  {
		  println getMessages().getString(Lipsync2Daisy.constUI_ui_confpath_is_missing)
		  if (!this.bGui)
		  	usage()
		  return false
	  }
	  
	  // fLipsyncCfg 	= getNewFile(strSmilTemplateDir, strLipsyncCfg)
	  fLipsyncCfg 	= new File(getCfgAbsolutePathOf(strLipsyncCfg))
	  if (!fLipsyncCfg.exists())
	  {
		  def msg = getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " + fLipsyncCfg.absolutePath
		  if (!this.bGui)
		  		usage()
		  throw new Exception(msg)
		  // return false
	  }
	  else
	  if (fLipsyncCfg.isDirectory())
	  {
		  System.err.println getMessages().getString(Lipsync2Daisy.constUI_ui_is_not_file) +" (lipsync2smil.cfg): " + fLipsyncCfg.absolutePath
		  if (!this.bGui)
		  	usage()
		  return false
	  }

	  if (!strLipsyncDataDir)
	  {
		  println getMessages().getString(Lipsync2Daisy.constUI_ui_input_data_is_missing)
		  if (!this.bGui)
		  	usage()
		  return false
	  }
	  
	  fLipsyncDataDir 		= new File(strLipsyncDataDir)
	  if (!fLipsyncDataDir.exists())
	  {
		  System.err.println getMessages().getString(Lipsync2Daisy.constUI_ui_dirnotexists) +": " + fLipsyncDataDir.absolutePath
		  if (!this.bGui)
		  	usage()
		  return false
	  }
	  else
	  if (!fLipsyncDataDir.isDirectory())
	  {
		  System.err.println getMessages().getString(Lipsync2Daisy.constUI_ui_is_not_dir) +" (lipsync xml-" +getMessages().getString(Lipsync2Daisy.constUI_ui_dir)+ "): " + fLipsyncDataDir.absolutePath
		  if (!this.bGui)
		  	usage()
		  return false
	  }

	  if (!strSmilTemplateDir)
	  {
		  println getMessages().getString(Lipsync2Daisy.constUI_ui_templatedir_is_missing)
		  if (!this.bGui)
		  	usage()
		  return false
	  }
	  
	  fSmilTemplateDir 	= new File(strSmilTemplateDir)
	  if (!fSmilTemplateDir.exists())
	  {
		  System.err.println getMessages().getString(Lipsync2Daisy.constUI_ui_dirnotexists)+": " + fSmilTemplateDir.absolutePath
		  if (!this.bGui)
		  	usage()
		  return false
	  }
	  else
	  if (!fSmilTemplateDir.isDirectory())
	  {
		  System.err.println getMessages().getString(Lipsync2Daisy.constUI_ui_is_not_dir)+" (smil-template-" +getMessages().getString(Lipsync2Daisy.constUI_ui_dir) + "): " + fSmilTemplateDir.absolutePath		  
		  if (!this.bGui)
		  	usage()
		  return false
	  }

	  if (!strOutputDir)
	  {
		  println getMessages().getString(Lipsync2Daisy.constUI_ui_inputdir_is_missing)
		  if (!this.bGui)
		  	usage()
		  return false
	  }

	  fOutputDir = new File(strOutputDir)
	  if (!fOutputDir.exists())
	  {
		  System.err.println getMessages().getString(Lipsync2Daisy.constUI_ui_dirnotexists) +": " + fOutputDir.absolutePath
		  if (!this.bGui)
		  	usage()
		  return false
	  }
	  else
	  if (!fOutputDir.isDirectory())
	  {
		  System.err.println getMessages().getString(Lipsync2Daisy.constUI_ui_is_not_dir) +" (" +getMessages().getString(Lipsync2Daisy.constUI_ui_outputdir) +"): " + fOutputDir.absolutePath
		  if (!this.bGui)
		  	usage()	
		  return false
	  }
	  
	  // lue cfg tiedosto
	  def ind = -1, iRivi = 0
	  String before, after
	  File fSmil, fXml, fExistingXml
	  CfgFilePair cfgFilePair
	  
	  VoiceDataFile vdf
	  String basename
	  
	  println getMessages().getString(Lipsync2Daisy.constUI_ui_reading_cfg_and_input_files)+":"
	  println getMessages().getString(Lipsync2Daisy.constUI_ui_logfile) + ": " +logfilename	  
	  
	  xmlVoiceDataFiles = [:]
	  def fname 
	  listVDFs = []
	  
	  daisyIDs = new DaisyIDs()
	  VoiceDataFile.daisyids = daisyIDs
	  
	  def lipsyncFiles = []
  // println listVDFs
	   
	  Properties prop = new Properties ()
	  File fProp = getNewFile(getCfgAbsolutePathOf(""), strLipsyncCfg)
	  println getMessages().getString(Lipsync2Daisy.constUI_ui_reading_cfgs) +": " +fProp
	  def proptext = fProp.getText()
	  if (!proptext.contains("\\\\"))
		proptext = proptext.replaceAll("\\\\", "\\\\\\\\")
	  prop.load(new StringReader(proptext))
	  
	  smilbasefilename = "speechgen"
	  
	  def tmp_smilbasefilename = prop.getProperty("smilbasefilename", null)
	  if (tmp_smilbasefilename)
	  {
		  smilbasefilename = tmp_smilbasefilename
	  }

	  println "Cfg variable smilbasefilename: " +smilbasefilename

	  prop3 = null
	  	  
	  m_boldetcxmlelementsinmodifiedneighboursentencies = prop.getProperty("boldetcxmlelementsinmodifiedneighboursentencies", null)
	  
	  // if (!bGui) // tka 7.1.13
	  // { // tka 7.1.13
	  if (!executetype)
	  {
		  String strExecuteType = prop.getProperty("executetype", null)
		  if (strExecuteType)
		  {
			  this.setExecutetype(strExecuteType)
		  }
		  else
				 throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +": executetype!")
  		}
	   // tka 7.1.13 }

	  strCustomTestAttributeNames = prop.getProperty("customtest_elements")
	  if (strCustomTestAttributeNames)
	  {
		  for(String value in strCustomTestAttributeNames.split(" "))
		  {
			  if (value)
				  listCustomTestAttributeNames.add value.toLowerCase()
		  }
	  }

	  strTypeAttributeOfDtbooks = prop.getProperty("typeattributeOfdtbook")
	  if (strTypeAttributeOfDtbooks)
	  {
		  if (!strTypeAttributeOfDtbooks.contains("|"))
		  {
				  def arrValue = strTypeAttributeOfDtbooks.split(",")
				def key = arrValue[0]
				def strvalue = arrValue[1]
				hmTypeAttributeOfDtbook.put key.toLowerCase(), strvalue.toLowerCase()
		  }
		  else
		  {
			  def arrTypeAttributeOfDtbooks = strTypeAttributeOfDtbooks.split("\\|")
			  for(String value in arrTypeAttributeOfDtbooks)
			  {
				  if (value)
				  {
					  def arrValue = value.split(",")
					  def key = arrValue[0]
					  def strvalue = arrValue[1]
					  hmTypeAttributeOfDtbook.put key.toLowerCase(), strvalue.toLowerCase()
				  }
			  }
		  }
	  }


	  templatefiles				= prop.getProperty("templatefiles", null)
	  strDtbookoutputfilename	= prop.getProperty("dtbookoutputfilename", null)
	  // if (executetype == this.cnstExecuteDaisy3 && !strDtbookoutputfilename)
	  	 // throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_dtbookoutputfilenamehasemptyvalue))
			      
	  if (!strDtbookoutputfilename)
	  	 strDtbookoutputfilename = dtbookfilename
		   
	  if (executetype == this.cnstExecuteDaisy3)
	  println "Cfg variable dtbookoutputfilename: " +strDtbookoutputfilename
	  
	  def strXmlfiles = prop.getProperty(cnst_locale_prop_field_xmlfles, null)
	  def notfounded = false
	  if (strXmlfiles)
	  {
		  xmlfiles = []
		  File fcheck
		  int dialogResult = -5
		  strXmlfiles.split(",").each { f ->
			  if (notfounded)
			  	  return
			  fcheck = new File(fLipsyncDataDir.getAbsolutePath(), f.trim())
			  if (!fcheck.exists())
			  {
				  
				  println getMessages().getString(Lipsync2Daisy.constUI_ui_missgininputfile) +": " +fcheck.getCanonicalPath()
				  dialogResult = JOptionPane.showConfirmDialog (console,
					  getMessages().getString(Lipsync2Daisy.constUI_ui_missgininputfile) +": " +fcheck.getCanonicalPath() +"\n" +getMessages().getString(Lipsync2Daisy.constUI_ui_areinputfilesreadedautomatic), getMessages().getString(Lipsync2Daisy.constUI_ui_ui_warning), JOptionPane.OK_OPTION);				  
				  notfounded = true
				  return
			  }
			  xmlfiles.add f
		  }
		  if (dialogResult != -5 && dialogResult != JOptionPane.OK_OPTION)
		  {
		  	 notfounded = false
			 println ""
			 return false
		  }
	  }
	  else
	  	notfounded = true
	  
	  if (notfounded)
	  { // no user defined xmlfiles, seek from read dir:
		  def p = ~/\d+\.xml/
		  def tmp_fXmlFiles = []
		  fLipsyncDataDir.eachFileMatch(p)
		  {
			  tmp_fXmlFiles.add it
		  }
		  
		  if (!tmp_fXmlFiles)
		  	throw new Exception("No files to read in directory: " +fLipsyncDataDir)
		  def fnamea, fnameb			  
		  tmp_fXmlFiles = tmp_fXmlFiles.sort { a, b ->
			    def aName = a.getName().toString().replace(".xml","")
				def bName = b.getName().toString().replace(".xml","")
			    def n1 = (aName =~ /\d+/)[-1] as Integer
			    def n2 = (bName =~ /\d+/)[-1] as Integer
			    def s1 = aName.replaceAll(/\d+$/, '').trim()
			    def s2 = bName.replaceAll(/\d+$/, '').trim()
			
			    if (s1 == s2){
			        return n1 <=> n2
			    }
			    else{
			        return s1 <=> s2
			    }
		  }*.name
	  /*
			 try { 
				 fnamea = a.getName()
				 fnameb = b.getName()
				 (fnamea.replace("\\.xml", "") as int) <=> (fnameb.replace("\\.xml", "") as int)  
			 } catch(Exception ca) 
			 { 
				 (fnamea <=> fnameb)
			 }	
		  }
	 */
		  xmlfiles = tmp_fXmlFiles
	  }

	  String strDoctitle_on_off	= prop.getProperty("doctitle_on_off", null)
	  doctitle_on_off 			= Boolean.valueOf(strDoctitle_on_off)
	  def tmp_doctitle			= prop.getProperty("doctitle", null)
	  if (tmp_doctitle)
	  {
	  	doctitle = tmp_doctitle
	  }

	  def tmp_dc_title 			= prop.getProperty("dctitle", null)
	  if (tmp_dc_title)
	  {
		  dc_title = tmp_dc_title
	  }
	  
	  String strSmil2cfgfile 
	  String strSmil3cfgfile
	  
	  strSmilTemplateDir3 = null
	  if (executetype == this.cnstExecuteDaisy3)
	  {
		  if (!strSmilTemplateDir3)
		  	strSmilTemplateDir3 = strSmilTemplateDir +File.separator +cnstExecuteDaisy3CfgSubDir		  		  
		  strSmilTemplateDir2 = strSmilTemplateDir +File.separator +cnstExecuteDaisy2CfgSubDir  // prop.getProperty("smil2templatedir", null)
		  if (!strSmilTemplateDir2)
		  		throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" smild2templatedir " +getMessages().getString(Lipsync2Daisy.constUI_ui_variable)+", " +getMessages().getString(Lipsync2Daisy.constUI_ui_configfile)+": " +strSmilTemplateDir2)
		  if (!strSmilTemplateDir3 && prop.getProperty("smil3templatedir", null))		  		  
		  	  strSmilTemplateDir3 = prop.getProperty("smil3templatedir", null)
		  if (!strSmilTemplateDir3)
			throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" smil3templatedir " +getMessages().getString(Lipsync2Daisy.constUI_ui_variable)+", " +getMessages().getString(Lipsync2Daisy.constUI_ui_configfile)+": " +strSmilTemplateDir3)
			
		  if (!strSmil2cfgfile) // muutetaan vain jos ei ennestään arvoa
		  	  strSmil2cfgfile = prop.getProperty("smil2cfgfile", null)				
		  if (!strSmil2cfgfile)
		  	  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" smil2cfgfile " +getMessages().getString(Lipsync2Daisy.constUI_ui_variable)+", " +getMessages().getString(Lipsync2Daisy.constUI_ui_configfile)+": " +fLipsyncCfg)
		  strSmil2cfgfile = getNewFile(strSmilTemplateDir2, strSmil2cfgfile).getAbsolutePath()		  

		  // if (!strSmil3cfgfile) // muutetaan vain jos ei ennestään arvoa
		  	// strSmil3cfgfile = prop.getProperty("smil3cfgfile", null)
		  
		  strSmil3cfgfile = strLipsyncCfg
		  if (!strSmil3cfgfile)
		  	throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" smil3cfgfile " +getMessages().getString(Lipsync2Daisy.constUI_ui_variable)+", " +getMessages().getString(Lipsync2Daisy.constUI_ui_configfile)+": " +fLipsyncCfg)
		  strSmil3cfgfile = getNewFile(strSmilTemplateDir3, strSmil3cfgfile).getAbsolutePath()

		  if (prop.getProperty("dtbook.smilref.templatefile", null))
		  	 dtbooksmilreftemplatefile = prop.getProperty("dtbook.smilref.templatefile", null)
		  if (!dtbooksmilreftemplatefile)
			throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" dtbook.smilref.templatefile: " +fLipsyncCfg)

		  if (prop.getProperty("dtbook.listAddDaisy3SmillRef", null))
		  {
			def tmp = prop.getProperty("dtbook.listAddDaisy3SmillRef", null)
			if (tmp)
				listAddDaisy3SmillRef = tmp.split(" ") as List
		  }
		  if (listAddDaisy3SmillRef.isEmpty())
		  	throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" dtbook.listAddDaisy3SmillRef: " +fLipsyncCfg)

		  def tmp
		  if (prop.getProperty("dtbook.listNoDaisy3SmillRefIfManySentencies", null))
		  {
			tmp = prop.getProperty("dtbook.listNoDaisy3SmillRefIfManySentencies", null)
			if (tmp && !tmp.equals("\"\""))
				listNoDaisy3SmillRefIfManySentencies = tmp.split(" ") as List
			// if (listNoDaisy3SmillRefIfManySentencies.isEmpty())
			  // throw new Exception(getMessages().getString(Lipsync2Smil.constUI_ui_missingcfgvalue) +" dtbook.listNoDaisy3SmillRefIfManySentencies: " +fLipsyncCfg)
		  }
		  else
		  	  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" dtbook.listNoDaisy3SmillRefIfManySentencies: " +fLipsyncCfg)
				
		  tmp = prop.getProperty("dtbook.pagenum_onoff", null)
		  if (!tmp)
			  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" dtbook.pagenum_onoff: " +tmp)			  
		  if (tmp)
		  {
		  	Lipsync2Daisy.dtbook_pagenum_on_off = tmp
			if (!(Lipsync2Daisy.dtbook_pagenum_on_off in ["on", "off"]))
				throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" dtbook.pagenum_onoff (right value: on or off): " +tmp)
		  }		  
			
		  tmp = prop.getProperty("speechgen_smil_meta_tmpl_file_name", null)
		  if (!tmp)
			  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" speechgen_smil_meta_tmpl_file_name: " +tmp)
		  if (tmp)
		  {
			  speechgen_smil_meta_tmpl_file_name = tmp
		  }
		  		  
		  tmp = prop.getProperty("speechgen_ncx_meta_tmpl_file_name", null)
		  if (!tmp)
			  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" speechgen_ncx_meta_tmpl_file_name: " +tmp)
		  if (tmp)
		  {
			  speechgen_ncx_meta_tmpl_file_name = tmp
		  }
		  
		  tmp = prop.getProperty("bookStructfile", null)
		  if (!tmp)
			  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" bookStructfile: " +tmp)
		  if (tmp) 
		  {
			  /* luetaan ncx:n bookstruct arvot,
			   jolloin tiedetään mitkä customattributes bookstruct attribute arvot pitää lisätä */			   
			  bookStructfilename = tmp
			  def fBStruct =  new File(strSmilTemplateDir3 +File.separator +bookStructfilename)
			  if (!fBStruct.exists())
			  	throw new Exception("Cfg " +getMessages().getString(Lipsync2Daisy.constUI_ui_file)+" bookStructfile " +getMessages().getString(Lipsync2Daisy.constUI_ui_missing) +": " +fBStruct)
			  def arrBSRows = fBStruct.getText().split("\n")			  
			  def key, value2, arrBSVar
			  hmBookStruct = [:]
			  
			  for(String var in arrBSRows)
			  {
				  if (var == null || !var.contains("="))
				  	  continue
				  var = var.toString().replaceAll("[\n\r]", "")
				  arrBSVar = var.split("=")
				  key      = arrBSVar[0]
				  value2    = arrBSVar[1]
				  hmBookStruct.put(key, value2)
			  }
		  }

		  prop3 = prop 
		  prop  = new Properties()
		  File fProp2 = getNewFile(strSmilTemplateDir, strSmil2cfgfile)
		  proptext = fProp2.getText() // lisätään c:\path\file \ merkit seuraavasti: c:\\path\\file  
		  if (!proptext.contains("\\\\"))
				proptext = proptext.replaceAll("\\\\", "\\\\\\\\")
		  prop.load(new StringReader(proptext))		  		  
	  }
	  
	  if (prop3 || executetype == this.cnstExecuteDaisy2) // luetaan myös daisy 3 ajossa daisy2:n asetukset
	  { // pohjaksi daisy3:n ajolle		

		  if (prop3 == null)
		  {
			  String tmp_movespanpage_elementnames = prop.getProperty("movespanpage_elementnames", null)
			  if (tmp_movespanpage_elementnames)
			  {
				  def arraySpanE = tmp_movespanpage_elementnames.split(",")
				  for(ename in arraySpanE)
				  {
					  movespanpage_elementnames.add ename.trim()
				  }
			  }
		  }
		  if (!dc_title)  		 		  
		  	dc_title 					= prop.getProperty("dc_title", null)
		  dc_authrows 				= prop.getProperty("dc_authrows", null)
		  if (dc_authrows)
		  		dc_authrows = dc_authrows.toString()
		  dc_date					= prop.getProperty("dc_date", null)
		  if (!dc_date)
		  	dc_date = ""
		  // pages 					= prop.getProperty("pages", null)
		  if (!pages)
		  	  pages = ""
		  dc_identifier				= prop.getProperty("dc_identifier", null)
		  region_id					= prop.getProperty("region_id", null)
		  doctitle_par_id			= prop.getProperty("doctitle_par_id", null)
		  doctitle_text_id			= prop.getProperty("doctitle_text_id", null)
		  doctitle_audio_src_id		= prop.getProperty("doctitle_audio_src_id", null)
		  doctitle_content_id		= prop.getProperty("doctitle_content_id", null)
		  par_id_base				= prop.getProperty("par_id_base", null)
		  audio_id_base				= prop.getProperty("audio_id_base", null)
		  text_id_base				= prop.getProperty("text_id_base", null)
		  content_id_base			= prop.getProperty("content_id_base", null)
		  
		  smil_tmpl_file_name		= prop.getProperty("smil_tmpl_file_name", null)
		  // println "koe: " +prop.getProperty("smil_meta_tmpl_file", null)
		  // println "koe2: " +prop.hasProperty("smil_meta_tmpl_file")
		  
		  if (prop3)
		  	smil_meta_tmpl_file_name	= prop3.getProperty("speechgen_smil_meta_tmpl_file_name", null)
		  par_tmpl_file_name		= prop.getProperty("par_tmpl_file_name", null)
		  ncc_tmpl_file_name		= prop.getProperty("ncc_tmpl_file_name", null)
		  h1_tmpl_file_name			= prop.getProperty("h1_tmpl_file_name", null)
		  def tmp_content_file_name	= prop.getProperty("content_file_name", null)

		  content_file_name = "content.html"
		  ncc_file_name = "ncc.html"

		  if (tmp_content_file_name)
		  	  content_file_name = tmp_content_file_name
		  else
		  if (executetype == this.cnstExecuteDaisy3)
		  {
			  content_file_name = "" 
		  }
		  def tmp_ncc_file_name		= prop.getProperty("ncc_file_name", null)
		  if (tmp_ncc_file_name)
		  	  ncc_file_name = tmp_ncc_file_name
		  else
		  if (executetype == this.cnstExecuteDaisy3)
		  {
			  ncc_file_name = "" 
		  }

		  // xhtmlDoctype				= pr.getProperty("xhtmlDoctype", null)
		  contenttype				= prop.getProperty("contenttype", null)
		  seq_tmpl_file_name		= prop.getProperty("seq_tmpl_file_name", null)
		  content_tmpl_file_name	= prop.getProperty("content_tmpl_file_name", null)
		  def strPage_lipsync_time_on_off = prop.getProperty("page_lipsync_time_on_off", null)	  
		  if (strPage_lipsync_time_on_off.toString().toLowerCase() == "off")
		  		page_lipsync_time_on_off = false
		  else
		  if (strPage_lipsync_time_on_off.toString().toLowerCase() == "on")
		  	 page_lipsync_time_on_off = true
		  else
		  	 throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_wrongvalue) +": " +strPage_lipsync_time_on_off +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_invariable) +" (.cfg): page_lipsync_time_on_off!")
	    if (executetype == this.cnstExecuteDaisy3 && prop3)
		{
			if (!this.donotAddTheseFilesIntoOPF)
			{  		 		  
				String tmp_doNotAddTheseFilesIntoOPF = prop3.getProperty("doNotAddTheseFilesIntoOPF", null)
			    if (tmp_doNotAddTheseFilesIntoOPF)
			    {
					String arrdoNotAddTheseFilesIntoOPF = tmp_doNotAddTheseFilesIntoOPF.split(",")
					def listAdd = []
				    if (arrdoNotAddTheseFilesIntoOPF && arrdoNotAddTheseFilesIntoOPF.length() > 0)
	                {
						for(dnafname in arrdoNotAddTheseFilesIntoOPF)
						{
							listAdd.add dnafname 	
						}
						donotAddTheseFilesIntoOPF = listAdd
					 }
				}
		  }

			strPage_lipsync_time_on_off = prop3.getProperty("page_lipsync_time_on_off", null)
			if (strPage_lipsync_time_on_off.toString().toLowerCase() == "off")
					page_lipsync_time_on_off = false
			else
			if (strPage_lipsync_time_on_off.toString().toLowerCase() == "on")
				 page_lipsync_time_on_off = true
			else
				 throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_wrongvalue) +": " +strPage_lipsync_time_on_off +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_invariable) +" page_lipsync_time_on_off!")		  
		}
	    VoiceData.page_lipsync_time_on_off = page_lipsync_time_on_off
			   			
		timeshift_into_voicedatas	= prop.getProperty("timeshift_into_voicedatas", null)
	    if (timeshift_into_voicedatas && !bGui)
		{
			def fValue = Float.parseFloat(timeshift_into_voicedatas)
			VoiceData.fTimeshift_into_voicedatas = fValue
		} 
		
		change_xmlelements_startendtimes_between_wordelements = false
		def strchange_xmlelements_startendtimes_between_wordelements = prop.getProperty("change_xmlelements_startendtimes_between_wordelements", null)
		if (!strchange_xmlelements_startendtimes_between_wordelements) // wrong writen prop.variable name for older .cfg files:
 			strchange_xmlelements_startendtimes_between_wordelements = prop.getProperty("change_xmlelements_startendtimes_between_wordelments", null)
		if (strchange_xmlelements_startendtimes_between_wordelements.toString().toLowerCase() == "off")
			change_xmlelements_startendtimes_between_wordelements = false
		else
		if (strchange_xmlelements_startendtimes_between_wordelements.toString().toLowerCase() == "on")
			change_xmlelements_startendtimes_between_wordelements = true
		else
			 throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" " +strchange_xmlelements_startendtimes_between_wordelements +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_invariable) +" change_xmlelements_startendtimes_between_wordelements!")
			
		if (executetype == this.cnstExecuteDaisy3 && prop3)
		{
			def strRemovedCustomTestAttributeNames = null
			if (prop3)
				 strRemovedCustomTestAttributeNames = prop3.getProperty("customtest_elements_removed_from_head_element")
			if (strRemovedCustomTestAttributeNames)
			{
				for(String value in strRemovedCustomTestAttributeNames.split(" "))
				{
					if (value)
						listRemovedCustomTestAttributeNames.add value.toLowerCase()
				}
			}
	  
			 strPage_lipsync_time_on_off = prop3.getProperty("page_lipsync_time_on_off", null)
			 if (strPage_lipsync_time_on_off.toString().toLowerCase() == "off")
				page_lipsync_time_on_off = false
			 else
			 if (strPage_lipsync_time_on_off.toString().toLowerCase() == "on")
				page_lipsync_time_on_off = true
			 else
				  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_missingcfgvalue) +" " +strPage_lipsync_time_on_off +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_invariable) +" page_lipsync_time_on_off!")
		}
	
    	 // TODO: TEE dtbookelements_file_name    
		/*
		 * 
		  dtbookelements_file_name  = prop.getProperty("dtbookelements_filename", "")
		  if (!dtbookelements_file_name)
				  throw new Exception("dtbookelements_file_name " +getMessages().getString(Lipsync2Smil.constUI_ui_empty) +". "+getMessages().getString(Lipsync2Smil.constUI_ui_putdtbookelements_file_name)+"!")
		  else
		  {
			  fDtbookelements_file = new File(dtbookelements_file_name)
			if (!fDtbookelements_file)
				throw new Exception(getMessages().getString(Lipsync2Smil.constUI_ui_file) +": " +fDtbookelements_file +getMessages().getString(Lipsync2Smil.constUI_ui_missing) +"!!")
			strfDtbookelements = fDtbookelements_file.getText()
			if (!strfDtbookelements)
				throw new Exception(getMessages().getString(Lipsync2Smil.constUI_ui_file2) +": " +fDtbookelements_file +" " +getMessages().getString(Lipsync2Smil.constUI_ui_content)+ " " +getMessages().getString(Lipsync2Smil.constUI_ui_missing) +"!!")
			def arrDtbooks = strfDtbookelements.split("\n")
			if (!arrDtbooks)
				throw new Exception(getMessages().getString(Lipsync2Smil.constUI_ui_file2) +": " +fDtbookelements_file +" " +getMessages().getString(Lipsync2Smil.constUI_ui_content)+ "väärä!!")
			def values, dtbookelement, dtbookvalue
			def hmDtbookElements = [:]
			int iRow = 1
			for(String row in arrDtbooks)
			{
				values=row.trim().split("==")
				int i = 1
				dtbookvalue = ""
				dtbookelement = null
				for(String onevalue in values)
				{
					if (i == 1)
						dtbookelement = onevalue
					else
					if (i == 2)
						dtbookvalue = onevalue
					else
						throw new Exception(getMessages().getString(Lipsync2Smil.constUI_ui_file2) +": " +fDtbookelements_file +" rivillä: " +iRow +" väärä arvo: '" +row +"'")
					i++
				}
				iRow++
				hmDtbookElements.put(dtbookelement, dtbookvalue)
			}
			VoiceDataFile.hmXhtmls = hmDtbookElements
		  }
		  */
	
		  println "Luetaan template tiedostoja:"
		  
		  File fSmilTemplate = new File(getSmilTemplateDir() +File.separator + smil_tmpl_file_name)
		  if (smil_tmpl_file_name)
		  {
		  	  if (!fSmilTemplate.exists())
				  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_file3) +": " + fSmilTemplate + " ei ole!")
			  
			  strSmilTemplate = fSmilTemplate.getText()
		  } 

		  if (smil_meta_tmpl_file_name)
		  {
			  File fSmilMetaTemplate = new File(strSmilTemplateDir3 +File.separator + smil_meta_tmpl_file_name)
			  if (!fSmilMetaTemplate.exists())
				  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_file3) +": " + fSmilMetaTemplate + " ei ole!")
			  
			  strSmilMetaTemplate = fSmilMetaTemplate.getText()
		  }

		  if (par_tmpl_file_name)
		  {
			  File fParTemplate = new File(getSmilTemplateDir() +File.separator + par_tmpl_file_name)
			  if (!fParTemplate.exists())
		  		  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_file3) +": " + fParTemplate + " ei ole!")
			  
			  strParTemplate = fParTemplate.getText()
		  }
	
		  if (seq_tmpl_file_name)
		  {
		  	 File fSeqTemplate = new File(getSmilTemplateDir() +File.separator + seq_tmpl_file_name)
			 if (!fSeqTemplate.exists())
			 	 throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_file3) +": " + fSeqTemplate + " ei ole!")
			  
			  strSeqTemplate = fSeqTemplate.getText()
			  strSeqTemplate2 = strSeqTemplate
		      if (executetype == this.cnstExecuteDaisy3)
			  {
				fSeqTemplate = new File(strSmilTemplateDir2 +File.separator + seq_tmpl_file_name)
				if (!fSeqTemplate.exists())
			 		 throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_file3) +": " + fSeqTemplate + " ei ole!")
			    strSeqTemplate2 = fSeqTemplate.getText()
			  } 
		  }	  
		  	  
		  if (ncc_tmpl_file_name)
		  {
			  File fNccTemplate = new File(getSmilTemplateDir() +File.separator + ncc_tmpl_file_name)
			  if (!fNccTemplate.exists())
			  	throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_file3) +": " + fNccTemplate + " ei ole!")
			  
			  strNccTemplate = fNccTemplate.getText()
		  }
		  
		  	  
		  if (h1_tmpl_file_name)
		  {
			  File fH1Template = new File(getSmilTemplateDir() +File.separator + h1_tmpl_file_name)
			  if (!fH1Template.exists())
			  	throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_file3) +": " + fH1Template + " ei ole!")
			  
			  strH1Template = fH1Template.getText()
		  }
	
		  if (content_tmpl_file_name)
		  {
			  File fContentTemplate = new File(getSmilTemplateDir() +File.separator + content_tmpl_file_name)
			  if (!fContentTemplate.exists())
			  	throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_file3) +": " + fContentTemplate + " ei ole!")
			  
			  strContentTemplate = fContentTemplate.getText()
			  if (!strContentTemplate)
		  	 	throw new Exception("strContentTemplate: is null!")
		  }
		  
		  
	    /*
		  println "Reading rows of config file:"
	
		  fLipsyncCfg.eachLine { line ->
			  iRivi++
			  if (!line)
				  return
			  line = line.trim()
			  if (line.startsWith("#"))
				  return
			  ind = line.indexOf('=')
			  if (ind == -1)
			  {
				  error("cfg-" +getMessages().getString(Lipsync2Smil.constUI_ui_row) +" " + iRivi +": = " +getMessages().getString(Lipsync2Smil.constUI_ui_character) +" " +getMessages().getString(Lipsync2Smil.constUI_ui_missing) +"!")
			  }
			  if (0 == ind)
				  error("cfg-" +getMessages().getString(Lipsync2Smil.constUI_ui_row) +" " + iRivi +": = " +getMessages().getString(Lipsync2Smil.constUI_ui_character) +" on mutta sen edestä puttuu .smil tiedoston nimi !")
			  if(line.size() == ind +1)
				  error("cfg-" +getMessages().getString(Lipsync2Smil.constUI_ui_row) +" " + iRivi +": = " +getMessages().getString(Lipsync2Smil.constUI_ui_character) +" on mutta sen lopusta puuttuu xml tiedoston nimi !")
				  
			  before 	= line.substring(0, ind).trim()
			  if(!before.toLowerCase().endsWith(".smil"))
				  error("cfg-rivi " + iRivi +": .smil puuttuu tiedoston nimestä !")
	
			  after 	= line.substring(ind+1).trim()
			  if(!after.toLowerCase().endsWith(".xml"))
				  error("cfg-rivi " + iRivi +": .xml puuttuu tiedoston nimestä !")
				  
			  fSmil = new File(fSmilTemplateDir.absolutePath +File.separator +before)
			  if (!fSmil.exists())
				  error("cfg-rivi " + iRivi +": "+ fSmil + " tiedostoa ei ole olemassa!")
			  fExistingXml = xmlFiles.get(after)
			  if (fExistingXml)
				  fXml = fExistingXml
			  else
			  {
				  fXml = new File(fLipsyncDataDir.absolutePath +File.separator +after)
				  if (!fXml.exists())
					  error("cfg-rivi " + iRivi +": "+ fXml + " tiedostoa ei ole olemassa!")
				  xmlFiles.put after, fXml
			  }
			  cfgFilePair = new CfgFilePair ()
			  cfgFilePair.fSmil = fSmil
			  cfgFilePair.fXml = fXml
			  if (fExistingXml)
				  vdf = existingVoiceDataFiles.get(after)
			  else
			  {
				  vdf = new VoiceDataFile()
				  vdf.file = cfgFilePair.fXml
				  ind = before.lastIndexOf('.')
				  if (ind > -1)
					  basename = before.substring(0, ind)
				  else
					  basename = before
				  vdf.basename = basename
	  
				  pages += vdf.loadData()
				  existingVoiceDataFiles.put after, vdf
			  }
			  // cfgFilePair.voicedatafile = vdf
			  // cfgFilePairs.put(before, cfgFilePair)
		  }
		  
		  println "Luettu asetustiedoston käsiteltävät tiedostonimet."
			  */
		  
		  
		  // fLipsyncDataDir.eachFile { file ->
		  def file, fpath = fLipsyncDataDir.getAbsolutePath()
		 // xmlfiles = xmlfiles.reverse()
		  def bMissingFile = false, listScannedXmlFiles = []
		  def fname2
		  def bDubbelFile = false
		  
		  lipsyncFiles = []
		  
		  xmlfiles.each { filename ->
			  fname2 = filename.toString().trim()
			  file = new File(fpath +File.separator + fname2)
			  if (!file.exists())
			  {
				  println getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +file
				  bMissingFile = true
				  return
			  }		  
			  if (file.isDirectory())
			  {
				  println "Readfile is a directory: " +file
				  bMissingFile = true
				  return
			  }		 
			  fname = file.getName()
			  // if (!fname.toLowerCase().endsWith(".xml"))
				 // return
			  lipsyncFiles.add file
			  if (listScannedXmlFiles.contains(fname))
			  {
				  println getMessages().getString(Lipsync2Daisy.constUI_ui_input2filesincfgfile) +": " +fname
				  bDubbelFile = true
				  return
			  }
	
			  listScannedXmlFiles.add fname
		  }
		  
		  if (lipsyncFiles.size() == 0)
		  {
			  println  getMessages().getString(Lipsync2Daisy.constUI_ui_missinginputfiles) +"!"
			  usage()
			  return false
		  }
		  else
		  if (bMissingFile)
		  {
			  println getMessages().getString(Lipsync2Daisy.constUI_ui_missinginputfiles) +"!"
			  usage()
			  return false
		  }
		  if (bDubbelFile)
		  {
			  println getMessages().getString(Lipsync2Daisy.constUI_ui_severalsameinputfile) +"!"
			  usage()
			  return false
		  }
		  
	      println "\n" +getMessages().getString(Lipsync2Daisy.constUI_ui_readinglipsyncinputfiles) +":\n"
		  def VoiceData tmp_title
		  def tmp_authors
		  // old_totaltime = 0.0
		  		  
		  // tarkistetaan lipsync xml tiedostot xml parserilla
		  // generate a dtbook xml from lipsync xml data and check that data with xml parser:
		  
		  validateXmlRows = [:]
		  arrayXmlRows = []
		  StringBuffer sbXml = new StringBuffer ()
		  StringBuffer sbNcxXml = new StringBuffer ()
 		  lipsyncFiles.each { File file2 ->
			  if (file2.isDirectory())
				  return
			  fname = file2.getName()
			  vdf = new VoiceDataFile()
			  vdf.file = file2
			  vdf.correctXml()			  
			  arrayXmlRows.addAll vdf.loadXml()
		  }
		  
		if (bXmlValidation && (executetype == cnstExecuteDaisy3 || executetype == cnstExecuteDaisy2))
        {   
		  for(xmlrow1 in arrayXmlRows)
		  {		
			  validateXmlRows.put(xmlrow1.iLine, xmlrow1)
			  sbXml << xmlrow1.xmlrow +"\n"
		  }

		  	/*
		  def fTest = new File("koe4.txt")
		  if (fTest.exists())
		  	fTest.delete()
		  fTest.setText(sbXml.toString())
		  */

		  LipsyncXmlRow xmlRow
		  try {
			  if (!validateXml(sbXml.toString()))
				  return false
		  } catch(Exception e){
		  	  def fText = new File(getUserHome() +File.separator + "lipsync_xml_error_in_validate.xml")
			  if (fText.exists())
		  		 fText.delete()
			  fText << sbXml.toString()
			  println "\n" +getMessages().getString(Lipsync2Daisy.constUI_ui_lipsyncinclipboard) +": " +fText
		  	  def msg = e.getMessage()
			  def stacktrace = this.stackTraceToString(e)
			  StringSelection data = new StringSelection(sbXml.toString());
			  Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			  clipboard.setContents(data, data);
			  def lineNumber = null
			  if (!(e instanceof SAXParseException))
			  {
	   			  println getMessages().getString(Lipsync2Daisy.constUI_ui_xmlerrormissinglinenumber)
		 		  println msg
			  }
			  else
			  if (e instanceof SAXParseException)
			  {
	  			 // xmlRow = validateXmlRows.get(lineNumber)
				 lineNumber = e.getLineNumber();
				 xmlRow = arrayXmlRows[lineNumber]
				 if (!lineNumber)
				 {
					println getMessages().getString(Lipsync2Daisy.constUI_ui_wrong) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_index) +": " +lineNumber +"! " +getMessages().getString(Lipsync2Daisy.constUI_ui_xmlerrormissinglinenumber)
					println msg
					return
				}
			 }

			println "\n" +getMessages().getString(Lipsync2Daisy.constUI_ui_lipsyncvalidationerror) +"!:"
			println msg
			println getMessages().getString(Lipsync2Daisy.constUI_ui_file) +": " +xmlRow.file 
			println "Lipsync " +getMessages().getString(Lipsync2Daisy.constUI_ui_row) + ": " +xmlRow.iLine
			println "Lipsync " +getMessages().getString(Lipsync2Daisy.constUI_ui_rowdata) +": " +xmlRow.lipsyncrow
			println "Readed xml " +getMessages().getString(Lipsync2Daisy.constUI_ui_file) +": " +fText
			println "Readed xml " +getMessages().getString(Lipsync2Daisy.constUI_ui_file) +" "+ getMessages().getString(Lipsync2Daisy.constUI_ui_row) +": " +lineNumber
			println "Readed xml " +getMessages().getString(Lipsync2Daisy.constUI_ui_file) +" "+ getMessages().getString(Lipsync2Daisy.constUI_ui_column)+ ": " +columnNumber +"\n"					
			return
			  }			    
		  }
		  
		  def prev_pages = 0
		  // fLipsyncDataDir.eachFile { file ->	  
		  lipsyncFiles.each { File file2 ->
			  if (file2.isDirectory())
				  return
			  fname = file2.getName()
			  if (!fname.toLowerCase().endsWith(".xml"))
				  return
			  vdf = new VoiceDataFile()
			  vdf.file = file2
			  ind = fname.lastIndexOf('.')
			  if (ind > -1)
				  basename = fname.substring(0, ind)
			  else
				  basename = fname
			  vdf.basename = basename
		  	  prev_pages = pages
			  pages = vdf.loadData()
			  if (prev_pages && prev_pages != pages && pages == 0)
			  	pages = prev_pages 

			  if (executetype == cnstExecuteDaisy3)
			  	vdf.setDaisy3SentenceBegins()
			  if (((double)vdf.totaltime) > Lipsync2Daisy.totaltime)
			  	Lipsync2Daisy.totaltime = (double)vdf.totaltime
			  if (Lipsync2Daisy.maxpage < vdf.totalpage)
			  	Lipsync2Daisy.maxpage = vdf.totalpage
			  if (Lipsync2Daisy.totalpage < vdf.totalpage)
			  	Lipsync2Daisy.totalpage = vdf.totalpage
			  // totaltime += vdf.totaltime()
			  // old_totaltime += vdf.old_totaltime
			  xmlVoiceDataFiles.put(fname, vdf)
			  listVDFs.add vdf
			  tmp_title = vdf.getVoiceDataTitle()
			  if (tmp_title)
			  	vdTitle = tmp_title
			  tmp_authors = vdf.getVoiceDataAuthors()
			  if (tmp_authors)
			  	listAuthors = tmp_authors
		  }
		    
		  // TODO: toteuta on-off doctilelle:
		  if (doctitle_on_off)
		  {
			  for (VoiceDataFile vf in listVDFs)
		  	  {
				 if (vf.setVoiceDataIntoDocTitle())
				 {
				 	break
				 }
			  }
		  }
		  
		  VoiceData vd, vdPrev, vdFirst
		  def newVdList = []

		  // sijoitetaan seuraavan filen alkuajaksi edellisen loppuaika:
		  for (VoiceDataFile vf in listVDFs)
		  {
			  newVdList = []
			  if (vf.listitems && vf.listitems.size() > 0)
			  {
			  	vd = (VoiceData)vf.listitems.get(vf.listitems.size()-1)
				vdFirst = (VoiceData)vf.listitems.get(0)
				// vdFirst.start = 0.0
				// vf.listitems.set(0, vdFirst)
				if (vdPrev && vdFirst && !vdFirst.isStartTimeEndTimeSet)
				{
					vdFirst.start = vdPrev.end
					vdFirst.isStartTimeEndTimeSet = true
					int iCnt = 0
					newVdList = []
					for(VoiceData vdd in vf.listitems)
					{
						iCnt++
						if (iCnt == 1)
							newVdList.add vdFirst
						else
							newVdList.add vdd
					}
					vf.listitems = newVdList
				}
				if (vd /* && !vd.isStartTimeEndTimeSet */)
				{
					vdPrev = vd
				}
			  }
			  else
			  	vdPrev = null
		  
		  }

	  // sijoitetaan edellisen filen loppuajaksi seuraavan alkuaika:		
	  int iItems = listVDFs.size(), iMaxItems = listVDFs.size()
	  for (VoiceDataFile vf in listVDFs.reverse())
	  {
		  newVdList = []
		  if (vf.listitems && vf.listitems.size() > 0)
		  {
		  	vd = (VoiceData)vf.listitems.get(vf.listitems.size()-1)
			vdFirst = (VoiceData)vf.listitems.get(0)
			if (iItems != iMaxItems && vdPrev && vd && !vd.isStartTimeEndTimeSet)
			{
				vd.end = vdPrev.start
				vd.isStartTimeEndTimeSet = true
				int iCnt = 0
				int iMax = vf.listitems.size()
				newVdList = []
				for(VoiceData vdd in vf.listitems)
				{
					iCnt++
					if (iCnt == iMax)
						newVdList.add vd
					else
						newVdList.add vdd
				}
				vf.listitems = newVdList
			}
			if (vdFirst /* && !vd.isStartTimeEndTimeSet */)
			{
				vdPrev = vdFirst
			}
		  }
		  else
		  	vdPrev = null	  
		  iItems--
		  }
	  }     
	  
/*
	  // sijoitetaan alkavan tiedoston 1. puheajaksi 0.0		
	  int iItems = 0, iMaxItems = listVDFs.size()
	  def newVdList = [], newVFList = []
	  int iCnt = 0
	  def bFirstWordVD = false
	  for (VoiceDataFile vf in listVDFs)
	  {
		  if (vf.listitems && vf.listitems.size() > 0)
		  {
			  bFirstWordVD = true
			  for(VoiceData vd in vf.listitems)
			  {				
				iCnt++
				if (bFirstWordVD)
					vd.start = 0.0
				if (vd.name == VoiceData.cnstLipsyncWord)
					bFirstWordVD = false
				newVdList.add vd
			}
			vf.listitems = newVdList
		  }
	  	  newVFList.add vf
	  }
	  listVDFs = newVFList
      */    
 
	  if (executetype == this.cnstExecuteDaisy3)
	  {
		 // strSmilTemplateDir = strSmilTemplateDir3
		 str_css_file = prop3.getProperty("css_file")
		 if (!str_css_file)
				  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_cfgvariablemusthavevalue) +": css_file")
		 strDoctitle_on_off			= prop3.getProperty("doctitle_on_off", null)
		 doctitle_on_off 			= Boolean.valueOf(strDoctitle_on_off)
		 doctitle					= prop3.getProperty("doctitle", null)
		 dc_title 					= prop3.getProperty("dc_title", null)
		 
		 def strPage_lipsync_time_on_off = prop3.getProperty("page_lipsync_time_on_off", null)
		 if (strPage_lipsync_time_on_off.toString().toLowerCase() == "off")
			page_lipsync_time_on_off = false
		 else
		 if (strPage_lipsync_time_on_off.toString().toLowerCase() == "on")
			page_lipsync_time_on_off = true
		 else
			  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_wrong) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_value) +": " +strPage_lipsync_time_on_off +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_invariable)+" (.cfg): page_lipsync_time_on_off!")

		 if (strDtbookoutputfilename)
		 	this.dtbookfilename = strDtbookoutputfilename

		 readSmil3TemplateFiles(strSmilTemplateDir3)			
	  }
	  
	  return true	
  }
  
  def private void readSmil3TemplateFiles(strSmilTemplateDir3)
  {
	  // strSmilTemplateDir3 prop3
	  // TODO
  }
  
  /**
   * Tämä methodi generoi daisy3 .opf tiedoston sisältöineen
   * 
   * @param filename
   * @param bDeleteFile
   */
  def private void generateOptFileAfterTemplate(String filename, boolean bDeleteFile)
  {
  	 println "\ngenerateOptFileAfterTemplate(" +filename +")\n"
	   
	 // TODO: <meta content="2:57:57.065" name="dtb:totalTime"/>
	   
	 String strOutFileName = filename.replace(".tmpl", "")
	 int ind = strOutFileName.lastIndexOf('.')
	 String strFileNameExt
	 if (ind > -1)
	 	strFileNameExt = strOutFileName.substring(ind +1)
	 else
	 	throw new Exception("strFileNameExt is null!")

	 String strTemplate = readFromDaisy3TemplateDirFile(filename)
	 String strMediatype = prop3.getProperty(strFileNameExt)
	 if (!strMediatype)
	 	 throw new Exception(strFileNameExt +" is null!")
	 /*
	  	h1_id = register.getNextNccId()
					item.generateIdValues()
					def h_level = (item.iH_level < 1 ? 1 :item.iH_level)
					binding = ["h_level" : h_level, "h1_id":  'd' +h1_id, "css_class": "  class=\"page-normal\" ",
						"content_file_name" : smil_file_name,
						 "contentid": item.text_id,
						 "content_text": item.text ]
					strH1 = h1Template.make(binding).toString()
					
	  */
	 def manifest_items 		= getManifestItems(strOutFileName, strMediatype)
	 def spine_refs     		= getSpinerefs()
	 def speechgen_opf_meta		= getspeechgen_opf_meta_for_opf()
	 strTemplate = strTemplate.replace("\$speechgen_opf_meta", speechgen_opf_meta)
	 strTemplate = strTemplate.replace("\$manifest_items", manifest_items)
	  
	 strTemplate = strTemplate.replace("\$totalTime", VoiceData.getClipTime(Lipsync2Daisy.totaltime))
	 strTemplate = strTemplate.replace("\$doctitle", doctitle)
	 strTemplate = strTemplate.replace("\$spine_refs", spine_refs)
	 strTemplate = removeMetaOptText(strTemplate)
	 writeIntoOutputDirFile(strOutFileName, strTemplate, bDeleteFile)
  }
  
  /**
   * opf daisy 3 tiedoston osatekstien generointi
   * 
   * @param strOutFileName
   * @param strMediatype
   * @return
   */
  def String getManifestItems(strOutFileName, strMediatype)
  {
	  String filename = prop3.getProperty("manifest_items")
	  String strItem_tmpl = readFromDaisy3TemplateDirFile(filename)
	  StringBuffer sb = new StringBuffer ()
	  strMediatype = prop3.getProperty("smil")
	  String tmp
	  int iCnter = 1
	  for(VoiceDataFile vdf in listVDFs)
	  {
		    tmp = strItem_tmpl.replace("\$href", vdf.smil_file_name)
			tmp = tmp.replace("\$id", cnstOPF_SmilPrefix +iCnter++)
			tmp = tmp.replace("\$mediatype", strMediatype)
	  		sb << "\t\t\t" +tmp +"\n"			   
	  }
	  
	  /*
	  String ncdId = "ncx"
	  String strNcxMediatype = prop3.getProperty(ncdId)
	  sb << strItem_tmpl.replace("\$href", "speechgen.ncx").replace("\$id", ncdId).replace("\$mediatype", strNcxMediatype) +"\n"
	  */
	 
	  // hae kuvatiedostot tuloshakemistosta	  
	  String strPictureExts = prop3.getProperty("picture_exts")
	  if (!strPictureExts)
	  		throw new Exception("cfg picture_exts " +getMessages().getString(Lipsync2Daisy.constUI_ui_empty) +"!")
	  def extlist = strPictureExts.split(" ")
	
	  String strpicture_media_types = prop3.getProperty("picture_media_types")
	  if (!strpicture_media_types)
			  throw new Exception("cfg picture_media_types " +getMessages().getString(Lipsync2Daisy.constUI_ui_missinginputfiles) +"!")
	  def picturemedialist = strpicture_media_types.split(" ")
	
	  String filename2 = prop3.getProperty("manifest_items")
	  strItem_tmpl = readFromDaisy3TemplateDirFile(filename2)
	  String strPictureFileName
	  int iMediatypeInd = 0	  
	  File fOutPutDir = new File(strOutputDir)
	  boolean opf_file_added = false
	  def String fname
	  
	  for(File f in fOutPutDir.listFiles())
	  {
		iMediatypeInd = 0
	  	for(String ext in extlist)
		  {
			  fname = f.getName().toLowerCase()
			  if (fname.endsWith("." +ext))
			  {
		
			  	sb << "\t\t\t" +strItem_tmpl.replace("\$href", f.getName()).replace("\$id", cnstOPF_Prefix +iCnter++).replace("\$mediatype", "image/" +picturemedialist[iMediatypeInd]) +"\n"
				iMediatypeInd++
			  }
		  }
	  }

	  String strAudioExts = prop3.getProperty("audio_exts")
	  if (!strAudioExts)
			  throw new Exception("audio_exts is null!")
	  def audiolist = strAudioExts.split(" ")

	  String straudio_media_types = prop3.getProperty("audio_media_types")
	  if (!straudio_media_types)
			  throw new Exception("cfg audio_media_types on tyhjä!")
	  def audiomedialist = straudio_media_types.split(" ")

	  for(File f in fOutPutDir.listFiles())
	  {
		  if (f.getName() in donotAddTheseFilesIntoOPF)
		  	   continue
		  iMediatypeInd = 0
		  for(String ext in audiolist)
		  {
			  if (f.getName().toLowerCase().endsWith("." +ext))
				  sb << "\t\t\t" +strItem_tmpl.replace("\$href", f.getName()).replace("\$id", cnstOPF_Prefix +iCnter++).replace("\$mediatype", "audio/" +audiomedialist[iMediatypeInd]) +"\n"
			  iMediatypeInd++
		  }

		  /*
	  	  if (f.getName().toLowerCase().endsWith(".res") && !(f.getName() in doNotAddTheseFilesIntoOPF))
	  	  {
			 sb << "\t\t\t" +strItem_tmpl.replace("\$href", f.getName()).replace("\$id", "resource").replace("\$mediatype", "application/x-dtbresource+xml") +"\n"
		  }
		  */
	  }

	  sb << "\t\t\t" +"<item href=\""+ strOutFileName +"\" id=\"" +(cnstOPF_Prefix +iCnter++) +"\" media-type=\"text/xml\"/>" +"\n"
	  
	  // sb << "\t\t\t" +"<item href=\"" + strOutFileName +"\" id=\"" +(cnstOPF_Prefix +iCnter++) +"\" media-type=\"text/xml\"/>" +"\n"
	  		  
	  sb << "\t\t\t" +"<item href=\"" + str_css_file +"\" id=\"" +(cnstOPF_Prefix +iCnter++) +"\" media-type=\"text/css\"/>" +"\n"

	  def xmlMediatypeList = []
	  def dtbookxmlmediatype = prop3.getProperty("dtbook_xml")
	  xmlMediatypeList.add dtbookxmlmediatype

	  for(File f in fOutPutDir.listFiles())
	  {
		  iMediatypeInd = 0
	
		  for(String ext in ['xml'])
		  {
			  fname = f.getName().toLowerCase()
			  if (fname.endsWith("." +ext))
			  {
		
				  sb << "\t\t\t" +strItem_tmpl.replace("\$href", f.getName()).replace("\$id", cnstOPF_Prefix +iCnter++).replace("\$mediatype", xmlMediatypeList[iMediatypeInd]) +"\n"
				iMediatypeInd++
			  }
		  }
	  }

	  String filename3 = prop3.getProperty("manifest_item_static_text")
	  String strStaticText = readFromDaisy3TemplateDirFile(filename3)
	  sb << "\t\t\t" +"\n" +strStaticText +"\n" 

	  sb.toString()
  }
  
  /**
   * opf daisy 3 tiedoston osatekstien generointi
   * 
   * @return
   */
  def String getSpinerefs()
  {
	  String filename = prop3.getProperty("item_refs")
	  String strItem_tmpl = readFromDaisy3TemplateDirFile(filename)
	  StringBuffer sb = new StringBuffer ()
	  
	  int iCnter = 1
	  for(VoiceDataFile vdf in listVDFs)
	  {
	  		sb << "\t\t\t" +strItem_tmpl.replace("\$id", cnstOPF_SmilPrefix +iCnter++) +"\n"
	  }
	  sb.toString()
  }
  
  /**
   * opf daisy 3 tiedoston osatekstien generointi
   * 
   * @return
   */
  def getspeechgen_opf_meta()
  {
	  StringBuffer sb = new StringBuffer ()
	  
	  int iCnter = 1
	  for(VoiceDataFile vdf in listVDFs)
	  {
		  for(VoiceData vd in vdf.listitems)
		  {
			  if (vd.name == VoiceData.cnstLipsyncXmlmark && vd.text.toString().toLowerCase().startsWith("<meta "))
			  	sb << "\t\t\t" +vd.text +"\n"
		  }
	  }
	  sb.toString()
  }

  def getspeechgen_opf_meta_for_opf()
  {
	  StringBuffer sb = new StringBuffer ()
	  
	  int iCnter = 1
	  for(VoiceDataFile vdf in listVDFs)
	  {
		  for(VoiceData vd in vdf.listitems)
		  {
			  if (vd.name == VoiceData.cnstLipsyncXmlmark && vd.text.toString().toLowerCase().startsWith("<meta "))
				  sb << "\t\t\t" +getDcXmlElements(vd.text) +"\n"
		  }
	  }
	  sb.toString()
  }

  def String getDcXmlElements(String metavalue)
  {
	  if (!metavalue)
	  	return metavalue
	  def match = metavalue =~ /<meta\s+name\s*=\s*\"(.*?)\"\s+content\s*=\s*\"(.*?)\"\s*\/>/
	  if (match.find())
	  {
		  def name = match[0][1]?.toString().replaceAll("[\r\n]","")
		  def content = match[0][2]?.toString().replaceAll("[\r\n]","")
		  if (name && name == "dtb:uid")
		  	return "<dc:Identifier id=\"uid\">" +content +"</dc:Identifier>"
		  return "<" +name +">" +content +"<" +'/' +name +">"
	  }
	  else
	  	println "getDcXmlElements no meta-element: " +metavalue
	  
	  metavalue
  }

  /**
   * lukee parametrinsa mukaan template daisy3 tiedoston
   * 
   * @param filename
   * @return
   * @throws NullPointerException
   * @throws Exception
   */
  def readFromDaisy3TemplateDirFile(String filename)
  throws NullPointerException, Exception
  {
	  String ret = hmDaisy3Templates.get(filename)
	  if (ret)
	  		return ret
			  
	  File fTemplate = new File(strSmilTemplateDir3 +File.separator +filename)
	  ret = readFromFile(fTemplate)
	  hmDaisy3Templates.put(filename, ret)
	  ret
  }
  
  /*
   * lukee tiedoston sisällön (tekstitiedoston)
   * 
   */
  def String readFromFile(File f)
  throws NullPointerException, Exception
  {
		if (!f)
			throw new NullPointerException("Parameter f is null!")
		if (!f.exists())
			throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) +": " +f)
		f.getText(/* "UTF-8" */)
  }

  /**
   * kirjoittaa tulostushakemistoon parametriensa mukaisen tiedoston
   * 
   * @param filename tiedoston nimi
   * @param value kirjoitettava mjono
   * @param bDeleteFile poistetaanko mahdollisesti jo olemassa oleva tiedosto ennen kirjoittamista
   * @return
   * @throws NullPointerException
   */
  def writeIntoOutputDirFile(String filename, String value, boolean bDeleteFile, boolean bPrintWritte = true)
  throws NullPointerException
  {
	  File fOut = new File(this.strOutputDir +File.separator +filename)
	  if (bPrintWritte)
	  	 println getMessages().getString(Lipsync2Daisy.constUI_ui_writingfile) +": " +fOut
	  writeIntoFile(fOut, value, bDeleteFile)
  }
  
  def String readOutputDirFile(String filename, boolean bPrintRead = true)
  throws NullPointerException
  {
	  File fOut = new File(this.strOutputDir +File.separator +filename)
	  if (bPrintRead)
	  	 println getMessages().getString(Lipsync2Daisy.constUI_ui_writingfile) +": " +fOut
	  println getMessages().getString(Lipsync2Daisy.constUI_ui_readingfile) +": " +fOut
	  readFromFile(fOut)
  }

  /**
   * Kirjoita value tiedostoon
   * 
  * @param f tiedoston nimi
   * @param value kirjoitettava mjono
   * @param bDeleteFile poistetaanko mahdollisesti jo olemassa oleva tiedosto ennen kirjoittamista
      * @return
   */
  def writeIntoFile(File f, String value, boolean bDeleteFile = false)
  throws NullPointerException
  {
		if (!f)
			throw new NullPointerException("Parameter f is null!")  
		if (value == null)
			throw new NullPointerException("Parameter value is null!") 
		if (bDeleteFile && f.exists())
			if (!f.delete())
				throw new Exception("En voi poistaaa tiedostoa: " +f)
		f.setText(value.replaceAll("^M", "").replaceAll("\r\n", "\n").replaceAll("(?s)\\p{Cntrl}&&[^\n]", "").replaceAll("(?s)\\t", ""), "UTF-8")
  }
  
  def String getDynamicCustomTestValues(String customtext, String strtemplate)
  { // teplate-value: customtext
	 if (customtext == null || !customtext)
     {
		 /* if (ddd)
		 	return strtemplate.replace("\$customtext","")
          */
	 	return ""
	 }
	 if (strtemplate == null || !strtemplate)
	 	return ""
	def binding2 = ["customtext":  customtext]
	def template2 = engine.createTemplate(strtemplate)
 	def data2 = template2.make(binding2).toString()
	data2
  }

  
  /**
   * generoidaan daisy 3 smil tiedostot
   * 
   * @param filename
   * @param bDeleteFile
   */
  def private void generateSmilFilesAfterTemplate(String filename, boolean bDeleteFile)
  {
	 println "\ngenerateSmilFilesAfterTemplate(" +filename +")\n"
	 
	 // TODO: <meta content="2:57:57.065" name="dtb:totalTime"/>
	 
	 String strOutFileName = filename.replace(".tmpl", "")
	 int ind = strOutFileName.lastIndexOf('.')
	 String strFileNameExt
	 if (ind > -1)
	 	strFileNameExt = strOutFileName.substring(ind +1)
	 else
	 	throw new Exception("strFileNameExt is null!")

	 String strTemplate = readFromDaisy3TemplateDirFile(filename)
	 String strTemplate2 = readFromDaisy3TemplateDirFile(speechgen_smil_meta_tmpl_file_name)
	 String strMediatype = prop3.getProperty(strFileNameExt)
	 if (!strMediatype)
	 	 throw new Exception(strFileNameExt +" is null!")
	 /*
	  	h1_id = register.getNextNccId()
					item.generateIdValues()
					def h_level = (item.iH_level < 1 ? 1 :item.iH_level)
					binding = ["h_level" : h_level, "h1_id":  'd' +h1_id, "css_class": "  class=\"page-normal\" ",
						"content_file_name" : smil_file_name,
						 "contentid": item.text_id,
						 "content_text": item.text ]
					strH1 = h1Template.make(binding).toString()
					
	  */
	 def speechgen_opf_meta	= getspeechgen_opf_meta()
	 def seq, data, data2
	 def binding, binding2
	 def template, template2 = engine.createTemplate(strTemplate2)

	 def xxxx, pagecount = ""
	 def double itotaltime = 0.0
	 def hmSegs, customtext
	 def newlistVDFs = []
	 
	 def customattributeTemplate = readFromDaisy3TemplateDirFile("customattributes.tmpl")
	 // if (dtbook_pagenum_on_off == "off")
	 	// customattributes = ""
	 old_totaltime = 0.0
	 def firstTime = true
	 def sum_itotaltime = itotaltime
	 def prev_seqduration = 0.0
	 def VoiceDataFile prev_vdf = null
				 
	 for(VoiceDataFile vdf in listVDFs)
	 {
		 Lipsync2Daisy.currentxmlfilenameprinted = false
		 Lipsync2Daisy.currentxmlfilename = vdf.file.toString()
	 
		 data = "" // strTemplate
		 vdf.prev_totaltime = totaltime
		 vdf.countDuration(firstTime,(prev_vdf == null ? 0.0 : prev_vdf.totaltime) )
		 totaltime = vdf.end
		 old_totaltime += vdf.old_totaltime
		 hmSegs = getSmilSeqs(vdf)
	     seq = hmSegs.get("string")
		 vdf = hmSegs.get("vdf")
		 customtext = hmSegs.get("customtext")
		 // data = data.replace("\$speechgen_meta", speechgen_opf_meta)
		 // data = data.replace("\$seq", seq)
		 binding2 = ["totalElapsedTime":  VoiceData.getClipTime(sum_itotaltime), "pagecount": pagecount ]
		 data2 = template2.make(binding2).toString()		 
		 binding = ["speechgen_meta":  speechgen_opf_meta +data2,
			 "customAttributes": getDynamicCustomTestValues(customtext, customattributeTemplate), "seq": seq ]
			 // <customTest defaultState="false" id="pagenum" override="visible"/>
		 if (!template)
		 	template = engine.createTemplate(strTemplate)
		 data = template.make(binding).toString()		 
		 writeIntoOutputDirFile(vdf.smil_file_name, data, true)
		 prev_seqduration = Double.parseDouble(getSmilParsseqdur(vdf))
		 if (Lipsync2Daisy.bCalculateMP3FileLengths)
		 	prev_seqduration = vdf.totaltime
		 // itotaltime = (vdf.totaltime - vdf.start)
		 // sum_itotaltime += itotaltime
		 sum_itotaltime += prev_seqduration
		 newlistVDFs.add vdf
		 prev_vdf = vdf
		 firstTime = false
	 }
	 listVDFs = newlistVDFs
	 Lipsync2Daisy.totaltime = sum_itotaltime
  }

  /**
   * Palauttaa merkkijonon generoitaessa daisy3 smil seq template'a
   * 
   * @param vdf
   * @return
   */
  def private HashMap getSmilSeqs(VoiceDataFile vdf)
  {
	  StringBuffer sb = new StringBuffer ()
	  String strTemplate = readFromDaisy3TemplateDirFile("seq.tmpl")
	  // TODO:
	  def seqdur = getSmilParsseqdur(vdf)
	  def hmPars = getSmilPars(vdf)
	  def pars   = hmPars.get("string")
	  def customtext = hmPars.get("customtext")
	  def binding = ["seqdur":  VoiceData.getClipTime(Double.parseDouble(seqdur)), "pars": pars ]
	  def template = engine.createTemplate(strTemplate)
	  sb << template.make(binding).toString()	  
	  hmPars.put("string", sb.toString())
	  hmPars.put("customtext", customtext)	  
	  return hmPars
  }
 
  /**
   *   * Palauttaa keston merkkijonon generoitaessa daisy3 smil seq template'a  
   * @param vdf
   * @return
   */
  private def String getSmilParsseqdur(VoiceDataFile vdf)
  {
 	  def duration = 0.0
	  VoiceData lastitem, firstitem
	  def cnt = 0

	  for(VoiceData vd in vdf.listitems)
	  {
		  if (vd.name == VoiceData.cnstLipsyncWord)
		  {
			  cnt++
			  if (cnt == 1)
			  {
			  	lastitem = null
				firstitem = vd
			  }
			  else
			  	lastitem = vd
		  }		  		
	  }
	  if (!firstitem)
	  	return duration.toString()
		  
	  if (lastitem)
	  {
		 // return "" +lastitem.end - firstitem.start    
		  return "" +lastitem.end
	  }
	  duration = firstitem.getDuration()
	  duration.toString()
  }
  
  def private String getCustomTestAttributeName(String elementname)
  {
	  if (elementname == null)
	  		return ""
	  if (elementname in listCustomTestAttributeNames)
	  	  return elementname
	  ""
  }
  
  /**
    * Palauttaa pars merkkijonon generoitaessa daisy3 smil seq template'a
    * @param vdf
   * @return
   */
  def private HashMap getSmilPars(VoiceDataFile vdf)
  {
	  StringBuffer sb = new StringBuffer ()
	  StringBuffer sbCustomtext  = new StringBuffer ()
	  String strTemplate = readFromDaisy3TemplateDirFile("par_sequence.tmpl")
	  def par_id = ""
	  def text_id = ""	  
	  def dtb_id = ""
	  def speec_file_name = ""
	  def audio_id = ""
	  def clip_begin = ""
	  def clip_end = ""
	  def begin, end
	  def data
	  def binding
	  def iPage = -1
	  def template
	  def iCnt = 0
	  
	  def updatedlistitems = []
	  def isSmilVD = false
	  def isFirstSmilVD = true
	  def listAddCustomTestElements = []
	  // TODO: SEQ & TOTALTIME / SMIL-FILE	
	  def isFirstItem = false
	  
	  for(VoiceData vd in vdf.listitems)
	  {	
	      /*
		  if (vd.iH_level > 0)
		  	println "<h"
		  if (vd.text && vd.text.toString().toLowerCase().contains("list"))
		  		println "<list>"
		  if (vd.text && vd.text.toString().toLowerCase().startsWith("</list"))
			  println "</list>"
		  if (vd.text && vd.text.toString().toLowerCase().startsWith("</img"))
			  println "</img>"
		  if (vd.text?.toString().toLowerCase().startsWith("<img ") )
		  	 println "<img "
		  if (vd.iPage > 0 || (vd.xmlText && vd.xmlText.toString().toLowerCase().contains("page" )))
		  	 println "<PAGENUM"
		   // if (vd.content_id == "dtb12")
			  // println "dtb12"
			*/
			
		   if (vd.iPage > 0 && (vd.clip_begin == vd.clip_end || vd.start == vd.end))
			   vd.clip_end = vd.next.clip_end
	 
		  def elementname = null
		  if (vd.name == VoiceData.cnstLipsyncXmlmark || (Lipsync2Daisy.dtbook_pagenum_on_off != "off" && vd.iPage > 0 && !vd.isXmlMarkPunkt))
		  {
		  		elementname = vd.text?.toString().toLowerCase().replace("<","").replace(">","").replace("/","").split(" ")[0]
				if (vd.iPage > 0 && !vd.isXmlMarkPunkt)
				{
					elementname = "pagenum"
				}
				if (elementname)
				{
					/* println "elementname '" +elementname +"'"
					if (elementname == "prodnote")
						println("prodnote")
					if (elementname == "table")
						println("table")
					if (elementname.contains("page"))
						println("pagenum")
					if (elementname in listCustomTestAttributeNames)
						println "dddk"
					*/
				}
		  }
		  if (Lipsync2Daisy.dtbook_pagenum_on_off == "off" && (vd.iPage > 0 || (vd.text && (vd.text.toString().toLowerCase().startsWith("</pagenum>") || vd.text.toString().toLowerCase().startsWith("<pagenum"))
			  || (vd.xmlText && (vd.xmlText.toString().toLowerCase().startsWith("<pagenum")
				  || vd.xmlText?.toString().toLowerCase().startsWith("</pagenum>")))
			  )))
		  {
			  updatedlistitems.add(vd)
			  continue
		  }
		  
		  isSmilVD = (vd.name == VoiceData.cnstLipsyncWord)
		  // if (!isSmilVD && vd.iPage > 0)
		  	// isSmilVD = true
		  if ((Lipsync2Daisy.dtbook_pagenum_on_off != "off" && vd.iPage > 0 && !vd.isXmlMarkPunkt) || (vd.isXmlMarkPunkt && ( /* vd.text?.toString().toLowerCase().startsWith("<img ")
			  || vd.text?.toString().toLowerCase() == "</img>" 
			  || */ /* vd.text?.toString().toLowerCase().startsWith("<pagenum ")
			  || vd.text?.toString().toLowerCase().startsWith("<table")
			  || vd.text?.toString().toLowerCase() == "</table>"
			  || vd.text?.toString().toLowerCase() == "</list>"
			  || vd.text?.toString().toLowerCase().startsWith("<list " )
			  || */ elementname in listCustomTestAttributeNames)))
		  {
			  // def elementname = vd.text?.toString().toLowerCase().replace("<","").replace(">","").replace("/","").split(" ")[0] 
			  if (vd.iPage > 0 || vd.text?.toString().toLowerCase().startsWith("<$elementname"))
			  {
				  def data2 = null
				  if (vd.iPage < 0)
				  	data2 = vd.getSmilTableStart(elementname)
				  else
				  {
						print ""
				  }
				  if (!(elementname in listAddCustomTestElements))
				  // if ( /* !(elementname in listAddCustomTestElements) && */ !(elementname in listRemovedCustomTestAttributeNames))
				  {
					  if (!(elementname in listRemovedCustomTestAttributeNames))
					  {
						  sbCustomtext << "<customTest defaultState='false' id='$elementname' override='visible'/>\n"
						  listAddCustomTestElements.add elementname
					  }
				  } 
				  if (!(elementname in listNCXCustomTestElements))
				  {					  
					  def bookstruct_attr = hmBookStruct.get elementname
					  if (bookstruct_attr != null)
					  {
						  if (Lipsync2Daisy.dtbook_pagenum_on_off == "off" && elementname == "page")
						  {
						  	bookstruct_attr = bookstruct_attr 
						  }
						  else
						  if (!(elementname in listRemovedCustomTestAttributeNames))
						  {
							 // if (!(elementname in listRemovedCustomTestAttributeNames))
							  // {
								  sbNCXCustomTestElements << "<smilCustomTest id='$elementname' bookStruct='$bookstruct_attr' defaultState='false' override='visible'/>\n"
								  listNCXCustomTestElements.add elementname
							 //  }
						  }
					  }
					  
					  if (!(elementname in listRemovedCustomTestAttributeNames))
					  {
						  if (!(elementname in listNCXCustomTestElements))
						  {
							  sbNCXCustomTestElements << "<smilCustomTest id='$elementname' defaultState='false' override='visible'/>\n"
							  listNCXCustomTestElements.add elementname
						  }
					  }
				  }
				  
				  if (vd.iPage < 0)
				  {
					  sb << "       " +data2 +"\n"
					  // vd.isSmillPar = true
					  updatedlistitems.add(vd)
					  continue
				  }
			  }
			  if (vd.text?.toString().toLowerCase() =="</$elementname>")
			  {
				  if (vd.iPage < 1 && elementname != "pagenum")
				  	sb << "       </seq>\n"
				  // vd.isSmillPar = true
				  updatedlistitems.add(vd)
				  continue
			  }
			  isSmilVD = true
		  }
		 
		  if (!isSmilVD && vd.iH_level > 0)
		  {
		  		// isSmilVD = true // on xml h1 tms = tulostetaan smil par
			    continue // tulostetaan teksti otsikko eli seuraava vd
		  }
		  /*		 
		  else
		  if (isSmilVD && vd.iH_level > 0)
		  {
			isSmilVD = false // on word h1 tms = ei tulosteta smil par
		  }
		  */
			
		  if (isSmilVD)
		  {
			  if (Lipsync2Daisy.dtbook_pagenum_on_off == "off" && (vd.iPage > 0 || vd.previous?.iPage > 0 ))
			  {
				  updatedlistitems.add(vd)
				  continue
			  }
			  
			  if (vd.clip_begin == null)
			  {
				def convertAllways = true
				vd.generateIdValues()
				if (vd.clip_begin == vd.clip_end)
					vd.clip_end = vd.next.clip_end
				if (!isFirstItem)
				{
					vd.isFirstItem = true
					isFirstItem = true
				}
			  	vd.convert2Smil(convertAllways)
			  }	
			  
			  if (VoiceData.fTimeshift_into_voicedatas != 0.0)
			  {
				  vd.clip_begin = null // tka 2013.4.13
				  vd.clip_end = null // tka 2013.4.13
				  /*
				  if (!isFirstItem)
				  {
					vd.isFirstItem = true
					isFirstItem = true
				  }
				  */
				  vd.convert2Smil() // tka 2013.4.13
			  }
				  
			  par_id = vd.par_id
			  text_id = vd.text_id
			  iPage = vd.iPage
			  dtb_id = (Lipsync2Daisy.dtbook_pagenum_on_off == "on" && vd.iPage > 0 ? ("page" +vd.iPage) : vd.content_id)
			  speec_file_name = vdf.mp3_file_name
			   // vdf.smil_file_name
			  audio_id = vd.audio_id
			  clip_begin = vd.clip_begin
	  		  if (isFirstSmilVD)
			  {
				  vd.start = 0.0
				  clip_begin = "0:00:00.0"
				  isFirstSmilVD = false
			  }
			  begin = vd.start
			  // TODO: isFirstSmilVD & clip_begin = "0:0:00" 
			  clip_end = vd.clip_end
			  end = vd.end
			  if (!clip_end || !clip_begin)
			  {
				  /*
				  if (!isFirstItem)
				  {
					  vd.isFirstItem = true
					  isFirstItem = true
				  }  
				  */
			  	 vd.convert2Smil()
			  }
				   /*
			  if (isFirstSmilVD)
			  {
			  	clip_begin = "0:00:00"
				isFirstSmilVD = false
			  }
			  */
			  if (!clip_end)
				clip_end = vd.clip_end
			  if (!clip_begin)
			  {
			  	clip_begin = vd.clip_begin
				begin = vd.start
			  }
			  // data = new String(strTemplate)
			  if (clip_begin == clip_end)
			  {
				 if (vd.next?.clip_begin > clip_end)
				 {
			  	 	clip_end = vd.next.clip_begin
					end = vd.end
				 }
				 else
				 {
					 if (vd.isXmlMarkPunkt && vd.text?.toString().toLowerCase().startsWith("<img "))
					 {
						int iAdded = ++Integer.parseInt(clip_end.toString().substring(clip_end.toString().length()-1))					
						if (iAdded < 10)
							 clip_end = clip_end.toString().substring(0, clip_end.toString().length()-1) +iAdded
						else
						{
							iAdded = ++Integer.parseInt(clip_end.toString().substring(clip_end.toString().length()-2, clip_end.toString().length()-1))
							clip_end = clip_end.toString().substring(0, clip_end.toString().length()-2) +iAdded +"0"
						}
					 }
				 }
			  }
			  def this_element_name = getCustomTestAttributeName(elementname)
			  
			  if (iCnt == 0)
			  	clip_begin = "0:00:00.0"
			  iCnt++
			  binding = ["custompagetemplate": ( Lipsync2Daisy.dtbook_pagenum_on_off == "on" && vd.iPage > 0 ? 'customTest="pagenum"' : (this_element_name ? this_element_name : "")),"par_id":  par_id, "text_id": text_id, "dtbookfilename": dtbookfilename, 
				  "content_id": (iPage && iPage != -1 ? ("page" +iPage) : vd.content_id),
				  "dtb_id" : dtb_id, "speec_file_name" : speec_file_name, /* "audio_id": audio_id, */
				  "clip_begin": clip_begin, "clip_end": clip_end ]
			  if (!template)
				  template = engine.createTemplate(strTemplate)
			  data = template.make(binding).toString()
	  
			  // data = data.replace("$text_id", text_id).replace("$par_id", par_id).replace("$dtbookfilename", dtbookfilename)
			  // data = data.replace("$dtb_id", dtb_id).replace("$speec_file_name", speec_file_name)
			  // data = data.replace("$audio_id", audio_id).replace("$clip_begin", clip_begin)
			  // data = data.replace("$clip_end", clip_end)
				 
			  sb << data +"\n"
			  vd.isSmillPar = true
		  }	
		  updatedlistitems.add(vd)
	  }
	  vdf.listitems = updatedlistitems
	  
	  def ret = [:]
	  ret.put("string", sb.toString())
	  ret.put("vdf", vdf)
	  ret.put("customtext", sbCustomtext.toString());
	  return ret
  }
  
  /**
   * generoidaan dtbook smil tiedosto
   * 
   * @param filename
   * @param bDeleteFile
   */
  def private generateDtbookFileAfterTemplate(String filename, boolean bDeleteFile, String tmp_filename = null)
  {
	   if (tmp_filename == null)
	   		println "\ngenerateDtbookFileAfterTemplate(" +filename +")\n"
	   String strTemplate = readFromDaisy3TemplateDirFile(filename)	   
   	  // TODO	   
	   
	   def iCnt = 0
	   def StringBuffer sbSmil
	   StringBuffer sb = new StringBuffer()
	   def newUpdatedListVDFs = []
	   ReturnStrinAndList retvalues
	   
	   def speechgen_opf_meta		= getspeechgen_opf_meta() 
	   // sb.append speechgen_opf_meta
	   boolean bNcxMarkOn = true
	    
	   for(VoiceDataFile v in listVDFs)
	   {
		   // println v.file
		   Lipsync2Daisy.currentxmlfilenameprinted = false
		   Lipsync2Daisy.currentxmlfilename = v.file.toString()
	   
		   currentVoiceDataFile = v
		   iCnt++
		   if (tmp_filename)
		   {
		   		sb << v.getDaisy3DtbookXmlData(bNcxMarkOn)
		   }
		   else
		   		sb << v.getDaisy3DtbookXmlData()
		   if (tmp_filename == null)
		   		newUpdatedListVDFs.add v
	   }

  	   def binding = ["xmlversion": VoiceDataFile.xmlversion_voicedata.text, "daisydtbook":  sb.toString() ]
	   def template = engine.createTemplate(strTemplate)
	   def data = template.make(binding).toString()
	   if (tmp_filename != null)
	   { 
		    def tmpFName = (str_user_home ? str_user_home +File.separator +tmp_filename : strLipsyncBaseCfgDir +File.separator +tmp_filename )
	   		def fTmpXml = new File(tmpFName)//   19999 
			if (fTmpXml.exists())
				fTmpXml.delete()
			fTmpXml.setText(data.replaceAll("(?s)\\p{Cntrl}&&[^\n]", ""))
			return null
	   }	   
	   
	   if (tmp_filename == null)
	   {
		   doctitle = getDocTitle(data)
		   docauthors = getDocAuthorsAudio(listAuthors) // getDocAuthors(data)
	   }

	   if (tmp_filename)
	   		writeIntoOutputDirFile(tmp_filename /* filename.replace(".tmpl", "") */, data.toString().replaceAll("(\\w)\\(", "\$1 (").replaceAll("\\)(\\w)", "\\) \$1").replaceAll("\\(\\s+(\\w)", "\\(\$1").replaceAll("(\\w)\\s+\\)", "\$1\\)"), bDeleteFile)
	   else
	   		writeIntoOutputDirFile(dtbookfilename /* filename.replace(".tmpl", "") */, data.toString().replaceAll("(\\w)\\(", "\$1 (").replaceAll("\\)(\\w)", "\\) \$1").replaceAll("\\(\\s+(\\w)", "\\(\$1").replaceAll("(\\w)\\s+\\)", "\$1\\)"), bDeleteFile)
	  return newUpdatedListVDFs
  }
  
  def static public String getTypeAttributeOfDtbook(String elementname)
  {
	  hmTypeAttributeOfDtbook.get elementname
  }

  def private String getVDAudio(VoiceData vd)
  {
	  if (vd == null || vd.start == -1)
	  	return ""
	  def clipbegin = VoiceData.getClipTime(vd.start)
	  def clipend = VoiceData.getClipTime(vd.end)
	  def src = vd.mp3_file_name
	  def final filename = "docauthor_audio.tmpl"
	  String strTemplate = readFromDaisy3TemplateDirFile(filename)
	  def template, binding, data
	  if (strTemplate)
		 template = engine.createTemplate(strTemplate)
	  def sb = new StringBuffer ()
	  binding = ["clipbegin":  clipbegin, "clipend": clipend,  "src": src]
	  data = template.make(binding).toString()
	  data
  }
  
  def private String getDoctitleAuthors(vdAuthors)
  {
	  if (vdAuthors == null || vdAuthors.size() == 0)
	  	return ""
	  def final filename = "docauthor.tmpl"
	  String strTemplate = readFromDaisy3TemplateDirFile(filename)
	  def template, binding, data
	  if (strTemplate)
		 template = engine.createTemplate(strTemplate)
	  def sb = new StringBuffer () 
	  vdAuthors.each {
		  binding = ["docauthor_audio": getVDAudio(it) ]
		  // VoiceData.getClipTime(it.clip_end)
		  data = template.make(binding).toString()
	      sb.append data
	  }
      sb.toString()
	  // ssss
  }

  def private String getDoctitleAudio(VoiceData vdTitle)
  {
	  if (vdTitle == null)
	  	  return ""			
	  def final filename = "doctitle_audio.tmpl"
	  String strTemplate = readFromDaisy3TemplateDirFile(filename)
	  def binding = ["clipbegin":  VoiceData.getClipTime(vdTitle.start), "clipend": VoiceData.getClipTime(vdTitle.end),
		  "src": (vdTitle.mp3_file_name ? vdTitle.mp3_file_name : "")	]
	  def template
	  if (strTemplate)
		 template = engine.createTemplate(strTemplate)
	  def data = template.make(binding).toString()		 
      data
  }

  def private String getDocAuthorsAudio(listAuthors)
  {
	  	if (listAuthors == null)
	  		return ""
	  	def final filename = "docauthor_ncx_audio.tmpl"
		String strTemplate = readFromDaisy3TemplateDirFile(filename)
		def binding
		def template = engine.createTemplate(strTemplate)
		def data
		def sb = new StringBuffer()
		
		for (VoiceData vd in listAuthors)
		{
			binding = ["author":  vd.text, "clipbegin":  VoiceData.getClipTime(vd.start), "clipend": VoiceData.getClipTime(vd.end),
				"src": (vd.mp3_file_name ? vd.mp3_file_name : "")]
			data = template.make(binding).toString()
			sb << data
		}
		sb.toString()
  }
  
  /**
   * Palauttaa 2 arvoa hashmap:ssa: toinnen page_sb ja toinen vd jossa mennään, koska kokoaa
   * page_sb:hen enemmänkin dataa jos vd parametrin VoiceData ok. Eli sellainen, jonka dataa
   * pitää koota NCX tiedostoon. Kokoaa vd ja sitä vastaavan mjono collectNCXPageList listaan.
   */
  	def private collectNCXPageString2(param, VoiceData prev_hlevel_vd, String file_nav_sb = nul)
	{
		def page_sb = new StringBuffer()
		def local_nav_sb  = new StringBuffer()
		def nav_data = "", clipBegin, clipEnd
		def dtb_id, nav_binding, match, playorder
		def content_src, strid, strplayOrder, strvalue
		def src, page_binding, page_data, navPoint = ""
		def strclass, nav_id, text, strText
		def VoiceData vd
		def VoiceDataFile vdf
		def bPrev_HLevel_vd_higher_than_this_vd = false
		def tmp_bPrev_HLevel_vd_higher_than_this_vd = false
		def founded_vd = false
		String pageTemplate = readFromDaisy3TemplateDirFile("pagetarget.ncx.tmpl")
		if (!page_template)
			page_template = engine.createTemplate(pageTemplate)
			
		if (param instanceof VoiceDataFile)
		{
			vdf = param
			def tmp_hmRet, tmp_page_sb, tmp_nav_sb
			founded_vd = true
			// if (file_nav_sb)
				// local_nav_sb << file_nav_sb
			for(VoiceData fvd in vdf.listitems)
			{
			   if (fvd == null)
				   continue
			   tmp_hmRet = collectNCXPageString(fvd, prev_hlevel_vd, null)
			   if (tmp_hmRet == null) // not founded
					  continue
			   prev_hlevel_vd = tmp_hmRet.get("prev_hlevel_vd")
			   tmp_bPrev_HLevel_vd_higher_than_this_vd = tmp_hmRet.get("bPrev_HLevel_vd_higher_than_this_vd")
			   // tmp_page_sb = tmp_hmRet.get("page_sb")
			   // if (tmp_page_sb)
					// page_sb << tmp_page_sb.toString()
			   tmp_nav_sb = tmp_hmRet.get("nav_sb")
			   if (tmp_nav_sb)
			   {
				 /*
				 if (tmp_bPrev_HLevel_vd_higher_than_this_vd)
				 {
					 def tmp_nav_sb2 = nav_sb.toString().replace("\$navPoint", tmp_nav_sb.toString())
					 if (nav_sb.length() > 0)
						 nav_sb.delete(0, nav_sb.length()-1)
					 nav_sb << tmp_nav_sb2
				 }
				 else
				 {
					 def tmp_nav_sb2 = nav_sb.toString().replace("\$navPoint", "")
					 if (nav_sb.length() > 0)
						 nav_sb.delete(0, nav_sb.length()-1)
					 nav_sb << tmp_nav_sb2
				 }
						nav_sb << tmp_nav_sb.toString()
					*/
				   def item = ["vd": prev_hlevel_vd, "navPoint": tmp_nav_sb.toString().replaceAll("\$navPoint", "")]
				   collectNCXPageList.add item
			   }
			}
		}
		else
		if (param instanceof VoiceData) // listataan page-listaa ncx:ään
		{
			vd = param
			text = ""
			strText = vd.xmlText?.toString()
			/*
			if (strText != null && strText.toString().contains("<page"))
			{
				println "koe"
			}
			*/
				
			// if (!strText)
				// continue
			/*
			if (vd.iH_level != 1 && (strText &&
				!strText.toLowerCase().startsWith("<pagenum ") ?* || strText.startsWith("</")*? ) )
				continue
			*/
			if (!vd.isXmlMarkPunkt && vd.iH_level > 0)
			{
				if (vd.text == null)
					return null
				/*
				match = vd.text?.toString() =~ /<\/?(h|H)\d+>/
				if (!match.find())
					return null
				*/
						 // TODO:
				strclass = "h" +vd.iH_level
				// tka 2013040.12:
				/*
				if (vd.iPlayOrder == 0)
					vd.iPlayOrder = getPlayOrder()
				playorder = vd.iPlayOrder
				nav_id = "ncx-" +playorder
				*/
				//strText = vd.next?.text?.toString()
				strText = vd.text?.toString()
				// strText = vd.next?.text
				if (strText == null || strText == "null" || strText == "<h1>" || strText == "</h1>" || strText == "</H1>" )
				{
					println "strText = '$strText'"
					println "strText = '$strText'"
				}
	 
				if (strText && strText.contains("</"))
					text = "generate error: Error in voicedata element!; possible ending block of h1 as a text voicedata !"
					//clipBegin = vd.next?.clip_begin
				clipBegin = vd.clip_begin
				//clipEnd = vd.next?.clip_end
				clipEnd = vd.clip_end
				//src = vd.next?.smil_file_name
				src = vd.mp3_file_name					// content_src = vd.next?.smil_file_name // +"#" +vd.text_id
				content_src = vd.smil_file_name // +"#" +vd.text_id
				dtb_id = vd.par_id
				text = strText
				if (prev_hlevel_vd && prev_hlevel_vd.iH_level < vd.iH_level)
				{
					bPrev_HLevel_vd_higher_than_this_vd = false
					/*
					def tmp_hmRet = collectNCXPageString(vd, prev_hlevel_vd, null)
					if (tmp_hmRet != null) // not founded
					{
						prev_hlevel_vd = tmp_hmRet.get("prev_hlevel_vd")
						tmp_bPrev_HLevel_vd_higher_than_this_vd = tmp_hmRet.get("bPrev_HLevel_vd_higher_than_this_vd")
						tmp_page_sb = tmp_hmRet.get("page_sb")
						if (tmp_page_sb)
							 page_sb << tmp_page_sb.toString()
						tmp_nav_sb = tmp_hmRet.get("nav_sb")
						if (tmp_nav_sb)
						{
						  if (tmp_bPrev_HLevel_vd_higher_than_this_vd)
						  {
							  def tmp_nav_sb2 = nav_sb.toString().replace("\$navPoint", tmp_nav_sb.toString())
							  nav_sb.delete(0, nav_sb.length()-1)
							  nav_sb << tmp_nav_sb2
						  }
						  else
								 nav_sb << tmp_nav_sb.toString()
					}
					*/
				}
				
				if (vd.iPlayOrder == 0)
					vd.iPlayOrder = getPlayOrder()
				playorder = vd.iPlayOrder
				nav_id = "ncx-" +playorder
						
				navPoint = navPoint.toString().replaceAll("\$navPoint", "")
				
				nav_binding = ["strclass":  strclass, "strid": nav_id,
							"strplayorder": playorder, "text": text,
							"clipBegin": clipBegin, "clipEnd": clipEnd,
							"src": src, "content_src": content_src,
							"dtb_id": dtb_id, /* jätetään siksi kunnes ollaan tultu
							ylemmälle tasolle, jolloin tiedetään mitä tehdään: */
							"navPoint": "\$navPoint" /* navPoint */ ]
						// if (!nav_templae)
							// nav_templae = engine.createTemplate(navPointTemplate)
				nav_data = nav_template.make(nav_binding).toString()
				local_nav_sb << nav_data
				founded_vd = true
				if (prev_hlevel_vd && prev_hlevel_vd.iH_level > vd.iH_level)
				{
					bPrev_HLevel_vd_higher_than_this_vd = true
				}
				prev_hlevel_vd = vd
			}
			else
			if (Lipsync2Daisy.dtbook_pagenum_on_off != "off" && vd.iPage > 0 || (vd.xmlText && vd.xmlText.toString().toLowerCase().startsWith("<pagenum ")
				|| (vd.text && vd.text.toString().toLowerCase().startsWith("<pagenum "))) )
			{
				// if is not right page instance and it will be in vd.next
				if (vd.text && vd.text.toString().toLowerCase().startsWith("<pagenum "))
					vd = vd.next // koska vastaavaa nextia käytetään generoitaessa smil-tiedoston sisältöä
				// vd.iPlayOrder = getPlayOrder()
				strclass = "pagenum"
				strvalue = vd.text
				if (vd.iPage > -1)
					strvalue = vd.iPage
				else
				if (vd.iPage == -1 && strvalue && strvalue.toString().startsWith("</page") && vd.previous.iPage > -1) // if page iPage word / value does not exists!
				{
					strvalue = "" +vd.previous.iPage
					clipBegin = vd.previous.clip_begin
					if (!clipBegin)
						clipBegin = VoiceData.getClipTime(vd.previous.start)
					vd.clip_begin = clipBegin
				}
				else
					clipBegin = vd.clip_begin
				if (!clipBegin) // sometimes can be empty
				{
					clipBegin = VoiceData.getClipTime(vd.start)
					vd.clip_begin = clipBegin
				}
				clipEnd = vd.clip_end
				if (!clipEnd)
				{
					clipEnd = VoiceData.getClipTime(vd.end)
					// into comment 5.4.2015: tka vd.clip_begin = clipEnd
					vd.clip_end = clipEnd
				}

				src = vd.mp3_file_name
				content_src = vd.smil_file_name // +"#" +vd.text_id
				dtb_id = vd.par_id
				if (vd.text && vd.text.toString().toLowerCase().startsWith("</pagenum"))
					dtb_id = vd.previous.par_id
	
				if (vd.iPlayOrder == 0)
					vd.iPlayOrder = getPlayOrder()
				playorder = vd.iPlayOrder
				strid = "ncx-" +playorder
				strplayOrder = "" +playorder
				nav_id = "ncx-" +playorder
				Lipsync2Daisy.totalpage++

				// Lipsync2Smil.maxpage++
				page_binding = ["strid":  strid, "strplayOrder": strplayOrder,
					"strplayorder": strplayOrder, "strvalue": strvalue,
					"clipBegin": clipBegin, "clipEnd": clipEnd,
					"src": src, "content_src": content_src,
					"dtb_id": dtb_id
					 ]
				page_data = page_template.make(page_binding).toString()
				if (page_data /* && page_data.length() > 0 */)
					Lipsync2Daisy.ncx_total_pages++
				page_sb << page_data
				founded_vd = true
			}
			else
			if (vd.iPage == -1 /* ei ole pagenum */ && vd.text && vd.text.toString().toLowerCase().startsWith("<") )
			{
				def elementname = vd.text.toString().split(" ")[0].toString().substring(1)
				if (elementname && elementname in listNCXCustomTestElements)
				{
					// Lipsync2Smil.maxpage++
					// vd.iPlayOrder = getPlayOrder()
					strclass = elementname
					strvalue = vd.text
					clipBegin = vd.clip_begin
					if (!clipBegin) // sometimes can be empty
					{
						clipBegin = VoiceData.getClipTime(vd.start)
						vd.clip_begin = clipBegin
					}
					clipEnd = vd.clip_end
					if (!clipEnd)
					{
						clipEnd = VoiceData.getClipTime(vd.end)
						vd.clip_begin = clipEnd
					}

					src = vd.mp3_file_name
					content_src = vd.smil_file_name // +"#" +vd.text_id
					dtb_id = vd.par_id
		
					if (vd.iPlayOrder == 0)
						vd.iPlayOrder = getPlayOrder()
					playorder = vd.iPlayOrder
					strid = "ncx-" +playorder
					strplayOrder = playorder
					nav_id = "ncx-" +playorder
					// text = vd.text
	
					/*
					page_binding = ["strid":  strid, "strplayOrder": strplayOrder,
						"strplayorder": strplayOrder, "strvalue": strvalue,
						"clipBegin": clipBegin, "clipEnd": clipEnd,
						"src": src, "content_src": content_src,
						"dtb_id": dtb_id
						 ]
					page_data = page_template.make(page_binding).toString()
					page_sb << page_data
					*/
					navPoint = navPoint.toString().replaceAll("\$navPoint", "")
					
					strclass = elementname
					nav_binding = ["strclass":  strclass, "strid": nav_id,
						"strplayorder": playorder, "text": text,
						"clipBegin": clipBegin, "clipEnd": clipEnd,
						"src": src, "content_src": content_src,
						"dtb_id": dtb_id, /* jätetään siksi kunnes ollaan tultu
						ylemmälle tasolle, jolloin tiedetään mitä tehdään: */
						"navPoint": "\$navPoint" /* navPoint */ ]
					// if (!nav_templae)
						// nav_templae = engine.createTemplate(navPointTemplate)
					nav_data = nav_template.make(nav_binding).toString()
					local_nav_sb << nav_data
					founded_vd = true
				}
			}
		}
		
		if (!founded_vd)
			return null
			
		if (page_sb.toString() == "")
			page_sb = null
			
		def hmRet = [:]
		hmRet.put "prev_hlevel_vd", prev_hlevel_vd?.toString()
		hmRet.put "page_sb", page_sb?.toString()
		hmRet.put "nav_sb", local_nav_sb?.toString()
		hmRet.put "bPrev_HLevel_vd_higher_than_this_vd", bPrev_HLevel_vd_higher_than_this_vd
		hmRet.put "vd", vd
		hmRet
	}
	
  /**
   * Palauttaa 2 arvoa hashmap:ssa: toinnen page_sb ja toinen vd jossa mennään, koska kokoaa
   * page_sb:hen enemmänkin dataa jos vd parametrin VoiceData ok. Eli sellainen, jonka dataa
   * pitää koota NCX tiedostoon. Kokoaa vd ja sitä vastaavan mjono collectNCXPageList listaan.
   */
  def collectNCXPageString(param, VoiceData prev_hlevel_vd, String file_nav_sb)
    {
		// def page_sb = new StringBuffer() 
		def local_nav_sb  = new StringBuffer()
		def nav_data = "", clipBegin, clipEnd
		def dtb_id, nav_binding, match, playorder
		def content_src, strid, strplayOrder, strvalue
		def src, page_binding, page_data, navPoint = ""
		def strclass, nav_id, text, strText
		def VoiceData vd
		def VoiceDataFile vdf
		def bPrev_HLevel_vd_higher_than_this_vd = false
		def tmp_bPrev_HLevel_vd_higher_than_this_vd = false
		def founded_vd = false
		String pageTemplate = readFromDaisy3TemplateDirFile("pagetarget.ncx.tmpl")
		if (!page_template)
			page_template = engine.createTemplate(pageTemplate)
			
		if (param instanceof VoiceDataFile)
		{	
			vdf = param
			def tmp_hmRet, tmp_page_sb, tmp_nav_sb 
			founded_vd = true
			// if (file_nav_sb)
				// local_nav_sb << file_nav_sb
			for(VoiceData fvd in vdf.listitems)
			{
			   if (fvd == null)
				   continue
			   tmp_hmRet = collectNCXPageString(fvd, prev_hlevel_vd, null)
			   if (tmp_hmRet == null) // not founded
			   	   continue
			   prev_hlevel_vd = tmp_hmRet.get("prev_hlevel_vd")
			   tmp_bPrev_HLevel_vd_higher_than_this_vd = tmp_hmRet.get("bPrev_HLevel_vd_higher_than_this_vd") 
			   // tmp_page_sb = tmp_hmRet.get("page_sb")
			   // if (tmp_page_sb)
			   	 // page_sb << tmp_page_sb.toString()
			   tmp_nav_sb = tmp_hmRet.get("nav_sb")
			   if (tmp_nav_sb)
			   {
				 /*  
				 if (tmp_bPrev_HLevel_vd_higher_than_this_vd)
				 {
					 def tmp_nav_sb2 = nav_sb.toString().replace("\$navPoint", tmp_nav_sb.toString())
					 if (nav_sb.length() > 0)
					 	nav_sb.delete(0, nav_sb.length()-1)
					 nav_sb << tmp_nav_sb2 
				 }
				 else
				 {
					 def tmp_nav_sb2 = nav_sb.toString().replace("\$navPoint", "")
					 if (nav_sb.length() > 0)
						 nav_sb.delete(0, nav_sb.length()-1)
					 nav_sb << tmp_nav_sb2
				 }
			   	 	nav_sb << tmp_nav_sb.toString()
			   	 */
				   def item = ["vd": prev_hlevel_vd, "navPoint": tmp_nav_sb.toString().replaceAll("\$navPoint", "")]
				   collectNCXPageList.add item
			   }					
			}
		}
		else
		if (param instanceof VoiceData) // listataan page-listaa ncx:ään
		{
			vd = param
			text = ""
			strText = vd.xmlText?.toString()
			// if (!strText)
				// continue
			/*
			if (vd.iH_level != 1 && (strText &&
				!strText.toLowerCase().startsWith("<pagenum ") ?* || strText.startsWith("</")*? ) )
				continue
			*/
			if (!vd.isXmlMarkPunkt && vd.iH_level > 0)
			{
				if (vd.text == null)
					return null
				/*
				match = vd.text?.toString() =~ /<\/?(h|H)\d+>/
				if (!match.find())
					return null
				*/
	 					// TODO:
				strclass = "h" +vd.iH_level
				// tka 2013040.12:
				/*
				if (vd.iPlayOrder == 0)
					vd.iPlayOrder = getPlayOrder()
				playorder = vd.iPlayOrder
				nav_id = "ncx-" +playorder
				*/
				//strText = vd.next?.text?.toString()
				strText = vd.text?.toString()
				// strText = vd.next?.text
				if (strText == null || strText == "null" || strText == "<h1>" || strText == "</h1>" || strText == "</H1>" )
				{
					println "strText = '$strText'"
					println "strText = '$strText'"
				}
	 
				if (strText && strText.contains("</"))
					text = "generate error: Error in voicedata element!; possible ending block of h1 as a text voicedata !"
					//clipBegin = vd.next?.clip_begin
				clipBegin = vd.clip_begin
				//clipEnd = vd.next?.clip_end
				clipEnd = vd.clip_end
				//src = vd.next?.smil_file_name
				src = vd.mp3_file_name					// content_src = vd.next?.smil_file_name // +"#" +vd.text_id
				content_src = vd.smil_file_name // +"#" +vd.text_id
				dtb_id = vd.par_id
				text = strText
				if (prev_hlevel_vd && prev_hlevel_vd.iH_level < vd.iH_level)
				{
					bPrev_HLevel_vd_higher_than_this_vd = false
					/*
					def tmp_hmRet = collectNCXPageString(vd, prev_hlevel_vd, null)
					if (tmp_hmRet != null) // not founded
					{
						prev_hlevel_vd = tmp_hmRet.get("prev_hlevel_vd")
						tmp_bPrev_HLevel_vd_higher_than_this_vd = tmp_hmRet.get("bPrev_HLevel_vd_higher_than_this_vd")
						tmp_page_sb = tmp_hmRet.get("page_sb")
						if (tmp_page_sb)
							 page_sb << tmp_page_sb.toString()
						tmp_nav_sb = tmp_hmRet.get("nav_sb")
						if (tmp_nav_sb)
						{
						  if (tmp_bPrev_HLevel_vd_higher_than_this_vd)
						  {
							  def tmp_nav_sb2 = nav_sb.toString().replace("\$navPoint", tmp_nav_sb.toString())
							  nav_sb.delete(0, nav_sb.length()-1)
							  nav_sb << tmp_nav_sb2
						  }
						  else
								 nav_sb << tmp_nav_sb.toString()
					}			
					*/		
				}
				
				if (vd.iPlayOrder == 0)
					vd.iPlayOrder = getPlayOrder()
				playorder = vd.iPlayOrder
				nav_id = "ncx-" +playorder
						
				navPoint = navPoint.toString().replaceAll("\$navPoint", "")
				
				nav_binding = ["strclass":  strclass, "strid": nav_id,	
							"strplayorder": playorder, "text": text,
							"clipBegin": clipBegin, "clipEnd": clipEnd,
							"src": src, "content_src": content_src,
							"dtb_id": dtb_id, /* jätetään siksi kunnes ollaan tultu 
							ylemmälle tasolle, jolloin tiedetään mitä tehdään: */
							"navPoint": "\$navPoint" /* navPoint */ ]
						// if (!nav_templae)
							// nav_templae = engine.createTemplate(navPointTemplate)
				nav_data = nav_template.make(nav_binding).toString()
				local_nav_sb << nav_data
				founded_vd = true
				if (prev_hlevel_vd && prev_hlevel_vd.iH_level > vd.iH_level)
				{
					bPrev_HLevel_vd_higher_than_this_vd = true
				}
				prev_hlevel_vd = vd
			}
			else
			if (Lipsync2Daisy.dtbook_pagenum_on_off != "off" && (vd.xmlText && vd.xmlText.toString().toLowerCase().startsWith("<pagenum ")
				|| (vd.text && vd.text.toString().toLowerCase().startsWith("<pagenum "))) )
			{
				vd = vd.next // koska vastaavaa nextia käytetään generoitaessa smil-tiedoston sisältöä
				// vd.iPlayOrder = getPlayOrder()
				strclass = "pagenum"
				strvalue = vd.text
				if (vd.iPage > -1)
					strvalue = "" +vd.iPage
				clipBegin = vd.clip_begin
				if (!clipBegin) // sometimes can be empty
				{
					clipBegin = VoiceData.getClipTime(vd.start)
					vd.clip_begin = clipBegin
				}
				clipEnd = vd.clip_end
				if (!clipEnd)
				{
					clipEnd = VoiceData.getClipTime(vd.end)
					vd.clip_begin = clipEnd
				}

				src = vd.mp3_file_name
				content_src = vd.smil_file_name // +"#" +vd.text_id
				dtb_id = vd.par_id
	
				if (vd.iPlayOrder == 0)
					vd.iPlayOrder = getPlayOrder()
				playorder = vd.iPlayOrder
				strid = "ncx-" +playorder
				strplayOrder = playorder
				nav_id = "ncx-" +playorder
				Lipsync2Daisy.totalpage++

				// Lipsync2Smil.maxpage++
				page_binding = ["strid":  strid, "strplayOrder": strplayOrder,
					"strplayorder": strplayOrder, "strvalue": strvalue,
					"clipBegin": clipBegin, "clipEnd": clipEnd,
					"src": src, "content_src": content_src,
					"dtb_id": dtb_id
					 ]				
				page_data = page_template.make(page_binding).toString()
				if (page_data /* && page_data.length() > 0 */)
					Lipsync2Daisy.ncx_total_pages++
				page_sb << page_data
				founded_vd = true
			}
			else
			if (vd.iPage == -1 /* ei ole pagenum */ && vd.text && vd.text.toString().toLowerCase().startsWith("<") )
			{
				def elementname = vd.text.toString().split(" ")[0].toString().substring(1)
				if (elementname && elementname in listNCXCustomTestElements)
				{
					// Lipsync2Smil.maxpage++
					// vd.iPlayOrder = getPlayOrder()
					strclass = elementname
					strvalue = vd.text
					clipBegin = vd.clip_begin
					if (!clipBegin) // sometimes can be empty
					{
						clipBegin = VoiceData.getClipTime(vd.start)
						vd.clip_begin = clipBegin
					}
					clipEnd = vd.clip_end
					if (!clipEnd)
					{
						clipEnd = VoiceData.getClipTime(vd.end)
						vd.clip_begin = clipEnd
					}

					src = vd.mp3_file_name
					content_src = vd.smil_file_name // +"#" +vd.text_id
					dtb_id = vd.par_id
		
					if (vd.iPlayOrder == 0)
						vd.iPlayOrder = getPlayOrder()
					playorder = vd.iPlayOrder
					strid = "ncx-" +playorder
					strplayOrder = playorder
					nav_id = "ncx-" +playorder
					// text = vd.text
	
					/*
					page_binding = ["strid":  strid, "strplayOrder": strplayOrder,
						"strplayorder": strplayOrder, "strvalue": strvalue,
						"clipBegin": clipBegin, "clipEnd": clipEnd,
						"src": src, "content_src": content_src,
						"dtb_id": dtb_id
						 ]
					page_data = page_template.make(page_binding).toString()
					page_sb << page_data
					*/
					navPoint = navPoint.toString().replaceAll("\$navPoint", "")
					
					strclass = elementname
					nav_binding = ["strclass":  strclass, "strid": nav_id,
						"strplayorder": playorder, "text": text,
						"clipBegin": clipBegin, "clipEnd": clipEnd,
						"src": src, "content_src": content_src,
						"dtb_id": dtb_id, /* jätetään siksi kunnes ollaan tultu
						ylemmälle tasolle, jolloin tiedetään mitä tehdään: */
						"navPoint": "\$navPoint" /* navPoint */ ]
					// if (!nav_templae)
						// nav_templae = engine.createTemplate(navPointTemplate)
					nav_data = nav_template.make(nav_binding).toString()
					local_nav_sb << nav_data
					founded_vd = true
				}
			}		
		}
		
		if (!founded_vd)
			return null
			
		def hmRet = [:]
		hmRet.put "prev_hlevel_vd", prev_hlevel_vd
		// hmRet.put "page_sb", page_sb
		hmRet.put "nav_sb", local_nav_sb.toString().replaceAll("\$navPoint", "")
		hmRet.put "bPrev_HLevel_vd_higher_than_this_vd", bPrev_HLevel_vd_higher_than_this_vd
		hmRet
	}
	
  /**
   * Palautetaan korvattu VD-puurakenteesta navpoint mjono:
   * 
   * @param vdtreenode
   * @return
   */
  def String getNavPointValueOfVDTreeNode(VDTreeNode vdtreenode)
  { 
	 StringBuffer sb = new StringBuffer ()
	 String data
	 for (VDTreeNode item in vdtreenode.children)
	 {
		 if (item == null)
		 	continue
		 data = item.data
		 if (data == null)
		 	continue
		 if (item.children.size() == 0)
		 	sb << data.replace("\$navPoint","")
		 else
		 	sb << getNavPointValueOfVDTreeNode(item)
	 } 
	 data = vdtreenode.data	 
	 data.replace("\$navPoint", sb.toString())
  }
  
  def VoiceData getVdOf(xmltest_id)
  {
	 ncxitemList.get xmltest_id
  }
  
  def List getChildrenOf(xmltest_item)
  {
	  if (xmltest_item == null)
	  	  return null
			
	  def ret = null
	  def tmp = xmltest_item.depthFirst().findAll { it.@xmltest_id.text() != "" }
	  if (tmp == null || tmp.size() < 1 )
	  	return null

	  def list = []
	  def vdtreenode
	  
	  for(item in tmp)
	  {
		  if (item == null)
		  	continue
		  if (item.@xmltest_id.text() == xmltest_item.@xmltest_id.text())
		  		break
		  vdtreenode = new VDTreeNode()
		  // def item = ["vd": prev_hlevel_vd, "navPoint": tmp_nav_sb.toString().replaceAll("\$navPoint", "")]
		  vdtreenode.vd = getVdOf(item.@xmltest_id.text())
		  vdtreenode.prev_vd = vdtreenode.vd.previous
		  // vdtreenode.data = data2
		  vdtreenode.playorder = vdtreenode.vd.iPlayOrder
		  vdtreenode.children = getChildrenOf(item)
		  list.add vdtreenode
	  }
	  
	  if (list == null || list.size() == 0)
	  	return null
	  list
  }
  
  def showHeapSize()
  {
	  StringBuffer sb = new StringBuffer() 
	  sb <<  "=================================================================\n"
	  // Get current size of heap in bytes
	  long heapSize = Runtime.getRuntime().totalMemory();
	  sb <<  "Heapsize " +heapSize +"\n"
	  
	  // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
	  long heapMaxSize = Runtime.getRuntime().maxMemory();
	  sb <<  "heapMaxSize " +heapMaxSize +"\n"
	  
	   // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
	  long heapFreeSize = Runtime.getRuntime().freeMemory();
	  sb <<  "heapFreeSize " +heapFreeSize +"\n"
	  
	  int mb = 1024*1024;
	  
	 //Getting the runtime reference from system
	 Runtime runtime = Runtime.getRuntime();
	  
	 sb << ("##### Heap utilization statistics [MB] #####")  +"\n"
	  
	 //Print used memory
	 sb << ("Used Memory (mb):"
		 + (runtime.totalMemory() - runtime.freeMemory()) / mb) +"\n";

	 //Print free memory
	 sb << ("Free Memory (mb):"
		 + runtime.freeMemory() / mb) +"\n";
	  
	 //Print total available memory
	 sb << ("Total Memory (mb):" + runtime.totalMemory() / mb) +"\n";

	 //Print Maximum available memory
	 sb << ("Max Memory (mb):" + runtime.maxMemory() / mb) +"\n";

	 sb <<  "=================================================================\n"
	 
	 info(sb.toString())
	 
  }
  
  def List getVdTreeListAfterXml()
  {	  
	  info "\nbefore: getVdTreeListAfterXml()" 
	  showHeapSize()
	  
	  def bDeleteFile = true
	  def fileName = "VdTreeXml.xml"
	  generateDtbookFileAfterTemplate(dtbook_templatefilename, bDeleteFile, fileName)
	  def fTest = new File(getUserHome() +File.separator +fileName )
	  try {
		  def strNCXMarkedDtbookXml = fTest.getText("UTF-8") // this.getDtbookxmlrows(true)
		  /*
		  fTest = new File(strOutputDir +File.separator + "esimies_talouden_johtajana_julkishallinnossa.xml")
		  strNCXMarkedDtbookXml = fTest.getText("UTF-8") // 
		  
		  if (fTest.exists())
		  	fTest.delete()
		  def strNCXMarkedDtbookXml = this.getDtbookxmlrows(true)
	      // fTest.setText(strNCXMarkedDtbookXml, "UTF-8")
	       * 
	       */
		  
		  strNCXMarkedDtbookXml += "\n</dtbook>\n"
		  
		  /*
		  def fKoe1 = new File("koe1.txt")
		  if (fKoe1.exists())
			  fKoe1.delete()
		  fKoe1.setText(strNCXMarkedDtbookXml)
		  */
		  
		  def validating = false        // default is false
		  def namespaceAware = false   // default is true

		  def parseddata = null 
		  try {
		  // def data = new XmlParser(validating, namespaceAware).parseText(xml) // sax parse
		  def parser = new XmlSlurper(validating, namespaceAware)
		  parser.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", false);
		  parser.setEntityResolver(cr);	  
		  	parseddata = parser.parse(new StringReader(strNCXMarkedDtbookXml)) //sax parser
		  } catch(Exception e){
		    this.LOGGER.severe("XmlParser error (vdtree): " +e.getMessage())
		    severe(e)
		    // fix xml data after validation xml error with tagsoup xml parser:
		  	org.ccil.cowan.tagsoup.Parser tagsoupParser = new org.ccil.cowan.tagsoup.Parser();
			parseddata = new XmlSlurper(tagsoupParser).parseText(strNCXMarkedDtbookXml)
	 	  }
		  def book = parseddata.book
		  def bookchildren = book.'*' // depthFirst().findAll{ it.@xmltest_id }
		  def xmltest_item_children
		  
		  def vdtreelist = []
		  hmVdlist = [:]
		  def VDTreeNode vdtreenode
		  def prev_vd, vd_book, vdtreenode_book
		  def xmlid2
		  int iTest = 0
		  
		  /*
		  def fKoe = new File("koe2.txt")
		  if (fKoe.exists())
		  	fKoe.delete()
		  */
		  
		  // all vd items into a hash variable for later use, in the next loop:
		  for(VoiceDataFile vdf in listVDFs)
		  {
			  for(VoiceData vd in vdf.listitems)
			  {
				  xmlid2 = getVDTreeHashValue(vd)
				  if (xmlid2 == null)
				  	throw new Exception("getVDTreeHashValue(vd) returned null: " +vd.toString())
				  // fKoe.append(xmlid2 +"\n")
				  hmVdlist.put(xmlid2, vd)
				  iTest++
			  }
		  }
		  
		  // println "after xml iTest=" +iTest
		  // println "after bookchildren=" +bookchildren.size()
		  
		  // set order of prev and next VoiceData and isNCXItem flag:
		  
		  // find xml book item and correspond vd item for highest vdtree:
		  def bBreakFor = false
		  for(VoiceDataFile vdf in listVDFs)
		  {
			  for(VoiceData vd in vdf.listitems)
			  {
				  if (vd.isXmlMarkPunkt && vd.text.startsWith("<book"))
				  {
		 			   vdtreenode = new VDTreeNode()			   
					   vdtreenode.vd = vd
					   vdtreenode.xmlnode = book
					   vd_book = vd	
					   vdtreenode_book = vdtreenode				
					   bBreakFor = true
					   prev_vd = vd
					   break		   
				  }
			  }
			  if (bBreakFor)
			  	 break
		  }
		  
		  // build vdtee hiearachy for coming generating ncx data (afyer vd items):
		  for(bc in bookchildren)
		  {
			 // println bc
			  vdtreenode = new VDTreeNode()
			  vdtreenode.xmlnode = bc
			  xmlid2 = bc.@xmltest2_id
			  if (xmlid2)
			  {
			  	 def hmitem = hmVdlist.get(xmlid2.toString())
				 if (hmitem)
				 	vdtreenode.vd = hmitem
			  }
				  
			  vdtreenode = addChildVdTrees(vdtreenode, bc)
			  if (vdtreenode != null)
			  {
				  vdtreenode.xmlnode = null
			  	  vdtreenode_book.children.add vdtreenode
			  }
			  else
			  {
			  	println "vdtreenode = addChildVdTrees(vdtreenode, bc) is null"
			  }
		  }
		  
		  hmVdlist = null
		  if (vdtreenode_book)
		  {
			 vdtreelist.add vdtreenode_book
		  }
			  
		  info "after: getVdTreeListAfterXml()"
		  showHeapSize()	  
		  
		  vdtreelist
	  } catch(Exception e){
	    if (!fTest.exists())
			println getMessages().getString(Lipsync2Daisy.constUI_ui_file_does_not_exist) + ": " +fTest
		else
			println "Error in file: " +fTest
	  	throw e
	  }
  }
  
  def static String getVDTreeHashValue(VoiceData vd)
  {
	  vd.strReadedFileName.replaceAll("\\\\", "/") +"/" +vd.iLine
  }
  
  def VDTreeNode addChildVdTrees(VDTreeNode vdtreenode, bc)
  {
	  // if (bc instanceOf )
	  
	  def children = bc.'*'
	  int siz = children.size()
	  if (siz > 0)
	  {
		  // if (vdtreenode?.vd?.iH_level > 0)
		  	// println("ddd1")

		  def xmlid2 
		  def newvdtreenode, foundedvd
		  for(item in children)
		  {
			  // if (item.name() == "text")
			  xmlid2 = item.@xmltest2_id
			  if (xmlid2 && xmlid2.toString())
			  {
				  foundedvd = hmVdlist.get(xmlid2.toString())
				  if (foundedvd == null)
				  {
					  // println("") 
				  	  continue
				  }
				  newvdtreenode = new VDTreeNode()
				  newvdtreenode.xmlnode = item				  
		  	 	  newvdtreenode.vd = foundedvd 
				  // if (foundedvd.iH_level > 0)
					 //  println("foundedvd")
				  newvdtreenode = addChildVdTrees(newvdtreenode, item)
				  newvdtreenode.xmlnode = null
				  vdtreenode.children.add newvdtreenode
			  }
		  }
	  }
	  vdtreenode
  }
  
  def private collectVoiceTreeNodeNCXPageString(VDTreeNode vdtree, VoiceData prev_hlevel_vd)
  {
	  if (vdtree == null)
	  	  return null
	  if (vdtree.vd == null)
	  	 return vdtree		  
	  if (vdtree.xmlnode == null)
	  	return vdtree
		  
	  def map = [:]
	  def list = [], tmp_chi, local_nav_sb = new StringBuffer ()
	  def local_page_sb = new StringBuffer ()
	  int ichildren = vdtree.children.size()
	  
	  for(chi in vdtree.children)
	  {
		  chi = collectVoiceTreeNodeNCXPageString(chi, null)
		  /*
		  if (map.size())
		  {
			  chi = map.get "VDTreeNode"
			  local_nav_sb << map.get("nav_sb")
			  // local_page_sb << map.get("page_sb")
			  
			  list.add chi 
		  }
		  */
		  list.add chi
	  }
	  vdtree.children = list
	  
	  map = [:]
	  
	  if (vdtree.vd.isNCXItem) // bNcxMarkOn
	  {
		  println "bNcxMarkOn"
	  }

	  /*	  
	  // map.put "prev_hlevel_vd", prev_hlevel_vd
	  // map.put "page_sb", page_sb
	  map.put "nav_sb", local_nav_sb.toString().replaceAll("\$navPoint", "")
	  // map.put "page_sb", local_page_sb.toString()
	  // map.put "bPrev_HLevel_vd_higher_than_this_vd", bPrev_HLevel_vd_higher_than_this_vd
	  map.put("VDTreeNode", vdtree)
	  map
	  */
	  vdtree
  }
  
  /**
   * generoidaan .ncx tiedosto
   * 
   * @param filename
   * @param bDeleteFile
   */
  def private void generateNcxFileAfterTemplate(String filename, boolean bDeleteFile)
  {
	 println "\ngenerateNcxFileAfterTemplate(" +filename +")"
	 println "(" +getMessages().getString(Lipsync2Daisy.constUI_ui_moment) +"...)\n"
	   
	 Lipsync2Daisy.ncx_total_pages = 0
	 
	 StringBuffer sb = new StringBuffer()
	 String strTemplate = readFromDaisy3TemplateDirFile(filename)	 
	 def iCnt = 0
	 def template, binding	 
	 String strMetaTemplate = readFromDaisy3TemplateDirFile(speechgen_ncx_meta_tmpl_file_name)
	 def speechgen_ncx_meta	= getspeechgen_opf_meta()
	 
	 // TODO: mm doctitle_audio
	 def doctitle_audio = getDoctitleAudio(vdTitle)
	 def docauthor = "" // getDoctitleAuthors(listAuthors)
	 def navPoints = "\$navPoints"
	 def docauthor_audio = getDocAuthorsAudio()
	 // <audio clipBegin="0:00:00" clipEnd="0:00:04.557" src="speechgen0001.mp3"/>
	 	  
	 if (docauthor == null)
	 	docauthor = ""
	 if (navPoints == null)
		 navPoints = ""
	 if (doctitle_audio == null)
		 doctitle_audio = ""	
	 if (docauthor_audio == null)
		 docauthor_audio = ""			 

	 nav_sb = new StringBuffer()
	 page_sb = new StringBuffer()
	 collectNCXPageList = [] //
	
	 String navPointTemplate = readFromDaisy3TemplateDirFile("navpoint.tmpl")
	 String pageTemplate = readFromDaisy3TemplateDirFile("pagetarget.ncx.tmpl")
	 def playorder, text, clipBegin, clipEnd, src, content_src
	 def page_template, page_data
	 def strid, strplayOrder, strvalue  
	 def strText, match
	 String strTemp

	 def newlist
	 def VoiceData prev = null, prev_hlevel_vd
	 def new_listVDFs = []
	 def prev_added = false
	 
	 ncxitemList = [:]
	 
	 def iTest = 0
	 
	 // set order of prev and next VoiceData and isNCXItem flag: 
	 for(VoiceDataFile vdf in listVDFs)
	 {
		 newlist = []
		 for(VoiceData vd in vdf.listitems)
		 {	
			 vd.isNCXItem = isNCXItem(vd)
				 
			 prev_added = false
			 if (vd.isNCXItem)
			 {
			 	// println vd
				ncxitemList.put vd.content_id, vd
			 }
			 vd.previous = prev
			 if (prev)
			 {
				 prev.next = vd
			 	 newlist.add prev
				 prev_added = true
			 }
			 prev = vd
			 iTest++
		 }
		 prev.next = null
		 if (!prev_added)
		 	newlist.add prev
		 vdf.listitems = newlist
		 new_listVDFs.add vdf
	 }
	 listVDFs = new_listVDFs
	 
	 def data2, vdtreelist = []
	 
	 // println "iTest vd-items: " +iTest
	 // println "listVDFs -items: " +listVDFs.size()
	 iTest = 0
	 	 
	 vdtreelist = getVdTreeListAfterXml()
	 ncxitemList = null
	 // return	 
	 
	 for(item in vdtreelist)
	 {
		 iTest += item.countitems()
	 }
	 // logger.info "vdtreelist -items: " +iTest

	 def pagetarget = ""
	 def params

	 if (!nav_template)
	 	nav_template = engine.createTemplate(navPointTemplate)
	 if (!page_template)
	 	page_template = engine.createTemplate(pageTemplate)

		 /*
	 // kokoa collectNCXPageList listaan vd ja sitä vastaava mjono parit:
	 for(VoiceDataFile vdf in listVDFs)
	 {
		 // println vdf.file
	 	Lipsync2Smil.currentxmlfilenameprinted = false
		Lipsync2Smil.currentxmlfilename = vdf.file.toString()
	
		 currentVoiceDataFile = vdf
		 iCnt++
	 
		 params = collectNCXPageString(vdf, prev_hlevel_vd, ?* nav_sb.toString() *? null)
		 if (params == null)
		 	continue
		 // page_sb << params.get("page_sb")
		 // nav_sb << params.get("nav_sb")
		 prev_hlevel_vd = params.get("prev_hlevel_vd")
	 }
  */
	
	 if (bExecuteGarbageCollector)
	 {	 
		 System.gc()
		 // Thread.sleep(5000)
	 }
	 
	 // kokoa collectNCXPageList listaan vd ja sitä vastaava mjono parit:
	 def newvdtreelist = []
	 for(VDTreeNode vdtree in vdtreelist)
	 {
		 // println vdf.file
	 	Lipsync2Daisy.currentxmlfilenameprinted = false
		Lipsync2Daisy.currentxmlfilename = vdtree.vd.strReadedFileName
	
		 iCnt++
	 
		 vdtree.setLocalVoiceTreeNodeNCXPageString()
		 if (vdtree.page_sb)
		 	page_sb << vdtree.page_sb
		 // if (vdtree.nav_sb)
		 	// nav_sb << vdtree.nav_sb
		 // prev_hlevel_vd = params.get("prev_hlevel_vd")
		newvdtreelist.add vdtree
	 }
	 
	 vdtreelist = newvdtreelist
	 newvdtreelist = []
	 iCnt = 0
	 
	 for(VDTreeNode vdtree in vdtreelist)
	 {
		 // println vdf.file
		Lipsync2Daisy.currentxmlfilenameprinted = false
		Lipsync2Daisy.currentxmlfilename = vdtree.vd.strReadedFileName
	
		 iCnt++
	 
		 vdtree.correctLocalVoiceTreeNodeNCXPageString()
		 newvdtreelist.add vdtree
	 }
	 
	 vdtreelist = newvdtreelist
	 newvdtreelist = []
	 iCnt = 0
	 
	 for(VDTreeNode vdtree in vdtreelist)
	 {
		 // println vdf.file
		Lipsync2Daisy.currentxmlfilenameprinted = false
		Lipsync2Daisy.currentxmlfilename = vdtree.vd.strReadedFileName
	
		 iCnt++
	 
		 vdtree.collectLocalVoiceTreeNodeNCXPageString()
		 if (vdtree.nav_sb)
			 nav_sb << vdtree.nav_sb.toString().replaceAll("\$navPoint", "")
		 newvdtreelist.add vdtree
	 }
	 
	 /*
	 def fTest = new File("koe5.txt")
	 fTest.setText(nav_sb.toString())
	 */
	 vdtreelist = newvdtreelist
	 
	 iCnt = 0	 
	 // update vd and vf lists with ncx flags after collectNCXPageList
	 def updateFileList = [], updatevdlist 
	 def ncxvd
	 
	 /*
	 for(VoiceDataFile vf in listVDFs)
	 {
		 for(VoiceData vd in vf.listitems)
		 {
			 updatevdlist = []
			 for(itemi in collectNCXPageList)
			 {
				 ncxvd = item.get("vd")
				 if (vd == ncxvd)
				 {
				 	vd.isNCXItem = true
				 }
			 }
			 updatevdlist.add vd
		 }
		 vf.listitems = updatevdlist 
	 }
	 listVDFs = updateFileList
	 */
	 
	 
	 // nav_sb datat:
	 def VoiceData item_vd
	 def VoiceData prev_item_vd
	 def prev_item
	 def navPoint = "", prev_navPoint 
	 int iCnt2 = 0
	 	 
	 // kerää ja luo vd-puuhun datat, jotta tiedetään jatkossa mitkä mjonot kuuluvat sisäkkäin:
	 VDTreeNode vdtreenode, vdtreenode_parent, prev_vdtreenode
	 VoiceData vd

	 /* test code:
	 def fkoe = new File("koe35.txt")
	 for(item in collectNCXPageList)
	 {
		 for (v in item.values())
		 {
	 		if (v.toString().contains("playOrder=\"37") || v.toString().contains("playOrder=\"1\"") || v.toString().contains("playOrder=\"35"))
			 {
		 		println "ddd"
			 }
	 		if (v.toString().contains("playOrder="))
			 	fkoe.append v.toString() 
		 }
	 }
	 */	 
	 
	 def prev_vd, last_h1_vdtreenode

	 /*
	  * old loop:
	   for(item in collectNCXPageList)
	 {
		 iCnt2++
		 if (item == null) // ohita
		 	continue
	
		 ?* test code:
		 for (v in item.values())
		 {
	 		if (v.toString().contains("playOrder=\"37") || v.toString().contains("playOrder=\"1\"") || v.toString().contains("playOrder=\"35"))
			 {
		 		println "ddd"
			 }
	 	 }
	 	 *?
		 
		 if (vdtreenode_parent == null)
		 	vdtreenode_parent = last_h1_vdtreenode

		 vd = item.get("vd")
		 data2 = item.get("navPoint")		

		 vdtreenode = new VDTreeNode()
		 // def item = ["vd": prev_hlevel_vd, "navPoint": tmp_nav_sb.toString().replaceAll("\$navPoint", "")]
		 vdtreenode.vd = vd
		 vdtreenode.prev_vd = prev_vd
		 vdtreenode.data = data2
		 vdtreenode.playorder = vd.iPlayOrder
		 vdtreenode.prev = vdtreenode_parent?.last_children
		 
		 if (Lipsync2Smil.dtblevel < vd.iH_level)
		 	Lipsync2Smil.dtblevel = vd.iH_level 
			 
		 if (vd.iH_level == 1)
		 {
			 if (vdtreenode_parent)
			 {
				 // check if current parent is at 1. level. If not, return highist parent:
				 while(vdtreenode_parent.parent != null && vdtreenode_parent.vd.iH_level != 1)
				 	vdtreenode_parent = vdtreenode_parent.parent
			
				vdtreenode_parent.noaddtreenodelist = false
			 	vdtreelist.add vdtreenode_parent
			 }
			 last_h1_vdtreenode = vdtreenode
			 vdtreenode_parent = vdtreenode			 
		 }
		 else
		 {		
			 def bAddtreenodelist = true
			 if (vdtreenode_parent && vdtreenode_parent.vd.iH_level == (vd.iH_level +1)) // vd is one lower level than parent
			 {
				 vdtreenode.parent = vdtreenode_parent // tell which is parent
			 }
			 else
			 if (vdtreenode_parent && vdtreenode_parent.vd.iH_level <= vd.iH_level) // parent has same level than vd, get one higther parent
			 {
				 // check if current parent is at lower or same level. If not, return suitable parent:
				 while(vdtreenode_parent.parent != null && vdtreenode_parent.vd.iH_level != 1 && vdtreenode_parent.vd.iH_level <= vd.iH_level)
					 vdtreenode_parent = vdtreenode_parent.parent
				vdtreenode.parent = vdtreenode_parent // tell which is parent
			 }
			 else
			 if (vdtreenode_parent && vdtreenode_parent.vd.iH_level > vd.iH_level)
			 {
				 if (vdtreenode_parent == null)
					 throw new Exception("Otsake ei ala h1:llä!" +vd)
				?*	 
				 if (vd.iH_level > (vdtreenode_parent.vd.iH_level +1))
				 {
					 vdtreenode_parent = prev_vdtreenode
				 // throw new Exception("Otsake h$vd.iH_level on liian suuri edellisenä (h$vdtreenode_parent.vd.iH_level) tulevaan!")
				 }
				 *?
					 // check if current parent is at lower or same level. If not, return suitable parent:
				 while(vdtreenode_parent.parent != null && vdtreenode_parent.vd.iH_level > (vd.iH_level+1))
						 vdtreenode_parent = vdtreenode_parent.last_children
					vdtreenode.parent = vdtreenode_parent // tell which is parent
			
				 vdtreenode.parent = vdtreenode_parent
			 }
			 else // nykyinen ei ole suurempi tasoinen tai sama
			 {
				 if (vdtreenode_parent)
				 {
					 while(vdtreenode_parent.parent != null && vdtreenode_parent.vd.iH_level >= vd.iH_level)
					 	vdtreenode_parent = vdtreenode_parent.parent
					?*
					while(vdtreenode_parent != null && vdtreenode_parent.parent != null 
						 && vdtreenode_parent.vd.iH_level != 1 && vdtreenode_parent.vd.iH_level >= vdtreenode.vd.iH_level)
					 	vdtreenode_parent = vdtreenode_parent.parent
					 *?
					if (vdtreenode_parent == null)
						vdtreenode_parent = vdtreenode
					else
					{
						if (vdtreenode_parent.parent != null && vdtreenode_parent.vd.iH_level == vdtreenode.vd.iH_level)
						{
							vdtreenode_parent = vdtreenode_parent.parent
						}
						
						if (vdtreenode_parent.vd.iH_level == 1 && vdtreenode.vd.iH_level == 1)
						 {
							vdtreenode_parent.noaddtreenodelist = false
						 	vdtreelist.add vdtreenode_parent
	  					    vdtreenode_parent = vdtreenode
							bAddtreenodelist = false
						 }
					}							
				 }
			 }
			 if (vdtreenode_parent && bAddtreenodelist)  // vd is one of a parent's children
			 {
			 	vdtreenode_parent.children.add vdtreenode
				vdtreenode_parent.last_children = vdtreenode
			 }
		 }
		 prev_vdtreenode = vdtreenode // a current treenode is next previous
		 prev_vd = vd // a current vd is next previous vd
	 }  // end of for loop	 
	  */
	 
	 /*
	 if (vdtreenode_parent.noaddtreenodelist)
	 	vdtreelist.add vdtreenode_parent
	*/ 
		 /* test code:
		 // korvataan $navpoint alemman tason hierarkian $navpoint arvolla:
		 def fkoe3 = new File("koe35_2.txt")
		 for(VDTreeNode item in vdtreelist)
		 {
			 test35(item)
			 fkoe3.append item.data
			 if (item.children.size() > 0)
			 {
				 fkoe3.append "---"
				 fkoe3.append item.children*.toString()
				 fkoe3.append "==="
			 }
		 }
		 */
		 
	 /*
	 // korvataan $navpoint alemman tason hierarkian $navpoint arvolla:
	 for(VDTreeNode item in vdtreelist)
	 {
		 ?* test code:
		 if (item.data.toString().contains("playOrder=\"37") || item.data.toString().contains("playOrder=\"1\"") || item.data.toString().contains("playOrder=\"35"))
		 {
				println "ddd"
		 }
		 *?
		 nav_sb << getNavPointValueOfVDTreeNode(item)
	 }
     */
				
	 /*
	 def new_collectNCXPageList = []
	 for(item in collectNCXPageList.reverse())
	 {
		 iCnt2++
		 if (prev_item == null) // lopusta ensimmäinen eli viimeinen
		 {
			 item_vd  = item.get "vd"
			 navPoint = item.get "navPoint"
			 navPoint = navPoint.replace("\$navPoint", "")
			 def tmp_item = ["vd": item_vd, "navPoint": navPoint.toString()]
			 item = tmp_item
			 // nav_sb << navPoint.toString()
			 // new_collectNCXPageList.add tmp_item 
		 }
		 else
		 { // viimeisestä ylöspäin:
			 // if (iCnt2 != 2) // viimeinen on jo lisätty
			 // {
				 item_vd = item.get "vd"
				 navPoint  = item.get "navPoint"
				 prev_item_vd = prev_item.get "vd"
				 prev_navPoint  = prev_item.get "navPoint"
				 if (prev_item_vd.iH_level <= item_vd.iH_level)
				 {
					 navPoint = navPoint.replace("\$navPoint", "")
		 			 def tmp_item = ["vd": item_vd, "navPoint": navPoint.toString()]
					 item = tmp_item
					  ?*
					 def tmpstr = nav_sb.toString() 
					 if (nav_sb.length() > 0)
					 	nav_sb.delete(0, nav_sb.length()-1)
					 tmpstr = navPoint.toString() +tmpstr
					 nav_sb << tmpstr
					 *?
					 // new_collectNCXPageList.add tmp_item					 
					 new_collectNCXPageList.add prev_item
				 }	
				 else
				 {
					 navPoint = navPoint.replace("\$navPoint", prev_navPoint)
		 			 def tmp_item = ["vd": prev_item_vd, "navPoint": navPoint.toString()]
					 item = tmp_item
					 ?*
					 def tmpstr = nav_sb.toString() 
					 if (nav_sb.length() > 0)
					 	nav_sb.delete(0, nav_sb.length()-1)
					 tmpstr = navPoint.toString() +tmpstr
					 nav_sb << tmpstr
					 *?
					 // new_collectNCXPageList.add tmp_item
				 }				 
			// }
		 }
		 prev_item = item
	 }
	 new_collectNCXPageList.add prev_item
	 
	 // navPoint = prev_item.get "navPoint"
	 // nav_sb << navPoint
	 
	 iCnt2 = 0 // käännetään takaisin oikeaan järjestykseen + nav_sb täyttö:
	 for(item in new_collectNCXPageList.reverse())
	 {
		 iCnt2++
		 item_vd  = item.get "vd"
		 navPoint = item.get "navPoint"
		 nav_sb << navPoint 
		 prev_item = item
	 }
	 */
	 	 
	 def binding2 = []
	 def customattributes = sbNCXCustomTestElements.toString()
	 def template2 = engine.createTemplate(strMetaTemplate)
	 def bindingtemplate2 = ["dtblevel":  ((VoiceDataFile.depth) < 1 ? 0 : VoiceDataFile.depth +1), "totalpage": Lipsync2Daisy.ncx_total_pages /* (Lipsync2Smil.dtbook_pagenum_on_off == "off" ? 0 : (Lipsync2Smil.totalpage == 0 ? 0 : Lipsync2Smil.totalpage+1)) */ /* Lipsync2Smil.maxpage */, 
		 "maxpage": (dtbook_pagenum_on_off == "on" ? Lipsync2Daisy.maxpage : 0) /* Lipsync2Smil.totalpage */, "customattributes" : customattributes
		  ] 
	 def metadata = template2.make(bindingtemplate2).toString() // template2.make(binding2.toString())

	 /* test code:
	 if (nav_sb.toString().contains("playOrder=\"35"))
	 {
			println "ddd"
	 }
	 */
	 def ncx_doctitle = doctitle.toString().replace("</sent>", "").replace("</SENT>", "").replaceAll("<sent.*?>","").replaceAll("<SENT.*?>","")
	 // bind and generate highest .tpml file:
	 binding = ["speechgen_ncx_meta":  speechgen_ncx_meta +"\n" +metadata, "doctitle": ncx_doctitle,
		 "doctitle_audio": doctitle_audio, "docauthor": docauthor,
		 "docauthor_audio": docauthor_audio, "navPoints": nav_sb.toString().replaceAll("\$navPoint", ""),
		 "pageLists": (Lipsync2Daisy.dtbook_pagenum_on_off == "off" ? "" : "<pageList id=\"page\">\n" +page_sb.toString() +"\n" +"</pageList>\n"),
		 "totalpage": Lipsync2Daisy.totalpage,
		 "docauthors": docauthors.toString()
		  ]
	 if (!template)
		 template = engine.createTemplate(strTemplate)
	 def data = template.make(binding).toString()
	 // if (Lipsync2Smil.dtbook_pagenum_on_off == "off")
	 	// data = data.replaceAll("<pageList id=\"page\">.*?</pageList>", "")
	 		
	 writeIntoOutputDirFile(filename.replace(".tmpl", ""), data.toString().replaceAll("\\\$navPoint", ""), bDeleteFile)
  }
  
  /**
   * "test" method
   *  
   * @param item
   * @return
   */
  def test35(item)
  {
	  println item.playorder 
	  if (item.playorder == 35)
	  	 println "ddd"
	  else
	  {
		  if(item.children != null && item.children.size() > 0)
		  {
			  for( vtree in item.children)
			  {
				  test35(vtree)
			  }
		  }
	  }
  }

  /**
   * generoidaan daisy 3 tiedotot tulostushakemistoon
   */
  def private void convertLipsync2SmilContentAfterDaisy3_old()
  {
	  
	  println "\nAloitetaan konvertointi Daisy 3 smil:iin..."
	  
	  VoiceDataDaisy3.dtbooksmilreftemplate = readFromDaisy3TemplateDirFile(dtbooksmilreftemplatefile)
	  
	  if (!VoiceDataDaisy3.dtbooksmilreftemplate)
	  	throw new Exception("cfg dtbooksmilreftemplate: dtbook.smilref.templatefile tiedoston nimi ja tiedosto ei saa olla tyhjä!")
		  
	  if (!templatefiles)
	  	throw new Exception("cfg muuttujassa oltava arvoja: templatefiles!")

	  voiceObjectMapValues = [:]
	  
	  StringBuffer sbContent = new StringBuffer ()
	  StringBuffer sbContentMeta = new StringBuffer ()
	  StringBuffer sbNcc = new StringBuffer ()
	  StringBuffer sbCss= new StringBuffer ()
	  StringBuffer sbSmil
	  File fSmil
	  
	  // alusta template tekstit:
	  /*
	  VoiceDataFile.strSmilTemplate = strSmilTemplate
	  VoiceDataFile.strH1Template = strH1Template
	  VoiceDataFile.strSeqTemplate = strSeqTemplate
	  VoiceDataFile.content_file_name = content_file_name
	  VoiceData.strParTemplate = strParTemplate
	  VoiceData.page_lipsync_time_on_off = page_lipsync_time_on_off
	  def cnstTimeFormatter = new DecimalFormat("##################0.000")
	  cnstTimeFormatter.setRoundingMode(RoundingMode.UNNECESSARY)
	  VoiceData.cnstTimeFormatter = cnstTimeFormatter
	  VoiceDataFile.dc_identifier  = dc_identifier
	  VoiceDataFile.dc_title = dc_title
	  VoiceDataFile.region_id = region_id
	  VoiceDataFile.register = this
	  VoiceData.register = this
	  */
	  int iCnt = 0
	  
	  def time_together = 0.0
	  	  
	  tocItems = 0
	  // merkataan mitka voicedata xml instanssit aloittaavat ja lopettavat
	  // daisy3 lauseet. Eli merkataan lauseita aloittavat lopettavat xml docbook
	  // elementit:
			
	  def list = [], newlistVDFs = []
	  VoiceData prev_vd
	  def isDaisy3_sentence_begin_founded = false
	  def daisy3sentencesCanStart = false
	  old_totaltime = 0.0
	  VoiceDataFile prev_vdf = null
	  def firstTime = true
	  
	  for(VoiceDataFile vdf in listVDFs)
	  {
		  // println vdf.file
		  Lipsync2Daisy.currentxmlfilenameprinted = false
		  Lipsync2Daisy.currentxmlfilename = vdf.file.toString()
	  
		  vdf.countDuration(firstTime,(prev_vdf == null ? 0.0 : prev_vdf.totaltime) )
		  totaltime = vdf.end // - prev_totaltime // end - start
		  old_totaltime += vdf.old_totaltime
		  totaltime += vdf.totaltime
		  currentVoiceDataFile = vdf
		  // currentVoiceDataFile.convert2Smil()
		  iCnt++
		  /*
		  if (!vdf.mp3_file_name)
		  {
			 vdf.mp3_file_name = getNextSmilMp3FileName()
			 vdf.smil_file_name   = getCurrentSmilFileName()
			 vdf.bSentenceWordMode  = bSentenceWordMode
		  }
		  */
		  
		  // if (doctitle_on_off && iCnt == 1)
			  // currentVoiceDataFile.isDocTitle = true
		  // vdf.setBaseValuesOfConversion()
		  list = []
		  isDaisy3_sentence_begin_founded = false
		  def afterWordsHasSeveralSentencies = false, hasMixedSeveralSentencies = false 
		  prev_vd = null
		  
		  for(VoiceData vd in currentVoiceDataFile.listitems)
		  {
			  vd.generateIdValues()
			  if (!daisy3sentencesCanStart)
			  {
				  if (vd.name == 'xmlmark' 
					  && (vd.text.toString().startsWith('<book') || vd.text.toString().startsWith('<BOOK')))		   
				  	daisy3sentencesCanStart = true
			  }
			  else
			  {
				  if (prev_vd && prev_vd.text)
				  { 
					  if (prev_vd.text.toString().toLowerCase().startsWith("<p>")
						  || prev_vd.text.toString().toLowerCase().startsWith("<p "))
				  	      hasMixedSeveralSentencies = hasHasSeveralSentencies(prev_vd)
					  else
					  if (prev_vd.text.toString().toLowerCase().startsWith("</p>")
						  || prev_vd.text.toString().toLowerCase().startsWith("</p "))
						  hasMixedSeveralSentencies = false
				  }
					  
				  if (!isDaisy3_sentence_begin_founded 
					  && prev_vd && (prev_vd.name == VoiceData.cnstLipsyncXmlmark && vd.name == VoiceData.cnstLipsyncWord && vd.iPage == -1
						  && !prev_vd.text.toString().toLowerCase().startsWith("</pagenum"))
					  || (prev_vd && (prev_vd.xmlText && prev_vd.xmlText.toString().toLowerCase().startsWith("<pagenum")					  
						  || (prev_vd.text && prev_vd.text.toString().toLowerCase().startsWith("<p "))
						  || (prev_vd.text && prev_vd.text.toString().toLowerCase().startsWith("<p>"))))				  
					  /* || (prev_vd.name == VoiceData.cnstLipsyncWord && prev_vd.iPage > -1) */
					  )
				  {
				  	 prev_vd.isDaisy3_sentence_begin = true
					 if (!isDaisy3_sentence_begin_founded
						   && prev_vd.text && (prev_vd.text.toString().toLowerCase().startsWith("<p ")
						  || prev_vd.text.toString().toLowerCase().startsWith("<p>"))
					 	 )
					 {
						 if (hasHasSeveralSentencies(prev_vd))
						 {
							 // hasMixedSeveralSentencies = true
							 isDaisy3_sentence_begin_founded = true
							 // afterWordsHasSeveralSentencies = true
							 // prev_vd.afterWordsHasSeveralSentencies = true
						 }
						 // else
						 	// hasMixedSeveralSentencies = false 
					 }  
					 /*
					 else
					 */
					 if (vd.name == VoiceData.cnstLipsyncWord)
					 {
						 if (!afterWordsHasSeveralSentencies && vd.next 
							 && (vd.next.name == VoiceData.cnstLipsyncWord /* || 
								 (prev_vd.text && (prev_vd.text.toString().toLowerCase().startsWith("<p>")
									 || prev_vd.text.toString().toLowerCase().startsWith("<p ")) 
								  && hasHasSeveralSentencies(prev_vd)) */))
						 {
						 	afterWordsHasSeveralSentencies = true
							vd.afterWordsHasSeveralSentencies = afterWordsHasSeveralSentencies
							prev_vd.afterWordsHasSeveralSentencies = true
						 }
						 else
						 {
						 	prev_vd.afterWordsHasSeveralSentencies = false
							afterWordsHasSeveralSentencies = false
						 }
					 }
					 else
					 {
						if (prev_vd.text && (prev_vd.text.toString().toLowerCase().startsWith("</p ")
						 || prev_vd.text.toString().toLowerCase().startsWith("</p>")) )
					 	afterWordsHasSeveralSentencies = false
					 }
					 
					 if (!isDaisy3_sentence_begin_founded
						   && prev_vd && prev_vd.name == VoiceData.cnstLipsyncXmlmark && vd.name == VoiceData.cnstLipsyncWord && vd.iPage == -1)
					 	isDaisy3_sentence_begin_founded = true
				  }
				  else
				  if (isDaisy3_sentence_begin_founded
					  && prev_vd && (/* prev_vd.name == VoiceData.cnstLipsyncXmlmark || */
						 (/* prev_vd.name == VoiceData.cnstLipsyncPunct && */  vd.isXmlMarkPunkt /* prev_vd.isXmlMarkPunkt */ )))
				  {
				  	vd.isDaisy3_sentence_end = true
				    isDaisy3_sentence_begin_founded = false
				  }
			  }

			  if (afterWordsHasSeveralSentencies && vd.text 
				  && (vd.text.toString().toLowerCase().startsWith("</p ")
				  || vd.text.toString().toLowerCase().startsWith("</p>")) )
			  { 
				  afterWordsHasSeveralSentencies = false
				  hasMixedSeveralSentencies = false
				  vd.afterWordsHasSeveralSentencies = afterWordsHasSeveralSentencies
			  }
			  else
			  if (afterWordsHasSeveralSentencies && /* (isDaisy3_sentence_begin_founded
				   || hasMixedSeveralSentencies )  
				   && */ prev_vd.name == VoiceData.cnstLipsyncWord )
			  	prev_vd.afterWordsHasSeveralSentencies = afterWordsHasSeveralSentencies
			  	  
			  if (prev_vd)
			  	 list.add prev_vd
			  prev_vd = vd
		  }
		  list.add prev_vd
		  
		  vdf.listitems = list
		  firstTime = false
		  newlistVDFs.add(vdf)
	  }
	  listVDFs = newlistVDFs
	  
	  // poista vanhat tulostustiedostot
	  File fOutputDir = new File(this.strOutputDir)
	  if (!fOutputDir.exists())
	  		throw new Exception("Hakemistoa ei ole: " +fOutputDir)
	  if (!fOutputDir.isDirectory())
		  throw new Exception("Ei ole hakemisto: " +fOutputDir)
	  String strFileName
	  
	  println()
	  
	  for(File fOut in fOutputDir.listFiles())
	  {
		  strFileName = fOut.getName().toLowerCase() 
		  if (strFileName.endsWith(".smil") 
			  || strFileName.endsWith(".xml")
			  || strFileName.endsWith(".opf")
			  || strFileName.endsWith(".ncx")
			  )
		  {
			  if (!fOut.delete())
			  	throw new Exception("Ei voitu poistaa tiedostoa: " +fOut)
			 println "Poistettu: " +fOut
		  }
	  }

	  def bDeleteFile = false
	  
	  // for(VoiceDataFile vdf in listVDFs)
	  // {
		 // println vdf.file
	
		  for(String templatefilename in templatefiles.split(" "))
		  {
			  if (!templatefilename)
			  	 continue
			  if (templatefilename.endsWith(".opf.tmpl"))
			  	 generateOptFileAfterTemplate(templatefilename, bDeleteFile)
			  else
			  if (templatefilename.endsWith(".smil.tmpl"))
			  	  generateSmilFilesAfterTemplate(templatefilename, bDeleteFile)
			  else
			  if (templatefilename.endsWith("dtbook.xml.tmpl"))
				  generateDtbookFileAfterTemplate(templatefilename, bDeleteFile)
			  else
			  if (templatefilename.endsWith(".ncx.tmpl"))
				  generateNcxFileAfterTemplate(templatefilename, bDeleteFile)
		  }
	  // }
		  
	  /*
	  voiceObjectMapValues = [:]
	  
	  StringBuffer sbContent = new StringBuffer ()
	  StringBuffer sbContentMeta = new StringBuffer ()
	  StringBuffer sbNcc = new StringBuffer ()
	  StringBuffer sbCss= new StringBuffer ()
	  StringBuffer sbSmil
	  File fSmil
	  
	  // alusta template tekstit:
	  VoiceDataFile.strSmilTemplate = strSmilTemplate
	  VoiceDataFile.strH1Template = strH1Template
	  VoiceDataFile.strSeqTemplate = strSeqTemplate
	  VoiceDataFile.content_file_name = content_file_name
	  VoiceData.strParTemplate = strParTemplate
	  VoiceData.page_lipsync_time_on_off = page_lipsync_time_on_off
	  def cnstTimeFormatter = new DecimalFormat("####0.000")
	  if (executetype == cnstExecuteDaisy2)
	  {
		  cnstTimeFormatter.setRoundingMode(RoundingMode.UNNECESSARY)
		  VoiceData.cnstTimeFormatter = cnstTimeFormatter
	  }
	  else
	  if (executetype == cnstExecuteDaisy3)
	  {
	  	  cnstTimeFormatter = new DecimalFormat("#####:##:#0.000")
	  	  cnstTimeFormatter.setRoundingMode(RoundingMode.UNNECESSARY)
		  VoiceData.cnstTimeFormatter = cnstTimeFormatter
	  }
	  
	  VoiceDataFile.dc_identifier  = dc_identifier
	  VoiceDataFile.dc_title = dc_title
	  VoiceDataFile.region_id = region_id
	  VoiceDataFile.register = this
	  VoiceData.register = this
	  int iCnt = 0
	  
	  def time_together = 0.0
	  
	  tocItems = 0
	  
	  for(VoiceDataFile v in listVDFs)
	  {
		  println v.file
		  currentVoiceDataFile = v
		  iCnt++
		  if (!v.mp3_file_name)
		  {
			   v.mp3_file_name = getNextSmilMp3FileName()
			 v.smil_file_name   = getCurrentSmilFileName()
			 v.bSentenceWordMode  = bSentenceWordMode
		  }
		  
		  // if (doctitle_on_off && iCnt == 1)
			  // currentVoiceDataFile.isDocTitle = true
		  currentVoiceDataFile.setBaseValuesOfConversion()
		  sbContent << currentVoiceDataFile.convert2SmilXhtml()
		  sbContentMeta << currentVoiceDataFile.convertMetaSmilXhtml()
		  sbNcc << currentVoiceDataFile.convert2Ncc()
		  tocItems += currentVoiceDataFile.iNccItems
		  sbSmil = new StringBuffer()
		  sbSmil << currentVoiceDataFile.convert2Smil()
		  fSmil = new File(strOutputDir + File.separator +v.smil_file_name)
		  if (fSmil.exists())
		  {
			   if (fSmil.delete())
			   println "Edellinen " +getMessages().getString(Lipsync2Smil.constUI_ui_file) +fSmil +" poistettu."
		  }
		  if (executetype == cnstExecuteDaisy2)		  
		  		fSmil.append sbSmil.toString(), "UTF-8"
		  time_together += v.totaltime() // time_together
	  }
	  
	 // println "time_together: " +time_together
	  //println "time_together/1000: " +time_together/1000
	  def timeDate = new Date(time_together.toLong())
	  SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss")
	  time_together = (time_together/1000)/60
	  def str_time_together = "" +sdf.format(timeDate);
	  //str_time_together = str_time_together.toString().replace(".",":")
	  //println "str_time_together: " +str_time_together
	  // time_together = str_time_together
	  println()	 	 	  
	  
	  ?* kommmenttikoodi
	  for(entity in daisyIDs.openLipsyncEntity.reverse())
	  {
		  sbContent.append("\n</" + entity +">\n")
	  }
	  *?
	  
	  // jälkikäsittely:
	  def strContent = sbContent.toString()
	  def strMetatext= sbContentMeta.toString()
			
	  def contentbinding = [ "metatext": strMetatext,
						   "bodytext": strContent ]
	  if (!contentbinding)
		  throw new Exception("contentbinding is null!")
		  
	  if (!engine)
		  throw new Exception("engine is null!")
		  
	  if (!strContentTemplate)
		  throw new Exception("strContentTemplate is null!")
		  
	  // if (!contentTemplate)
			  contentTemplate = engine.createTemplate(strContentTemplate)
	  String strContentHtml = contentTemplate.make(contentbinding).toString()

	  ?*
	  if (!strContent.toLowerCase().contains("<!DOCTYPE"))
	  {
		  def search = "<head>"
		  int indHeaderStart = strContent.indexOf(search)
		  if (indHeaderStart > -1)
		  {
			  def before 		= strContent.substring(0, indHeaderStart)
			  def modContent 	= before + "\n" + xhtmlDoctype +"\n"
			  def after 		= strContent.substring(indHeaderStart)
			  if (!after.toLowerCase().contains("<meta http-equiv="))
			  {
				  indHeaderStart 	= after.indexOf(search)
				  before	 	 	= after.substring(indHeaderStart, indHeaderStart+search.length()) +"\n"
				  def modAfter 		= cont+ "\n" + after.substring(indHeaderStart+search.length())
				  after 			= before +modAfter
			  }
			  strContent		= modContent + after
		  }
	  }
	  *?
	  
	  int ind = strContentHtml.indexOf("")
	  File f = new File(strOutputDir + File.separator +content_file_name)
	  if (f.exists())
	  {
		 if (f.delete())
			 println "Edellinen tiedosto " +f +" poistettu."
	  }
		   
	  ?*
	  if (f.asWritable("UTF-8"))
		  println "ON"
	  else
		  println "Ei"
	  *?
		  
	  f.append(strContentHtml.replaceAll("(?s)([\n\\\\r]+)(\t*<div)", "\n\$2"), "UTF-8")

				?*
	  FileOutputStream fos = new FileOutputStream(f3, false)
	  BufferedOutputStream bos = new BufferedOutputStream(fos)
	  OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8")
	  osw.write new String(sbContent.toString().getBytes("UTF-8"), "UTF-8") // , "UTF-8"
	  osw.close()
	  *?
	  
	  ?*
	  Writer out = new BufferedWriter(new OutputStreamWriter(
		  new FileOutputStream(strOutputDir + File.separator +content_file_name), "UTF-8"));
	  try {
		  out.write(sbContent.toString());
	  } finally {
		  out.close();
	  }
	  *?
	  
	  if (dc_authrows.toString().contains(","))
	  {
		  def tmp = dc_authrows
		  dc_authrows = ""
		  for(a in tmp.split(","))
			  dc_authrows += "				<meta name=\"dc:creator\" content=\"" +a +"\" />\n"
	  }
	  
	  def depth = (VoiceDataFile.depth == null ? 0 : VoiceDataFile.depth +1)
	  def pageNormal = VoiceDataFile.iFirstPage ? (pages - VoiceDataFile.iFirstPage+1) : pages
	  def pageMax = VoiceDataFile.iFirstPage ? (pages - VoiceDataFile.iFirstPage+1) : pages
	  
	  def nccbinding = [   "dc_date": dc_date,
							"dc_identifier":  dc_identifier,
						   "dc_title": dc_title,
						   "depth": depth,
						   "dc_authrows": dc_authrows,
						   "pages": pageNormal,
						   "pages": pageMax,
						   "tocItems": tocItems,
						   "time_together": VoiceData.getClipTime(time_together),
						   "header": sbNcc.toString() ]
	  
	  // if (!nccTemplate)
			  nccTemplate = engine.createTemplate(strNccTemplate)
	  String strNcc = nccTemplate.make(nccbinding).toString()

	  File f2 = new File(strOutputDir + File.separator +ncc_file_name)
	  if (f2.exists())
		  if (f2.delete())
			  println "Edellinen tiedosto " +f2 +" poistettu."

  	 if (executetype == cnstExecuteDaisy2)
	 	  f2.append(strNcc, "UTF-8")
	  
	  // sbCss.append daisyIDs.toCss()
	  
  	  println "\n===================="
	  println "Konvertointi valmis."
	  println "====================\n"
	  
	  str_user_home = System.getProperty("user.home")
	  if (str_user_home && bGui )
	  {
		  def File fsettings = new File(str_user_home +File.separator +strClassName +".properties")
		  if (fsettings.exists())
		  {
			  if (!fsettings.isDirectory())
				  if (!fsettings.delete())
				  {
					  throw new Exception("En voi poistaa tiedostoa: " +fsettings)
				  }
			  Properties settprop = new Properties ()
			  settprop.setProperty("cfgfile", fLipsyncCfg.getAbsolutePath())
			  // settprop.setProperty("templatedir", this.strParTemplate)
			  settprop.setProperty("templatedir", this.strSmilTemplateDir)
			  settprop.setProperty("readdir", fLipsyncDataDir.getAbsolutePath())
			  settprop.setProperty("outputdir", fOutputDir.getAbsolutePath())
			  FileWriter fw = new FileWriter(fsettings)
			  settprop.store(fw, "Käyttäjäkohtaiset asetukset / " +this.strAppName)
			  fw.close()
		  }
	  }
	  */

  }

  def private isHtmlHeaderEnd(String value)
  {
	  if (!value)
	  	return false
	  value = value.trim()
	  def len = value.length()
	  if (len > 2)
	  {  
		  def strH = value.substring(0,1).trim()
		  if (!strH || !(strH in ['h','H']))
			  return false
		  def strEnd = value.substring(len -1, len).trim()
		  if (!strEnd || strEnd != '>')
		  	return false

		  def strNumber = value.substring(1,len-1).trim()
		  if (!strNumber)
			  return false
		  try {
			  int iValue = Integer.parseInt strNumber
			  if (iValue > 0)
			  {
			  	return true
			  }
		  } catch(Exception e){
		  	  return false
		  }
	  }
	  false
  }
  
  /**
   * generoidaan daisy 3 tiedotot tulostushakemistoon
   */
  def private void convertLipsync2SmilContentAfterDaisy3()
  {	  
	  println "\n" +getMessages().getString(Lipsync2Daisy.constUI_ui_staringintodaisy).replace("%i", "3") +"..."
	  
	  def strPage_lipsync_time_on_off = prop3.getProperty("page_lipsync_time_on_off", null)
	  if (strPage_lipsync_time_on_off.toString().toLowerCase() == "off")
		 page_lipsync_time_on_off = false
	  else
	  if (strPage_lipsync_time_on_off.toString().toLowerCase() == "on")
		 page_lipsync_time_on_off = true
	  else
		   throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_wrong) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_value) +": " +strPage_lipsync_time_on_off +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_invariable) +" (.cfg): page_lipsync_time_on_off!")
	  VoiceData.page_lipsync_time_on_off = page_lipsync_time_on_off

	  VoiceDataDaisy3.dtbooksmilreftemplate = readFromDaisy3TemplateDirFile(dtbooksmilreftemplatefile)
	  
	  if (!VoiceDataDaisy3.dtbooksmilreftemplate)
		  throw new Exception("cfg dtbooksmilreftemplate: dtbook.smilref.templatefile " +getMessages().getString(Lipsync2Daisy.constUI_ui_file2) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_name) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_and)+" " +getMessages().getString(Lipsync2Daisy.constUI_ui_file) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_cannotbeempty) +"!")
		  
	  if (!templatefiles)
		  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_cfgvariablemusthavevalue) +": templatefiles!")

	
	  // check that all needed template file types are existing:
	  def smil_template_file_exist = false
	  def opf_template_file_exist = false
	  def dtbookxml_template_file_exist = false
	  def ncx_template_file_exist = false
	  for(String templatefilename in templatefiles.split(" "))
	  {
		  if (!templatefilename)
			   continue
		  if (templatefilename.endsWith(".opf.tmpl"))
			   opf_template_file_exist = true
		  else
		  if (templatefilename.endsWith(".smil.tmpl"))
			  smil_template_file_exist = true
		  else
		  if (templatefilename.endsWith("dtbook.xml.tmpl"))
			  dtbookxml_template_file_exist = true
		  else
		  if (templatefilename.endsWith(".ncx.tmpl"))
			  ncx_template_file_exist = true
	  }

	  if (!dtbookxml_template_file_exist)
	  		println getMessages().getString(Lipsync2Daisy.constUI_ui_file) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_type) +" dtbook xml template " +getMessages().getString(Lipsync2Daisy.constUI_ui_missing)+ "! "
	  if (!ncx_template_file_exist)
	  		println getMessages().getString(Lipsync2Daisy.constUI_ui_file) + " " +getMessages().getString(Lipsync2Daisy.constUI_ui_type) +" ncx template " +getMessages().getString(Lipsync2Daisy.constUI_ui_missing) + "! "
	  if (!smil_template_file_exist)
		 println getMessages().getString(Lipsync2Daisy.constUI_ui_file) + " "+getMessages().getString(Lipsync2Daisy.constUI_ui_type) +" smil template " +getMessages().getString(Lipsync2Daisy.constUI_ui_missing) + "! "
	  if (!opf_template_file_exist)
		 println getMessages().getString(Lipsync2Daisy.constUI_ui_file) +"  " +getMessages().getString(Lipsync2Daisy.constUI_ui_type) +" opf template " +getMessages().getString(Lipsync2Daisy.constUI_ui_missing) + "! "
	  if (!(dtbookxml_template_file_exist && ncx_template_file_exist
		  && smil_template_file_exist && opf_template_file_exist))
	  		return ;
	  // }
	  voiceObjectMapValues = [:]
	  
	  StringBuffer sbContent = new StringBuffer ()
	  StringBuffer sbContentMeta = new StringBuffer ()
	  StringBuffer sbNcc = new StringBuffer ()
	  StringBuffer sbCss= new StringBuffer ()
	  StringBuffer sbSmil
	  File fSmil
	  
	  // alusta template tekstit:
	  /*
	  VoiceDataFile.strSmilTemplate = strSmilTemplate
	  VoiceDataFile.strH1Template = strH1Template
	  VoiceDataFile.strSeqTemplate = strSeqTemplate
	  VoiceDataFile.content_file_name = content_file_name
	  VoiceData.strParTemplate = strParTemplate
	  VoiceData.page_lipsync_time_on_off = page_lipsync_time_on_off
	  def cnstTimeFormatter = new DecimalFormat("##0.000")
	  cnstTimeFormatter.setRoundingMode(RoundingMode.UNNECESSARY)
	  VoiceData.cnstTimeFormatter = cnstTimeFormatter
	  VoiceDataFile.dc_identifier  = dc_identifier
	  VoiceDataFile.dc_title = dc_title
	  VoiceDataFile.region_id = region_id
	  VoiceDataFile.register = this
	  VoiceData.register = this
	  */
	  int iCnt = 0
	  
	  def time_together = 0
	  
	  tocItems = 0
	  // merkataan mitka voicedata xml instanssit aloittaavat ja lopettavat
	  // daisy3 lauseet. Eli merkataan lauseita aloittavat lopettavat xml docbook
	  // elementit:
			
	  def list = [], newlistVDFs = []
	  VoiceData prev_vd
	  def isDaisy3_sentence_begin_founded = false
	  def daisy3sentencesCanStart = false
	  def listDaisy3sentences = []
	  def listDaisy3Xmlmarks  = []
	  def strLastListDaisy3Xmlmark = null
	  def lastListDaisy3XmlmarkVoiceData = null

	  for(VoiceDataFile vdf in listVDFs)
	  {
		  // println vdf.file
		  Lipsync2Daisy.currentxmlfilenameprinted = false
		  Lipsync2Daisy.currentxmlfilename = vdf.file.toString()
	  
		  currentVoiceDataFile = vdf
		  // currentVoiceDataFile.convert2Smil()
		  iCnt++
		  /*
		  if (!vdf.mp3_file_name)
		  {
			 vdf.mp3_file_name = getNextSmilMp3FileName()
			 vdf.smil_file_name   = getCurrentSmilFileName()
			 vdf.bSentenceWordMode  = bSentenceWordMode
		  }
		  */
		  
		  // if (doctitle_on_off && iCnt == 1)
			  // currentVoiceDataFile.isDocTitle = true
		  // vdf.setBaseValuesOfConversion()
		  list = []
		  isDaisy3_sentence_begin_founded = false
		  def afterWordsHasSeveralSentencies = false, hasMixedSeveralSentencies = false
		  prev_vd = null
		  
		  for(VoiceData vd in currentVoiceDataFile.listitems)
		  {
			  vd.generateIdValues()	
			  vd.bSeekSequenceEndOf = false
			  if (vd && vd.name == VoiceData.cnstLipsyncXmlmark 
				  && !vd.text.toString().endsWith("/>"))
			  {
				  if (strLastListDaisy3Xmlmark 
					  && vd.text.toString().toLowerCase().startsWith("</" +strLastListDaisy3Xmlmark))
				  {
					  if (listDaisy3Xmlmarks.size() > 0)
					  	listDaisy3Xmlmarks.remove(listDaisy3Xmlmarks.size()-1)
					  if (listDaisy3Xmlmarks.size() == 0)
					  	lastListDaisy3XmlmarkVoiceData = null
					  else
					  {
					  	lastListDaisy3XmlmarkVoiceData = listDaisy3Xmlmarks.get(listDaisy3Xmlmarks.size()-1)
						  def value = vd.text.toString().substring(1)
						  if (value && value.contains(" "))
							  value = value.split(" ")[0]+">"
						  // else
							  //value = value.substring(0, value.length()-1)
						  strLastListDaisy3Xmlmark = value
					  }
				  }
				  else
				  {
				  	listDaisy3Xmlmarks.add vd
					def value = vd.text.toString().substring(1)
					if (value && value.contains(" "))
						value = value.split(" ")[0]+">"
					// else
						//value = value.substring(0, value.length()-1)
					if (value && isHtmlHeaderEnd(value))
						vd.isDaisy3_sentence_begin = true
						
					strLastListDaisy3Xmlmark = value
					lastListDaisy3XmlmarkVoiceData = vd
					
					if (daisy3sentencesCanStart)
					{
						int sentenceChilds = getHasSeveralSentencies(vd)
						if (sentenceChilds)
							vd.sentenceChilds = sentenceChilds
						if (vd.text.toString().toLowerCase().startsWith("<p ")
						  || vd.text.toString().toLowerCase().startsWith("<p>"))
						{
							if (vd.sentenceChilds == 1)
							{
								vd.isDaisy3_sentence_begin = true
								isDaisy3_sentence_begin_founded = false
								afterWordsHasSeveralSentencies = false
							}
							else
							{
								if (vd.sentenceChilds > 1)
								{
									vd.isDaisy3_sentence_begin = false
									isDaisy3_sentence_begin_founded = true
									afterWordsHasSeveralSentencies = true
								}
							}
						}
						else
						{
							def textValue = vd.text.toString()
							if (textValue.endsWith(">"))
								textValue = textValue.substring(0, textValue.length()-1)
							//if (textValue.contains("line"))
								//println "textValue"
							def listValue = (textValue.length() > 1  ? textValue.toLowerCase().split(" ")[0].substring(1) : null ) 
							if (listValue)
							{
								if (listValue in listAddDaisy3SmillRef)
									vd.isDaisy3_sentence_begin = true
								if (listValue in listNoDaisy3SmillRefIfManySentencies && vd.sentenceChilds > 0)
								{
									isDaisy3_sentence_begin_founded = false
									afterWordsHasSeveralSentencies = false
								}
							}
							else
							if (vd.sentenceChilds == 1)
							{
								vd.isDaisy3_sentence_begin = true
							}	
						}
					}
				  }				 		
			  }	
			  else
			  if (vd && vd.name == VoiceData.cnstLipsyncWord)
			  {
				  /* tka 8.3.2015 into comment, until else reserved word: */
				  if (vd.xmlText.toString().toLowerCase().startsWith("<pagenum "))
				  {
					  vd.isDaisy3_sentence_begin = true
				  }
				  else				  
				  if (afterWordsHasSeveralSentencies /* && vd.next 
					  && (vd.next.name == VoiceData.cnstLipsyncXmlmark || 
					  (vd.next.name == VoiceData.cnstLipsyncWord
					  && vd.next.text && Character.isUpperCase(vd.next.text.charAt(0)))
					  ) */ )					 
				  {
					  vd.isDaisy3_sentence_begin = true
					  vd.afterWordsHasSeveralSentencies = true
				  }
			  }		  
			  
			  if (!daisy3sentencesCanStart)
			  {
				  if (vd.name == 'xmlmark'
					  && (vd.text.startsWith('<book') || vd.text.startsWith('<BOOK')))
					  daisy3sentencesCanStart = true
			  }
			  else
			  {
				  /*
				  if (prev_vd && prev_vd.name == VoiceData.cnstLipsyncXmlmark
					  && !vd.text.toString().endsWith("/>")
					  && !prev_vd.text.startsWith('<book') && !prev_vd.text.startsWith('<BOOK') )	
				  {
					  if (prev_vd.text)
					  {
						  if (prev_vd.text.toString().toLowerCase().startsWith("</p ")
						  || prev_vd.text.toString().toLowerCase().startsWith("</p>")) 
					      {
							  afterWordsHasSeveralSentencies = false
							  hasMixedSeveralSentencies = false
							  prev_vd.afterWordsHasSeveralSentencies = afterWordsHasSeveralSentencies
							  prev_vd.isDaisy3_sentence_end = true
							  isDaisy3_sentence_begin_founded = false
						  }
						  else
						  if (prev_vd.text.toString().toLowerCase().startsWith("<p ")
							  || prev_vd.text.toString().toLowerCase().startsWith("<p>"))
						  {
							  afterWordsHasSeveralSentencies = false
							  hasMixedSeveralSentencies = hasHasSeveralSentencies(vd)
							  if (hasMixedSeveralSentencies)
							  {
							  	afterWordsHasSeveralSentencies = true
								prev_vd.afterWordsHasSeveralSentencies_on = true
							  }
							  prev_vd.afterWordsHasSeveralSentencies = afterWordsHasSeveralSentencies
							  prev_vd.isDaisy3_sentence_begin = true
							  isDaisy3_sentence_begin_founded = true
							  prev_vd.isDaisy3_sentence_begin_on = isDaisy3_sentence_begin_founded
						  }
						  else
						  {
							if (afterWordsHasSeveralSentencies && prev_vd.text.toString().toLowerCase().startsWith("</"))
							{
								prev_vd.afterWordsHasSeveralSentencies = false
							}
							else
							if (vd && vd.name == VoiceData.cnstLipsyncWord)
							{
								if (vd.xmlText.toString().toLowerCase().startsWith("<pagenum "))
								{
									vd.isDaisy3_sentence_begin = true
								}
								else
								{
									isDaisy3_sentence_begin_founded = true
									prev_vd.isDaisy3_sentence_begin = isDaisy3_sentence_begin_founded
									prev_vd.isDaisy3_sentence_begin_on = isDaisy3_sentence_begin_founded
									if (!afterWordsHasSeveralSentencies)
									{
										def tmp = hasHasSeveralSentencies(prev_vd)
										if (tmp)
										{
											isDaisy3_sentence_begin_founded = true
											prev_vd.isDaisy3_sentence_begin_on = isDaisy3_sentence_begin_founded
										}
									}
								}
							}  
						  }
					  }	
				  }
				  */
				  
				  /*
				  if (afterWordsHasSeveralSentencies && (isDaisy3_sentence_begin_founded
					   || hasMixedSeveralSentencies )
					   && vd.name == VoiceData.cnstLipsyncWord )
					  vd.afterWordsHasSeveralSentencies = afterWordsHasSeveralSentencies			  
					  
			     if (!vd.text.toString().endsWith("/>")) 
				    vd.isDaisy3_sentence_begin = isDaisy3_sentence_begin_founded
				 */ 
			  }
					
			  if (prev_vd)
				   list.add prev_vd
			  prev_vd = vd
		  }
		  list.add prev_vd
		  
		  vdf.listitems = list
		  newlistVDFs.add(vdf)
	  }
	  listVDFs = newlistVDFs
	  
	  iCnt = 0
	  def elementname
	
	  println "Seeksequencies..."
	  println()
	  
	  list = []
	  newlistVDFs = []
	  def vd_seekSequenceEndOf
	  
	  for(VoiceDataFile vdf in listVDFs)
	  {
		  // println vdf.file
		  Lipsync2Daisy.currentxmlfilenameprinted = false
		  Lipsync2Daisy.currentxmlfilename = vdf.file.toString()
	  
		  currentVoiceDataFile = vdf
		  iCnt++
		  list = []
		  prev_vd = null
		  
		  for(VoiceData vd in currentVoiceDataFile.listitems)
		  {
			  /*
			  if (vd.text.toString().contains("<list"))
			  	println "ddeee"
			  */
			  			 
			 elementname = vd.text?.toString().replace("<", "").replace(">", "").split(" ")[0]
			 if (elementname && vd.name == VoiceData.cnstLipsyncXmlmark && vd.text 
				 && (elementname in Lipsync2Daisy.getListCustomTestAttributeNames()))
			  	vd_seekSequenceEndOf = vd.seekSequenceEndOf(elementname)
  				
			  if (prev_vd)
				   list.add prev_vd
			  prev_vd = vd
		  }
		  list.add prev_vd
		  
		  vdf.listitems = list
		  newlistVDFs.add(vdf)
	  }
	  listVDFs = newlistVDFs
	  
	  // poista vanhat tulostustiedostot
	  File fOutputDir = new File(this.strOutputDir)
	  if (!fOutputDir.exists())
			  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_dirnotexists) +": " +fOutputDir)
	  if (!fOutputDir.isDirectory())
		  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_is_not_dir) +": " +fOutputDir)
	  String strFileName
	  
	  for(File fOut in fOutputDir.listFiles())
	  {
		  strFileName = fOut.getName().toLowerCase()
		  if (strFileName.endsWith(".smil")
			  || strFileName.endsWith(".xml")
			  || strFileName.endsWith(".opf")
			  || strFileName.endsWith(".ncx")
			  )
		  {
			  if (!fOut.delete())
				  throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_cannotdeletefile) +": " +fOut)
			 println getMessages().getString(Lipsync2Daisy.constUI_ui_deleted) +": " +fOut
		  }
	  }

	  def bDeleteFile = true
	  
	  // for(VoiceDataFile vdf in listVDFs)
	  // {
		 // println vdf.file

		  for(String templatefilename in templatefiles.split(" "))
		  {
			  if (!templatefilename)
				   continue
			  if (templatefilename.endsWith(".smil.tmpl"))
					generateSmilFilesAfterTemplate(templatefilename, bDeleteFile)
		  }

		  /*
		  def vdf_total_time
		  Lipsync2Smil.totaltime = 0
		  for(VoiceDataFile vdf in listVDFs)
		  {
			  vdf_total_time = vdf.totaltime
			  if (Lipsync2Smil.totaltime < vdf_total_time)
			  	Lipsync2Smil.totaltime = vdf_total_time 
		  }	
		  */	 
		  
		  for(String templatefilename in templatefiles.split(" "))
		  {
			  if (!templatefilename)
				   continue
			  if (templatefilename.endsWith("dtbook.xml.tmpl"))
			  {
				   dtbook_templatefilename = templatefilename
				   listVDFs = generateDtbookFileAfterTemplate(templatefilename, bDeleteFile)
			  }
		  }

		  for(String templatefilename in templatefiles.split(" "))
		  {
			  if (!templatefilename)
				   continue
			  if (templatefilename.endsWith(".opf.tmpl"))
				   generateOptFileAfterTemplate(templatefilename, bDeleteFile)
			  else
			  /*
			  if (templatefilename.endsWith(".smil.tmpl"))
					generateSmilFilesAfterTemplate(templatefilename, bDeleteFile)
			  else
			  */
			  /*
			  if (templatefilename.endsWith("dtbook.xml.tmpl"))
				 listVDFs = generateDtbookFileAfterTemplate(templatefilename, bDeleteFile)
			  else
			  */
			  if (templatefilename.endsWith(".ncx.tmpl"))
				  generateNcxFileAfterTemplate(templatefilename, bDeleteFile)
		  }
	  // }
		  
	  /*
	  voiceObjectMapValues = [:]
	  
	  StringBuffer sbContent = new StringBuffer ()
	  StringBuffer sbContentMeta = new StringBuffer ()
	  StringBuffer sbNcc = new StringBuffer ()
	  StringBuffer sbCss= new StringBuffer ()
	  StringBuffer sbSmil
	  File fSmil
	  
	  // alusta template tekstit:
	  VoiceDataFile.strSmilTemplate = strSmilTemplate
	  VoiceDataFile.strH1Template = strH1Template
	  VoiceDataFile.strSeqTemplate = strSeqTemplate
	  VoiceDataFile.content_file_name = content_file_name
	  VoiceData.strParTemplate = strParTemplate
	  VoiceData.page_lipsync_time_on_off = page_lipsync_time_on_off
	  def cnstTimeFormatter = new DecimalFormat("##0.000")
	  cnstTimeFormatter.setRoundingMode(RoundingMode.UNNECESSARY)
	  VoiceData.cnstTimeFormatter = cnstTimeFormatter
	  VoiceDataFile.dc_identifier  = dc_identifier
	  VoiceDataFile.dc_title = dc_title
	  VoiceDataFile.region_id = region_id
	  VoiceDataFile.register = this
	  VoiceData.register = this
	  int iCnt = 0
	  
	  def time_together = 0
	  
	  tocItems = 0
	  
	  for(VoiceDataFile v in listVDFs)
	  {
		  println v.file
		  currentVoiceDataFile = v
		  iCnt++
		  if (!v.mp3_file_name)
		  {
			   v.mp3_file_name = getNextSmilMp3FileName()
			 v.smil_file_name   = getCurrentSmilFileName()
			 v.bSentenceWordMode  = bSentenceWordMode
		  }
		  
		  // if (doctitle_on_off && iCnt == 1)
			  // currentVoiceDataFile.isDocTitle = true
		  currentVoiceDataFile.setBaseValuesOfConversion()
		  sbContent << currentVoiceDataFile.convert2SmilXhtml()
		  sbContentMeta << currentVoiceDataFile.convertMetaSmilXhtml()
		  sbNcc << currentVoiceDataFile.convert2Ncc()
		  tocItems += currentVoiceDataFile.iNccItems
		  sbSmil = new StringBuffer()
		  sbSmil << currentVoiceDataFile.convert2Smil()
		  fSmil = new File(strOutputDir + File.separator +v.smil_file_name)
		  if (fSmil.exists())
		  {
			   if (fSmil.delete())
			   println getMessages().getString(Lipsync2Smil.constUI_ui_previous) +" " +getMessages().getString(Lipsync2Smil.constUI_ui_file)+" " +fSmil +" " +getMessages().getString(Lipsync2Smil.constUI_ui_deleted) +"."
		  }
		  if (executetype == cnstExecuteDaisy2)
				  fSmil.append sbSmil.toString(), "UTF-8"
		  time_together += v.totaltime() // time_together
	  }
	  
	 // println "time_together: " +time_together
	  //println "time_together/1000: " +time_together/1000
	  def timeDate = new Date(time_together.toLong())
	  SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss")
	  time_together = (time_together/1000)/60
	  def str_time_together = "" +sdf.format(timeDate);
	  //str_time_together = str_time_together.toString().replace(".",":")
	  //println "str_time_together: " +str_time_together
	  // time_together = str_time_together
	  println()
	  
	  ?* kommmenttikoodi
	  for(entity in daisyIDs.openLipsyncEntity.reverse())
	  {
		  sbContent.append("\n</" + entity +">\n")
	  }
	  *?
	  
	  // jälkikäsittely:
	  def strContent = sbContent.toString()
	  def strMetatext= sbContentMeta.toString()
			
	  def contentbinding = [ "metatext": strMetatext,
						   "bodytext": strContent ]
	  if (!contentbinding)
		  throw new Exception("contentbinding is null!")
		  
	  if (!engine)
		  throw new Exception("engine is null!")
		  
	  if (!strContentTemplate)
		  throw new Exception("strContentTemplate is null!")
		  
	  // if (!contentTemplate)
			  contentTemplate = engine.createTemplate(strContentTemplate)
	  String strContentHtml = contentTemplate.make(contentbinding).toString()

	  ?*
	  if (!strContent.toLowerCase().contains("<!DOCTYPE"))
	  {
		  def search = "<head>"
		  int indHeaderStart = strContent.indexOf(search)
		  if (indHeaderStart > -1)
		  {
			  def before 		= strContent.substring(0, indHeaderStart)
			  def modContent 	= before + "\n" + xhtmlDoctype +"\n"
			  def after 		= strContent.substring(indHeaderStart)
			  if (!after.toLowerCase().contains("<meta http-equiv="))
			  {
				  indHeaderStart 	= after.indexOf(search)
				  before	 	 	= after.substring(indHeaderStart, indHeaderStart+search.length()) +"\n"
				  def modAfter 		= cont+ "\n" + after.substring(indHeaderStart+search.length())
				  after 			= before +modAfter
			  }
			  strContent		= modContent + after
		  }
	  }
	  *?
	  
	  int ind = strContentHtml.indexOf("")
	  File f = new File(strOutputDir + File.separator +content_file_name)
	  if (f.exists())
	  {
		 if (f.delete())
			 println "Edellinen tiedosto " +f +" poistettu."
	  }
		   
	  ?*
	  if (f.asWritable("UTF-8"))
		  println "ON"
	  else
		  println "Ei"
	  *?
		  
	  f.append(strContentHtml.replaceAll("(?s)([\n\\\\r]+)(\t*<div)", "\n\$2"), "UTF-8")

				?*
	  FileOutputStream fos = new FileOutputStream(f3, false)
	  BufferedOutputStream bos = new BufferedOutputStream(fos)
	  OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8")
	  osw.write new String(sbContent.toString().getBytes("UTF-8"), "UTF-8") // , "UTF-8"
	  osw.close()
	  *?
	  
	  ?*
	  Writer out = new BufferedWriter(new OutputStreamWriter(
		  new FileOutputStream(strOutputDir + File.separator +content_file_name), "UTF-8"));
	  try {
		  out.write(sbContent.toString());
	  } finally {
		  out.close();
	  }
	  *?
	  
	  if (dc_authrows.toString().contains(","))
	  {
		  def tmp = dc_authrows
		  dc_authrows = ""
		  for(a in tmp.split(","))
			  dc_authrows += "				<meta name=\"dc:creator\" content=\"" +a +"\" />\n"
	  }
	  
	  def depth = VoiceDataFile.depth
	  def pageNormal = VoiceDataFile.iFirstPage ? (pages - VoiceDataFile.iFirstPage+1) : pages
	  def pageMax = VoiceDataFile.iFirstPage ? (pages - VoiceDataFile.iFirstPage+1) : pages
	  
	  def nccbinding = [   "dc_date": dc_date,
							"dc_identifier":  dc_identifier,
						   "dc_title": dc_title,
						   "depth": depth,
						   "dc_authrows": dc_authrows,
						   "pages": pageNormal,
						   "pages": pageMax,
						   "tocItems": tocItems,
						   "time_together": VoiceData.getClipTime(time_together),
						   "header": sbNcc.toString() ]
	  
	  // if (!nccTemplate)
			  nccTemplate = engine.createTemplate(strNccTemplate)
	  String strNcc = nccTemplate.make(nccbinding).toString()

	  File f2 = new File(strOutputDir + File.separator +ncc_file_name)
	  if (f2.exists())
		  if (f2.delete())
			  println "Edellinen tiedosto " +f2 +" poistettu."

	   if (executetype == cnstExecuteDaisy2)
		   f2.append(strNcc, "UTF-8")
	  
	  // sbCss.append daisyIDs.toCss()
	  
  	println "\n===================="
	println "Konvertointi valmis."
	println "====================\n"

	  
	  str_user_home = System.getProperty("user.home")
	  if (str_user_home && bGui )
	  {
		  def File fsettings = new File(str_user_home +File.separator +strClassName +".properties")
		  if (fsettings.exists())
		  {
			  if (!fsettings.isDirectory())
				  if (!fsettings.delete())
				  {
					  throw new Exception("En voi poistaa tiedostoa: " +fsettings)
				  }
			  Properties settprop = new Properties ()
			  settprop.setProperty("cfgtiedosto", fLipsyncCfg.getAbsolutePath())
			  // settprop.setProperty("templatehakemisto", this.strParTemplate)
			  settprop.setProperty("templatehakemisto", this.strSmilTemplateDir)
			  settprop.setProperty("lukuhakemisto", fLipsyncDataDir.getAbsolutePath())
			  settprop.setProperty("tuloshakemisto", fOutputDir.getAbsolutePath())
			  FileWriter fw = new FileWriter(fsettings)
			  settprop.store(fw, "Käyttäjäkohtaiset asetukset / " +this.strAppName)
			  fw.close()
		  }
	  }
	  */	
  }

  def String getLastBeginTimeOfDaisy2(String value, String lastremoveid)
  {
	  if (!value || !lastremoveid)
	  {
		  if (!lastremoveid)
		  {
			  println " ========================== "
			  println " Cannot find clip begin time for text src id because lastremoveid is null!"
			  println " ========================== "
		  }
		  else
		  {
			  println " ========================== "
			  println " Cannot find clip begin time for text src id because value of a smilfile text is null!"
			  println " ========================== "
		  }

		   return null
	  }
	  def idnumber = lastremoveid.replace("dtb", "")
	  def matcher2 = value =~ /(?s)<par\s+endsync="last"\s+id="tcp$idnumber"\s*>[\s\n\c\r]*<text\s+id=".*?"\s+src=".*?#$lastremoveid"\s+\/>[\s\n\c\r]*<audio\s+src=".*?"\s+id="aud$idnumber"\s+clip-begin=".*?"\s+clip-end="(.*?)"\s*\/>[\s\n\c\r]*<\/par>/
	  def ret = ""
	  if (matcher2.find())
	  {
		  ret = matcher2[0][1]
	  }
	  else
	  {
		  println " ========================== "
		  println " Cannot find clip begin time for text src id: " +lastremoveid
		  println " ========================== "
	  }
	  ret
  }
  
  def String getParStringOfDaisy2(String value, String id)
  {
	  if (!value || !id)
	  {
		  if (!id)
		  {
			  println " ========================== "
			  println " Cannot find par for text src id because id is null!"
			  println " ========================== "
		  }
		  else
		  {
			  println " ========================== "
			  println " Cannot find par for text src id because value of a smilfile text is null!"
			  println " ========================== "
		  }

		   return null
	  }
	  def idnumber = id.replace("dtb", "")
	  // def matcher2 = value =~ /(?s)(<par\s+endsync="last"\s+id="tcp$idnumber">[\s\n\r]*<text\s+id=".*?"\s+src=".*?#dtb$idnumber"\s+\/>[\s\n\r]*<audio\s+clipBegin="(.*?)"\s+clipEnd="(.*?)"\s+src="(.*?)"\s+\/>[\s\n\r]*<\/par>)/
	  def matcher2 = value =~ /(?s)(<par\s+endsync="last"\s+id="tcp$idnumber"\s*>[\s\n\r]*<text\s+id=".*?"\s+src=".*?#dtb$idnumber"\s+\/>[\s\n\r]*<audio\s+src="(.*?)"\s+id=".*?"\s+clip-begin="(.*?)"\s+clip-end="(.*?)"\s*\/>[\s\n\r]*<\/par>)/
	  def ret = ""
	  if (matcher2.find())
	  {
		  ret = matcher2[0][1]
	  }
	  else
	  {
		  println " ========================== "
		  println " Cannot fild clip begin time for text src id: " +id
		  println " ========================== "
	  }
	  ret
  }
  
  def String getParStringOf(String value, String id)
  {
	  if (!value || !id)
	  {
		  if (!id)
		  {
			  println " ========================== "
			  println " Cannot find par for text src id because id is null!"
			  println " ========================== "
		  }
		  else
		  {
			  println " ========================== "
			  println " Cannot find par for text src id because value of a smilfile text is null!"
			  println " ========================== "
		  }

		   return null
	  }
	  def idnumber = id.replace("dtb", "")
	  def matcher2 = value =~ /(?s)(<par\s+id="tcp$idnumber">[\s\n\r]*<text\s+id=".*?"\s+src=".*?#dtb$idnumber"\s+\/>[\s\n\r]*<audio\s+clipBegin="(.*?)"\s+clipEnd="(.*?)"\s+src="(.*?)"\s+\/>[\s\n\r]*<\/par>)/
	  def ret = ""
	  if (matcher2.find())
	  {
		  ret = matcher2[0][1]
	  }
	  else
	  {
		  println " ========================== "
		  println " Cannot fild clip begin time for text src id: " +lastremoveid
		  println " ========================== "
	  }
	  ret
  }
  

  def String getLastBeginTimeOf(String value, String lastremoveid)
  {
	  if (!value || !lastremoveid)
	  {
		  if (!lastremoveid)
		  {
			  println " ========================== "
			  println " Cannot find clip begin time for text src id because lastremoveid is null!"
			  println " ========================== "
		  }
		  else
		  {
			  println " ========================== "
			  println " Cannot find clip begin time for text src id because value of a smilfile text is null!"
			  println " ========================== "
		  }

	  	 return null
	  }
	  def idnumber = lastremoveid.replace("dtb", "")
	  def matcher2 = value =~ /(?s)(<par\s+id="tcp$idnumber">[\s\n\r]*<text\s+id=".*?"\s+src=".*?#$lastremoveid"\s+\/>[\s\n\r]*<audio\s+clipBegin="(.*?)"\s+clipEnd="(.*?)"\s+src="(.*?)"\s+\/>[\s\n\r]*<\/par>)/
	  def ret = ""
	  if (matcher2.find())
	  {
		  ret = matcher2[0][3]
	  }
	  else
	  {
		  println " ========================== "
		  println " Cannot find clip begin time for text src id: " +lastremoveid
		  println " ========================== "
	  }
	  ret
  }
  
  /**
   * This method returns modified smil file content if it has seqence xml block with same dtb end id
   * than firstid parameter. If not, then it returns unmodified smil file content.
   * 
   * @param smiltext readed smile file content
   * @param firstId a firtid which is going to change when combinazing sentencies
   * @param removeIdElements id values to be removed
   * @return Possible modified file content
   */
  def getRemovedSeqIdSmilFileContent(String smiltext, String firstId, removeIdElements)
  {
  	  def ret = smiltext
	  def matcher = smiltext =~ /<seq\s+(.*?)end="DTBuserEscape;(.*?)\.end"\s+(.*?)id="(.*?)">/	  
	  def founded = false, sb = new StringBuffer(), strStart, strEnd
	  def iStart, iEnd, strFirstFounded, strFounded, strThirdFounded, strFourthFounded, strDtb, i = 0

	  while(matcher.find())
	  {
		  strFirstFounded = matcher[i][1]
		  strFounded = matcher[i][2]
		  strThirdFounded = matcher[i][3]
		  strFourthFounded = matcher[i++][4]
	  	  if (strFounded)
		  {			  
			strDtb = strFounded.toString().replace("tcp","dtb")
			if (strDtb && strDtb in removeIdElements)
			{
				founded = true
				iStart = matcher.start()
				iEnd = matcher.end() 
				strStart = smiltext.substring(0, iStart)
				strEnd = smiltext.substring(iEnd)
				def modseq = "<seq " +(strFirstFounded ? strFirstFounded : " ") + 'end="DTBuserEscape;' + firstId.replace("dtb", "tcp") + '.end" ' + (strThirdFounded ? strThirdFounded : " ") + 'id="' + (strFourthFounded ? strFourthFounded : " ") +'">' 
			  	def ret2 = strStart +"\n" +modseq +"\n" + strEnd
				  /*
				 def f = new File("koe.txt")
				 f.setText(ret2)
				 */
			    return ret2
			} 
		  }
		  else
		  if (strFounded && strFounded in removeIdElements)
		  {
			  founded = true
			  ret.add strFounded
		  } 
	  }
	  
	  ret
  }

  def private void rewriteSmilFileSentenciesDaisy3(String smilfilename, String firstId, removeIdElements)
  {
	  // hhhhhhh
	  
	  if (removeIdElements == null || removeIdElements.size() == 0)
	  	 return
	  String value = this.readOutputDirFile(smilfilename, false)
	  if (!value)
	  	return
		if (smilfilename == "speechgen0013.smil")
			println "smilfilename"
			
	  value = getRemovedSeqIdSmilFileContent(value, firstId, removeIdElements)
	  def parfirstId, firsttextId, firsttextsrc, firstaudiosrc, firstaudioid, firstclipbegin, firstclipend
	  def id, textId, textsrc, audiosrc, audioid, clipbegin, clipend, indtextsrc = -1, indtextsrc2 = -1
	  def matcher2, iRemoveIdElements = removeIdElements.size(), iRemovedElements = 0
	  def iCnt = 0, iMatchRow = 0, strstart = "", strend = "", strFounded, dtdid, dtdid2
	  def StringBuffer sb = new StringBuffer()
	  def StringBuffer sb2 = new StringBuffer()
	  def firstfounded = false, firstprinted = false
	  def lastremoveid = removeIdElements.get (removeIdElements.size() -1)
	  // to find modified text block:
	  def endId = lastremoveid.replace("dtb", "")
	  id = firstId.replace("dtb", "")
	  // find a text block between first id and last id par items:
	  def matcher = value =~ /(?s)<par\s+id="tcp$id">[\s\n\r]*<text\s+id=".*?"\s+src=".*?#dtb$id"\s+\\/>[\s\n\r]*<audio\s+clipBegin="(.*?)"\s+clipEnd="(.*?)"\s+src=".*?"\s+\\/>[\s\n\r]*<\\/par>.*?<par\s+id="tcp$endId">[\s\n\r]*<text\s+id=".*?"\s+src=".*?#dtb$endId"\s+\\/>[\s\n\r]*<audio\s+clipBegin="(.*?)"\s+clipEnd="(.*?)"\s+src=".*?"\s+\\/>[\s\n\r]*<\\/par>/	  
	  def strPar, newpar, strBetweenPars, newclipbegin = getLastBeginTimeOf(value, lastremoveid)
	  def istrBetweenPars_end = -1
	  def par_first

	  if (matcher.find())
	  {
		  strFounded = matcher.group() // founded text block
		  par_first = getParStringOf(strFounded, id)
		  if (!par_first)
		  	  return 
		  firstfounded = true
		  /*
		  def par_last = getParStringOf(strFounded, lastremoveid)
		  if (!par_last)
		  	  return
		  */ 
		  strstart = value.substring(0, matcher.start()) 
		  strend = value.substring(matcher.end())
		  // find all except par items:
		  def arrBetween = strFounded.split("(?s)<par.*?<\\/par>")
		  strBetweenPars = ""
		  def sbBetweenPars = new StringBuffer()
		  // collect all except par items:
		  for(str1 in arrBetween)
		  {
			  if (!str1)
			  	continue
			  strBetweenPars << str1			  
		  } 
		  strBetweenPars = strBetweenPars.toString()
		  
		  matcher2 = par_first =~ /(?s)(<par\s+id="(.*?)">[\s\n\r]*<text\s+id="(.*?)"\s+src="(.*?)"\s+\/>[\s\n\r]*<audio\s+clipBegin="(.*?)"\s+clipEnd="(.*?)"\s+src="(.*?)"\s+\/>[\s\n\r]*<\/par>)/
		  if (matcher2.find())
		  {
			  strFounded = matcher2[0][1]
			  parfirstId = matcher2[0][2]
			  firsttextId = matcher2[0][3]
			  firsttextsrc = matcher2[0][4]
			  indtextsrc = firsttextsrc.toString().indexOf("#")
			  if (indtextsrc > -1)
					  dtdid = firsttextsrc.toString().substring(indtextsrc +1)
			  firstclipbegin = matcher2[0][5]
			  firstclipend = matcher2[0][6]
			  firstaudiosrc = matcher2[0][7]
			  if (!dtdid || dtdid != firstId && strFounded)
			  {
				  sb << par_first +"\n"
			  }
			  
			  newpar = """
	  	 <par  id="$parfirstId">
            <text id="$firsttextId" src="$firsttextsrc" />
            <audio clipBegin="$firstclipbegin" clipEnd="$newclipbegin" src="$firstaudiosrc" />
         </par>"""
			sb << strstart +"\n" +newpar +"\n" + strBetweenPars +"\n" + strend +"\n"
			def newvalue = sb.toString()
			if (!newvalue)
					 return
			 writeIntoOutputDirFile(smilfilename, newvalue, true, false)
			 return 
		  }		 	
	  }
  }

  def private void rewriteSmilFileSentenciesDaisy2(String smilfilename, String firstId, removeIdElements)
  {
	  // hhhhhhh
	  
	  if (removeIdElements == null || removeIdElements.size() == 0)
		   return
	  String value = this.readOutputDirFile(smilfilename, false)
	  if (!value)
		  return
	  // value = getRemovedSeqIdSmilFileContent(value, firstId, removeIdElements)
	  def parfirstId, firsttextId, firsttextsrc, firstaudiosrc, firstaudioid, firstclipbegin, firstclipend
	  def id, textId, textsrc, audiosrc, audioid, clipbegin, clipend, indtextsrc = -1, indtextsrc2 = -1
	  def matcher2, iRemoveIdElements = removeIdElements.size(), iRemovedElements = 0
	  def iCnt = 0, iMatchRow = 0, strstart = "", strend = "", strFounded, dtdid, dtdid2
	  def StringBuffer sb = new StringBuffer()
	  def StringBuffer sb2 = new StringBuffer()
	  def firstfounded = false, firstprinted = false
	  def lastremoveid = removeIdElements.get (removeIdElements.size() -1)
	  // to find modified text block:
	  def endId = lastremoveid.replace("dtb", "")
	  id = firstId.replace("dtb", "")
	  // find a text block between first id and last id par items:
	  def matcher = value =~ /(?s)<par\s+endsync="last"\s+id="tcp$id"\s*>[\s\n\c\r]*<text\s+id=".*?"\s+src=".*?"\s+\/>[\s\n\c\r]*<audio\s+src=".*?"\s+id="aud$id"\s+clip-begin=".*?"\s+clip-end=".*?"\s*\/>[\s\n\c\r]*<\/par>.*?<par\s+endsync="last"\s+id="tcp$endId"\s*>[\s\n\c\r]*<text\s+id=".*?"\s+src=".*?"\s*\/>[\s\n\c\r]*<audio\s+src=".*?"\s+id="aud$endId"\s+clip-begin=".*?"\s+clip-end=".*?"\s*\/>[\s\n\c\r]*<\/par>/
	  def strPar, newpar, strBetweenPars, newclipbegin = getLastBeginTimeOfDaisy2(value, lastremoveid)
	  def istrBetweenPars_end = -1
	  def par_first

	  if (matcher.find())
	  {
		  strFounded = matcher.group() // founded text block
		  par_first = getParStringOfDaisy2(strFounded, id)
		  if (!par_first)
				return
		  firstfounded = true
		  /*
		  def par_last = getParStringOf(strFounded, lastremoveid)
		  if (!par_last)
				return
		  */
		  strstart = value.substring(0, matcher.start())
		  strend = value.substring(matcher.end())
		  // find all except par items:
		  def arrBetween = strFounded.split("(?s)<par.*?<\\/par>")
		  strBetweenPars = ""
		  def sbBetweenPars = new StringBuffer()
		  // collect all except par items:
		  for(str1 in arrBetween)
		  {
			  if (!str1)
				  continue
			  strBetweenPars << str1
		  }
		  strBetweenPars = strBetweenPars.toString()
		  
		  matcher2 = par_first =~ /(?s)<par\s+endsync="last"\s+id="(.*?)"\s*>[\s\n\r]*<text\s+id="(.*?)"\s+src="(.*?)"\s+\\/>[\s\n\r]*<audio\s+src="(.*?)"\s+id="(.*?)"\s+clip-begin="(.*?)"\s+clip-end="(.*?)"\s+\\/>[\s\n\r]*<\\/par>/
		  if (matcher2.find())
		  {
			  strFounded = matcher2[0]
			  parfirstId = matcher2[0][1]
			  firsttextId = matcher2[0][2]
			  firsttextsrc = matcher2[0][3]
			  indtextsrc = firsttextsrc.toString().indexOf("#")
			  if (indtextsrc > -1)
					  dtdid = firsttextsrc.toString().substring(indtextsrc +1)
			  firstclipbegin = matcher2[0][6]
			  firstclipend = matcher2[0][7]
			  firstaudiosrc = matcher2[0][4]
			  if (!dtdid || dtdid != firstId && strFounded)
			  {
				  sb << par_first +"\n"
			  }
			  
			  newpar = """
	  	 <par endsync="last" id="$parfirstId">
            <text id="$firsttextId" src="$firsttextsrc" />
            <audio src="$firstaudiosrc" id="aud$id" clip-begin="$firstclipbegin" clip-end="$newclipbegin" />
         </par>"""
			sb << strstart +"\n" +newpar +"\n" + strBetweenPars +"\n" + strend +"\n"
			def newvalue = sb.toString()
			if (!newvalue)
					 return
			 writeIntoOutputDirFile(smilfilename, newvalue, true, false)
			 return
		  }
	  }
  }

  def private String modiFySentenciesInContentHtmlAndCorrespondingSmilFiles(sentence)
  {
	    if (!sentence)
	  	return sentence
		  
	  def ret = ""
	  def matcher = sentence =~ /(?s)(<span\s+class="sentence"\s+id="(?<id>.*?)">[\s\n\r\c\t]*<a\s+href="(?<smilref>.*?)"\s*>(?<content>.*?)<\/a>[\s\n\t\c\r]*<\/span>)/
	  def iCnt = 0, iMatchRow = 0, id, indStart = -1, indEnd = -1, indMain = 0
	  def firstId = "", firstSmilref = "", newcontent = "", content = "", strFound	  
	  def removeIdElements = [], smilfilename, sb = new StringBuffer()
	  def firstContent, smilref, indsmilref = -1, dtdid, strBetweenStart
	  def bSbEndsWithSpace = false, bFoundStartsWithSpace = false
	  
	  while(matcher.find())
	  {
		  iCnt++
		  indStart = matcher.start()
		  indEnd = matcher.end()
		  bSbEndsWithSpace = sb.toString().endsWith(" ")
		  
		  strBetweenStart = sentence.toString().substring(indMain, indStart)
		  if (strBetweenStart && strBetweenStart.toString().replaceAll("\n\t", "").trim().length() > 0)
		  	  sb << strBetweenStart.toString().replaceAll("\n\t", "").trim() 
		  
		  if (iCnt == 1)
		  {
			   strFound = matcher[iMatchRow][1].toString()
			   firstId = matcher.group("id")
			   firstSmilref = matcher.group("smilref")
			   firstContent = matcher.group("content")			   
			   bFoundStartsWithSpace = firstContent.startsWith(" ")
			   sb << firstContent
			  // firstEndTime
			  // firstId
			  // firstStartTime
		  }
		  else
		  {
			   strFound= matcher[iMatchRow][1].toString()			   
			   id = matcher.group("id")
			   smilref = matcher.group("smilref")
			   /*
			   indsmilref = smilref.toString().indexOf("#")
			   if (indsmilref > -1)
					   dtdid = smilref.toString().substring(indsmilref +1)
			   */
			   content = matcher.group("content")
			   bFoundStartsWithSpace = content.startsWith(" ")
			   def strSb = sb.toString()
			   /*
			   if ((!strBetweenStart || strBetweenStart.toString().replaceAll("\n\t", "").trim().length() == 0) 
			       && !strSb.endsWith(" ") && !strSb.endsWith("\t"))
			   */
			   if (!bSbEndsWithSpace && !bFoundStartsWithSpace)
			   	   sb << " "			   
			   sb << content
			   removeIdElements.add id.toString()
		  }
		  iMatchRow++
		  indMain = indEnd
	  }
	  
	  strBetweenStart = sentence.toString().substring(indMain)
	  if (strBetweenStart  && strBetweenStart.toString().replaceAll("\n\t", "").trim().length() > 0)
			sb << strBetweenStart.toString().replaceAll("\n\t", "").trim()
	
	  
	  int indHMark = firstSmilref.toString().indexOf("#")
	  if (indHMark > -1)
		   smilfilename = firstSmilref.toString().substring(0, indHMark)
	  println "Id: " +firstId
	  newcontent = sb.toString().trim()
	  rewriteSmilFileSentenciesDaisy2(smilfilename, firstId, removeIdElements)
	  ret = "<span class=\"sentence\" id=\"" + firstId +"\">\n<a href=\"" + firstSmilref +"\">" +newcontent +"</a>\n</span>"
	  ret
  }
  
  def private String modiFySentenciesInDtbookAndCorrespondingSmilFiles(sentence)
  {
	  if (!sentence)
	  	return sentence
		  
/*		  
	  if (sentence.toString().contains("Vastaavalla tavalla tekemisell"))
	  {
	  	println "stop"
	  }
*/	    
	  def ret = ""
	  def matcher = sentence =~ /(?s)(<sent\s+(id="(?<regexid>.*?)"\s+smilref="(?<regexsmilref>.*?)"|smilref="(?<regexsmilref2>.*?)"\s+id="(?<regexid2>.*?)")\s*>(?<content>.*?)<\/sent>[\s\n\r]*)/
	  def iCnt = 0, iMatchRow = 0, indStart = -1, indEnd = -1, indMain = 0
	  def firstId = "", firstSmilref = "", newcontent = "", content = "", firstContent = ""	  
	  def removeIdElements = [], smilfilename, sb = new StringBuffer()
	  def id = "", smilref = "", strFound, strBetweenStart
	  int indHMark = -1
	  def bSbEndsWithSpace = false, bFoundStartsWithSpace = false
	  
	  while(matcher.find())
	  {
		  iCnt++
		  indStart = matcher.start()
		  indEnd = matcher.end()
		  bSbEndsWithSpace = sb.toString().endsWith(" ")
		  strBetweenStart = sentence.toString().substring(indMain, indStart)
		  if (strBetweenStart && strBetweenStart.toString().replaceAll("\n\t", "").trim().length() > 0)
				sb << strBetweenStart.toString().replaceAll("\n\t", "").trim()
	
		  if (iCnt == 1)
		  {
			   strFound= matcher[iMatchRow][1].toString()			   
			   firstId = matcher.group("regexid")
			   if (!firstId)
			   {
			   		firstId = matcher.group("regexid2")
			   }
			   firstSmilref = matcher.group("regexsmilref")
			   if (!firstSmilref)
			   {
			   		firstSmilref = matcher.group("regexsmilref2")
			   }
			   firstContent = matcher.group("content") 
			   bFoundStartsWithSpace = firstContent.startsWith(" ")			   
			   indHMark = firstSmilref.toString().indexOf("#")
			   if (indHMark > -1)
		  	 	  smilfilename = firstSmilref.toString().substring(0, indHMark)
	  
			   sb << firstContent 
			  // firstEndTime
			  // firstId
			  // firstStartTime
		  }
		  else
		  {
			   strFound= matcher[iMatchRow][1].toString()
			   id = matcher.group("regexid")
			   if (!id)
			   {
				 id = matcher.group("regexid2")
			   }
			   smilref = matcher.group("regexsmilref")
			   if (!smilref)
			   {
				  smilref = matcher.group("regexsmilref2")
			   }
			   content = matcher.group("content")	
			   bFoundStartsWithSpace = content.startsWith(" ")
			   def strSb = sb.toString()
			   /*
			   if ((!strBetweenStart || strBetweenStart.toString().replaceAll("\n\t", "").trim().length() == 0)
				   && !strSb.endsWith(" ") && !strSb.endsWith("\t"))
			   */
			   if (!bSbEndsWithSpace && !bFoundStartsWithSpace)
					  sb << " "
			   sb << content
			   removeIdElements.add id.toString()
		  }
		  iMatchRow++
		  indMain = indEnd
	  }
	  
	  strBetweenStart = sentence.toString().substring(indMain)
	  if (strBetweenStart && strBetweenStart.toString().replaceAll("\n\t", "").trim().length() > 0)
			sb << strBetweenStart.toString().replaceAll("\n\t", "").trim()
			
	  println "Id: " +firstId
	  rewriteSmilFileSentenciesDaisy3(smilfilename, firstId, removeIdElements)
	  newcontent = sb.toString()
	  ret = "<sent id=\"$firstId\" smilref=\"$firstSmilref\" >$newcontent</sent>\n"
	  ret
  }
  
  def private String [] getNewBoldEtcItems(String value)
  {
		if (!value)
			return value
		def ret = value.split(",")
		ret.collect().each { it.toString().trim() }
  }
  
  def private getSentenceregex(regex)
  {
	   if (m_boldetcxmlelementsinmodifiedneighboursentencies)
	  {
		  def newitems = getNewBoldEtcItems(m_boldetcxmlelementsinmodifiedneighboursentencies)
		  def strRegex = regex.toString()
		  def firstSearch = "<em>|<strong>"
		  int ind = strRegex.indexOf(firstSearch)
		  if (ind > -1)
		  {
			  def secondSearch = "</em>|</strong>"
			  int indEnd = strRegex.indexOf(secondSearch)
			  if (indEnd > -1)
			  {
				  def start = strRegex.substring(0,ind)
				  def between = strRegex.substring(ind +firstSearch.length(), indEnd)
				  def last = strRegex.substring(indEnd +secondSearch.length())
				  def newFirst = newitems.collect { return "<" +it.toString() +">|" }.toString().replace("[", "").replace("]", "").replaceAll("\\s", "").replaceAll(",", "")
				  int indFirst = newFirst.toString().lastIndexOf(">|")
				  if (indFirst > -1)
				  	newFirst = newFirst.substring(0,indFirst) +">"
				  def newSecond = newitems.collect { "</" +it.toString() +">|" }.toString().replace("[", "").replace("]", "").replaceAll("\\s", "").replaceAll(",", "")
				  int indSecond = newSecond.toString().lastIndexOf(">|")
				  if (indSecond > -1)
				  	newSecond = newSecond.substring(0,indSecond) +">"
				  return start +newFirst +between +newSecond + last
			  }
		  }
	  }
	  regex
  }

  def private void removeNeigthBornSentencesInAllElementsDaisy2()
  {
	  println "\n" +getMessages().getString(Lipsync2Daisy.constUI_ui_modifying) +" daisy2 " +getMessages().getString(Lipsync2Daisy.constUI_ui_documentselements)+":"
	  
	  def contenthtml = readOutputDirFile(this.content_file_name, false)
	  // def matcher = contenthtml =~ /(?s)(?<=<p>)(.*?)(?=<\/p>)/
	  def matchersentence, i = 0, iSentences = 0, groupCount = 0
	  def sentencelist = [], sb = new StringBuffer(), sentence, sbSentence = new StringBuffer()
	  def iSentenceCnt = 0, iSentenceInd = -1, ind = 0, strPContent, strBetween
	  def search = "<span ", iEnd = 0, iStart = 0, indMain = 0, iPStart, iPEnd, iPMain = -1
	  def strStart, strPEnd, strParBlock
	  def sentenceregex = getSentenceregex(/(?s)([\s\t\c\r\n]*(<em>|<strong>)*[\s\t\c\r\n]*<span\s+class="sentence"\s+id="(.*?)">[\s\n\c\r\t]*<a\s+href="(.*?)">(.*?)<\/a>[\s\n\c\r\t]*<\/span>[\s\t\c\r\n]*(<\/em>|<\/strong>)*[\s\t\c\r\n]*)+/)	
	  // m_boldetcxmlelementsinmoifiedneighboursentencies
	  	  
		  matchersentence = contenthtml =~ sentenceregex 
		  while(matchersentence.find())
		  {
			  iPStart = -1
			  iPEnd = -1
			  
			  iSentences = 0
			  iStart = matchersentence.start()
			  iEnd = matchersentence.end()
			  		   
			  strParBlock = matchersentence[i++][1]		 
			  sentencelist = []

			  sbSentence = new StringBuffer()
		  	
			  groupCount = matchersentence.groupCount()
			  sentence = matchersentence.group()
	
	  		  strStart = contenthtml.substring(indMain, iStart)
			  if (strStart)
			  	sb << strStart
		  
			  iSentenceCnt = 0
			  iSentenceInd = 0
			  while((ind = sentence.indexOf(search, iSentenceInd)) > -1)
			  {
				  iSentenceInd = ind +search.length()
				  iSentenceCnt++
			  }
			  if (iSentenceCnt > 1)
			  {
				  // sbSentence << sentence
				  sentence = modiFySentenciesInContentHtmlAndCorrespondingSmilFiles(sentence)
				  // println "iSentenceCnt > 1"
			  }
			  sb << sentence
			  strPEnd = contenthtml.substring(iEnd)
			  indMain = iEnd
		   }
		  
		  strPEnd = contenthtml.substring(iEnd)
		  if (strPEnd)
			  sb << strPEnd
			  
			  indMain = iEnd
	  writeIntoOutputDirFile(this.content_file_name, sb.toString(),  true, false)
  }
  
  def private void removeNeigthBornSentencesInPElementsDaisy2()
  {
	   println "\n" +getMessages().getString(Lipsync2Daisy.constUI_ui_modifying) +" daisy2 " +getMessages().getString(Lipsync2Daisy.constUI_ui_documentselements)+":"	 
	  
	  def contenthtml = readOutputDirFile(this.content_file_name, false)
	  def matcher = contenthtml =~ /(?s)(?<=<p>)(.*?)(?=<\/p>)/
	  def matchersentence, i = 0, iSentences = 0, groupCount = 0
	  def sentencelist = [], sb = new StringBuffer(), sentence, sbSentence = new StringBuffer()
	  def iSentenceCnt = 0, iSentenceInd = -1, ind = 0, strPContent, strBetween
	  def search = "<span ", iEnd = 0, iStart = 0, indMain = 0, iPStart, iPEnd, iPMain = -1
	  def strPStart, strPEnd 
	  
	  while(matcher.find())
	  {
		  iSentences = 0
		  iStart = matcher.start()
		  iEnd = matcher.end()
		  
		  strPContent = matcher[i++][1].toString()
		  /*
		  if (strPContent.contains("class=\"page-normal\""))
		  {
			  println "page"
		  }
		  */
		  
		  matchersentence = strPContent =~ getSentenceregex(/(?s)([\s\t\c\r\n]*(<em>|<strong>)*[\s\t\c\r\n]*<span\s+class="sentence"\s+id="(.*?)">[\s\n\c\r\t]*<a\s+href="(.*?)">(.*?)<\/a>[\s\n\c\r\t]*<\/span>[\s\t\c\r\n]*(<\/em>|<\/strong>)*[\s\t\c\r\n]*)+/)
		  sentencelist = []
		  sb << contenthtml.substring(indMain, iStart)
		  // sb << contenthtml.substring(iStart, iEnd)

		  sbSentence = new StringBuffer()
		  iPMain = -1
		  iPStart = -1
		  iPEnd = -1
		  
		  /*
		  if (indMain > -1)
		  {
			  strBetween = contenthtml.substring(indMain, iStart)
			  if (strBetween)
				  sb << strBetween
		  }
		  */
	
		  while(matchersentence.find())
		  {
			  // sbSentence = new StringBuffer()
			  groupCount = matchersentence.groupCount()
			  sentence = matchersentence.group()
			  iPStart = matchersentence.start()
			  iPEnd = matchersentence.end()
			  // sbSentence << sentence
			  
			  if (iPMain == -1)
			  {
				  strPStart = strPContent.substring(0, iPStart)
				  if (strPStart)
					  sbSentence << strPStart
			  }
			  else
			  {
				  strPEnd = strPContent.substring(iPMain, iPStart)
				  if (strPEnd)
				  	sbSentence << strPEnd
			  }
		  
			  iSentenceCnt = 0
			  iSentenceInd = 0
			  while((ind = sentence.indexOf(search, iSentenceInd)) > -1)
			  {
				  iSentenceInd = ind +search.length()
				  iSentenceCnt++
			  }
			  if (iSentenceCnt > 1)
			  {
				  /*
				  if (sentence.contains("<strong>") || sentence.contains("</strong>") || sentence.contains("<em>") || sentence.contains("</em>"))
				  {
				  	  println "stop"
				  }
				  */
				  // sbSentence << sentence
				  sentence = modiFySentenciesInContentHtmlAndCorrespondingSmilFiles(sentence)
				  // println "iSentenceCnt > 1"
			  }
			  sbSentence << sentence
			  strPEnd = strPContent.substring(iPEnd)
			  iPMain = iPEnd
			  // sb << sbSentence.toString()
		   }
		  
		  strPEnd = strPContent.substring(iPEnd)
		  if (strPEnd)
			  sbSentence << strPEnd
			  
		   // matchersentence.appendTail(sbSentence);
		   // println "'" + sbSentence.toString() +"'"
		   sb << sbSentence.toString()
		   
		  indMain = iEnd
	  }	 
  	  sb << contenthtml.substring(iEnd)
	  // matcher.appendTail(sb)
	  writeIntoOutputDirFile(this.content_file_name, sb.toString(),  true, false)
  }
  
  def private void removeNeigthBornSentencesInPElementsDaisy3()
  {
	  println "\n" +getMessages().getString(Lipsync2Daisy.constUI_ui_modifyneighbornsentencies)+":"
	  
	  def dtbooktext = readOutputDirFile(this.dtbookfilename)
	  def matcher = dtbooktext =~ /(?s)(?<=<p>)(.*?)(?=<\/p>)/
	  def matchersentence, i = 0, iSentences = 0, groupCount = 0
	  def sentencelist = [], sb = new StringBuffer(), sentence, sbSentence = new StringBuffer()
	  def iSentenceCnt = 0, iSentenceInd = -1, ind = 0, iStartSentencies = -1
	  def search = "<sent ", iEnd = 0, iStart = 0, indMain = 0, istrBetweenSentencies = -1
	  def strBetweenSentencies, strStart, strEnd, strParBlock, strStartSentencies, strEndSentencies 
	  
	  /*
	  if (dtbooktext.contains("<page"))
	  {
	  	  println "dtbooktext"
	  }
	  */
	  	
	  while(matcher.find())
	  {
		  strParBlock = matcher[i++][1]
		  iSentences = 0
		  iStart = matcher.start()
		  iEnd = matcher.end()
		  // matchersentence = strParBlock =~ /(?s)((<sent\s+id="(.*?)"\s+smilref="(.*?)"\s*>(.*?)<\/sent>[\s\n\r]*)+)/
		  matchersentence = strParBlock =~ getSentenceregex(/((<\/*br\/*>|<em>|<strong>)*[\s\n\r]*<sent\s+.*?>(?<content>.*?)<\\/sent>*[\s\n\r]*(<\/*br\/*>|<\/em>|<\/strong>)*[\s\n\r]*)+/)		  
		  // id="(.*?)"\s+smilref="(.*?)"\s*
		  sentencelist = []
		  strStart = dtbooktext.substring(indMain, iStart)
		  if (strStart)
		  	 sb << strStart 
		  strEnd = dtbooktext.substring(iEnd)
		  // sb << dtbooktext.substring(iStart, iEnd)

		  sbSentence = new StringBuffer()
		  strStartSentencies = null
		  istrBetweenSentencies = -1
		  iStartSentencies = -1
		  
		  while(matchersentence.find())
		  {
			  if (istrBetweenSentencies > -1)
			  {
				  strBetweenSentencies = strParBlock.toString().substring(istrBetweenSentencies, matchersentence.start())
				  if (strBetweenSentencies)
					   sb << strBetweenSentencies
			  }

			  if (iStartSentencies == -1)
			  {
			  	  strStartSentencies = strParBlock.toString().substring(0, matchersentence.start())
				  if (strStartSentencies)
				     sb << strStartSentencies
				  iStartSentencies = 0
			  }
			  istrBetweenSentencies = matchersentence.end()			  
			  strEndSentencies = strParBlock.toString().substring(istrBetweenSentencies)
   
			  // id="(.*?)"\s+smilref="(.*?)"\s*
			  // sbSentence = new StringBuffer()
			  groupCount = matchersentence.groupCount()
			  sentence = matchersentence.group()
			  // sbSentence << sentence
		  
			  iSentenceCnt = 0
			  iSentenceInd = 0
			  while((ind = sentence.indexOf(search, iSentenceInd)) > -1)
			  {
				  iSentenceInd = ind +search.length()
				  iSentenceCnt++
			  }
			  if (iSentenceCnt > 1)
			  {
				  // sbSentence << sentence
				  sentence = modiFySentenciesInDtbookAndCorrespondingSmilFiles(sentence)
				  // println "iSentenceCnt > 1"
			  }
			  /*
			  strBetweenSentencies = dtbooktext.substring(indMain+1, indMain+matchersentence.start())
			  if (strBetweenSentencies)
			  	  sb << strBetweenSentencies
			  */
			  sb << sentence
			  /*
			  if (groupCount && groupCount > 1 ) // many sentences between earch another, merge those:
			  	matchersentence.appendReplacement(sb, getPossibleMergetSentencies(matchersentence));
			  // sentencelist.add matchersentence[iSentences++]
			   */
		   }
		  
		   // matchersentence.appendTail(sbSentence);
		   if (strEndSentencies)
				sb << strEndSentencies

		  indMain = iEnd
	  }
	  	 
	  if (strEnd)
  	  	sb << strEnd
	  // matcher.appendTail(sb)
			
	  println()	
	  writeIntoOutputDirFile(this.dtbookfilename, sb.toString() /* dtbooktext */,  true)
  }
  
  def private void removeNeigthBornSentencesInAllElementsDaisy3()
  {
	  println "\n" +getMessages().getString(Lipsync2Daisy.constUI_ui_modifyneighbornsentencies)+":"
	  
	  def dtbooktext = readOutputDirFile(this.dtbookfilename)
	  def matcher = dtbooktext =~ /(?s)(?<=<p>)(.*?)(?=<\/p>)/
	  def matchersentence, i = 0, iSentences = 0, groupCount = 0
	  def sentencelist = [], sb = new StringBuffer(), sentence, sbSentence = new StringBuffer()
	  def iSentenceCnt = 0, iSentenceInd = -1, ind = 0, iStartSentencies = -1
	  def search = "<sent ", iEnd = 0, iStart = 0, indMain = 0, istrBetweenSentencies = -1
	  def strBetweenSentencies, strStart, strEnd, strParBlock, strStartSentencies, strEndSentencies
	  
	  /*
	  if (dtbooktext.contains("<page"))
	  {
			println "dtbooktext"
	  }
	  */
		  
	  // while(matcher.find())
	  // {
		  // strParBlock = matcher[i++][1]
		  // iSentences = 0
		  // iStart = matcher.start()
		  // iEnd = matcher.end()
		  // matchersentence = strParBlock =~ /(?s)((<sent\s+id="(.*?)"\s+smilref="(.*?)"\s*>(.*?)<\/sent>[\s\n\r]*)+)/
		  matchersentence = dtbooktext =~ getSentenceregex(/((<\/*br\/*>|<em>|<strong>)*[\s\n\r]*<sent\s+.*?>(?<content>.*?)<\\/sent>*[\s\n\r]*(<\\/*br\/*>|<\/em>|<\/strong>)*[\s\n\r]*)+/)
		  while(matchersentence.find())
		  {
			  iSentences = 0
			  iStart = matchersentence.start()
			  iEnd = matchersentence.end()
			  strParBlock = matchersentence[i++][1]			 
			  
			  // id="(.*?)"\s+smilref="(.*?)"\s*
			  sentencelist = []
			  strStart = dtbooktext.substring(indMain, iStart)
			  if (strStart)
				   sb << strStart
			  
			  strEnd = dtbooktext.substring(iEnd)
			  // sb << dtbooktext.substring(iStart, iEnd)
	
			  sbSentence = new StringBuffer()
			  strStartSentencies = null
			  iStartSentencies = -1
			  
			  /*
			  if (istrBetweenSentencies > -1)
			  {
				  strBetweenSentencies = strParBlock.toString().substring(istrBetweenSentencies, matchersentence.start())
				  if (indMain != 0 && strBetweenSentencies)
					   sbSentence << strBetweenSentencies				  
			  }
			  */
	
			  	/*
				  if (iStartSentencies == -1)
				  {
					  strStartSentencies = dtbooktext.toString().substring(0, matchersentence.start())
					  if (indMain == 0 && strStartSentencies)
						 sb << strStartSentencies
					  iStartSentencies = 0
				  }
				  */
			  istrBetweenSentencies = matchersentence.end()
			  strEndSentencies = dtbooktext.toString().substring(istrBetweenSentencies)
	   
				  // id="(.*?)"\s+smilref="(.*?)"\s*
				  // sbSentence = new StringBuffer()
				  groupCount = matchersentence.groupCount()
				  sentence = matchersentence.group()
				  // sbSentence << sentence
			  
				  iSentenceCnt = 0
				  iSentenceInd = 0
				  while((ind = sentence.indexOf(search, iSentenceInd)) > -1)
				  {
					  iSentenceInd = ind +search.length()
					  iSentenceCnt++
				  }
				  if (iSentenceCnt > 1)
				  {
					  // sbSentence << sentence
					  sentence = modiFySentenciesInDtbookAndCorrespondingSmilFiles(sentence)
					  // println "iSentenceCnt > 1"
				  }
				  /*
				  strBetweenSentencies = dtbooktext.substring(indMain+1, indMain+matchersentence.start())
				  if (strBetweenSentencies)
						sb << strBetweenSentencies
				  */
				  sb << sentence
				  // sb << sbSentence.toString()
				  /*
				  if (groupCount && groupCount > 1 ) // many sentences between earch another, merge those:
					  matchersentence.appendReplacement(sb, getPossibleMergetSentencies(matchersentence));
				  // sentencelist.add matchersentence[iSentences++]
				   */	
				  indMain = iEnd
		   }
		  
		   // matchersentence.appendTail(sbSentence);
		   // println "'" + sbSentence.toString() +"'"
		   // sb << strEnd.toString()
		   //if (strEndSentencies)
				//sb << strEndSentencies

		  indMain = iEnd
	  // }
		   
	  if (strEnd)
			sb << strEnd
	  // matcher.appendTail(sb)
		
	  println()
	  writeIntoOutputDirFile(this.dtbookfilename, sb.toString() /* dtbooktext */,  true)
 }

 def public void lipsyncfilename_println(value)
  {
	  println value
  }

 def private getPagesValue(pages, iFirstPage)
  {
	  def ret = pages
	  try {
		if (iFirstPage instanceof String)
			iFirstPage = Integer.parseInt(iFirstPage)
		if (pages instanceof String)
			pages = Integer.parseInt(pages)
	  	ret = (pages - iFirstPage)+1
	  }catch(Exception e){
	  	if (iFirstPage && pages)
		    ret = pages
		else
			ret = iFirstPage
	  }
	  ret	  
  }
  
  /**
   * generoidaan daisy 2 tiedostot
   * 
   * @throws Exception
   */
  // def private int 
  def private void convertLipsync2SmilContentAfterDaisy2()
  throws Exception
  {
	  println "\n" +getMessages().getString(Lipsync2Daisy.constUI_ui_staringintodaisy).replace("%i", "2") +"..."
	  
	  voiceObjectMapValues = [:]
	  
	  StringBuffer sbContent = new StringBuffer ()
	  StringBuffer sbContentMeta = new StringBuffer ()
	  StringBuffer sbNcc = new StringBuffer ()
	  StringBuffer sbCss= new StringBuffer ()
	  StringBuffer sbSmil
	  File fSmil
	  
	  // alusta template tekstit:
	  VoiceDataFile.strSmilTemplate = strSmilTemplate 
	  VoiceDataFile.strH1Template = strH1Template
	  VoiceDataFile.strSeqTemplate = strSeqTemplate
	  VoiceDataFile.strSeqTemplate2 = strSeqTemplate2
	  VoiceDataFile.content_file_name = content_file_name
	  VoiceData.strParTemplate = strParTemplate
	  VoiceData.page_lipsync_time_on_off = page_lipsync_time_on_off	  
	  def cnstTimeFormatter = new DecimalFormat("######0.000")
	  VoiceData.executeMode = executetype
	  if (executetype == cnstExecuteDaisy2)
	  {
		  cnstTimeFormatter.setRoundingMode(RoundingMode.UNNECESSARY)
		  VoiceData.cnstTimeFormatter = cnstTimeFormatter
	  }
	  else
	  if (executetype == cnstExecuteDaisy3)
	  {
		  cnstTimeFormatter = new DecimalFormat("######:##:#0.000")
		  cnstTimeFormatter.setRoundingMode(RoundingMode.UNNECESSARY)
		  VoiceData.cnstTimeFormatter = cnstTimeFormatter
	  }
	  VoiceData.cnstTimeFormatter = cnstTimeFormatter
	  VoiceDataFile.dc_identifier  = dc_identifier 		  
	  VoiceDataFile.dc_title = dc_title
	  VoiceDataFile.region_id = region_id
	  VoiceDataFile.register = this
	  VoiceData.register = this
	  int iCnt = 0
	  double totaltime = 0.0
	  
	  def time_together = 0
	  // kkkk
	  
	  tocItems = 0
	  boolean firstitem = true
	  VoiceDataFile vPrev
	  def new_listVDFs = []
	  
	  for(VoiceDataFile v in listVDFs)
	  {
		  // lipsyncfilename_println v.file
		  currentxmlfilename = v.file.toString()
		  currentxmlfilenameprinted = false
		  
		  currentVoiceDataFile = v
		  iCnt++
		  if (!v.mp3_file_name)
		  {
		  	 v.mp3_file_name = getNextSmilMp3FileName()
			 if (iCnt == 1 && vdTitle && !vdTitle.mp3_file_name)
			 	vdTitle.mp3_file_name = v.mp3_file_name 
			 v.smil_file_name = getCurrentSmilFileName()
			 v.bSentenceWordMode = bSentenceWordMode
		  }
		  
		  // if (doctitle_on_off && iCnt == 1)
		  	// currentVoiceDataFile.isDocTitle = true
		  if (vPrev)
		  {
		  	  v.vd_previous = vPrev.vd_first
			  vPrev.vd_previous = v.vd_first
		  } 
		  currentVoiceDataFile.setBaseValuesOfConversion()
		  
		  v.prev_totaltime = totaltime
		  v.countDuration(firstitem, (vPrev == null ? 0.0 : vPrev.totaltime) ) // tka added 21.3.2013
		  totaltime = v.end

		  
		  sbContent << currentVoiceDataFile.convert2SmilXhtml()
		  sbContentMeta << currentVoiceDataFile.convertMetaSmilXhtml()
		  sbNcc << currentVoiceDataFile.convert2Ncc()
		  tocItems += currentVoiceDataFile.iNccItems
		  sbSmil = new StringBuffer()
		  sbSmil << currentVoiceDataFile.convert2Smil()
		  fSmil = new File(strOutputDir + File.separator +v.smil_file_name)
		  if (executetype == cnstExecuteDaisy2)
		  {
			  if (fSmil.exists())
			  {
			  	 if (fSmil.delete())
				   println getMessages().getString(Lipsync2Daisy.constUI_ui_old) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_file) +" " +fSmil +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_deleted) +"."
			  }
			  
			println getMessages().getString(Lipsync2Daisy.constUI_ui_writing) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_file) +": " +fSmil
		  	fSmil.append sbSmil.toString().replaceAll("(?s)\\p{Cntrl}&&[^\n]", ""), "UTF-8"
		  }
		  v.countDuration(firstitem, (vPrev == null ? 0.0 : vPrev.totaltime) ) // tka added 21.3.2013
		  old_totaltime += v.old_totaltime
		  firstitem = false
		  // time_together += v.totaltime // time_together
		  time_together = v.totaltime // tka: 8.3.2015
		  // if (totaltime < v.totaltime)  // tka into comments 21.3.2013
		  	 // totaltime = v.totaltime // tka into comments 21.3.2013
		  //if (time_together < v.time_together()) // tka into comments 21.3.2013
		  	// time_together = v.time_together() // time_together	 // tka into comments 21.3.2013
		  // if (totalpage < v.totalpage()) // tka into comments 21.3.2013
		  	// totalpage = v.totalpage() // tka into comments 21.3.2013
		  if (vPrev)
		  	new_listVDFs.add vPrev
		  vPrev = v
	  }
	  
	  new_listVDFs.add vPrev
	  listVDFs = new_listVDFs
	  
	 // println "time_together: " +time_together
	  //println "time_together/1000: " +time_together/1000
	  def timeDate = new Date(time_together.toLong())
	  SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss")	  
	  // tka 21.3.2012: time_together = (time_together/1000)/60
	  def str_time_together = "" +sdf.format(timeDate);
	  //str_time_together = str_time_together.toString().replace(".",":")
	  //println "str_time_together: " +str_time_together
	  // time_together = str_time_together
	  println()
	  
	  /*
	  for(entity in daisyIDs.openLipsyncEntity.reverse())
	  {
		  sbContent.append("\n</" + entity +">\n")
	  }	
	  */  	  
	  
	  // jälkikäsittely:
	  def strContent = sbContent.toString()
	  def strMetatext= sbContentMeta.toString()
	  	  
	  def contentbinding = [ "metatext": strMetatext, 
						   "bodytext": strContent ]
	  if (!contentbinding)
	  	throw new Exception("contentbinding is null!")
		  
	  if (!engine)
	  	throw new Exception("engine is null!")
		  
	  if (!strContentTemplate)
	  	throw new Exception("strContentTemplate is null!")
		  
	  // if (!contentTemplate)
	  		contentTemplate = engine.createTemplate(strContentTemplate)
	  String strContentHtml = contentTemplate.make(contentbinding).toString()

	  /*
	  if (!strContent.contains("<!DOCTYPE"))
	  {
		  def search = "<head>"
		  int indHeaderStart = strContent.indexOf(search)
		  if (indHeaderStart > -1)
		  {
			  def before 		= strContent.substring(0, indHeaderStart)
			  def modContent 	= before + "\n" + xhtmlDoctype +"\n"
			  def after 		= strContent.substring(indHeaderStart)
			  if (!after.toLowerCase().contains("<meta http-equiv="))
			  {
				  indHeaderStart 	= after.indexOf(search)
				  before	 	 	= after.substring(indHeaderStart, indHeaderStart+search.length()) +"\n"
				  def modAfter 		= cont+ "\n" + after.substring(indHeaderStart+search.length())
				  after 			= before +modAfter				  
			  }
			  strContent		= modContent + after
		  } 			  
	  }
	  */
	  
	  int ind = strContentHtml.indexOf("")
	  File f = new File(strOutputDir + File.separator +content_file_name)
	  if (executetype == cnstExecuteDaisy2)
	  if (f.exists())
	  {
		 if (f.delete())
		 	println getMessages().getString(Lipsync2Daisy.constUI_ui_previous) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_file) +f +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_deleted) + +"."
	  }
	  	 
	  /*
	  if (f.asWritable("UTF-8"))
	  	println "ON"
	  else
	  	println "Ei"
	  */		  
		  
	  if (executetype == cnstExecuteDaisy2)
	  {
		  println getMessages().getString(Lipsync2Daisy.constUI_ui_writing) + " " +getMessages().getString(Lipsync2Daisy.constUI_ui_file) +": " +f
		  def newContentString = strContentHtml.replaceAll("(?s)([\n\\\\r]+)(\t*<div)", "\n\$2")
		  // move page span in wrong place into inside of list:
		  
		  for(elementname in movespanpage_elementnames)
		  {
		      //newContentString = newContentString.replaceAll("(?s)(</li>)[\n\\s.]*(<span\\s+class=\"page-normal\"\\s+id=\".*?\">.*?</span>)", "\$2\n\$1")
		      // newContentString = newContentString.replaceAll("(?s)(</p>)[\n\\s.]*(<span\\s+class=\"page-normal\"\\s+id=\".*?\">.*?</span>)", "\$2\n\$1")
			  newContentString = newContentString.replaceAll("(?s)(</$elementname>)[\n\\s.]*(<span\\s+class=\"page-normal\"\\s+id=\".*?\">.*?</span>)", "\$2\n\$1")
		  }
		  f.append(newContentString.replaceAll("(?s)\\p{Cntrl}&&[^\n]", ""), "UTF-8")
	  }

	  		  /*
	  FileOutputStream fos = new FileOutputStream(f3, false)
	  BufferedOutputStream bos = new BufferedOutputStream(fos)
	  OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8")
	  osw.write new String(sbContent.toString().getBytes("UTF-8"), "UTF-8") // , "UTF-8"
	  osw.close()
	  */
	  
	  /*	  
	  Writer out = new BufferedWriter(new OutputStreamWriter(
		  new FileOutputStream(strOutputDir + File.separator +content_file_name), "UTF-8"));
	  try {
		  out.write(sbContent.toString());
	  } finally {
		  out.close();
	  }
	  */
	  
	  if (dc_authrows.toString().contains(","))
	  {
		  def tmp = dc_authrows
		  dc_authrows = ""
		  for(a in tmp.split(","))
		  	dc_authrows += "				<meta name=\"dc:creator\" content=\"" +a +"\" />\n" 
	  }
	  
	  def depth = (VoiceDataFile.depth == null ? 0 : VoiceDataFile.depth)
	  //def pageNormal = VoiceDataFile.iFirstPage ? (pages - VoiceDataFile.iFirstPage+1) : pages
	  def pageNormal = VoiceDataFile.iFirstPage ? (maxpage - VoiceDataFile.iFirstPage-2) : maxpage
	  def pageMax = /* VoiceDataFile.iFirstPage ? (pages - VoiceDataFile.iFirstPage+1) : */ maxpage
	  
	  def nccbinding = [   "dc_date": dc_date,
		  				  "dc_identifier":  dc_identifier, 
						   "dc_title": dc_title,
						   "depth": depth,
						   "dc_authrows": dc_authrows,
						   "pagenormal": pageNormal, 
						   "pages": pageMax,
						   "tocItems": tocItems,
						   "time_together": VoiceData.getClipTime(time_together),
						   "header": sbNcc.toString() ]
	  
	  // if (!nccTemplate)
	  		nccTemplate = engine.createTemplate(strNccTemplate)
	  String strNcc = nccTemplate.make(nccbinding).toString()

	  File f2 = new File(strOutputDir + File.separator +ncc_file_name)
	  if (executetype == cnstExecuteDaisy2)
	  if (f2.exists())
	  	if (f2.delete())
		  	println getMessages().getString(Lipsync2Daisy.constUI_ui_previous) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_file) +" " +f2 +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_deleted) +"."

	  
	 if (executetype == cnstExecuteDaisy2)
	 {
		 println getMessages().getString(Lipsync2Daisy.constUI_ui_writing) +" " +getMessages().getString(Lipsync2Daisy.constUI_ui_file) +": " +f2
	 	 f2.append(strNcc.replaceAll("(?s)\\p{Cntrl}&&[^\n]", ""), "UTF-8")
	 }
	  
	  // sbCss.append daisyIDs.toCss()
	  
	  // if (executetype == cnstExecuteDaisy2)
	  	// println "\nGeneration is ready.\n"
	  
  	  saveUserProperties()
  }
  
  def readConsoleTextFields()
  {
	  strLipsyncCfg = console.textCfg.getText()
	  strLipsyncDataDir = console.textInputPath.getText()
	  strOutputDir = console.textOutputPath.getText()
	  // strSmilTemplateDir = console.textCfgPath.getText()
	  strLipsyncBaseCfgDir = console.textCfgPath.getText()
	  strSmilTemplateDir = strLipsyncBaseCfgDir +File.separator +"daisy2templates" 
	  try {
		  def value = Double.parseDouble(console.correctTimeText.getText())
		  VoiceData.fTimeshift_into_voicedatas = value
	  } catch(Exception e){
	    info e.getMessage()
	  	Lipsync2Daisy.severe(e)
	  }
  }
  
   def void saveGuiConversionPathSettings()
   {
	   Properties [] arrayGCPS = console.getGuiConversionPathSettingsArray();
	   File fGcps;
	   int max = JPanelEarlierSetups.maxbuttons;
	   for(int i = 1 ; i <= max; i++)
	   {
		   fGcps = new File(this.getUserHome() +File.separator +cnst_lasrtarticlesofuser +i +".properties")
		   if (fGcps.exists())
		   	  if (fGcps.delete())
				 println "Cannot remove file: " +fGcps
	   }
	   
	   int i = 0;
	   for(Properties gcps in arrayGCPS)
	   {
		   i++
		   fGcps = new File(this.getUserHome() +File.separator +cnst_lasrtarticlesofuser +i +".properties")
		   if (gcps == null || gcps.size() == 0)
		   {
			   continue
		   }
		   saveUserProperties(gcps, fGcps)
	   }
   }
   
   def Properties saveUserProperties(Properties gcps, File fGcps)
   {
   		 if (fGcps.isDirectory())
		 {
			 throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_userhomecfgfileisdir) +": " +fGcps)
		 }
		 FileWriter fw
		 try {
			 fw = new FileWriter(fGcps)
			 gcps.store(fw, getMessages().getString(Lipsync2Daisy.constUI_ui_userhomecfgfilecomment) +" / " +this.strAppName)			 
			 return gcps
		 }catch(Exception e){
			def msg = getMessages().getString(Lipsync2Daisy.constUI_ui_cannotfinduserhomecfgfile) +": " +fsettings
			 println msg
			 Lipsync2Daisy.severe(e)
			 return null
		 } finally {
		 	fw.close()
		 }
   }
   
   def public void readGuiConversionPathSettings()
   {
	   Properties [] arrayGCPS = console.getGuiConversionPathSettingsArray();
	   int max = arrayGCPS.size();
	   File fGcps;
	   for(int i = 0; i < max; i++ )
	   {
		   fGcps = new File(getUserHome() +File.separator +"lipsyn2daisybookguisettings"+i +".properties")
		   if (!fGcps.exists())
			   continue
		   arrayGCPS[i] = readGuiConversionPathSetting(fGcps)
	   }	  	
   }
  
   def private Properties readGuiConversionPathSetting(File fGcps)
   {
	   if (!fGcps.exists())
		   return null;
	   try {
		   Properties clProp = new Properties()
		   clProp.load(new StringReader(fGcps.getText("UTF-8")))
		   return clProp;
	   }catch(Exception e){
			def msg = getMessages().getString(Lipsync2Daisy.constUI_ui_cannotfinduserhomecfgfile) +": " +fsettings
		   println msg
		   Lipsync2Daisy.severe(e)
		   return null;
	   }
	   null
   }
  
  def Properties saveUserProperties(File fsettings) {
			  
		  // if (fsettings.exists())
		  // {
		  	  if (fsettings.isDirectory())
			  {
				throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_userhomecfgfileisdir) +": " +fsettings)
			  }
			  /*
			  if (!fsettings.isDirectory())
				  if (fsettings.exists() && !fsettings.delete())
				  {
					  throw new Exception(getMessages().getString(Lipsync2Smil.constUI_ui_cannotdelete) +": " +fsettings)
				  }
			  */
			  Properties settprop = new Properties ()
			  try {
				  FileReader fr = new FileReader(fsettings)
				  settprop.load(fr)
			  }catch(Exception e){
			  		def msg = getMessages().getString(Lipsync2Daisy.constUI_ui_cannotfinduserhomecfgfile) +": " +fsettings
				  println msg
				  Lipsync2Daisy.severe(e)
			  }

			  def propVariablePostExt = ""
			  if (executetype == cnstExecuteDaisy3)
			  	propVariablePostExt = "3"
				  
			  settprop.setProperty("fTimeshiftintovoicedatas", VoiceData.fTimeshift_into_voicedatas.toString())
			  settprop.setProperty("executetype", "" +executetype);
			  settprop.setProperty("calculatemp3filelength", "" +bCalculateMP3FileLengths);
			  settprop.setProperty("uilanguage", strUILanguage);			  
			  
			  if (fLipsyncCfg)
			  	settprop.setProperty(cnst_locale_prop_field_cfgfile +propVariablePostExt, strLipsyncCfg /* fLipsyncCfg.getAbsolutePath() */)
			  // settprop.setProperty(cnst_locale_prop_field_templatedir, this.strParTemplate)
		      if (executetype)
			  {
				  if (executetype == cnstExecuteDaisy2)
				  {
					  if (strSmilTemplateDir)
				  		settprop.setProperty(cnst_locale_prop_field_templatedir +propVariablePostExt, this.strSmilTemplateDir)
				  }
				  else
				  if (executetype == cnstExecuteDaisy3)
				  {
					  if (strSmilTemplateDir3)
						settprop.setProperty(cnst_locale_prop_field_templatedir +propVariablePostExt, this.strSmilTemplateDir3)
				  }
			  }
			  if (fLipsyncDataDir)
			  	settprop.setProperty("readdir" +propVariablePostExt, fLipsyncDataDir.getAbsolutePath())
			  if (strLipsyncBaseCfgDir)
			  	 settprop.setProperty("cfgpath" +propVariablePostExt, strLipsyncBaseCfgDir)
			  if (fOutputDir)
			  	settprop.setProperty("outputdir" +propVariablePostExt, fOutputDir.getAbsolutePath())
				
			  if (currentLocale)
			  {
				  def lang = currentLocale.getLanguage()
				  def country =  currentLocale.getCountry()
				  if (lang && country)
				  {
					  settprop.setProperty("userlocale", lang +"_" +country)
				  }
			  }
			  
			  
  
			  FileWriter fw = new FileWriter(fsettings)
			  settprop.store(fw, getMessages().getString(Lipsync2Daisy.constUI_ui_userhomecfgfilecomment) +" / " +this.strAppName)
			  fw.close()
			  return settprop
		  // }
	  // }
  }
  
  def generateIdValues(voiceObject, Map variableValues)
  {
	  if (!voiceObject)
	  	throw new Exception("voiceObject is null!")
	  if (variableValues == null)
	  	throw new Exception("variableValues is null!")
	  if (variableValues.isEmpty())
	  	throw new Exception("variableValues is empty!")
      if (voiceObjectMapValues.get(voiceObject))
	  		return // jo asetettu aikaisemmin 
	  voiceObjectMapValues.put(voiceObject, getMapValues(voiceObject.class, variableValues))
  }
  
  def Map getGeneratedValues(voiceObject)
  {
	  if (!voiceObject)
		  throw new Exception("voiceObject is null!")
		  
	  voiceObjectMapValues.get(voiceObject)
  }

  def Map getMapValues(Class clss, Map variableValues)
  throws Exception
  {
	  if (!clss)
		  throw new Exception("class is null!")
	  if (!variableValues)
		  throw new Exception("variableValues is null!")

	  null
  }
  
  def getNextGlobalRegisterCounter() { ++cntGlobalRegisterCounter }
  def getCurrenttGlobalRegisterCounter() { cntGlobalRegisterCounter }
  def private getNextSmilFileCounter() { ++cntSmilFileCounter }
  def private getCurrentSmilFileCounter() { cntSmilFileCounter }
  def private getCurrentNccCounter() { cntNccRegisterCounter }
  def private getNextNccCounter() { ++cntNccRegisterCounter }
  

  def String getNextSmilMp3FileName()
  {
	  int iNextNumber = getNextSmilFileCounter()
	  def ret  = smilbasefilename + (""+iNextNumber).padLeft(4).replaceAll(" ", "0") +".mp3"
	  ret
  }
  
  def static String getSmilMp3FileNameOfLipsyncXmlFName(String strNumber)
  {
	  def ret  = smilbasefilename + (""+strNumber).padLeft(4).replaceAll(" ", "0") +".mp3"
	  ret
  }

  def String getCurrentSmilMp3FileName()
  {
	  smilbasefilename + (""+getCurrentSmilFileCounter()).padLeft(4).replaceAll(" ", "0") +".mp3"
  }

  def String getCurrentSmilFileName()
  {
	  smilbasefilename + (""+getCurrentSmilFileCounter()).padLeft(4).replaceAll(" ", "0") +".smil"
  }

  def String getNextH1Id()
  {
	  Integer.toHexString(++cntGlobalRegisterCounter)
  }
  
  def String getNextNccId()
  {
	  Integer.toHexString(getNextNccCounter())
  }

  /**
   * Tallettaa ohjelmaa lopetettaessa user home .properties arvot gui:sta
   * 
   * @param textCfg
   * @param textInputPath
   * @param textOutputPath
   * @param p_strSmilTemplateDir
   * @param p_executeType
   */
  public Properties saveUserPropertiesFromGui(String textCfg, String textInputPath, String textOutputPath, String p_strCfgDir, p_executeType, String strCorrectTime, boolean bCorrectMp3Files, File saveFile = null)
  {
	  bGui = true // tässä saattaa olla uusi instanssi gui:sta, siksi uudelleen tilojen asetus	  
	  this.strLipsyncCfg 		= textCfg
	  this.strLipsyncDataDir 	= textInputPath
	  this.strOutputDir 		= textOutputPath
	  this.bCalculateMP3FileLengths = bCorrectMp3Files; 
	  //this.strSmilTemplateDir = p_strCfgDir
	  if (!p_strCfgDir)
	  {
		  println getMessages().getString(Lipsync2Daisy.constUI_ui_givetextfieldvalue) +"!: cfg dir"
		  return null
	  }
	  this.strLipsyncBaseCfgDir = p_strCfgDir
	  this.strSmilTemplateDir   = p_strCfgDir +File.separator +cnstExecuteDaisy2CfgSubDir
	  if (executetype == cnstExecuteDaisy3)
	  {
	  	this.strSmilTemplateDir3 = p_strCfgDir +File.separator +cnstExecuteDaisy3CfgSubDir
		this.strSmilTemplateDir   = this.strSmilTemplateDir3
	  } 
		  
	  if (strLipsyncCfg == null || strLipsyncCfg.trim().size() == 0)
	  	fLipsyncCfg = null
	  else
	  	fLipsyncCfg = new File(strLipsyncCfg)
		  
	  if (strOutputDir == null || strOutputDir.trim().size() == 0)
		  fOutputDir = null
	  else
	  	fOutputDir = new File(strOutputDir)
		  
	  if (strLipsyncDataDir == null || strLipsyncDataDir.trim().size() == 0)
		fLipsyncDataDir = null
	  else
	  	fLipsyncDataDir = new File(strLipsyncDataDir)

	  try {
		  def value = Double.parseDouble(strCorrectTime)
		  VoiceData.fTimeshift_into_voicedatas = value
	  } catch(Exception e){
	  	println e.getMessage()
	  	Lipsync2Daisy.severe(e)
		 return null
	  }

	  if (saveFile == null)	  
	  	return this.saveUserProperties()
	  else
	  	return this.saveUserProperties(saveFile)
  }
  
  def public Properties saveUserProperties()
  {
	  str_user_home = System.getProperty("user.home")
	  if (str_user_home && bGui )
	  {
		  def File fsettings = new File(str_user_home +File.separator +strClassName +".properties")
		  return saveUserProperties(fsettings)
	  }
	  return null;
  }
  
  /**
   * This method will delete all old generated files before a new generation, because
   * there can be daisy2 result files among result daisy3 files or vice versa.
   * 
   * @param p_strOutputDir Used output dir
   */
  def private boolean deleteOldGeneratedFilesFromResultDir(String p_strOutputDir)
  {
  	 if (!p_strOutputDir)
	   return false
	 File fileOutDir = new File(p_strOutputDir)
	 if (!fileOutDir.exists())
	 {
	 	if (!fileOutDir.mkdir())
		{
			JOptionPane.showConfirmDialog (console, 
				getMessages().getString(Lipsync2Daisy.constUI_ui_cannotcreateoutputdir) +": " +fileOutDir.getCanonicalPath() +"\n", getMessages().getString(Lipsync2Daisy.constUI_ui_warning), JOptionPane.OK_OPTION);
			return false			
		}
	 }
	 else
	 {
		 if (fileOutDir. isFile())
		 {
			 JOptionPane.showConfirmDialog (console,
				 getMessages().getString(Lipsync2Daisy.constUI_ui_is_not_dir) +": " +fileOutDir.getCanonicalPath() +"\n", getMessages().getString(Lipsync2Daisy.constUI_ui_warning), JOptionPane.OK_OPTION);
			 return false
		 }

		 println()
		 println getMessages().getString(Lipsync2Daisy.constUI_ui_deletingprevgenfiles) +": "	+fileOutDir
		 	 
		 fileOutDir.listFiles( // use groovy glosure as FileNameFilter isntance and at last delete fouded list files:
			 [accept:{dir, file-> file ==~ /content\.html|ncc\.html|speechgen.ncx|speechgen.opf|.*?\.smil|.*?\.xml/ }] as FilenameFilter
		   ).toList().each { 
		     it.delete()
			 println getMessages().getString(Lipsync2Daisy.constUI_ui_deleted) +": " +it
		   }
		 println()
	 }
  }

  def public void generateDaisyFiles()
  {
	  if (!readAllFiles())
	  {
		  println "readAllFiles() " +getMessages().getString(Lipsync2Daisy.constUI_ui_returned)+ ": false!"
			return
	  }
	  
	  deleteOldGeneratedFilesFromResultDir(strOutputDir)
	  if (executetype == cnstExecuteDaisy2)
	  {
		  convertLipsync2SmilContentAfterDaisy2()
		  if (bRemoveNeigthBornSentencesInPElements)
		  {
			  removeNeigthBornSentencesInPElementsDaisy2()
		  }
		  else
		  if (bRemoveNeigthBornSentencesInAllElements)
		  {
			  removeNeigthBornSentencesInAllElementsDaisy2()
		  }
	  }
	  else
	  if (executetype == cnstExecuteDaisy3)
	  {						VoiceData.cnstTime = ""
		  VoiceData.clipStrinEndValue = ""
		  convertLipsync2SmilContentAfterDaisy2()
		  convertLipsync2SmilContentAfterDaisy3()
		  if (bRemoveNeigthBornSentencesInPElements)
		  {
			  removeNeigthBornSentencesInPElementsDaisy3()
		  }
		  else
		  if (bRemoveNeigthBornSentencesInAllElements)
		  {
			  removeNeigthBornSentencesInAllElementsDaisy3()
		  }
	  }
	  if (Lipsync2Daisy.bCalculateMP3FileLengths)
	  {
		  println "\n............................................."
		  println getMessages().getString(Lipsync2Daisy.constUI_ui_old) +" totaltime: " +old_totaltime
		  println getMessages().getString(Lipsync2Daisy.constUI_ui_new) +" totaltime: " +totaltime
		  // println getMessages().getString(Lipsync2Smil.constUI_ui_new) +" time_together: " +time_together
		  println "...............................................\n"
	  }
	println "\n===================="
	println getMessages().getString(Lipsync2Daisy.constUI_ui_conversion_ready) +"."
	println "====================\n"

  }  
  /**
   * Ajo guista, generointi
   * 
   * @param textCfg
   * @param textInputPath
   * @param textOutputPath
   * @param p_strSmilTemplateDir
   * @param p_executeType
   */
  public Properties convertFromGui(String textCfg, String textInputPath, String textOutputPath, String p_strSmilTemplateDir, p_executeType, boolean p_bCalculateMP3FileLengths, String p_correctTimeText, boolean p_bXmlValidation, String uilanguage)
  {
	  bXmlValidation = p_bXmlValidation
	  if (!textCfg?.trim())
	  {
		  JOptionPane.showConfirmDialog (console,
			  getMessages().getString(Lipsync2Daisy.constUI_ui_warningemptycfgfile), getMessages().getString(Lipsync2Daisy.constUI_ui_warning), JOptionPane.ERROR_MESSAGE);
		  return null
	  }
	  if (!p_strSmilTemplateDir?.trim())
	  {
		  JOptionPane.showConfirmDialog (console,
			  getMessages().getString(Lipsync2Daisy.constUI_ui_warningemptycfgpath), getMessages().getString(Lipsync2Daisy.constUI_ui_warning), JOptionPane.ERROR_MESSAGE);
		  return null
	  }
	  if (!textInputPath?.trim())
	  {
		  JOptionPane.showConfirmDialog (console,
			  getMessages().getString(Lipsync2Daisy.constUI_ui_warningemptyinputpath), getMessages().getString(Lipsync2Daisy.constUI_ui_warning), JOptionPane.ERROR_MESSAGE);
		  return null
	  }
	  if (!textOutputPath?.trim())
	  {
		  JOptionPane.showConfirmDialog (console,
			  getMessages().getString(Lipsync2Daisy.constUI_ui_warningemptyoutputpath), getMessages().getString(Lipsync2Daisy.constUI_ui_warning), JOptionPane.ERROR_MESSAGE);
		  return null
	  }	  
	  if (textInputPath.trim() == textOutputPath.trim())
	  {
		  JOptionPane.showConfirmDialog (console,
			  getMessages().getString(Lipsync2Daisy.constUI_ui_warningsamediroutputinput), getMessages().getString(Lipsync2Daisy.constUI_ui_warning), JOptionPane.ERROR_MESSAGE);
		  return null
	  }
	  
	  bGui = true // tässä saattaa olla uusi instanssi gui:sta, siksi uudelleen tilojen asetus
	  this.strLipsyncCfg 		= textCfg
	  this.strLipsyncDataDir 	= textInputPath
	  this.strOutputDir 		= textOutputPath
	  this.strLipsyncBaseCfgDir = p_strSmilTemplateDir	  	  
	  this.setExecutetype(p_executeType)
	  this.strUILanguage = uilanguage
	  bCalculateMP3FileLengths = p_bCalculateMP3FileLengths
      strSmilTemplateDir3 = this.strLipsyncBaseCfgDir +File.separator +cnstExecuteDaisy3CfgSubDir		  		  
	  strSmilTemplateDir2 = this.strLipsyncBaseCfgDir +File.separator +cnstExecuteDaisy2CfgSubDir 
	  strSmilTemplateDir = this.strLipsyncBaseCfgDir
	  
	  VoiceDataFile.strOutputDir = strOutputDir
	  correctTimeText = p_correctTimeText
	  try {
		  def dvalue = Double.parseDouble(correctTimeText)
		  if (!dvalue.toString().endsWith(".0"))
		  {
			  println getMessages().getString(Lipsync2Daisy.constUI_ui_wrongtimefieldvalue) +": " +correctTimeText
			  return
		  }
		  VoiceData.fTimeshift_into_voicedatas = dvalue 
	  } catch(Exception e){
	  		def msg = getMessages().getString(Lipsync2Daisy.constUI_ui_isnotfloatpointnumber) +": " +correctTimeText
			  println msg
  		    Lipsync2Daisy.severe(e)
			return
	  }
	  // console.setEditIntoFalse()
	  
	  String [] args = new String[4]

	  try {
		  args[0] = strLipsyncCfg
		  args[1] = strSmilTemplateDir
		  args[2] = strLipsyncDataDir  
		  args[3] = strOutputDir 
	
		  readcommandlineparameters(args)
		  
		  generateDaisyFiles()
							
		  return saveUserProperties()
		  /*
		  Thread run = new Thread(new Runnable(){
			  public void run()
			  {
				  if (!readAllFiles())
		  	 		return	  
				  convertLipsync2SmilContentAfterDaisy2()
			  }
		  })	  	  
		  run.start()
		  */
	  }catch(Exception e){
	      println e.getMessage()
		  def sttrace = stackTraceToString(e)
		  severe e.getMessage() +" " +sttrace
		  severe(e)
		  return null
	  }
  }
  
  def boolean hasHasSeveralSentencies(VoiceData prev_vd)
  {		
	  if (!prev_vd)
	  	return false
	  if (prev_vd.name != VoiceData.cnstLipsyncXmlmark)
	  {		
		// if (!prev_vd.previous)
			return false
	  	// return hasHasSeveralSentencies(prev_vd.previous)
	  }
		  
	  VoiceData item = prev_vd.next
	  if (!item)  
	  	return false
	  boolean founded = false
	  int iCounter = 0
	  while(!founded && item 
		  && !item.text.toString().toLowerCase().startsWith("</" +prev_vd.text.toString().substring(1)))
	  {
		  if (item.name == VoiceData.cnstLipsyncWord)
		  {
			  iCounter++
			  if (iCounter > 1)
			  {
				  founded = true
				  break
			  }
		  }
		  // def static final cnstLipsyncXmlmark = "xmlmark")
		  item = item.next
	  }
	  
	  founded
  }
  
  def int getHasSeveralSentencies(VoiceData prev_vd)
  {
	  if (!prev_vd)
		  return 0
	  if (prev_vd.name != VoiceData.cnstLipsyncXmlmark)
	  {
		// if (!prev_vd.previous)
			return 0
		  // return hasHasSeveralSentencies(prev_vd.previous)
	  }
		  
	  VoiceData item = prev_vd.next
	  if (!item)
		  return 0
	  boolean founded = false
	  int iCounter = 0
	  while(!founded && item
		  && !item.text.toString().toLowerCase().startsWith("</" +prev_vd.text.toString().substring(1)))
	  {
		  if (item.name == VoiceData.cnstLipsyncWord)
		  {
			  iCounter++
		  }
		  // def static final cnstLipsyncXmlmark = "xmlmark")
		  item = item.next
	  }	  
	  iCounter
  }

  def String getDocTitle(String line)
  {
	  // "<doctitle.*?>(.*?)</doctitle>"
	  def match = line =~ /(?s:<doctitle.*?>(.*?)<\\/doctitle>)/
	  if (match.find())
	  {
		  def title = match[0][1]?.toString().replaceAll("[\r\n]","")
	  	  return title
	  }
	  String ret = ""
	  ret
  }
  
  def String [] getDocAuthors(String line)
  {
	  // "<docauthor.*?>(.*?)</docauthor>"
	  def match = line =~ /(?s:<docauthor.*?>(.*?)<\\/docauthor>)/
	  def list = []
	  def value
	  def int item = 0
	  while(match.find())
	  {
		  value = match[item++][0]
	  	  list.add value
	  }
	  return (String []) list.toArray()
  }

  def public int getExecutetype()
  {
	  return this.executetype
  }
  
  def public void setExecutetype(String p_executeMode)
  {	  
	  //  change ui text into selected execution type:
	  switch(p_executeMode)
	  {
		  case "daisy2uitext" :
		  case "2" :
		  case cnstUI_Combobox_daisy2uitext :
			    executetype = cnstExecuteDaisy2					   
				break
		  case "daisy3uitext":
		  case "3" :
		  case cnstUI_Combobox_daisy3uitext :
			    executetype = cnstExecuteDaisy3
				break
		  default:
			    throw new Exception(getMessages().getString(Lipsync2Daisy.constUI_ui_exectypewrongvalue) +": " +p_executeMode +"! " +getMessages().getString(Lipsync2Daisy.constUI_ui_exectypevaluesmustbevalues))
				break
	  }
	  strSmilTemplateDir3 = this.strLipsyncBaseCfgDir +File.separator +cnstExecuteDaisy3CfgSubDir
	  strSmilTemplateDir2 = this.strLipsyncBaseCfgDir +File.separator +cnstExecuteDaisy2CfgSubDir
	  // yyyy
	  
	  // if (console)
	  	// readUserProperties()
  }
  
  def String getDtbookxmlrows(boolean withVoiceDataId = false)
  {
	  StringBuffer sb = new StringBuffer ()
	  for (VoiceDataFile vf in listVDFs)
	  {
		  sb << vf.dtbookxmlrows(withVoiceDataId)
	  }
	  sb.toString()
  }
  
  def static boolean isNCXItem(VoiceData vd)
  {
	  if (Lipsync2Daisy.dtbook_pagenum_on_off != "off" && vd.iPage > 0 /* (vd.xmlText && vd.xmlText.toString().toLowerCase().startsWith("<pagenum ")
		  || (vd.text && vd.text.toString().toLowerCase().startsWith("<pagenum "))) */ )
	  	return true
	  if (!vd.isXmlMarkPunkt && vd.iH_level > 0)
		  return true
	   if (vd.iPage == -1 /* ei ole pagenum */ && vd.text && vd.text.toString().toLowerCase().startsWith("<") )
	   {
		  def elementname = vd.text.toString().split(" ")[0].toString().substring(1)
		  if (elementname && elementname in listNCXCustomTestElements)
		  	return true
	   }
  
	  false
  }
  
  
  
  public String stackTraceToString(Throwable e) {
	  StringBuilder sb = new StringBuilder();
	  for (StackTraceElement element : e.getStackTrace()) {
		  sb.append(element.toString());
		  sb.append("\n");
	  }
	  return sb.toString();
  }
  
  
}