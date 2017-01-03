
import groovy.runtime.metaclass.java.lang.StringMetaClass2

class TestMetaPrintln {

	public TestMetaPrintln() {
		// TODO Auto-generated constructor stub
	}

	static main(args) 
	{
			StringMetaClass2.oldprintln = String.metaClass.&println
			println "kissa"
			def String koe = "mrhaki"
			
			//System.console().println " yötön yö äitien päivä!"
			println " yötön yö äitien päivä!"
			
			// Original methods are still invoked.
			println koe
			assert koe.toUpperCase() == 'MRHAKI'
			
			if (koe.toUpperCase().equals('MRHAKI'))
				println "assert ok"
			else
				println "assert not ok"
			// Invoke 'hasGroovy' method we added via the DelegatingMetaClass.
			// assert !'Java'.hasGroovy()
			// assert 'mrhaki loves Groovy'.hasGroovy()
			// assert 'Groovy'.toLowerCase().hasGroovy()
	}	
}
