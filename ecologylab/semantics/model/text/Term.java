package ecologylab.semantics.model.text;

import java.text.DecimalFormat;


public class Term implements Comparable<Term>
{

	private String stem;
	private String word;
	private boolean hasWord = false;
	private double idf;
	public static DecimalFormat ONE_DECIMAL_PLACE = new DecimalFormat("#0.0");

	public double idf()
	{
		return idf;
	}

	protected Term(String stem, double idf)
	{
		this.stem = stem;
		this.idf = idf;
	}
	
	public String toString()
	{
		return stem;
	}

	protected void setWord ( String word )
	{
		hasWord = true;
		this.word = word;
	}
	
	public String getWord()
	{
		if (!hasWord)
			return stem;
		return word;
	}
	
	public String getStem()
	{
		return stem;
	}
	
	public boolean hasWord()
	{
		return hasWord;
	}

	public boolean isStopword ( )
	{
		return false;
	}

	public int compareTo(Term o) 
	{
		double difference = this.idf() - o.idf();
			
		if (difference > 0)
			return -1;
		else
			return (difference == 0) ? this.getWord().compareTo(o.getWord()) : 1;
	}

}
