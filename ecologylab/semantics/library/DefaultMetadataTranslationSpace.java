/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.generic.Debug;
import ecologylab.xml.TranslationScope;

/**
 * 
 * 
 * @author Bharat Bandaru
 * 
 */
public class DefaultMetadataTranslationSpace extends Debug
{
	public static final String NAME = "defaultMetadataTranslationSpace";
	public static final String PACKAGE_NAME = "defaultMetadataTranslationSpace";
	
	protected static final Class TRANSLATIONS[] = 
	{
		Dlms.class,
		Document.class,
		Flickr.class,
		Icdl.class,
		IcdlImage.class,
		Image.class,
		Nsdl.class,
		Rss.class,
		Search.class,
		Text.class,
		Pdf.class
	};

	public static TranslationScope get()
	{
		return TranslationScope.get(PACKAGE_NAME, TRANSLATIONS);
	}
	
}
