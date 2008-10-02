package ecologylab.semantics.model.text;

import java.util.HashSet;


public class XTerm
{

	public String stem;
	public HashSet<XReferringElement> referringElements;
	private double idf;

	public double idf()
  {
    return idf;
  }

  protected XTerm(String stem, double idf)
	{
		this.stem = stem;
		this.idf = idf;
	}

	protected void addReference(XReferringElement r)
			throws ReferringElementException
	{
		if (referringElements.contains(r))
			throw new ReferringElementException("Referring Element " + r + " already exists in term: " + this);
		referringElements.add(r);
	}

	protected void removeReference(XReferringElement r)
			throws ReferringElementException
	{
		if (!referringElements.contains(r))
			throw new ReferringElementException("Referring Element " + r + " doesn't exist in term: " + this);
		referringElements.remove(r);
	}


	class ReferringElementException extends RuntimeException
	{
		public ReferringElementException(String man)
		{
			super(man);
		}
	}
	
	public String toString()
	{
	  return stem + "(" + idf + ")";
	}

}
