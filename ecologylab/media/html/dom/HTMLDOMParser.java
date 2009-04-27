package ecologylab.media.html.dom;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.tidy.AttVal;
import org.w3c.tidy.DOMDocumentImpl;
import org.w3c.tidy.Lexer;
import org.w3c.tidy.Out;
import org.w3c.tidy.OutImpl;
import org.w3c.tidy.StreamIn;
import org.w3c.tidy.TdNode;
import org.w3c.tidy.Tidy;

import sun.io.ByteToCharASCII;

import ecologylab.generic.StringTools;
import ecologylab.media.html.dom.documentstructure.AnchorContext;
import ecologylab.media.html.dom.documentstructure.ContentPage;
import ecologylab.media.html.dom.documentstructure.ImageCollectionPage;
import ecologylab.media.html.dom.documentstructure.ImageFeatures;
import ecologylab.media.html.dom.documentstructure.IndexPage;
import ecologylab.media.html.dom.documentstructure.TextOnlyPage;
import ecologylab.media.html.dom.utils.HTMLAttributeNames;
import ecologylab.media.html.dom.utils.StringBuilderUtils;
import ecologylab.xml.XMLTools;


/**
 * Connect to JTidy parser to parse HTML pages for cF.
 * Bridge code between combinFormation and JTidy
 * This parsing code integrates with the Image-Text Surrogate extractor code.
 * 
 * @author eunyee
 *
 */
public class HTMLDOMParser extends Tidy
implements HTMLAttributeNames
{
	String url 			= "";

	/**
	 * because Tidy extends Serializable
	 */
	private static final long serialVersionUID = 1L;

	public HTMLDOMParser()
	{	
		super();
	}

	/**
	 * Parse HTML Document, and return the root DOM node
	 * 
	 * @param in
	 * @param out
	 * @param htmlType
	 */
	public org.w3c.dom.Document parse(InputStream in)
	{
		return parseDOM(in, null);
	}

	/**
	 * Extract Image and Text surrogates while walk through DOM 
	 * 
	 * @param in
	 * @param htmlType
	 */
	public void parse(InputStream in, TidyInterface htmlType, String url)
	{
		this.url = url;
		Document parsedDoc = parseDOM(in, null);

		if (!(parsedDoc instanceof DOMDocumentImpl)) 
		{
			//Does this ever happen anymore?
			return;
		}
		
		DOMWalkInformationTagger taggedDoc = walkAndTagDom(parsedDoc, htmlType);
		
		extractImageTextSurrogates(taggedDoc, htmlType);
		
		//Now, find hrefs, with their context and generate containers with metadata
		ArrayList<AnchorContext> anchorContexts = findHrefsAndContext(htmlType, taggedDoc.getAllAnchorNodes());
		
  	if(htmlType != null)
			htmlType.generateContainersFromContexts(anchorContexts);
  	
		taggedDoc.recycle();
	}

	/**
	 * Extract Image and Text Surrogates while walk through DOM
	 * 
	 * historically was called as pprint() in JTidy. 
	 */
	public void extractImageTextSurrogates(DOMWalkInformationTagger taggedDoc, TidyInterface htmlType)
	{

		TdNode contentBody = RecognizedDocumentStructure.recognizeContentBody(taggedDoc);
		//System.out.println("\n\ncontentBody = " + contentBody);       
		ArrayList<HTMLElement> imgNodes = taggedDoc.getAllImgNodes();

		recognizeDocumentStructureToGenerateSurrogate(htmlType, taggedDoc, contentBody, imgNodes);
	}

	/**
	 * This is the walk of the dom that calls print tree, and the parser methods such as closeHref etc.
	 * @param doc
	 * @param htmlType
	 * @return
	 */
	private DOMWalkInformationTagger walkAndTagDom(org.w3c.dom.Document doc, TidyInterface htmlType)
	{
		Out jtidyPrettyOutput = new OutImpl();
		TdNode rootTdNode;

		rootTdNode = ((DOMDocumentImpl)doc).adaptee;

		jtidyPrettyOutput.state = StreamIn.FSM_ASCII;
		jtidyPrettyOutput.encoding = configuration.CharEncoding;

		DOMWalkInformationTagger domTagger = new DOMWalkInformationTagger(configuration, htmlType);
		domTagger.state = StreamIn.FSM_ASCII;
		domTagger.encoding = configuration.CharEncoding;

		// walk through the HTML document object.
		// gather all paragraphText and image objects in the data structure.
		//FIXME -- get rid of this call and object!
		domTagger.printTree(jtidyPrettyOutput, (short)0, 0, null, rootTdNode);

		domTagger.flushLine(jtidyPrettyOutput, 0);
		return domTagger;
	}

	/**
	 * Recognize the page type based on whether the page has contentBody node or not, 
	 * text length in the whole page, and whether the informative images reside in the page. 
	 * 
	 * Based on the recognized page type, it generates surrogates.  
	 * 
	 * @param htmlType
	 * @param domWalkInfoTagger
	 * @param contentBody
	 * @param imgNodes
	 */
	private void recognizeDocumentStructureToGenerateSurrogate(TidyInterface htmlType,
			DOMWalkInformationTagger domWalkInfoTagger, TdNode contentBody,
			ArrayList<HTMLElement> imgNodes) 
	{
		RecognizedDocumentStructure pageCategory = null;

		if( contentBody!=null )
		{
			// Content Pages
			pageCategory = new ContentPage();
		}
		else
		{
			final int numImgNodes = imgNodes.size();
			if( (numImgNodes>0) && ((domWalkInfoTagger.getTotalTxtLength()/numImgNodes)<200) )
			{	
				// High probability to be an image-collection page
				pageCategory = new ImageCollectionPage();
			}
			else if( numImgNodes!=0 )
			{
				// Index Pages (include index-content pages)
				//FIXME -- should also look at text only pages & especially use link ratio as a feature!!!!
				pageCategory = new IndexPage();
			}
		}
		TreeMap<Integer, ParagraphText> paragraphTextsTMap = domWalkInfoTagger.getParagraphTextsTMap();

		if (pageCategory != null)
		{
			pageCategory.generateSurrogates(contentBody, imgNodes, domWalkInfoTagger.getTotalTxtLength(), paragraphTextsTMap, htmlType);
		}

		// No Informative images are in this document. Form surrogate only with text.  	
		// We cannot tell whether the images in the pages are informative or not until downloding all, thus this is the case after we 
		// look through all the images in the page and determine no image is worth displaying.
		if( (htmlType.numCandidatesExtractedFrom()==0) && (paragraphTextsTMap.size()>0) )
		{
			pageCategory = new TextOnlyPage();
			pageCategory.generateSurrogates(contentBody, imgNodes, domWalkInfoTagger.getTotalTxtLength(), paragraphTextsTMap, htmlType);
		}

	}

	/**
	 * @param arrayList 
	 * @param articleMain
	 */
	protected ArrayList<AnchorContext> findHrefsAndContext(TidyInterface htmlType, ArrayList<HTMLElement> anchorElements)
	{
		ArrayList<AnchorContext> anchorNodeContexts = new ArrayList<AnchorContext>();
		
		for(HTMLElement anchorElement : anchorElements)
		{
			TdNode anchorNodeNode 				  = anchorElement.getNode();
			String href 									  = anchorElement.getAttribute(HTMLAttributeNames.HREF);
			if ((href != null) && !href.startsWith("javascript:"))
			{
				TdNode parent 							  = anchorNodeNode.parent();
				//FIXME -- this routine drops all sorts of significant stuff because it does not concatenate across tags.
				StringBuilder anchorContext 	= getTextInSubTree(parent, false);
				StringBuilder anchorText 			= getTextInSubTree(anchorNodeNode, true);
				if ((anchorContext != null) || (anchorText != null))
				{
					String anchorContextString	= null;
					if (anchorContext != null)
					{
						XMLTools.unescapeXML(anchorContext);
						anchorContextString				= StringTools.toString(anchorContext);
						StringBuilderUtils.release(anchorContext);
					}
					String anchorTextString			= null;
					if (anchorText != null)
					{
						XMLTools.unescapeXML(anchorText);
						anchorTextString					= StringTools.toString(anchorText);
						StringBuilderUtils.release(anchorText);
					}
					anchorNodeContexts.add(new AnchorContext(href, anchorTextString, anchorContextString));
				}
			}
		}
		return anchorNodeContexts;
	}
	
  public StringBuilder getTextInSubTree(TdNode node, boolean recurse)
  {
  	return getTextinSubTree(node, recurse, null);
  }

	/**
   * Non-recursive method to get the text for the <code>node</code>
   * Collects the text even if the node contains other nodes in between,
   * specifically the <code>anchor</code>. It does not however include the 
   * text from the anchor node.
   * @param node
   * @param te
   * @return
   */
	//FIXME -- why is text in anchor node not included?
  public StringBuilder getTextinSubTree(TdNode node, boolean recurse, StringBuilder result)
  {
  	for (TdNode childNode	= node.content(); childNode != null; childNode = childNode.next())
  	{
			if (recurse && (childNode.element!=null) && (!childNode.element.equals("script")))
			{
				//Recursive call with the childNode
				result = getTextinSubTree(childNode, true, result);
			}	
			else if (childNode.type == TdNode.TextNode )
  		{
  			int length	= 0;
				if (result != null)
				{
					result.append(' ');							// append space to separate text chunks
					length		= result.length();
				}
  			result			= StringBuilderUtils.trimAndDecodeUTF8(result, childNode, 0, true);
  			
  			if ((result != null) && (length == result.length()))
  					result.setLength(length - 1);	// take the space off if nothing was appended
  		} 
  		else if ("img".equals(childNode.element))
  		{
  			AttVal altAtt	= childNode.getAttrByName(ALT);
  			String alt		= (altAtt != null) ? altAtt.value : null;
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
}