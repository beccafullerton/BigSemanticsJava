/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.collections.WeightSet;
import ecologylab.generic.Debug;
import ecologylab.semantics.collecting.ContainerWeightingStrategy;
import ecologylab.semantics.collecting.Crawler;
import ecologylab.semantics.collecting.DownloadStatus;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.metadata.builtins.Clipping;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.model.text.InterestModel;

/**
 * The per CompoundDocument component of the basic Crawler, involving outlinks, but not ImageClippings or TextClippings.
 * 
 * @author andruid
 */
public class CompoundDocumentParserCrawlerResult<CR extends Crawler> extends Debug 
implements ParserResult
{
	protected CompoundDocument								compoundDocument;
	
	protected final SemanticsSessionScope			semanticsSessionScope;
	
	protected final CR												crawler;

	private WeightSet<DocumentClosure>	candidateLocalOutlinks;
	
	boolean															crawlingOutlinks;
	
	protected static final double				MIN_WEIGHT_THRESHOLD	= 0.;
	
	protected boolean										useFirstCandidateWeight	= true;
	
	protected boolean										recycled;
	
	/** Number of surrogates from this container in a candidate pool */
	protected int												numSurrogatesFrom = 0;
	
	
	public CompoundDocumentParserCrawlerResult(CompoundDocument	compoundDocument)
	{
		this.compoundDocument				= compoundDocument;
		this.semanticsSessionScope	= compoundDocument.getSemanticsSessionScope();
		this.crawler								= (CR) semanticsSessionScope.getCrawler();
	}

	//////////////////////////////////////// candidates loops state ////////////////////////////////////////////////////////////
	public void addCandidateOutlink (Document newOutlink )
	{
		if (!newOutlink.isSeed() && !newOutlink.isDownloadDone())	// a seed is never a candidate
		{
			DocumentClosure documentClosure	= newOutlink.getOrConstructClosure();
			if (documentClosure != null && documentClosure.getDownloadStatus() == DownloadStatus.UNPROCESSED)
			{
				if (candidateLocalOutlinks == null)
					candidateLocalOutlinks			=  new WeightSet<DocumentClosure>(new ContainerWeightingStrategy(InterestModel.getPIV()));
				candidateLocalOutlinks.insert(documentClosure);
			}
		}
	}
	
	protected int clippingPoolPriority()
	{
		int result = useFirstCandidateWeight ? (compoundDocument.isSeed() ? 0 : 1) : 2;
		useFirstCandidateWeight		= false;
				
		return result;
	}
	/**
	 * 
	 * 1. First, only one surrogate goes to candidate pool. 
	 * 2. Good looking surrogates, number of surrogates from current container, and users' interest 
	 *    expression will determine to bring more surrogates from current container to the candidate pool.
	 * @param getText 
	 */
	protected synchronized void perhapsAddOutlinkClosureToCrawler ( )
	{
		if (candidateLocalOutlinks == null || candidateLocalOutlinks.size() == 0)
		{
			makeInactiveAndConsiderRecycling();
			return;
		}
		
		double maxWeight = candidateLocalOutlinks.maxWeight();
		boolean doRecycle = true;
		if (maxWeight > MIN_WEIGHT_THRESHOLD)
		{
			DocumentClosure candidate = candidateLocalOutlinks.maxSelect();
			doRecycle = !crawler.addClosureToPool(candidate); // successful add means do not recycle
		}
		else
		{
			//Debug only
			debug("This container failed to provide a decent container so is going bye bye, max weight was " + maxWeight );
		}
			
		if (doRecycle)
			makeInactiveAndConsiderRecycling();
	}
	
	
	private void makeInactiveAndConsiderRecycling()
	{
		crawlingOutlinks = false;
		recycle();
	}

	private void considerRecycling()
	{
		if (isActive())
			recycle();
		else
			debug("DIDNT RECYCLE AFTER CONSIDERATION.\nCONTAINERS_ACTIVE: " 
					+ crawlingOutlinks
				/*	+ "\tTEXT_SURROGATES_ACTIVE: "
					+ additionalTextSurrogatesActive
					+ "\tIMAGE_SURROGATES_ACTIVE: "
					+ additionalImgSurrogatesActive */);
	}
	
	/**
	 * Test for recycleable.
	 * 
	 * @return	true if this is still involved in collecting.
	 */
	protected boolean isActive()
	{
		return crawlingOutlinks;
	}

	public void collect()
	{
		for (Clipping clipping: compoundDocument.getClippings())
		{
			collect(clipping);
		}
		
		initiateCollecting();
	}

	/**
	 * Start up collecting loops -- DocumentClosures only.
	 */
	protected void initiateCollecting()
	{
		crawlingOutlinks	= true;
		perhapsAddOutlinkClosureToCrawler();
	}
	
	protected void collect(Clipping clipping)
	{
		// try collecting the outlink
		DocumentClosure outlinkClosure	= clipping.getOutlinkClosure();
		if (outlinkClosure == null)
		{
			Document outlink							= clipping.getOutlink();
			if (outlink != null)
				outlinkClosure							= outlink.getOrConstructClosure();
		}
		if (outlinkClosure != null && semanticsSessionScope.isLocationNew(outlinkClosure.location()))
			crawler.addClosureToPool(outlinkClosure);
	}
	
	protected boolean isEmpty()
	{
		return outlinksIsEmpty();
	}

	protected boolean outlinksIsEmpty()
	{
		return ((candidateLocalOutlinks == null) || (candidateLocalOutlinks.size() == 0));
	}

	public DocumentClosure swapNextBestOutlinkWith(DocumentClosure c)
	{
		
		if (outlinksIsEmpty())
			return null;
		synchronized (candidateLocalOutlinks)
		{
			candidateLocalOutlinks.insert(c);
			return candidateLocalOutlinks.maxSelect();
		}
	}
	
	public boolean isRecycled()
	{
		return recycled;
	}

	@Override
	public synchronized void recycle()
	{
		if (!recycled)
		{
			recycled									= true;
			
			//FIXME -- IMPLEMENT RECYCLE!!!
		}
	}


}
