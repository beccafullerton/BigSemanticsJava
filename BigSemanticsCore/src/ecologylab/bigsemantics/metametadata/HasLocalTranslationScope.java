package ecologylab.bigsemantics.metametadata;

import ecologylab.serialization.SimplTypesScope;

/**
 * This interface marks classes that can hold a local translation scope, e.g. a meta-metadata
 * package or a meta-metadata.
 * 
 * @author quyin
 *
 */
public interface HasLocalTranslationScope
{

	/**
	 * 
	 * @return The local translation scope.
	 */
	SimplTypesScope getLocalTranslationScope();
	
}
