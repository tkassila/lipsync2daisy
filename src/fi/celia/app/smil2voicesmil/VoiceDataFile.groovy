package fi.celia.app.smil2voicesmil

import java.awt.TextArea;
import java.lang.annotation.Retention;
import java.lang.Comparable

import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder

import fi.celia.app.smil2voicesmil.daisy3.Daisy3SmilFile
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

/**
 * 
 * This class is capable to read one Lipsync xml file. A set xml file contains one directory
 * and are produced by Lipsync application. The files corresponds readed (dtbook) text and
 * voice files. Lipsync application marks into xml where every word and punkt in every voice
 * files from beginning of a voice file.
 * <p>
 * This class is a container for VoiceData objects. Each VoiceData object holds a read xml datarow:
 * xmlmark, word, or punkt. 
 * 
 * Tämä luokka lukee Lipsync-ohjelman tuottamaa .xml datatiedoston ajettavassa ohjelmassa.
 * 
 * @author Tuomas Kassila
 *
 */
class VoiceDataFile extends Daisy3SmilFile implements Comparable {
	static DaisyIDs daisyids	
	
	def File file
	def basename
	def VoiceData vdTitle
	def VoiceData vd_previous
	def VoiceData vd_first
	def listAuthors = null
	def numberInBasename
	def listitems
	def loadedlistitems	
	def used = false
	def VoiceData previousLinkVoice	
	def static sentencePunctions = ['.', '?', '!']
	def static Template template
	def static String strH1Template	
	def static Template h1Template	
	def static content_file_name
	def static bSentenceWordMode = true
	def static xmlversion_voicedata
	def iNccItems = 0
	def static int depth = 1
	def static int iFirstPage
	def start = 0.0
	def end = 0.0
	def prev_totaltime = 0.0
	
	/**
	 * unset values
	 */
	def iSmilFile 	= -1
	def iTcp		= -1
	def iDtp 		= -1
	def iText 		= -1

	static String strSmilTemplate
	static String strSeqTemplate
	static String strSeqTemplate2
	static String dc_identifier = null
	static String dc_title  = null
	static String region_id = null
	def static SimpleTemplateEngine engine = new SimpleTemplateEngine()

	def seq = null
	def time_together = 0.0
	def int totalpage = 0
	def double totaltime = 0.0
	def old_totaltime = 0.0
	def par = null
	def mp3_file_name
	def smil_file_name
	
	VoiceData firstVoiceData, lastVoiceData
	
	/**
	 * register on itseasiassa Lipsync2Smil objekti, joka tallettaa
	 * kulloisenkin VoiceDataFile tai kulloisenkin VoiceData objektin
	 */
	def static Lipsync2Daisy register
	def static Lipsync2Daisy lipsync2Smil
	def isDocTitle = false
	def static String strOutputDir
	def static hmXhtmls = [:]
	def static hmRegexXhtmls = [:]
	def bHasPageVds = false // has this file page vm elements

	public VoiceDataFile()
	{
		Object.metaClass.'static'.println = Lipsync2Daisy.new_pritln_method
		VoiceDataFile.metaClass.'static'.println = Lipsync2Daisy.new_pritln_method
	}
	
	/*
	def public void setFile(File f)
	{
		file = f
		if (f)
		{
			def name = f.getName()
			int ind = name.indexOf('.')
			if (ind > -1)
			{
				basename = name.substring(0, ind)
				def match = basename =~ /[0-9]+/
				def numbers = null
				if (match.find())
				{
					numbers = match[0]
					numberInBasename = Integer.valueOf(numbers) 
				}
			}
		}	
		else
		{
			basename = null
			numberInBasename = null
		}
	}
	*/
	
	/**
	 * Methodi asettaa tiedoston base-nimen
	 * 
	 * @param value Asetettava arvo
	 */
	def public void setBasename(String value)
	{
	 	int ind = value.indexOf('.')
		if (ind > -1)
			basename = value.substring(0, ind)
		def match = basename =~ /[0-9]+/
		def numbers = null
		if (match.find())
		{
			numbers = match[0]
			numberInBasename = Integer.valueOf(numbers) 
		}
	}
	
	/**
	 * builder toString -methodi
	 */
	def String toString() {
		return new ToStringBuilder(this).
		  append(this.file).
			append(this.basename).
			append(this.used).
			append(this.listitems).
			toString()
	  }
	 
	/**
	 * builder equals -methodi
	 */
	  def boolean equals(obj) {
		  if (!(obj instanceof VoiceDataFile)) {
			 return false
		  }
		  VoiceDataFile rhs = (VoiceDataFile) obj
			 return new EqualsBuilder().
				 append(this.file, rhs.file).
				 append(this.listitems, rhs.listitems).
				 isEquals()
	   }
	
	  /**
	  * builder hashCode -methodi
	  */
	   def int hashCode() {
		  return new HashCodeBuilder(17, 37).
			   append(this.file).
			   append(this.listitems).
			   toHashCode()
	   }

	   /**
	    * builder compareTo -methodi
	    */
	   def int compareTo(obj) {
		  VoiceDataFile lmp = (VoiceDataFile)obj
			 return new CompareToBuilder().
				 append(lmp.numberInBasename, this.numberInBasename).
				 append(lmp.file, this.file).
				// append(lmp.listitems, this.listitems).
				 toComparison()
	   }
	   
	   /*
	public String toString()
	{
		StringBuffer sb = new StringBuffer()
		for(VoiceData item item in listitems)
			sb.append("  " +item +"\n")
		basename + " used " +used +" " +file + "\n" + sb.toString()
	}
	*/
	
	   /**
	    * kutsutaan smil2voicesmil-ohjelmasta
	    */
	def VoiceData getClipVoiceData(int iCounterALink /* String src_id */)
	throws Exception
	{
		VoiceData ret = null
		if (!listitems)
			return ret
		/*
		for(VoiceData item in listitems)
		{
			if (item.smilid && item.smilid == src_id)
				return item 
		}
		*/

		def sentenceitems = []
		def iStartPunct
		def iCounter = 0
		
		// listaa seuraava käyttämättän lauseellinen item:ja:
		for(VoiceData item in listitems)
		{
			if (!item)
				continue
			if (item.name == 'xmlmark')
			{
				if ( item.text.toString().toLowerCase().contains("</h1>") 
					|| item.text.toString().toLowerCase().contains("</h2>")
					|| item.text.toString().toLowerCase().contains("</h3>") || item.text.toString().toLowerCase().contains("</h4>")
					|| item.text.toString().toLowerCase().contains("<img"))
				{
					/* && !item.text.toString().toLowerCase().contains("<pagenum") */
				// if (this.bDebug)
					//println "kuva tai h-xmlmark"
					
				}
				else
					continue
			}
			
			if (item.name == 'punct' && item.isXmlMarkPunkt)
				continue
			if (item.smilused)
				continue // jos voicedata jo käytetty ohita

			if (item.smilused)
				continue // jos voicedata jo käytetty ohita
			item.smilused = true
			iCounter++
			sentenceitems.add item
			if (item.name == 'punct' && !item.isXmlMarkPunkt 
				&& item.text in sentencePunctions /* && iCounter != 1 
				&& (item.text == "." || item.text == "!"
					|| item.text == "?" ) */) //punct will function as a break, except like - character / punct katkaisee, paitsi ensimmäinen esim tavuviiva
			{
				break;
			}
		}

		if (sentenceitems.size() == 0)
			return null
		int ind = sentenceitems.size() -1
		if (ind == -1)
			throw new Exception("ind == -1")
				
		def startTime 	= (previousLinkVoice == null ? 0 : previousLinkVoice.end)
		def endTime		= ((VoiceData)sentenceitems.get(ind)).end
				
		ret = new VoiceData()
		previousLinkVoice = ret
		ret.start 	= startTime
		ret.end 	= endTime
		
		ret
	}


	/**
	 * Data lataus aikaisemmin asetetusta tiedostosta ja data elementtien luonti datan
	 * mukaan mukaillen. Eli luettavaa dataa muutetaan sen mukaan mitä on tarpeen lopputuloksen
	 * eli smil-tietojen generoinnissa (=lipsyn2smil-ohjelma).
	 * 
	 * @return
	 * @throws Exception
	 */
	def int loadData()
	throws Exception
	{
		int iPages = 0
		
		if (!file)
			return 
		// def pattern = ~ /<(.*?)\\smsStart="(.*)?"\\smsEnd="(.*)?">(.*)?<\/
		def matcher, foudedStr, isXmlMarkPunkt = false		
		loadedlistitems = []
		listitems = []
		def String name, start, end, text
		def xmlMarkEntityStart
		int iLine = 0, ind = -1, iItems = 0
		def VoiceData voicedata, pageVoiceData, prevSentenceVoiceData, prevVoiceData
		def VoiceData prevH1VoiceData
		def first_level1 	= false
		def first_h1 		= false
		def first_doctitle	= false
		def first_docauthor = false
		def first_p = false
		def h1_item = false
		def samekinditems = []
		def level_founded = false
		def isWordPunct = false
		def isPageNumStarted = false
		def isAddedIntoSamekinditems = false
		def isConvertCollectDataBeforeCurrentVD = false
		def isConvertCollectDataAfterCurrentVD = false
		def isImage = false, iH_level = 0
		def poem = false, poem_linegroup = false, poem_line = false
		def poem_data_collected = false, bOhitettuJuuriXml = false
		def isNewDocAuthor = false, bExceptionHandled = false
		def strPageNumLine, strPageNumberLine  
		
		Lipsync2Daisy.currentxmlfilenameprinted = false
		Lipsync2Daisy.currentxmlfilename = file.toString()
		// println file
		// println()
		
		// println "read data rows:"
		
		old_totaltime = totaltime
		def new_total_time = null
		
		/*
		if (Lipsync2Smil.bCalculateMP3FileLengths)
		{
			def mp3fname = file.getName().toLowerCase()
			int ind2 = mp3fname.indexOf(".xml")
			if (ind2 > -1)
			{
				def strNumber = mp3fname.substring(0, ind2)
				def fname = VoiceDataFile.strOutputDir +File.separator + Lipsync2Smil.getSmilMp3FileNameOfLipsyncXmlFName(strNumber)
				new_total_time = MP3.millisecondsOfMp3File(fname)
				if (!new_total_time || new_total_time == -1)
				{
					println "\n-----------------------------------------------"
					println lipsync2Smil.getMessages().getString(Lipsync2Smil.constUI_ui_error_in_calculating_file_length) +": " +fname
					println "-----------------------------------------------\n"
				}
				else
				{
					println "\n............................................."
					println "Mp3 length (new end):     " +new_total_time
					println "\n............................................."
				}
			}
		}
		*/
		
		file.eachLine { line ->
			
			line = line.replaceAll("(?s)\\p{Cntrl}&&[^\n]", "")
			
			bExceptionHandled = false
			iLine++
			if (!line)
				return	
				
			// isXmlMarkPunkt = false
			// isPageNumStarted = false
			
			if (line.startsWith("<?"))
			{
				if (xmlversion_voicedata == null)
					xmlversion_voicedata = new VoiceData(name: null, start: -1,
						end: -1, text: line, iLine: iLine,
						isXmlMarkPunkt: true, mp3_file_name: mp3_file_name,
						smil_file_name: null, iH_level: 0,
						strReadedLine: line, strReadedFileName: file.toString() )
				else
				{
					/*
					voicedata = new VoiceData(name: null, start: -1,
						end: -1, text: line, iLine: iLine,
						isXmlMarkPunkt: true, mp3_file_name: mp3_file_name,
						smil_file_name: null, iH_level: 0,
						strReadedLine: line, strReadedFileName: file.toString() )
					// voicedata.ddddddd
					 * 
					listitems.add voicedata
					 */
				}
				return // ohita xml ohjausrivit
			}
				
			if (line.toLowerCase().startsWith("<lipsync>"))
				return // ohita lipsync ohjausrivit
			if (line.toLowerCase().startsWith("</lipsync>"))
				return // ohita lipsync ohjausrivit
			
			isConvertCollectDataBeforeCurrentVD = false
			isConvertCollectDataAfterCurrentVD = false
			isImage = false
			isAddedIntoSamekinditems = false
			isWordPunct = false
			
			// esi ajat merkkijonosta:
			
			def regex = /<(.*?)\smsStart\s*=\s*"(.*?)"\s+msEnd\s*=\s*"(.*?)">(.*)/
			matcher = line =~ regex
			if (!matcher.find())
			{
 				throw new Exception("" +file +" " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_xmlerror_row) +"($iLine): " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cannotfound) +": " +regex.toString())
				// return
			}
			// parsi data riviltä:
			foudedStr	= matcher[0]
			name 		= matcher[0][1].toString()
			start 		= matcher[0][2].toString()
			end 		= matcher[0][3].toString()
			text 		= matcher[0][4].toString()
			ind = text.lastIndexOf("</" + name +">")
			if (ind > -1)
				text = text.substring(0, ind)
			
			// fixed page-5 and empty page value bug:
			if (name == VoiceData.cnstLipsyncWord && isPageNumStarted &&  text.trim().length() == 0 && prevVoiceData.iPage > -1)
				text = (prevVoiceData?.iPage > -1 ? "" +prevVoiceData.iPage : "")
				
			// käsittele cdata:
			if (text.contains("<![CDATA[")) // name == VoiceData.cnstLipsyncPunct)
			{
				text = text.replaceAll("<!\\[CDATA\\[", "").replaceAll("\\]\\]>", "")
				if (text && (text.startsWith("<?") || 
					(bOhitettuJuuriXml && (text == "<" || text == "!"))
					))
				{
					// println "<? skip...."
					bOhitettuJuuriXml = true 
					return
				}
				else
				   bOhitettuJuuriXml = false
					
				if (text && text.toLowerCase().startsWith("<meta"))
				{
					if (text.contains("dc:Identifier"))
					{
						def search2 = "content="
						int ind2 = text.indexOf(search2)
						if (ind2 > -1)
						{
							def tmp_str = text.substring(ind2 +search2.length())
							if (tmp_str && tmp_str.length() > 2)
							{
								int ind3 = tmp_str.substring(1).indexOf('"')
								if (ind2 > -1)
								{
									this.dc_identifier = tmp_str.substring(1).substring(0, ind3)
									// println dc_identifier
									register.dc_identifier = dc_identifier
								} 
							}
						}
					}
					else
					if (text.contains("dc:Title"))
					{
						def search2 = "content="
						int ind2 = text.indexOf(search2)
						if (ind2 > -1)
						{
							def tmp_str = text.substring(ind2 +search2.length())
							if (tmp_str && tmp_str.length() > 2)
							{
								int ind3 = tmp_str.substring(1).indexOf('"')
								if (ind3 > -1)
								{
									this.dc_title = tmp_str.substring(1).substring(0, ind3)
									// println dc_identifier
									VoiceDataFile.dc_title = this.dc_title 
									register.dc_title = dc_title
								}
							}
						}
					}
				}
	
				if (daisyids != null && name == VoiceData.cnstLipsyncXmlmark)
				{
					def matchEntity = text =~ /<(\/?)(\w+)\s*>/
					if (matchEntity.find())
					{
						def strike 	= matchEntity[0][1].toString()					
						def tmp 	= matchEntity[0][2].toString()
						if (tmp && !strike) // && tmp == xmlMarkEntityStart)
							daisyids.openLipsyncEntity.add tmp
						else
						if (tmp && strike && daisyids.openLipsyncEntity.size() > 0
							&& tmp == daisyids.openLipsyncEntity.get(daisyids.openLipsyncEntity.size()-1)) 
							daisyids.openLipsyncEntity.remove(daisyids.openLipsyncEntity.size()-1)
					}
				}
				// jos kappaleen sisällä on punct joka ei ole lauseen loppu, muuta rivin tyyppi word:ksi:
				if (first_p && name == 'punct' && !(text in sentencePunctions) /* (text == '-' || text == '–') */)
				{
					// println "hip"
					name = VoiceData.cnstLipsyncWord
					isWordPunct = true
				}
				if (name == VoiceData.cnstLipsyncPunct && ((isXmlMarkPunkt && (text == "<" || text == "!")) || (!first_level1 && text == "-") ))	
				{
					// println "test: skip pucnt"	
					return	// skip xml row	
				}
			}
			try {
				
				if (name.equals(VoiceData.cnstLipsyncXmlmark)) // is dtbook xml element
				{
					isXmlMarkPunkt = true	
						
					if (!poem && text.toLowerCase().startsWith("<poem" )) // if poem block is starting / jos runo-lohko alkaa
						poem = true
					else
					if (poem && text.toLowerCase().startsWith("</poem>" )) // if poem block is ending / jos runo-lohko loppuu
						poem = false
					else
					if (poem && text.toLowerCase().startsWith("<linegroup" )) // if poem linegroup block is starting / jos runo-lohkon lingroup alkaa
						poem_linegroup = true
					else
					if (poem && text.toLowerCase().startsWith("</linegroup>" )) // if poem linegroup block is ending / jos runo-linegroup lohko loppuu
						poem_linegroup = false
					else
					if (poem_linegroup && text.toLowerCase().startsWith("<line" )) // jos runo-lohkon lingroup alkaa
						poem_line = true
					else
					if (poem_line && text.toLowerCase().startsWith("</line>" )) // if line block is ending / jos line loppuu
					{
						isConvertCollectDataBeforeCurrentVD = true
						poem_line = false
					}
					else
					if (!isImage && text.toLowerCase().startsWith("<img " )) // if picture / jos kuva
						isImage = true
					else
					if (!isPageNumStarted && (text.toLowerCase().startsWith("<page_num" ) || text.toLowerCase().startsWith("<pagenum" )))  // if page is starting / jos sivunumerointi elementti
					{
						isPageNumStarted = true
						// pageVoiceData = null // koska page-voi tulla ilman seuraavaa sivu-dataa ja
						// silloin otetaan sivunumero page-id:stä
					}
					else
					if (isPageNumStarted /* && text.toLowerCase() == "</pagenum>" */ )  // if page block is engding / jos sivunumerointi elementti loppuu
					{
						isPageNumStarted = false
						// isConvertCollectDataBeforeCurrentVD = true
					}
					else
					/* level1 - tulostus sallittu/ei: */
					if (!first_level1 && text.toLowerCase() == "<level1>"
						|| text.toLowerCase() == "<level2>"
						|| text.toLowerCase() == "<level3>"
						|| text.toLowerCase() == "<level4>"
						|| text.toLowerCase() == "<level5>"
						|| text.toLowerCase() == "<level6>")  // if 1-6 level element / jos 1-6. tason elementti
						first_level1 = true
					else
					if (first_level1 && text.toLowerCase() == "</level1>" 
						|| text.toLowerCase() == "</level2>"
						|| text.toLowerCase() == "</level3>"
						|| text.toLowerCase() == "</level4>"
						|| text.toLowerCase() == "</level5>"
						|| text.toLowerCase() == "</level6>")  // if 1-6 level is ending /jos 1-6. tason elementti loppuu
					{
						first_level1 = false
						// isConvertCollectDataBeforeCurrentVD = true
					}
					else
					/* 	level1 - h1 koodaus: 				
					if (!first_doctitle && first_level1 && !first_h1 && text.toLowerCase() == "<h1>" )
						first_h1 = true					
					else
					if (!first_doctitle && first_level1 && first_h1 && text.toLowerCase() == "</h1>" )
						first_doctitle	= true
					*/					
					if (!first_doctitle && text.toLowerCase() == "<doctitle>" )  // if article or book title / jos artikkelin/kirjan otsikko
					{
						first_doctitle = true
					}
					else
					if (first_doctitle && text.toLowerCase() == "</doctitle>" ) // if title is ending / jos artikkelin/kirjan otsikko loppuu
					{
						first_doctitle	= false
						isConvertCollectDataBeforeCurrentVD = true
					}
					else
					if (!first_docauthor && text.toLowerCase() == "<docauthor>" ) // if doc.writer is starting / jos kirjoittaja node
					{
						first_docauthor = true
						isNewDocAuthor = true
					}
					else
					if (first_docauthor && text.toLowerCase() == "</docauthor>" ) // if writer block is ending / jos kirjoittaja node loppuu
					{
						first_docauthor	= false
						isConvertCollectDataBeforeCurrentVD = true
						isNewDocAuthor = false
					}
					else
					if (!h1_item && (text.toLowerCase() == "<h1>" 
						|| text.toLowerCase() == "<h2>"
						|| text.toLowerCase() == "<h3>"
						|| text.toLowerCase() == "<h4>"
						|| text.toLowerCase() == "<h5>"
						|| text.toLowerCase() == "<h6>") ) // jos otsikko node
					{
						h1_item = true
						def str_h_level = text.substring(2,3)
						iH_level = Integer.valueOf(str_h_level) 
						if (depth < iH_level)
							depth = iH_level
					}
					else
					if (h1_item && (text.toLowerCase() == "</h1>" 
						|| text.toLowerCase() == "</h2>"
						|| text.toLowerCase() == "</h3>"
						|| text.toLowerCase() == "</h4>"
						|| text.toLowerCase() == "</h5>"
						|| text.toLowerCase() == "</h6>") )// if header is ending / jos otsikko nodet loppuu
					{
						h1_item = false
						isConvertCollectDataBeforeCurrentVD = true
						iH_level = 0
					}
					else
					if (!first_p && text.toLowerCase() == "<p>" ) // if p block is starting / jos kappale alkaa
					{
						first_p = true
					}
					else
					if (first_p && text.toLowerCase() == "</p>" ) // if p block is ending / jos kappale loppuu
					{
						first_p	= false
						isConvertCollectDataBeforeCurrentVD = true
					}					
				}
				else
				if (name == VoiceData.cnstLipsyncWord )
				{
					isXmlMarkPunkt = false
					
					if (poem_line)
						poem_data_collected = true
					else
						poem_data_collected = false
				}
					
		
				
				if (!isPageNumStarted && isXmlMarkPunkt) // </pagenum> löytynyt
				{
					if (pageVoiceData) // jos ei null
						if (pageVoiceData == voicedata) // <pagenum löytynyt, muttei perässä tulevaa page-word dataa
					{
						pageVoiceData.name = VoiceData.cnstLipsyncWord // muuta tulostuvaksi
						if (!pageVoiceData.xmlText)
						{
							pageVoiceData.xmlText = pageVoiceData.text
							pageVoiceData.text = "" +pageVoiceData.iPage // aseta tulostuva teksti
						}
						iPages = pageVoiceData.iPage
						pageVoiceData.isXmlMarkPunkt = false // ei ole dtbook-data-node, vaan muutetaan lipsync-dataksi
					} 
				}		
				
		
				if (isConvertCollectDataBeforeCurrentVD)
				{
					int iSize = samekinditems.size()
					if (iSize > 0) // jos kerätty nodeja, käsittele otsikkodata-nodeja
					{						
						def VoiceData item = getNewVoiceData(samekinditems)
						if (item && !item.isAddedIntoList)
						{
							listitems.add item
							item.isAddedIntoList = true
						}
						samekinditems.clear()
					}
				}
				
/*
				if (end && end == "9990000000000000000000")
					println "stop"
*/				
				// luo uusi voiceata (=ovat dtbook-dataa tai lipsync-dataa) 
				voicedata = new VoiceData(name: name.toString(), start: Double.valueOf(start.toString()), 
					end: Double.valueOf(end.toString()), text: text.toString(), iLine: iLine, 
					isXmlMarkPunkt: isXmlMarkPunkt, mp3_file_name: mp3_file_name, 
					smil_file_name: smil_file_name, iH_level: iH_level,
					strReadedLine: line, strReadedFileName: file.toString() )
				if (!firstVoiceData)
				{
					firstVoiceData = voicedata
				}
				
				lastVoiceData = voicedata
				
				if (prevH1VoiceData && !voicedata.isH1) // jos edellinen h1, h2 jne, laita sen loppuajaksi tämän
				// seuraavan loppuaika
				{
					prevH1VoiceData.end = voicedata.start
					prevH1VoiceData = null // aika asetettu, null ok
				}
				
				if (voicedata.name != 'punct' && prevSentenceVoiceData && prevSentenceVoiceData.name == 'punct')
				{
					prevSentenceVoiceData = null
				}
				
				if (isPageNumStarted && isXmlMarkPunkt) // sivunumero-dtbook-dataa
				{
					pageVoiceData = voicedata
					// poimi etukäteen jos lipsync-teksti-sivunumerodataa ei tulisikaan sivunumero,
					// select page number at first, if page number data is missing, 
					// by example id="page10" into 10 page number:
					def search = /<page_*num\s+page=".*?"\s+id="[-a-zA-Z]+-?(\d+)"/
					def matcher2 = line.toString().toLowerCase() =~ search
					def founded = matcher2.find() 
					if (!founded)
					{
						search = /<page_*num\s+id="[-a-zA-Z]+-?(\d+)"\s+page=".*?"/
						matcher2 = line.toString().toLowerCase() =~ search
					}
					if (!founded && !matcher2.find())
					{
						 throw new Exception("" +file +" " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_xmlerror_row) +" ($iLine): " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_cannotfound) +": $search")
						 println("" +file +" " +Lipsync2Daisy.getMessages().getString(Lipsync2Daisy.constUI_ui_xmlerrorrow) + " ($iLine): " +Lipsync2Daisy.getMessages().getString(Lipsync2Daisy.constUI_ui_cannotfound)+ ": $search")
						// return
					}
					def strPageNum 	= matcher2[0][1].toString()
					try {
						// pageVoiceData.strPage = strPageNum						
						pageVoiceData.iPage = Integer.parseInt(strPageNum)						
						if (!iFirstPage)
							iFirstPage = pageVoiceData.iPage
						pageVoiceData.isAllowedPrint = true
						if (!pageVoiceData.xmlText)
						{
							pageVoiceData.xmlText = pageVoiceData.text
							// pageVoiceData.text = "" +pageVoiceData.iPage
						}
						iPages = pageVoiceData.iPage	
						strPageNumLine = line
					} catch(Exception e){
						System.err.println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_warning) +": " + lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_pagenum_error_notnumber) +":" +iLine +"):" +strPageNum 
						Lipsync2Daisy.severe(e)
					}
				}
				else
				if (isPageNumStarted && !isXmlMarkPunkt)
				{ // pagenum is founded and later page number data / sivunumero <pagenum ensin löytynyt ja sen perästä lipsync-sivunumero-data
					try {
						try {
							if (text)
							{
								pageVoiceData.strPage += " " + text
								pageVoiceData.strPage = pageVoiceData.strPage.trim()
								voicedata.xmlText = voicedata.text
								strPageNumberLine = line
								if (voicedata.iPage == -1)
								{
									try { // can also what ever:
										if (voicedata.iPage == -1)
											voicedata.iPage = Integer.parseInt(text)
									} catch(Exception e2){
										if (pageVoiceData.iPage != -1)
										{
											voicedata.iPage = pageVoiceData.iPage
											voicedata.strPage = pageVoiceData.strPage
										}
										
										if (voicedata.iPage == -1 && text.contains('.'))
										{
											// register.setErrorColorOn()
											println "\n====================================================="
											println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_paagenum_error_punct) +": " + e2 +"\n" +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_paagenum_error_punct2) +": "
											println strPageNumLine
											println strPageNumberLine
											println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_row)+": " +iLine +"\n"+lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_readed_line)+ ": " + line +"\n" +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_skip_this_line)+".\n"
											println "====================================================="
											Lipsync2Daisy.severe(e2)
											return
										}
										else
										{
											// register.setErrorColorOn()
											println "\n====================================================="
											println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_paagenum_error_punct) +": " + e2 +"\n" +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_paagenum_error_punct2) +": "
											println strPageNumLine
											println strPageNumberLine
											println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_row)+": " +iLine +"\n" +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_readed_line)+ ": " + line +"\n"
											println "====================================================="
											Lipsync2Daisy.severe(e2)
										}
									}
								}
							}							
						} catch(Exception e){
							bExceptionHandled = true
							if (voicedata.iPage == -1 && text.contains('.'))
							{
								// register.setErrorColorOn()
								println "\n====================================================="
								println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_paagenum_error_punct) +": " + e +"\n" +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_paagenum_error_punct2) +": "
								println strPageNumLine
								println strPageNumberLine
								println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_row)+": "+iLine +"\n" +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_readed_line)+": " + line +"\n" +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_skip_this_line)+".\n"
								println "====================================================="
								Lipsync2Daisy.severe(e)
								return
							}
							else
							{
								// register.setErrorColorOn()
								println "\n====================================================="
								println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_paagenum_error_punct) +": " + e +"\n" +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_paagenum_error_punct2) +": "
								println strPageNumLine
								println strPageNumberLine
								println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_row)+": " +iLine +"\n" +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_readed_line)+": " + line +"\n"
								println "====================================================="
								Lipsync2Daisy.severe(e)								
							}
							strPageNumberLine = line
							// register.setErrorColorOff()														
						}
						
						if (!iFirstPage)
						{
							iFirstPage = pageVoiceData.iPage
						}

						if (pageVoiceData.iPage != -1)
							iPages = pageVoiceData.iPage
						else
						if (iPages != -1)
							pageVoiceData.iPage = iPages
						if (!VoiceData.page_lipsync_time_on_off)
						{ // jos ei käytetä lipsync-sivunumero-datan omaa aikaa vaan edelltävää <pagenum aika-arvoja
							voicedata.start = pageVoiceData.start
							voicedata.end = pageVoiceData.end
						}
						
						if (!VoiceData.page_lipsync_time_on_off)
						{
							if (iPages == -1)
								pageVoiceData.isAllowedPrint = false
							else
								pageVoiceData.isAllowedPrint = true
						}
					} catch(Exception e){
						if (!bExceptionHandled)
						{
							System.err.println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_pagenumber_error_in_row) +":" +iLine +"): " +text 
							println e.getMessage()
							Lipsync2Daisy.severe(e)				
						}
						return
					}
				}
			
				voicedata.isWordPunct = isWordPunct
				voicedata.isImage = isImage
				
				if (/* isXmlMarkPunkt && */ !first_level1 && voicedata.text == '-')
				{
					voicedata.isAllowedPrint = false // ylimääräistä lipsyn - riviä ei tulosteta / dtbook data rivi
				}
					
				if (first_doctitle && !isXmlMarkPunkt)
				{
					voicedata.isDocTitle = true
					vdTitle = voicedata
				}
				else				
				if (first_docauthor && !isXmlMarkPunkt)
				{
					voicedata.isDocauthor = true
					if (listAuthors == null)
					{
						listAuthors = []
					}
					else
					{
						if (listAuthors.size()> 0 && !isNewDocAuthor)
						{
							VoiceData earlierauthor = listAuthors.getAt(listAuthors.size()-1)
							if (earlierauthor)
							{
								earlierauthor.text += " " +voicedata.text
								earlierauthor.end = voicedata.end
								return
							}
						} 
					}
					if (!listAuthors.contains(voicedata))
					{
						listAuthors.add voicedata
					} 
					isNewDocAuthor = false
				}
				else
				if (h1_item && !isXmlMarkPunkt)  // jos h1-data-node
				{					
					voicedata.isH1 = true
					voicedata.iH_level = iH_level
					if (depth < iH_level)
					{
						depth = iH_level
					}
					prevH1VoiceData = voicedata
				}
				else
				if (first_p && !isXmlMarkPunkt) // jos 
				{
					if (bSentenceWordMode)
					{
						if (voicedata.name != 'punct' && prevVoiceData &&
							 prevVoiceData.text in sentencePunctions) // jos dtbook-data-node ja lause-tulostus smil:hin
						{
							int iSize = samekinditems.size()
							if (iSize > 0)
							{
								def VoiceData item = getNewVoiceData(samekinditems)
								if (item && !item.isAddedIntoList)
								{
									listitems.add item
									item.isAddedIntoList = true
								}
								samekinditems.clear()
							}
							// listitems.add voicedata
						}
						/*
						if (!isXmlMarkPunkt &&  
							voicedata.name == 'punct' && voicedata.text in sentencePunctions )
						{
							if (prevVoiceData && prevVoiceData.name == 'punct')
								println "previoius: " +prevVoiceData 
							samekinditems.add voicedata							 
							isAddedIntoSamekinditems = true
							def isLastPunct = true
							def item = getNewVoiceData(samekinditems, isLastPunct)
							if (item)
							{
								listitems.add item
							} 
							samekinditems.clear()
						}
						else
						{
						*/
						else
						{
							if (voicedata.name == 'punct' && isXmlMarkPunkt)
								isXmlMarkPunkt = false
							if (voicedata.name == 'punct')
							{
							    prevSentenceVoiceData = voicedata
							}
							// samekinditems.add voicedata
							// isAddedIntoSamekinditems = true
					    }					    
						if (voicedata.name == 'punct')
						{
							prevSentenceVoiceData = voicedata
						}
					}
					voicedata.isP = true
									
				}
				
				loadedlistitems.add voicedata
				
				if ( /* isXmlMarkPunkt || */ !bSentenceWordMode
					|| (!isXmlMarkPunkt &&
					voicedata.name != 'punct' && prevVoiceData &&
					 prevVoiceData.text in sentencePunctions)) // jos dtbook-data-node ja lause-tulostus smil:hin
				{					
					int iSize = samekinditems.size()
					if (iSize > 0)
					{
						def VoiceData item = getNewVoiceData(samekinditems)
						if (item && !item.isAddedIntoList)
						{
							listitems.add item
							item.isAddedIntoList = true
						}
						samekinditems.clear()
					}
					// listitems.add voicedata
				}
				else
				{
					/*
					if (!isAddedIntoSamekinditems)
					{
						if (!voicedata.isAddedIntoList)
						{
							listitems.add voicedata
							voicedata.isAddedIntoList = true
						}
					}
					*/
				}
				iItems++
				if (!voicedata.isAddedIntoList )
				{
					if ((voicedata.text == '-' && voicedata.isAllowedPrint) || voicedata.text != '-' )
					{
						if (isXmlMarkPunkt)
						{
							int iSize = samekinditems.size()
							if (iSize > 0)
							{
								def VoiceData item = getNewVoiceData(samekinditems)
								if (item && !item.isAddedIntoList)
								{
									listitems.add item
									item.isAddedIntoList = true
								}
								samekinditems.clear()
							}
							listitems.add voicedata
						}
						else
						{
							samekinditems.add voicedata
						}
					}
					// voicedata.isAddedIntoList = true
				}
				prevVoiceData = voicedata				
			} catch(Exception e){
				if (!bExceptionHandled)
				{
					println e.getMessage()
					Lipsync2Daisy.severe(e)
				}
			}						
		}
		
		VoiceData vdPrev, vf2Prev, vdPrevWord
		def listUpdated = []
		for(VoiceData vd in listitems)
		{
			// if (/* vd.name == VoiceData.cnstLipsyncWord && */ vd.iPage > totalpage)
				// totalpage = vd.iPage
			// if (vd.name == VoiceData.cnstLipsyncWord && vd.end && vd.end > 0 && vd.end > totaltime)
				// totaltime = vd.end
			if (vd.iPage > -1 && vd.strPage == "")
				vd.strPage = "" +vd.iPage
				
			if (vd.end > totaltime)
				totaltime = vd.end
			if (vd.iPage > totalpage)
				totalpage = vd.iPage
			if (vdPrevWord)
			{
				if (vd && vd.name == VoiceData.cnstLipsyncXmlmark)
				{
					vd.start = vdPrevWord.end
					vd.end = vdPrevWord.end
				}
				else
				{
					vd.start = vdPrevWord.end					
				}
				vd.isStartTimeEndTimeSet = true
				
			}
			if (vd && vd.name != VoiceData.cnstLipsyncXmlmark)
				vdPrevWord = vd
			if (vd.iPage > 0)
				bHasPageVds = true
			if (vd.iPage > 0)
				vd.correctStartAndEndValuesAfterPipelineValidation()
			vf2Prev = vdPrev
			vdPrev = vd
			listUpdated.add vd
		}
		
		if (vdPrev && vdPrev.end /* && vdPrev.end == totaltime */ && vdPrev.next && vdPrev.next.start)
			totaltime = vdPrev.next.start
		listitems = listUpdated
		
		def newlist, prev, new_listVDFs
		
		// vd:n prev ja next kuntoon:
		newlist = []
		for(VoiceData vd in listitems)
		{
			vd.previous = prev
			if (prev)
			{
				prev.next = vd
				 newlist.add prev
			}
			prev = vd
		}
		prev.next = null
		newlist.add prev
		listitems = newlist
		
		listUpdated = []
		for(VoiceData vd in listitems)
		{	
			if (vd.iPage > 0) // korjataan iPage alku ja loppu arvot
			{
				if (vd.end == vd.start && vd.next && vd.next.iPage > 0
					&& vd.end < vd.next.end)
					vd.end = vd.next.end
			}

			/*
			if (bFirstVdItem && vd.name == VoiceData.cnstLipsyncWord)
			{
				vd.start = 0.0
				bFirstVdItem = false
			}
			*/
			
			if (vd.next != null && vd.next.start > 0.0 /* && vd.name == VoiceData.cnstLipsyncWord */)
			{
				def nnext = vd.next
				def next_value = vd.end
				while (nnext && (!nnext.start || nnext.start == vd.end))
				{
					next_value = nnext.start
					nnext = nnext.next
				}
				vd.end = next_value
			}
			listUpdated.add vd
		}
		
		if (this.vdTitle && firstVoiceData)
			vdTitle.mp3_file_name = firstVoiceData.mp3_file_name
				
		listitems = listUpdated
		
		if (Lipsync2Daisy.doctitle_on_off /* && !dc_title */ && this.vdTitle && this.vdTitle.text)
			dc_title = getTitle(vdTitle)

		println  lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_readed_autio_elements)+ ": " + iItems +"\n"
		return iPages
	}
	
	def private String getTitle(VoiceData vdtitle)
	{
		if (!vdtitle)
		{
			return ""
		}
				
		def strTitle = vdtitle.text
		
		def StringBuffer sb = new StringBuffer()
		for(VoiceData vd in this.listitems)
		{
			sb << vd.text
		}
		def xmltext = sb.toString()
		def matcher = xmltext =~ /(?s)<doctitle>(.*?)<\/doctitle>/
		def String strDocTitle = null
		if (matcher.find())
		{
			strDocTitle = matcher.group(1).toString()
			if (strDocTitle)
				strDocTitle = strDocTitle.toString().replaceAll("[\n\r]", "").replaceAll("</*.*?>", " ")
		    return strDocTitle.toString().trim()
		}
		else
		{ 
		    def regex = '(?s)<meta\\s+name=\\"dc:Title\\"\\s+content=\\"(.*?)\\"\\s*/>'
			matcher = xmltext =~ /$regex/
			if (matcher.find())
			{
				strDocTitle = matcher.group(1)
				if (strDocTitle)
					strDocTitle = strDocTitle.replaceAll("[\n\r]", "").replaceAll("</*.*?>", " ")
				return strDocTitle.toString().trim()
			}
		}
	}
	
	/**
	 * Luo uusi voicedata objekti sen mukaan mitä on luettu dataa eli samanlaista dataa luettu (datarivejä)
	 * 
	 * @param samekinditems
	 * @return
	 */
	def private VoiceData getNewVoiceData(List samekinditems, isLastPunct = false)
	{
		VoiceData ret
		VoiceData tmp_item
		if (bSentenceWordMode && samekinditems.size() > 0)
		{
			def start_time, end_time, str_data = ""
			int iSameKindCnt = 0, iSize = samekinditems.size()
			if (iSize == 1)
			{
				def VoiceData ret2 = (VoiceData)samekinditems.get(0) 
				return ret2
			}
			else
			{
				tmp_item = (VoiceData)samekinditems.get(0)
				for(VoiceData item in samekinditems)
				{
					iSameKindCnt++
					if (iSameKindCnt == 1)
						start_time = item.start
					if (iSameKindCnt == iSize)
						end_time = item.end
					str_data += ((str_data.length() == 0 
						|| item.name == 'punct'
						|| item.isWordPunct) ? item.text : " " +item.text)
				}
				
				// str_data = str_data.trim() +" "
				// str_data = str_data.replaceAll(" ,", ",")
				ret = new VoiceData(start: start_time, end: end_time, text: str_data, 
					smil_file_name: tmp_item.smil_file_name,
					strReadedLine: tmp_item.strReadedLine, strReadedFileName: tmp_item.strReadedFileName,
					iPage: tmp_item.iPage )
				ret.mp3_file_name = tmp_item.mp3_file_name
				ret.iH_level = tmp_item.iH_level
				ret.isXmlMarkPunkt = tmp_item.isXmlMarkPunkt
				ret.iLine = tmp_item.iLine
				ret.name = tmp_item.name
				ret.isDocTitle = tmp_item.isDocTitle
				ret.isDocauthor = tmp_item.isDocauthor
				ret.isH1 = tmp_item.isH1
			}
		}
		ret
	}
	
	/**
	 * ei käytäössä
	 * 
	 * @return
	 */
	def String convert2SmilDtbook()
	{
		StringBuffer sb = new StringBuffer ()
		def str 
		
		for(VoiceData item in listitems)
		{
			str = item.text
			if (item.name == VoiceData.cnstLipsyncXmlmark )
				sb.append "\n" + str /*	+"\n" */
			else
			if (item.name == VoiceData.cnstLipsyncWord && (str.contains("</p>") || str.contains("</P>")) )
				sb.append " " + str	+"\n"	
			else
			if (item.name == VoiceData.cnstLipsyncPunct)
				sb.append str
			else
				sb.append " " + str
		}
		sb.toString()
	}
	
	/**
	 * Palauttaa Content.html:ää varten tätä .xml tiedostoa vastaavan <meta...> content'in vastaavista
	 * voicedatoista.
	 * 
	 * @return Ks yllä.
	 */
	def String convertMetaSmilXhtml()
	{
		StringBuffer sb = new StringBuffer ()
		def str
		
		// dtbook elementtejä vastaavat xhtml-content-datat:
		
		int iCnt = 0
		def bStartLink = true, bUnAddedEndLink = false
		def smillinkstart_item, trimmed_str
		
		for(VoiceData item in listitems)
		{
			if (!item.isAllowedPrint) // jos tieodoston luvussa ei sallittu tulostus, ohita
				continue
				
			str = item.text.toString()
			trimmed_str = str.trim()
			if (!(trimmed_str.startsWith("<meta") || trimmed_str.startsWith("<META")))
				continue// jos ei <meta, ohita

			sb.append "\t\t\t" +str +"\n"
		}

		sb.toString()

	}
	
	def private void initialize_hmXhtmlsHashVariable()
	{
		hmXhtmls.clear()
		hmRegexXhtmls.clear()
		def hashMapFile = new File(register.strSmilTemplateDir2 +File.separator +"dtbookelements.cfg")
		if (!hashMapFile.exists())
			throw new FileNotFoundException("Missing cfg File not found:" +hashMapFile)
		def hashMapText = hashMapFile.getText()
		if (!hashMapText)
			throw new Exception("Missing cfg data in file:" +hashMapFile)
		hashMapText = hashMapText.replaceAll("(?s)(#.*?\n)","")
		if (!hashMapText)
			throw new Exception("Missing cfg data in file after removing comment lines:" +hashMapFile)
		def hashMaplines = hashMapText.split("\n"), hashMapOldValue, hashMapNewValue, arrhashMapline
		def bRegexLine = false
		 
		for(hashvariableline in hashMaplines)
		{
			arrhashMapline = hashvariableline.split("==")
			if (!arrhashMapline)
				continue
			hashMapOldValue = arrhashMapline[0]
			if (!hashMapOldValue)
				continue
			hashMapOldValue = hashMapOldValue.toString().replaceAll("(?s)\\p{Cntrl}", "").trim() // clean value
			if (!hashMapOldValue)
				continue
						
			bRegexLine = false
			if (hashMapOldValue.startsWith("regex:"))
			{
				hashMapOldValue = hashMapOldValue.replace("regex:", "")
				bRegexLine = true
			}	
			
			if (arrhashMapline.size() == 1)
				hashMapNewValue = ""
			else
			{
				hashMapNewValue = arrhashMapline[1]?.toString().replaceAll("(?s)\\p{Cntrl}", "").trim() // clean value
				int ind = 2
				while (!hashMapNewValue && ind < arrhashMapline.size()) 
				{
					hashMapNewValue = arrhashMapline[ind++]
				}
				hashMapNewValue = hashMapNewValue?.toString().replaceAll("(?s)\\p{Cntrl}", "").trim() // clean value
			}
						
			if (bRegexLine)
			{
				if (!(hmRegexXhtmls[hashMapOldValue]))
					hmRegexXhtmls.put hashMapOldValue, hashMapNewValue
			}
			else
			{
				if (!(hmXhtmls[hashMapOldValue]))
					hmXhtmls.put hashMapOldValue, hashMapNewValue
			}
		}		
	}
	
	/**
	 * Palauttaa Content.html:ää varten tätä .xml tiedostoa vastaavan content'in vastaavista
	 * voicedatoista.
	 * 
	 * @return Ks yllä.
	 */
	def String convert2SmilXhtml()
	{
		StringBuffer sb = new StringBuffer ()
		def str
		
		// dtbook elementtejä vastaavat xhtml-content-datat:
		
		// if (hmXhtmls.size() == 0)  // jos ei alustettu
		// {			
		  /*
			hmXhtmls.put("<book>", "")
			hmXhtmls.put("</book>", "")
			hmXhtmls.put("<dtbook>", "")
			hmXhtmls.put("</dtbook>", "")
			hmXhtmls.put("<frontmatter>", "")
			hmXhtmls.put("</frontmatter>", "")
			hmXhtmls.put("<doctitle>", "")
			hmXhtmls.put("</doctitle>", "")
			hmXhtmls.put("<docauthor>", "")
			hmXhtmls.put("</docauthor>", "")
			hmXhtmls.put("<bodymatter>", "")
			hmXhtmls.put("</bodymatter>", "")
			hmXhtmls.put("<level1>", "<div class=\"level1\">")
			hmXhtmls.put("</level1>", "</div>")
			hmXhtmls.put("<level2>", "<div class=\"level2\">")
			hmXhtmls.put("</level2>", "</div>")
			hmXhtmls.put("<level3>", "<div class=\"level3\">")
			hmXhtmls.put("</level3>", "</div>")
			hmXhtmls.put("<level4>", "<div class=\"level4\">")
			hmXhtmls.put("</level4>", "</div>")
			hmXhtmls.put("<level5>", "<div class=\"level5\">")
			hmXhtmls.put("</level5>", "</div>")
			hmXhtmls.put("<level6>", "<div class=\"level6\">")
			hmXhtmls.put("</level6>", "</div>")
			// hmXhtmls.put("</book>", "</body>")
			hmXhtmls.put("</book>", "")
			//hmXhtmls.put("<book>", "<body>")
			hmXhtmls.put("<book>", "")
			hmXhtmls.put("<h1>", "")			
			hmXhtmls.put("</h1>", "")			
			hmXhtmls.put("<h2>", "")			
			hmXhtmls.put("</h2>", "")			
			hmXhtmls.put("<h3>", "")			
			hmXhtmls.put("</h3>", "")
			hmXhtmls.put("<h4>", "")
			hmXhtmls.put("</h4>", "")
			hmXhtmls.put("<h5>", "")
			hmXhtmls.put("</h5>", "")
			hmXhtmls.put("<h6>", "")
			hmXhtmls.put("</h6>", "")
			hmXhtmls.put("</body>", "")
			hmXhtmls.put("<head>", "")
			hmXhtmls.put("</head>", "")
			hmXhtmls.put("</poem>", "")
			hmXhtmls.put("<poem>", "")
			hmXhtmls.put("<line>", "")
			hmXhtmls.put("</line>", "")
			hmXhtmls.put("<linegroup>", "")
			hmXhtmls.put("</linegroup>", "")
			hmXhtmls.put("<imggroup>", "")
			hmXhtmls.put("</imggroup>", "")
			
			hmXhtmls.put("<list>", "<ul>")
			hmXhtmls.put("</list>", "</ul>")
			*/
			// initialize_hmXhtmlsHashVariable()
		// }
		
		initialize_hmXhtmlsHashVariable()
		
		int iCnt = 0
		def bStartLink = true, bUnAddedEndLink = false
		def smillinkstart_item, trimmed_str
		
		for(VoiceData item in listitems)
		{
			if (!item.isAllowedPrint) // jos tieodoston luvussa ei sallittu tulostus, ohita
				continue
				
			str = item.text.toString()
			trimmed_str = str.trim() 
			if (trimmed_str.startsWith("<page") || trimmed_str.startsWith("<PAGE"))
			{
				if (item.next && !item.next.text.toString().startsWith("</page")) // if iPage text value is empty and next item			
				{ // do not contains "<page" value. If not, then continue
					continue // jos kappaleen alku, ohita
				}
			}
			if (trimmed_str.startsWith("</page") || trimmed_str.startsWith("</PAGE"))
				continue// jos kappaleen loppu, ohita
			if (trimmed_str.startsWith("<meta") || trimmed_str.startsWith("<META"))
				continue// jos <meta, ohita				

			// bSmilWordMode
			if (bUnAddedEndLink)
			{
				sb.append getSmilLinkEnd(smillinkstart_item)
				bUnAddedEndLink = false
			}
			if (str == '<p>' || str == '<P>')
			{
				iCnt = 0
				bStartLink = true
			}
			if (item.name == VoiceData.cnstLipsyncXmlmark )
			{
				if (str == '<p>' || str == '<P>') // jos kappaleen alku, tuota vastaava content-mjono
				{
					// str = str.replace(str, "<p id=\"dtd" +register.getNextGlobalRegisterCounter() +"\">\n")					
					sb.append "\n" + getXHtmlCorrespond(str) +"\n"
				}
				else
				if (item.isImage) // jos kuva, tuota vastaava content-mjono
				{
					item.generateIdValues() // generoi id-arvot jne
					sb.append "\n" + str.replaceAll("<img\\s+id=\"(.*?)\"", "<img id=\"" +item.content_id +"\"")
					// sb.append "\n" + str.toString().replace('<img id="(.*?)"', '<img id="' +item.content_id +'" ') // +"\n"
				}
				else // muut, tuota vastaava content-mjono
					sb.append "\n" + getXHtmlCorrespond(str) // +"\n"
			}
			else
			if (item.name == VoiceData.cnstLipsyncWord && (str.contains("</p>") || str.contains("</P>")) )
			{ // jos kappaleen loppu, tuota sisennystä +rivinvaihto
				// if (bStartLink)
					// sb.append getSmilLink(item)
				iCnt++
				if (iCnt > 1)
					sb.append " "
				sb.append str +"\n"
			}
			else
			if (item.name == 'punct' ) // jos pistemerkkejä, tulosta
				sb.append str
			else
			{ // jos linkkitieto, lisää linkkidatat
				bStartLink = true
				if (bStartLink)
				{
					sb.append getSmilLinkStart(item)
					smillinkstart_item = item
					bStartLink = false
					bUnAddedEndLink = true // onko aloitettu ilman päättävää xml-nodea
				}
				iCnt++
				if (iCnt > 1)
					sb.append " "
				if (item.iPage == -1 || (item.iPage > -1 && !item.text.toString().startsWith("<page")))
					sb.append str
			}			
		}
		if (bUnAddedEndLink)// onko aloitettu muttei päätetty vastaavalla xml-nodella
			sb.append getSmilLinkEnd(smillinkstart_item) // niin lisää puuttuva loppu-xml-node-data

		sb.toString().replaceAll("<img\\s+(.*?)>[\n\\r\\s\\t]*</img>", "<img \$1/>\n")
	}
	
	/**
	 * Palauttaa alkavaa voicedataa vastaavat xhtml-mjonon
	 * 
	 * @param item
	 * @return
	 * @throws Exception 
	 */
	def private String getSmilLinkStart(VoiceData item)
	throws Exception
	{
		if (!item)
			throw new NullPointerException("item is null!")
		if (item.isDocTitle)
			return """		<div class="title" id="${item.content_id}">
					<a href="${item.smil_file_name}#${item.text_id}">"""

		if (item.isDocauthor)			
			return """		<div class="docauthor" id="${item.content_id}">
					<a href="${item.smil_file_name}#${item.text_id}">"""
		if (item.isH1)
		return """		<h${item.iH_level} id="${item.content_id}">
				<a href="${item.smil_file_name}#${item.text_id}">"""

		if (item.iPage != -1)
		{
			if (item.text && item.text?.toString().startsWith("<page"))
				return """		<span class="page-normal" id="page${item.iPage}">
		<a href="${item.smil_file_name}#${item.text_id}">${item.iPage}"""
				
			return """		<span class="page-normal" id="page${item.iPage}">
		<a href="${item.smil_file_name}#${item.text_id}">"""
		}
		
		return """		<span class="sentence" id="${item.content_id}">
		<a href="${item.smil_file_name}#${item.text_id}">"""
	}

	
	/**
	 * Palauttaa loppu voicedataa vastaavat xhtml-mjonon
	 * 
	 * @param item
	 * @return
	 * @throws Exception
	 */
	def private String getSmilLinkEnd(VoiceData item)
	throws Exception
	{
		if (!item)
			throw new NullPointerException("item is null!")
		if (item.isDocTitle)
			return """</a>\n		</div>\n"""

		if (item.isDocauthor)
			return """</a>\n		</div>\n"""
		if (item.isH1)
			return """</a>\n		</h${item.iH_level}>\n"""
					
		"""</a>\n		</span>\n"""
	}

	/**
	 * Palauttaa dtbook-xml-nodea vastaavan mjonon tai mjono itsensä sellaisenaan 
	 * jos sitä ei tarvitse muuttaa
	 *  
	 * @param xmlmark
	 * @return
	 */
	def private String getXHtmlCorrespond(String xmlmark)
	{
		def ret = xmlmark
		/*
		if (ret.contains("list"))
			println "lsit"
		*/
		// println "xmlmark='" + xmlmark +"'"
		def value = hmXhtmls.get(xmlmark)
		
		if (ret.startsWith("<dtbook") || ret.startsWith("<DTBOOK"))
			return ""
		if (value != null)
			return value
		else
		{ // search with regex
			def match
			for (regexvalue in hmRegexXhtmls.keySet())
			{
				if (!regexvalue)
					continue
				match = xmlmark =~ /$regexvalue/
				if (match.find())
				{
					return hmRegexXhtmls.get(regexvalue)
				}
			}
		}
		ret
	}

	/**
	 * Palauttaa mjonon joka vastaa tämän xml-tiedoston ncc:tä
	 * 
	 * @return
	 */
	def String convert2Ncc()
	{
		StringBuffer sbH1 = new StringBuffer() 
		int iCnt = 0
		def doctitle_start_time, doctitle_end_time
		def VoiceData h1_item
		def h1_item_title = ""
		def h1_id, contentid, founded = false
		def content_text, binding, strH1 = "" 
		
		if (!h1Template) // jos templatea ei asetettu, hae sellainen
			h1Template = engine.createTemplate(strH1Template)
		
		// etsitään h1 otsakkeet tästä filestä:
			 
		for(VoiceData item in listitems)
		{
			// TODO: ISALLOWEDPRINT !!??
			if (item.isXmlMarkPunkt)
				continue
				
			if (!(item.isH1 || item.iH_level > 0) && !item.isDocTitle)
			{
				if (item.iPage != -1)
				{
					h1_id = register.getNextNccId()
					item.generateIdValues()
					def h_level = (item.iH_level < 1 ? 1 :item.iH_level)
					binding = ["h_level" : h_level, "h1_id":  'd' +h1_id, "css_class": "  class=\"page-normal\" ",
						"content_file_name" : smil_file_name,
						 "contentid": item.text_id,
						 "content_text": (item.next && item.next.text?.toString().startsWith("</pagenum>") ? item.iPage : item.text?.toString().replaceAll("\\.", "").trim()) ]
					strH1 = h1Template.make(binding).toString()
					// <h1 ... </h1> <span ... </span> :ksi:
					strH1 = strH1.replace("<h1 ", "<span ").replace("</h1>", "</span>").replace("<H1 ", "<span ").replace("</H1>", "</span>")
					sbH1 << strH1 +"\n"
					iNccItems++
				}
				else
				if (founded)
				{
					h1_id = register.getNextNccId()
					h1_item.generateIdValues()
					def h_level = (h1_item.iH_level < 1 ? 1 :h1_item.iH_level)
					binding = ["h_level" : h_level, "h1_id":  'd' +h1_id, "css_class": (h1_item.isDocTitle ? " class=\"title\" " : ""),
						"content_file_name" : smil_file_name,
						 "contentid": h1_item.text_id,
						 "content_text": h1_item_title ]
					strH1 = h1Template.make(binding).toString()
					sbH1 << strH1 +"\n"
					h1_item = null
					h1_item_title = ""
					founded = false
					iNccItems++
				}
				continue
			}
			iCnt++
			founded = true
			// println iCnt
			h1_item_title += (item.text +" ")
			if (!h1_item)
				h1_item = item
		}
				
		if (founded)
		{
			h1_id = register.getNextNccId()
			h1_item.generateIdValues()
			def h_level = (h1_item.iH_level < 1 ? 1 :h1_item.iH_level)
			binding = ["h_level" : h_level, "h1_id":  'd' +h1_id, "css_class": (h1_item.isDocTitle ? " class=\"title\" " : ""),
				"content_file_name" : smil_file_name,
				 "contentid": h1_item.text_id,
				 "content_text": h1_item_title.trim() ]
			strH1 = h1Template.make(binding).toString()
			sbH1 << strH1 +"\n"
			iNccItems++
		}
		
		sbH1.toString()
	}
	
	/**
	 * Palauttaa mjonon joka vastaa tämän xml-tiedoston smill-tiedostoa
	 * 
	 */
	def String convert2Smil()
	{
		StringBuffer sbSeq = new StringBuffer() 
		int iCnt = 0
		def doctitle_start_time, doctitle_end_time
		def doctitle_item, doctitle_item_title = "", item_title = ""
		VoiceData lastitem
		def isFirstItem = false		
		
		for(VoiceData item in listitems)
		{
			if (!item.isDocTitle && !item.isImage && (item.isXmlMarkPunkt || item.name == VoiceData.cnstLipsyncXmlmark))
			{
				continue
			}
			
			iCnt++
			// println iCnt
			if (item.isDocTitle)
			{
				if (!doctitle_start_time)
				{
					doctitle_start_time = 0 //item.start
					doctitle_end_time = item.end
				}
				else
				{
					if (doctitle_start_time > item.start)
						doctitle_start_time = item.start
					if (doctitle_end_time < item.end)
						doctitle_end_time = item.end
				}
				doctitle_item_title += item.text
				doctitle_item = item
			}
			
			if (doctitle_item)
			{
				if (!time_together)
					doctitle_start_time = 0					
				// if (doctitle_end_time)
					// time_together = doctitle_end_time // - doctitle_start_time 
				def dt = new VoiceData(text: doctitle_item_title, start: doctitle_start_time, 
					end: doctitle_end_time, mp3_file_name: mp3_file_name, smil_file_name: smil_file_name,
					strReadedLine: item.strReadedLine, strReadedFileName: item.strReadedFileName)
				dt.isDocTitle = true
				dt.generateIdValues()
				if (!isFirstItem)
				{
					dt.isFirstItem = true
					isFirstItem = true
				}
				sbSeq << dt.convert2Smil() +"\n"
				// sbSeq << doctitle_item.convert2Smil(doctitle_start_time, doctitle_end_time)
				doctitle_item = null // lisätty jo
			}
					
			// if (isDocTitle && iCnt == 1)
				// item.isDocTitle = true
			/*	
			if (!bSmilWordMode)
			{
				item_title += item.text
			}
			else
			{
			*/
			else
			{
				// time_together = item.end
				item.generateIdValues()
				if (!isFirstItem)
				{
					item.isFirstItem = true
					isFirstItem = true
				}
								
				sbSeq << item.convert2Smil() +"\n"
			// }
			}
			if (doctitle_start_time)
			{
				doctitle_start_time = null
				doctitle_end_time = null
			}
			// time_together += item.duration
			//time_together += item.totaltime()		
			lastitem = item
		}

		def bindingSeg = ["timetogether":  time_together, "seq": sbSeq.toString() ]
		// println "'" + strSeqTemplate2 + "'"
		def seq_template
		// if (!seq_template)
			seq_template = engine.createTemplate(strSeqTemplate2)

		def binding = ["dc_identifier":  dc_identifier, "dc_title": (this.dc_title ? this.dc_title : doctitle_item_title?.toString()), 
			           "region_id" : region_id, "smil":  seq_template.make(bindingSeg).toString()]
		
		// System.gc()
		// if (!template)
			template = engine.createTemplate(strSmilTemplate)
		template.make(binding).toString()
	}
	
	
	def setBaseValuesOfConversion()
	throws Exception
	{
		int iValue, iCnt = 0
		def listitems2 = []
		def bFirts = true
				
		for(VoiceData item in listitems)
		{	
			if (item.iH_level > 0)
			{ 
				if (VoiceDataFile.depth == null || VoiceDataFile.depth < item.iH_level )
					VoiceDataFile.depth = item.iH_level
			}

			if (bFirts)
				vd_first = item		
			bFirts = false
			if (vd_previous)
				vd_previous.next = item
			item.previous = vd_previous
			item.mp3_file_name = mp3_file_name
			item.smil_file_name = smil_file_name
			// tka kommentiksi 3.11.12: listitems2.add item
			if (item.generate_base_value_set)
			{
				vd_previous = item		
				listitems2.add item // tka added 3.11.12:
				continue
			}
			if (item.isXmlMarkPunkt)
			{
				vd_previous = item
				listitems2.add item // tka added 3.11.12:
				continue
			}
			if (item.isDocTitle)
			{
				item.generateIdValues()
				vd_previous = item
				listitems2.add item // tka added 3.11.12:
				continue
			}
			iValue = register.getNextGlobalRegisterCounter()
			if (iValue < 1)
				throw new Exception("getNextGlobalRegisterCounter() " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_getnextglobalregistercounter_wrong_value)+ ": " +iValue)
			item.base_value_of_conversion = iValue
			item.generateIdValues()
			vd_previous = item
			listitems2.add item // tka added 3.11.12:
		}
		
		listitems = listitems2
	}
	
	def boolean setVoiceDataIntoDocTitle()
	{
		def founded = false
		if (vdTitle == null)
		for(VoiceData item in listitems)
		{
			// item.
		}
		if (vdTitle)
		{
			dc_title = vdTitle.text
			if (dc_title)
			founded = true
		}
		if (founded)
			return true
		false
	}
	
	def double time_together()
	{
		if (!firstVoiceData || !lastVoiceData )
			return 0
		// if (!firstVoiceData.start || !lastVoiceData.end )
			// return 0
		// lastVoiceData.end - firstVoiceData.start 
		time_together
	}
	
	def double totalpage()
	{
		totalpage
	}
	
	def String getDaisy3DtbookXmlData(boolean bNcxMarkOn = false)
	throws Exception
	{
		int iValue, iCnt = 0
		StringBuffer sb = new StringBuffer()
		def newUpdatedListVD = []
		VoiceData last
		
		for(VoiceData item in listitems)
		{
			// if (item.isXmlMarkPunkt)
		   // if (item.isDocTitle)
			/*
			if (item.isDaisy3_sentence_end)
				sb << "isDaisy3_sentence_end->"
			else
			if (item.isDaisy3_sentence_begin)
				sb << "isDaisy3_sentence_begin->"
			if (item.text.contains("table"))
				println "table"
			*/
			/*	
			if (item.iPage > 0)
			{
				if (item.name == 'xmlmark' && Lipsync2Smil.dtbook_pagenum_on_off != "off" )
					sb <<  '<pagenum id="page-' +item.iPage +'" page="normal" smilref="' +smil_file_name +"#" +item.par_id +'">' +item.iPage
			}
			else
			*/
				sb << item.getDaisy3DtbookText(bNcxMarkOn) << "\n"
			// item.setPossiblePlayOrderValue()
			last = item
			if (!bNcxMarkOn)
				newUpdatedListVD.add(item)
		}
		
		if (!bNcxMarkOn)
			listitems = newUpdatedListVD
		// palauta string että muuttunut lista items:ja	
		// new Lipsync2Smil.ReturnStrinAndList(value: sb.toString(), list: newUpdatedListVD)
		sb.toString()
	}
	
	def VoiceData getVoiceDataTitle()
	{
		vdTitle
	}
	
	def getVoiceDataAuthors()
	{
		listAuthors
	}

	/**
	 * This calculates duration of the this file. (called esspecially from daisy3 code.)	
	 */
	def void countDuration(boolean firstitem, double p_prev_totaltime)
	{
		totaltime = 0.0
		start = null
		end = null
		
		def calculateduration = 0.0 // tka added 8.2.2015
		if (p_prev_totaltime)
			prev_totaltime = p_prev_totaltime
				
		for(VoiceData item in listitems)
		{
			if (!start)
			{
				if (firstitem)
					start = 0.0
				else
					start = item.start
			}
			if (!end)
				end = item.end
			else
			if (end < item.end)
				end = item.end
				
			calculateduration = calculateduration + item.getDuration() // tka added 8.2.2015
		}
		if (!start)
			start = 0.0
		if (!end)
			end = 0.0
		if (firstitem)
			totaltime = 0.0
		totaltime = end // - prev_totaltime // end - start
		if (totaltime < 0)
			println "Totaltime " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_is_under_zero)+": " +totaltime
		old_totaltime = totaltime
		if (Lipsync2Daisy.bCalculateMP3FileLengths)
		{
			def fname = VoiceDataFile.strOutputDir +File.separator + mp3_file_name
			def new_total_time = MP3.millisecondsOfMp3File(fname)
			if (!new_total_time || new_total_time == -1)
			{
				println "\n-----------------------------------------------"
				println lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_error_in_calculating_file_length) + ": " +fname
				println "-----------------------------------------------\n"
			}
			else
			{
				old_totaltime = end +prev_totaltime
				end = new_total_time - start
				println "\n............................................."
				println "Lipsync old totaltime: " +old_totaltime
				println "Mp3 length (new end):     " +new_total_time
			}
		}
		
		if (!start)
			start = 0.0
		if (!end)
			end = 0.0
		totaltime = end +prev_totaltime // - prev_totaltime // end - start

		/*			
		if (Lipsync2Smil.bCalculateMP3FileLengths)
		{
				println "\n............................................."
				println "Lipsync old totaltime: " +old_totaltime
				println "Mp3 length (new end):     " +new_total_time
			println "Lipsync new totaltime:     " +totaltime
			println "...............................................\n"
		}
		*/

		/*
		if (calculateduration) // tka added 8.2.2015
			totaltime = calculateduration // tka added 8.2.2015
		else
			totaltime = end // tka added 8.2.2015
		*/
		if (totaltime < 0)
			println "Totaltime " +lipsync2Smil.getMessages().getString(Lipsync2Daisy.constUI_ui_is_under_zero)+ ": " +totaltime

	}	
	
	def correctXml()
	{
		def lines = []
		def iLine = 0
		def lastLine = "", prevLastLine = ""
		file.eachLine { line ->
			iLine++
			lines.add line
			prevLastLine = lastLine
			lastLine = line.toString()
		}
		
		def modified = false
		def search = "<![CDATA["
		int ind = prevLastLine.indexOf(search)
		if (ind > -1)
		{
			String strFounded = prevLastLine.substring(ind +search.length())
			search = "]]>"
			int ind2 = strFounded.indexOf(search)
			if (ind2 > -1)
			{
				String strValue = strFounded.substring(0, ind2)				
				if (!strValue || strValue && strValue.length() > 0 && strValue.trim().length() == 0)
				{
					lines.remove(lines.size() -2)
					modified = true
				}
			}
		}		
		if (modified)
		{
			StringBuffer sb = new StringBuffer ()
			for(line in lines)
				sb << line +"\n" 
			file.setText(sb.toString())
			file = new File(file.absolutePath)
			lines.clear()
			iLine = 0
			lastLine = ""	
			file.eachLine { line ->
				iLine++
				lines.add line
				prevLastLine = lastLine
				lastLine = line.toString()
			}
			modified = false
		}
		if (prevLastLine && lines.size() > 0)
		{
			search = "<![CDATA["
			ind = prevLastLine.indexOf(search)
			if (ind > -1)
			{
				String strFounded = prevLastLine.substring(ind +search.length())
				search = "]]>"
				int ind2 = strFounded.indexOf(search)
				if (ind2 > -1)
				{
					String strValue = strFounded.substring(0, ind2)
					if (strValue && strValue.length() > 0 
						&& !(strValue.contains("<") && strValue.contains(">")) )
					{
						def line = lines.remove(lines.size() -2)
						def len = lines.size()
						def String prev 
						ind = len -2
						search = "<![CDATA["
						def ind3, prevInd = -1
						String strValue1, strFounded1, search2 = "]]>"
						while(ind > -1 && (prev = lines.get(ind)?.toString()) != null && prevInd != ind)
						{			
							prevInd = ind
							ind3 = prev.indexOf(search)
							if (ind3 > -1)
							{
								strFounded1 = prev.substring(ind3 +search.length())								
								int ind4 = strFounded1.indexOf(search2)
								if (ind4 > -1)
								{
									strValue1 = strFounded1.substring(0, ind4)
									if (strValue1 && strValue1.length() > 0 && strValue1.startsWith("</"))
										ind--
								}
							}
						}
						lines.add (ind+1, prevLastLine)
						modified = true
					}
				}
			}
		}
		if (modified)
		{
			StringBuffer sb = new StringBuffer ()
			for(line in lines)
				sb << line +"\n"
			file.setText(sb.toString())
			file = new File(file.absolutePath)
		}
	}
	
	def LipsyncXmlRow [] loadXml()
	throws Exception
	{
		int iPages = 0
		
		if (!file)
			return ""
		Lipsync2Daisy.currentxmlfilenameprinted = false
		Lipsync2Daisy.currentxmlfilename = file.toString()
			
		// println file
		// println()
		
		
		// println "read data rows:"
		// StringBuffer sb = new StringBuffer () 

		LipsyncXmlRow xmlRow
		def xmlRows = []
		def iLine = 0
		def matcher, foudedStr, ind, name, text
				
		file.eachLine { line ->
			iLine++
			if (line == null || !line)
			{
				// sb << line + "\n"
				xmlRow = new LipsyncXmlRow()
				xmlRow.xmlrow = line.toString()
				xmlRow.iLine = iLine
				xmlRow.file = file
				xmlRow.lipsyncrow = line
				xmlRows.add xmlRow
				return
			}
				
			if (line.startsWith("<?"))
			{ // smil xml:n oma <? xml rivi !
				// sb << line + "\n"
				return // xml ohjausrivit
			}
				
			if (line.toLowerCase().startsWith("<lipsync>"))
				return // ohita lipsync ohjausrivit
			if (line.toLowerCase().startsWith("</lipsync>"))
				return // ohita lipsync ohjausrivit
		
			
			// esi ajat merkkijonosta:
			
			def regex = /<(.*?)\smsStart="(.*?)"\s+msEnd\s*=\s*"(.*?)">(.*)/
			matcher = line =~ regex
			if (!matcher.find())
			{
				 // throw new Exception("" +file +" Virheelinen xml-rivi ($iLine): Ei löytynyt: " +regex.toString())
				// sb << line + "\n"
				xmlRow = new LipsyncXmlRow()
				xmlRow.xmlrow = line.toString()
				xmlRow.iLine = iLine
				xmlRow.file = file
				xmlRow.lipsyncrow = line
				xmlRows.add xmlRow
				return 
			}
			// parsi data riviltä:
			foudedStr	= matcher[0]
			name 		= matcher[0][1].toString()
			start 		= matcher[0][2].toString()
			end 		= matcher[0][3].toString()
			text 		= matcher[0][4].toString()
			ind = text.lastIndexOf("</" + name +">")
			if (ind > -1)
				text = text.substring(0, ind)
				
			// handle cdata:
			if (text.contains("<![CDATA[")) // name == VoiceData.cnstLipsyncPunct)
			{
				text = text.replaceAll("<!\\[CDATA\\[", "").replaceAll("\\]\\]>", "")
				// sb << text + "\n"
				xmlRow = new LipsyncXmlRow()
				xmlRow.xmlrow = text.toString()
				xmlRow.lipsyncrow = line
				xmlRow.iLine = iLine
				xmlRow.file = file
				xmlRows.add xmlRow
				return 
			}
			// sb << line + "\n"
			xmlRow = new LipsyncXmlRow()
			xmlRow.file = file
			xmlRow.xmlrow = text.toString()
			xmlRow.iLine = iLine
			xmlRow.lipsyncrow = line
			xmlRows.add xmlRow
		}
		// sb.toString()
		xmlRows.toArray()
	}	
	
	def String dtbookxmlrows(boolean withVoiceDataId = false)
	{
		StringBuffer sb = new StringBuffer () 
		for(VoiceData item in listitems)
		{
			sb << item.dtbookxmlrow(withVoiceDataId) +"\n"
		}
		sb.toString()
	}
}
