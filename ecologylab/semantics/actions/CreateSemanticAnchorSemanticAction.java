package ecologylab.semantics.actions;

import java.util.Map;

import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * needs to be implemented by the client.
 * 
 * @author amathur
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.CREATE_SEMANTIC_ANCHOR)
class CreateSemanticAnchorSemanticAction<IC extends InfoCollector, SAH extends SemanticActionHandler>
		extends SemanticAction<IC, SAH> implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return CREATE_SEMANTIC_ANCHOR;
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