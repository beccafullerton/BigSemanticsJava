package ecologylab.semantics.model;

import ecologylab.generic.Debug;
import ecologylab.semantics.model.text.SemanticTextToken;
import ecologylab.serialization.SimplTypesScope;

public class TextTokenTranslations extends Debug
{
	public static final String TEXT_TOKEN_SCOPE_NAME = "text_token_scope";
		
	public static final Class	TRANSLATIONS[]	= 
	{ 
		SemanticTextToken.class
	};

	/**
	 * 
	 */
	public TextTokenTranslations()
	{
		super();
	}

	public static SimplTypesScope get()
	{
		return SimplTypesScope.get(TEXT_TOKEN_SCOPE_NAME, TRANSLATIONS);
	}
}
