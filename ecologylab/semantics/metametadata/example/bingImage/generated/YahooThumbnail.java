package ecologylab.semantics.metametadata.example.bingImage.generated;

/**
This is a generated code. DO NOT edit or modify it.
 @author MetadataCompiler 

**/ 



import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataInteger;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.Hint;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit

public class  YahooThumbnail
extends  Metadata
{

	@xml_tag("Url")@simpl_scalar @simpl_hints(Hint.XML_LEAF) private MetadataParsedURL	thumbUrl;
	@xml_tag("Width")@simpl_scalar @simpl_hints(Hint.XML_LEAF) private MetadataInteger	width;
	@xml_tag("Height")@simpl_scalar @simpl_hints(Hint.XML_LEAF) private MetadataInteger	height;

/**
	Constructor
**/ 

public YahooThumbnail()
{
 super();
}

/**
	Constructor
**/ 

public YahooThumbnail(MetaMetadataCompositeField metaMetadata)
{
super(metaMetadata);
}

/**
	Lazy Evaluation for thumbUrl
**/ 

public MetadataParsedURL	thumbUrl()
{
MetadataParsedURL	result	=this.thumbUrl;
if(result == null)
{
result = new MetadataParsedURL();
this.thumbUrl	=	 result;
}
return result;
}

/**
	Gets the value of the field thumbUrl
**/ 

public ParsedURL getThumbUrl(){
return thumbUrl().getValue();
}

/**
	Sets the value of the field thumbUrl
**/ 

public void setThumbUrl( ParsedURL thumbUrl )
{
this.thumbUrl().setValue(thumbUrl);
}

/**
	The heavy weight setter method for field thumbUrl
**/ 

public void hwSetThumbUrl( ParsedURL thumbUrl )
{
this.thumbUrl().setValue(thumbUrl);
rebuildCompositeTermVector();
 }
/**
	 Sets the thumbUrl directly
**/ 

public void setThumbUrlMetadata(MetadataParsedURL thumbUrl)
{	this.thumbUrl = thumbUrl;
}
/**
	Heavy Weight Direct setter method for thumbUrl
**/ 

public void hwSetThumbUrlMetadata(MetadataParsedURL thumbUrl)
{	 if(this.thumbUrl!=null && this.thumbUrl.getValue()!=null && hasTermVector())
		 termVector().remove(this.thumbUrl.termVector());
	 this.thumbUrl = thumbUrl;
	rebuildCompositeTermVector();
}
/**
	Lazy Evaluation for width
**/ 

public MetadataInteger	width()
{
MetadataInteger	result	=this.width;
if(result == null)
{
result = new MetadataInteger();
this.width	=	 result;
}
return result;
}

/**
	Gets the value of the field width
**/ 

public Integer getWidth(){
return width().getValue();
}

/**
	Sets the value of the field width
**/ 

public void setWidth( Integer width )
{
this.width().setValue(width);
}

/**
	The heavy weight setter method for field width
**/ 

public void hwSetWidth( Integer width )
{
this.width().setValue(width);
rebuildCompositeTermVector();
 }
/**
	 Sets the width directly
**/ 

public void setWidthMetadata(MetadataInteger width)
{	this.width = width;
}
/**
	Heavy Weight Direct setter method for width
**/ 

public void hwSetWidthMetadata(MetadataInteger width)
{	 if(this.width!=null && this.width.getValue()!=null && hasTermVector())
		 termVector().remove(this.width.termVector());
	 this.width = width;
	rebuildCompositeTermVector();
}
/**
	Lazy Evaluation for height
**/ 

public MetadataInteger	height()
{
MetadataInteger	result	=this.height;
if(result == null)
{
result = new MetadataInteger();
this.height	=	 result;
}
return result;
}

/**
	Gets the value of the field height
**/ 

public Integer getHeight(){
return height().getValue();
}

/**
	Sets the value of the field height
**/ 

public void setHeight( Integer height )
{
this.height().setValue(height);
}

/**
	The heavy weight setter method for field height
**/ 

public void hwSetHeight( Integer height )
{
this.height().setValue(height);
rebuildCompositeTermVector();
 }
/**
	 Sets the height directly
**/ 

public void setHeightMetadata(MetadataInteger height)
{	this.height = height;
}
/**
	Heavy Weight Direct setter method for height
**/ 

public void hwSetHeightMetadata(MetadataInteger height)
{	 if(this.height!=null && this.height.getValue()!=null && hasTermVector())
		 termVector().remove(this.height.termVector());
	 this.height = height;
	rebuildCompositeTermVector();
}
}

