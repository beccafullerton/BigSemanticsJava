/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.UserAgent;
import ecologylab.serialization.ElementState;

/**
 * @author amathur
 *
 */
public class UserAgents extends ElementState
{
	@simpl_map("user_agent") private HashMapArrayList<String, UserAgent> userAgent; 
	
}
