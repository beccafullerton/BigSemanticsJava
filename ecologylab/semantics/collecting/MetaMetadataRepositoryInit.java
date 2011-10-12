/**
 * 
 */
package ecologylab.semantics.collecting;

import java.io.File;

import ecologylab.appframework.ApplicationProperties;
import ecologylab.appframework.EnvironmentGeneric;
import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.io.Assets;
import ecologylab.io.AssetsRoot;
import ecologylab.io.Files;
import ecologylab.semantics.metadata.builtins.ClippableDocument;
import ecologylab.semantics.metadata.builtins.Clipping;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataRepositoryLoader;
import ecologylab.semantics.namesandnums.DocumentParserTagNames;
import ecologylab.semantics.namesandnums.SemanticsAssetVersions;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.Format;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;

/**
 * Initializes the MetaMetadataRepository, using its standard location in the /repository directory of the ecologylabSemantics project,
 * or its associated zip file in the Assets cache.
 * 
 * @author andruid
 */
public class MetaMetadataRepositoryInit extends Scope<Object>
implements DocumentParserTagNames, ApplicationProperties, SemanticsNames
{
	
	public static String												DEFAULT_REPOSITORY_LOCATION	= "../ecologylabSemantics/repository";

	public static Format												DEFAULT_REPOSITORY_FORMAT		= Format.XML;

	public static MetaMetadataRepositoryLoader	DEFAULT_REPOSITORY_LOADER		= new MetaMetadataRepositoryLoader();

	public static final String									SEMANTICS										= "semantics/";

	protected static File												METAMETADATA_REPOSITORY_DIR_FILE;

	protected static File												METAMETADATA_SITES_FILE;

	/**
	 * 
	 * The repository has the metaMetadatas of the document types. The repository is populated as the
	 * documents are processed.
	 */
	protected static MetaMetadataRepository			META_METADATA_REPOSITORY;

	public static MetaMetadata									DOCUMENT_META_METADATA;
	public static MetaMetadata									PDF_META_METADATA;
	public static MetaMetadata									SEARCH_META_METADATA;
	public static MetaMetadata									IMAGE_META_METADATA;
	public static MetaMetadata									DEBUG_META_METADATA;
	public static MetaMetadata									IMAGE_CLIPPING_META_METADATA;

	static
	{
		SimplTypesScope.graphSwitch	= GRAPH_SWITCH.ON;
		
		MetaMetadataRepository.initializeTypes();

	}
	
	public static MetaMetadataRepository getRepository()
	{
		return META_METADATA_REPOSITORY;
	}
	
	private MetaMetadataRepository	metaMetadataRepository;

	private SimplTypesScope				metadataTranslationScope;

	private final SimplTypesScope	generatedDocumentTranslations;

	private final SimplTypesScope	generatedMediaTranslations;

	private final SimplTypesScope	repositoryClippingTranslations;
	
	/**
	 * This constructor should only be called from SemanticsScope's constructor!
	 * 
	 * @param metadataTranslationScope
	 */
	protected MetaMetadataRepositoryInit(SimplTypesScope metadataTranslationScope)
	{
		if (SingletonApplicationEnvironment.isInUse() && !SingletonApplicationEnvironment.runningInEclipse())
		{
			AssetsRoot mmAssetsRoot = new AssetsRoot(
					EnvironmentGeneric.configDir().getRelative(SEMANTICS), 
					Files.newFile(PropertiesAndDirectories.thisApplicationDir(), SEMANTICS + "/repository")
					);
	
			METAMETADATA_REPOSITORY_DIR_FILE 	= Assets.getAsset(mmAssetsRoot, null, "repository", null, !USE_ASSETS_CACHE, SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
		}
		else
		{
			METAMETADATA_REPOSITORY_DIR_FILE 	= new File(DEFAULT_REPOSITORY_LOCATION);
		}
		
		Debug.println("\t\t-- Reading meta_metadata from " + METAMETADATA_REPOSITORY_DIR_FILE);
		
		META_METADATA_REPOSITORY 					= DEFAULT_REPOSITORY_LOADER.loadFromDir(METAMETADATA_REPOSITORY_DIR_FILE, DEFAULT_REPOSITORY_FORMAT);
		
		DOCUMENT_META_METADATA						= META_METADATA_REPOSITORY.getMMByName(DOCUMENT_TAG);
		PDF_META_METADATA									= META_METADATA_REPOSITORY.getMMByName(PDF_TAG);
		SEARCH_META_METADATA							= META_METADATA_REPOSITORY.getMMByName(SEARCH_TAG);
		IMAGE_META_METADATA								= META_METADATA_REPOSITORY.getMMByName(IMAGE_TAG);
		DEBUG_META_METADATA								= META_METADATA_REPOSITORY.getMMByName(DEBUG_TAG);
		IMAGE_CLIPPING_META_METADATA			= META_METADATA_REPOSITORY.getMMByName(IMAGE_CLIPPING_TAG);
		
		META_METADATA_REPOSITORY.bindMetadataClassDescriptorsToMetaMetadata(metadataTranslationScope);
		this.metadataTranslationScope	= metadataTranslationScope;
		this.metaMetadataRepository		= META_METADATA_REPOSITORY;
		this.generatedDocumentTranslations	= 
			metadataTranslationScope.getAssignableSubset(REPOSITORY_DOCUMENT_TRANSLATIONS, Document.class);
		this.generatedMediaTranslations	=
			metadataTranslationScope.getAssignableSubset(REPOSITORY_MEDIA_TRANSLATIONS, ClippableDocument.class);
		this.repositoryClippingTranslations =
			metadataTranslationScope.getAssignableSubset(REPOSITORY_CLIPPING_TRANSLATIONS, Clipping.class);
	}
	
	public MetaMetadataRepository getMetaMetadataRepository()
	{
		return metaMetadataRepository;
	}

	public SimplTypesScope getMetadataTranslationScope()
	{
		return metadataTranslationScope;
	}

	/**
	 * @return the generatedDocumentTranslations
	 */
	public SimplTypesScope getGeneratedDocumentTranslations()
	{
		return generatedDocumentTranslations;
	}


	/**
	 * @return the generatedMediaTranslations
	 */
	public SimplTypesScope getGeneratedMediaTranslations()
	{
		return generatedMediaTranslations;
	}
	
	/**
	 * @return the repositoryClippingTranslations
	 */
	public SimplTypesScope getRepositoryClippingTranslations()
	{
		return repositoryClippingTranslations;
	}
	
}
