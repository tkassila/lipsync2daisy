package fi.celia.app.smil2voicesmil.daisy3

import groovy.text.SimpleTemplateEngine;
import groovy.text.SimpleTemplateEngine

import fi.celia.app.smil2voicesmil.Lipsync2Daisy
import fi.celia.app.smil2voicesmil.VoiceData

/**
 * A base class for VoiceData class. This class contains daisy 3 attributes. In the 
 * child VoiceData class there is daisy 2 attributes.
 * 
 * Perusluokka VoiceData-luokalle. Tähän kerätään lipsync ohjelman äänidatan
 * daisy3 attribuutit. Lähinnä Daisy2 attribuutit perityssä luokassa VoiceData.
 *  
 * @author Tuomas
 *
 */
class VoiceDataDaisy3 {
	
	def isDaisy3_sentence_begin = false
	def isDaisy3_sentence_begin_on = false
	def isDaisy3_sentence_end = false
	def afterWordsHasSeveralSentencies = false
	def afterWordsHasSeveralSentencies_on = false
	// def static File filedtbooksmilreftemplate
	def sentenceChilds = 0
	def iPlayOrder = 0
	
	def static String dtbooksmilreftemplate
	def static template
	def static SimpleTemplateEngine engine = new SimpleTemplateEngine()	
	
	def static extraSmilRefXmlElements = ["<doctitle>", "<docauthor>"]

	public VoiceDataDaisy3()
	{
		super()
	}
	
	def String getDaisy3DtbookText(boolean bNcxMarkOn)
	{
		def ret = getDaisy3DtbookText()
		if (!bNcxMarkOn)
			return ret

		if (!ret)
			return ret
			
		if (ret.startsWith("</"))
			return ret
			
		def xmlid2 = Lipsync2Daisy.getVDTreeHashValue(this), xmlattribute2 = "xmltest2_id"
		def strxmlid2 = xmlattribute2 +"='" +xmlid2 +"'"
		if (ret.contains(">"))
			ret = ret.replaceAll("(<([^/].*?)/>)", "<\$2 " +strxmlid2 + "/>").replaceAll("(<([^/].*?[^/.])>)", "<\$2 " +strxmlid2 + ">")
		else
		if (ret.contains("/>"))
			ret = ret.replaceAll("/>", " " +strxmlid2 + "/>")
	
		if (!isNCXItem)
			return ret	
			
		def xmlid = content_id, xmlattribute = "xmltest_id"
		def strxmlid = xmlattribute +"='" +xmlid +"'"
		if (isXmlMarkPunkt)
		{
				if (ret.contains(">"))			
					ret = ret.replaceAll("(<([^/].*?)/>)", "<\$2 " +strxmlid + "/>").replaceAll("(<([^/].*?[^/.])>)", "<\$2 " +strxmlid + ">")
				else
				if (et.contains("/>"))
					ret = ret.replaceAll("/>", " " +strxmlid + "/>")
		}
		else
		if (isH1)
		{
			if (ret.contains(">"))
				ret = ret.replaceAll("(<([^/].*?)/>)", "<\$2 " +strxmlid + "/>").replaceAll("(<([^/].*?[^/.])>)", "<\$2 " +strxmlid + ">")
			else
			if (ret.contains("/>"))
				ret = ret.replaceAll("/>", " " +strxmlid + "/>")
		}
		else
			ret = ret +" " +strxmlid
		
	   ret
	}
	
	def String getDaisy3DtbookText()
	{
		def bReturnData = true
				
		def tmp_strXmlText = xmlText 
		if (!tmp_strXmlText || tmp_strXmlText == null)
			tmp_strXmlText == ""
			
		if (isSmillPar /* && name != cnstLipsyncWord */
			/* && strXmlText && !strXmlText.toString().startsWith("</") */)
		{
			// && name == cnstLipsyncXmlmark
			// println "ddd"
			// return text +getSmilRefValue()
		}

		def presubstr =  text?.toString()
		def arrSplit = (presubstr && presubstr.size() > 1 ? presubstr.substring(1).split(" ") : null)
		def elementname = null
		if (arrSplit != null && arrSplit.size() > 0)
			elementname = arrSplit[0].replace(">", " ").replace("<", " ").trim()		
		if (text && text.toLowerCase().startsWith("<table") || text.toLowerCase().startsWith("<list")
			|| elementname in Lipsync2Daisy.getListCustomTestAttributeNames())
		{
			if (!(elementname in Lipsync2Daisy.getListCustomTestAttributeNames()))
			{ // jos ei ole smil:ssÃ¤, niin ei tehdÃ¤ smil-referenssejÃ¤kÃ¤Ã¤n:
				def ret2 = text.toString().replace(">", " ").replace("class='.*?'", "").replace('class=".*?"', "")
				int ind = ret2.indexOf('class="')
				if (ind > -1)
				{
					int ind2 = ret2.indexOf('"', ind +'class="'.length())
					if (ind2 > -1)
						ret2 = ret2.substring(0, ind) + ret2.substring(ind2 +1)
				}
				
				return ret2 +" class=\"$elementname\">"
			}
			
			if (!text.contains("class="))
			{
				return text.toString().replace(">", " ") +" class=\"$elementname\" " +getSmilRefValue(true) +">"
			}
			else
			{				
				if (text.contains("class=\"$elementname\""))
					return text.toString().replace(">", " ") +getSmilRefValue(true) +">"
				def ret = getTextWithOutClassAttribute(text).replace(">", " ") +" class=\"$elementname\" " +getSmilRefValue(true) +">"
				return ret
			} 
		}

		if (Lipsync2Daisy.dtbook_pagenum_on_off == "off" 
			&& (iPage > 0 || previous?.iPage > 0 || text?.toString().toLowerCase().startsWith("</pagenum>") 
				|| text?.toString().toLowerCase().startsWith("<pagenum"))
			)
		{
			bReturnData = false
			return ""
		}
						
		if (name == cnstLipsyncXmlmark && text.toString().startsWith("</"))
		{
			isDaisy3_sentence_begin = false
			afterWordsHasSeveralSentencies = false
		}
		else
		if (name == cnstLipsyncWord && tmp_strXmlText.toString().startsWith("</pagenum"))
		{
			isDaisy3_sentence_begin = false
			afterWordsHasSeveralSentencies = false
		}
		else
		if (name == cnstLipsyncWord && (tmp_strXmlText.toString().startsWith("</h1") 
			|| tmp_strXmlText.toString().startsWith("</h2") 
			|| tmp_strXmlText.toString().startsWith("</h3")
			|| tmp_strXmlText.toString().startsWith("</h4")
			|| tmp_strXmlText.toString().startsWith("</h5")
			|| tmp_strXmlText.toString().startsWith("</h6")	)
		)
		{ 
			isDaisy3_sentence_begin = false
			afterWordsHasSeveralSentencies = false
		}
		
		if (!tmp_strXmlText || tmp_strXmlText == null)
			tmp_strXmlText == ""
		
		if (VoiceData.bPrintWordSentencies)
		{
			if (name == cnstLipsyncWord)
			{
				if (iPage > -1)
				{
					def ret = null
					if (next && next.text.toString().startsWith("</pagenum>")) // if next visual page value is missing from xml data!
						ret = "<pagenum page=\"normal\" " +getSmilRefValue() +">" +iPage +"</pagenum>"
					else
					 	ret = "<pagenum page=\"normal\" " +getSmilRefValue() +">" +text.toString().replaceAll("\\.", "") +"</pagenum>"
					return ret
				}
				
				if (iH_level > 0)
				{
					def ret = "<h" +iH_level +" " +getSmilRefValue() +">" +text.toString() 
					return ret
					// return ""
				}
				
				// if (iPage < 0)
								// {
					// if (text && !text.toString().startsWith("</") && text.toString().startsWith("<"))
					// {
						def ret = "<sent " +getSmilRefValue(true) +" >" + text +"</sent>"
						return ret
					// }
				//}
			
			}
			else
			if (name == VoiceData.cnstLipsyncXmlmark)
			{
				if (iPage > -1)
				{
					return ""					
				} 
				if (text && text.toString().toLowerCase() == "</pagenum>")
					return "" // text
				if (iH_level > 0)
				{
					def ret = "<h" +iH_level +" " +getSmilRefValue(true) +" >" 
					return ret
				}
				return text
			}
			return text
		}
		
		if (!isDaisy3_sentence_begin || (isDaisy3_sentence_begin && afterWordsHasSeveralSentencies))	
		{
			if (iPage > 0)
			{
				// tka 2013040.12: if (iPlayOrder == 0)
					// tka 2013040.12: iPlayOrder = Lipsync2Smil.getPlayOrder()
				if (afterWordsHasSeveralSentencies && name == VoiceData.cnstLipsyncXmlmark)
					return (text && text.trim().size() > 0 ? getSmilRefBlock() : "")
				else
				{
					if (!tmp_strXmlText || tmp_strXmlText == null)
						return "" +iPage +"\n"		
					return tmp_strXmlText + iPage +"\n"
				}
			}
			else
			{
				if (afterWordsHasSeveralSentencies && text && text.trim().size() > 0 
					&& text.toString().toLowerCase() != "<p>")
				{
					def ret = "<sent " +getSmilRefValue() +" >" + text +"</sent>"			
					return ret
				}			
				// tka 2013040.12: if (iPlayOrder == 0 && iH_level > 0 /* || (text && text.toString().toLowerCase().startsWith("<p>")) */)	
					// tka 2013040.12: iPlayOrder = Lipsync2Smil.getPlayOrder()
				if (isSmillPar && name != cnstLipsyncWord 
					&& text && !text.toString().startsWith("</") )
				{
					// && name == cnstLipsyncXmlmark
					def matcher = text =~ /(<.*?[\s\t]+)(.*?\/>)/
					if (matcher.find())
					{
						def match_start = matcher[0][1]
						def match_end = matcher[0][2]
						def ret = match_start +getSmilRefValue() +" " +match_end
						return ret
					}
				}
				if (name == cnstLipsyncXmlmark 
					&& text in extraSmilRefXmlElements)
					return text.replace(">"," ") +(next ? next.getSmilRefValue() : getSmilRefValue()) +">"
					
				return text
			}
		}	
		
		// tka 2013040.12: if (iPage > 0  && iPlayOrder == 0)
			// tka 2013040.12: iPlayOrder = Lipsync2Smil.getPlayOrder()
		/*	
		if (text != "<p>" && !text.toString().startsWith("<h"))
			printf "text != <p>"
		*/
		return (text && text.trim().size() > 0 ? getSmilRefBlock() : "")
	}

	def String getSmilRefBlock()
	{
		boolean bPrintIdValue = !(iPage > 0) 
		if (xmlText == null && text == null)
		{
			println "xmlText == null && text == null!"
			return ""
		} 
		if (xmlText == null || text == null)
		{
			println "xmlText == null || text == null!"
		} 
		int len = (iPage > 0 ? (xmlText ? xmlText.size() : (text ? text.size() : 0)) : text?.size() )
		if (len == 0)
		{
			println "len == 0!"
			return ""
		}
			 
	    if (text == null)
		{
			println "iPage < 1 && text == null!"
			return ""
		}

		try {
			def ret = (iPage > 0 ? xmlText.substring(0, (len-1<0 ? 0 : len-1)) : text.substring(0, (len-1<0 ? 0 : len-1))) +" " +
			getSmilRefValue(bPrintIdValue) +" " + 
			(iPage > 0 ? xmlText.substring((len-1<0 ? 0 : len-1), len) : 
				text.substring((len-1<0 ? 0 : len-1), len)) +
			(iPage > 0 ? iPage : "")
			ret
		} catch(Exception e){
			println e.getMessage()
			Lipsync2Daisy.severe(e)
		}
	}
	
	def getIdValue()
	{
		def idvalue = (((iH_level > 0 && content_id == null) /* || isP */ || text.toString().toLowerCase() == "<p>") ? next.content_id : content_id)
		return idvalue 
	}
	
	def String getSmilRefValue(boolean bPrintIdValue = true)
	{				
		def idvalue = getIdValue() 
		if (iPage > 0)
			idvalue = "page" +iPage
		def smilrefvalue = smil_file_name +"#" + (((iH_level > 0 && content_id == null)/* || isP */ || text.toString().toLowerCase() == "<p>") ? next.par_id : par_id)
		def binding = ["idvalue":  (bPrintIdValue ? idvalue : ""), "smilrefvalue": smilrefvalue ]
		if (!template)
			template = engine.createTemplate(dtbooksmilreftemplate)
		def data = template.make(binding).toString()
		if (!bPrintIdValue && data)
			data = data.replace("id=\"\"", "")
		data
	}
	
	def static String getTextWithOutClassAttribute(String p_text)
	{
		if (!p_text)
			return p_text
		def search = "class=\""
		int ind = p_text.indexOf(search)
		def bSimpleStrikeChar = false
		if (ind == -1)
		{
			search = "class='"
			ind = p_text.indexOf(search)
			bSimpleStrikeChar = true
		}
		if (ind > -1)
		{
			def chSeach = "\""
			if (bSimpleStrikeChar)
				chSeach = "'"
			int ind2 = p_text.indexOf(chSeach, ind +search.length())
			if (ind2 > -1)
			{
				def str1 = p_text.substring(0, ind) +" "
				def str2 = p_text.substring(ind2)
				return str1
			}
		}
		p_text // no change
	}
	
	def void setPossiblePlayOrderValue()
	{
		if (iPlayOrder == 0)
		{
			if (!isXmlMarkPunkt && iH_level > 0)
				iPlayOrder = Lipsync2Daisy.getPlayOrder()
			else
			if (Lipsync2Daisy.dtbook_pagenum_on_off != "off" && (xmlText && xmlText.toString().toLowerCase().startsWith("<pagenum ")
				|| (text && text.toString().toLowerCase().startsWith("<pagenum "))) )
				iPlayOrder = Lipsync2Daisy.getPlayOrder()
			else
			{
				def elementname = text?.toString().split(" ")[0].toString().substring(1)
				if (elementname && elementname.contains(">"))
					elementname = elementname.replaceAll(">","").replaceAll("<","").replace("/","")
				if (elementname && text?.toString() != "</" && elementname in Lipsync2Daisy.listNCXCustomTestElements)
					iPlayOrder = Lipsync2Daisy.getPlayOrder()
			}
		}
	}
}
