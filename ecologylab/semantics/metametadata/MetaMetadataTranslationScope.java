package ecologylab.semantics.metametadata;

import ecologylab.generic.Debug;
import ecologylab.io.BasicSite;
import ecologylab.net.UserAgent;
import ecologylab.semantics.actions.ConditionTranslationScope;
import ecologylab.semantics.actions.NestedSemanticAction;
import ecologylab.semantics.actions.SemanticActionTranslationScope;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.connectors.CookieProcessing;
import ecologylab.semantics.connectors.SemanticsSite;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.serialization.TranslationScope;
import ecologylab.textformat.NamedStyle;

public class MetaMetadataTranslationScope extends Debug
{
	public static final String		NAME						= "meta_metadata";

	public static final String		BASE_NAME				= "meta_metadata_base";

	protected static final Class	BASE_CLASSES[]	=
	{
		MetadataClassDescriptor.class,
		MetadataFieldDescriptor.class,
		MetaMetadataField.class,
		MetaMetadataScalarField.class,
		MetaMetadataCompositeField.class,
		MetaMetadataNestedField.class,
		MetaMetadataCollectionField.class,
		MetaMetadata.class,
		SearchEngines.class,
		SearchEngine.class,
		UserAgent.class, 
		NamedStyle.class, 
		SemanticAction.class,
		NestedSemanticAction.class,
		
		SemanticsSite.class,
		BasicSite.class,
		Argument.class,
		
		RegexFilter.class,
		
		CookieProcessing.class,
		
		MetaMetadataRepository.class, 
		
		
		MetaMetadataSelector.class,
		
		DefVar.class,	// cannot be in NestedSemanticActionsTranslationScope because these are collected separtely in MetaMetadataField
	};

	public static final TranslationScope BASE_TRANSLATIONS	= TranslationScope.get(BASE_NAME, BASE_CLASSES);

	public static final TranslationScope[]	SCOPE_SET	= 
	{
		BASE_TRANSLATIONS, 
		SemanticActionTranslationScope.get(),
		ConditionTranslationScope.get(),
	};

	public static TranslationScope get()
	{
		return TranslationScope.get(NAME, SCOPE_SET);
	}
}
