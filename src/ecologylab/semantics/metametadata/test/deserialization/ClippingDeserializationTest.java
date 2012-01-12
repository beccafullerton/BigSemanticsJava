/**
 * 
 */
package ecologylab.semantics.metametadata.test.deserialization;

import java.io.File;

import ecologylab.generic.Debug;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.cyberneko.CybernekoWrapper;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * @author andruid
 *
 */
public class ClippingDeserializationTest extends Debug
implements SemanticsNames
{
	static
	{
		MetaMetadataRepository.initializeTypes();
	}
	private static final SimplTypesScope	META_METADATA_TRANSLATIONS	= RepositoryMetadataTranslationScope.get();

	private static final SimplTypesScope MY_TRANSLATIONS	= SimplTypesScope.get("mine",
			META_METADATA_TRANSLATIONS, InformationCompositionTest.class);
	/**
	 * 
	 */
	public ClippingDeserializationTest()
	{
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] s)
	{
		File file2	= new File("ecologylab/semantics/metametadata/test/deserialization/imageClipping.xml");

		File file		= new File("ecologylab/semantics/metametadata/test/deserialization/textClipping.xml");
		
		SemanticsSessionScope sss	= new SemanticsSessionScope(META_METADATA_TRANSLATIONS, CybernekoWrapper.class);
		
		try
		{
//			ElementState es	= MY_TRANSLATIONS.deserialize(file);
//
//			es.serialize(System.out);
			
			ElementState es2	= (ElementState) MY_TRANSLATIONS.deserialize(file2, Format.XML);

			SimplTypesScope.serialize(es2, System.out, StringFormat.XML);

		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
