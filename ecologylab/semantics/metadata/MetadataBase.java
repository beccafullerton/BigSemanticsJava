/**
 * 
 */
package ecologylab.semantics.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.OneLevelNestingIterator;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.xml.ClassDescriptor;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldDescriptor;
import ecologylab.xml.ScalarUnmarshallingContext;
import ecologylab.xml.types.element.ArrayListState;

/**
 * Base class for Metadata fields that represent scalar values.
 * 
 * These, for example, lack mixins.
 * 
 * @author andruid
 *
 */
public class MetadataBase<MM extends MetaMetadataField> extends ElementState implements Iterable<FieldDescriptor>
{

	HashMapArrayList<String, FieldDescriptor> metadataFieldDescriptors;

	/**
	 * Hidden reference to the MetaMetadataRepository. DO NOT access this field directly.
	 * DO NOT create a static public accessor.
	 * -- andruid 10/7/09.
	 */
	private static MetaMetadataRepository		repository;

	MM							metaMetadata;

	/**
	 * 
	 */
	public MetadataBase()
	{
		// TODO Auto-generated constructor stub
	}
	
	public static void setRepository(MetaMetadataRepository repo)
	{
		repository	= repo;
	}
	
	/**
	 * Only use this accessor, in order to maintain future code compatability.
	 * 
	 * @return
	 */
	public MetaMetadataRepository repository()
	{
		return repository;
	}
	/**
	 * This is actually the real composite term vector.
	 * 
	 * @return	Null for scalars.
	 */
	public ITermVector termVector()
	{
		return null;
	}



	public void recycle()
	{
		super.recycle();
		
		metadataFieldDescriptors = null;
	}
	

	/**
	 * Rebuilds the composite TermVector from the individual TermVectors, when there is one.
	 * This implementation, in the base class, does nothing.
	 */
	public void rebuildCompositeTermVector()
	{
		
	}

	/**
	 * Determine if the Metadata has any entries.
	 * @return	True if there are Metadata entries.
	 */
	public boolean hasCompositeTermVector()
	{
		return false;
	}
	
	/**
	 * Efficiently retrieve appropriate MetadataFieldDescriptor, using lazy evaluation.
	 * 
	 * @param fieldName
	 * @return
	 */
	public MetadataFieldDescriptor getMetadataFieldDescriptor(String fieldName)
	{
		return (MetadataFieldDescriptor) metadataFieldDescriptors().get(fieldName);
	}

	protected HashMapArrayList<String, FieldDescriptor> metadataFieldDescriptors()
	{
		HashMapArrayList<String, FieldDescriptor> result	= this.metadataFieldDescriptors;
		if (result == null)
		{
			result			= computeFieldDescriptors();
			result			= 
			metadataFieldDescriptors	= result;
		}
		return result;
	}


	protected HashMapArrayList<String, FieldDescriptor> computeFieldDescriptors()
	{
		return ClassDescriptor.getFieldDescriptors(this.getClass(), MetadataFieldDescriptor.class);
	}

	public MetaMetadataField metaMetadataField()
	{
		Metadata parent	= (Metadata) this.parent();
		return (parent == null) ? null : parent.metaMetadataField();
	}
	public Iterator<FieldDescriptor> iterator()
	{
		return metadataFieldDescriptors().iterator();
	}

	//FIXEME:The method has to search even all the mixins for the key.
	public FieldDescriptor get(String key)
	{
		HashMapArrayList<String, FieldDescriptor> fieldDescriptors = metadataFieldDescriptors();
		return fieldDescriptors.get(key);
	}
	public boolean set(String tagName, String value)
	{
		return set(tagName, value, null);
	}
	public boolean set(String tagName, String value, ScalarUnmarshallingContext scalarUnMarshallingContext)
	{
		tagName = tagName.toLowerCase();
		//Taking care of mixins
		MetadataBase metadata = getMetadataWhichContainsField(tagName);

		if(value != null && value.length()!=0)
		{
			if(metadata != null)
			{
				FieldDescriptor fieldDescriptor = get(tagName);
				if(fieldDescriptor != null /* && value != null && value.length()!=0 */)	// allow set to nothing -- andruid & andrew 4/14/09
				{
					fieldDescriptor.set(metadata, value, scalarUnMarshallingContext);
					return true;
				}
				else 
				{
					debug("Not Able to set the field: " + tagName);
					return false;
				}
			}
		}
		return false;
	}
	
	public MetadataBase getMetadataWhichContainsField(String tagName)
	{
		HashMapArrayList<String, FieldDescriptor> fieldDescriptors = metadataFieldDescriptors();
		
		FieldDescriptor metadataFieldDescriptor = fieldDescriptors.get(tagName);
		if (metadataFieldDescriptor != null)
		{
			return this;
		}
		//No mixins in MetadataBase.
//		if(mixins() != null && mixins().size() > 0)
//		{
//			for (Metadata mixinMetadata : mixins())
//			{
//				fieldAccessors 	= mixinMetadata.metadataFieldAccessors();
//				FieldAccessor mixinFieldAccessor 	= fieldAccessors.get(tagName);
//				if(mixinFieldAccessor != null)
//				{
//					return mixinMetadata;
//				}
//			}
//		}
		return null;
	}
	
	public boolean hwSet(String tagName, String value)
	{
		return set(tagName, value);
	}
		
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Inherited
    public @interface semantics_mixin
    {

    }
    
    public MetaMetadata getMetaMetadata()
    {
   	 return null;
    }
    
    public void setMetaMetadata(MetaMetadata metaMetadata)
    {
    	
    }

    public ArrayListState<Metadata> getMixins()
    {
   	 return null;
    }
    
 	/**
 	 * Provides MetadataFieldDescriptors for each of the ecologylab.xml annotated fields in this
 	 * (probably a subclass).
 	 */
 	public OneLevelNestingIterator<FieldDescriptor, ? extends MetadataBase> fullNonRecursiveIterator()
	{
		return new OneLevelNestingIterator<FieldDescriptor, MetadataBase<?>>(this, null);
	}

}
