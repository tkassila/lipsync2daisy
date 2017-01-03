/**
 * 
 */
package fi.celia.app.smil2voicesmil.test

import java.util.List;

/**
 * @author tkassila
 *
 */
class CheckTwoClassesofOneClassCompilerError {

	/**
	 * 
	 */
	public CheckTwoClassesofOneClassCompilerError() {
	}

	static main(args) {
		if (args.length == 0)
			return 
		File fGroovy = new File(args[0])
		String code = fGroovy.getText()
		int indBegin = code.indexOf("{")
		int indEnd = code.lastIndexOf("}")
		String possiblecodebetween
		
		def matcherMethod = code // ~= /((def)?\s+public static List getListCustomTestAttributeNames()/
		while(indBegin > -1 && indEnd > -1)
		{
			if (indBegin > 0 && indEnd > 0)
			{ // possible code between methods
				possiblecodebetween = code.substring(indBegin, indEnd)
				if (possiblecodebetween)
				{
					println possiblecodebetween
				}
			}
			indEnd = code.lastIndexOf("}", indBegin)
			if (indEnd > -1)
				indBegin = code.indexOf("{", indEnd)
			// indBegin
		}
	}

}
