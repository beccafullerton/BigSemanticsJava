/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import ecologylab.generic.HashMapArrayList;
import ecologylab.model.NamedStyle;
import ecologylab.net.ParsedURL;
import ecologylab.net.UserAgent;
import ecologylab.semantics.library.TypeTagNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.ElementState.xml_map;
import ecologylab.xml.types.element.HashMapState;

/**
 * @author damaraju
 * 
 */

public class MetaMetadataRepository extends ElementState implements PackageSpecifier, TypeTagNames
{
	private static final String	DEFAULT_STYLE_NAME	= "default";

	/**
	 * The name of the repository.
	 */
	@xml_attribute
	private String																	name;

	/**
	 * The package in which the class files have to be generated.
	 */
	@xml_tag("package")
	@xml_attribute
	private String																	packageName;

	@xml_nested 
	private HashMapState<String, UserAgent> 				userAgents; 
	
	@xml_nested
	private HashMapState<String, NamedStyle> 				namedStyles;
	
	private String																	defaultUserAgentString = null;

	/**
	 * The keys for this hashmap are the values within TypeTagNames.
	 */
	@xml_map("meta_metadata")
	private HashMapArrayList<String, MetaMetadata>	repositoryByTagName;
	
	private HashMap<String, MetaMetadata>						repositoryByURL = null;

	static final TranslationScope										TS	= MetaMetadataTranslationScope.get();

	private static HashMap purlMetadataMap = new HashMap();
	
	private TranslationScope												metadataTScope;

	// for debugging
	protected static File														REPOSITORY_FILE;

	static{
		purlMetadataMap.put("http://portal.acm.org/citations","acm_portal");
	}
	public static void main(String args[])
	{
		REPOSITORY_FILE = new File(
				/* PropertiesAndDirectories.thisApplicationDir(), */"H:\\web\\code\\java\\cf\\config\\semantics\\metametadata\\defaultRepository.xml");
		MetaMetadataRepository metaMetaDataRepository = load(REPOSITORY_FILE);
		try
		{
			metaMetaDataRepository.writePrettyXML(System.out);
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static MetaMetadataRepository load(File file)
	{
		MetaMetadataRepository result = null;
		try
		{
			result = (MetaMetadataRepository) ElementState.translateFromXML(file, TS);
			// result.populateURLBaseMap();
			// // necessary to get, for example, fields for document into pdf...
			// result.populateInheritedValues();
			//			
			// result.populateMimeMap();
			// For debug
			// this.metaMetaDataRepository.writePrettyXML(System.out);
		}
		catch (XMLTranslationException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Recursively Copying MetadataFields from srcMetaMetadata to destMetaMetadata.
	 * 
	 * @param destMetaMetadata
	 * @param srcMetaMetadata
	 */
	protected void recursivePopulate(MetaMetadata destMetaMetadata)
	{
		// recursivePopulate(destMetaMetadata, destMetaMetadata.getExtendsClass());
	}

	/**
	 * Get MetaMetadata by ParsedURL if possible. If that lookup fails, then lookup by tag name, to
	 * acquire some default.
	 * 
	 * @param purl
	 * @param tagName
	 * @return
	 */
	public MetaMetadata get(ParsedURL purl, String tagName)
	{
		MetaMetadata result = getByPURL(purl);
		return (result != null) ? result : getByTagName(tagName);
	}

	/**
	 * Find the best matching MetaMetadata for the ParsedURL -- if there is a match.
	 * TODO implement a better URL pattern matcher here
	 * @param parsedURL
	 * @return appropriate MetaMetadata, or null.
	 */
	public MetaMetadata getByPURL(ParsedURL parsedURL)
	{
		if (repositoryByURL == null)
		{
			repositoryByURL = new HashMap<String, MetaMetadata>();
			
			for (MetaMetadata metaMetadata : repositoryByTagName)
			{
				ParsedURL purl = metaMetadata.getUrlBase();
				if (purl != null)
					repositoryByURL.put(purl.host(), metaMetadata);
			}
		}
		
		return (parsedURL == null) ? null : repositoryByURL.get(parsedURL.host());
	}

	public MetaMetadata getByTagName(String tagName)
	{

		return (tagName == null) ? null : repositoryByTagName.get(tagName);
	}

	public Collection<MetaMetadata> values()
	{
		return (repositoryByTagName == null) ? null : repositoryByTagName.values();
	}

	public String packageName()
	{
		return packageName;
	}

	@Override
	protected void postTranslationProcessingHook()
	{

	}

	public static String documentTag()
	{
		return DOCUMENT_TAG;
	}

	public MetaMetadata getByClass(Class<? extends Metadata> thatClass)
	{
		String tag = metadataTScope.lookupTag(thatClass);

		return (tag == null) ? null : repositoryByTagName.get(tag);
	}

	public MetaMetadata getByMetadata(Metadata metadata)
	{
		ParsedURL purl = metadata.getLocation();

		MetaMetadata result = getByPURL(purl);
		if (result == null)
			result = getByClass(metadata.getClass());
		return result;
	}

	public MetaMetadata lookupByMime(String mimeType)
	{
		return null;
	}

	public MetaMetadata lookupBySuffix(String suffix)
	{
		return null;
	}

	public TranslationScope translationScope()
	{
		return TS;
	}

	public void setMetadataTranslationScope(TranslationScope metadataTScope)
	{
		this.metadataTScope = metadataTScope;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName()
	{
		return packageName;
	}

	/**
	 * @param packageName
	 *          the packageName to set
	 */
	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}
	
	public NamedStyle lookupStyle(String styleName)
	{
		return namedStyles.get(styleName);
	}
	
	public NamedStyle getDefaultStyle()
	{
		return namedStyles.get(DEFAULT_STYLE_NAME);
	}
	
	public HashMapState<String, UserAgent> userAgents()
	{
		if (userAgents == null)
			userAgents = new HashMapState<String, UserAgent>();
		
		return userAgents;
		
	}
	
	public String getUserAgentString(String name)
	{
		return userAgents().get(name).userAgentString();
	}

	public String getDefaultUserAgentString()
	{
		if (defaultUserAgentString == null)
		{
			for(UserAgent userAgent : userAgents().values())
			{
				if (userAgent.isDefaultAgent())
				{
					defaultUserAgentString = userAgent.userAgentString();
					break;
				}
			}
		}
		
		return defaultUserAgentString;
	}
}
