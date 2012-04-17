package ecologylab.semantics.documentparsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.collecting.SemanticsSite;
import ecologylab.semantics.html.DOMParserInterface;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.html.documentstructure.LinkType;
import ecologylab.semantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.dom.IDOMProvider;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.ImageClipping;
import ecologylab.semantics.metadata.builtins.TextClipping;
import ecologylab.semantics.model.text.utils.Filter;
import ecologylab.semantics.seeding.SemanticsPrefs;
import ecologylab.serialization.XMLTools;

/**
 * Parse HTML page and create DOM
 * 
 * @author eunyee
 * 
 */
public abstract class HTMLDOMParser<D extends Document> extends ContainerParser<D> 
implements DOMParserInterface
{

	/**
	 * Root DOM of the current document
	 */
	private org.w3c.dom.Document	dom;

	protected IDOMProvider				provider;

	
	boolean indexPage = false;
	boolean contentPage = false;

	private	boolean 	isFile = false;

	protected boolean 	bold;
	protected boolean 	italic;

	/** <code>Filter</code> that recognizes junk images from URL */
	public static final Filter 		filter			= new Filter();	// url filtering

	public HTMLDOMParser(SemanticsGlobalScope infoCollector)
	{
		super(infoCollector);
	}


	public org.w3c.dom.Document getDom() throws IOException
	{
		org.w3c.dom.Document result = this.dom;
		if (result == null)
		{
			result = createDom();
			this.dom = result;
		}
		return result;
	}

	/**
	 * 
	 * @return The DOM provided by the input stream or a bogus DOM with a root node empty
	 * @throws IOException 
	 */
	private org.w3c.dom.Document createDom() throws IOException
	{
		provider											= semanticsScope.constructDOMProvider();
    InputStream inputStream = inputStream();
    Reader reader						= reader();
		org.w3c.dom.Document document = reader != null ? provider.parseDOM(reader, null) : provider.parseDOM(inputStream, null);
    return document;
	}

	/**
	 * 
	 * @return The root node of the document, which should be <html>.
	 * @throws IOException 
	 */
	public Node getRootNode() throws IOException
	{
		return getDom();
	}

	/**
	 * Andruid says: NEVER override this method when you parse HTML. Instead, override postParse().
	 * @throws IOException 
	 */
	@Override
	abstract public void parse() throws IOException;

	@Override
	public void recycle()
	{
		dom = null;
		provider = null;
		super.recycle();
	}

	/**
	 * Called when the parser see's the <code>&lt;title&gt; tag.
	 */
	public void setTitle(Node titleNode)
	{
		StringBuilder title = null;
		NodeList children = titleNode.getChildNodes();
		for (int i=0; i<children.getLength(); i++)
		{
			Node node = children.item(i);
			if (node.getNodeType() == Node.TEXT_NODE)
			{
				title = StringBuilderUtils.trimAndDecodeUTF8(title, node, 0, true);
				if (title != null)
				{
					XMLTools.unescapeXML(title);
					getDocument().hwSetTitle(StringTools.toString(title));
					StringBuilderUtils.release(title);
				}
				break;
			}
		}
	}

	/**
	 * Create TextElement and add to the localCollection in Container.
	 */
	public void constructTextClipping(ParagraphText paraText)
	{
		if ((paraText != null) && (paraText.length() > 0))
		{
			StringBuilder buffy	= paraText.getBuffy();
			if (buffy.indexOf("@") == -1 )	 // filter out paragraphs with email addresses
			{				
				TextClipping textClipping = new TextClipping(semanticsScope.getMetaMetadataRepository().getMMByName(semanticsScope.TEXT_TAG));
				textClipping.setText(StringTools.toString(buffy));
				textClipping.setSourceDoc(getDocument());
				getDocument().addClipping(textClipping);
			}
		}
	}

	public int numExtractedClippings()
	{
		return ((CompoundDocument) getDocument()).numClippings();
	}

	/**
	 * For each anchorContext: create purl and check to see Aggregates AnchorContext by their
	 * destination hrefs. sets the metadata creates a container adds an outlink from the ancestor
	 * 
	 */
	public void generateCandidateContainersFromContexts(ArrayList<AnchorContext> anchorContexts,
			boolean fromContentBody)
	{
		HashMapArrayList<ParsedURL, ArrayList<AnchorContext>> hashedAnchorContexts = new HashMapArrayList<ParsedURL, ArrayList<AnchorContext>>();
		for (AnchorContext anchorContext : anchorContexts)
		{

			ParsedURL destHref = anchorContext.getHref();
			if (destHref.isImg())
			{ // The href associated is actually an image. Create a new img element and associate text to
				// it.
				Image newImage					= semanticsScope.getOrConstructImage(destHref);
				newImage.constructClipping(getDocument(), null, null, anchorContext.getAnchorText());
				continue;
			}

			ArrayList<AnchorContext> arrayList = hashedAnchorContexts.get(destHref);
			if (arrayList == null)
			{
				arrayList = new ArrayList<AnchorContext>();
				hashedAnchorContexts.put(destHref, arrayList);
			}
			arrayList.add(anchorContext);
		}
		// Now that we have aggregated AnchorContext,
		// We generate One SemanticAnchor per purl, that aggregates all the semantics of the set of
		// anchorContexts
		for (ParsedURL hrefPurl : hashedAnchorContexts.keySet())
		{
			ArrayList<AnchorContext> anchorContextsPerHref = hashedAnchorContexts.get(hrefPurl);

			SemanticAnchor semanticAnchor = 
				new SemanticAnchor(fromContentBody ? LinkType.WILD_CONTENT_BODY : LinkType.WILD, hrefPurl, anchorContextsPerHref, purl(), 1);

			handleSemanticAnchor(semanticAnchor, hrefPurl);
		}
	}

	protected void handleSemanticAnchor(SemanticAnchor semanticAnchor, ParsedURL hrefPurl)
	{
		//FIXME -- should we depend on Seeding here?? or do this in post-processing for CompoundDocumentParserCrawlerResult??
		if (hrefPurl != null && !hrefPurl.isNull() && semanticsScope.accept(hrefPurl))
		{
			Document hrefDocument		= semanticsScope.getOrConstructDocument(hrefPurl);
			if (hrefDocument == null || hrefDocument.isRecycled())
			{
				warning("hrefDocument is null or recycled: " + hrefPurl);
				return; // Should actually raise an exception, but this could happen when a container is not
						// meant to be reincarnated
			}
			Document sourceDocument	= getDocument();
			
			hrefDocument.addSemanticInlink(semanticAnchor, sourceDocument);
			sourceDocument.addCandidateOutlink(hrefDocument);			
		}
	}

	static final PrefBoolean	SHOW_PAGE_STRUCTURE_PREF	= PrefBoolean.usePrefBoolean("show_page_structure", false);

	public void setRecognizedDocumentStructure(Class<? extends RecognizedDocumentStructure> pageType)
	{
		if (SHOW_PAGE_STRUCTURE_PREF.value())
		{
			CompoundDocument metadata = (CompoundDocument) getDocument();
			if (metadata != null)
				metadata.setPageStructure(pageType.getSimpleName());
			else
				error("Can't setPageStructure() cause NULL Metadata :-(");
		}
	}

	public static int	MAX_TEXT_CONTEXT_LENGTH	= 1500;

	/**
	 * trim the text context under the limit if it is too long.
	 * 
	 * @param textContext
	 * @return
	 */
	// FIXME -- call site for this should really be when we build contexts in walkAndTagDOM()
	public static String trimTooLongContext(String textContext)
	{
		if (textContext.length() > MAX_TEXT_CONTEXT_LENGTH)
			return textContext.substring(0, MAX_TEXT_CONTEXT_LENGTH);
		else
			return textContext;
	}
	@Override
	public void setContent ( )
	{
		contentPage = true;		
	}

	@Override
	public void setIndexPage ( )
	{
		indexPage = true;
	}

	@Override
	public boolean isIndexPage ( )
	{
		return indexPage;
	}

	@Override
	public boolean isContentPage ( )
	{
		return contentPage;
	}

	@Override
	public void removeTheContainerFromCandidates(ParsedURL containerPURL)
	{
		warning("Not Implemented: removeTheContainerFromCandidates(" + containerPURL);
	}

	/**
	 * Parser found a bold (or strong) tag or an end bold tag.
	 */
	public void setBold(boolean on) 
	{
		bold	= on;
	}

	/**
	 * Parser found an italic (or em) tag or an end italic tag.
	 */
	public void setItalic(boolean on) 
	{
		italic	= on;
	}

	protected ParsedURL buildAndFilterPurl(String urlString)
	{
		ParsedURL result	= buildPurl(urlString);
		return (result != null) && filterPurl(result) ? result : null;
	}
		
	/**
	 * Filters the parsedURL to check if 
	 * <li>infoCollector accepts
	 * <li>name does not start with File
	 * <li>is Crawlable
	 * <br>Do less checking if it's drag'n'drop (container==null)
	 * @param urlString
	 * @return
	 */
	protected boolean filterPurl(ParsedURL parsedURL)
	{
		Document document	= getDocument();
		//FIXME -- should we depend on Seeding here?? or do this in post-processing for CompoundDocumentParserCrawlerResult??
		return (parsedURL != null && 
				semanticsScope.accept(parsedURL) && 
				(!parsedURL.getName().startsWith("File:") && 
					parsedURL.crawlable() && !document.isJustCrawl() &&
					(document == null || parsedURL.isImg() || 
					(isFile && parsedURL.isHTML()) || !isFile)));
	}

	protected ParsedURL buildPurl(String urlString)
	{
		Document sourceDocument	= getDocument();
		return sourceDocument.isAnonymous() ? 
				ParsedURL.createFromHTML(null, urlString, false) :
			  sourceDocument.getLocation().createFromHTML(urlString, isSearchPage());
	}

	/**
	 * add an image+text surrogate for this that was extracted from a different document. FIXME this
	 * currently does the same thing as a surrogate extracted from this, but we might want to make a
	 * special collection for these "anchor surrogates".
	 * 
	 * Really, this should be setting the outlink somehow...
	 */
	public ImageClipping constructAnchorImageClipping(ImgElement imgNode, ParsedURL anchorHref)
	{
		CompoundDocument source	= (CompoundDocument)documentClosure.getDocument();
		ImageClipping clipping = constructImageClipping(getDocument(), source, null, imgNode);
		CompoundDocument outlink	= (CompoundDocument) semanticsScope.getOrConstructDocument(anchorHref);
		clipping.setOutlink(outlink);
		return clipping;
	}
	/**
	 * create image and text surrogates for this HTML document, and add these surrogates into the
	 * localCollection in Container.
	 */
	public ImageClipping constructImageClipping(ImgElement imgNode, ParsedURL anchorHref)
	{
		if(documentClosure.location().equals(anchorHref))
			debug("This should be something else here!!");
		debug("PART 1  "+anchorHref);
		Document outlink				= semanticsScope.getOrConstructDocument(anchorHref);
		Document sourceDocument = getDocument();
		return constructImageClipping(sourceDocument, sourceDocument, outlink, imgNode);
	}
	/**
	 * Construct an ImageClipping, associating it properly in the hypermedia graph.
	 * 
	 * @param basisDocument		The CompoundDocument to add the clipping to. 
	 * @param sourceDocument	The CompoundDocument to be listed as the Clipping's source. The one it is a surrogate for.
	 * 												Usually the same as basisDocument, but for a surrogate for X, found in Y, instead uses outlink here.
	 * @param outlink					The Document to be listed as the Clipping's href destination.
	 * @param imgNode					Representation of the source HTML + textContext and additional extractedCaption.
	 * 
	 * @return
	 */
	public ImageClipping constructImageClipping(Document basisDocument, Document sourceDocument, Document outlink, ImgElement imgNode)
	{
		ParsedURL srcPurl = imgNode.getSrc();

		ImageClipping result			= null;
		if (srcPurl != null)
		{
			int width			= imgNode.getWidth();
			int height		= imgNode.getHeight();
			int mimeIndex	= srcPurl.mediaMimeIndex();
			boolean isMap = imgNode.isMap();

			switch (ImageFeatures.designRole(width, height, mimeIndex, isMap))
			{
			case ImageFeatures.INFORMATIVE:
			case ImageFeatures.UNKNOWN:
				String alt	= imgNode.getAlt();

				if (alt != null)
					alt = alt.trim();
				
				Image image						= semanticsScope.getOrConstructImage(srcPurl);
				if (image == null)
					return null;
				image.setWidth(width);
				image.setHeight(height);
				
				result	= image.constructClipping(basisDocument, sourceDocument, outlink, alt, imgNode.getTextContext());
				result.setXpath(imgNode.xpath());
				break;
			case ImageFeatures.UN_INFORMATIVE:
			default:
				semanticsScope.getGlobalCollection().registerUninformativeImage(srcPurl);
			}
		}
		return result;
	}
	
	public static boolean isAd ( ParsedURL hrefPurl )
	{
		String lc			= hrefPurl.lc();
		boolean filterMatch	= SemanticsPrefs.FILTER_OUT_ADS.value() && filter.matchLc(lc);
		return filterMatch;
	}

	/**
	 * @return	true if <code>this</code> is a search page, and so needs
	 * special parsing of URLs, to unpack nested entries.
	 *		false in all other (usual) cases.
	 */
	public boolean isSearchPage() 
	{
		return false;
	}

	protected void findFaviconPath(Document doc, XPath xpath)
	{
		String favi_res = "";
		try
		{
			favi_res = xpath.evaluate("//link[@rel=\"shortcut icon\"]/@href", dom);
		}
		catch (XPathExpressionException e)
		{
			warning("Cannot find favicon path: " + e.getMessage() + "\n Stack trace:");
			e.printStackTrace();
		}
		
		if(favi_res != null && favi_res != "") {
			//Found one
			//System.out.println("Got a path: " + favi_res);
			SemanticsSite site = doc.getSite();
			site.setFaviconPath(favi_res, doc.getLocation());
			
		} else {
			//Did not. Look in the root.
		}
		
	}


}