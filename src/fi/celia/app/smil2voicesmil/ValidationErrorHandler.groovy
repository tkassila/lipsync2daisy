package fi.celia.app.smil2voicesmil

	/**
	 * xml validation / xml errorhandler class before a xml convertion. This class is collecting
	 * xml parser exceptions, errors etc.
	 */
	class ValidationErrorHandler implements org.xml.sax.ErrorHandler
	{
		def listExceptions = []
		def listWarnings = []
		def listFatalErrors = []
		def noExceptions = true
		def noFatalErrors = true
		def noWarnings = true
		
		/**
		 * Receive notification of a recoverable error.
		 */
		void error(org.xml.sax.SAXParseException exception)
		{
			listExceptions.add exception
			noExceptions = false
		}
		/**
		 * Receive notification of a non-recoverable error.
		 */
		void fatalError(org.xml.sax.SAXParseException exception)
		{
			listFatalErrors.add exception
			noFatalErrors = false
		}
		/**
		 * Receive notification of a warning.
		 */
		void 	warning(org.xml.sax.SAXParseException exception)
		{
			listWarnings.add exception
			noWarnings = false
		}		
	}
