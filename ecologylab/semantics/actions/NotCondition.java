package ecologylab.semantics.actions;

import org.junit.Test;

import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.StringFormat;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_tag;

@simpl_inherit
@simpl_tag("not")
public class NotCondition extends Condition
{

	@simpl_composite
	@simpl_scope(ConditionTranslationScope.CONDITION_SCOPE)
	private Condition	check;

	@Override
	public boolean evaluate(SemanticActionHandler handler)
	{
		return !check.evaluate(handler);
	}

	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<not><and><or><and /><or /></or><not_null /></and></not>";
		NotCondition not = (NotCondition) MetaMetadataTranslationScope.get().deserialize(xml,
				StringFormat.XML);
		System.out.println(not);
		System.out.println(not.check);
		System.out.println(ClassDescriptor.serialize(not, StringFormat.XML));
	}

}
