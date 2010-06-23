package ecologylab.semantics.metametadata;

import java.io.IOException;

import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.xml.XMLTools;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
@xml_tag("mm_nested_field")
public class MetaMetadataNestedField extends MetaMetadataCompositeField
{

	/**
	 * The type/class of metadata object.
	 */
	@xml_attribute
	protected String	type;

	@xml_attribute
	protected boolean	entity	= false;

	public MetaMetadataNestedField()
	{
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataNestedField(MetaMetadataField mmf)
	{
		this.name = mmf.name;
		this.extendsAttribute = mmf.extendsAttribute;
		this.hide = mmf.hide;
		this.alwaysShow = mmf.alwaysShow;
		this.style = mmf.style;
		this.layer = mmf.layer;
		this.xpath = mmf.xpath;
		this.navigatesTo = mmf.navigatesTo;
		this.shadows = mmf.shadows;
		this.stringPrefix = mmf.stringPrefix;
		this.isFacet = mmf.isFacet;
		this.ignoreInTermVector = mmf.ignoreInTermVector;
		this.comment = mmf.comment;
		this.dontCompile = mmf.dontCompile;
		this.key = mmf.key;
		this.textRegex = mmf.textRegex;
		this.matchReplacement = mmf.matchReplacement;
		this.contextNode = mmf.contextNode;
		this.tag = mmf.tag;
		this.ignoreExtractionError = mmf.ignoreExtractionError;
		this.kids = mmf.kids;
	}

	public MetaMetadataNestedField(MetaMetadataField copy, String name)
	{
		super(copy, name);
	}

	public String getType()
	{
		return type;
	}
	
	public boolean isEntity()
	{
		return entity;
	}
	
	public String getTypeOrName()
	{
		if (type != null)
			return type;
		else 
			return getName();
	}
	
	public String getTagForTranslationScope()
	{
		return entity == true ? DocumentParserTagNames.ENTITY : tag != null ? tag : name;
	}
	
	@Override
	protected void doAppending(Appendable appendable, int pass) throws IOException
	{
		appenedNestedMetadataField(appendable, pass);
	}

	/**
	 * Append method for Is_nested=true fields
	 * 
	 * @param appendable
	 * @throws IOException
	 */
	protected void appenedNestedMetadataField(Appendable appendable, int pass) throws IOException
	{
		String variableType = " @xml_nested " + XMLTools.classNameFromElementName(getTypeOrName());
		String fieldType = XMLTools.classNameFromElementName(getTypeOrName());
		if (isEntity())
		{
			variableType = " @xml_nested Entity<" + XMLTools.classNameFromElementName(getTypeOrName())
					+ ">";
			fieldType = "Entity<" + XMLTools.classNameFromElementName(getTypeOrName()) + ">";
		}
		if (pass == MetadataCompilerUtils.GENERATE_FIELDS_PASS)
		{
			appendable.append("\nprivate " + getTagDecl() + variableType + "\t" + name + ";");
		}
		else if (pass == MetadataCompilerUtils.GENERATE_METHODS_PASS)
		{
			appendLazyEvaluationMethod(appendable, getName(), fieldType);
			appendSetterForCollection(appendable, getName(), fieldType);
			appendGetterForCollection(appendable, getName(), fieldType);
		}
	}
	
	protected String getMetaMetadataTagToInheritFrom()
	{
		if (isEntity())
			return  DocumentParserTagNames.ENTITY;
		else if (type != null)
			return type;
		else
			return null;
//			return name;
	}

}
