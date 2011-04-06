/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;

/**
 * This is the base class for all seeds which bascially return a collection of links to be
 * interleaved with other seeds. Example of such seeds are search result pages and feeds.
 * 
 * @author andruid
 * 
 */
public abstract class LinksetParser
		extends ParserBase implements DispatchTarget<Document>
{

	public LinksetParser(NewInfoCollector infoCollector)
	{
		super(infoCollector);
	}

	/**
	 * @param infoCollector
	 * @param seed
	 * @param defaultTag TODO
	 */
	protected void getMetaMetadataAndContainerAndQueue(NewInfoCollector infoCollector, ParsedURL purl, Seed seed, String defaultTag)
	{
		Document document								= infoCollector.getGlobalDocumentMap().getOrConstruct(purl);
		DocumentClosure documentClosure	= document.getOrConstructClosure();
		if (documentClosure != null)
		{
//			container.presetDocumentType(this);
			documentClosure.setPresetDocumentParser(this);
			documentClosure.setDispatchTarget(this);
			document.setAsTrueSeed(seed);

			seed.queueSeedOrRegularContainer(documentClosure);			
		}
	}


	/**
	 * call doneQueueing() to notify seed distributor
	 */
	public void delivery(Document sourceDocument)
	{
		Seed seed = sourceDocument.getSeed();
		if (seed != null)
		{
			SeedDistributor aggregator = seed.seedDistributer(infoCollector);
			if (aggregator != null)
				aggregator.doneQueueing(sourceDocument);
		}
	}
	
	/**
	 * Connects to a SeedDistributor, when appropriate.
	 * 
	 * @return the Seed that initiated this, if it is seeding time, or null, if it is not.
	 */
	@Override
	public Seed getSeed()
	{
		return searchSeed;
	}

	public void incrementResultSoFar()
	{
		this.resultsSoFar++;
	}

	public int getResultSoFar()
	{
		return this.resultsSoFar;
	}

	public int getResultNum()
	{
		return resultsSoFar + searchSeed.currentFirstResultIndex();
	}

	

}
