/**
 * 
 */
package ecologylab.bigsemantics.metadata;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import ecologylab.bigsemantics.gui.EditValueEvent;
import ecologylab.bigsemantics.gui.EditValueListener;
import ecologylab.bigsemantics.gui.EditValueNotifier;
import ecologylab.bigsemantics.metadata.scalar.MetadataScalarBase;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.bigsemantics.metametadata.MetaMetadataCollectionField;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.bigsemantics.metametadata.MetaMetadataNestedField;
import ecologylab.bigsemantics.metametadata.MmdCompilerService;
import ecologylab.bigsemantics.metametadata.MmdGenericTypeVar;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.CollectionType;
import ecologylab.serialization.types.ScalarType;
import ecologylab.serialization.types.scalar.CompositeAsScalarType;

/**
 * 
 * @author andruid
 *
 */
public class MetadataFieldDescriptor<M extends Metadata> extends FieldDescriptor implements EditValueNotifier
{
	
	static
	{
		MetadataScalarType.init();
	}
	
	final private boolean isMixin;

	Method hwSetMethod;

	Method getter;
	
	/**
	 * The name in the MetaMetadataComposite field whose declaration resulted in the generation of
	 * this.
	 */
	@simpl_scalar
	private String mmName;

	private ArrayList<EditValueListener> editValueListeners = new ArrayList<EditValueListener>();

	private MetaMetadataField definingMmdField;

	private boolean startedTraversalForPolymorphism	= false;
	
	public MetadataFieldDescriptor(ClassDescriptor declaringClassDescriptor, Field field, FieldType annotationType) // String nameSpacePrefix
	{
		super(declaringClassDescriptor, field, annotationType);
		if (field != null)
		{
			isMixin							= field.isAnnotationPresent(semantics_mixin.class);
			//TODO -- for future expansion??? andruid 4/14/09
//			hwSetMethod					= ReflectionTools.getMethod(thatClass, "hwSet", SET_METHOD_ARG);
		}
		else
		{
			isMixin							= false;
		}
		this.mmName						= deriveMmName();
		checkScalarType();
	}
	
	public MetadataFieldDescriptor(ClassDescriptor baseClassDescriptor, FieldDescriptor wrappedFD, String wrapperTag)
	{
		super(baseClassDescriptor, wrappedFD, wrapperTag);
		isMixin				= false;
		checkScalarType();
	}
	
	public MetadataFieldDescriptor(MetaMetadataField definingMmdField, String tagName, String comment, FieldType type, ClassDescriptor elementClassDescriptor,
			ClassDescriptor declaringClassDescriptor, String fieldName, ScalarType scalarType,
			Hint xmlHint, String fieldType)
	{
		super(tagName, comment, type, elementClassDescriptor, declaringClassDescriptor, fieldName, scalarType, xmlHint, fieldType);
		this.isMixin = false;
		this.definingMmdField = definingMmdField;
		
		if (definingMmdField.getOtherTags() != null)
		{
			String[] otherTags = definingMmdField.getOtherTags().split(",");
			for (String otherTag : otherTags)
				this.otherTags().add(otherTag.trim());
		}
		
		// child tag for collections
		if (definingMmdField instanceof MetaMetadataCollectionField)
		{
			String childTag = ((MetaMetadataCollectionField) definingMmdField).getChildTag();
			this.setCollectionOrMapTagName(childTag);
		}
		
		// simpl_scope for inherently polymorphic fields
		if (definingMmdField instanceof MetaMetadataNestedField)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) definingMmdField;
			String scopeName = nested.getPolymorphicScope();
			this.setUnresolvedScopeAnnotation(scopeName);
		}
		
		checkScalarType();
	}
	
	public MetadataFieldDescriptor()
	{
		isMixin = false;
		// empty constructor to satisfy S.IM.PL
	}
	
	private void checkScalarType()
	{
		if (this.field != null
				&& MetadataScalarBase.class.isAssignableFrom(this.field.getType())
				&& this.getScalarType() != null
				&& this.getScalarType() instanceof CompositeAsScalarType)
		{
			warning("A CompositeAsScalarType Field!");
			warning("Please check if metadata scalar types registered before MetadataFieldDescriptors formed!");
		}
	}

	@Override
	public boolean isMixin() 
	{
		return isMixin;
	}

	@Override
	public void addEditValueListener(EditValueListener listener)
	{
		editValueListeners.add(listener);
	}

/**
 * Edit the value of a scalar.
 * 
 * @return True if the value of the field is set; otherwise, false.
 */
	@Override
	public boolean fireEditValue(Metadata metadata, String fieldValueString)
	{
		boolean result = false;
		if (isScalar())
		{
			result = this.set(metadata, fieldValueString);
			if(result)	// uses reflection to call a set method or access the field directly if there is not one.
			{
				metadata.rebuildCompositeTermVector();	// makes this as if an hwSet().
				
				//Call the listeners only after the field is properly set.
				EditValueEvent event = new EditValueEvent(this, metadata);
	
				for(EditValueListener listener : editValueListeners)
				{
					listener.editValue(event);
				}
			}
		}
		
		return result;
	}
	
	@Override
	public void removeEditValueListener(EditValueListener listener)
	{
		editValueListeners.remove(listener);
	}
	
	public MetadataBase getNestedMetadata(MetadataBase context)
	{
		return isScalar() ? null : (MetadataBase) getNested(context);
	}
	
	@Override
	public void setFieldToScalar(Object context, String value, ScalarUnmarshallingContext scalarUnmarshallingContext)
	{		
		super.setFieldToScalar(context, value, scalarUnmarshallingContext);
	}
	
	private String deriveMmName()
	{
		String result	= null;
		
		Field thatField = this.field;
		final mm_name mmNameAnnotation 	= thatField.getAnnotation(mm_name.class);
	
		if (mmNameAnnotation != null)
		{
			result			= mmNameAnnotation.value();
		}
		if (result == null)
		{
			result			= XMLTools.getXmlTagName(thatField.getName(), null);
			if (!this.isScalar() && !thatField.isAnnotationPresent(mm_no.class))
				error("Missing @mm_name annotation for " + thatField + "\tusing " + result);
		}
		return result;
	}
	
	/**
	 * @return the mmName
	 */
	public String getMmName()
	{
		return mmName;
	}
	
	/**
	 * get the (defining) meta-metadata field object. currently, only used by the compiler.
	 *  
	 * @return
	 */
	public MetaMetadataField getDefiningMmdField()
	{
		return definingMmdField;
	}
	
	public void setDefiningMmdField(MetaMetadataField mmdField)
	{
		this.definingMmdField = mmdField;
	}
	

	private String fixNull(String s)
	{
		return s == null ? "NULL" : s;
	}
	
	@Override
	public String toString()
	{
		String name = getName(); if (name == null) name = "NO_FIELD";
		
		return fixNull(this.getClassSimpleName()) + "[" + fixNull(name) + " < " + fixNull(declaringClassDescriptor == null ? null : declaringClassDescriptor.getDescribedClass().getName())
				+ " type=0x" + (getType()!= null ? Integer.toHexString(getType().getTypeID()) : "NULL") + "]";
	}
	
	@Override
	public void setWrapped(boolean wrapped)
	{
		super.setWrapped(wrapped);
	}
	
	public void setWrappedFD(MetadataFieldDescriptor wrappedFD)
	{
		super.setWrappedFD(wrappedFD);
	}
	
	@Override
	public void setTagName(String tagName)
	{
		super.setTagName(tagName);
	}
	
	@Override
	public void setCollectionOrMapTagName(String collectionOrMapTagName)
	{
		super.setCollectionOrMapTagName(collectionOrMapTagName);
	}
	
	@Override
	public MetadataFieldDescriptor clone()
	{
		return (MetadataFieldDescriptor) super.clone();
	}
	
	public void setGeneric(String genericParametersString)
	{
		this.isGeneric = true;
		this.genericParametersString = genericParametersString;
	}
	
	private MmdCompilerService compilerService;
	
	public void setCompilerService(MmdCompilerService compilerService)
	{
		this.compilerService = compilerService;
	}
	
	private String cachedJavaType;
	
	@Override
	public String getJavaType()
	{
		if (cachedJavaType != null)
			return cachedJavaType;
		
		CollectionType collectionType = this.getCollectionType();
		String javaType = super.getJavaType();
		
		if (compilerService != null && collectionType != null && definingMmdField instanceof MetaMetadataNestedField)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) definingMmdField;
			Collection<MmdGenericTypeVar> genericTypeVars = nested.getGenericTypeVarsCollection();
			if (genericTypeVars != null && genericTypeVars.size() > 0)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(javaType.substring(0, javaType.indexOf('<')));
				sb.append("<");
				sb.append(this.getElementClassDescriptor().getDescribedClassSimpleName());
				try
				{
					compilerService.appendGenericTypeVarParameterizations(sb, nested.getGenericTypeVarsCollection(), nested.getRepository());
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sb.append(">");
				return sb.toString();
			}
		}
		
		if (getType() == FieldType.COMPOSITE_ELEMENT)
		{
			// FIXME this part needs to be debugged!!!!
			MetaMetadataNestedField nested = (MetaMetadataNestedField) definingMmdField;
			Collection<MmdGenericTypeVar> genericTypeVars = nested.getGenericTypeVarsCollection();
			for (MmdGenericTypeVar mmdGenericTypeVar : genericTypeVars)
			{
				if (mmdGenericTypeVar.getNestedGenericTypeVarScope() == null || mmdGenericTypeVar.getNestedGenericTypeVars().size() == 0)
				{
					javaType = mmdGenericTypeVar.getName();
					break;
				}
			}
		}
		
		cachedJavaType = javaType;
		return javaType;
	}
	
	private String cachedCSharpType;
	
	@Override
	public String getCSharpType()
	{
		if (cachedCSharpType != null)
			return cachedCSharpType;
		
		CollectionType collectionType = this.getCollectionType();
		String csType = super.getCSharpType();
		
		String typeName = (csType.indexOf("<") < 0) ? 
							csType :
							csType.substring(csType.indexOf("<") + 1, csType.indexOf(">"));
		
		if (compilerService != null && definingMmdField instanceof MetaMetadataNestedField)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) definingMmdField;
			Collection<MmdGenericTypeVar> genericTypeVars = nested.getGenericTypeVarsCollection();
			Collection<MmdGenericTypeVar> superMmdGenericTypeVars = ((nested.getTypeMmd() != null )? 
																		nested.getTypeMmd().getGenericTypeVarsCollection() :
																		null);
			if (collectionType != null && genericTypeVars != null && genericTypeVars.size() > 0)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(csType.substring(0, csType.indexOf('<')));
				sb.append("<");
				sb.append(this.getElementClassDescriptor().getDescribedClassSimpleName());
				try
				{
					compilerService.appendGenericTypeVarParameterizations(sb, nested.getGenericTypeVarsCollection(), nested.getRepository());
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sb.append(">");
				return sb.toString();
			}			
			else if (nested.getRepository().getMMByName(XMLTools.getXmlTagName(typeName, null)) != null && 
						superMmdGenericTypeVars != null && superMmdGenericTypeVars.size() > 0)
			{
				StringBuilder sb = new StringBuilder();
				
				if (collectionType != null)
					sb.append(csType.substring(0, csType.indexOf(">")));
				else
					sb.append(csType);
				//sb.append(this.getElementClassDescriptor().getDescribedClassSimpleName());
				try
				{
					compilerService.appendGenericTypeVarExtends(sb, superMmdGenericTypeVars, nested.getRepository());
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (collectionType != null)
					sb.append(">");
								
				return sb.toString();
			}	
		}
		
		if (getType() == FieldType.COMPOSITE_ELEMENT)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) definingMmdField;
			for (MmdGenericTypeVar mmdGenericTypeVar : nested.getGenericTypeVarsCollection())
			{
				if (mmdGenericTypeVar.getNestedGenericTypeVarScope() == null || mmdGenericTypeVar.getNestedGenericTypeVars().size() == 0)
				{
					csType = mmdGenericTypeVar.getName();
					break;
				}
			}
		}
		
		cachedCSharpType = csType;
		return csType;
	}
	
}
