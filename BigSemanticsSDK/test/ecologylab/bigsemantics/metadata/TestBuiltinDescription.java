package ecologylab.bigsemantics.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.generated.library.RepositoryMetadataTypesScope;
import ecologylab.bigsemantics.metadata.builtins.declarations.MetadataDeclaration;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataStringScalarType;
import ecologylab.bigsemantics.metadata.scalar.types.SemanticsTypes;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.ScalarType;
import ecologylab.serialization.types.TypeRegistry;

public class TestBuiltinDescription {

	@Before
	public void ResetSTSBeforeExecution()
	{
		SimplTypesScope.ResetAllTypesScopes();
	}
	
	@Test
	public void testThatMetadataStringsAreDescribedAsScalars()
	{
	  new SemanticsTypes(); // this registers all metadata scalar types.
	  
		final class myMetadataStringScalar
		{
			@simpl_scalar
			private MetadataString myString;			
		}

		ClassDescriptor cd = ClassDescriptor.getClassDescriptor(myMetadataStringScalar.class);
		
		assertEquals(1, cd.allFieldDescriptors().size());
		FieldDescriptor theField = (FieldDescriptor)cd.allFieldDescriptors().get(0);	
		
		ScalarType<MetadataString> scalarType = TypeRegistry.getScalarType(MetadataString.class);
    assertEquals(MetadataStringScalarType.class, scalarType.getClass());
		assertFalse("This is a scalar value, not a composite as scalar", XMLTools.isCompositeAsScalarvalue(theField.getField()));
		assertEquals("This should be a scalar field.", FieldType.SCALAR, theField.getType());
		assertEquals(MetadataStringScalarType.class, theField.getScalarType().getClass());
	}
	
	@Test
	public void testThatMetadataCanBeDescribed()
	{
		ClassDescriptor cd = ClassDescriptor.getClassDescriptor(Metadata.class);
		
	}
	
	@Test
	public void testThatTheRepositoryScopeExists()
	{
		
		SimplTypesScope defaultGet = RepositoryMetadataTypesScope.get();
		
		assertNotNull(defaultGet);
		
		SimplTypesScope sts = SimplTypesScope.get("repository_metadata");
		
		assertNotNull(sts);		
		
	}
	
	public void invalidClassDescriptionWillThrowException() {
		//SimplTypesScope defaultGet = RepositoryMetadataTranslationScope.get();

		ClassDescriptor<?> cd = ClassDescriptor.getClassDescriptor(MetadataDeclaration.class);	
	}

}
