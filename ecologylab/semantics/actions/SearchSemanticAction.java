package ecologylab.semantics.actions;

import java.util.Map;

import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.SeedPeer;
import ecologylab.semantics.connectors.SemanticsSessionObjectNames;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag("search")
public class SearchSemanticAction<SA extends SemanticAction> extends NestedSemanticAction<SA>
{

	private static final String	ARG_QUERY	= "query";
	
	@simpl_scalar
	private String engine;
	
	@Override
	public String getActionName()
	{
		return "search";
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		String query = (String) args.get(ARG_QUERY);
		if (query == null || query.isEmpty())
			return null;
		
		InfoCollector ic = getInfoCollector();
		SeedSet seedSet = ic.getSeedSet();
		SearchState search = new SearchState(query, engine);
		synchronized (seedSet)
		{
			seedSet.add(search);
//			seedSet.performSeeding(ic.sessionScope());
//			search.performSeedingSteps(ic);
//			SeedPeer seedPeer = search.getSeedPeer();
//			seedPeer.notifyInterface(ic.sessionScope(), SemanticsSessionObjectNames.SEARCH_DASH_BOARD);
		}
		return null;
	}
}
