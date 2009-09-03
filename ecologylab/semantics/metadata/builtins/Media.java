package ecologylab.semantics.metadata.builtins;

/**
 * This is a generated code. DO NOT edit or modify it.
 * 
 * @author MetadataCompiler
 **/

import ecologylab.semantics.library.scalar.*;
import ecologylab.semantics.metadata.*;
import ecologylab.semantics.metadata.scalar.MetadataString;

import java.util.*;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.net.ParsedURL;
import ecologylab.generic.HashMapArrayList;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.types.element.Mappable;
import ecologylab.semantics.library.DefaultMetadataTranslationSpace;
import ecologylab.semantics.library.scholarlyPublication.*;
import ecologylab.semantics.library.uva.*;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.ElementState.xml_tag;

/**
 * The Media Class
 **/

@xml_inherit
@xml_tag("media")
public class Media extends Metadata
{

	/**
	 * Constructor
	 **/

	public Media()
	{
		super();
	}

	/**
	 * Constructor
	 **/

	public Media(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
	}

	/**
	
**/

	@xml_tag("context")
	@xml_nested
	private MetadataString	context;

	/**
	 * Lazy Evaluation for context
	 **/

	public MetadataString context()
	{
		MetadataString result = this.context;
		if (result == null)
		{
			result = new MetadataString();
			this.context = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field context
	 **/

	public String getContext()
	{
		return context().getValue();
	}

	/**
	 * Sets the value of the field context
	 **/

	public void setContext(String context)
	{
		this.context().setValue(context);
	}

	/**
	 * The heavy weight setter method for field context
	 **/

	public void hwSetContext(String context)
	{
		this.context().setValue(context);
		rebuildCompositeTermVector();
	}

	/**
	 * Sets the context directly
	 **/

	public void setContextMetadata(MetadataString context)
	{
		this.context = context;
	}

	/**
	 * Heavy Weight Direct setter method for context
	 **/

	public void hwSetContextMetadata(MetadataString context)
	{
		if (this.context != null && this.context.getValue() != null && hasTermVector())
			termVector().remove(this.context.termVector());
		this.context = context;
		rebuildCompositeTermVector();
	}
}
