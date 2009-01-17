package ecologylab.semantics.model.text;

import ecologylab.generic.IFeatureVector;

public class InterestModel
{
	// participant interest vector

	private static TermVector	participantInterest		= new TermVector();

	private static TermVector	unitParticipantInterest	= new TermVector();

	private static long			timestamp;

	public static double		INTEREST_TIME_CONSTANT	= 120;

	public static void expressInterest ( IFeatureVector<Term> interestingTermVector, short magnitude )
	{
		timeScaleInterest();
		TermVector xtv = new TermVector(interestingTermVector);
		xtv.multiply(magnitude);
		xtv.clamp(magnitude);
		participantInterest.add(1, xtv);
		unitize();
	}

	public static void expressInterest ( Term term, short magnitude )
	{
		magnitude /= 2;
		timeScaleInterest();
		participantInterest.add(term, magnitude);
		unitize();
	}

	private static void timeScaleInterest ( )
	{
		long delta_t = System.nanoTime() - timestamp;
		double delta_t_in_seconds = delta_t / 1e9;
		participantInterest.multiply(Math.exp(-delta_t_in_seconds / INTEREST_TIME_CONSTANT));
		timestamp = System.nanoTime();
	}

	public static TermVector getPIV ( )
	{
		return participantInterest;
	}

	public static double getAbsoluteInterestOfTermVector ( IFeatureVector<Term> tv )
	{
		return unitParticipantInterest.idfDotSimplex(tv);
	}

	public static short getInterestExpressedInTermVector ( IFeatureVector<Term> termVector )
	{
		double retVal = unitParticipantInterest.dotSimplex(termVector);
		retVal /= unitParticipantInterest.commonDimensions(termVector);
		retVal *= 10;
		return (short) retVal;
	}

	public static short getInterestExpressedInXTerm ( Term term )
	{
		return (short) (2 * participantInterest.get(term));
	}

	public static void expressInterest ( InterestExpressibleElement element, short magnitude )
	{
		expressInterest(element.getInterestExpressionTermVector(), magnitude);
	}

	public static short getInterestExpressedInElement ( InterestExpressibleElement element )
	{
		return getInterestExpressedInTermVector(element.getInterestExpressionTermVector());
	}

	private static void unitize ( )
	{
		unitParticipantInterest = participantInterest.unit();
	}

	public static void setTermInterest ( Term term, short newValue )
	{
		participantInterest.set(term, newValue);
		unitize();
	}

	public static void expressInterest ( String query, short i )
	{
		expressInterest(new TermVector(query), i);
	}

}
