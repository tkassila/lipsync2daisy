package fi.celia.app.smil2voicesmil

/**
 * This class is holding read data from a xml row of a lipsync xml file. It contains also information
 * line, parsed and unparsed xmlrow data.
 * 
 * @author tk
 *
 */
class LipsyncXmlRow {
	int iLine = 0
	def File file
	def String xmlrow
	def String lipsyncrow
}
