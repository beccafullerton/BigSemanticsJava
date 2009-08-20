package ecologylab.semantics.actions;

import ecologylab.collections.Scope;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * 
 * @author amathur
 *
 */

@xml_inherit
public @xml_tag(SemanticActionStandardMethods.CREATE_SEMANTIC_ANCHOR)
class CreateSemanticAnchorSemanticAction extends SemanticAction implements SemanticActionStandardMethods
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
	
}
