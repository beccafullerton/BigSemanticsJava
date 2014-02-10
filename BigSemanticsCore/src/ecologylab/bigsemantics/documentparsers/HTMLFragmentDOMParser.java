package ecologylab.bigsemantics.documentparsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.html.DOMParserInterface;
import ecologylab.bigsemantics.html.ImgElement;
import ecologylab.bigsemantics.html.utils.HTMLNames;
import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.bigsemantics.metadata.builtins.AnonymousDocument;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metadata.builtins.Image;
import ecologylab.bigsemantics.metadata.builtins.ImageClipping;
import ecologylab.generic.Continuation;
import ecologylab.generic.DomTools;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.XMLTools;

public class HTMLFragmentDOMParser extends HTMLDOMParser implements DOMParserInterface, HTMLNames
{
  InputStream                             fragmentStream;

  Reader                                  reader;

  ArrayList<ImageClipping>                imageClippings            = new ArrayList<ImageClipping>();

  ParsedURL                               containerPurl;

  Document                                containerDocument;

  Document                                textOutlink;

  StringBuilder                           bodyTextBuffy             = new StringBuilder();

  SpecialImageUrlHandler                  specialImageUrlHandler    = new SpecialImageUrlHandler();

  private static HashMap<String, Integer> namesOfBreaklineNodeNames = null;


	public HTMLFragmentDOMParser(Reader reader, InputStream inputStream)
	{
	  super();
		fragmentStream 			= inputStream;
		this.reader					= reader;
		AnonymousDocument anonymousDocument = new AnonymousDocument();
		setDocumentClosure(anonymousDocument.getOrConstructClosure());
	}

	@Override
	public void parse() throws IOException
	{
		org.w3c.dom.Document dom = getDom();
		//DomTools.prettyPrint(dom);
		
		int containerNodeIndex = 0;
		NodeList bodyNodeList = dom.getElementsByTagName(BODY);
		if (bodyNodeList.getLength() > 0)
		{
			Node bodyNode = bodyNodeList.item(0);
			parseText(bodyTextBuffy, bodyNode);
			
			checkForSimplSourceLocation(bodyNode);
			checkForMetadata(bodyNode);
		}

		parseImages(dom);
	}

	private void parseImages(org.w3c.dom.Document dom)
	{
		NodeList imgNodeList	= dom.getElementsByTagName(IMG);
		int numImages 				= imgNodeList.getLength();
		if (numImages > 0)
		{
			for (int i = 0; i < numImages; i++)
			{
				Node imgNode = imgNodeList.item(i);
				Node parent 			= imgNode.getParentNode();

				Document outlink	= null;
				boolean changeSourceDoc = false;
				
				String src 				= DomTools.getAttribute(imgNode, SRC);
				src               = specialImageUrlHandler.changeImageUrlIfNeeded(src);
				ParsedURL imgPurl	= ImgElement.constructPurl(containerPurl, src);

				do
				{
					if (A.equals(parent.getNodeName()))
					{
						String hrefString	= DomTools.getAttribute(parent, HREF);
						if (hrefString != null)
						{
							try
							{
							  if (imgPurl == null)
							  {
							    String srcUrl = specialImageUrlHandler.getImageUrlFromParameters(hrefString);
							    if (srcUrl != null)
							      imgPurl = ImgElement.constructPurl(containerPurl, srcUrl);
							  }
							  
							  if (imgPurl == null)
							    break;
							  
							  StringBuilder newImgHrefBuf = StringBuilderUtils.acquire();
								changeSourceDoc = specialImageUrlHandler.changeImageRefUrlAndSourceDocIfNeeded(hrefString, newImgHrefBuf);
								hrefString = newImgHrefBuf.length() > 0 ? newImgHrefBuf.toString() : hrefString;
								StringBuilderUtils.release(newImgHrefBuf);
							}
							catch (UnsupportedEncodingException e)
							{
								error("Image ref URL cannot be decoded because it is using unsupported encoding. " +
										  "We support UTF-8 only.");
								e.printStackTrace();
							}
							ParsedURL aHref						= ImgElement.constructPurl(containerPurl, hrefString);
							if (aHref != null)
								outlink									= getSemanticsScope().getOrConstructDocument(aHref);
						}
						break;
					}
					parent	= parent.getParentNode();		
				} while (parent != null);
				
				if (imgPurl == null)
				  continue;
				
				SemanticsGlobalScope semanticsSessionScope	= getSemanticsScope();
				Image image																	= semanticsSessionScope.getOrConstructImage(imgPurl);
				if (image != null)
				{
					String altText = DomTools.getAttribute(imgNode, ALT);
					final ImageClipping imageClipping = image.constructClipping(containerDocument, null /*outlink*/, altText, null);
					if (changeSourceDoc)
					{
					  outlink.queueDownload(new Continuation<DocumentClosure>()
            {
              @Override
              public void callback(DocumentClosure o)
              {
                Document downloadedDoc = o.getDocument();
                if (downloadedDoc != null && !downloadedDoc.isRecycled())
                {
                  imageClipping.setSourceDoc(downloadedDoc);
                  imageClipping.setOutlinks(null);
                  imageClipping.setMetadataChanged(true);
                }
              }
            });
					}
					imageClippings.add(imageClipping);
				}				
			}
		}
	}
	
	public void parseText(StringBuilder buffy, Node bodyNode)
	{
		//debug("Node:" + bodyNode.getNodeName() + ":" + bodyNode.getNodeValue());
		
		NodeList children = bodyNode.getChildNodes();
		boolean addLine = false; // this is outside of the loop below to make it work correctly
		for (int i = 0; i < children.getLength(); i++)
		{
			Node kid = children.item(i);
			
			if (A.equals(kid.getNodeName()) && textOutlink == null)	// first cut; needs refinement
			{
				String hrefString	= DomTools.getAttribute(kid, HREF);
				if (hrefString != null)
				{
					ParsedURL aHref						= ImgElement.constructPurl(containerPurl, hrefString);
					if (aHref != null)
						textOutlink							= getSemanticsScope().getOrConstructDocument(aHref);
				}
			}

			if (addLine == false)
				addLine = shouldBreakLineWithNodeName(kid.getNodeName());
			if (kid.getNodeValue() != null)
			{
				String v = kid.getNodeValue();
				if (kid.getNodeName().equals(HASH_COMMENT))
					continue;
				addWithOneSpaceBetween(buffy, v, false);
				if (addLine)
				{
					buffy.append('\n');
					addLine = false;
				}
			}
			else if(shouldBreakLineWithNodeName(kid.getNodeName()))
			{
				buffy.append('\n');
			}
			//addWithOneSpaceBetween(buffy, walkDomAddingTextAndAddNewlines(kid), true);
			parseText(buffy, kid);
		}
	}

	private static void addWithOneSpaceBetween(StringBuilder buffy, String v, boolean newlineOK)
	{
		char lastChar	= (buffy.length() > 0) ? buffy.charAt(buffy.length() - 1) : ' ';
		if (lastChar != '\n')
			buffy.append(' ');
		
		if (!newlineOK )
			v = v.replaceAll("\\n", " ");
		v = v.replaceAll("^[\\s]+", "");
		v = v.replaceAll("[\\s]+", " ");
		if (v.length() > 0)
			buffy.append(v);
	}		
	
	/**
	 * @author rhema returns true when a breakline would make sense based on the node name.
	 * 
	 * @param nodeName
	 *          such as p, div, br
	 * @return
	 */
	private static boolean shouldBreakLineWithNodeName(String nodeName)
	{
		String name = nodeName.toLowerCase();
		if (namesOfBreaklineNodeNames == null)
		{
			namesOfBreaklineNodeNames = new HashMap<String, Integer>();
			namesOfBreaklineNodeNames.put("p", 1);
			namesOfBreaklineNodeNames.put("h1", 1);
			namesOfBreaklineNodeNames.put("h2", 1);
			namesOfBreaklineNodeNames.put("h3", 1);
			namesOfBreaklineNodeNames.put("h4", 1);
			namesOfBreaklineNodeNames.put("h5", 1);
			namesOfBreaklineNodeNames.put("h6", 1);
			namesOfBreaklineNodeNames.put("br", 1);
			namesOfBreaklineNodeNames.put("div", 1);
		}
		return namesOfBreaklineNodeNames.containsKey(name);
	}

	void checkForSimplSourceLocation(Node node)
	{
		node.getAttributes();
		if (node.getAttributes() != null && setContainerLocation(node) != null)
		{
			return;
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			checkForSimplSourceLocation(children.item(i));
		}
	}
	
	private ParsedURL setContainerLocation(Node elementNode)
	{
		if (containerPurl == null && elementNode != null)
		{
			String containerLocation = DomTools.getAttribute(elementNode, SIMPL_SOURCE_LOCATION);
			
			if (containerLocation == null || containerLocation.length() == 0)
				containerLocation = DomTools.getAttribute(elementNode, SIMPL);
			
			if (containerLocation == null || containerLocation.length() == 0)
				containerLocation = DomTools.getAttribute(elementNode, CONTAINER);

			if (containerLocation != null && containerLocation.length() > 0)
			{
				containerLocation 	= XMLTools.unescapeXML(containerLocation);
				containerPurl 		= ParsedURL.getAbsolute(containerLocation);
				containerDocument	= getSemanticsScope().getOrConstructDocument(containerPurl);
			}
		}
		return containerPurl;
	}
	
	void checkForMetadata(Node node)
	{
		node.getAttributes();
		if (node.getAttributes() != null && parseInjectedMetadata(node) != null)
		{
			return;
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			checkForMetadata(children.item(i));
		}
	}

	private ParsedURL parseInjectedMetadata(Node elementNode)
	{
		if (containerPurl == null && elementNode != null)
		{
			String containerMetadata = DomTools.getAttribute(elementNode, SIMPL_METADATA);
			DomTools.prettyPrint(elementNode);
			
			if (containerMetadata != null && containerMetadata.length() > 0)
			{
				System.out.println("\n\nsimpl:metadata:\n"+containerMetadata+"\n\n");
				Document metadataFromBrowser	= Document.constructAndMapFromJson(containerMetadata, getSemanticsScope());				
				if (metadataFromBrowser != null) 
				{
					// workflows need to be modified to accomodate metadata coming from drag
					System.out.println("\nSetting container document to injected metadata\n");
					
					containerDocument	= metadataFromBrowser;
					containerPurl 		= metadataFromBrowser.getLocation();
				}
			}
		}
		return containerPurl;
	}

	@Override
	public InputStream inputStream()
	{
		return fragmentStream;
	}

	@Override
	public Reader reader()
	{
		return reader;
	}
	public String getBodyText()
	{
		return bodyTextBuffy.toString();
	}
	public Document getTextOutlink()
	{
		return textOutlink;
	}

	public ArrayList<ImageClipping> getImageClippings()
	{

		return imageClippings;
	}

	public void setContent()
	{
	}

	public void setIndexPage()
	{
	}

	public Document getContainerDocument()
	{
		return containerDocument;
	}

	public ParsedURL getContainerPurl()
	{
		return containerPurl;
	}
	
	@Override
	public void recycle()
	{
		fragmentStream 		= null;
		reader 						= null;
		imageClippings.clear();
		imageClippings 		= null;
		containerPurl			= null;
		containerDocument	= null;
		textOutlink				= null;
		bodyTextBuffy			= null;
		super.recycle();
	}

}
