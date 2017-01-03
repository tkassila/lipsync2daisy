/**
 * 
 */

/**
 * @author tk
 *
 */
class TestLanguageFiles {

	/**
	 * 
	 */
	public TestLanguageFiles() {		
	}

	static main(args) {
		def packagepath = "fi.celia.app.smil2voicesmil".replaceAll("(?s)\\.", "\\\\")
		def File fSource = new File("src" +File.separator + packagepath + File.separator +"Lipsync2Smil.groovy")
		if (!fSource.exists())
			throw new FileNotFoundException("Lipsync2Smil.groovy does not exists!")
		def sourceText = fSource.getText("UTF-8")
		if (!sourceText)
			throw new Exception("Lipsync2Smil.groovy: missing content!")
		
		def File flanfi = new File("lipsync2smil_fi_FI.properties")
		if (!flanfi.exists())
			throw new FileNotFoundException(flanfi +" does not exists!")
		def flanfiText = flanfi.getText("UTF-8")
		if (!flanfiText)
			throw new Exception(flanfi +": missing content!")

		def File flanen = new File("lipsync2smil_en_EN.properties")
		if (!flanen.exists())
			throw new FileNotFoundException(flanen +" does not exists!")
		def flanenText = flanen.getText("UTF-8")
		if (!flanenText)
			throw new Exception(flanen +": missing content!")
	
		def File flansw = new File("lipsync2smil_sw_SW.properties")
		if (!flansw.exists())
			throw new FileNotFoundException(flansw +" does not exists!")
		def flanswText = flansw.getText("UTF-8")
		if (!flanswText)
			throw new Exception(flansw +": missing content!")
		
		def constName, constValue, i = 0, iFounded = 0
		def matcher, listNames = [], listValues = [], matcher2
		
		println "Missing language lines, variables: (still can exists in code without constUI_ by ex.: .getMeessage('daisy2uitext') "
		
		sourceText.eachLine { line ->
			matcher = line =~ /(?s)(constUI_.*?)\s*=\s*"(.*?)"/			
			if(matcher.find())
			{			
				constName = matcher[i][1].toString()
				constValue = matcher[ i][2].toString()
				
				if (constName == "constUI_ui_unmodifiedsentencies")
					println "stop"
					
				if (constName.toString().contains(" ") || constName.toString().startsWith("//"))
					return
				
				if (!constValue)
				{
					println "Error: " +constName +"has empty constValue value!"
				}
						
				iFounded++
				listValues.add(constValue)
				listNames.add(constName)
				
				/*
				if (!constName|| !constName.contains("constUI_"))
					return
				if (!constValue || constValue.contains("true") || constValue.contains("track"))
					return
				*/
				// println constValue
				matcher2 = flanfiText =~ /(?s)$constValue?\s*(?==)/
				if (!matcher2.find())
				{
					println constValue +" puuttuu tiedostosta: " +flanfi
				}
								
				matcher2 = flanenText =~ /(?s)$constValue?\s*(?==)/
				if (!matcher2.find())
				{
					println constValue +" puuttuu tiedostosta:" +flanen
				}
				
				matcher2 = flanswText =~ /(?s)$constValue?\s*(?==)/
				if (!matcher2.find())
				{
					println constValue +" puuttuu tiedostosta:" +flansw
				}
			}
		}
		
		def langname, values, langvalues = listValues.toArray()
		
		println "\nVakiota ei ole koodin consteissa:"
		def allreadyprinted = []
		
		flanfiText.eachLine { line ->
			values = line.split "="
			if (!values)
				return
			langname = values[0].toString()
			if (!langname)
				return
			if (!(langname in listValues))
			{
				println langname +" puuttuu sorsasta"
				allreadyprinted.add(langname)
			}
		}
		
		flanenText.eachLine { line ->
			values = line.split "="
			if (!values)
				return
			langname = values[0]
			if (!langname)
				return
			if (!(langname in allreadyprinted) && !(langname in listValues))
			{
				println langname +" puuttuu sorsasta"
				allreadyprinted.add(langname)
			}
		}
		
		flanswText.eachLine { line ->
			values = line.split "="
			if (!values)
				return
			langname = values[0]
			if (!langname)
				return
			if (!(langname in allreadyprinted) && !(langname in listValues))
			{
				println langname +" puuttuu sorsasta "
				allreadyprinted.add(langname)
			}
		}
		
		println "Founded " +iFounded
		def listNamesInCode = []
		
		println()
		println "Wrong writen const names:"
		println()
		
		int iLine = 0
		sourceText.eachLine { line ->
			iLine++
			matcher = line =~ /(?s)(constUI_.*?)\W/
			if(matcher.find())
			{
				constName = matcher[i][1].toString()				
				if (!constValue)
				{
					println "Error: " +constName +"has empty constValue value!"
				}
					
				if (!(constName in listNames))
					println "Error " +iLine +": " +line
			}
		}
		println "valmis"
	}

}
