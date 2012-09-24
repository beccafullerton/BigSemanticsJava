/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.TranslationContext;

/**
 * @author andruid
 *
 */
public class MetadataParsedURLScalarType extends
		MetadataScalarType<MetadataParsedURL, ParsedURL>
{

	public MetadataParsedURLScalarType()
	{
		super(MetadataParsedURL.class, ParsedURL.class, null, null);
	}

	@Override
	public MetadataParsedURL getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataParsedURL(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}
	
	@Override
	public String marshall(MetadataParsedURL instance, TranslationContext serializationContext)
	{
		return operativeScalarType().marshall(instance.getValue(), serializationContext);
	}

	@Override
	public boolean affordsInterestExpression()
	{
		return false;
	}
}