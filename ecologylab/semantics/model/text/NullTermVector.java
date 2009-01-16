package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Observer;
import java.util.Set;

import ecologylab.generic.IFeatureVector;

public class NullTermVector implements ITermVector
{

	public static NullTermVector	ntv	= new NullTermVector();

	public static NullTermVector singleton()
	{
		return ntv;
	}

	public void addObserver(Observer o)
	{
		// TODO Auto-generated method stub

	}

	public void deleteObserver(Observer o)
	{
		// TODO Auto-generated method stub

	}

	public double dot(IFeatureVector<Term> v)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public Set<Term> elements()
	{
		// TODO Auto-generated method stub
		return new HashSet<Term>();
	}

	public double get(Term term)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public HashMap<Term, Double> map()
	{
		// TODO Auto-generated method stub
		return new HashMap();
	}

	public Set<Double> values()
	{
		// TODO Auto-generated method stub
		return new HashSet<Double>();
	}

	public double norm()
	{
		return 0;
	}

	public double idfDot(IFeatureVector<Term> v)
	{
		return 0;
	}

	public String toString()
	{
		return "NullTV";
	}

	public IFeatureVector<Term> unit()
	{
		return this;
	}
	
	public IFeatureVector<Term> simplex()
	{
		return this;
	}

	public int commonDimensions(IFeatureVector<Term> v)
	{
		// TODO Auto-generated method stub
		return 1;
	}

	public double dotSimplex(IFeatureVector<Term> v)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public double idfDotNoTF(IFeatureVector<Term> v)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
