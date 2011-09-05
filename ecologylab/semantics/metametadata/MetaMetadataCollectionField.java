package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.metadata.Metadata.mm_dont_inherit;
import ecologylab.semantics.metadata.Metadata.mm_name;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.FieldTypes;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.ScalarType;

@SuppressWarnings("rawtypes")
@simpl_inherit
@xml_tag("collection")
public class MetaMetadataCollectionField extends MetaMetadataNestedField
{

	public static final String		UNRESOLVED_NAME	= "&UNRESOLVED_NAME";

	@simpl_scalar
	protected String							childTag;

	/**
	 * The type for collection children.
	 */
	@simpl_scalar
	protected String							childType;

	@mm_dont_inherit
	@simpl_scalar
	protected String							childExtends;

	@simpl_scalar
	protected MetadataScalarType	childScalarType;

	/**
	 * Specifies adding @simpl_nowrap to the collection object in cases where items in the collection
	 * are not wrapped inside a tag.
	 */
	@simpl_scalar
	protected boolean							noWrap;

	/**
	 * for caching getTypeNameInJava().
	 */
	private String								typeNameInJava	= null;

	public MetaMetadataCollectionField()
	{
		
	}

	@Override
	protected Object clone()
	{
		MetaMetadataCollectionField cloned = new MetaMetadataCollectionField();
		cloned.setCloned(true);
		cloned.inheritAttributes(this);
		cloned.copyClonedFieldsFrom(this);
		
		this.cloneKidsTo(cloned);
		
		cloned.clonedFrom = this;
		return cloned;
	}

	public String getChildTag()
	{
		if (childTag != null)
			return childTag;
		if (childType != null)
			return childType;
		return null;
	}

	public String getChildType()
	{
		return childType;
	}
	
	public String getChildExtends()
	{
		return childExtends;
	}

	public ScalarType getChildScalarType()
	{
		return childScalarType;
	}

	public boolean isNoWrap()
	{
		return noWrap;
	}

	@Override
	public String getAdditionalAnnotationsInJava()
	{
		return "@" + mm_name.class.getSimpleName() + "(\"" + getName() + "\")";
	}

	@Override
	protected String getTypeNameInJava()
	{
		String rst = typeNameInJava;
		if (rst == null)
		{
			String className = null;
			if (this.getFieldType() == FieldTypes.COLLECTION_SCALAR)
			{
				className = this.getChildScalarType().getJavaClass().getSimpleName();
			}
			else
			{
				String typeName = getTypeName();
				className = XMLTools.classNameFromElementName(typeName);
			}
//			rst = "ArrayList<" + className + ">";
			rst = "List<" + className + ">";
			typeNameInJava = rst;
		}
		return typeNameInJava;
	}

	/**
	 * @return the tag
	 */
	public String resolveTag()
	{
		if (isNoWrap())
		{
			// is it sure that it will be a collection field?
//			String childTag = ((MetaMetadataCollectionField) this).childTag;
//			String childType = ((MetaMetadataCollectionField) this).childType;
			return (childTag != null) ? childTag : childType;
		}
		else
		{
			return (tag != null) ? tag : name;
		}
		// return (isNoWrap()) ? ((childTag != null) ? childTag : childType) : (tag != null) ? tag : name;
	}
	
	public String getTagForTranslationScope()
	{
		// FIXME: seems broken when rewriting collection xpath without re-indicating child_type
		return childType != null ? childType : tag != null ? tag : name;
	}

	@Override
	protected String getMetaMetadataTagToInheritFrom()
	{
		return childType != null ? childType : null;
	}
	
	@Override
	public HashMapArrayList<String, MetaMetadataField> getChildMetaMetadata()
	{
		return (kids != null && kids.size() > 0) ? kids.get(0).getChildMetaMetadata() : null;
	}
	
	public MetaMetadataCompositeField getChildComposite()
	{
		return (kids != null && kids.size() > 0) ? (MetaMetadataCompositeField) kids.get(0) : null;
	}
	
	/**
	 * Get the MetaMetadataCompositeField associated with this.
	 * 
	 * @return	this, because it is a composite itself.
	 */
	public MetaMetadataCompositeField metaMetadataCompositeField()
	{
		return getChildComposite();
	}
	
	@Override
	/**
	 * Each object in a collection of metadata require a specific MMdata composite object to be associated with them.
	 * This is unavailable in the MMD XML, and must be generated when the XML is read in.
	 */
	public void deserializationPostHook()
	{
		int typeCode = this.getFieldType();
		if (typeCode == FieldTypes.COLLECTION_SCALAR)
			return;
		
		String childType = getChildType();
		String childCompositeName = childType != null ? childType : UNRESOLVED_NAME;
		final MetaMetadataCollectionField thisField = this;
		MetaMetadataCompositeField composite = new MetaMetadataCompositeField(childCompositeName, kids)
		{
			@Override
			protected void typeChanged(String newType)
			{
				thisField.childType = newType;
			}

			@Override
			protected void extendsChanged(String newExtends)
			{
				thisField.childExtends = newExtends;
			}
			
			@Override
			protected void tagChanged(String newTag)
			{
				if (thisField.childTag == null)
					thisField.childTag = newTag;
			}
		};
		composite.setParent(this);
		composite.setType(childType);
		composite.setExtendsAttribute(this.childExtends);
		kids.clear();
		kids.put(composite.getName(), composite);
		composite.setPromoteChildren(this.shouldPromoteChildren());
	}

	public boolean isCollectionOfScalars()
	{
		return childScalarType != null || (this.getInheritedField() == null ? false : ((MetaMetadataCollectionField) this.getInheritedField()).isCollectionOfScalars());
	}

	protected void clearInheritFinishedOrInProgressFlag()
	{
		super.clearInheritFinishedOrInProgressFlag();
		if (this.getChildComposite() != null)
			this.getChildComposite().clearInheritFinishedOrInProgressFlag();
	}
	
	@Override
	protected void inheritMetaMetadataHelper()
	{
		/*
		 * the childComposite should hide all complexity between collection fields and composite fields,
		 * through hooks when necessary.
		 */
		int typeCode = this.getFieldType();
		switch (typeCode)
		{
		case FieldTypes.COLLECTION_ELEMENT:
		{
			// prepare childComposite: possibly new name, type, extends, tag and inheritedField
			MetaMetadataCompositeField childComposite = this.getChildComposite();
			if (childComposite.getName().equals(UNRESOLVED_NAME))
				childComposite.setName(this.childType == null ? this.name : this.childType);
			childComposite.type = this.childType; // here not using setter to reduce unnecessary re-assignment of this.childType
			childComposite.extendsAttribute = this.childExtends;
			childComposite.tag = this.childTag;
			childComposite.setRepository(this.getRepository());
			childComposite.setPackageName(this.packageName());
			
			MetaMetadataCollectionField inheritedField = (MetaMetadataCollectionField) this.getInheritedField();
			if (inheritedField != null)
				childComposite.setInheritedField(inheritedField.getChildComposite());
			childComposite.setDeclaringMmd(this.getDeclaringMmd());
			childComposite.setMmdScope(this.getMmdScope());

			childComposite.inheritMetaMetadata(); // inheritedMmd might be inferred from type/extends
			
			this.setInheritedMmd(childComposite.getInheritedMmd());
			this.setMmdScope(childComposite.getMmdScope());
			break;
		}
		case FieldTypes.COLLECTION_SCALAR:
		{
			MetaMetadataField inheritedField = this.getInheritedField();
			if (inheritedField != null)
				this.inheritAttributes(inheritedField);
			break;
		}
		}
	}

	@Override
	public MetadataFieldDescriptor findOrGenerateMetadataFieldDescriptor(TranslationScope tscope, MetadataClassDescriptor contextCd)
	{
		MetadataFieldDescriptor fd = this.getMetadataFieldDescriptor();
		if (fd == null)
		{
			String tagName = this.resolveTag();
			String fieldName = this.getFieldNameInJava(false);
			String javaTypeName = this.getTypeNameInJava();
			boolean wrapped = !this.isNoWrap();
			if (!wrapped)
				tagName = null;
			
			int typeCode = this.getFieldType();
			switch (typeCode)
			{
			case FieldTypes.COLLECTION_ELEMENT:
			{
				MetaMetadata inheritedMmd = this.getInheritedMmd();
				assert inheritedMmd != null : "IMPOSSIBLE: inheritedMmd == null: something wrong in the inheritance process!";
				inheritedMmd.findOrGenerateMetadataClassDescriptor(tscope);
				MetadataClassDescriptor fieldCd = inheritedMmd.getMetadataClassDescriptor();
				assert fieldCd != null : "IMPOSSIBLE: fieldCd == null: something wrong in the inheritance process!";
				fd = new MetadataFieldDescriptor(
						this,
						tagName,
						this.getComment(),
						typeCode,
						fieldCd,
						contextCd,
						fieldName,
						null,
						null,
						javaTypeName);
				break;
			}
			case FieldTypes.COLLECTION_SCALAR:
			{
				if (this.kids.size() > 0)
					warning("Ignoring nested fields inside " + this + " because child_scalar_type specified ...");
				
				ScalarType scalarType = this.getChildScalarType();
				fd = new MetadataFieldDescriptor(
						this,
						tagName,
						this.getComment(),
						typeCode,
						null,
						contextCd,
						fieldName,
						scalarType,
						null,
						javaTypeName);
			}
			}
			fd.setWrapped(wrapped);
		}
		this.metadataFieldDescriptor = fd;
		return fd;
	}
	
	@Override
	protected MetadataClassDescriptor bindMetadataClassDescriptor(TranslationScope metadataTScope)
	{
		MetaMetadataCompositeField childComposite = getChildComposite();
		if (childComposite != null)
			return childComposite.bindMetadataClassDescriptor(metadataTScope);
		return null;
	}

	@Override
	protected void customizeFieldDescriptor(TranslationScope metadataTScope, MetadataFieldDescriptor metadataFieldDescriptor)
	{
		super.customizeFieldDescriptor(metadataTScope, metadataFieldDescriptor);
		if (this.childTag != null)
			metadataFieldDescriptor.setCollectionOrMapTagName(this.childTag);
		metadataFieldDescriptor.setWrapped(!this.isNoWrap());
	}
	
	public void recursivelyRestoreChildComposite()
	{
		int typeCode = this.getFieldType();
		if (typeCode == FieldTypes.COLLECTION_SCALAR)
			return;
		
		if (kids != null && kids.size() == 1)
		{
			MetaMetadataCompositeField childComposite = getChildComposite();
			if (childComposite != null)
			{
				HashMapArrayList<String, MetaMetadataField> childsKids = childComposite.getChildMetaMetadata();
				this.setChildMetaMetadata(childsKids);
				if (childsKids != null)
					for (MetaMetadataField field : childsKids)
					{
						field.setParent(this);
						if (field instanceof MetaMetadataNestedField)
							((MetaMetadataNestedField) field).recursivelyRestoreChildComposite();
					}
			}
			return;
		}
		warning("collection field without a (correct) child composite: " + this);
	}

}
