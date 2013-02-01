package ecologylab.bigsemantics.seeding;

import java.io.File;
import java.util.ArrayList;

import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.bigsemantics.namesandnums.CFPrefNames;
import ecologylab.bigsemantics.namesandnums.SemanticsAssetVersions;
import ecologylab.io.Assets;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.formatenums.Format;

/**
 * A collection + registry of SeedSets.
 *
 * @author andruid
 */
public @simpl_inherit class CuratedSeedSets extends ElementState
implements CFPrefNames
{
	@simpl_collection
	@simpl_nowrap
	@simpl_scope(BaseSeedTranslations.TSCOPE_NAME)
	ArrayList<SeedSet>									arrayList;
	
	static CuratedSeedSets	singleton;
	
	static SimplTypesScope TSCOPE	= SimplTypesScope.get("curated_seed_sets", BaseSeedTranslations.get(), CuratedSeedSets.class);
	public CuratedSeedSets()
	{
		super();
	}
	
	//TODO -- don't use getElementStateById; instead use @simpl_map
	public static SeedSet lookup(String id)
	{
		if (singleton == null)
			init();
		return (SeedSet) singleton.getElementStateById(id);
	}

	private static void init()
	{
		File seedingsFile	= Assets.getAsset(SemanticsAssetVersions.SEMANTICS_ASSETS_ROOT, CFPrefNames.CURATED + "/curated_seed_sets.xml");
		if (!SingletonApplicationEnvironment.runningInEclipse())
			Assets.updateAssetsXml("CuratedSeedSets.init()");
		try
		{
			CuratedSeedSets cs		= (CuratedSeedSets) TSCOPE.deserialize(seedingsFile, Format.XML);
			singleton				= cs;
		} catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}
	
	static CuratedSeedSets all()
	{
		if (singleton == null)
			init();
		return singleton;
	}
}
