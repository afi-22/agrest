package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.protocol.Sort;

import java.util.List;

/**
 * Parsing of Sort and Dir query parameters from string or related in JSON values.
 *
 * @since 2.13
 */
public interface ISortParser {

    List<Sort> parse(String unparsedSort, String unparsedDir);

    List<Sort> parseJson(JsonNode json);
}
