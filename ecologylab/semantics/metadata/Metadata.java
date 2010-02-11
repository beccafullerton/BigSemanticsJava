package ecologylab.semantics.metadata;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.ClassAndCollectionIterator;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.OneLevelNestingIterator;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.model.text.CompositeTermVector;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.Term;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.xml.FieldDescriptor;
import ecologylab.xml.types.element.ArrayListState;

/**
 * This is the new metadata class that is the base class for the meta-metadata system. It contains
 * all the functionality of the previous Metadata, and MetadataField classes.
 * 
 * Classes will extend this base class to provide a nested metadata structure
 * 
 * @author sashikanth
 * 
 */
abstract public class Metadata extends MetadataBase<MetaMetadata>
{
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
	@xml_nested
	ArrayListState<Metadata>	mixins;

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
		// setupMetadataFieldAccessors();
	}

	public Metadata(MetaMetadata metaMetadata)
	{
		this.metaMetadata = metaMetadata;
		// setupMetadataFieldAccessors();
	}
	@Override
	public MetaMetadata metaMetadataField()
	{
		if (metaMetadata == null && repository() != null)
			metaMetadata		= repository().getByClass(getClass());
		return metaMetadata;
	}
	/**
	 * 
	 */
	ArrayListState<Metadata> mixins()
	{
		ArrayListState<Metadata> result = this.mixins;
		if (result == null)
		{
			result = new ArrayListState<Metadata>();
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
		attributeName = attributeName.toLowerCase();

		OneLevelNestingIterator<FieldDescriptor, ? extends MetadataBase> fullIterator = fullNonRecursiveIterator();
		while (fullIterator.hasNext())
		{
			FieldDescriptor fieldAccessor = fullIterator.next();
			MetadataBase currentMetadata = fullIterator.currentObject();
			// getFieldName() or getTagName()??? attributeName is from TypeTagNames.java
			if (attributeName.equals(fieldAccessor.getFieldName()))
			{
				String valueString = fieldAccessor.getValueString(currentMetadata);
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

		OneLevelNestingIterator<FieldDescriptor, ? extends MetadataBase> fullIterator = fullNonRecursiveIterator();
		while (fullIterator.hasNext())
		{
			FieldDescriptor fieldAccessor = fullIterator.next();
			MetadataBase currentMetadata = fullIterator.currentObject();
			MetaMetadata currentMetaMetadata = currentMetadata.getMetaMetadata();
			MetaMetadataField metaMetadata = (metaMetadataField != null) ? metaMetadataField
					.lookupChild(fieldAccessor) : (currentMetaMetadata != null) ? currentMetaMetadata
					.lookupChild(fieldAccessor) : null;

			// When the iterator enters the metadata in the mixins "this" in getValueString has to be
			// the corresponding metadata in mixin.
			boolean hasVisibleNonNullField = false;

			if (fieldAccessor.isPseudoScalar())
				hasVisibleNonNullField 	= MetadataString.isNotNullAndEmptyValue(fieldAccessor.getValueString(currentMetadata));
			else if (fieldAccessor.isNested())
			{
				Metadata nestedMetadata = (Metadata) fieldAccessor.getNested(currentMetadata);
				hasVisibleNonNullField 	= (nestedMetadata != null) ? (nestedMetadata.numberOfVisibleFields(metaMetadata) > 0) : false;
			}
			else
			{
				Collection collection 	= fieldAccessor.getCollection(currentMetadata);
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
		
		ClassAndCollectionIterator<FieldDescriptor, MetadataBase<?>> i = metadataIterator();
		while (i.hasNext())
		{
			MetadataBase m = i.next();
			if (m != null)
			{
				ITermVector mTermVector = m.termVector();
				if (m != null && !vectors.contains(mTermVector))
					termVector.add(mTermVector);
			}
		}
	}

	@Override
	protected void postTranslationProcessingHook()
	{
		initializeMetadataCompTermVector();
	}
	/**
	 * Sets the field to the specified value and wont rebuild composteTermVector
	 * 
	 * @param fieldName
	 * @param value
	 */
	// TODO -- May throw exception if there is no field accessor.
	public boolean set(String tagName, String value)
	{
		tagName = tagName.toLowerCase();
		// Taking care of mixins
		MetadataBase metadata = getMetadataWhichContainsField(tagName);

		if (value != null && value.length() != 0)
		{
			if (metadata != null)
			{
				FieldDescriptor fieldAccessor = get(tagName);
				if (fieldAccessor != null && value != null && value.length() != 0)
				{
					fieldAccessor.set(metadata, value);
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

	public boolean hwSet(String tagName, String value)
	{
		if (set(tagName, value))
		{
			// value is properly set.
			rebuildCompositeTermVector();
			return true;
		}
		return false;
	}

	/**
	 * Returns the metadata class if it contains a Field with name NOTE: Currently should be used ONLY
	 * for mixins
	 * 
	 * @param tagName
	 * @return
	 */
	// FIXME -- use fullNonRecursiveIterator
	public Metadata getMetadataWhichContainsField(String tagName)
	{
		HashMapArrayList<String, FieldDescriptor> fieldAccessors = metadataFieldDescriptors();

		FieldDescriptor metadataFieldAccessor = fieldAccessors.get(tagName);
		if (metadataFieldAccessor != null)
		{
			return this;
		}
		// The field may be in mixin
		if (mixins() != null && mixins().size() > 0)
		{
			for (Metadata mixinMetadata : mixins())
			{
				fieldAccessors = mixinMetadata.metadataFieldDescriptors();
				FieldDescriptor mixinFieldAccessor = fieldAccessors.get(tagName);
				if (mixinFieldAccessor != null)
				{
					return mixinMetadata;
				}
			}
		}
		return null;
	}

	// public void setMixinField(String tagName, String value)
	// {
	// tagName = tagName.toLowerCase();
	// Metadata metadata = getMetadataWhichhasField(tagName);
	//		
	// if(metadata != null)
	// {
	// HashMapArrayList<String, FieldAccessor> fieldAccessors =
	// Optimizations.getFieldAccessors(metadata.getClass());
	// FieldAccessor fieldAccessor = fieldAccessors.get(tagName);
	// if(fieldAccessor != null)
	// {
	// fieldAccessor.set(metadata, value);
	// }
	// else
	// {
	// System.out.println("No field Accessor");
	// //fieldAccessor.set(this, value);
	// }
	// }
	// }

	public Field getFields()
	{

		return null;
	}

	public MetaMetadataField childMetaMetadata(String name)
	{
		return metaMetadata == null ? null : metaMetadata.lookupChild(name);
	}

	@Override
	public MetaMetadata getMetaMetadata()
	{
		return metaMetadata;
	}

	@Override
	public void setMetaMetadata(MetaMetadata metaMetadata)
	{
		this.metaMetadata = metaMetadata;
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
		ClassAndCollectionIterator<FieldDescriptor, MetadataBase<?>> i = metadataIterator();
		while (i.hasNext())
		{
			MetadataBase m = i.next();
			if (m != null)
				tv.add(m.termVector());
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
	public ArrayListState<Metadata> getMixins()
	{
		return mixins();
	}

	/**
	 * @param mixins
	 *          the mixins to set
	 */
	public void setMixins(ArrayListState<Metadata> mixins)
	{
		this.mixins = mixins;
	}

	/**
	 * Efficiently retrieve appropriate MetadataFieldAccessor, using lazy evaluation.
	 * 
	 * @param fieldName
	 * @return
	 */
	public MetadataFieldDescriptor getMetadataFieldDescriptor(String fieldName)
	{
		return (MetadataFieldDescriptor) metadataFieldDescriptors().get(fieldName);
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
		ClassAndCollectionIterator<FieldDescriptor, MetadataBase<?>> iterator = metadataIterator();
		for(MetadataBase metadata = iterator.next(); metadata != null; metadata = iterator.next())
			metadata.recycle();
		super.recycle();
		termVector.recycle();
		
	}
	
	public boolean isRecycled()
	{
		return (termVector != null && termVector.isRecycled());
	}

	/**
	 * Take the mixins out of the collection, because they do not really provide direct access to the
	 * mixin fields. Instead, one uses fullNonRecursiveIterator() when that is desired.
	 */
	@Override
	protected HashMapArrayList<String, FieldDescriptor> computeFieldDescriptors()
	{
		HashMapArrayList<String, FieldDescriptor> result = super.computeFieldDescriptors();
		result.remove("mixins");
		return result;
	}

	/**
	 * Provides MetadataFieldAccessors for each of the ecologylab.xml annotated fields in this
	 * (probably a subclass), plus all the ecologylab.xml annotated fields in the mixins of this, if
	 * there are any.
	 */
	public OneLevelNestingIterator<FieldDescriptor, ? extends MetadataBase> fullNonRecursiveIterator()
	{
		return new OneLevelNestingIterator<FieldDescriptor, Metadata>(this, (mixins == null) ? null
				: mixins.iterator());
	}

	public ClassAndCollectionIterator<FieldDescriptor, MetadataBase<?>> metadataIterator()
	{
		return new ClassAndCollectionIterator<FieldDescriptor, MetadataBase<?>>(this);
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
	
}
