/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.DefaultMetadataTranslationSpace;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.TranslationSpace;
import ecologylab.xml.xml_inherit;

/**
 * @author damaraju
 *
 */
public class MetaMetadata extends MetaMetadataField
{
	
	@xml_attribute 			String 		name;
	@xml_attribute private 	String 		urlBase;
	
	TranslationSpace 					translationSpace;

	public MetaMetadata()
	{
		super();
	}

	public boolean isSupported(ParsedURL purl)
	{
		return purl.toString().startsWith(urlBase);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}

	public TranslationSpace getTS() {
		return translationSpace;
	}

	public void setTS(TranslationSpace ts) {
		translationSpace = ts;
	}
	
	TranslationSpace DEFAULT_METADATA_TRANSLATIONS	= DefaultMetadataTranslationSpace.get();
	
	/**
	 * Lookup the Metadata class object that corresponds to the tag_name in this.
	 * @return
	 */
	public Class<? extends Metadata> getMetadataClass()
	{
		return (Class<? extends Metadata>) DEFAULT_METADATA_TRANSLATIONS.getClassByTag(name);
	}
	

	/**
	 * Lookup the Metadata class that corresponds to the (tag) name of this, using the DefaultMetadataTranslationSpace.
	 * Assuming that is found, use reflection to instantiate it.
	 * 
	 * @return	An instance of the Metadata subclass that corresponds to this, or null, if there is none.
	 */
	public Metadata newMetadata()
	{
		Metadata result	= null;
		Class<? extends Metadata> metadataClass	= getMetadataClass();
		if (metadataClass != null)
		{
			result		= ReflectionTools.getInstance(metadataClass);
			result.setMetaMetadata(this);
		}
		return result;
	}
}
