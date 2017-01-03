package fi.celia.app.smil2voicesmil

/**
 * Thsi class is holding objects for one Lipsync xml data file. The class
 * is used in both older smil2voicesmil and current lipsync java appölications.
 * 
 * Tämä luokka kokoaa yhtä smil tiedostoa vastaavat lipsync-ohjelman 
 * tuottamat .xml data tiedostot. Käytetään smil2voicesmil ja lipsync-
 * ohjelmissa.
 * 
 * @author Tuomas Kassila
 *
 */
class CfgFilePair {
	def File fSmil
	def fXmls = []
	def voicedatafiles = []
}
