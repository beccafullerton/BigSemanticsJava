/**
 * 
 */
package ecologylab.bigsemantics.seeding;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.documentparsers.FeedParser;

/**
 * A Feed is a Document Seed whose links are interleaved by a SeedDistributor, enabling round-robin scheduling
 * of downloads.
 * 
 * @author andruid
 * @author abhinav
 */
public class Feed extends DocumentState
{

	private String queryString;
	
	/**
	 * 
	 */
	public Feed()
	{

	}

  protected boolean useDistributor()
  {
  	return true;
  }
  
  @Override
	public void performInternalSeedingSteps(SemanticsGlobalScope infoCollector)
	{
	 new FeedParser(infoCollector, this);
	}
  
	/**
	 * Called after a seed is parsed to prevent it being parsed again later during re-seeding.
	 * This override does nothing, because Feed seeds should remain active.
	 * 
	 * @param inActive the inActive to set
	 */
	@Override
	public void setActive(boolean value)
	{
		
	}

	/**
	 * (current for debug only)
	 */
	@Override
	public String getQuery()
	{
		if (queryString == null)
		{
			queryString = "Feed(" + url + ")";
		}
		return queryString;
	}
	
}
