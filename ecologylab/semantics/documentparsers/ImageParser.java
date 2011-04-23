/**
 * 
 */
package ecologylab.semantics.documentparsers;

import java.io.IOException;

import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;

/**
 * @author andruid
 *
 */
public class ImageParser extends DocumentParser<Image>
{

	/**
	 * 
	 */
	public ImageParser()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param infoCollector
	 */
	public ImageParser(NewInfoCollector infoCollector)
	{
		super(infoCollector);
	}

/**
 * 
 * @see ecologylab.semantics.documentparsers.DocumentParser#parse()
 */
	@Override
	public Document parse() throws IOException
	{
		debug("parse() not ");
		return null;
	}

}
