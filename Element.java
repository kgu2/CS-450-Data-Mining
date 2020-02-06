import java.math.BigDecimal;

public class Element implements Comparable<Element>{

	    private int index;
	    private double value;

	    
	    Element(int index, double value){
	        this.index = index;
	        this.value = value;
	    }

	    public int compareTo(Element e) {
	    		    
	        if(this.value < e.value)
	        {
	        	return -1;
	        }
	        else if (this.value > e.value)
	        {
	        	return 1;
	        }
	        else
	        {
	        	return 0;
	        }	
	    }
	    
	    public double getvalue()
	    {
	    	return this.value;
	    }
	    
	    public int getindex()
	    {
	    	return this.index;
	    }
	    
	    public String toString()
	    {
	    	return this.value + "";
	    }

	
}
