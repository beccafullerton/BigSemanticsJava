package ecologylab.semantics.metametadata;

import java.util.regex.Pattern;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.formatenums.StringFormat;

@simpl_inherit
public class RegexFilter extends ElementState
{

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	private Pattern	regex;

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	private int			group;

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	private String	replace;

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	private boolean	normalizeText	= true;

	private String	javaRegex;

	private String	javaReplace;

	public RegexFilter()
	{

	}

	public RegexFilter(Pattern regex, String replace)
	{
		this.regex = regex;
		this.replace = replace;
	}

	public Pattern getRegex()
	{
		return regex;
	}

	public String getJavaRegex()
	{
		if (javaRegex == null && regex != null)
			javaRegex = regex.pattern().replaceAll("\\\\", "\\\\\\\\");
		return javaRegex;
	}

	public String getReplace()
	{
		return replace;
	}

	public String getJavaReplace()
	{
		if (javaReplace == null && replace != null)
			javaReplace = replace.replaceAll("\\\\", "\\\\\\\\");
		return javaReplace;
	}

	public int getGroup()
	{
		return group;
	}

	public void setGroup(int group)
	{
		this.group = group;
	}

	public static void main(String[] args)
	{
		String[] testPatterns =
		{ "\\s+", "\\\\\\\\ 4 back slashes", };
		String testReplace = "";
		for (String p : testPatterns)
		{
			RegexFilter rf = new RegexFilter(Pattern.compile(p), testReplace);
			System.out.println();
			SimplTypesScope.serializeOut(rf, "some message", StringFormat.XML);
			System.out.println();
			System.out.println("In java annotation: " + rf.getJavaRegex());
			System.out.println();
		}
	}

	public boolean isNormalizeText()
	{
		return normalizeText;
	}

	public void setNormalizeText(boolean normalizeText)
	{
		this.normalizeText = normalizeText;
	}

}
