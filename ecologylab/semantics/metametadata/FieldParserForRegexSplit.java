package ecologylab.semantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class FieldParserForRegexSplit extends FieldParser
{

	public static final String	DEFAULT_KEY	= "$0";

	@Override
	public List<Map<String, String>> getCollectionResult(FieldParserElement parserElement, String input)
	{
		List<Map<String, String>> rst = new ArrayList<Map<String, String>>();

		if (input != null)
		{
			String[] parts = parserElement.getRegex().split(input);
			for (int i = 0; i < parts.length; ++i)
			{
				Map<String, String> item = new HashMap<String, String>();
				item.put(DEFAULT_KEY, parts[i]);
				rst.add(item);
			}
		}

		return rst;
	}

	@Test
	public void testSplit()
	{
		String test = "2007. Mixed media, sound, pneumatics, robotics, elector magnetic beaters, dentist chair, electric guitar, computer, various control systems, 9 10 x 13 1 x 8 2 (118 x 157 x 98 cm) 5 min. Gift of the Julia Stoschek Foundation, D�sseldorf, and the Dunn Bequest. � 2011 Janet Cardiff and George Bures Miller. Photo: Ugarte & Lorena Lopez. Courtesy of the artist, Luhring Augustine, New York and Galerie Barbara Weiss, Berlin. ";

		FieldParserElement pe = new FieldParserElement("regex_split", "\\s*\\.\\s+");
		List<Map<String, String>> rst = getCollectionResult(pe, test);
		for (Map<String, String> obj : rst)
		{
			System.out.println(obj.get(DEFAULT_KEY));
		}
	}

}
