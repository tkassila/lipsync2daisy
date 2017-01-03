/**
 * 
 */

//import java.text.DateFormat;
import java.text.*;
import java.util.*;

//import org.joda.time.*;
//import org.joda.time.format.*;

public class TestNumber {  
 
    public static void main(String args[])  {
     
        //formatting numbers upto 2 decimal places in Java
        DecimalFormat df = new DecimalFormat("0,00,00.000");       
        DecimalFormat mydf = new DecimalFormat("#:##:#0.000");
        System.out.println(mydf.format(1.0));
        System.out.println(df.format(364565.1454));
        String test = df.format(1000.145);
        // System.out.println(test.replaceAll("([0-9]+)[\\s+]([0-9]+)[\\s+]([0-9]+,[0-9]+)", "$1:$2:$3"));
        System.out.println(test.replaceAll("([0-9]+)[\\s\\p{Z}]+", "$1:"));
        char ch = test.charAt(1);
        int d = (int)ch;
             
        Calendar cal = Calendar.getInstance();
        cal.set(cal.getTime().getHours(), cal.getTime().getMonth(), cal.getTime().getDate(), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeInMillis(cal.getTimeInMillis() +200000);
        java.text.DateFormat df2 = java.text.DateFormat.getTimeInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String strTime = sdf.format(cal.getTime());
        System.out.println(strTime);
        
        
    }
     
}

/*
Output:
364,565.14
364,565.15
364,565.140
364,565.145
*/

// Read more: http://javarevisited.blogspot.com/2012/03/how-to-format-decimal-number-in-java.html#ixzz2KdJQ5Jpz
