package ecologylab.semantics.connectors;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.appframework.types.prefs.PrefBoolean;

/**
 * combinFormation-specific set of reusable String constants for getting properties from the environment.
 * 
 * KEEP all actual parameter calls out of here, because this gets loaded before
 * properties do, and they will get stuck at null for that reason!!!
 * BEWARE!
 * <p/>
 * The golden rule here is: "Do not define my constants, before my properties are loaded!"
 * 
 * @author andruid
 * @author blake
 */

public interface CFPrefNames
{
	public static final String	SEED_SET									= "seed_set";
	public static final String	CF_APPLICATION_NAME				= "combinFormation";
	public static final String	DISPLAY_STATUS						= "display_status";
	public static final String 	ENABLE_INCONTEXT_SLIDER 	= "incontext_slider";
	public static final String	APPLICATION_ENVIRONMENT		= "application_environment";
	public static final String	DASHBOARD_ENABLED_NAME		= "dashboard_enabled";
	public static final String	DASHBOARD_NAME						= "dashboard";
	public static final String	SEED_VISIBILITY_CLOSED 		= "closed";

	public static final String	IN_CONTEXT_INTERFACE			= "in_context_interface";
	
	public static final String	STUDY_IN_CONTEXT_INTERFACE			= "study_in_context_interface";
	
	/**
	 * The name of the user interface currently in use, and its path in /config/interface.
	 */
	public static final String 	DEFAULT_INTERFACE  				= IN_CONTEXT_INTERFACE;

	public static final String  USERINTERFACE_PREF_NAME		= "userinterface";

	public static String 		INFO_EXTRACTION_PARAM					= "info_extraction_method";
	public static int 			OLD_UNSTRUCTURED_EXTRACTION		= 0;
	public static int 			EUNYEE_STRUCTURED_EXTRACTION	= 1;

	/**
	 * The reduced interface for non-generative cF.
	 */
	public static final String REDUCED 										= "reduced";
/**
 * The reduced interface + the reset button, for final phase of the mixed-initiative condition, in studies.
 */
	public static final String REDUCED_PLUS 							= "reduced_plus";
	
	public static final String EMPTY_INTERFACE						= "empty";

	public static final String		CURATED									= "curated";
	
	public static final String 	IGNORE_PDF								= "ignore_pdf";
	
	public static final String	DECAY_INTEREST  					= "decay_interest";
	
	public static final String	USE_LOCAL_CF_PREF_NAME				= "use_local_cf";
	
	public static final String	STUDY_COMPOSITION_SAVE_LOCATION	= "study_composition_save_location";
	
	public static final String	SHOW_ZONES								= "show_zones";
	
	public static final String  HOTSPACE_SURROGATE_DENSITY	= "elements_per_square_inch";
	
	public static final String	DISABLE_SMOOTH_FIT					= "disable_smooth_fit";
	
	public static final String	USING_PROXY							= "using_proxy";
	
	public static final String	CRAWL_CAREFULLY						= "crawl_carefully";
	
	public static final String	MIN_WAIT_TIME						= "min_wait_time";
	
	public static final String	MAX_WAIT_TIME						= "max_wait_time";
	
	public static final String	SAVE_SURROGATES						= "save_surrogates";
	
	public static final String	SAVE_SURROGATES_MAX					= "save_surrogates_max";
	
	public static final String	SAVE_SURROGATES_LOCATION			= "save_surrogates_location";
	
	public static final String	SAVE_TERM_VECTORS							= "save_term_vectors";
}
