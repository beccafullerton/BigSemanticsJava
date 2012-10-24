/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import ecologylab.collections.SetElement;
import ecologylab.concurrent.Downloadable;
import ecologylab.generic.Continuation;
import ecologylab.io.DownloadProcessor;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.collecting.DocumentDownloadedEventHandler;
import ecologylab.semantics.collecting.DownloadStatus;
import ecologylab.semantics.collecting.SemanticsDownloadMonitors;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.collecting.SemanticsSite;
import ecologylab.semantics.dbinterface.IDocumentCache;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.documentparsers.ParserBase;
import ecologylab.semantics.downloaders.controllers.DefaultDownloadController;
import ecologylab.semantics.downloaders.controllers.DownloadController;
import ecologylab.semantics.downloaders.controllers.DownloadControllerType;
import ecologylab.semantics.downloaders.controllers.OODSSDownloadController;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVectorFeature;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.serialization.library.geom.PointInt;

/**
 * New Container object. Mostly just a closure around Document.
 * Used as a candidate and wrapper for downloading.
 * 
 * @author andruid
 *
 */
public class DocumentClosure extends SetElement
implements TermVectorFeature, Downloadable, SemanticActionsKeyWords, Continuation<DocumentClosure>
{
	Document											document; 
	
	private PURLConnection				purlConnection;
	
	private DownloadController		downloadController;

	private DocumentParser				documentParser;
	
	/**
	 * This is tracked mainly for debugging, so we can see what pURL was fed into the meta-metadata address resolver machine.
	 */
	ParsedURL											initialPURL;

	SemanticInLinks								semanticInlinks;

	ArrayList<Continuation<DocumentClosure>> continuations;
	
	DownloadStatus								downloadStatus	= DownloadStatus.UNPROCESSED;
	
	PointInt											dndPoint;

	protected		SemanticsGlobalScope	semanticsScope;
	
	/**
	 * Keeps state about the search process, if this is encapsulates a search result;
	 */
	protected SearchResult	searchResult;


	/**
	 * If true (the normal case), then any MediaElements encountered will be added
	 * to the candidates collection, for possible inclusion in the visual information space.
	 */
	boolean							collectMedia	= true;
	/**
	 * If true (the normal case), then hyperlinks encounted will be fed to the
	 * web crawler, providing that they are traversable() and of the right mime types.
	 */
	boolean							crawlLinks		= true;
	
	private final Object DOWNLOAD_STATUS_LOCK			= new Object();
	private final Object DOCUMENT_LOCK						= new Object();   
	
	/**
	 * 
	 */
	private DocumentClosure(Document document, SemanticsGlobalScope semanticsSessionScope,
			SemanticInLinks semanticInlinks, DownloadControllerType downloadControllerType)
	{
		super();
		this.document = document;
		this.semanticsScope = semanticsSessionScope;
		this.semanticInlinks = semanticInlinks;
		this.continuations = new ArrayList<Continuation<DocumentClosure>>();

		if (downloadControllerType == DownloadControllerType.DEFAULT)
			this.downloadController = new DefaultDownloadController();
		else if (downloadControllerType == DownloadControllerType.OODSS)
			this.downloadController = new OODSSDownloadController();
	}

	/**
	 * Should only be called by Document.getOrCreateClosure().
	 * 
	 * @param document
	 * @param semanticInlinks
	 */
	DocumentClosure(Document document, SemanticInLinks semanticInlinks,
			DownloadControllerType downloadControllerType)
	{
		this(document, document.getSemanticsScope(), semanticInlinks, downloadControllerType);
	}

	/////////////////////// methods for downloadable //////////////////////////
	/**
	 * Called by DownloadMonitor to initiate download, and cleanup afterward.
	 * NO OTHER OBJECT SHOULD EVER CALL THIS METHOD!
	 * 
	 * Actually download the document and parse it.
	 * Connect to the purl. Figure out the appropriate Meta-Metadata and DocumentType.
	 * Process redirects as needed.
	 * @throws IOException 
	 */


	@Override
	public void performDownload()
	throws IOException
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			if (!(downloadStatus == DownloadStatus.QUEUED || downloadStatus == DownloadStatus.UNPROCESSED))
				return;
			setDownloadStatusInternal(DownloadStatus.CONNECTING);
		}
		if (semanticsScope == null)	// this should NEVER happen!!!!!!!!!!!!!!!!!!!!
		{
			SemanticsGlobalScope documentInfoCollector = document.getSemanticsScope();
			if (documentInfoCollector == null)
			{
				error("Cant downloadAndParse: InfoCollector= null.");
				return;
			}
			semanticsScope	= documentInfoCollector;
		}
		document.setSemanticsSessionScope(semanticsScope);
		
		if (recycled() || document.isRecycled())
		{
			println("ERROR: Trying to downloadAndParse() page that's already recycled -- "+ location());
			return;
		}
		
		//if the semantics scope provides DB lookup
		IDocumentCache dbProvider = semanticsScope.getDBDocumentProvider();
		if (dbProvider != null)
		{
			Document document = dbProvider.retrieveDocument(this);
			if (document != null)
			{
				changeDocument(document);
				return;
			}
		}
		
		ParsedURL location = location();
		// connect call also evolves Document based on redirects & mime-type;
		//the parameter is used to get and set document properties with request and response respectively
		downloadController.connect(this);  

		MetaMetadata metaMetadata = (MetaMetadata) document.getMetaMetadata();
		//check for more specific meta-metadata
		if (metaMetadata.isGenericMetadata())
		{ // see if we can find more specifc meta-metadata using mimeType
			MetaMetadataRepository repository = semanticsScope.getMetaMetadataRepository();
			MetaMetadata mimeMmd = repository.getMMByMime(purlConnection.mimeType());
			if (mimeMmd != null && !mimeMmd.equals(metaMetadata))
			{
			  // new meta-metadata!
				if (!mimeMmd.getMetadataClass().isAssignableFrom(document.getClass()))
				{
				  // more specifc so we need new metadata!
					Document document	= (Document) mimeMmd.constructMetadata(); // set temporary on stack
					changeDocument(document);
				}
				metaMetadata	= mimeMmd;
			}
		}
		
		//determine document parser	
		if (documentParser == null)
			documentParser = DocumentParser.get(metaMetadata, semanticsScope);
		if (documentParser != null)
		{
			documentParser.fillValues(purlConnection, this, semanticsScope);
		}
		else if (!DocumentParser.isRegisteredNoParser(purlConnection.getPurl()))
		{
			warning("No DocumentParser found: " + metaMetadata);
		}
		
		if (purlConnection.isGood() && documentParser != null)
		{
			// container or not (it could turn out to be an image or some other mime type), parse the baby!
			setDownloadStatusInternal(DownloadStatus.PARSING);
			if (documentParser.downloadingMessageOnConnect())
				semanticsScope.displayStatus("Downloading " + location(), 2);
			
			//MetaMetadata metaMetadata	= (MetaMetadata) document.getMetaMetadata();
			ArrayList<SemanticAction> beforeSemanticActions	= metaMetadata.getBeforeSemanticActions();
			if (beforeSemanticActions != null)
			{
				SemanticActionHandler handler	= new SemanticActionHandler(semanticsScope, documentParser);
				handler.takeSemanticActions(metaMetadata, document, beforeSemanticActions);
			}
			documentParser.parse();
			
			ArrayList<SemanticAction> afterSemanticActions	= metaMetadata.getAfterSemanticActions();
			if (afterSemanticActions != null)
			{
				SemanticActionHandler handler	= new SemanticActionHandler(semanticsScope, documentParser);
				handler.takeSemanticActions(metaMetadata, document, afterSemanticActions);
			}
			
			setDownloadStatusInternal(DownloadStatus.DOWNLOAD_DONE);
			if (!ParserBase.DONOT_SETUP_DOCUMENT_GRAPH_CALLBACKS)
			{
				Set<DocumentDownloadedEventHandler> listeners = semanticsScope.getDocumentDownloadingMonitor().getListenersForDocument(document);
				if (listeners != null && listeners.size() > 0)
					addContinuations(listeners);
			}
			
			//store document in DB
			if (dbProvider != null)
			{
				dbProvider.storeDocument(this.document);
			}
		}
		else
		{
			if (documentParser != null)
				warning("Error opening connection for " + location);
			recycle();
		}
		
		document.setDownloadDone(true);
		document.downloadAndParseDone(documentParser);
		
		purlConnection.recycle();
		purlConnection	= null;
	}
	
	/**
	 * Document metadata object must change, because we learned something new about its type.
	 * @param newDocument
	 */
	public void changeDocument(Document newDocument) 
	{
		synchronized (DOCUMENT_LOCK)
		{
			Document oldDocument	= document;
			this.document					= newDocument;
			
			SemanticsSite oldSite = oldDocument.site();
			SemanticsSite newSite = newDocument.site();
			if (oldSite != null && oldSite != newSite)
			{
			  // calling changeDocument() because of redirecting?
			  if (oldSite.isDownloading())
  			  oldSite.endDownload();
			}
			
			newDocument.inheritValues(oldDocument);	
			
//			SimplTypesScope.serializeOut(newDocument, "After changeDocument()", StringFormat.XML);
			
			semanticInlinks				= newDocument.semanticInlinks; // probably not needed, but just in case.
			oldDocument.recycle();
		}
	}

	public ParsedURL getInitialPURL()
	{
		return initialPURL;
	}

	@Override
	public SemanticsSite getSite()
	{
		Document document = this.document;
		return (document == null) ? null : document.getSite();
	}
	
	@Override
	public SemanticsSite getDownloadSite()
	{
		Document document = this.document;
		if(document != null)
		{
			if(document.getDownloadLocation().isFile())
			 return null;
		}
		return (document == null) ? null : document.getSite();
	}
	
	public boolean isFromSite(SemanticsSite site)
	{
		return site != null && site == getSite();
	}
	
	@Override
	public void recycle()
	{
		recycle(false);
	}
	@Override
	public synchronized void recycle(boolean recycleDocument)
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			if (downloadStatus == DownloadStatus.RECYCLED)
				return;
			setDownloadStatusInternal(DownloadStatus.RECYCLED);
		}
		
		if (documentParser != null)
			documentParser.recycle();

		if (purlConnection != null)
			purlConnection.recycle();

		semanticInlinks	= null;

		initialPURL			= null;
		
//		if (continuations != null)
//			continuations.clear();	
//		continuations		= null;
			
		//??? should we recycle Document here -- under what circumstances???
		if (recycleDocument)
			document.recycle();
	}
	@Override
	public boolean recycled()
	{
		Document document = this.document;
		return document == null || document.isRecycled();
	}
	
	@Override
	public boolean isRecycled()
	{
		return document == null || document.isRecycled();
	}


	@Override
	public ParsedURL location()
	{
		Document document = this.document;
		return (document == null) ? null : document.getLocation();
	}
	
	@Override
	public ParsedURL getDownloadLocation()
	{
		Document document = this.document;
		return (document == null) ? null : document.getDownloadLocation();
	}

	/**
	 * @return the semanticInlinks
	 */
	public SemanticInLinks getSemanticInlinks()
	{
		return semanticInlinks;
	}

	/**
	 * @return the document
	 */
	public Document getDocument()
	{
		synchronized (DOCUMENT_LOCK)
		{
			return document;
		}
	}

	/**
	 * Download if necessary, using the 
	 * {@link ecologylab.concurrent.DownloadMonitor DownloadMonitor} if USE_DOWNLOAD_MONITOR
	 * is set (it seems it always is), or in a new thread.
	 * Control will be passed to {@link #downloadAndParse() downloadAndParse()}.
	 * Does nothing if this has been previously queued, if it has been recycled, or if it isMuted().
	 * 
	 * @return	true if this is actually queued for download. false if it was previously, if its been recycled, or if it is muted.
	 */
	public boolean queueDownload()
	{
		if (recycled())
		{
			debugA("ERROR: cant queue download cause already recycled.");
			return false;
		}
		if (this.getDownloadLocation() == null)
			return false;
		final boolean result = !filteredOut(); // for dashboard type on the fly filtering
		if (result)
		{
			if (!testAndSetQueueDownload())
				return false;	
			delete();				// remove from candidate pools! (invokes deleteHook as well)  
			
			downloadMonitor().download(this, continuations == null ? null : this);
		}
		return result;
	}
	/**
	 * Test and set state variable inside of QUEUE_DOWNLOAD_LOCK.
	 * 
	 * @return		true if this really queues the download, and false if it had already been queued.
	 */
	private boolean testAndSetQueueDownload()
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			if (downloadStatus != DownloadStatus.UNPROCESSED)
				return false;
			setDownloadStatusInternal(DownloadStatus.QUEUED);
			return true;
		}
	}
	/**
	 * Test state variable inside of QUEUE_DOWNLOAD_LOCK.
	 * 
	 * @return true if result has already been queued, connected to, downloaded, ... so it should not be operated on further.
	 */
	public boolean downloadHasBeenQueued()
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			return downloadStatus != DownloadStatus.UNPROCESSED;
		}
	}

	public DownloadProcessor<DocumentClosure> downloadMonitor()
	{
		SemanticsDownloadMonitors downloadMonitors = semanticsScope.getDownloadMonitors();
    return downloadMonitors.downloadProcessor(document.isImage(), isDnd(), isSeed(), document.isGui());
	}

	
	@Override
	public int hashCode()
	{
		return (document == null) ? -1 : document.hashCode();
	}

	public boolean isSeed()
	{
		return (document != null) && document.isSeed();
	}

	@Override
	public ITermVector termVector()
	{
		return (document == null) ? null : document.termVector();
	}

	/**
	 * Called by DownloadMonitor in case a timeout happens.
	 */
	@Override
	public void handleIoError(Throwable e)
	{
		setDownloadStatusInternal(DownloadStatus.IOERROR);
		document.setDownloadDone(true);
		if (documentParser != null)
			documentParser.handleIoError(e);

		recycle();
	}

	@Override
	public String message()
	{
		return document == null ? "recycled" : document.getLocation().toString();
	}

	/**
	 * Keeps state about the search process, if this Container is a search result;
	 */
	public SearchResult searchResult()
	{
		return searchResult;
	}
	/**
	 * 
	 * @param resultDistributer
	 * @param searchNum			Index into the total number of (seeding) searches specified and being aggregated.
	 * @param resultNum		Result number among those returned by google.
	 */
	public void setSearchResult(SeedDistributor resultDistributer, int resultNum)
	{
		searchResult	= new SearchResult(resultDistributer, resultNum);
	}
	public SeedDistributor resultDistributer()
	{
		return (searchResult == null) ? null : searchResult.resultDistributer();
	}


	private ArrayList<Continuation<DocumentClosure>> continuations()
	{
		return continuations;
	}
	
	public void addContinuation(Continuation<DocumentClosure> continuation)
	{
		synchronized (continuations)
		{
			continuations().add(continuation);
		}
	}
	public void addContinuationBefore(Continuation<DocumentClosure> continuation)
	{
		synchronized (continuations)
		{
			continuations().add(0, continuation);
		}
	}	

	public void addContinuations(Collection<? extends Continuation<DocumentClosure>> incomingContinuations)
	{
		synchronized (continuations)
		{
			ArrayList<Continuation<DocumentClosure>> continuations = continuations();
			for (Continuation<DocumentClosure> continuation: incomingContinuations)
				continuations.add(continuation);
		}
	}

	public ArrayList<Continuation<DocumentClosure>> getContinuations()
	{
		return continuations;
	}

	public Seed getSeed()
	{
		return document != null ? document.getSeed() : null;
	}

	/**
	 * @return the downloadStatus
	 */
	public DownloadStatus getDownloadStatus()
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			return downloadStatus;
		}
	}
	
	/**
	 * (this method does not lock DOWNLOAD_STATUS_LOCK!)
	 * 
	 * @param newStatus
	 */
	private void setDownloadStatusInternal(DownloadStatus newStatus)
	{
		this.downloadStatus = newStatus;
		if (this.document != null)
			document.setDownloadStatus(newStatus);
	}

	public boolean isUnprocessed()
	{
		return getDownloadStatus() == DownloadStatus.UNPROCESSED;
	}

	/**
	 * @param presetDocumentParser the presetDocumentParser to set
	 */
	public void setDocumentParser(DocumentParser presetDocumentParser)
	{
		this.documentParser = presetDocumentParser;
	}

	@Override
	public String toString()
	{
		return super.toString() + "[" + document.getLocation() + "]";
	}
	public void serialize(OutputStream stream)
	{
		serialize(stream, StringFormat.XML);
	}
	public void serialize(OutputStream stream, StringFormat format)
	{
		Document document	= getDocument();
		try
		{
			SimplTypesScope.serialize(document, System.out, format);
			
			System.out.println("\n");
		}
		catch (SIMPLTranslationException e)
		{
			error("Could not serialize " + document);
			e.printStackTrace();
		}
	}
	public void serialize(StringBuilder buffy)
	{
		Document document	= getDocument();
		try
		{			
			SimplTypesScope.serialize(document, buffy, StringFormat.XML);
			System.out.println("\n");
		}
		catch (SIMPLTranslationException e)
		{
			error("Could not serialize " + document);
			e.printStackTrace();
		}
	}

	/**
	 * @return the infoCollector
	 */
	public SemanticsGlobalScope getSemanticsScope()
	{
		return semanticsScope;
	}

	/**
	 * Dispatch all of our registered callbacks.
	 */
	@Override
	public void callback(DocumentClosure o)
	{
		// FIXME callback() could be called from different DownloadMonitor threads! and this is causing
		// concurrent modification problems for this.continuations collection in some cases.
		// we may need a synchronization mechanism here, but should look out for performance.
		if (continuations == null)
			return;
		
		List<Continuation<DocumentClosure>> currentContinuations;
		synchronized (continuations)
		{
			currentContinuations = new ArrayList<Continuation<DocumentClosure>>(continuations);
		}
		if (currentContinuations != null)
		{
			for (Continuation<DocumentClosure> continuation: currentContinuations)
			{
				try
				{
				  continuation.callback(o);
				}
				catch (Exception e)
				{
					error("EXCEPTION INVOKING CALLBACK ON " + o + ": " + continuation);
					e.printStackTrace();
				}
			}
		}
		
		// wait to recycle continuations until after they have been called.
		if (isRecycled())
		{
			continuations.clear();	
			continuations		= null;
		}
	}
	
	public void setPurlConnection(PURLConnection purlConnection) {
		this.purlConnection = purlConnection;
	}

	/**
	 * Close the current connection.
	 * Re-open a connection to the same location.
	 * Use the same Document object; don't process re-directs, or anything like that.
	 * Re-connect simply.
	 * 
	 * @return	PURLConnection for the new connection.
	 */
	public PURLConnection reConnect()
	{
		purlConnection.close();
		purlConnection.recycle();
		purlConnection	= document.getLocation().connect(document.getMetaMetadata().getUserAgentString());
		return purlConnection;
	}

	@Override
	public boolean isImage()
	{
		return document.isImage();
	}
	
	public PointInt getDndPoint()
	{
		return dndPoint;
	}

	public void setDndPoint(PointInt dndPoint)
	{
		this.dndPoint = dndPoint;
	}
	
	public boolean isDnd()
	{
		return dndPoint != null;
	}
	
	public DocumentParser getParser()
	{
		return documentParser;
	}

	/**
	 * This method is called before we actually hit the website. Thus, it uses the initial URL to
	 * test if we need to hit the website. If it returns true, we definitely don't need to hit the
	 * website; if it returns false, we need to hit the website, but the actual document might have
	 * been cached using another URL.
	 */
  @Override
  public boolean isCached()
  {
    if (document == null || document.getLocation() == null)
      return false;
    return this.downloadController.isCached(document.getLocation());
  }
	
}
