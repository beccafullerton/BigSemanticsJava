package ecologylab.semantics.seeding;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.appframework.types.prefs.PrefInt;

public interface SemanticsPrefs
{

	public static final PrefInt	NUM_SEARCH_RESULTS		= Pref.usePrefInt("num_search_results", 20);
	public static final PrefBoolean	CRAWL					= Pref.usePrefBoolean("crawl", true);
	/**
	 * If true (and it usually is), then use seeds as the basis for a
	 * spanning set of URL prefixes that limit where the crawler will go.
	 * Corresponds to the stay close / allow wandering interactive runtime option.
	 */
	public static final PrefBoolean	LIMIT_TRAVERSAL	= Pref.usePrefBoolean("limit_traversal", true);
	public static final PrefBoolean	FILTER_OUT_ADS		= Pref.usePrefBoolean("ads_bias", true);
	

}
