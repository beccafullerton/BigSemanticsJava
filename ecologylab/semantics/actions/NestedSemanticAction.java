/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;

import ecologylab.xml.ElementState.xml_nowrap;

/**
 * This class is the base class for semantic actions which can have nested semantic actions inside
 * them. Right now only FOREACH and IF semantic actions can have other semantic actions nested
 * inside them.
 * 
 * @author amathur
 * 
 */

public abstract class NestedSemanticAction<SA extends SemanticAction> extends SemanticAction
{

	/**
	 * List of nested semantic actions.
	 */
	@xml_nowrap 
	@xml_collection
	// @xml_scope(NestedSemanticActionsTranslationScope.NESTED_SEMANTIC_ACTIONS_SCOPE)
	@xml_classes({
		BackOffFromSite.class,
		CreateAndVisualizeImgSurrogateSemanticAction.class,
		CreateAndVisualizeTextSurrogateSemanticAction.class,
	  CreateContainerSemanticAction.class,
		CreateSemanticAnchorSemanticAction.class,
		EvaluateRankWeight.class,
		ForEachSemanticAction.class,
		GeneralSemanticAction.class,
		GetFieldSemanticAction.class,
		GetXPathNodeSemanticAction.class,
		IfSemanticAction.class,
		ParseDocumentLaterSemanticAction.class,
		ParseDocumentNowSemanticAction.class,
		SetFieldSemanticAction.class,
		SetMetadataSemanticAction.class,
		
		CreateConceptOutlinkSemanticAction.class,
		AnalyzeParagraphSemanticAction.class
	})
	private ArrayList<SA>	nestedSemanticActionList;

	/**
	 * @return the nestedSemanticActionList
	 */
	public ArrayList<SA> getNestedSemanticActionList()
	{
		return nestedSemanticActionList;
	}

	/**
	 * @param nestedSemanticActionList
	 *          the nestedSemanticActionList to set
	 */
	public void setNestedSemanticActionList(ArrayList<SA> nestedSemanticActionList)
	{
		this.nestedSemanticActionList = nestedSemanticActionList;
	}
}
