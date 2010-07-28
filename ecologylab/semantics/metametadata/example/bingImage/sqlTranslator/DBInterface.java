package ecologylab.semantics.metametadata.example.bingImage.sqlTranslator;

public interface DBInterface
{
	/* Type of database */
	String	POSTGRESQL											= "postgreSQL";

	String	POSTGRESQL_DRIVER								= "org.postgresql.Driver";

	String	POSTGRESQL_DEFAULT_USER_NAME		= "comma78";

	String	POSTGRESQL_DEFAULT_PWD					= "postgresql";

	String	POSTGRESQL_DEFAULT_URI					= "jdbc:postgresql://127.0.0.1:5432/";
	
	//should be set True to store data 
	boolean POSTGRESQL_DEFAULT_COMMIT_MODE 	= true; 

	/**
	 * a list of postgresql keywords ref.
	 * http://www.postgresql.org/docs/8.4/interactive/sql-keywords-appendix.html
	 */
	public static final String	POSTGRESQL_RESERVED_KEYWORDS[]	=
																															{ "ALL", "ANALYSE", "ANALYZE", "AND",
			"ANY", "ARRAY", "AS", "ASC", "ASYMMETRIC", "AUTHORIZATION", "BETWEEN", "BIGINT", "BINARY",
			"BIT", "BOTH", "CASE", "CAST", "CHAR", "CHARACTER", "CHECK", "COALESCE", "COLLATE", "COLUMN",
			"CONSTRAINT", "CREATE", "CROSS", "CURRENT_CATALOG", "CURRENT_DATE", "CURRENT_ROLE",
			"CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEC", "DECIMAL",
			"DEFAULT", "DEFERRABLE", "DESC", "DISTINCT", "DO", "ELSE", "END", "EXCEPT", "EXISTS",
			"EXTRACT", "FALSE", "FETCH", "FLOAT", "FOR", "FOREIGN", "FREEZE", "FROM", "FULL", "GRANT",
			"GREATEST", "GROUP", "HAVING", "ILIKE", "IN", "INITIALLY", "INNER", "INOUT", "INT",
			"INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISNULL", "JOIN", "LEADING", "LEAST",
			"LEFT", "LIKE", "LIMIT", "LOCALTIME", "LOCALTIMESTAMP", "NATIONAL", "NATURAL", "NCHAR",
			"NEW", "NONE", "NOT", "NOTNULL", "NULL", "NULLIF", "NUMERIC", "OFF", "OFFSET", "OLD", "ON",
			"ONLY", "OR", "ORDER", "OUT", "OUTER", "OVER", "OVERLAPS", "OVERLAY", "PLACING", "POSITION",
			"PRECISION", "PRIMARY", "REAL", "REFERENCES", "RETURNING", "RIGHT", "ROW", "SELECT",
			"SESSION_USER", "SETOF", "SIMILAR", "SMALLINT", "SOME", "SUBSTRING", "SYMMETRIC", "TABLE",
			"THEN", "TIME", "TIMESTAMP", "TO", "TRAILING", "TREAT", "TRIM", "TRUE", "UNION", "UNIQUE",
			"USER", "USING", "VALUES", "VARCHAR", "VARIADIC", "VERBOSE", "WHEN", "WHERE", "WINDOW",
			"WITH", "XMLATTRIBUTES", "XMLCONCAT", "XMLELEMENT", "XMLFOREST", "XMLPARSE", "XMLPI",
			"XMLROOT", "XMLSERIALIZE"

																															}; 

}
