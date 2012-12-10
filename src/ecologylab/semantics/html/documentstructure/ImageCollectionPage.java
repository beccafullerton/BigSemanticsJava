package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.jar.Attributes.Name;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.DOMParserInterface;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.serialization.XMLTools;

/**
 * Generate surrogates for the documents that are determined as Image-Collection Pages.
 * 
 * @author eunyee
 * 
 */
public class ImageCollectionPage extends RecognizedDocumentStructure
{
	public ImageCollectionPage(ParsedURL purl)
	{
		super(purl);
	}

	/**
	 * Generate surrogates for the images inside the image-collection pages.
	 */
	@Override
	public void generateSurrogates(Node articleMain, ArrayList<ImgElement> imgElements, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTextMap, DOMParserInterface htmlType)
	{
		Collection<ParagraphText> paraTextsC	= paraTextMap.values();
		ParagraphText[] paraTexts	= new ParagraphText[paraTextsC.size()];
		paraTextsC.toArray(paraTexts);
		for (int i = 0; i < imgElements.size(); i++)
		{
			ImgElement imgElement 				= imgElements.get(i);
			
			String altText 								= imgElement.getNonBogusAlt();
			
			if (altText == null)
			{
				final Node imageNodeNode 	= imgElement.getNode();
				StringBuilder extractedCaption = getLongestTxtinSubTree(imageNodeNode.getParentNode().getParentNode(), null);	// returns null in worst case
				if (extractedCaption == null)
					extractedCaption = getLongestTxtinSubTree(imageNodeNode.getParentNode().getParentNode().getParentNode(), null);	// returns null in worst case
								
				if (extractedCaption != null)
				{
					XMLTools.unescapeXML(extractedCaption);
					imgElement.setAlt(StringTools.toString(extractedCaption));
					
					StringBuilderUtils.release(extractedCaption);
				}
			}

			ParsedURL anchorPurl = findAnchorPURL(imgElement);

			// images in the image-collection pages won't have anchors
			// If there is an anchor, it should be pointing to the bigger image.
			if (anchorPurl == null)
				htmlType.constructImageClipping(imgElement, null);
			else if ((anchorPurl != null) && anchorPurl.isImg())
			{
				htmlType.constructImageClipping(imgElement, anchorPurl);
				htmlType.removeTheContainerFromCandidates(anchorPurl);
			}
			else // if (anchorPurl.isHTML() || anchorPurl.isPDF() || anchorPurl.isRSS())
			{
				// TODO find the anchorContext for this purl
				Node parent		= imgElement.getNode().getParentNode();
				Node gParent	= parent.getParentNode();
				Node ggParent	= gParent.getParentNode();
				for (ParagraphText paraText : paraTexts)
				{
					Node paraTextNode	= paraText.getElementNode();
					NodeList children = paraTextNode.getChildNodes();
					Node contextNode	= null;
					if (paraTextNode == parent)
						contextNode				= parent;
					else if (paraTextNode == gParent)
						contextNode				= gParent;
					else if (paraTextNode == ggParent)
						contextNode				= ggParent;					
					else for (int j=0; j<children.getLength(); j++)
					{
						Node childNode = children.item(j);
						if (paraTextNode == childNode)
						{
							contextNode			= childNode;
						}
					}
					if (contextNode != null)
					{
						paraText.setImgElementTextContext(imgElement);
						break;
					}
				}
				
				//TODO: sashi - Do something with the tv and InterestModel 
				//if(altText == null && extractedCaption == null )
				htmlType.constructImageClipping(imgElement, anchorPurl);
			}
			imgElement.recycle();
		}
	}
}