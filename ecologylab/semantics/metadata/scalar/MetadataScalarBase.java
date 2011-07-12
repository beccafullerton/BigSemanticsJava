package ecologylab.semantics.metadata.scalar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.model.text.ITermVector;

abstract
public class MetadataScalarBase<T> implements MetadataBase
{
	protected T		value;
	
	public static final String	VALUE_FIELD_NAME	= "value";
	
	public MetadataScalarBase()
	{
		super();
	}
	
	public MetadataScalarBase(T value)
	{
		super();
		this.value	= value;
	}

	public T getValue()
	{
		return value;
	}
	
	public void setValue(T value)
	{
		this.value = value;
	}
	
	public String toString()
	{
		T value				= getValue();
		//return (value == null) ?
		//		super.toString() + "[null]" :
		//		super.toString() + "[" + value.toString() + "]";
		return (value == null) ? "null" : value.toString();
	}
	

//	public MetaMetadataField metaMetadataField()
//	{
//		Metadata parent	= (Metadata) this.parent();
//		return (parent == null) ? null : parent.getMetaMetadata();
//	}

	
	@Override
	public void rebuildCompositeTermVector()
	{
		
	}
	
	public boolean set(Object value)
	{
		setValue((T) value);
		return true;
	}
  
	public boolean hasCompositeTermVector()
	{
		return false;
	}
	
	/**
	 * In general, scalar fields, by type, should not contribute to the CompositeTermVector.
	 * The obvious exceptions are Strings.
	 */
	@Override
	public boolean ignoreInTermVector()
	{
		return true;
	}


 	
 	public static final ArrayList<MetadataFieldDescriptor>	EMPTY_FD_ARRAY_LIST	= new ArrayList<MetadataFieldDescriptor>(0);
 	
 	public static final Iterator<MetadataFieldDescriptor> EMPTY_FD_ITERATOR	= EMPTY_FD_ARRAY_LIST.iterator();
 	
 	public Iterator<MetadataFieldDescriptor> iterator()
 	{
 		return EMPTY_FD_ITERATOR;
 	}
 	
 	public void recycle()
 	{
 		
 	}
 	
	@Override
	public ITermVector termVector(HashSet<Metadata> visitedMetadata)
	{
		return termVector();
	}
}