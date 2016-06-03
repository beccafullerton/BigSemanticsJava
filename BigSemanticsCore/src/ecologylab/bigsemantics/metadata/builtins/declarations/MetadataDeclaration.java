package ecologylab.bigsemantics.metadata.builtins.declarations;

/**
 * Automatically generated by MetaMetadataJavaTranslator
 *
 * DO NOT modify this code manually: All your changes may get lost!
 *
 * Copyright (2016) Interface Ecology Lab.
 */

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.bigsemantics.metadata.builtins.MetadataBuiltinsTypesScope;
import ecologylab.bigsemantics.metadata.mm_name;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metadata.semantics_mixin;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.namesandnums.SemanticsNames;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_descriptor_classes;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_scope;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 
 *The Metadata Class
 */ 
@simpl_descriptor_classes({MetadataClassDescriptor.class, MetadataFieldDescriptor.class})
@simpl_inherit
public class MetadataDeclaration
{
	/** 
	 *Stores the name of the meta-metadata, and is used on restoring from XML.
	 */ 
	@simpl_scalar
	private MetadataString metaMetadataName;

	@simpl_collection
	@simpl_scope("repository_metadata")
	@mm_name("mixins")
	@semantics_mixin
	private List<Metadata> mixins;

	@simpl_collection
	@simpl_scope("repository_metadata")
	@mm_name("linked_metadata_list")
	private List<Metadata> linkedMetadataList;

	public MetadataDeclaration()
	{ super(); }

	public MetadataString	metaMetadataName()
	{
		MetadataString	result = this.metaMetadataName;
		if (result == null)
		{
			result = new MetadataString();
			this.metaMetadataName = result;
		}
		return result;
	}

	public String getMetaMetadataName()
	{
		return this.metaMetadataName == null ? null : metaMetadataName().getValue();
	}

	public MetadataString getMetaMetadataNameMetadata()
	{
		return metaMetadataName;
	}

	public void setMetaMetadataName(String metaMetadataName)
	{
		if (metaMetadataName != null)
			this.metaMetadataName().setValue(metaMetadataName);
	}

	public void setMetaMetadataNameMetadata(MetadataString metaMetadataName)
	{
		this.metaMetadataName = metaMetadataName;
	}

	public List<Metadata> getMixins()
	{
		return mixins;
	}

  // lazy evaluation:
  public List<Metadata> mixins()
  {
    if (mixins == null)
      mixins = new ArrayList<Metadata>();
    return mixins;
  }

  // addTo:
  public void addToMixins(Metadata element)
  {
    mixins().add(element);
  }

  // size:
  public int mixinsSize()
  {
    return mixins == null ? 0 : mixins.size();
  }

	public void setMixins(List<Metadata> mixins)
	{
		this.mixins = mixins;
	}

	public List<Metadata> getLinkedMetadataList()
	{
		return linkedMetadataList;
	}

  // lazy evaluation:
  public List<Metadata> linkedMetadataList()
  {
    if (linkedMetadataList == null)
      linkedMetadataList = new ArrayList<Metadata>();
    return linkedMetadataList;
  }

  // addTo:
  public void addToLinkedMetadataList(Metadata element)
  {
    linkedMetadataList().add(element);
  }

  // size:
  public int linkedMetadataListSize()
  {
    return linkedMetadataList == null ? 0 : linkedMetadataList.size();
  }

	public void setLinkedMetadataList(List<Metadata> linkedMetadataList)
	{
		this.linkedMetadataList = linkedMetadataList;
	}
}
