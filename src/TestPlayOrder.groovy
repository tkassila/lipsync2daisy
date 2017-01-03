
class TestPlayOrder {

	public TestPlayOrder() {
		// TODO Auto-generated constructor stub
	}

	static main(args) {
		File f = new File(args[0]) // koe.txt")
		println f
		
		def text = f.getText()
		if (text)
		{
			def matcher = text =~ /class="(.+?)".*?playOrder="(\d+)"/
			def hmPlayOders = [:], value, key1, listPlayOrders = []
			int iCnter = 0 
			def classvar, rowmatcher
			
			while(matcher.find())
			{
				rowmatcher = matcher[iCnter++]
				listPlayOrders.add rowmatcher
				classvar = rowmatcher[1]
				key1 = rowmatcher[2]
				value = hmPlayOders.get(key1)
				// listPlayOrders.add key1
				if (value==null)
					hmPlayOders.put(key1, 1)
				else
					hmPlayOders.put(key1, ++value)				
			}
			
			def item
			int max = 0
			for (key in hmPlayOders.keySet())
			{
				item = hmPlayOders.get(key)
				println key +" " + item
				if (max < Integer.parseInt(key))
					max = Integer.parseInt(key)
			}
			
			println "\nPuuttuvat numerot:"
			(1 .. max).each {
				if (hmPlayOders.get(it.toString()) == null)
					println "" +it
				
			}
			
			println "\nlista numerot:"
			for(list_item in listPlayOrders)
			{
				println list_item
			}
		}
	}

}
