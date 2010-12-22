/**
 * 
 */
package ecologylab.semantics.seeding;

import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.Hint;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

/**
 * 
 *
 * @author andruid 
 */
public class InlineSeed extends Seed
{
	@simpl_scalar @simpl_hints(Hint.XML_LEAF_CDATA) protected String			content;

	/**
	 * Blank constructor used by automatic ecologylab.serialization instantiations. 
	 */
	public InlineSeed()
	{

	}

	/* (non-Javadoc)
	 * @see cf.state.Seed#setCategory(java.lang.String)
	 */
	@Override
	public void setCategory(String value)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see cf.gui.dashboard.DashboardOperand#categoryString()
	 */
	public String categoryString()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see cf.gui.dashboard.DashboardOperand#detailedCategoryString()
	 */
	public String detailedCategoryString()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see cf.gui.dashboard.DashboardOperand#isDeletable()
	 */
	public boolean isDeletable()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cf.gui.dashboard.DashboardOperand#isEditable()
	 */
	public boolean isEditable()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cf.gui.dashboard.DashboardOperand#isRejectable()
	 */
	public boolean isRejectable()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cf.gui.dashboard.DashboardOperand#performInternalSeedingSteps(cf.model.InfoCollector)
	 */
	public void performInternalSeedingSteps(InfoCollector infoCollector)
	{
		if (content != null)
		{
			//FIXME
//			try
//			{
//				ElementState inlineDoc	= infoCollector.inlineDocumentTranslations().deserializeCharSequence(content);
//				DocumentParser xmlBaseType	= infoCollector.constructDocumentType(inlineDoc);
//				if (xmlBaseType != null)
//					xmlBaseType.parse(inlineDoc);
//				else
//					error("Can't find type for " + inlineDoc);
//			} catch (SIMPLTranslationException e)
//			{
//				e.printStackTrace();
//			}
		}

	}

	/* (non-Javadoc)
	 * @see cf.gui.dashboard.DashboardOperand#setValue(java.lang.String)
	 */
	public boolean setValue(String value)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cf.gui.dashboard.DashboardOperand#valueString()
	 */
	public String valueString()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
