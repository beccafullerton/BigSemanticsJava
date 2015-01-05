package ecologylab.bigsemantics.metadata.builtins.declarations.person;

/**
 * Automatically generated by MetaMetadataJavaTranslator
 *
 * DO NOT modify this code manually: All your changes may get lost!
 *
 * Copyright (2014) Interface Ecology Lab.
 */

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.MetadataBuiltinsTypesScope;
import ecologylab.bigsemantics.metadata.scalar.MetadataDate;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.namesandnums.SemanticsNames;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import java.lang.String;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** 
 *Date and place of birth for a person
 */ 
@simpl_inherit
public class BirthDetailDeclaration extends Metadata
{
	@simpl_scalar
	private MetadataDate birthDate;

	@simpl_scalar
	private MetadataString birthPlace;

	public BirthDetailDeclaration()
	{ super(); }

	public BirthDetailDeclaration(MetaMetadataCompositeField mmd) {
		super(mmd);
	}


	public MetadataDate	birthDate()
	{
		MetadataDate	result = this.birthDate;
		if (result == null)
		{
			result = new MetadataDate();
			this.birthDate = result;
		}
		return result;
	}

	public Date getBirthDate()
	{
		return this.birthDate == null ? null : birthDate().getValue();
	}

	public MetadataDate getBirthDateMetadata()
	{
		return birthDate;
	}

	public void setBirthDate(Date birthDate)
	{
		if (birthDate != null)
			this.birthDate().setValue(birthDate);
	}

	public void setBirthDateMetadata(MetadataDate birthDate)
	{
		this.birthDate = birthDate;
	}

	public MetadataString	birthPlace()
	{
		MetadataString	result = this.birthPlace;
		if (result == null)
		{
			result = new MetadataString();
			this.birthPlace = result;
		}
		return result;
	}

	public String getBirthPlace()
	{
		return this.birthPlace == null ? null : birthPlace().getValue();
	}

	public MetadataString getBirthPlaceMetadata()
	{
		return birthPlace;
	}

	public void setBirthPlace(String birthPlace)
	{
		if (birthPlace != null)
			this.birthPlace().setValue(birthPlace);
	}

	public void setBirthPlaceMetadata(MetadataString birthPlace)
	{
		this.birthPlace = birthPlace;
	}
}
