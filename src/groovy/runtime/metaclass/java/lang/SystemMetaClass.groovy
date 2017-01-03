package groovy.runtime.metaclass.java.lang

class SystemMetaClass extends DelegatingMetaClass 
{
	public SystemMetaClass(MetaClass meta) {
		super(meta)
	}

	Object invokeMethod(Object object, String method, Object[] arguments) {
		if (method == 'println') {
			// super.invokeMethod "Called lipsync_println"
			super.invokeMethod object, method, arguments
		} else {
			super.invokeMethod object, method, arguments
		}
	}
}