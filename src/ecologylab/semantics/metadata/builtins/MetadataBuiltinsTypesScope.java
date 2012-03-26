/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.generic.Debug;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.declarations.MetadataBuiltinDeclarationsTranslationScope;
import ecologylab.semantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.serialization.SimplTypesScope;

/**
 * Encapsulates ClassDescriptors for Metadata and its subclasses that are coded by hand.
 */
public class MetadataBuiltinsTypesScope extends Debug
{
	public static final String NAME = "metadata_builtin_translations";
	
	protected static final Class CLASSES[] = 
	{
		Metadata.class, 
		Annotation.class,
		ClippableDocument.class,
		Clipping.class,
		CompoundDocument.class,
		DebugMetadata.class,
		Document.class, 
		DocumentMetadataWrap.class,
		Image.class, 
		ImageClipping.class,
		MediaClipping.class,
		TextClipping.class,
		GeoLocation.class,
	};
	
	static
	{
		MetadataScalarType.init();
	}

	public static SimplTypesScope get()
	{
		return SimplTypesScope.get(NAME, MetadataBuiltinDeclarationsTranslationScope.get(), CLASSES);
	}	
}
