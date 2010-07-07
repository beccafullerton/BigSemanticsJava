package ecologylab.semantics.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecologylab.semantics.metametadata.DefVar;
import ecologylab.xml.simpl_inherit;
import ecologylab.xml.ElementState.xml_tag;

@simpl_inherit
@xml_tag("string_operation")
public class StringOperationsSemanticAction<SA extends SemanticAction> extends
		NestedSemanticAction<SA>
{

	// <arg> names
	private static final String	ARG_CONCAT_ANOTHER	= "another";

	// <def_var> names
	private static final String VAR_CONCAT_ANOTHER  = "another";
	
	private static final String	VAR_SUBSTRING_BEGIN	= "begin";

	private static final String	VAR_SUBSTRING_END		= "end";

	private static final String	VAR_FIELD_DELIM			= "delim";

	private static final String	VAR_FIELD_INDEX			= "index";

	// actions
	public static final String	ACTION_LENGTH				= "length";

	public static final String	ACTION_CONCAT				= "concat";

	public static final String	ACTION_SUBSTRING		= "substring";

	public static final String	ACTION_FIELD				= "field";

	public static final String	ACTION_TRIM					= "trim";

	@simpl_scalar
	private String							action;

	@Override
	public String getActionName()
	{
		return "string_operation";
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		String dest = (String) obj;

		if (action.equals(ACTION_LENGTH))
		{
			Integer len = dest.length();
			return len;
		}
		else if (action.equals(ACTION_CONCAT))
		{
			String another = getDefVar(VAR_CONCAT_ANOTHER).getValue();
			if (another == null)
				another = (String) args.get(ARG_CONCAT_ANOTHER);
			StringBuilder sb = new StringBuilder(dest);
			sb.append(another);
			return sb.toString();
		}
		else if (action.equals(ACTION_SUBSTRING))
		{
			int length = dest.length();

			DefVar varBegin = getDefVar(VAR_SUBSTRING_BEGIN);
			int begin = Integer.valueOf(varBegin.getValue());
			DefVar varEnd = getDefVar(VAR_SUBSTRING_END);
			int end = Integer.valueOf(varEnd.getValue());

			// enable negative index
			while (begin < 0)
				begin += length;
			while (end < 0)
				end += length;

			return dest.substring(begin, end);
		}
		else if (action.equals(ACTION_FIELD))
		{
			String delim = getDefVar(VAR_FIELD_DELIM).getValue();
			int index = Integer.valueOf(getDefVar(VAR_FIELD_INDEX).getValue());
			return dest.split(delim)[index];
		}
		else if (action.equals(ACTION_TRIM))
		{
			return dest.trim();
		}

		return dest;
	}

	private Map<String, DefVar>	defVarMap;

	protected DefVar getDefVar(String name)
	{
		if (defVarMap == null)
		{
			defVarMap = new HashMap<String, DefVar>();
			List<DefVar> vars = getDefVars();
			for (DefVar var : vars)
			{
				defVarMap.put(var.getName(), var);
			}
		}
		if (defVarMap.containsKey(name))
			return defVarMap.get(name);
		else
			return null;
	}
}
