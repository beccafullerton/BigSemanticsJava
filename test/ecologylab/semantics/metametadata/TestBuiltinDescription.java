package ecologylab.semantics.metametadata;

import static org.junit.Assert.*;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.builtins.declarations.MetadataDeclaration;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metadata.scalar.types.MetadataStringScalarType;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.SIMPLDescriptionException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.TypeRegistry;


import org.junit.Test;

public class TestBuiltinDescription {

	
	@Test
	public void testThatMetadataStringsAreDescribedAsScalars()
	{
		final class myMetadataStringScalar
		{
			@simpl_scalar
			private MetadataString myString;
			
		}
		
		
		
		
		ClassDescriptor cd = ClassDescriptor.getClassDescriptor(myMetadataStringScalar.class);
		
		assertEquals(1, cd.allFieldDescriptors().size());
		FieldDescriptor theField = (FieldDescriptor)cd.allFieldDescriptors().get(0);	
		
		assertEquals(MetadataStringScalarType.class, TypeRegistry.getScalarType(MetadataString.class));
		assertFalse("This is a scalar value, not a composite as scalar", XMLTools.isCompositeAsScalarvalue(theField.getField()));
		assertEquals("This should be a scalar field.", FieldType.SCALAR, theField.getType());
		assertEquals(MetadataStringScalarType.class, theField.getScalarType());
	}
	
	@Test
	public void testThatMetadataCanBeDescribed()
	{
		ClassDescriptor cd = ClassDescriptor.getClassDescriptor(Metadata.class);
	}
	
	@Test
	public void testThatTheRepositoryScopeExists()
	{
		
		SimplTypesScope defaultGet = RepositoryMetadataTranslationScope.get();
		
		assertNotNull(defaultGet);
		
		SimplTypesScope sts = SimplTypesScope.get("repository_metadata");
		
		assertNotNull(sts);		
		
	}
	
	public void invalidClassDescriptionWillThrowException() throws SIMPLDescriptionException {
		//SimplTypesScope defaultGet = RepositoryMetadataTranslationScope.get();

		ClassDescriptor<?> cd = ClassDescriptor.getClassDescriptor(MetadataDeclaration.class);	
	}

}
