package ecologylab.bigsemantics.html.documentstructure;

import ecologylab.net.ParsedURL;


/**
 * Generate surrogates for the documents that are determined as Index Pages.
 * We only generate surrogates for the other pages from the Index Pages. 
 * 
 * @author eunyee
 *
 */
public class IndexPage extends RecognizedDocumentStructure
{
	public IndexPage(ParsedURL purl)
	{
		super(purl);
	}
}