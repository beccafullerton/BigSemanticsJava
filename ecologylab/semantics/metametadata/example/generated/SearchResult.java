package ecologylab.semantics.metametadata.example.generated;

/**
This is a generated code. DO NOT edit or modify it.
 @author MetadataCompiler 

**/ 



import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBuiltinsTranslationScope;
import ecologylab.semantics.metadata.builtins.*;
import ecologylab.semantics.metadata.builtins.DebugMetadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Entity;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.Media;
import ecologylab.semantics.metadata.scalar.*;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.xml.ElementState.xml_tag;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.types.element.Mappable;
import ecologylab.xml.xml_inherit;
import java.util.*;


/**
	
**/ 

@xml_inherit

public class  SearchResult
extends  Document
{


/**
	The heading of search result
**/ 

	 @xml_leaf private MetadataString	heading;

/**
	The snippet of search result
**/ 

	 @xml_leaf private MetadataString	snippet;

/**
	The link of the search result
**/ 

	 @xml_leaf private MetadataParsedURL	link;

/**
	Constructor
**/ 

public SearchResult()
{
 super();
}

/**
	Constructor
**/ 

public SearchResult(MetaMetadata metaMetadata)
{
super(metaMetadata);
}

/**
	Lazy Evaluation for heading
**/ 

public MetadataString	heading()
{
MetadataString	result	=this.heading;
if(result == null)
{
result = new MetadataString();
this.heading	=	 result;
}
return result;
}

/**
	Gets the value of the field heading
**/ 

public String getHeading(){
return heading().getValue();
}

/**
	Sets the value of the field heading
**/ 

public void setHeading( String heading )
{
this.heading().setValue(heading);
}

/**
	The heavy weight setter method for field heading
**/ 

public void hwSetHeading( String heading )
{
this.heading().setValue(heading);
rebuildCompositeTermVector();
 }
/**
	 Sets the heading directly
**/ 

public void setHeadingMetadata(MetadataString heading)
{	this.heading = heading;
}
/**
	Heavy Weight Direct setter method for heading
**/ 

public void hwSetHeadingMetadata(MetadataString heading)
{	 if(this.heading!=null && this.heading.getValue()!=null && hasTermVector())
		 termVector().remove(this.heading.termVector());
	 this.heading = heading;
	rebuildCompositeTermVector();
}
/**
	Lazy Evaluation for snippet
**/ 

public MetadataString	snippet()
{
MetadataString	result	=this.snippet;
if(result == null)
{
result = new MetadataString();
this.snippet	=	 result;
}
return result;
}

/**
	Gets the value of the field snippet
**/ 

public String getSnippet(){
return snippet().getValue();
}

/**
	Sets the value of the field snippet
**/ 

public void setSnippet( String snippet )
{
this.snippet().setValue(snippet);
}

/**
	The heavy weight setter method for field snippet
**/ 

public void hwSetSnippet( String snippet )
{
this.snippet().setValue(snippet);
rebuildCompositeTermVector();
 }
/**
	 Sets the snippet directly
**/ 

public void setSnippetMetadata(MetadataString snippet)
{	this.snippet = snippet;
}
/**
	Heavy Weight Direct setter method for snippet
**/ 

public void hwSetSnippetMetadata(MetadataString snippet)
{	 if(this.snippet!=null && this.snippet.getValue()!=null && hasTermVector())
		 termVector().remove(this.snippet.termVector());
	 this.snippet = snippet;
	rebuildCompositeTermVector();
}
/**
	Lazy Evaluation for link
**/ 

public MetadataParsedURL	link()
{
MetadataParsedURL	result	=this.link;
if(result == null)
{
result = new MetadataParsedURL();
this.link	=	 result;
}
return result;
}

/**
	Gets the value of the field link
**/ 

public ParsedURL getLink(){
return link().getValue();
}

/**
	Sets the value of the field link
**/ 

public void setLink( ParsedURL link )
{
this.link().setValue(link);
}

/**
	The heavy weight setter method for field link
**/ 

public void hwSetLink( ParsedURL link )
{
this.link().setValue(link);
rebuildCompositeTermVector();
 }
/**
	 Sets the link directly
**/ 

public void setLinkMetadata(MetadataParsedURL link)
{	this.link = link;
}
/**
	Heavy Weight Direct setter method for link
**/ 

public void hwSetLinkMetadata(MetadataParsedURL link)
{	 if(this.link!=null && this.link.getValue()!=null && hasTermVector())
		 termVector().remove(this.link.termVector());
	 this.link = link;
	rebuildCompositeTermVector();
}
}

