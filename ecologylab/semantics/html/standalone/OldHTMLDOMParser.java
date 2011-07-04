package ecologylab.semantics.html.standalone;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import ecologylab.generic.StringTools;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.AElement;
import ecologylab.semantics.html.DOMWalkInformationTagger;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.DOMParserInterface;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.semantics.html.documentstructure.ContentPage;
import ecologylab.semantics.html.documentstructure.ImageCollectionPage;
import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.html.documentstructure.IndexPage;
import ecologylab.semantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.semantics.html.documentstructure.TextOnlyPage;
import ecologylab.semantics.html.dom.IDOMProvider;
import ecologylab.semantics.html.utils.HTMLAttributeNames;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.serialization.XMLTools;


/**
 * WARNING: This code is deprecated in cF and ecologylabSemantics.
 * It remains only for the purpose of maintaining some standalone classes, which Andruid
 * imagines are for algorithm verification (and thus, useful.)
 * <p/>
 * 
 * Connect to JTidy parser to parse HTML pages for standalone algorithm measurement.
 * This parsing code integrates with the Image-Text Surrogate extractor code.
 * 
 * @author eunyee
 *
 */
@Deprecated
public class OldHTMLDOMParser
implements HTMLAttributeNames, IDOMProvider
{
	PURLConnection purlConnection;
	IDOMProvider provider;

	/**
	 * because Tidy extends Serializable
	 */
	private static final long serialVersionUID = 1L;

	public OldHTMLDOMParser()
	{	
		super();
	}

	/**
	 * Parse HTML Document, and return the root DOM node
	 * 
	 * @param in
	 * @param purl TODO
	 * @param out
	 * @param tidyInterface
	 */
	public org.w3c.dom.Document parse(PURLConnection purlConnection)
	{
		this.purlConnection		= purlConnection;
		return provider.parseDOM(purlConnection.inputStream(), null);
	}

	/**
	 * Extract Image and Text surrogates while walk through DOM 
	 * 
	 * @param in
	 * @param htmlType
	 */
	public void parse(PURLConnection purlConnection, DOMParserInterface htmlType)
	{
		Document parsedDoc = parse(purlConnection);

		
		DOMWalkInformationTagger taggedDoc = walkAndTagDom(parsedDoc, htmlType);
		
		extractImageTextSurrogates(taggedDoc, htmlType);
		
		//Now, find hrefs, with their context and generate containers with metadata
		ArrayList<AnchorContext> anchorContexts = buildAnchorContexts(taggedDoc.getAllAnchorNodes(), purl());
		
  	if(htmlType != null)
			htmlType.generateCandidateContainersFromContexts(anchorContexts, false);
  	
  	anchorContexts.clear();
		taggedDoc.recycle();
	}

	/**
	 * This is the walk of the dom that calls print tree, and the parser methods such as closeHref etc.
	 * @param doc
	 * @param htmlType
	 * @return
	 */
	public DOMWalkInformationTagger walkAndTagDom(Node rootTdNode, DOMParserInterface htmlType)
	{

//		jtidyPrettyOutput.state = StreamIn.FSM_ASCII;
//		jtidyPrettyOutput.encoding = configuration.CharEncoding;

		DOMWalkInformationTagger domTagger = new DOMWalkInformationTagger(purlConnection.getPurl(), htmlType);

		StringWriter writer = new StringWriter();
		// walk through the HTML document object.
		// gather all paragraphText and image objects in the data structure.
		//FIXME -- get rid of this call and object!
		domTagger.printTree(rootTdNode, writer);

		domTagger.flushLine(writer);
		return domTagger;
	}

	/**
	 * Extract Image and Text Surrogates while walk through DOM
	 * 
	 * historically was called as pprint() in JTidy. 
	 */
	public void extractImageTextSurrogates(DOMWalkInformationTagger taggedDoc, DOMParserInterface htmlType)
	{

		Node contentBody = RecognizedDocumentStructure.recognizeContentBody(taggedDoc);
		//System.out.println("\n\ncontentBody = " + contentBody);       
		ArrayList<ImgElement> imgNodes = taggedDoc.getAllImgNodes();

		recognizeDocumentStructureToGenerateSurrogate(htmlType, taggedDoc, contentBody, imgNodes);
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
	private void recognizeDocumentStructureToGenerateSurrogate(DOMParserInterface htmlType,
			DOMWalkInformationTagger domWalkInfoTagger, Node contentBody,
			ArrayList<ImgElement> imgNodes) 
	{
		RecognizedDocumentStructure pageCategory = null;

		if( contentBody!=null )
		{
			// Content Pages
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
			else if( numImgNodes!=0 )
			{
				// Index Pages (include index-content pages)
				//FIXME -- should also look at text only pages & especially use link ratio as a feature!!!!
				pageCategory = new IndexPage(purl());
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
		if( (htmlType.numExtractedClippings()==0) && (paragraphTextsTMap.size()>0) )
		{
			pageCategory = new TextOnlyPage(purl());
			pageCategory.generateSurrogates(contentBody, imgNodes, domWalkInfoTagger.getTotalTxtLength(), paragraphTextsTMap, htmlType);
		}
		if (pageCategory != null)
			htmlType.setRecognizedDocumentStructure(pageCategory.getClass());
	}


	/**
	 * Transform an set of AElements (HTML a) into a set of AnchorContexts.
	 * In some cases, an AElement may result in no entry, because the anchor text and anchor context are both empty.
	 * @param anchorElements
	 * 
	 * @return
	 */
	public ArrayList<AnchorContext> buildAnchorContexts(ArrayList<AElement> anchorElements, ParsedURL sourcePurl)
	{
		ArrayList<AnchorContext> anchorNodeContexts = new ArrayList<AnchorContext>();
		
		for (AElement aElement : anchorElements)
		{
			AnchorContext aContext= constructAnchorContext(aElement, sourcePurl);
			if(aContext!=null)
			{
					anchorNodeContexts.add(aContext);
			}
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
	 * 
	 * @return					AnchorContext object, or null.
	 */
	public AnchorContext constructAnchorContext(AElement aElement, ParsedURL sourcePurl)
	{
		Node anchorNodeNode 				  = aElement.getNode();
		ParsedURL href 									= aElement.getHref();
		if (href != null)
		{
			Node parent 							  = anchorNodeNode.getParentNode();
			//FIXME -- this routine drops all sorts of significant stuff because it does not concatenate across tags.
			StringBuilder anchorContext 	= getTextInSubTree(parent, false);
			
			//TODO: provide ability to specify alternate anchorContext
			StringBuilder anchorText 			= getTextInSubTree(anchorNodeNode, true);
			if ((anchorContext != null) || (anchorText != null))
			{
				String anchorContextString	= null;
				if (anchorContext != null)
				{
					XMLTools.unescapeXML(anchorContext);
					StringTools.toLowerCase(anchorContext);
					anchorContextString				= StringTools.toString(anchorContext);
					StringBuilderUtils.release(anchorContext);
				}
				String anchorTextString			= null;
				if (anchorText != null)
				{
					XMLTools.unescapeXML(anchorText);
					StringTools.toLowerCase(anchorText);
					anchorTextString					= StringTools.toString(anchorText);
					StringBuilderUtils.release(anchorText);
				}
				return new AnchorContext(href, anchorTextString, anchorContextString, sourcePurl, false, false);
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
   * text from the anchor node.
   * @param node
   * @param te
   * @return
   */
	//FIXME -- why is text in anchor node not included?
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
  			
  			if ((result != null) && (length == result.length()))
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
  
  ParsedURL purl()
  {
  	return purlConnection.getPurl();
  }

	@Override
	public void setQuiet(boolean b)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setShowWarnings(boolean b)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public org.w3c.dom.Node parse(InputStream in, String file, OutputStream out)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document parseDOM(InputStream inputStream, OutputStream out)
	{
		// TODO Auto-generated method stub
		return null;
	}
}