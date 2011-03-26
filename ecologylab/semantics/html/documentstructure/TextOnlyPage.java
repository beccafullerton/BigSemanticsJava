package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.tidy.TdNode;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.TidyInterface;


/**
 * This is for the text-only page. 
 * This page might contain images but those images are not informative images, thus we recognize those pages with non-informative images 
 * as text-only pages. 
 * 
 * @author eunyee
 * @author andruid
 */
public class TextOnlyPage extends RecognizedDocumentStructure
{
	public TextOnlyPage(ParsedURL purl)
	{
		super(purl);
	}

	private static final int	MIN_PARA_TEXT_LENGTH	= 25;
	public static final int			MAX_TEXT_SURROGATES	= 5;
	
	/**
	 * Generate only text surrogates 
	 */
	@Override
	public void generateSurrogates(TdNode articleMain, ArrayList<ImgElement> imgNodes, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTexts, TidyInterface htmlType)
	{
		int count	= 0;
		while (!paraTexts.isEmpty() && count++<MAX_TEXT_SURROGATES)
		{
			ParagraphText paraText		= paraTexts.remove(paraTexts.lastKey());
			generateTextSurrogate(paraText, htmlType);
			paraText.recycle();	// releases to pool
		}
	}

	private void generateTextSurrogate(ParagraphText paraText, TidyInterface htmlType)
	{
		if (paraText.length()>MIN_PARA_TEXT_LENGTH)
		{
			paraText.unescapeXML();
			// Creates a TextElement using the buffy in paraText
			htmlType.newTxt(paraText);//, paraTexts.node );
		}
	}
}