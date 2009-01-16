package ecologylab.semantics.model.text;

import java.text.DecimalFormat;
import java.util.HashSet;


public class Term
{

	public String stem;
	public String word;
	public boolean hasWord = false;
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

	public void setWord ( String word )
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
	
	public boolean hasWord()
	{
		return hasWord;
	}

	public boolean isStopword ( )
	{
		return false;
	}

}
