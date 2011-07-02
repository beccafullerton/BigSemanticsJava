/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.documentparsers.SearchParser;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.ImageClipping;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.simpl_inherit;

/**
 * This action needs to be implemented by the client.
 * 
 * @author amathur
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.CREATE_AND_VISUALIZE_IMG_SURROGATE)
class CreateAndVisualizeImgSurrogateSemanticAction
		extends SemanticAction
{

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.CREATE_AND_VISUALIZE_IMG_SURROGATE;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		Document source					= resolveSourceDocument();

		Image image 						= (Image) getArgumentObject(SemanticActionNamedArguments.METADATA);
		if (image == null)
		{
			ParsedURL imagePURL 	= (ParsedURL) getArgumentObject(SemanticActionNamedArguments.IMAGE_PURL);
			if (imagePURL != null)
			{
				image								= infoCollector.getOrConstructImage(imagePURL);
				
				//TODO -- if it already exists: (1) do we need to download??
				//															(2) should we merge metadata
			}
		}
		else
		{
			//TODO add to global collections?! if already there merge!
		}
		if (image != null)
		{
			image.setInfoCollector(infoCollector);

			Document mixin				= (Document) getArgumentObject(SemanticActionNamedArguments.MIXIN);
			if (mixin != null)
				image.addMixin(mixin);
			
			String caption 				= (String) getArgumentObject(SemanticActionNamedArguments.CAPTION);
			int width  		 		 		= getArgumentInteger(SemanticActionNamedArguments.WIDTH, 0);
			int height  		 		 	= getArgumentInteger(SemanticActionNamedArguments.HEIGHT, 0);
			
			ParsedURL hrefPURL 		= (ParsedURL) getArgumentObject(SemanticActionNamedArguments.HREF);
			Document outlink 			= (Document) getArgumentObject(SemanticActionNamedArguments.HREF_METADATA);
			if (hrefPURL != null & outlink == null)
				outlink				= infoCollector.getOrConstructDocument(hrefPURL);
			
			ImageClipping imageClipping	= image.constructClipping(source, outlink, caption, null);
			source.addClipping(imageClipping);
			
			SeedDistributor resultsDistributor = null;
			// look out for error condition
			SearchParser mmSearchParser = (documentParser instanceof SearchParser) ? (SearchParser) documentParser
					: null;
			DocumentClosure imageClosure= image.getOrConstructClosure();
			if (mmSearchParser != null)
			{
				Seed seed = mmSearchParser.getSeed();
				resultsDistributor = seed.seedDistributer(infoCollector);
				if (resultsDistributor != null) // Case 1 - Image Search
				{
					imageClosure.setSearchResult(resultsDistributor, ((SearchParser) documentParser).getResultSoFar());
					resultsDistributor.queueResult(imageClosure);
				}
			}
			if (resultsDistributor == null) // Case 2
			{
				//TODO -- add to Document.allLocalImages() if we did that
				imageClosure.queueDownload();
			}
			if (mmSearchParser != null)
			{
				// FIXME -- is this enough to do if a search fails????!
				mmSearchParser.incrementResultSoFar();
			}

			return image;
		}
		else
		{
			MetaMetadata mm	= getMetaMetadata();
			String mmString	= mm != null ? mm.getName() : "Couldn't getMetaMetadata()";
			error("Can't createAndVisualizeImgSurrogate because null PURL: " + mmString
					+ " - " + source.location());
		}
		return null;
	}

}
