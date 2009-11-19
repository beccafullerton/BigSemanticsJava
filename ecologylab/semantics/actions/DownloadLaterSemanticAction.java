package ecologylab.semantics.actions;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
public @xml_tag(SemanticActionStandardMethods.PARSE_LATER)
class DownloadLaterSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return PARSE_LATER;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

}
