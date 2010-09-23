/**
 * 
 */
package ecologylab.semantics.actions;

import java.io.IOException;

import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.serialization.Hint;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * @author amathur
 * 
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.PARSE_DOCUMENT)
class ParseDocumentSemanticAction<IC extends InfoCollector, SAH extends SemanticActionHandler>
		extends SemanticAction<IC, SAH>
{

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected boolean	now	= false;

	public boolean isNow()
	{
		return now;
	}

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.PARSE_DOCUMENT;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		Container container = semanticActionHandler.createContainer(this, documentParser, infoCollector);
		if (container != null)
		{
			infoCollector.getContainerDownloadIfNeeded(documentParser.getContainer(), container.purl(),
					null, false, false, false);
		}
		return null;
	}

}
