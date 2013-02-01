package ecologylab.bigsemantics.metametadata;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

@simpl_inherit
public class MetaMetadataSelectorReselectField extends ElementState implements IMappable<String>
{

	@simpl_scalar
	private String	name;

	@simpl_scalar
	private String	value;

	public MetaMetadataSelectorReselectField()
	{
		super();
	}

	@Override
	public String key()
	{
		return name;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}

}
