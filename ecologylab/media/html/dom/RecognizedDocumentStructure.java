package ecologylab.media.html.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.tidy.AttVal;
import org.w3c.tidy.Lexer;
import org.w3c.tidy.TdNode;

import ecologylab.generic.Generic;
import ecologylab.generic.IntSlot;
import ecologylab.generic.StringTools;
import ecologylab.media.PixelBased;
import ecologylab.media.html.dom.documentstructure.AnchorContext;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.xml.XMLTools;



/**
 * We recognize web pages to index page, index-content page, content page, image-collection page, and low-quality page 
 * to work in forming surrogates. 
 * 
 * The image-collection page has lots of images with very few text. Those images may link to the Image URL or not link to anything else, 
 * so we are looking at the page mime type. 
 * 
 * In the index-content page, images are formed as surrogates if it is linked to the document, and there is informative text nearby 
 * that image. 
 * 
 * @author eunyee
 *
 */
public class RecognizedDocumentStructure 
{

	private static final int PARAGRAPH_COUNT_MINI_ARTICLE_THRESHOLD = 2;

	private static final int PARAGRAPH_COUNT_ARTICLE_THRESHOLD = 5;

	static final int CHAR_COUNT_ARTICLE_THRESHOLD = 300;

	public RecognizedDocumentStructure()
	{

	}

	/**
	 * This is the case there is no article main, which means high probability to be an index page.
	 * Needs to author informative image and text surrogate in the whole document itself.
	 * 
	 * @param articleMain
	 * @param imgNodes   
	 * @param totalTxtLeng
	 * @param paraTexts  
	 * @param htmlType
	 */
	protected void generateSurrogates(TdNode articleMain, ArrayList<HtmlNodewithAttr> imgNodes, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTexts, TidyInterface htmlType)
	{
		recognizeImgSurrogateForOtherPages( imgNodes, totalTxtLeng, htmlType );

		htmlType.setIndexPage();
	}

	/**
	 * Recognize the image surrogate for the other page based on the link to the other document (checking mime type for the page)
	 * and nearby text whether the text is informative and can be associated with the image for the image+text surrogate. 
	 * 
	 */
	protected void recognizeImgSurrogateForOtherPages(ArrayList<HtmlNodewithAttr> imgNodes, int totalTxtLeng, TidyInterface htmlType)
	{    	
		for(int i=0; i<imgNodes.size(); i++)
		{
			HtmlNodewithAttr ina = imgNodes.get(i);
			TdNode imgNode = ina.getNode();
			String extractedCaptionTxt = ""; 
			extractedCaptionTxt = getLongestTxtinSubTree(imgNode.grandParent(), extractedCaptionTxt);

			// this if condition checks whether the nearest text to the image is substantial enough to form a surrogate. 
			// TODO needs to check parent Href and Text informativity 	
			if( (extractedCaptionTxt.length()>10) && (!extractedCaptionTxt.contains("advertis")) )
			{
				// TODO!! ask whether we should add this to the associateText or not.
				ina.addToAttributesMap(HTMLDOMParser.textContext, extractedCaptionTxt);

				ParsedURL anchorPurl = findAnchorPURLforImgNode(htmlType, ina);

				// Check whether the anchor mimetype is not an image. 
				if( (anchorPurl!=null) && !anchorPurl.isImg() )
				{
					htmlType.newAnchorImgTxt(ina, anchorPurl);
					htmlType.removeTheContainerFromCandidates(anchorPurl);
				}

			}
		}
	}


	/**
	 * recognize whether the image is informative or not based on its attributes and size, aspect ratio. 
	 * 
	 * @param ina
	 * @return true if it is informative otherwise false.
	 */
	public boolean recognizeInformImage(HtmlNodewithAttr ina) 
	{
		String imgUrl = ina.getAttribute("src");
		int width 		= ina.getAttributeAsInt("width");
		int height 		= ina.getAttributeAsInt("height");
		
		float aspectRatio = (float) width / (float) height;
		aspectRatio 	= (aspectRatio>1.0f) ?  (float)1.0f/aspectRatio : aspectRatio;
		String altStr = ina.getAttribute("alt");

		boolean informImg = true;

		// Advertisement Keyword in the "alt" value
		if( altStr!=null && altStr.toLowerCase().contains("advertis") )  
			informImg = false;

		/*	We don't need this as we have an advertisement filter in the container.createImgElement(), and the filter is in cf.model.Filter.
		 * -- Eunyee 	
		if( imgUrl!=null )
		{

			String urlChunks[] = imgUrl.split("/");
			for(int j=0; j<urlChunks.length; j++)
			{
				String temp = urlChunks[j];
			//	System.out.println("url Chunk:" + temp);
				if( temp.toLowerCase().equals("ad") || temp.toLowerCase().equals("adv") ||
						temp.toLowerCase().contains("advertis") )
					informImg = false;
			}
		}
		 */		
		if( (width!=-1 && width<PixelBased.MIN_WIDTH) || (height!=-1 && height<PixelBased.MIN_HEIGHT) )
			informImg = false;

		if( aspectRatio > 0.9 )
			informImg = false;

		return informImg;
	}

	/**
	 * Check whether there is an article part in the current document. 
	 * Returns the article part if found. 
	 * 
	 * @param taggedDoc
	 * @return
	 */
	public static TdNode recognizeContentBody(DOMWalkInformationTagger taggedDoc) 
	{
		TdNode grandParent = null;
		TdNode ggParent = null;

		/*
		 * grandParent node with the count information.
		 * 
		 */
		HashMap<TdNode, IntSlot> grandParentChildCounts = new HashMap<TdNode, IntSlot>(); 

		/*
		 * grand-grandParent node with the count information.
		 */
		HashMap<TdNode, IntSlot> greatGrandParentChildCounts = new HashMap<TdNode, IntSlot>();

		// get linearlized from TreeMap
		Collection<ParagraphText> paragrphTextsValues = taggedDoc.getParagraphTextsTMap().values();
		for(ParagraphText pt : paragrphTextsValues)
		{
			TdNode parent	= pt.getNode().parent();
			grandParent 	= parent.parent();
			ggParent 		= grandParent.parent();

			// FIXME: Refactor the below method
			//identify common grandParent
			if( grandParentChildCounts.containsKey(grandParent) )
			{
				IntSlot numProgenySoFar = grandParentChildCounts.get(grandParent);
				numProgenySoFar.value++;
			}
			else
				grandParentChildCounts.put(grandParent, new IntSlot(1));

			//identify common great grandParent
			if( greatGrandParentChildCounts.containsKey(ggParent) )
			{
				IntSlot numProgenySoFar = greatGrandParentChildCounts.get(ggParent);
				numProgenySoFar.value++;
			}
			else
				greatGrandParentChildCounts.put(ggParent, new IntSlot(1));

		}

		TdNode articleMainNode = null;

		Object[] paragraphTextsArray = paragrphTextsValues.toArray();

		articleMainNode = findArticleMainNode(taggedDoc, grandParentChildCounts, paragraphTextsArray);

		// if no common grandParent, look for common greatGrandParent. 
		if( articleMainNode == null )
		{
			articleMainNode = findArticleMainNode(taggedDoc, greatGrandParentChildCounts, paragraphTextsArray);

		}

		return articleMainNode;
	}

	/**
	 * identify article sub-tree by locating common ancestor. 
	 * (Eunyee's dissertation algorithm) 
	 * 
	 * @param taggedDoc
	 * @param ancestorChildCounts
	 * @param paragraphTextsArray
	 * @return
	 */
	private static TdNode findArticleMainNode(DOMWalkInformationTagger taggedDoc,
			HashMap<TdNode, IntSlot> ancestorChildCounts, Object[] paragraphTextsArray) 
	{
		TdNode articleMainNode = null;

		Set<TdNode> grandParents = ancestorChildCounts.keySet();
		for (TdNode grandParentNode : grandParents)
		{
			IntSlot tint 	= ancestorChildCounts.get(grandParentNode);

			// If the majority of the paragraph nodes has the common grandParent node, 
			// we recognize the grandParent node as an articleMain node.
			if( tint.value >= PARAGRAPH_COUNT_ARTICLE_THRESHOLD )
			{
				articleMainNode = grandParentNode;
				break;
			}
			/*
			else if( tint.value >= PARAGRAPH_COUNT_MINI_ARTICLE_THRESHOLD )
			{
				int size 			= pprint.paragraphTexts.size();
				ParagraphText pt1 	= (ParagraphText) paragraphTextsArray[size-1];
				ParagraphText pt2 	= (ParagraphText) paragraphTextsArray[size-2];			

				if( (pt1.ptext.length() > CHAR_COUNT_ARTICLE_THRESHOLD) &&
					(pt2.ptext.length() > CHAR_COUNT_ARTICLE_THRESHOLD))
				{
						articleMainNode = grandParentNode;
						break;
				}

				//else if(pt1.ptext.length()+pt2.ptext.length()>500)
				//{
				//	articleMainNode = grandParentNode;
				//}

			}
			 */
		}
		return articleMainNode;
	}


	/**
	 * 1)	Image should be not too small (small images are usually copyrights or icons..)
	 * 2)	Image with a link does not tend to be an article image. 
	 * 		It may be informative-image, but it is not an article-related image. 
	 * 3)	Image ratio can sometimes catch uninformative images. 
	 * 4)	Textual Features: terms in URL, Alt texts, descriptions, and nearest texts in DOM. 
	 * 
	 */
	protected void associateImageTextSurrogate(TidyInterface htmlType, TdNode articleBody, TreeMap<Integer, ParagraphText> paraTexts)
	{	
		for( HtmlNodewithAttr ina: imgNodesInContentBody) 
		{  		
			boolean articleImg = recognizeInformImage(ina);

			// If articleImg is set true, it means it is informative image, so it will be image surrogate.
			if( articleImg )
			{
				String extractedCaption = "";
				extractedCaption = getLongestTxtinSubTree(ina.getNode().grandParent(), extractedCaption);
				informImgNodes.add(ina.getNode());

				StringBuilder textContext	 = null;

				while( (textContext == null) && (paraTexts.size() > 0) )
				{
					ParagraphText pt 	= paraTexts.remove(paraTexts.lastKey());
					TdNode textNode 	= pt.getNode();
					if( textNode.grandParent().equals(articleBody) || 
							textNode.greatGrandParent().equals(articleBody) )
					{
						textContext = pt.getPtext();

						// add assocateText into the newImage, so that we can author Image+Text surrogate later
						if( textContext != null )
						{
							String altText = ina.getAttribute("alt");
							if( (extractedCaption.trim().length()>0) || ( (altText!=null) && (altText.length()>0) ) )
							{
								ina.addToAttributesMap(HTMLDOMParser.extractedCaption, XMLTools.unescapeXML(extractedCaption));
								TermVector captionTV = new TermVector(extractedCaption);
								TermVector associateTextTV = new TermVector(textContext);


								boolean altTextSimilarity = false;
								if( (altText!=null) && (altText.length()>0) )
								{
									TermVector altText_tv = new TermVector(altText);
									altTextSimilarity = (altText_tv.dot(associateTextTV)>0);	        						
								}

								// check for common sharp terms between associateText and captionText
								if( (associateTextTV.dot(captionTV)>0) || altTextSimilarity)
								{
									XMLTools.unescapeXML(textContext);				
									ina.addToAttributesMap(HTMLDOMParser.textContext, StringTools.toString(textContext));
									// i believe this was never used -- andruid 4/17/09
//									ina.addToAttributesMap(HTMLDOMParser.TextNode, textNode);
//									associated = true;
								}
							}
							else
							{
								//	ina.addToAttributesMap(HTMLDOMParser.extractedCaption, XMLTools.unescapeXML(extractedCaption));
								XMLTools.unescapeXML(textContext);				
								ina.addToAttributesMap(HTMLDOMParser.textContext, textContext.toString());
								// i believe this was never used -- andruid 4/17/09
								//								ina.addToAttributesMap(HTMLDOMParser.TextNode, textNode);
							}
						}
					}
					pt.recycle();
				}

				ParsedURL anchorPurl = findAnchorPURLforImgNode(htmlType, ina);

				// removed by andruid 10/16/08
				// 1) i dont see why we need to do this.
				// 2) IT CREATES A RACE CONDITION WITH PRUNE, WHICH STOPS THE CRAWLER :-(
				//        		if( anchorPurl!=null )
				//        			htmlType.removeTheContainerFromCandidates(anchorPurl);

				// i believe this was never used -- andruid 4/17/09
				//				ina.addToAttributesMap(HTMLDOMParser.ImgNode, ina.getNode());
				htmlType.newImgTxt(ina, anchorPurl);

			}
		}

	}

	/**
	 * Check whether the image node has the anchor url or not, if so return it as ParsedURL. 
	 *  
	 * @param htmlType
	 * @param ina
	 * @return
	 */
	protected ParsedURL findAnchorPURLforImgNode(TidyInterface htmlType, HtmlNodewithAttr ina) 
	{
		boolean isparentHref = ina.getNode().parent().element.equals("a"); 
		ParsedURL anchorPurl = null;
		if(isparentHref)
		{
			AttVal parentHref = ina.getNode().parent().getAttrByName("href");
			if( parentHref!=null )
			{
				anchorPurl = htmlType.getAnchorParsedURL(parentHref.value);
			}
			else
			{
				// probably the case that the anchor is pointing to the section in the HTML page
				// For example, <a name="">
			}
		}
		return anchorPurl;
	}


	/**
	 * All the article images that determined informative.
	 */
	private ArrayList<TdNode> informImgNodes		= new ArrayList<TdNode>();    

	/**
	 * All the image nodes under the sub-tree of the ArticleMain node.
	 */
	protected ArrayList<HtmlNodewithAttr> imgNodesInContentBody = new ArrayList<HtmlNodewithAttr>();

	/**
	 * Finding image nodes under the content body. 
	 * 
	 * @param contentBody
	 */
	public void findImgsInContentBodySubTree(TdNode contentBody)
	{
		String nodeElementString = "img";
		htmlNodesInContentBody(contentBody, nodeElementString, imgNodesInContentBody);
	}

	/**
	 * Common method to find a particular html node based on nodeElementString 
	 * that adds to either hrefNodesInContentBody or imgNodesInContentBody
	 * @param contentBody
	 * @param nodeElementString
	 * @param nodesInContentBody
	 */
	private void htmlNodesInContentBody(TdNode contentBody,
			String nodeElementString,
			ArrayList<HtmlNodewithAttr> nodesInContentBody)
	{
		TdNode contentNode = contentBody.content();

		while( contentNode != null )
		{
			htmlNodesInContentBody(contentNode, nodeElementString, nodesInContentBody);
			if( contentNode.element!=null && contentNode.element.equals(nodeElementString) )
			{
				HtmlNodewithAttr ina = new HtmlNodewithAttr(contentNode);
				nodesInContentBody.add(ina);
			}
			contentNode = contentNode.next();
		}
	}

	/**
	 * check the texts under the DOM node that is passed as a parameter.
	 * 
	 * @param parent node of the image node is passed in to the parameter. 
	 */
	//FIXME -- use StringBuilder, not String
	public String getLongestTxtinSubTree(TdNode node, String captionTxt)
	{
		TdNode childNode	= node.content();

		while( childNode != null )
		{
			if( (childNode.element!=null) && (!childNode.element.equals("script")))
			{
				//Recursive call with the childNode
				captionTxt = getLongestTxtinSubTree(childNode, captionTxt);
			}	
			if( childNode.type == TdNode.TextNode )
			{
				String tempstr = Lexer.getString(childNode.textarray(), childNode.start(), childNode.end()-childNode.start());
				tempstr = tempstr.trim();
				if( (!tempstr.startsWith("<!--")) && (captionTxt.trim().length() <= tempstr.trim().length()) )
					captionTxt = tempstr;

				//   			Debug.println("captionTxt=" + captionTxt);
			}

			childNode = childNode.next();
		}
		return captionTxt;
	}


	protected boolean checkLinkIn(TdNode parentNode, TdNode currentNode)
	{
		//  	System.out.println("Parent Node : " + parentNode.element + " : " + currentNode );
		//  	System.out.println("\nCurrentNode: " + parentNode.element );
		TdNode temp = parentNode.content();
		TdNode prevNode = null;
		while( temp != null )
		{
			/*
    		checkLinkIn(temp, temp);
			if( temp.element != null )    		
				System.out.println("NODE:" + temp.element);
			 */
			if( (prevNode!=null) && (prevNode.element!=null) && (prevNode.element.equals("a")) )
				return true;

			prevNode = temp;
			temp = temp.next();
		}
		return false;
	}



	/**
	 * Initial Implementation for PhatSurrogate Implementation. 
	 * 
	 * @param articleMain
	 */
	protected void printArticleText( TdNode articleMain )//, String paraElement)
	{
		if( articleMain!=null && (articleMain.element!=null) && !articleMain.element.equals("script") )
		{
			TdNode temp = articleMain.content();
			while( temp != null )
			{
				//System.out.println("\n\n---------- Paragraph HTML Element : " + paraElement );   		
				printArticleText(temp);
				if( temp.type==TdNode.TextNode ) //&& (temp.parent().element!=null) && temp.parent().element.equals(paraElement))
				{
					// Print Text in ArticleMain
					Lexer.getString(temp.textarray(), temp.start(), temp.end()-temp.start() );   			
				}
				temp = temp.next();
			}
		}

	}

	public ArrayList<HtmlNodewithAttr> getImgNodesInContentBody() 
	{
		return imgNodesInContentBody;
	}

	public ArrayList<TdNode> getInformImgNodes() 
	{
		return informImgNodes;
	}





}