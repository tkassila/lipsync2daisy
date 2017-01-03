package groovy.runtime.metaclass.java.lang

class StringMetaClass2 extends DelegatingMetaClass {
	
	public static oldprintln

	StringMetaClass2(MetaClass meta) {
		super(meta)
	}
	
	Object invokeMethod(Object object, String method, Object[] arguments) {		
		// oldprintln "StringMetaClass-> "
		if (method == 'hasGroovy') {
			object ==~ /.*[Gg]roovy.*/
		} else {
			super.invokeMethod object, method, arguments
		}
	}
}
