import java.text.DecimalFormat;
import java.util.Calendar;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


class TestGNumber {

	public TestGNumber() {
		// TODO Auto-generated constructor stub
	}

	static main(args) {
	
		/*
		java.util.Date date2 = new java.util.Date(100);
		Calendar now = Calendar.getInstance();
		now.time = date2
		DateTimeFormatter parser2 = DateTimeFormat.forPattern("HH:mm:ss:SSS");
		println parser2.parseDateTime(now.toString())
		*/
		
		println("Groovy GDK Date.clearTime()")
		def now = new Date()
		println "Now: ${now}"
		def timelessNow = now.clearTime()
		def timelessNow2 = now.clearTime()
		// timelessNow.setTime(1)
		/*
		timelessNow.setHours(0)
		timelessNow.setMinutes(0)
		timelessNow.setSeconds(0)
		*/
		// timelessNow.setTime((10000*6)*60)
		timelessNow.setTime(1)
		println "Now sans Time: ${timelessNow}"
		println "Mutated Time:  ${now}"
		
		def dateString = now.format("HH:mm:ss:SSS")
		def dateString2 = timelessNow.format("HH:mm:ss:SSS")
		def dateString3 = timelessNow2.format("HH:mm:ss:SSS")
		println "Formatted Now: ${dateString}"
		println "timelessNow Now: ${dateString2}"
		println "timelessNow2 Now: ${dateString3}"
		
		//formatting numbers upto 3 decimal places in Java
		// df = new DecimalFormat("#,###,##0.000");

	}

}
