package ecologylab.semantics.metadata;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.model.text.TermVector;
import ecologylab.model.text.WordForms;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.types.element.ArrayListState;

/**
 * This is the new metadata class that is the base class for the 
 * meta-metadata system. It contains all the functionality of the 
 * previous Metadata, and MetadataField classes.
 * 
 * Classes will extend this base class to provide a nested metadata structure
 * 
 * @author sashikanth
 *
 */
abstract public class Metadata extends MetadataBase
{
	MetaMetadata 							metaMetadata;
	
	/**
	 * Allows combining sinstantiated Metadata subclass declarations without hierarchy.
	 * 
	 * Could help, for example, to support user annotation.
	 */
	@xml_nested	ArrayListState<Metadata>	mixins;
	
	final static int						INITIAL_SIZE				= 5;

	/**
	 * Set to true if this cFMetadata object was restored from a saved collage.
	 * This is necessary to prevent cFMetadata from being added again and hence
	 * overwritting edited cFMetadata when the elements are recrawled on a restore.
	 */
	private boolean 						loadedFromPreviousSession 	= false;
	
	public Metadata()
	{
//		setupMetadataFieldAccessors();
	}
	
	public Metadata(MetaMetadata metaMetadata)
	{
		this.metaMetadata		= metaMetadata;
//		setupMetadataFieldAccessors();
	}
	
//	public Metadata(boolean createTermVector)
//	{
//		if(createTermVector)
//			compositeTermVector = new TermVector();
//	}
	/**
	 * 
	 */
	ArrayListState<Metadata> mixins()
	{
		ArrayListState<Metadata> result = this.mixins;
		if(result == null)
		{
			result = new ArrayListState<Metadata>();
			this.mixins = result;
		}
		return result;
	}
	
	public TermVector termVector()
	{
		return compositeTermVector;
	}
   
   /**
    * Increments interest in this MetadataField. 
    * @param delta	The change in interest.
    */
	public void incrementParticipantInterest(short delta)
	{
		participantInterest.increment(delta);
	}
	
	public void recycle()
	{
		//termVector.clear();
		compositeTermVector 				= null;
		participantInterest					= null;
	}
	
	public void addMixin(Metadata mixin)
	{
		if(mixin != null)
		{
			mixins().add(mixin);
		}
	}
	
	/**
    * Append the term to the field "value" as well as the term vector.
    * 
    * @param wf The term to add.
    */
	public void addTerm(WordForms wf)
	{
		compositeTermVector.add(wf);
		   
		//value = null; //force value to be updated later
		   
		//if (termVector.size() > 0)
		   
		//this concat is way cheaper than rebuilding the termvector toString().
		//value			= value.toString() + ' ' + wf.string();
		   
		//this.value = termVector.toString();
		//this.value = termVector.toString(termVector.size(), ' ');
		//else
		//	   this.value = "";
		
		compositeTermVector.combine(compositeTermVector, false);
	}
	   
	/**
	* Append terms to the field "value" as well as the term vector.
	* 
	* @param wfv The vector of terms to add.
	*/
	public void addTerm(TermVector wfv)
	{
		Iterator it = wfv.iterator();
		while (it.hasNext())
		{ 
			WordForms wf = (WordForms) it.next();
			compositeTermVector.add(wf);
		}
	}
	
	/**
	 * Modifies interest by delta. This function modifies the interest
	 * in the composite TermVector, the constituent individual TermVectors,
	 * and the interest in actual fields themselves (for the semantic web/DLs) 
	 * 
	 * @param delta		The amount to modify interest
	 */
	public void incrementInterest(short delta)
	{
		if(compositeTermVector != null && !compositeTermVector.isEmpty())
		{
			compositeTermVector.incrementParticipantInterest(delta);
			incrementParticipantInterest(delta);
		}
//		if(compositeTermVector != null && !compositeTermVector.isEmpty())
//		{
//			// first modify the composite TermVector
//			compositeTermVector.incrementParticipantInterest(delta);
//			
//			//TODO Sashikanth: iterate on child fields
//			Iterator it = iterator();
//			while (it.hasNext())
//			{
//				
//				// TermVectors
//				Metadata mData = (Metadata) it.next();
//				mData.termVector().incrementParticipantInterest(delta);
//				
//				// Lastly the actual fields
//				mData.incrementParticipantInterest(delta);
//			}
//		}
		
		
	}
	
	/**
	 * This is going to return a Iterator of <code>FieldAccessor</code>. Uses lazy evaluation :-)
	 * 
	 * @return	The HashMap Iterator.
	 */
	public Iterator<FieldAccessor> fieldAccessorIterator()
	{
		return metadataFieldAccessors().iterator();
	}
		
	/**
	 * Initializes the data termvector structure. This is not added to the individual
	 * fields (so that it can be changed) but is added to the composite term vector.
	 * If the data termvector has already been initialized, this operation will replace
	 * the old one and rebuild the composite term vector.
	 * 
	 * @param initialTermVector The initial set of terms
	 */
	public void initializeTermVector(TermVector initialTermVector)
	{
		//System.out.println("Initializing TermVector. size is " + this.size());
		
		if (compositeTermVector != null)
		{
//			dataTermVector = initialTermVector;
			
			//initialize the composite TermVector
			rebuildCompositeTermVector();
		}
//		if there is no cFMetadata then add to the composite TermVector
		else
		{
			compositeTermVector = initialTermVector;
		}
		
		// change from vikram's semantic branch
		//unscrapedTermVector.addAll(termVector);
	}

	public boolean isFilled(String attributeName)
	{
		attributeName = attributeName.toLowerCase();
		Iterator<FieldAccessor> fieldIterator = fieldAccessorIterator();
		while(fieldIterator.hasNext())
		{
			FieldAccessor fieldAccessor = fieldIterator.next();
			// getFieldName() or getTagName()??? attributeName is from TypeTagNames.java
			if(attributeName.equals(fieldAccessor.getFieldName()))
			{
				String valueString = fieldAccessor.getValueString(this);
				return (valueString != null && valueString != "null");
			}
		}
		
		//Supporting Mixins
		if(mixins() != null && mixins().size() > 0)
		{
			Iterator<Metadata> metadataIterator = mixins().iterator();
			while(metadataIterator.hasNext())
			{
				Metadata metadata = metadataIterator.next();
				fieldIterator = metadata.fieldAccessorIterator();
				while(fieldIterator.hasNext())
				{
					FieldAccessor fieldAccessor = fieldIterator.next();
					String valueString = fieldAccessor.getValueString(metadata);
					if(valueString != null && valueString != "null")
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
		}
		
		return false;
	}
	
	public int size() 
	{
		// TODO Sashikanth: Use Reflection to get the number of fields 
		//of the instantiated metadata object
		int size = 0;
		
		

		Iterator<FieldAccessor> fieldIterator = fieldAccessorIterator();
		while(fieldIterator.hasNext())
		{
			FieldAccessor fieldAccessor = fieldIterator.next();
			String valueString = fieldAccessor.getValueString(this);
			if(valueString != null && valueString != "null")
			{
//				System.out.println("field:"+fieldAccessor.getFieldName()+ " value:"+valueString);
				size++;
			}
		}
		
		//Supporting Mixins -- Not used as yet but these are working fine.
		if(mixins() != null && mixins().size() > 0)
		{
			Iterator<Metadata> metadataIterator = mixins().iterator();
			while(metadataIterator.hasNext())
			{
				Metadata metadata = metadataIterator.next();
				fieldIterator = metadata.fieldAccessorIterator();
				while(fieldIterator.hasNext())
				{
					FieldAccessor fieldAccessor = fieldIterator.next();
					String valueString = fieldAccessor.getValueString(metadata);
					if(valueString != null && valueString != "null")
					{
//						System.out.println("field:"+fieldAccessor.getFieldName()+ " value:"+valueString);
						size++;
					}
				}
			}
		}
		
		return size;

	}
	
	/**
	 * Determine if the Metadata has any entries.
	 * @return	True if there are Metadata entries.
	 */
	public boolean hasCompositeTermVector()
	{
		return (compositeTermVector != null);
	}
	/**
	 * Rebuilds the composite TermVector from the individual TermVectors
	 */
	public void rebuildCompositeTermVector()
	{
		//if there are no metadatafields retain the composite termvector
		//because it might have meaningful entries

		if (compositeTermVector != null)
			compositeTermVector.clear();
		else
			compositeTermVector	= new TermVector();
//		termVector.clear();

		Iterator<FieldAccessor> fieldIterator = fieldAccessorIterator();
		while(fieldIterator.hasNext())
		{
			FieldAccessor fieldAccessor = fieldIterator.next();
			String valueString = fieldAccessor.getValueString(this);
			if(valueString != null && valueString != "null")
			{
				compositeTermVector.addTerms(valueString, false);
			}
		}


		//Supporting Mixins
		if(mixins() != null && mixins().size() > 0)
		{
			Iterator<Metadata> metadataIterator = mixins().iterator();
			while(metadataIterator.hasNext())
			{
				Metadata metadata = metadataIterator.next();
				fieldIterator = metadata.fieldAccessorIterator();
				while(fieldIterator.hasNext())
				{
					FieldAccessor fieldAccessor = fieldIterator.next();
					String valueString = fieldAccessor.getValueString(metadata);
					if(valueString != null && valueString != "null")
					{
						compositeTermVector.addTerms(valueString, false);
					}
				}
			}
		}
		
		//add any actual data terms to the composite term vector
//		if (dataTermVector != null)
//			termVector.combine(dataTermVector);
		
	}
	
	
	/**
	 * The weight of the composite TermVector.
	 * @return	The composite TermVector's weight.
	 * @see ecologylab.model.text.TermVector#getWeight()
	 */
	public float getWeight()
	{
		return compositeTermVector == null ? 1 : compositeTermVector.getWeight();
	}
	
	/**
	 * The lnWeight
	 * @return	The lnWeight() of the composite TermVector.
	 * @see ecologylab.model.text.TermVector#lnWeight()
	 */
	public float lnWeight()
	{
		return compositeTermVector == null ? 0 : compositeTermVector.lnWeight();
	}
	
//	public Metadata lookupChildMetadata(String tagName)
//	{
//		HashMapArrayList<String, FieldAccessor> fieldAccessors = Optimizations.getFieldAccessors(this.getClass());
//		FieldAccessor fieldAccessor = fieldAccessors.get(tagName);
//		Metadata metadata = fieldAccessor.getField();
//		return metadata;
//	}
	
	/**
	 * Sets the field to the specified value and wont rebuild composteTermVector
	 * @param fieldName
	 * @param value
	 */
	//TODO -- May throw exception if there is no field accessor.
	public void set(String tagName, String value)
	{
		tagName = tagName.toLowerCase();
		//Taking care of mixins
		Metadata metadata = getMetadataWhichContainsField(tagName);

		if(metadata != null)
		{
			FieldAccessor fieldAccessor = get(tagName);
			if(fieldAccessor != null)
			{
				fieldAccessor.set(metadata, value);
			}
			else 
			{
				debug("No field Accessor");
			}
		}
	}
	
	/**
	 * Setting the field to the specified value and rebuilds the composteTermVector.
	 * @param fieldName
	 * @param value
	 */
	//TODO -- May throw exception if there is no field accessor.
	public void hwSet(String tagName, String value)
	{
		tagName = tagName.toLowerCase();
		Metadata metadata = getMetadataWhichContainsField(tagName);
		
		if(metadata != null)
		{
			FieldAccessor fieldAccessor = get(tagName);
			if(fieldAccessor != null)
			{
				fieldAccessor.set(metadata, value);
				rebuildCompositeTermVector();
			}
			else 
			{
				debug("No field Accessor");
			}
			//Debugging
//			if(fieldAccessor.getFieldName() == "title")
//			{
//			String valuestring = fieldAccessor.getValueString(this);
//			System.out.println("location:"+fieldAccessor.getValueString(this));
//			}
		}
	}
	/**
	 * Returns the metadata class if it contains a Field with name
	 * NOTE: Currently should be used ONLY for mixins
	 * @param tagName
	 * @return
	 */
	public Metadata getMetadataWhichContainsField(String tagName)
	{
		HashMapArrayList<String, FieldAccessor> fieldAccessors = metadataFieldAccessors();
		
		FieldAccessor metadataFieldAccessor = fieldAccessors.get(tagName);
		if (metadataFieldAccessor != null)
		{
			return this;
		}
		//The field may be in mixin
		if(mixins() != null && mixins().size() > 0)
		{
			for (Metadata mixinMetadata : mixins())
			{
				fieldAccessors 	= mixinMetadata.metadataFieldAccessors();
				FieldAccessor mixinFieldAccessor 	= fieldAccessors.get(tagName);
				if(mixinFieldAccessor != null)
				{
					return mixinMetadata;
				}
			}
		}
		return null;
	}
	
//	public void setMixinField(String tagName, String value)
//	{
//		tagName = tagName.toLowerCase();
//		Metadata metadata = getMetadataWhichhasField(tagName); 
//		
//		if(metadata != null)
//		{
//			HashMapArrayList<String, FieldAccessor> fieldAccessors = Optimizations.getFieldAccessors(metadata.getClass());
//			FieldAccessor fieldAccessor = fieldAccessors.get(tagName);
//			if(fieldAccessor != null)
//			{
//				fieldAccessor.set(metadata, value);
//			}
//			else 
//			{
//				System.out.println("No field Accessor");
//				//fieldAccessor.set(this, value);
//			}
//		}
//	}
	
	
	
	public Field getFields()
	{
		
		return null;
	}
	
	public MetaMetadataField childMetaMetadata(String name)
	{
		return metaMetadata == null ? null : metaMetadata.lookupChild(name);
	}

	public MetaMetadata getMetaMetadata()
	{
		return metaMetadata;
	}

	public void setMetaMetadata(MetaMetadata metaMetadata)
	{
		this.metaMetadata = metaMetadata;
	}
	
	public void initializeMetadataCompTermVector()
	{
		compositeTermVector = new TermVector();
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
	 * @param mixins the mixins to set
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
	public MetadataFieldAccessor getMetadataFieldAccessor(String fieldName)
	{
		return (MetadataFieldAccessor) metadataFieldAccessors().get(fieldName);
	}
	
	//For adding mapped attributes
	public void add(String key)
	{
		
	}
	
	public Metadata get(int index)
	{
		return null;
	}
	public TermVector getCompositeTermVector()
	{
		return super.getCompositeTermVector();
	}
	
}
