package ecologylab.bigsemantics.metadata.builtins;

import java.util.ArrayList;
import java.util.List;

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.declarations.ClippableDocumentDeclaration;
import ecologylab.bigsemantics.metadata.scalar.MetadataInteger;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * Image extends this, so that each image keeps track of all the clippings it is a participant itn.
 * Other media, such as video, will do the same thing.
 * 
 * @author andruid
 * 
 * @param <MC>
 *          The MediaClipping Type
 * @param <ME>
 *          The underling Media type
 */
@simpl_inherit
public class ClippableDocument extends ClippableDocumentDeclaration
{
//	/**
//	 * Clippings based on this.
//	 */
//	@mm_name("clippings") 
//	@simpl_collection
//	@simpl_classes(ImageClipping.class)
//	protected List<MediaClipping<ME>>	clippings;
//
//	@mm_name("width") 
//	@simpl_scalar
//	protected MetadataInteger								width;
//
//	@mm_name("height") 
//	@simpl_scalar
//	protected MetadataInteger								height;

	public static final int									INITIAL_CAPACITY	= 2;

	public ClippableDocument()
	{

	}

	public ClippableDocument(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
	}

	public List<Clipping> clippingsThisIsIn()
	{
		List<Clipping> result = this.getClippingsThisIsIn();
		if (result == null)
		{
			result = new ArrayList<Clipping>(INITIAL_CAPACITY);
			this.setClippingsThisIsIn(result);
		}
		return result;
	}

	public void addClipping(Clipping clipping)
	{
		List<Clipping> clippings = clippingsThisIsIn();
//		clipping.setParent(this);
		clippings.add(clipping);
	}

	public Document getClippingSource()
	{
		Document result = null;
		if (getClippingsThisIsIn() != null)
		{
			for (Clipping clipping : getClippingsThisIsIn())
			{
				result = clipping.getSourceDoc();
				if (result != null)
					break;
			}
		}
		return result;
	}

	/**
	 * Test to see if the value of the field is null, or if the field itself is null: width
	 */
	public boolean isNullWidth()
	{
		return getWidthMetadata() == null || getWidthMetadata().getValue() == null;
	}

	/**
	 * The heavy weight setter method for field width
	 */
	public void hwSetWidth(Integer width)
	{
		this.width().setValue(width);
		rebuildCompositeTermVector();
	}

	/**
	 * Heavy Weight Direct setter method for width
	 */
	public void hwSetWidthMetadata(MetadataInteger width)
	{
		if (!isNullWidth() && hasTermVector())
			termVector().remove(this.getWidthMetadata().termVector());
		this.setWidthMetadata(width);
		rebuildCompositeTermVector();
	}

	/**
	 * Test to see if the value of the field is null, or if the field itself is null: height
	 */
	public boolean isNullHeight()
	{
		return getHeightMetadata() == null || getHeightMetadata().getValue() == null;
	}

	/**
	 * The heavy weight setter method for field height
	 */
	public void hwSetHeight(Integer height)
	{
		this.height().setValue(height);
		rebuildCompositeTermVector();
	}

	/**
	 * Heavy Weight Direct setter method for height
	 */
	public void hwSetHeightMetadata(MetadataInteger height)
	{
		if (!isNullHeight() && hasTermVector())
			termVector().remove(this.getHeightMetadata().termVector());
		this.setHeightMetadata(height);
		rebuildCompositeTermVector();
	}

}
