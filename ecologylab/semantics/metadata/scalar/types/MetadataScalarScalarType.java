/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import java.lang.reflect.Field;

import ecologylab.semantics.metadata.scalar.MetadataScalarBase;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.xml.ScalarUnmarshallingContext;
import ecologylab.xml.types.scalar.ReferenceType;
import ecologylab.xml.types.scalar.ScalarType;
import ecologylab.xml.types.scalar.TypeRegistry;

/**
 * Basis for scalar types for classes that derive from MetadataScalarBase.
 * 
 * M is the MetadataBase type, like MetadataString.
 * 
 * T is the nested type, like String.
 * 
 * @author andruid
 */
public abstract class MetadataScalarScalarType<M, T> extends ReferenceType<M>
{
	ScalarType<T>		valueScalarType;
	
	Field						valueField;
	
	Class<M> 				metadataScalarTypeClass;
	
	/**
	 * @param thatClass
	 */
	public MetadataScalarScalarType(Class<M> metadataScalarTypeClass, Class valueClass)
	{
		super(metadataScalarTypeClass);
		this.metadataScalarTypeClass	= metadataScalarTypeClass;
		this.valueScalarType					= TypeRegistry.getType(valueClass);
	}
	
	Field valueField()
	{
		Field result	= valueField;
		if (result == null)
		{
			Class metadataScalarBaseClass	= MetadataScalarBase.class;
			try
			{
				Field valueField							= metadataScalarBaseClass.getField(MetadataScalarBase.VALUE_FIELD_NAME);
				valueField.setAccessible(true);
			}
			catch (SecurityException e)
			{
				error("Can't access value field for " + metadataScalarTypeClass);
			}
			catch (NoSuchFieldException e)
			{
				error("Can't find value field for " + metadataScalarTypeClass);
			}
		}
		return result;
	}
	
	@Override
  public boolean setField(Object largerMetadataContext, Field field, String valueString, String[] format, ScalarUnmarshallingContext scalarUnmarshallingContext)
  {
      if (valueString == null)
          return true;

      boolean result		= false;
      T valueObject;

      try
      {
          valueObject = valueScalarType.getInstance(valueString, format, scalarUnmarshallingContext);
          if (valueObject != null)
          {
          	M metadataScalarContext	= (M) field.get(largerMetadataContext);
          	valueField.set(metadataScalarContext, valueObject);
          	result 		= true;
          }
      }
      catch (Exception e)
      {
          setFieldError(field, valueString, e);
      }
      return result;
  }

	public T getValueInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return valueScalarType.getInstance(value, formatStrings, scalarUnmarshallingContext);
	}
	
	public static final Class[] METADATA_SCALAR_TYPES	=
	{
			MetadataStringScalarType.class, MetadataStringBuilderScalarType.class, MetadataIntegerScalarType.class,
			MetadataParsedURLScalarType.class,
	};
	
	public static void init()
	{
		TypeRegistry.register(METADATA_SCALAR_TYPES);
	}
	
	@Override
	public ScalarType operativeScalarType()
	{
		return valueScalarType;
	}
	
	@Override
	public Field operativeField(Field externalField)
	{
		return valueField;
	}
	
	@Override
  public String toString(Field field, Object largerMetadataContext)
  {
      String result = "COULDNT CONVERT!";
      try
      {
      	M metadataScalarContext	= (M) field.get(largerMetadataContext);
      	T instance = (T) valueField.get(largerMetadataContext);
      	if (instance == null)
      		result = DEFAULT_VALUE_STRING;
      	else
      		result = toString(instance);
      }
      catch (Exception e)
      {
      	e.printStackTrace();
      }
      return result;
  }
	public String toString(T instance)
	{
		return instance.toString();
	}
}
