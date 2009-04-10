package ecologylab.semantics.seeding;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ecologylab.collections.Scope;
import ecologylab.documenttypes.InfoCollector;
import ecologylab.gui.TopLevel;
import ecologylab.semantics.connectors.SeedPeer;
import ecologylab.semantics.connectors.SemanticsSessionObjectNames;
import ecologylab.services.messages.cf.SearchState;
import ecologylab.services.messages.cf.SeedCf;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;

/**
 * A collection of seeds that will be performed by the agent, or elsewhere,
 * by composition space services.
 * These are directives from the user via the startup enviornment.
 * 
 * @author andruid
 */
@xml_inherit
public class SeedSet extends ecologylab.services.messages.cf.SeedSet
implements SemanticsSessionObjectNames
{	
	public SeedSet()
	{
		
	}
	
//	public static SeedSet getFromXML(String seedSetString)
//	{
//		try
//		{
//			return (SeedSet) translateFromXMLCharSequence(seedSetString, CFServicesTranslations.get());
//		} catch (XMLTranslationException e)
//		{
//			println("ERROR parsing seedSetString.");
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	/**
	 * Number of SearchState seeds specified in this.
	 */
	private int 					searchNum		= 0;

    /**
     * Coordinate downloading of search results for seeding searches.
     */   	
   	ResultDistributer			resultDistributer;
   	
   	/**
   	 * Some of seeds such as YahooBuzzType have a starting seed(a buzz page) from an initial SeedSet and 
   	 * create a new SeedSet from the buzz page. Reference to the parent SeedSet from a child SeedSet. 
   	 */
   	SeedSet						parentSeedSet;
   	
   	public ResultDistributer resultDistributer(InfoCollector infoCollector)
   	{
   		ResultDistributer result	= resultDistributer;
   		
   		if (result == null)
   		{
   			if( parentSeedSet != null )
   				result = parentSeedSet.resultDistributer(infoCollector);
   			else
   				result		= new ResultDistributer(infoCollector, searchNum);
   			this.resultDistributer	= result;
   		}
   		return result;
   	}
   	
   	/**
   	 * In case for replace seed with the curated-seeds, SeedSet is reused. 
   	 * So, old resultDistributer is used without getting reset. 
   	 * Maybe there is better place to reset ResultDistributer -- eunyee
   	 * @return 
   	 */
   	public void resetResultDistributer()
   	{

  		if(resultDistributer != null )
  			resultDistributer.reset();
   	}
   	
   	/**
   	 * Set the reference of the parent SeedSet. 
   	 * @param resultDistributer
   	 */
   	public void setParentSeedSet(SeedSet seedSet)
   	{
   		this.parentSeedSet = seedSet;
   	}
   	
   	/**
   	 * Bring the seeds into the agent or directly into the compostion.
   	 * 
   	 * @param scope		Context passed between services calls.
   	 */
   	public void performSeeding(Scope scope)
   	{
   		InfoCollector infoCollector	= (InfoCollector) scope.get(INFO_COLLECTOR);
   		
   		infoCollector.trackFirstSeedSet(this);

   		int size	= size();
   		if (size > 0)
   	  		infoCollector.setPlayOnStart(true);

   		
   		// search bookkeeping for each seed
   		for (int i=0; i < size; i++)
   		{
   			Seed seed	=  (Seed) get(i);
   			if (seed.bookkeeping(this, searchNum))
   				searchNum++;
   		}
   		
   		// We need the same two for loops (above and below), because 
   		// it requires to have total searchNum before it starts performSeeding.
   		// Do not try to aggregate these two for loops unless you have better structure.

   		for (int i=0; i < size; i++)
   		{
   			Seed seed	= (Seed) get(i);
   			seed.fixNumResults();

   			if (seed.validate())
   			{
   				seed.performSeedingSteps(infoCollector);
  				
     			SeedPeer seedPeer	= seed.getSeedPeer();
     			if (seedPeer != null)
     				seedPeer.notifyInterface(scope, SEARCH_DASH_BOARD);
   			}
   		}

   		// seed set post processing
        if (resultDistributer(infoCollector) != null)
        {    	
        	if( parentSeedSet != null )
        	{   		
        		searchNum = searchNum + resultDistributer.totalSearches() - 1;       		
        	}
        	resultDistributer.setTotalSearches(searchNum);
        }
   	}
   	
   	public String toString()
   	{
   		return "SeedSet[" + size() + "]";
   	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	private int handleMoreSeedsDialogue(JFrame jFrame)
	{
		String seedText		= "";
		for (int i=0; i< size(); i++)
			seedText		+= "        " + ((Seed) get(i)).valueString() + "\n";
		if (!"".equals(seedText))
			seedText		+= "\n";
		int selectedOption = JOptionPane.showOptionDialog(jFrame, 
				"combinFormation has received a new set of seeds:\n" + 
				seedText +
				"\t- Do you want to mix these new seeds with the already existing seeds?\n" +
				"\t- Do you want to replace the current seeds with these new seeds?\n" +
				"\t- Do you want to ignore these new seeds?", "Warning",
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
				null, SeedCf.MULTIPLE_REQUESTSTS_DIALOG_OPTIONS, SeedCf.MULTIPLE_REQUESTSTS_DIALOG_OPTIONS[0]);
		return selectedOption + 1;
	}

	public void handleMoreSeeds(Scope clientConnectionScope, int selectedOption)
	{
		if (selectedOption == SeedCf.MULTIPLE_REQUESTSTS_ASK_USER)
		//if (true)
		{
			if (size() == 0)
			{
				selectedOption	= SeedCf.MULTIPLE_REQUESTSTS_IGNORE;
				debug("Received seeding request with 0 seeds. Ignoring!");
			}
			else
			{
				TopLevel topLevel	= (TopLevel) clientConnectionScope.get(TOP_LEVEL);
				if (topLevel != null)
					topLevel.toFront();
				selectedOption	= handleMoreSeedsDialogue(topLevel);
			}
		}
		switch (selectedOption)
		{
		case SeedCf.MULTIPLE_REQUESTSTS_MIX:
			performSeeding(clientConnectionScope);
			break;
		case SeedCf.MULTIPLE_REQUESTSTS_REPLACE:
			debug("handleMoreSeeds(REPLACE) " + clientConnectionScope.dump());
			InfoCollector infoCollector = (InfoCollector) clientConnectionScope.get(INFO_COLLECTOR);
			infoCollector.clear();
			performSeeding(clientConnectionScope);
			break;
		case SeedCf.MULTIPLE_REQUESTSTS_IGNORE:
		case JOptionPane.CLOSED_OPTION:
			debug("Ignoring multiple request.");
			break;
		default:
		}
	}
}