package fi.celia.app.smil2voicesmil

/**
 * This application (a main method) class was the first version of this appölcation and it is not used
 * any more. The new application is in class Lipsync2smil (Lipsync2smil.groovy file).
 * 
 * Tämän ohjelman seuraaja on Lipsync2Daisy-ohjelma, joka ei muokkaa olemassa olevia daisy smil-tiedostoja,
 * vaan luo vastaavat tiedostot ainoastaan Lipsync xml data tiedostosta.
 * 
 * Tämä luokka eli ohjelma lukee annetusta asetustiedostosta mukaan 'voice' .xml tiedostoja 
 * ja .smil tiedostoja. Asetustiedoston rivit muodostavat .smil tiedoston nimi ja sitä vastaavasta
 * 'voice' .xml tiedoston nimestä. Ohjelma siis muuttaa aina yhtä .smil tiedostoa sitä vastaavalla 
 * 'voice' .xml tiedoston tekstin ja alku- ja loppuaikojen mukaan. Smil tiedoston merkintöjen sekä
 * xml tiedostojen merkintöjen järjestys ovat samanlaiset. Voice .xml tiedostosta etsitään lauseiden loppuajat
 * ja sitä vastaavasta .smil tiedostosta muutettavat lauseiden alku- ja erityisesti loppuajat,
 * joita muutetaan.   
 * <p> 
 * 29.4.12 Lisätty default.cfg:n luonnissa smiltiedosto- ja xml-tiedostojen viimeisten merkitsevien
 * numeroiden (siis etunollat pois) samuuden mukaan kohdistetaan muuttujien arvot toisiinsa. Kuten
 * speechgen0001.smil=1.xml ... speechgen0010.smil=10.xml ... speechgen0011.smil=
 * Jos viimeinen tai jokin muu rivi jää kohdistamatta, tarkista, että: 1. puuttuuko jommastakummasta
 * tiedosto ajetuista hakemistoista. Tai: 2. Onko seuraava tilanne, että kahdella smil tiedostolla
 * on sama .xml tiedosto syötteenään. Kuten: speechgen0001.smil=1.xml ... speechgen0002.smil=1.xml
 * <p> 
 * 31.4.12 Korjattu xml-tiedostojen datan lukua, smil-arvojen alku- ja loppuaikojen lukua ja muuttamista.  
 * <p> 
 * @author Tuomas Kassila
 *
 */
class Smil2VoiceSmil {
	
	def bDebug = true
	def String strSmil2voicesmil
	def String strMachineSmilDir
	def String strVoiceDataDir
	def String strOutputDir
	def File   fSmil2voicesmil
	def File   fOutputDir
	def File   fMachineSmilDir
	def File   fVoiceDataDir
	def cfgFilePairs = [:]
	
	/**
	 * Ohjelman käynnistys. Komentoparametrit: ks usage() funtio.
	 * @param args
	 */
	public static void main(String [] args) {
		try {
			println "Smil2VoiceSmil v. 0.9 (c) Celia & Tuomas Kassila (2012) "
			Smil2VoiceSmil smil2VoiceSmil = new Smil2VoiceSmil()
			smil2VoiceSmil.luekomentoparametrit(args)
			smil2VoiceSmil.convertSmil2VoiceSmil()
		} catch(Exception e){
		    println e.getMessage()
			e.printStackTrace()
		}
	}

	/**
	 * Ohjelman käynnistysparametrit. 
	 */
	def private void usage()
	{
		System.err.println this.getClass().getName() +" smil2voicesmil.cfg voicexml_hakemisto muutettavasmil_hakemisto tuloshakemisto"
		System.err.println " - muuttaa asetustiedon mukaan voice-hakemistosta sekä xml-hakemistosta kone-smill-tiedostoja .xml tiedostojen"
		System.err.println "   mukaan tulos-hakemistoon."
		System.err.println this.getClass().getName() +" voicexml_hakemisto muutettavasmil_hakemisto"
		System.err.println " - luo oleutsasetustiedon (default.cfg) voice-hakemistoston sekä xml-hakemiston .smil ja .xml tiedostojen mukaisen,"
		System.err.println "   jota on usein kopioitava toiselle tiedostonimellä ja muokattava käsin lopulliseen muotoon."
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
	
	/**
	 * Lue komentoparaemtrit ja cfg tiedosto.
	 * @param args
	 * @return
	 * @throws Exception
	 */
	def private luekomentoparametrit(String [] args)
	throws Exception
	{
		println "Käsitellään komentoparametrit."
		if (args.length == 2) // tehdään listaus oletus .cfg tiedosto
		{
			luoOletusCfgTiedosto(args)
			println "Ohjelma valmis."
			System.exit(0)			
		}
		else
		if (args.length != 4)
		{
			System.err.println "Väärä komentoparametrien lukumäärä!"
			usage()
		}
		
		strSmil2voicesmil 	= args[0]
		fSmil2voicesmil 	= new File(strSmil2voicesmil)
		if (fSmil2voicesmil.isDirectory())
		{
			System.err.println "Ei ole tiedosto (smil2voicesmil.cfg): " + fSmil2voicesmil.absolutePath
			usage()
		}
		
		strVoiceDataDir   	= args[1]
		fVoiceDataDir 		= new File(strVoiceDataDir)
		if (!fVoiceDataDir.isDirectory())
		{
			System.err.println "Ei ole hakemisto (voicexml-hakemisto): " + fVoiceDataDir.absolutePath
			usage()
		}

		strMachineSmilDir 	= args[2]
		fMachineSmilDir 	= new File(strMachineSmilDir)   
		if (!fMachineSmilDir.isDirectory())
		{
			System.err.println "Ei ole hakemisto (smilhakemisto): " + fMachineSmilDir.absolutePath
			usage()
		}
	
		strOutputDir 		= args[3]
		fOutputDir		 	= new File(strOutputDir)   
		if (!fOutputDir.isDirectory())
		{
			System.err.println "Ei ole hakemisto (tulostushakemisto): " + fOutputDir.absolutePath
			usage()
		}			
		
		// lue cfg tiedosto
		def ind = -1, iRivi = 0
		String before, after
		File fSmil, fXml, fExistingXml
		CfgFilePair cfgFilePair
		
		VoiceDataFile vdf
		String basename
		def xmlFiles = [:], existingVoiceDataFiles = [:] 
		
		println "Luetaan asetustiedoston käsiteltävät tiedostot:"		

		fSmil2voicesmil.eachLine { line ->
			iRivi++
			if (!line)
				return
			line = line.trim()
			if (line.startsWith("#"))
				return
			ind = line.indexOf('=')
			if (ind == -1)
			{
				eror("cfg-rivi " + iRivi +": = merkki puuttuu!")
			}
			if (0 == ind)
				eror("cfg-rivi " + iRivi +": = merkki on mutta sen edestä puttuu .smil tiedoston nimi !")
			if(line.size() == ind +1)
				eror("cfg-rivi " + iRivi +": = merkki on mutta sen lopusta puuttuu xml tiedoston nimi !")
				
			before 	= line.substring(0, ind).trim()
			if(!before.toLowerCase().endsWith(".smil"))
				eror("cfg-rivi " + iRivi +": .smil puuttuu tiedoston nimestä !")

			after 	= line.substring(ind+1).trim()
			if(!after.toLowerCase().endsWith(".xml"))
				eror("cfg-rivi " + iRivi +": .xml puuttuu tiedoston nimestä !")
				
			fSmil = new File(fMachineSmilDir.absolutePath +File.separator +before)
			if (!fSmil.exists())	
				eror("cfg-rivi " + iRivi +": "+ fSmil + " tiedostoa ei ole olemassa!")
			fExistingXml = xmlFiles.get(after)
			if (fExistingXml)
				fXml = fExistingXml
			else
			{
				fXml = new File(fVoiceDataDir.absolutePath +File.separator +after)
				if (!fXml.exists())
					eror("cfg-rivi " + iRivi +": "+ fXml + " tiedostoa ei ole olemassa!")
				xmlFiles.put after, fXml
			}	
			cfgFilePair = cfgFilePairs.get(before)
			if (!cfgFilePair)
			{
				cfgFilePair = new CfgFilePair ()
				cfgFilePair.fSmil = fSmil
			}
			cfgFilePair.fXmls.add fXml
			if (fExistingXml)
				vdf = existingVoiceDataFiles.get(after)
			else
			{
				vdf = new VoiceDataFile()
				vdf.file = fXml // cfgFilePair.fXml
				ind = before.lastIndexOf('.')
				if (ind > -1)
					basename = before.substring(0, ind)
				else
					basename = before
				vdf.basename = basename
	
				vdf.loadData()
				existingVoiceDataFiles.put after, vdf
			}
			cfgFilePair.voicedatafiles.add vdf
			cfgFilePairs.put(before, cfgFilePair)
		}
		
		println "Luettu asetustiedoston käsiteltävät tiedostonimet."
	}
	
	/** luodaan default.cfg tiedosto, jossa on listattu parametrina annettujen
	 * 2:n hakmeistojen mukaiset .smil ja .xml tiedostot. Ne pitää kohdistaa
	 * vielä enen kuin kyseinen asetustiedostoa voidaan varsinaisessa ajossa 
	 * käyttöä.
	 */
	def private void luoOletusCfgTiedosto(args)
	throws Exception
	{
		String strSmildir 	= args[1]
		File fSmildir 	= new File(strSmildir)
		if (!fSmildir.isDirectory())
		{
			System.err.println "Ei ole hakmeisto: " + fSmildir.absolutePath
			usage()
		}
		
		def smilFileBaseNames = []
		fSmildir.eachFile { file ->
			if (file.isDirectory())
				return
			if (!file.getName().toLowerCase().endsWith(".smil"))
				return
			smilFileBaseNames.add(file.getName())
		}
		
		String strXmldir 	= args[0]
		File fXmldir 	= new File(strXmldir)
		if (!fXmldir.isDirectory())
		{
			System.err.println "Ei ole hakmeisto: " + fXmldir.absolutePath
			usage()
		}
		
		def xmlFileBaseNames = []
		fXmldir.eachFile { file ->
			if (file.isDirectory())
				return
			if (!file.getName().toLowerCase().endsWith(".xml"))
				return
			xmlFileBaseNames.add(file.getName())
		}
		
		StringBuffer sb = new StringBuffer ()
		int ind
		def baseName = ""
		def addXmlFile = false, bLastXmlNumbersOf = false
		
		// tee def. .cfg ja kohdista tied.nimet:		
		for(smilFileBaseName in smilFileBaseNames)
		{
			ind = smilFileBaseName.toString().lastIndexOf('.')
			if (ind > -1)
			{
				baseName = smilFileBaseName.toString().substring(0, ind)
				def match = baseName =~ /[0-9]+/
				def numbers = null
				bLastXmlNumbersOf = false
				addXmlFile = ((baseName+".xml") in xmlFileBaseNames) || ((baseName+".XML") in xmlFileBaseNames)
				if (!addXmlFile)
				{
					if (match.find())
						numbers = match[0]			
					bLastXmlNumbersOf = numbers && isLastNumbersAreSame(numbers, xmlFileBaseNames)
				}
				if (!bLastXmlNumbersOf && addXmlFile)
					sb.append("\n" +smilFileBaseName +"=" +baseName+".xml")
				else
				if (bLastXmlNumbersOf)
					sb.append("\n" +smilFileBaseName +"=" +getLastXmlNumbersOf(numbers, xmlFileBaseNames))
				else
					sb.append("\n" +smilFileBaseName+"=")
			}
			else
				sb.append("\n" +smilFileBaseName +"=")
		}
		
		sb.append("\n\n#  Kohdista:\n")
		def ind2
		// tutkitaan onko xml file name jo sb:ssä:
		for(xmlFileBaseName in xmlFileBaseNames)
		{
			ind = xmlFileBaseName.toString().lastIndexOf('.')
			if (ind > -1)
			{
				baseName = xmlFileBaseName.toString().substring(0, ind)
				ind2 = sb.toString().indexOf(baseName+".xml")
				if (ind2 == -1)
					ind2 = sb.toString().indexOf(baseName+".XML")
				if (ind2 > -1) // löytyi, ohita
					continue
				sb.append("\n" + xmlFileBaseName)
			}
			else
				sb.append("\n" + xmlFileBaseName)
		}
		
		// tutkitaan onko smil file name jo sb:ssä:
		for(smilFileBaseName in smilFileBaseNames)
		{
			ind = smilFileBaseName.toString().lastIndexOf('.')
			if (ind > -1)
			{
				baseName = smilFileBaseName.toString().substring(0, ind)
				ind2 = sb.toString().indexOf(baseName+".smil")
				if (ind2 == -1)
					ind2 = sb.toString().indexOf(baseName+".SMIL")
				if (ind2 > -1) // löytyi, ohita
					continue
				sb.append("\n" + smilFileBaseName)
			}
			else
				sb.append("\n" + smilFileBaseName)
		}

		File fdefaultCfg = new File("default.cfg")
		if (fdefaultCfg.exists())
			fdefaultCfg.delete()
		fdefaultCfg.append(sb.toString(), "UTF-8")
		
		println "Asetustiedosto: " + fdefaultCfg +" luotu. Ohjelma valmis."
	} 

	def private String getLastXmlNumbersOf(numbers, xmlFileBaseNames)
	{
		def match, xmlnumbers
		
		for( xmlfn in xmlFileBaseNames)
		{
			match = xmlfn =~ /[0-9]+/
			if (match.find())
			{
				xmlnumbers = match[0]
				if (xmlnumbers == numbers)
					return xmlfn
				if (areSame(xmlnumbers, numbers))
					return xmlfn
			}
		}
		"" // ok??
	}	
	
	def private boolean isLastNumbersAreSame(numbers, xmlFileBaseNames)
	{
		def match, xmlnumbers
		
		for( xmlfn in xmlFileBaseNames)
		{
			match = xmlfn =~ /[0-9]+/
			if (match.find())
			{
				xmlnumbers = match[0]
				if (xmlnumbers == numbers || areSame(xmlnumbers, numbers) )
					return true
			}
		}
		false
	}	
	
	def private boolean areSame(String xmlnumbers, String numbers)
	{
		while(xmlnumbers && xmlnumbers.startsWith("0"))
			xmlnumbers = xmlnumbers.substring(1)
		while(numbers && numbers.startsWith("0"))
			numbers = numbers.substring(1)
		if (xmlnumbers == numbers)
			return true
		false
	}
	
	def private convertSmil2VoiceSmil()
	throws Exception
	{
		// luetaan voice xml filet:
		
		def listXmlDataFiles = [:]
		String fname, basename
		int ind
		VoiceDataFile voiceDataFile
		def cfgFilePairsKeys = cfgFilePairs.keySet()
	
		/*
		// fVoiceDataDir.eachFile { file ->
		for(CfgFilePair cfgFilePair in cfgFilePairs)
		{
			if (!fVoiceDataDir)
				continue				
			fname = cfgFilePair.fXml.getName()				
			voiceDataFile = new VoiceDataFile()
			voiceDataFile.file = cfgFilePair.fXml
			ind = fname.lastIndexOf('.')
			if (ind > -1) 
				basename = fname.substring(0, ind)
			else
				basename = fname
			voiceDataFile.basename = basename
			listXmlDataFiles.put basename.toString(), voiceDataFile
		}
		*/
		
		// luetaan smil filet:
		
		def listAllSmilFiles = []
		def listSmilBaseNames = []
		def listChangeVoiseDatas = []
		def founded = false, xmlBaseName = "", ind2 = -1
		CfgFilePair cfgPair
		
		/*
		fMachineSmilDir.eachFile { file ->
			if (file.isDirectory())
				return
			if (file.getName().endsWith(".xml"))
				return
			listAllSmilFiles.add file
			if (!file.getName().endsWith(".smil"))
				return
		
			fname = file.getName()
			ind = fname.lastIndexOf('.')
			if (ind > -1)
				basename = fname.substring(0, ind)
			else
				basename = fname
			
			// etsi cfg file parien joukosta oikea .smil ja lataa se: 
			founded = false
			for(xmlfilename in cfgFilePairs.keySet())
			{
				cfgPair = (CfgFilePair)cfgFilePairs.get(xmlfilename)
				if (!cfgPair)
					continue
				if (cfgPair.fSmil.getName() == fname)
				{					
					founded = true
					break
				}
			}
			if (!founded)
				return
			
			listSmilBaseNames.add basename.toString()
			ind2 = cfgPair.fXml.getName().lastIndexOf('.')
			if (ind2 == -1)
				xmlBaseName = cfgPair.fXml.getName()
			else
				xmlBaseName = cfgPair.fXml.getName().substring(0, ind2)
			voiceDataFile = listXmlDataFiles.get(xmlBaseName ?* basename.toString() *?)
			if (!voiceDataFile)			
				return
			voiceDataFile.loadData()
			cfgPair.voicedatafile = voiceDataFile 
			listChangeVoiseDatas.add voiceDataFile
		}
		*/
		
		// TODO: tarkasta että kaikki .smil tiedostot tulevat käsitellyiksi!
		
		// fOutputDir
		/*
		 * Ei lueta ja käsitellä content.html:ää:
		File fContent = new File(fMachineSmilDir.absolutePath +File.separator + "content.html")
		if (!fContent.exists())
		{
			System.err.println "Tiedosto puuttuu: " + fContent
			usage()
		}
		
		StringBuffer sb = new StringBuffer()
		def matcher, smilbasename, smilid, smiltext
		def founded = false, space
		def contextText = fContent.getText()
		def before, indEnd = 0, indMatcher = 0
		matcher = contextText =~ /(\s*)<a\shref="(.*?)\.smil#(.*)?">[\n\s]*(.*)[\n\s]*?<\/a>/
		
		while(matcher.find())
		{
			
			before = contextText.substring(indEnd, matcher.start())
			sb.append before 
			?*
			if (!line)
			{
				sb.append("\n")
				continue
			}
			*?
			
			space 			= matcher[indMatcher][1]
			smilbasename 	= matcher[indMatcher][2]
			smilid		 	= matcher[indMatcher][3]
			smiltext		= matcher[indMatcher++][4]
			voiceDataFile 	= listXmlDataFiles.get(smilbasename.toString())
			if (!voiceDataFile)
			{
				sb.append(contextText.substring(matcher.start(), matcher.end()) +"\n")
				indEnd = matcher.end()
				continue
			}
			
			founded = false
			for(VoiceData voicedata in voiceDataFile.listitems)
			{
				if (!voicedata.used && voicedata.text == smiltext.toString().trim())
				{
					founded = true
					// voicedata.start, voicedata.endä
					voicedata.used = true
					voicedata.smilid = smilid
					sb.append(space +"<a href=\"" + voiceDataFile.basename +".smil#" + smilid + "\">" + smiltext + "</a>\n")
					break
				} 
			}			
			if (!founded)
				sb.append(contextText.substring(matcher.start(), matcher.end()) +"\n" +"\n")
			indEnd = matcher.end()
		}
		
		def after = contextText.substring(indEnd)
		sb.append after
		
		File fNewContent = new File(fOutputDir.absolutePath +File.separator + "content.html")
		if (fNewContent.exists())
			fNewContent.delete()
		fNewContent.append(sb.toString(), "UTF-8")
				 */
		
		// muuta vastaavia .smil tiedostojen aikoja:
		File fSmil, fNewSmil
		def text, newtext, allowedSmilNames = cfgFilePairs.keySet() 
		def VoiceDataFile voiceFile
		
		// for(VoiceDataFile voiceFile in listChangeVoiseDatas)
		
		for(smilfname in cfgFilePairs.keySet())
		{
			cfgPair = (CfgFilePair)cfgFilePairs.get(smilfname)
			if (!cfgPair)
				continue // TODO: VIRHE!
			
			println "Käsitellään: " +cfgPair.fSmil.getName() 
			// voiceFile = cfgPair.voicedatafiles 
			if (cfgPair.voicedatafiles.size() == 0)
				continue // TODO: VIRHE!	
					
			//if (!(voiceFile.basename +".smil" in allowedSmilNames))
				// continue // ei kelvollinen smil tiedosto
				
			fSmil = cfgPair.fSmil
			if (!fSmil.exists())
				eror("Tiedostoa ei ole: " +fSmil)
				
			text = fSmil.getText()
			if (!text) 
				eror("Tiedosto: " +fSmil +" on tyhjä!")
			newtext = changeSmilText(text, cfgPair.voicedatafiles)
			if (!newtext)
				eror("Tiedosto: " +fSmil +" newtext on tyhjä!")
			if (newtext == text)
				eror("Tiedosto: " +fSmil +". Ei onnistuttu muuttaa tekstin sisältää!")
				
			fNewSmil = new File(fOutputDir.absolutePath +File.separator + fSmil.getName())
			if (fNewSmil.exists())
				fNewSmil.delete()
			fNewSmil.append(newtext, "UTF-8")
		}
		
		println "Muutetut tiedostot hakemistossa: " +fOutputDir
		println "Ohjelma valmis."
	}
	
	def private String changeSmilText(String smiltext, voiceFiles)
	throws Exception	
	{
		String ret = smiltext
		if (ret && voiceFiles)
		{
			StringBuffer sb = new StringBuffer()
			// /<par[\\s\n\\w=".><#\/-]*?<\/par>/
			// [\s\n\w=".><#\/-]*?<\/par>
			def matcher = ret =~ /\s*<par[\s\n\w=".><#\/-]*?<\/par>/
			def before = null, after = null, after_ind = -1
			
			def alink
			def newAlink
			def voiceFileInd = 0
			VoiceDataFile voiceFile = (VoiceDataFile)voiceFiles.getAt(voiceFileInd)
			def startInd = 0, iCounterALink = 0
			
			if (!voiceFile)
				throw new Exception	("changeSmilText: voiceFile is null!")
				
			while(matcher.find()) {				
				if (!before)
				{
					before = ret.substring(0, matcher.start())
					sb.append(before)
				}
				alink	 	= matcher[startInd++]
				if (voiceFile.listitems.size() == iCounterALink)
				{
					voiceFile = voiceFiles.getAt(++voiceFileInd)
					if (!voiceFile)
					throw new Exception	("changeSmilText: voiceFile is null!")
				}
				newAlink = getNewAlink(alink, voiceFile, ++iCounterALink)
				sb.append(newAlink)
				after_ind = matcher.end()
			} 					
			
			if (!before)
				throw new Exception("<par...</par> ei löytynyt!")
			
			after = (after_ind == -1 ? "" : ret.substring(after_ind))
			// matcher.appendTail(sb)
			if (after)
				sb.append after 
			ret = sb.toString()
		}
		ret
	}

	def private String getNewAlink(String alink, VoiceDataFile voiceFile, int iCounterALink)
	throws Exception
	{
		if (bDebug)
			println alink
		if (!alink)
			return alink
		// /(\s*<par\sendsync=".*?"\sid=".*?">[\n\s]*<text id="(.*?)"\ssrc="(.*)?#(.*)?"\s\/>[\n\s]*<audio src="(.*)?"\sid="(.*)?"\sclip-begin="(.*)?"\sclip-end="(.*)?"\s\/>[\n\s]*<\/par>)/
		def matcher = alink =~  /(?-s:(\s*<par\s.*?>[\n\s\n]*)(<text\s.*[\s\t\n]*.*)([\s\t\n]*<\/par>))/
		if (!matcher.find())
			throw new Exception("getNewAlink ei löytynyt!: par!" +alink)
		def strFounded = matcher[0]
		def par_before = alink.substring(0, matcher.start())
		def par_par_start 	= matcher[0][1]
		def par_par_text 	= matcher[0][2] 
		def par_par_end 	= matcher[0][3]
		
		def ret = ""
		def before = ret.substring(0, matcher.start())
		StringBuffer sb = new StringBuffer()
		sb.append(par_before)
		def par_part = matcher[0][1]
		sb.append(par_par_start)
		sb.append(getNewTextAndAudio(par_par_text, voiceFile, iCounterALink))
		sb.append(par_par_end)
		sb.toString()
	}
	
	def private String getNewTextAndAudio(String par_par_text, VoiceDataFile voiceFile, int iCounterALink)
	throws Exception
	{
		if (!par_par_text)
			throw new Exception("getNewTextAndAudio tyhjä mjono!: par_par_text!")
		
		// /(\s*<par\sendsync=".*?"\sid=".*?">[\n\s]*<text id="(.*?)"\ssrc="(.*)?#(.*)?"\s\/>[\n\s]*<audio src="(.*)?"\sid="(.*)?"\sclip-begin="(.*)?"\sclip-end="(.*)?"\s\/>[\n\s]*<\/par>)/
		def matcherSclipBegin 	= par_par_text =~  /[\n\s]*<audio.*?(\sclip-begin="(.*?)")/		
		def matcherSclipEnd 	= par_par_text =~  /[\n\s]*<audio.*?(\sclip-end="(.*?)")/
		if (!matcherSclipBegin.find())
			throw new Exception("getNewTextAndAudio ei löytynyt!: <audio...clip-begin= !" +par_par_text)
		if (!matcherSclipEnd.find())
			throw new Exception("getNewTextAndAudio ei löytynyt!: <audio...clip-end= !" +par_par_text)
		def strFoundedmatcherSclipBegin = matcherSclipBegin[0][1].toString()
		def strFoundedmatcherSclipEnd = matcherSclipEnd[0][1].toString()
		def strSclipBegin = matcherSclipBegin[0][2].toString()
		def strSclipEnd = matcherSclipEnd[0][2].toString()
		// def par_before = // par_par_text.substring(0, matcher.start())
		int ind = strSclipBegin.toString().indexOf('=')
		if (ind == -1)
			throw new Exception("getNewTextAndAudio = ei löytynyt!: <audio...clip-begin !" +par_par_text)
		if (ind == 0)
			throw new Exception("getNewTextAndAudio sclip-begin nimi puuttuuu!: <audio...clip-begin !" +par_par_text)
		// def text_id 	= matcher[0][1]
		// def src_file 	= matcher[0][2]
		// def src_id	 	= matcher[0][3]
		// def audio_src	= matcher[0][4]
		// def audio_id	= matcher[0][5]
		def clip_begin	= strSclipBegin
		ind = strSclipEnd.indexOf('=')
		if (ind == -1)
			throw new Exception("getNewTextAndAudio = ei löytynyt!: <audio...clip-end!" +par_par_text)
		if (ind == 0)
			throw new Exception("getNewTextAndAudio sclip-begin nimi puuttuuu!: <audio...clip-end !" +par_par_text)
		def clip_end	= strSclipEnd
				
		def VoiceData clipVoiceData = voiceFile.getClipVoiceData(iCounterALink /* text_id */)
		if (!clipVoiceData)
			return par_par_text // TODO: ERROR ??!!
			
		//	ret.start 	= (startTime == 0.0 ? 0.0 : startTime / 1000)
		//	ret.end 	= endTime / 1000
	
		def clip_start_new	= (clipVoiceData.start == 0.0 ? 0.0 : clipVoiceData.start / 1000)
		if (clip_start_new == null)
			throw new Exception("getNewAlink:  ei löytynyt: clip_start_new!")
		def clip_end_new	= clipVoiceData.end / 1000
		if (clip_end_new == null)
			throw new Exception("getNewAlink:  ei löytynyt: clip_end_new!")		
		clip_begin = getClipString(clip_begin, clip_start_new).replaceAll("\n", "")
		def new_strFoundedmatcherSclipBegin = strFoundedmatcherSclipBegin.replace(strSclipBegin, clip_begin)
		clip_end = getClipString(clip_end, clip_end_new).replaceAll("\n", "")
		def new_strFoundedmatcherSclipEnd = strFoundedmatcherSclipEnd.replace(strSclipEnd, clip_end)
		par_par_text = par_par_text.replace(strFoundedmatcherSclipBegin, new_strFoundedmatcherSclipBegin).replace(strFoundedmatcherSclipEnd, new_strFoundedmatcherSclipEnd)
		// audio_src 	= voiceFile.basename +".mp3"
		par_par_text
	}

	def private String getNewAlink_old(String alink, VoiceDataFile voiceFile, int iCounterALink)
	throws Exception
	{
		if (bDebug)
			println alink
		if (!alink)
			return alink
		// /(\s*<par\sendsync=".*?"\sid=".*?">[\n\s]*<text id="(.*?)"\ssrc="(.*)?#(.*)?"\s\/>[\n\s]*<audio src="(.*)?"\sid="(.*)?"\sclip-begin="(.*)?"\sclip-end="(.*)?"\s\/>[\n\s]*<\/par>)/
		def matcher = alink =~  /(\s*<par\s.*?>[\n\s]*)(<text id="(.*?)"\ssrc="(.*)?#(.*)?"\s\/>[\n\s]*<audio src="(.*)?"\sid="(.*)?"\sclip-begin="(.*)?"\sclip-end="(.*)?"\s\/>)[\n\s]*<\/par>/
		if (!matcher.find())
			throw new Exception("getNewAlink ei löytynyt!: " +alink)
		def ret = ""
		def strFounded = matcher[0]
		def before = ret.substring(0, matcher.start())
		StringBuffer sb = new StringBuffer()
		sb.append(before)
		def par_part = matcher[0][1]
		sb.append(par_part)		
		def text_block 	= matcher[0][2]
		def text_begin
		if (text_block)
		{
			def search = "<text id=\""
			int ind = text_block.toString().indexOf(search)
			if (ind > -1)
			{
				 text_begin = text_block.toString().substring(0, ind+search.length())				
				//sb.append(text_begin)
			}
			else
				throw new Exception("getNewAlink text_begin!: " +alink)
		}
		def text_id 	= matcher[0][3]
		def src_file 	= matcher[0][4]
		def src_id	 	= matcher[0][5]
		def audio_src	= matcher[0][6]
		def audio_id	= matcher[0][7]
		def clip_begin	= matcher[0][8]
		def clip_end	= matcher[0][9]
				
		def VoiceData clipVoiceData = voiceFile.getClipVoiceData(iCounterALink /* text_id */)
		if (!clipVoiceData)
			return alink
			
		//	ret.start 	= (startTime == 0.0 ? 0.0 : startTime / 1000)
		//	ret.end 	= endTime / 1000
	
		def clip_start_new	= (clipVoiceData.start == 0.0 ? 0.0 : clipVoiceData.start / 1000)
		if (clip_start_new == null)
			throw new Exception("getNewAlink:  ei löytynyt: clip_start_new!")
		def clip_end_new	= clipVoiceData.end / 1000
		if (clip_end_new == null)
			throw new Exception("getNewAlink:  ei löytynyt: clip_end_new!")		
		clip_begin = getClipString(clip_begin, clip_start_new).replaceAll("\n", "")
		clip_end = getClipString(clip_end, clip_end_new).replaceAll("\n", "")
		// audio_src 	= voiceFile.basename +".mp3"
		
		def new_alink = 
"""$text_id" src="$src_file#$src_id" />
    <audio src="$audio_src" id="$audio_id" clip-begin="$clip_begin" clip-end="$clip_end" />
</par>"""
		sb.append(new_alink /* .replaceAll("\n\"", "\"") */)
		sb.toString()
	}
	
	def private String getClipString(String oldClip, double newClipValue)
	{
		try {
		if (!oldClip)
			return null
		if (newClipValue == null)
			return null
		int ind = oldClip.indexOf('=')
		if (ind == -1)
			return "" +newClipValue +"s"
		String ret = oldClip.substring(0, ind) +"=" +String.format("%.3f%n", newClipValue).replace(',', '.').replaceAll("[\r\n]+", "")
		return ret +"s"
		} catch(Exception e){
			e.printStackTrace()
		}
		
	}
}
