package fi.celia.app.smil2voicesmil.daisy3

import fi.celia.app.smil2voicesmil.VoiceData

/**
 * This class is a base class for VoiceDataFile class. It is needed when a user is converting
 * a lipsync xml into daisy 3 files.
 * 
 * @author tk
 *
 */
class Daisy3SmilFile {
	// def hmItem = []
	// def hmSmilItems = []
	
	def boolean isSentenceStarting(VoiceData vd)
	{
		if (vd == null || !vd.name || vd.name != VoiceData.cnstLipsyncXmlmark)
			return false
		def len = vd.text.length()
		if (vd.text && len > 1 && vd.text.startsWidth("<")
			&& vd.text.substring(1) in VoiceData.sentenceElements)
			return true
	
		false
	}

	def boolean isSentenceEnding(VoiceData vd)
	{
		if (vd == null || !vd.name || vd.name != VoiceData.cnstLipsyncXmlmark)
			return false
		def len = vd.text.length()
		if (vd.text && len > 2 && vd.text.startsWidth("</") 
			&& vd.text.substring(2) in VoiceData.sentenceElements)
			return true
		false
	}

	def void setDaisy3SentenceBegins()
	{		
		VoiceData vf2Prev, vdPrev
		def listUpdated = []
		/*
		 * cnstLipsyncWord = "word"
	def static final cnstLipsyncXmlmark = "xmlmark"
	def static final cnstLipsyncPunc
		 */
		def bIsUnderWordSentence = false
		
		/*
		for(VoiceData vd in listitems)
		{
			vd.isDaisy3_sentence_begin = false
			vd.isDaisy3_sentence_end   = false
			
			if (vd.name == VoiceData.cnstLipsyncXmlmark)
			{ 
				if (isSentenceStarting(vd))
				{
					vd.isDaisy3_sentence_begin = true
					bIsUnderWordSentence = true
				}
				else			 
				if (isSentenceEnding(vd))
				{
					vd.isDaisy3_sentence_end = true
					bIsUnderWordSentence = false
				}
			}

			if (vd.name == VoiceData.cnstLipsyncWord 
					&& bIsUnderWordSentence)
				vd.afterWordsHasSeveralSentencies = true
	
			vf2Prev = vdPrev
			vdPrev = vd
			listUpdated.add vd
		}
		
		if (listUpdated.size() > 0)
			listitems = listUpdated
			*/
	}
	 
}
