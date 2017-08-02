package grails.plugins.elasticsearch.util

import org.elasticsearch.common.ParsingException
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentLocation
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryParseContext

/**
 * Created by ehauske on 2/08/17.
 */
class AbstractQueryBuilderParser {

  /**
   * Parses a query excluding the query element that wraps it
   */
  public static QueryBuilder parseInnerQueryBuilder(XContentParser parser) throws IOException {
    if (parser.currentToken() != XContentParser.Token.START_OBJECT) {
      if (parser.nextToken() != XContentParser.Token.START_OBJECT) {
        throw new ParsingException(parser.getTokenLocation(), "[_na] query malformed, must start with start_object");
      }
    }
    if (parser.nextToken() == XContentParser.Token.END_OBJECT) {
      // we encountered '{}' for a query clause, it used to be supported, deprecated in 5.0 and removed in 6.0
      throw new IllegalArgumentException("query malformed, empty clause found at [" + parser.getTokenLocation() +"]");
    }
    if (parser.currentToken() != XContentParser.Token.FIELD_NAME) {
      throw new ParsingException(parser.getTokenLocation(), "[_na] query malformed, no field after start_object");
    }
    String queryName = parser.currentName();
    // move to the next START_OBJECT
    if (parser.nextToken() != XContentParser.Token.START_OBJECT) {
      throw new ParsingException(parser.getTokenLocation(), "[" + queryName + "] query malformed, no start_object after query name");
    }
    QueryBuilder result;
    try {
      def object = parser.namedObject(Optional.class, queryName, new QueryParseContext(parser))
      result = object.get();
    } catch (NamedXContentRegistry.UnknownNamedObjectException e) {
      // Preserve the error message from 5.0 until we have a compellingly better message so we don't break BWC.
      // This intentionally doesn't include the causing exception because that'd change the "root_cause" of any unknown query errors
      throw new ParsingException(new XContentLocation(e.getLineNumber(), e.getColumnNumber()),
        "no [query] registered for [" + e.getName() + "]");
    }
    //end_object of the specific query (e.g. match, multi_match etc.) element
    if (parser.currentToken() != XContentParser.Token.END_OBJECT) {
      throw new ParsingException(parser.getTokenLocation(),
        "[" + queryName + "] malformed query, expected [END_OBJECT] but found [" + parser.currentToken() + "]");
    }
    //end_object of the query object
    if (parser.nextToken() != XContentParser.Token.END_OBJECT) {
      throw new ParsingException(parser.getTokenLocation(),
        "[" + queryName + "] malformed query, expected [END_OBJECT] but found [" + parser.currentToken() + "]");
    }
    return result;
  }
}
