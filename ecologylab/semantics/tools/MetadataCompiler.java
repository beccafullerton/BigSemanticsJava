/**
 * 
 */
package ecologylab.semantics.tools;

import java.io.File;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;

/**
 * @author andruid
 *
 */
public class MetadataCompiler extends ApplicationEnvironment
{

	/**
	 * @param applicationName
	 * @throws XMLTranslationException
	 */
	public MetadataCompiler(String applicationName)
			throws XMLTranslationException
	{
		super(applicationName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param applicationName
	 * @param translationSpace
	 * @param args
	 * @param prefsAssetVersion
	 * @throws XMLTranslationException
	 */
	public MetadataCompiler(String applicationName,
			TranslationScope translationSpace, String[] args,
			float prefsAssetVersion) throws XMLTranslationException
	{
		super(applicationName, translationSpace, args, prefsAssetVersion);
		// TODO Auto-generated constructor stub
	}

	static final TranslationScope META_METADATA_TRANSLATIONS = MetaMetadataTranslationScope.get();

	/**
	 * @param applicationName
	 * @param args
	 * @throws XMLTranslationException
	 */
	public MetadataCompiler(String[] args)
			throws XMLTranslationException
	{
		super("MetadataCompiler", META_METADATA_TRANSLATIONS, args, 1.0F);  

		String patternXMLFilepath = "../cf/config/semantics/metametadata/defaultRepository.xml";

//		ElementState.setUseDOMForTranslateTo(true);
		MetaMetadataRepository test;
		try
		{
			test = (MetaMetadataRepository) ElementState.translateFromXML(patternXMLFilepath, META_METADATA_TRANSLATIONS);

			String userDirProperty	= System.getProperty("user.dir");
			File outputRoot			= new File(userDirProperty);
			
			for (MetaMetadata metaMetadata : test.values())
			{
				metaMetadata.translateToMetadataClass(System.out, outputRoot);
				System.out.println('\n');
			}
		} catch (XMLTranslationException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * @param baseClass
	 * @param applicationName
	 * @param args
	 * @throws XMLTranslationException
	 */
	public MetadataCompiler(Class baseClass, String applicationName,
			String[] args) throws XMLTranslationException
	{
		super(baseClass, applicationName, args);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param baseClass
	 * @param applicationName
	 * @param translationSpace
	 * @param args
	 * @param prefsAssetVersion
	 * @throws XMLTranslationException
	 */
	public MetadataCompiler(Class baseClass, String applicationName,
			TranslationScope translationSpace, String[] args,
			float prefsAssetVersion) throws XMLTranslationException
	{
		super(baseClass, applicationName, translationSpace, args,
				prefsAssetVersion);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			new MetadataCompiler(args);
		} catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
