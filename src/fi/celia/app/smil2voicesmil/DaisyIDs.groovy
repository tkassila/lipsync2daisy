package fi.celia.app.smil2voicesmil

/**
 * This class is used to produce file names and smil reference (link) values 
 * for generated daisy files and for content of those files.
 *  
 * @author tk
 *
 */
class DaisyIDs {

	def iSmilFileCntr = 0
	def iTcpCntr = 0
	def iDtpCntr = 0
	def iTextCntr = 0
	
	def openLipsyncEntity = []
	
	def String toCss()
	{
		null
	}
	
	def String getNextSmilFileName()
	{
		Lipsync2Daisy.smilbasefilename +(++iSmilFileCntr) +".smil"
	}

	def String getCurrentSmilFileName()
	{
		Lipsync2Daisy.smilbasefilename +(iSmilFileCntr) +".smil"
	}

	def String getNextTcp()
	{
		"tcp" +(++iTcpCntr) 
	}

	def String getCurrentTcp()
	{
		"tcp" +(iTcpCntr)
	}
	
	def String getNextDtp()
	{
		"dtp" +(++iDtpCntr)
	}

	def String getCurrentDtp()
	{
		"dtp" +(iDtpCntr)
	}
	
	def String getNextText()
	{
		"text" +(++iTextCntr)
	}

	def String getCurrentText()
	{
		"text" +(iTextCntr)
	}
}
