/**
 * 
 */
package ecologylab.bigsemantics.metadata.builtins;

import ecologylab.bigsemantics.metadata.builtins.declarations.ImageClippingDeclaration;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.annotations.simpl_inherit;


/**
 * @author andruid
 *
 */
@simpl_inherit
public class ImageClipping extends ImageClippingDeclaration
{
	
	public ImageClipping()
	{
		super();
	}
	
	public ImageClipping(MetaMetadataCompositeField mmd)
	{
		super(mmd);
	}

	public ImageClipping(MetaMetadataCompositeField metaMetadata, Image clippedMedia, Document source, Document outlink, String caption, String context)
	{
		this(metaMetadata);
		MediaClipping.initMediaClipping(this, clippedMedia, source, outlink, caption, context);
	}
	
	@Override
	public boolean isImage()
	{
		return true;
	}
	
}
