package ecologylab.bigsemantics.documentparsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.html.AElement;
import ecologylab.bigsemantics.html.DOMParserInterface;
import ecologylab.bigsemantics.html.DOMWalkInformationTagger;
import ecologylab.bigsemantics.html.ImgElement;
import ecologylab.bigsemantics.html.ParagraphText;
import ecologylab.bigsemantics.html.documentstructure.AnchorContext;
import ecologylab.bigsemantics.html.documentstructure.ContentPage;
import ecologylab.bigsemantics.html.documentstructure.ImageCollectionPage;
import ecologylab.bigsemantics.html.documentstructure.ImageFeatures;
import ecologylab.bigsemantics.html.documentstructure.IndexPage;
import ecologylab.bigsemantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.bigsemantics.html.documentstructure.TextOnlyPage;
import ecologylab.bigsemantics.html.utils.HTMLNames;
import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.bigsemantics.metadata.builtins.CompoundDocument;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.XMLTools;


/**
 * Parse HTML, create DOM, and author Image and Text surrogates from DOM
 * 
 * @author eunyee
 *
 */
public class HTMLDOMImageTextParser
extends ParserBase<CompoundDocument>
implements DOMParserInterface, HTMLNames
{
	public HTMLDOMImageTextParser(SemanticsGlobalScope infoCollector)	// this is of type In
	{
		super(infoCollector);
	}
	HashMap<Node, String> tdNodeAnchorContextStringCache;
	
	DOMWalkInformationTagger taggedDoc;
	
	@Override
	public Document populateMetadata(Document document, MetaMetadataCompositeField metaMetadata,
			org.w3c.dom.Document DOM, SemanticActionHandler handler)
	{
		recursiveExtraction(metaMetadata, document, DOM, null, handler.getSemanticActionVariableMap());
		return document;
	}
	
	@Override
	public void parse() throws IOException
	{
		DOMWalkInformationTagger taggedDoc = new DOMWalkInformationTagger(documentClosure.getDocument().getLocation(), this);
		// this function actually traverse the dom tree
		org.w3c.dom.Document dom = getDom();
		
		long t0 = System.currentTimeMillis();
    taggedDoc.generateCollections(dom);
		
		Node contentBody = getContentBody(taggedDoc);
		DOMWalkInformationTagger taggedContentNode = walkAndTagDom(contentBody, this);
		
		extractImageTextSurrogates(taggedDoc, contentBody);
		
		
		//The information on whether the anchorContexts where produced by the document entirely or the ContentNode.
		ArrayList<AnchorContext> anchorContexts = null;
		//This document's purl
		ParsedURL purl = purl();
		boolean fromContentBody = taggedContentNode != null;
		if (fromContentBody)
			anchorContexts = buildAnchorContexts(taggedContentNode.getAllAnchorNodes(), purl, true);
		else
			anchorContexts = buildAnchorContexts(taggedDoc.getAllAnchorNodes(), purl, false);
		
  	generateCandidateContainersFromContexts(anchorContexts, fromContentBody);
  	
  	anchorContexts.clear();
		taggedDoc.recycle();
		taggedDoc	= null;
		
		if (fromContentBody)
			taggedContentNode.recycle();
		getLogRecord().setMsContentBodyAndClippings(System.currentTimeMillis() - t0);
		
		MetaMetadata metaMetadata	= (MetaMetadata) getMetaMetadata();
		if (metaMetadata.getSemanticActions() != null || metaMetadata.hasChildren())
		{
		  t0 = System.currentTimeMillis();
			super.parse();
			getLogRecord().setMsImageTextParserCallingSuperParse(System.currentTimeMillis() - t0);
		}
	}

	
	/**
	 * This is the walk of the dom that calls print tree, and the parser methods such as closeHref etc.
	 * @param doc
	 * @param htmlType
	 * @return
	 */
	public DOMWalkInformationTagger walkAndTagDom(Node contentBody, DOMParserInterface htmlType)
	{
		// note that content body could be null if it is not a content page
		if (contentBody == null)
			return null;
		
		DOMWalkInformationTagger domTagger = new DOMWalkInformationTagger(documentClosure.getDocument().getLocation(), htmlType);
		domTagger.generateCollectionsFromRoot(contentBody);
		// walk through the HTML document object.
		// gather all paragraphText and image objects in the data structure.
		//FIXME -- get rid of this call and object!
//		domTagger.printTree(jtidyPrettyOutput, (short)0, 0, null, tdNode);
//		domTagger.flushLine(jtidyPrettyOutput, 0);
		return domTagger;
	}

	/**
	 * Extract Image and Text Surrogates while walk through DOM
	 * 
	 * historically was called as pprint() in JTidy. 
	 */
	public void extractImageTextSurrogates(DOMWalkInformationTagger taggedDoc, Node contentBody)
	{

		//System.out.println("\n\ncontentBody = " + contentBody);       
		ArrayList<ImgElement> imgNodes = taggedDoc.getAllImgNodes();

		recognizeDocumentStructureToGenerateSurrogate(taggedDoc, contentBody, imgNodes);
	}

	/**
	 * @param taggedDoc
	 * @return
	 */
	private Node getContentBody(DOMWalkInformationTagger taggedDoc)
	{
		Node contentBody = RecognizedDocumentStructure.recognizeContentBody(taggedDoc);
		return contentBody;
	}
	
	/**
	 * Recognize the page type based on whether the page has contentBody node or not, 
	 * text length in the whole page, and whether the informative images reside in the page. 
	 * 
	 * Based on the recognized page type, it generates surrogates.  
	 * @param domWalkInfoTagger
	 * @param contentBody
	 * @param imgNodes
	 */
	private void recognizeDocumentStructureToGenerateSurrogate(DOMWalkInformationTagger domWalkInfoTagger,
			Node contentBody, ArrayList<ImgElement> imgNodes) 
	{
		RecognizedDocumentStructure pageCategory = null;

		if ((contentBody!=null) && (contentBody.getParentNode()!=null) /*&& (!articleMain.parent().equals(document))*/ )
		{
			pageCategory = new ContentPage(purl());
		}
		else
		{
			final int numImgNodes = imgNodes.size();
			if( (numImgNodes>0) && ((domWalkInfoTagger.getTotalTxtLength()/numImgNodes)<200) )
			{	
				// High probability to be an image-collection page
				pageCategory = new ImageCollectionPage(purl());
			}
			else if ( numImgNodes!=0 )
			{
				// Index Pages (include index-content pages)
				//FIXME -- should also look at text only pages & especially use link ratio as a feature!!!!
				pageCategory = new IndexPage(purl());
			}
		}
		TreeMap<Integer, ParagraphText> paragraphTextsTMap = domWalkInfoTagger.getParagraphTextsTMap();

		if (pageCategory != null)
		{
			pageCategory.generateSurrogates(contentBody, imgNodes, domWalkInfoTagger.getTotalTxtLength(), paragraphTextsTMap, this);
		}

		// No Informative images are in this document. Form surrogate only with text.  	
		// We cannot tell whether the images in the pages are informative or not until downloading all, thus this is the case after we 
		// look through all the images in the page and determine no image is worth displaying.
		if( (numExtractedClippings()==0) && (paragraphTextsTMap.size()>0) )
		{
			pageCategory = new TextOnlyPage(purl());
			pageCategory.generateSurrogates(contentBody, imgNodes, domWalkInfoTagger.getTotalTxtLength(), paragraphTextsTMap, this);
		}
		if (pageCategory != null)
			setRecognizedDocumentStructure(pageCategory.getClass());
	}


	/**
	 * Transform an set of AElements (HTML a) into a set of AnchorContexts.
	 * In some cases, an AElement may result in no entry, because the anchor text and anchor context are both empty.
	 * @param anchorElements
	 * @param sourcePurl The purl from which these AElements were extracted from.
	 * 
	 * @return
	 */
	public ArrayList<AnchorContext> buildAnchorContexts(ArrayList<AElement> anchorElements, ParsedURL sourcePurl, boolean fromContentBody )
	{
		ArrayList<AnchorContext> anchorNodeContexts = new ArrayList<AnchorContext>();
		
		for (AElement aElement : anchorElements)
		{
			AnchorContext aContext= constructAnchorContext(aElement, sourcePurl, fromContentBody);
			if(aContext!=null)
					anchorNodeContexts.add(aContext);
		}
		return anchorNodeContexts;
	}
	

	
	/**
	 * Given the a element from the HTML, get its anchor text (text between open and close a tags),
	 * and its anchor context (surrounding text). If either of these is not null, then return an
	 * AnchorContext object.
	 * 
	 * The surrounding text is defined as all the text in the a element's parent node.
	 * This definition should perhaps be expanded, for example, by trying grandparent if parent
	 * is either null or the same as anchor text.
	 * 
	 * @param aElement	Anchor HTMLElement (a href=...)
	 * @param fromContentBody 
	 * 
	 * @return					AnchorContext object, or null.
	 */
	public AnchorContext constructAnchorContext(AElement aElement, ParsedURL sourcePurl, boolean fromContentBody)
	{
		Node anchorNodeNode 				  = aElement.getNode();
		ParsedURL href 									= aElement.getHref();
		if (href != null)
		{
			//Cache TdNode-AnchorContext getTextInSubTree.
			Node parent 							  = anchorNodeNode.getParentNode();
			//FIXME -- this routine drops all sorts of significant stuff because it does not concatenate across tags.
			StringBuilder anchorContext = null;
			
			if(tdNodeAnchorContextStringCache == null)
				tdNodeAnchorContextStringCache = new HashMap<Node, String>();

			String anchorContextString	= tdNodeAnchorContextStringCache.get(parent);
			if(anchorContextString == null)
			{
				anchorContext = getTextInSubTree(parent, false);				
				if (anchorContext != null)
				{
					anchorContextString = StringTools.unescapeAndLowerCaseStringBuilder(anchorContext);
					StringBuilderUtils.release(anchorContext);
					tdNodeAnchorContextStringCache.put(parent, anchorContextString);
				}
			}

			//TODO: provide ability to specify alternate anchorContext
			StringBuilder anchorText 			= getTextInSubTree(anchorNodeNode, true);
			if ((anchorContextString != null) || (anchorText != null))
			{

				String anchorTextString			= null;
				if (anchorText != null)
				{
					anchorTextString = StringTools.unescapeAndLowerCaseStringBuilder(anchorText);
					StringBuilderUtils.release(anchorText);
				}
				return new AnchorContext(href, anchorTextString, anchorContextString, sourcePurl, fromContentBody, false);
			}
		}
		return null;
	}


	
  public static StringBuilder getTextInSubTree(Node node, boolean recurse)
  {
  	return getTextinSubTree(node, recurse, null);
  }

	/**
   * Non-recursive method to get the text for the <code>node</code>
   * Collects the text even if the node contains other nodes in between,
   * specifically the <code>anchor</code>. It does not however include the 
   * text from the anchor node, as it exists in the anchorText
   * @param node
   * @param te
   * @return
   */
  public static StringBuilder getTextinSubTree(Node node, boolean recurse, StringBuilder result)
  {
  	NodeList children = node.getChildNodes();
  	for (int i=0; i<children.getLength(); i++)
  	{
  		Node childNode = children.item(i);
			if (recurse && (childNode.getNodeName()!=null) && (!childNode.getNodeName().equals("script")))
			{
				//Recursive call with the childNode
				result = getTextinSubTree(childNode, true, result);
			}	
			else if (childNode.getNodeType() == Node.TEXT_NODE )
  		{
  			int length	= 0;
				if (result != null)
				{
					result.append(' ');							// append space to separate text chunks
					length		= result.length();
				}
  			result			= StringBuilderUtils.trimAndDecodeUTF8(result, childNode, 0, true);
  			
  			if ((result != null) && (length == result.length()) && (length > 0))
  					result.setLength(length - 1);	// take the space off if nothing was appended
  		} 
  		else if ("img".equals(childNode.getNodeName()))
  		{
  			Node altAtt	= childNode.getAttributes().getNamedItem(ALT);
  			String alt		= (altAtt != null) ? altAtt.getNodeValue() : null;
  			if (!ImageFeatures.altIsBogus(alt))
  			{
  				if (result == null)
  					result		= StringBuilderUtils.acquire();
  				else
  					result.append(' ');
  				result.append(alt);
  			}
  		}
  	}
  	if (result != null)
  		XMLTools.unescapeXML(result);

  	return result;
  }
  
	@Override
	public synchronized void recycle()
	{
		if (this.tdNodeAnchorContextStringCache != null)
		{
			this.tdNodeAnchorContextStringCache.clear();
			this.tdNodeAnchorContextStringCache = null;
		}
		if (taggedDoc != null)
		{
			taggedDoc.recycle();
			taggedDoc	= null;
		}
		super.recycle();
	}
}