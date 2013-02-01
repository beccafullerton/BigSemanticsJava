/**
 * 
 */
package ecologylab.bigsemantics.actions;

import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * THE IF statement
 * 
 * @author amathur
 * 
 */
@simpl_inherit
public @simpl_tag(SemanticActionStandardMethods.IF)
class IfSemanticAction 
extends	NestedSemanticAction
{

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.IF;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
