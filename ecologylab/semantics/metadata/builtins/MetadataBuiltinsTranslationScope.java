/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.generic.Debug;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataInteger;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.serialization.TranslationScope;

/**
 * Encapsulates ClassDescriptors for Metadata and its subclasses that are coded by hand.
 */
public class MetadataBuiltinsTranslationScope extends Debug
{
	public static final String NAME = "metadata_builtin_translations";
	
	protected static final Class CLASSES[] = 
	{
		Metadata.class, 
		Document.class, 
		Entity.class,
		ClippableDocument.class, 
		Image.class, 
		Text.class,
		Surrogate.class,
		DebugMetadata.class,

		
	};

	public static TranslationScope get()
	{
		return TranslationScope.get(NAME, CLASSES);
	}	
}
