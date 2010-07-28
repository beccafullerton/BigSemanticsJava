package ecologylab.semantics.metametadata.example.bingImage.generated;

/**
This is a generated code. DO NOT edit or modify it.
 @author MetadataCompiler 

**/ 



import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.*;
import ecologylab.semantics.generated.library.bing.*;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBuiltinsTranslationScope;
import ecologylab.semantics.metadata.builtins.*;
import ecologylab.semantics.metadata.builtins.DebugMetadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Entity;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.Media;
import ecologylab.semantics.metadata.scalar.*;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.Hint;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.element.Mappable;
import java.util.*;

@simpl_inherit

public class  Search
extends  Document
{

	 @simpl_collection("search_result") @simpl_nowrap @mm_name("search_results") private ArrayList<SearchResult>	searchResults;

/**
	Constructor
**/ 

public Search()
{
 super();
}

/**
	Constructor
**/ 

public Search(MetaMetadataCompositeField metaMetadata)
{
super(metaMetadata);
}

/**
	Lazy Evaluation for searchResults
**/ 

public  ArrayList<SearchResult>	searchResults()
{
 ArrayList<SearchResult>	result	=this.searchResults;
if(result == null)
{
result = new  ArrayList<SearchResult>();
this.searchResults	=	 result;
}
return result;
}

/**
	Set the value of field searchResults
**/ 

public void setSearchResults(  ArrayList<SearchResult> searchResults )
{
this.searchResults = searchResults ;
}

/**
	Get the value of field searchResults
**/ 

public  ArrayList<SearchResult> getSearchResults(){
return this.searchResults;
}

}

