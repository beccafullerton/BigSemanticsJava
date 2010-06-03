package ecologylab.semantics.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ecologylab.generic.ClassAndCollectionIterator;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.OneLevelNestingIterator;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.CompositeTermVector;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.xml.ClassDescriptor;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldDescriptor;
import ecologylab.xml.ScalarUnmarshallingContext;
import ecologylab.xml.serial_descriptors_classes;
import ecologylab.xml.ElementState.xml_collection;

/**
 * This is the new metadata class that is the base class for the meta-metadata system. It contains
 * all the functionality of the previous Metadata, and MetadataField classes.
 * 
 * Classes will extend this base class to provide a nested metadata structure
 * 
 * @author sashikanth
 * 
 */
@serial_descriptors_classes({MetadataClassDescriptor.class, MetadataFieldDescriptor.class})
abstract public class Metadata extends ElementState
implements MetadataBase, Iterable<MetadataFieldDescriptor>
{
	/**
	 * Hidden reference to the MetaMetadataRepository. DO NOT access this field directly.
	 * DO NOT create a static public accessor.
	 * -- andruid 10/7/09.
	 */
	private static MetaMetadataRepository		repository;

	private static final String	MIXINS_FIELD_NAME	= "mixins";

	protected CompositeTermVector				termVector								= null;

	/**
	 * Seed object associated with this, if this is a seed.
	 */
	private Seed				seed;
	/**
	 * Allows combining instantiated Metadata subclass declarations without hierarchy.
	 * 
	 * Could help, for example, to support user annotation.
	 */
	@semantics_mixin
	@xml_collection("mixin")
	ArrayList<Metadata>									mixins;

	final static int					INITIAL_SIZE							= 5;

	/**
	 * Set to true if this cFMetadata object was restored from a saved collage. This is necessary to
	 * prevent cFMetadata from being added again and hence overwritting edited cFMetadata when the
	 * elements are recrawled on a restore.
	 */
	private boolean						loadedFromPreviousSession	= false;
	
	/**
	 * Indicates that this Container is a truly a seed, not just one
	 * that is associated into a Seed's inverted index.
	 */
	private boolean			isTrueSeed;
	
	/**
	 * Indicates that this Container is processed via drag and drop.
	 */
	private boolean			isDnd;
	
	public Metadata()
	{
		super();
	}

	public Metadata(MetaMetadata metaMetadata)
	{
		this();
//		this.metaMetadata = metaMetadata;
	}
	
	/**
	 * Don't exclude the mixins field from Metadata.
	 * 
	 * @param tagName
	 * 
	 * @return	true for the mixins field; otherwise false;
	 */
	public boolean excludeFieldByTag(String tagName)
	{
		return MIXINS_FIELD_NAME.equals(tagName);
	}
//	
	public MetadataClassDescriptor getMetadataClassDescriptor()
	{
		return (MetadataClassDescriptor) classDescriptor();
	}
	
	public MetaMetadata getMetaMetadata()
	{
		return getMetadataClassDescriptor().getMetaMetadata();
	}
	/**
	 * 
	 */
	ArrayList<Metadata> mixins()
	{
		ArrayList<Metadata> result = this.mixins;
		if (result == null)
		{
			result = new ArrayList<Metadata>();
			this.mixins = result;
		}
		return result;
	}

	public void addMixin(Metadata mixin)
	{
		if (mixin != null)
		{
			mixins().add(mixin);
			if (mixin.termVector() != null)
				termVector().add(mixin.termVector());
		}
	}

	public void removeMixin(Metadata mixin)
	{
		if (mixin != null)
		{
			if (mixins().remove(mixin) && mixin.termVector != null)
				termVector().remove(mixin.termVector);
		}
	}

	public boolean isFilled(String attributeName)
	{
		//FIXME -- toLowerCase() is BS!!!
		attributeName = attributeName.toLowerCase();

		OneLevelNestingIterator<MetadataFieldDescriptor, Metadata> fullIterator = fullNonRecursiveIterator();
		while (fullIterator.hasNext())
		{
			MetadataFieldDescriptor metadataFieldAccessor = fullIterator.next();
			Metadata currentMetadata = fullIterator.currentObject();
			// getFieldName() or getTagName()??? attributeName is from TypeTagNames.java
			if (attributeName.equals(metadataFieldAccessor.getFieldName()))
			{
				String valueString = metadataFieldAccessor.getValueString(currentMetadata);
				return MetadataString.isNotNullValue(valueString);
			}
		}
		return false;
	}

	/**
	 * @return the number of non-Null fields within this metadata
	 */
	public int size()
	{
		return numberOfVisibleFields(null);
	}

	public int numberOfVisibleFields(MetaMetadataField metaMetadataField)
	{
		int size = 0;

		OneLevelNestingIterator<MetadataFieldDescriptor, Metadata> fullIterator = fullNonRecursiveIterator();
		// iterate over all fields in this & then in each mixin of this
		while (fullIterator.hasNext())
		{
			MetadataFieldDescriptor metadataFieldDescriptor = fullIterator.next();
			Metadata currentMetadata = fullIterator.currentObject();	// stays the same for until we iterate over all mfd's for it
			MetaMetadata currentMetaMetadata = currentMetadata.getMetaMetadata();
			MetaMetadataField metaMetadata = (metaMetadataField != null) ? metaMetadataField
					.lookupChild(metadataFieldDescriptor) : (currentMetaMetadata != null) ? currentMetaMetadata
					.lookupChild(metadataFieldDescriptor) : null;

			// When the iterator enters the metadata in the mixins "this" in getValueString has to be
			// the corresponding metadata in mixin.
			boolean hasVisibleNonNullField = false;

			if (metadataFieldDescriptor.isPseudoScalar())
				hasVisibleNonNullField 	= MetadataString.isNotNullAndEmptyValue(metadataFieldDescriptor.getValueString(currentMetadata));
			else if (metadataFieldDescriptor.isNested())
			{
				Metadata nestedMetadata = (Metadata) metadataFieldDescriptor.getNested((MetadataBase) currentMetadata);
				hasVisibleNonNullField 	= (nestedMetadata != null) ? (nestedMetadata.numberOfVisibleFields(metaMetadata) > 0) : false;
			}
			else
			{
				Collection collection 	= metadataFieldDescriptor.getCollection(currentMetadata);
				hasVisibleNonNullField 	= (collection != null) ? (collection.size() > 0) : false;
			}

			// "null" happens with mixins fieldAccessor b'coz getValueString() returns "null".
			boolean isAlwaysShowAndNotHide = metaMetadata == null
					|| (metaMetadata.isAlwaysShow() || !metaMetadata.isHide());
			if (isAlwaysShowAndNotHide && hasVisibleNonNullField)
			{
				size++;
			}
		}
		return size;
	}

	public void rebuildTotally()
	{
		if (termVector != null)
			termVector.reinitialize();
		rebuildCompositeTermVector();
	}

	/**
	 * Rebuilds the composite TermVector from the individual TermVectors FIXME:Not able to move to the
	 * MetadataBase b'coz of mixins.
	 */
	@Override
	public void rebuildCompositeTermVector()
	{
		// if there are no metadatafields retain the composite termvector
		// because it might have meaningful entries
		if (termVector == null)
		{
			initializeMetadataCompTermVector();
			return;
		}

		Set<ITermVector> vectors = termVector.componentVectors();
		
		ClassAndCollectionIterator<MetadataFieldDescriptor, Metadata> i = metadataIterator();
		while (i.hasNext())
		{
			MetadataBase mb = i.next();
			if (mb != null)
			{
				// if mb is a Metadata object, this call may recursively initialize its CompositeTermVector
				ITermVector mTermVector = mb.termVector();
				if (mb != null && !vectors.contains(mTermVector))
					termVector.add(mTermVector);
			}
		}
	}

	@Override
	protected void postTranslationProcessingHook()
	{
		initializeMetadataCompTermVector();
	}

	public boolean hwSet(String tagName, String value)
	{
		if (setByTagName(tagName, value))
		{
			// value is properly set.
			rebuildCompositeTermVector();
			return true;
		}
		return false;
	}

	public Field getFields()
	{

		return null;
	}

	public void setMetaMetadata(MetaMetadata metaMetadata)
	{
		//FIXME -- get rid of all call sites for this method -- andruid 6/1/10
//		this.metaMetadata = metaMetadata;
	}

	@Override
	public CompositeTermVector termVector()
	{
		if (termVector == null)
			return initializeMetadataCompTermVector();
		return termVector;
	}

	// could get called twice if termVector() is called from different threads & termVector is null.
	public synchronized CompositeTermVector initializeMetadataCompTermVector()
	{
		if (termVector != null)
			return termVector;
		CompositeTermVector tv = new CompositeTermVector();
		ClassAndCollectionIterator<MetadataFieldDescriptor, Metadata> i = metadataIterator();
		while (i.hasNext())
		{
			MetadataBase mb = i.next();
			if (mb != null)
				tv.add(mb.termVector());
		}
		return (termVector = tv);
	}

	public boolean loadedFromPreviousSession()
	{
		return loadedFromPreviousSession;
	}

	public ParsedURL getLocation()
	{
		return null;
	}

	public void hwSetLocation(ParsedURL location)
	{
	}

	public void setLocation(ParsedURL location)
	{
	}

	public ParsedURL getNavLocation()
	{
		return null;
	}

	public void setNavLocation(ParsedURL navLocation)
	{
	}

	public void hwSetNavLocation(ParsedURL navLocation)
	{
	}

	/**
	 * @return the mixins
	 */
	public ArrayList<Metadata> getMixins()
	{
		return mixins();
	}

	/**
	 * @param mixins
	 *          the mixins to set
	 */
	public void setMixins(ArrayList<Metadata> mixins)
	{
		this.mixins = mixins;
	}


	// For adding mapped attributes
	public void add(String key)
	{

	}

	public Metadata get(int index)
	{
		return null;
	}

	public void recycle()
	{
		ClassAndCollectionIterator<MetadataFieldDescriptor, Metadata> iterator = metadataIterator();
		for(MetadataBase metadata = iterator.next(); metadata != null; metadata = iterator.next())
			metadata.recycle();
		super.recycle();
		termVector.recycle();
		termVector = null;
	}
	
	public boolean isRecycled()
	{
		return (termVector != null && termVector.isRecycled());
	}

	/**
	 * Provides MetadataFieldAccessors for each of the ecologylab.xml annotated fields in this
	 * (probably a subclass), plus all the ecologylab.xml annotated fields in the mixins of this, if
	 * there are any.
	 */
	public OneLevelNestingIterator<MetadataFieldDescriptor, Metadata> fullNonRecursiveIterator()
	{
		return new OneLevelNestingIterator<MetadataFieldDescriptor, Metadata>(this, (mixins == null) ? null	: mixins);
	}	

	public ClassAndCollectionIterator<MetadataFieldDescriptor, Metadata> metadataIterator()
	{
		return new ClassAndCollectionIterator<MetadataFieldDescriptor, Metadata>(this);
	}

	public boolean hasObservers()
	{
		return termVector != null && termVector.countObservers() > 0;
	}
	
	public boolean hasTermVector()
	{
		return termVector != null;
	}

	/**
	 * @return the seed
	 */
	public Seed getSeed()
	{
		return seed;
	}

	/**
	 * @param seed the seed to set
	 */
	public void setSeed(Seed seed)
	{
		this.seed = seed;
	}
	
	/**
	 * If this Container was a search, the index number of that search
	 * among the searches being aggregated at one time.
	 * Otherwise, -1.
	 * 
	 * @return	The search index number or -1 if not a search.
	 */
	public int searchNum()
	{
		if (isTrueSeed && (seed instanceof SearchState))
		{
			return ((SearchState) seed).searchNum();
		}
		return -1;
	}
	
	/**
	 * Called for true seed Containers.
	 * Calling this method does more than bind the Seed object with the Container in the model.
	 * It also sets the crucial isSeed flag, establishing that this Container is truly a Seed.
	 * <p/>
	 * NB: The seed object will also be bound with ancestors of the Container.
	 * 
	 * @param seed
	 */
	public void setAsTrueSeed(Seed seed)
	{
//		associateSeed(seed);
		this.seed			= seed;
		isTrueSeed		= true;
	}
	
	/**
	 * Indicate that this Container is being processed via DnD.
	 *
	 */
	void setDnd()
	{
		isDnd			= true;
	}
	public boolean isDnd()
	{
		return isDnd;
	}
	

	public static void setRepository(MetaMetadataRepository repo)
	{
		repository	= repo;
	}

	//FIXEME:The method has to search even all the mixins for the key.
	public MetadataFieldDescriptor getFieldDescriptorByTagName(String tagName)
	{
		return getMetaMetadata().getFieldDescriptorByTagName(tagName);
	}
	
	/**
	 * @return the metadataFieldDescriptorsByTagName
	 */
	public HashMapArrayList<String, MetadataFieldDescriptor> getMetadataFieldDescriptorsByTagName()
	{
		return getMetaMetadata().getMetadataFieldDescriptorsByTagName();
	}
	/**
	 * Sets the field to the specified value and wont rebuild composteTermVector
	 * 
	 * @param fieldName
	 * @param value
	 */
	// TODO -- May throw exception if there is no field accessor.
	// FIXME -- resolve with MetadataBase
//	public boolean setByTagName(String tagName, String value)
//	{
//		tagName = tagName.toLowerCase();
//		// Taking care of mixins
//		Metadata metadata = getMetadataWhichContainsField(tagName);
//
//		if (value != null && value.length() != 0)
//		{
//			if (metadata != null)
//			{
//				FieldDescriptor fieldAccessor = getFieldDescriptorByTagName(tagName);
//				if (fieldAccessor != null && value != null && value.length() != 0)
//				{
//					fieldAccessor.set(metadata, value);
//					return true;
//				}
//				else
//				{
//					debug("Not Able to set the field: " + tagName);
//					return false;
//				}
//			}
//		}
//		return false;
//	}
	
	public boolean setByTagName(String tagName, String value)
	{
		return setByTagName(tagName, value, null);
	}

	/**
	 * Unmarshall the valueString and set the field to 
	 * 
	 * @param tagName
	 * @param marshalledValue
	 * @param scalarUnMarshallingContext
	 * @return
	 */
	public boolean setByTagName(String tagName, String marshalledValue, ScalarUnmarshallingContext scalarUnMarshallingContext)
	{
		//FIXME -- why is this necessary???????????????????????
		if (marshalledValue != null && marshalledValue.length()!=0)
		{
			tagName = tagName.toLowerCase();	//FIXME -- get rid of this!
			MetadataFieldDescriptor fieldDescriptor = getFieldDescriptorByTagName(tagName);
			if(fieldDescriptor != null /* && value != null && value.length()!=0 */)	// allow set to nothing -- andruid & andrew 4/14/09
			{
				//FIXME -- override this method in MetadataFieldDescriptor!!!
				fieldDescriptor.set(this, marshalledValue, scalarUnMarshallingContext);
				return true;
			}
			else 
			{
				debug("Not Able to set the field: " + tagName);
			}
		}
		return false;
	}

		
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@Inherited
	public @interface semantics_mixin
	{

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
	@Override
	public boolean hasCompositeTermVector()
	{
		return termVector != null;
	}
	
	public Iterator<MetadataFieldDescriptor>	iterator()
	{
		return getMetadataFieldDescriptorsByTagName().iterator();
	}

}
