package fi.celia.app.smil2voicesmil

import fi.celia.app.smil2voicesmil.daisy3.VoiceDataDaisy3;
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

import java.text.DecimalFormat
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar;

import org.apache.commons.lang3.time.DurationFormatUtils

/**
 * This class is used to read lipsync2 xml data into it. There are three kind of lipsync xml elemets
 * as a row: <xmlmark attributes ...>some dtbook xml data</xmlmark>,
 * <word msStart="835" msEnd="1365">Satujen</word>
 * and <punct attributes>some dtbook xml data; usually punkt character or som another special 
 * charachters</punct>. Lipsync xml attributes are followings:  msStart="835" msEnd="835".
 * Begin and end times, when some textual element, word or character is inside of voice file
 * in milliseconds from start of a voice file.
 *
 * Tämä luokka lukee yhtä lipsync-ohjelman tuottamaa .xml data-elementtiä (dtbook-xml- tai teksti-ääni-xml-elementtiä)
 * ja tällaiseen objektiin talletetaan luetut data-elementti ja sen tiedot. Sen lisäksi siinä on ajoon
 * liittyävät muut attribuutit, jokta kertovat minkälainen objekti se on ajon kannalta. Ks yllä olevaa
 * englannin kielistä kuvausta lisätietojen saamiseksi.
 * <p> 
 * Osa methodeista lipsync2smil- ja osa smil2voicesmil-ohjelmaa varten!
 * <p>
 * @author Tuomas Kassila
 *
 */
class VoiceData extends VoiceDataDaisy3 
{
	def static final cnstLipsyncWord = "word"
	def static final cnstLipsyncXmlmark = "xmlmark"
	def static final cnstLipsyncPunct = "punct"	
	
	def name // on myös elementin tyyppi
	def double start // alkuaika
	def double end // loppuaika
	def text // luettu rivin teksti, tai tulostettava teksti
	def used 		= false // käytetty vai ei, lähinnä aikaisempi smil2voicesmil-ohjelma
	def smilused 	= false
	def smilid // ennen generoitia annettava uniikki id arvo
	def iLine = 0 // luetun rivin numero
	def isXmlMarkPunkt = false // onko esim dtbook-data rivi
	static String strParTemplate // asetetaan ohjelmasta
	static boolean page_lipsync_time_on_off // asetetaan ohjelmasta
	def mp3_file_name // asetetaan ohjelmasta
	def smil_file_name // asetetaan ohjelmasta
	def audio_id // ennen generointia annettava uniikki id arvo
	def clip_begin, clip_end
	def par_id = ""
	def text_id = ""
	def content_id = ""
	def base_value_of_conversion =  0 // unset value
	def generate_base_value_set = false
	def isDocTitle = false // onko artikkelin doctitle arvo
	def isDocauthor = false // onko artikkelin kirjailija(t)
	def isAllowedPrint = true // saako tulostaa
	def isH1 = false // onko h1 otsake
	def iH_level = 0
	def isP  = false // onko kappale
	def static SimpleTemplateEngine engine = new SimpleTemplateEngine()
	def static Template template
	def VoiceData previous // edellinen 
	def VoiceData next // seuraava
	def static cnstTime = "npt="
	def static executeMode = Lipsync2Daisy.cnstExecuteDaisy2
	def static DecimalFormat cnstTimeFormatter
	def isWordPunct = false // onko lauseen lopettava piste tms
	def iPage = -1 // sivunumeron arvo jos ei -1
	def strPage = ""
	def isImage = false // onko kuva dataa
	def isAddedIntoList = false
	def xmlText
	def strReadedLine
	def strReadedFileName
	def static float fTimeshift_into_voicedatas = 0.0
	// def static DateFormat df = java.text.DateFormat.getTimeInstance();
	def static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
	def static Calendar cal = Calendar.getInstance();
	def static clipStrinEndValue = "s"
	def isStartTimeEndTimeSet = false
	def isSmillPar = false
	def static sentenceElements = []
	def static boolean bPrintWordSentencies = true
	def table_end_value // table seq end value in .smil file
	def bSeekSequenceEndOf = false // called allready of method seekSequenceEndOf(...)
	def isNCXItem = false // contains this item in ncx (daisy 3) file
	def isFirstItem = false 
	
	/**
	* register on itseasiassa Lipsync2Smil objekti, joka tallettaa
	* kulloisenkin VoiceDataFile tai kulloisenkin VoiceData objektin.
	* Sekä antaa uniikit id arvo jne. Eli on kutsuva ohjelma.
	*/
	def static Lipsync2Daisy register

	public VoiceData()
	{
		Object.metaClass.'static'.println = Lipsync2Daisy.new_pritln_method
	}
	
	/*
	def public boolean getIsWordPunct()
	{
		if (this.isWordPunct && next == null)
			return this.isWordPunct
		def isOkNext = false
		while(next && next.name != VoiceData.cnstLipsyncPunct && next.name != VoiceData.cnstLipsyncWord)
		{
			if (next.name != VoiceData.cnstLipsyncXmlmark)
				isOkNext = true
		} 
		isOkNext
	}
	*/
	
	def boolean isPunctBeforeSpace() { 
		if (!isWordPunct)
			return false
		if (text in ["("])
			return true
		return false
	}
	
	def boolean isPunctAfterNotSpace()
	{
		if (!isWordPunct)
			return false
		if (text in ["("])
			return true
		return false
	}
	
	public void setStart(double dValue)
	{
		/*
		if (fTimeshift_into_voicedatas != 0.0)
		{
			start = dValue + fTimeshift_into_voicedatas
		}
		else
		*/
			start = dValue
	}

	public void setEnd(double dValue)
	{
		/*
		if (fTimeshift_into_voicedatas != 0.0)
		{
			end = dValue + fTimeshift_into_voicedatas
		}
		else
		*/
			end = dValue
	}

	/**
	 * Mukava mehtodi attribuuttien arvojen selaamiseen 
	 */
	public String toString()
	{
		name + " used: " +used + " smilused: " +smilused +" smilid: " + smilid +" start: " +start +" end: " +end + " iLine: " +iLine + 
		   " text: " +text +" audio_id: " +audio_id +" par_id: " +par_id +" text_id: " +text_id +" content_id: " +content_id +" iPage: " +iPage		
	}
	
	/**
	 * Palautta mjonon smil-tiedostoa varten
	 * @return
	 */
	public String convert2Smil(boolean bConvertAllways = false, doctitle_start_time = null, doctitle_end_time = null)
	{
		if (!bConvertAllways && isXmlMarkPunkt && !isImage)
		{
			if (isXmlMarkPunkt)
			{
				if (text && text.toString().toLowerCase() != "<table>")
					return ""
			}
			else
			{
				return ""
			}
		}
		
		if (!clip_begin)
		{
			if (previous && previous.end < start)
				clip_begin = cnstTime + getClipTime((this.isFirstItem ? 0.0 : previous.end)) +clipStrinEndValue
			else
				clip_begin = cnstTime + getClipTime((this.isFirstItem ? 0.0 : start)) +clipStrinEndValue
		}
		if (!clip_end)
			clip_end = cnstTime + getClipTime(end) +clipStrinEndValue

		def binding
			
		def page = (iPage == -1 ? "" : 'system-required="pagenumber-on"')

		/*
		StringBuffer sb = new StringBuffer()
		if (doctitle_start_time && doctitle_end_time)
		{
			binding = ["par_id":  par_id, "text_id": text_id, "content_id": content_id,
				"smil_file_name" : mp3_file_name, "audio_id": audio_id,
				"clip_begin": dddd doctitle_start_time, "clip_end": doctitle_end_time ]
			System.gc()
			template = engine.createTemplate(strParTemplate).make(binding)
			sb << template.toString()
		}
		*/

		binding = ["par_id":  par_id, "text_id": text_id, "content_id": (iPage && iPage != -1 ? ("page" +iPage) : content_id),
			"smil_file_name" : mp3_file_name, "audio_id": audio_id, 
			"clip_begin": clip_begin, "clip_end": clip_end,
			"page": page ]
		// engine = new SimpleTemplateEngine()
		// System.gc()
		if (!template)
			template = engine.createTemplate(strParTemplate)
		def ret = template.make(binding).toString()
		ret
		// sb.append template.toString()
		// sb.toString()
	}
	
	/**
	 * Palauttaa mjonon clip aikaa varten
	 * 
	 * @param value
	 * @return
	 */
	public static String getClipTime(double value)
	{		
		try {
			if (fTimeshift_into_voicedatas != 0.0)
			{
				value = value + fTimeshift_into_voicedatas
			}
			
			if (executeMode == Lipsync2Daisy.cnstExecuteDaisy2)
			{
				if (value == 0.0)
					return "0.000"
				return cnstTimeFormatter.format((value / 1000)).toString().replace(",", ".")
			}
			else
			if (executeMode == Lipsync2Daisy.cnstExecuteDaisy3)
			{
				if (value == 0.0)
					return "0:00:00.000"
					
				/*
				// Calendar cal = Calendar.getInstance();
		        cal.set(cal.getTime().getHours(), cal.getTime().getMonth(), cal.getTime().getDate(), 0, 0, 0);
		        cal.set(Calendar.MILLISECOND, 0);
		        cal.setTimeInMillis((cal.getTimeInMillis() +value as Long));
		        String strTime = sdf.format(cal.getTime());
		        */
				String strTime = DurationFormatUtils.formatDuration((long)value, "HH:mm:ss.SSS", true);
		        // println(strTime);
				return strTime // strTime.substring(1)
			}
		} catch(Exception e){
			e.printStackTrace()
		}
		value
	}
	
	/**
	 * Palauta kesto
	 * 
	 * @return
	 */
	public double getDuration()
	{
		end - start
	}
	
	def  seekSequenceEndOf(String elementname, VoiceData prev_seekSequenceEndOf = null)
	{
		// if (prev_seekSequenceEndOf != null && this == prev_seekSequenceEndOf)
			//return prev_seekSequenceEndOf
			
		if (bSeekSequenceEndOf)
			return prev_seekSequenceEndOf
			
		def loopnext = next
		def iElementCounter = 1
		def prev = this
		def String loop_elementtext, loop_elementname
		def VoiceData look_seek_item = null
		
	   def last_word, bStarted = true
	   
	   while(iElementCounter != 0 && loopnext /* && loopnext.name == cnstLipsyncXmlmark */ && loopnext.text
			/* && loopnext.text.toString().toLowerCase() != "</$elementname>" */ )
	   {
		   loop_elementtext = loopnext.text.toString().toLowerCase()
		   loop_elementname = loop_elementtext?.replace("<", "").replace(">", "").replace("/", "").split(" ")[0]
		   if (loopnext.table_end_value == null && loop_elementtext && (loop_elementtext.startsWith("<table") || loop_elementtext.startsWith("<list")
			   || (loop_elementname in Lipsync2Daisy.getListCustomTestAttributeNames()
				   && !loop_elementtext.contains("/"))))
		   {
			   iElementCounter++
			   def tmp_last_word = loopnext.seekSequenceEndOf(loop_elementname)
			   iElementCounter--
			   last_word = loopnext
			   /*
			   if (tmp_last_word)
			   		last_word = tmp_last_word
			   else
			   		last_word = look_seek_item
			   */
			   look_seek_item = loopnext			   
			   if (tmp_last_word)			   
			   		loopnext = tmp_last_word 
		   }
		   else
		   {
			   if (bStarted && loopnext && loopnext.name != cnstLipsyncXmlmark)
			   {
				   start = loopnext.start
				   clip_begin = loopnext.clip_begin
				   bStarted = false				   
			   }
			   
			   try {
			   if (loopnext.name == cnstLipsyncWord && (look_seek_item == null || (loopnext.iLine > look_seek_item.iLine )))
			   {
				   // if (loopnext.iPage != -1)
				   	  last_word = loopnext
			   }
			   }catch(Exception e){
			   		println e.getMessage()
				   Lipsync2Daisy.severe(e)		   
			   }
			   
			   if (loopnext && loopnext.name == cnstLipsyncXmlmark && loopnext.text
				   && loopnext.text.toString().toLowerCase().startsWith("</$elementname"))
			   {
				   iElementCounter--
				   if (iElementCounter == 0)
				   {
					   // println "break"
					   break
				   }
			   }
		   }
	
		   prev = loopnext
		   loopnext = loopnext.next
	   }
	   
	   /*
	   if (loopnext && loopnext.text == "</" +elementname +">") // founded the pair last one
	   {
			println loopnext
			def searched_child_element_for_table = searchTableEndOfVoiceData(loopnext)
			if (searched_child_element_for_table)
			{
				table_end_value = "DTBuserEscape;" +searched_child_element_for_table.par_id +".end"
				end = searched_child_element_for_table.end
				return loopnext
			}
	   }
		*/
	   
	   if (last_word)
	   {		  
		   if (look_seek_item == null || (last_word.iLine > look_seek_item.iLine ))
		   {
			   table_end_value = "DTBuserEscape;" +last_word.par_id +".end"
			   end = last_word.end
		   }
		   else
		   // if (last_word == look_seek_item)
		   {
			   // table_end_value = "DTBuserEscape;" +last_word.id +".end"
			   try {
				   table_end_value = "DTBuserEscape;" +look_seek_item.par_id +".end"
				   end = look_seek_item.end
			   }catch(Exception e){
			   	 println e.getMessage()
				Lipsync2Daisy.severe(e)		
			   }			   
		   }		   
		   // table_end_value = last_word.clip_end
	   }
	   
	   /*
	   if (loopnext && loopnext.text.toString().toLowerCase() == "</$elementname>")
	   {
		   return
	   }
	   */
	   
	   /*
	   if (loopnext && loopnext.name != cnstLipsyncXmlmark)
	   {
		   start = loopnext.start
		   clip_begin = loopnext.clip_begin
	   }
	   else
	   {
		   println "kddkfd"
	   }
	   */
	   	   
	   /*
	   while(loopnext && loopnext.text && loopnext.text.toString().toLowerCase() != "</$elementname>" )
	   {
		   loop_elementtext = loopnext.text.toString().toLowerCase()
		   loop_elementname = loop_elementtext?.replace("<", "").replace(">", "").replace("/", "").split(" ")[0]
		   if (loopnext.table_end_value == null && loop_elementtext && (loop_elementtext.startsWith("<table") || loop_elementtext.startsWith("<list")
			   || (loop_elementname in Lipsync2Smil.getListCustomTestAttributeNames()
				   && !loop_elementtext.contains("/"))))
		   {
			   iElementCounter++
			   loopnext.seekSequenceEndOf(loop_elementname)
			   look_seek_item = loopnext
			   last_word = look_seek_item
		   }
		   else
		   if (loopnext && loopnext.name == cnstLipsyncXmlmark && loopnext.text
			   && loopnext.text.toString().toLowerCase().startsWith("</$elementname"))
			   iElementCounter--
		   if (loopnext.name == VoiceData.cnstLipsyncWord)
		   {
			   last_word = loopnext
		   }
		   prev = loopnext
		   loopnext = loopnext.next
	   }
	   */
	   /*
	   if (last_word)
	   {
		   if (last_word == look_seek_item)
		   {
			   table_end_value = "DTBuserEscape;" +last_word.id +".end"
		   }
		   else
		   {
			   table_end_value = "DTBuserEscape;" +last_word.par_id +".end"
		   }
		   end = last_word.end
		   // table_end_value = last_word.clip_end
	   }
	   else
	   {
		   println "dd"
	   }
	   */
	   // println "dd"
	   bSeekSequenceEndOf = true
	   loopnext
	}
	
	def String getXmlOf(VoiceData startVD, VoiceData endVD, boolean bAddlipsync2smilid = false)
	{
		if (startVD == null)
			return null
		if (endVD == null)
			return null
			
		def VoiceData currentVD = startVD
		def StringBuffer sb = new StringBuffer () 
		while(currentVD != null && currentVD != endVD)
		{
			if (bAddlipsync2smilid && currentVD.isXmlMarkPunkt)
			{
				if (currentVD.text.toString().contains("</"))
					sb.append currentVD.text +"\n"
				else
					sb.append currentVD.text.toString().replace(">"," ") + "lipsync2smilid='" +currentVD.par_id  +"' >\n"
			}
			else
				sb.append currentVD.text +"\n"
			currentVD = currentVD.next
		}
		if (currentVD == null)
			throw new Exception("currentVD is null before reached of: " +endVD +" (" +startVD +" - " +endVD +"!" )
			
		if (bAddlipsync2smilid)
			sb.append endVD.text +"\n"
		else
			sb.append endVD.text +"\n"

		return sb.toString()
	}
	
	def VoiceData searchTableEndOfVoiceData(VoiceData loopnext)
	{
		def startVD = this
		def endVD = loopnext
		def bAddlipsync2smilid = true 
		def xmlItems = getXmlOf(startVD, endVD, bAddlipsync2smilid)
		if (xmlItems == null || !xmlItems)
			return null
		def bValidate = false
		def Node xmlitem = Lipsync2Daisy.parseXml(xmlItems, bValidate)
		if (xmlitem == null)
			return null
		def NodeList children = xmlitem.children()
		if (children == null || children.size() == 0)
			return null
		int iChildren = children.size()
		def lastChild = children.get iChildren-1
		if (lastChild == null)
			return null
		def id = lastChild.@lipsync2smilid.toString()
		if (id)
		{
			VoiceData currentVD = startVD
			while(currentVD && currentVD != endVD && currentVD.par_id != id)
				currentVD = currentVD.next
			return currentVD
		}
		return null
	}
	
	def void seekSequenceEndOf_old(String elementname)
	{
		def loopnext = next
		def iElementCounter = 1
		def prev = this
		def String loop_elementtext, loop_elementname, look_seek_item
		
	   def last_word
		while(loopnext && loopnext.name == cnstLipsyncXmlmark && loopnext.text
			&& loopnext.text.toString().toLowerCase() != "</$elementname>" )
	   {
		   loop_elementtext = loopnext.text.toString().toLowerCase()
		   loop_elementname = loop_elementtext?.replace("<", "").replace(">", "").replace("/", "").split(" ")[0]
		   if (loopnext.table_end_value == null && loop_elementtext && (loop_elementtext.startsWith("<table") || loop_elementtext.startsWith("<list")
			   || (loop_elementname in Lipsync2Daisy.getListCustomTestAttributeNames()
				   && !loop_elementtext.contains("/"))))
		   {
			   iElementCounter++
			   loopnext.seekSequenceEndOf(loop_elementname)
			   look_seek_item = loopnext
			   last_word = look_seek_item
		   }
		   else
		   if (loopnext && loopnext.name == cnstLipsyncXmlmark && loopnext.text
			   && loopnext.text.toString().toLowerCase().startsWith("</$elementname"))
			   iElementCounter--
		   prev = loopnext
		   loopnext = loopnext.next
	   }
	   if (loopnext && loopnext.text.toString().toLowerCase() == "</$elementname>")
	   {
		   return
	   }
	   if (loopnext && loopnext.name != cnstLipsyncXmlmark)
	   {
		   start = loopnext.start
		   clip_begin = loopnext.clip_begin
	   }
	   else
	   {
		  ; //  println "kddkfd"
	   }
	   
	   while(loopnext && loopnext.text && loopnext.text.toString().toLowerCase() != "</$elementname>" )
	   {
		  loop_elementtext = loopnext.text.toString().toLowerCase()
		   loop_elementname = loop_elementtext?.replace("<", "").replace(">", "").replace("/", "").split(" ")[0]
		   if (loopnext.table_end_value == null && loop_elementtext && (loop_elementtext.startsWith("<table") || loop_elementtext.startsWith("<list")
			   || (loop_elementname in Lipsync2Daisy.getListCustomTestAttributeNames()
				   && !loop_elementtext.contains("/"))))
		   {
			   iElementCounter++
			   loopnext.seekSequenceEndOf(loop_elementname)
			   look_seek_item = loopnext
			   last_word = look_seek_item
		   }
		   else
		   if (loopnext && loopnext.name == cnstLipsyncXmlmark && loopnext.text
			   && loopnext.text.toString().toLowerCase().startsWith("</$elementname"))
			   iElementCounter--
		   if (loopnext.name == VoiceData.cnstLipsyncWord)
		   {
			   last_word = loopnext
		   }
		   prev = loopnext
		   loopnext = loopnext.next
	   }
	   if (last_word)
	   {
		   if (last_word == look_seek_item)
		   {
			   table_end_value = "DTBuserEscape;" +last_word.id +".end"
		   }
		   else
		   {
			   table_end_value = "DTBuserEscape;" +last_word.par_id +".end"
		   }
		   end = last_word.end
		   // table_end_value = last_word.clip_end
	   }
	   else
	   {
		   ; // println "dd"
	   }
	}
	
	/**
	 * Generoi registerin avulla uudet id:t jos ei ole ennen kutsuttu tälle objektille.
	 * 
	 * @return void
	 */
	def generateIdValues()
	{	
			
		/*
		if (text && (text.toString().startsWith("<table") || text.toString().startsWith("<list")
			|| elementname in Lipsync2Smil.getListCustomTestAttributeNames()))
		{
			println text.toString()
			def loopnext = next
			while(loopnext && loopnext.name == cnstLipsyncXmlmark && loopnext.text
				 && loopnext.text.toLowerCase() != "</$elementname>" )
				loopnext = loopnext.next
			if (loopnext.text.toLowerCase() == "</$elementname>")
			{
				return 
			}
			if (loopnext.name == VoiceData.cnstLipsyncWord)
			{
				this.start = loopnext.start
				this.clip_begin = loopnext.clip_begin
			}
			
			def last_word
			while(loopnext && loopnext.text && loopnext.text.toLowerCase() != "</$elementname>" )
			{
				if (loopnext.name == VoiceData.cnstLipsyncWord)
				{
					last_word = loopnext 
				}
				loopnext = loopnext.next
			}			
			if (last_word)
			{
				this.end = last_word.end
				table_end_value = "DTBuserEscape;" +last_word.par_id +".end" 
				// table_end_value = last_word.clip_end
			}	
		}
			*/		
				
		if (isDocTitle)
		{
			if (!par_id)
				par_id 			= register.doctitle_par_id
			if (!text_id)
				text_id 		= register.doctitle_text_id
			if (!content_id)
				content_id 		= register.doctitle_content_id
			if (!audio_id)
				audio_id 		= register.doctitle_audio_src_id
			return
		}

		if (!base_value_of_conversion)
		{ 
			base_value_of_conversion = register.getNextGlobalRegisterCounter()			
		}
		if (!audio_id)
			audio_id 		= register.audio_id_base 			+base_value_of_conversion
		if (!par_id)
			par_id 			= register.par_id_base 	 			+base_value_of_conversion
		if (!text_id)
			text_id 		= register.text_id_base			 	+base_value_of_conversion
		if (!content_id)
			content_id 		= register.content_id_base 			+base_value_of_conversion
			
		if (table_end_value == "DTBuserEscape;.end") // corrected end value after missing par_id value of seeksequence voicedata object 
			table_end_value == "DTBuserEscape;" + end +".end"
	}

	public void correctStartAndEndValuesAfterPipelineValidation()
	{
		if (iPage > 0 && this.start == this.end)
		{
			this.start = this.start -1 // for daisy pipeline validation: diffeent values
			// otherwise some errors in validation
		}
	}
	
	public void setBase_value_of_conversion(int value)
	{
		if (generate_base_value_set)
			return
		this.base_value_of_conversion = value
		generate_base_value_set = true
	} 	
		
	/**
	 * kesken: ei tarvitse kutsua!
	 * 
	 * @return
	 */
	def String convert2Ncc()
	{
		if (!isH1)
			return ""
		text // TODO:
	}
	
	def double totaltime()
	{	
		def value = end - start
		if (value < 0)
			println "totaltime on alle nollan: " +value
		value 
	}

	/*	
	public void getClip_begin()
	{
		
	}
	*/
	
def String getSmilTableStart(String classvalue)
	{
		def customtext = "customTest=\"$classvalue\""
        if (classvalue && classvalue in Lipsync2Daisy.listRemovedCustomTestAttributeNames)
			customtext = ""
		return "<seq class=\"$classvalue\" $customtext end=\"$table_end_value\" fill=\"remove\" id=\"$par_id\">"
	}

	
	def String dtbookxmlrow(boolean withVoiceDataId = false)
	{
		// handle cdata:
		if (!text)
			return text
		def ret = text.toString()
		if (ret && ret.contains("<![CDATA[")) // name == VoiceData.cnstLipsyncPunct)
		{
			ret = ret.replaceAll("<!\\[CDATA\\[", "").replaceAll("\\]\\]>", "")
		}
		if (withVoiceDataId)
		{
			if (isNCXItem)
			{
				def xmlid = content_id, xmlattribute = "xmltest_id"
				def strxmlid = xmlattribute +"='" +xmlid +"'"
				if (isXmlMarkPunkt)
				{
					if (ret.contains("/>"))
						ret = ret.replaceAll("/>", " " +strxmlid + "/>")
					else
					if (ret.contains(">"))
						ret = ret.replaceAll(">", " " +strxmlid + ">")
				}
				else
				if (isH1)
					ret = "<h" +iH_level +
					" " +strxmlid +">" +ret // +"</h" +iH_level +">"
				else
					ret = ret +" " +strxmlid
			}
			/*
			else
			{
				if (isXmlMarkPunkt && previous && previous.isNCXItem && previous.isWordPunct)
				{
					def xmlid = content_id, xmlattribute = "xmltest_id"
					def strxmlid = xmlattribute +"='" +xmlid +"'"
					if (ret.contains("/>"))
						ret = ret.replaceAll("/>", " " +strxmlid + "/>")
					else
						ret = ret.replaceAll(">", " " +strxmlid + ">")
				}
			}
			*/
		}
		
		return ret
	}
	
	public void println(String msg)
	{
		Lipsync2Daisy.println msg
	}
}
