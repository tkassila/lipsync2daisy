/**
 * 
 */

	import java.io.Console;

		import java.nio.charset.Charset;

		import java.lang.reflect.Constructor;

		import java.lang.reflect.Field;

		import java.lang.reflect.InvocationTargetException;

		import static java.lang.System.out;

		import java.io.*;

		import java.math.RoundingMode;
		import java.text.DecimalFormat;
		
/**
 * @author Tuomas
 *
 */
public class TestCS {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
			// store Console character set in the private field

		// public class ConsoleCharset {

		// public static void main(String... args) {
		
		DecimalFormat cnstTimeFormatter = new DecimalFormat("####0.000");
	  	cnstTimeFormatter = new DecimalFormat("#####:##:#0.000");

		cnstTimeFormatter.setRoundingMode(RoundingMode.UNNECESSARY);

		double d1 = 0.0;		
		System.out.println (cnstTimeFormatter.format(d1));
		double d2 = 0.034;		
		System.out.println (cnstTimeFormatter.format(d2));
		double d3 = 100.563;		
		System.out.println (cnstTimeFormatter.format(d3));

		
		Constructor[] ctors = Console.class.getDeclaredConstructors();

		Constructor ctor = null;

		for (int i = 0; i < ctors.length; i++) 
		{

		ctor = ctors[i];

		if (ctor.getGenericParameterTypes().length == 0)
			break;

		}

		// obtain the internal character set used by java.io.Console

		try {

		ctor.setAccessible(true);
		
		try {
			System.setOut(new PrintStream(System.out, true, "UTF-8"));
		} catch(Exception e){
			e.printStackTrace();
		}
		
		Console c = (Console)ctor.newInstance();
		Field f = c.getClass().getDeclaredField("cs");
		f.setAccessible(true);
		out.format("Console charset : %s%n", f.get(c));
		out.format("Charset.defaultCharset(): %s%n", Charset.defaultCharset());

		c.printf("%s", "kISSA Kävelee öytä pitkin...");
		c.flush();
		// intercept the system input exceptions

		} catch (InstantiationException x) {

		x.printStackTrace();

		} catch (InvocationTargetException x) {

		x.printStackTrace();

		} catch (IllegalAccessException x) {

		x.printStackTrace();

		} catch (NoSuchFieldException x) {

		x.printStackTrace();

		}

	}
}
