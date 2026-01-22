package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.service.SearchParser.SearchQuery;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SearchParserTest {

    @Test
    void parse_null_returnsEmptyQuery() {
        SearchQuery q = SearchParser.parse(null);

        assertEquals("", q.titleSubstring());
        assertEquals(List.of(), q.tags());
    }

    @Test
    void parse_blank_returnsEmptyQuery() {
        SearchQuery q = SearchParser.parse("   \n\t  ");

        assertEquals("", q.titleSubstring());
        assertEquals(List.of(), q.tags());
    }

    @Test
    void parse_titleOnly() {
        SearchQuery q = SearchParser.parse("hello world");

        assertEquals("hello world", q.titleSubstring());
        assertEquals(List.of(), q.tags());
    }

    @Test
    void parse_tagsOnly() {
        SearchQuery q = SearchParser.parse("#java #spring");

        assertEquals("", q.titleSubstring());
        assertEquals(List.of("java", "spring"), q.tags());
    }

    @Test
    void parse_mixed_titleAndTags() {
        SearchQuery q = SearchParser.parse("hello #Java world #spring");

        assertEquals("hello world", q.titleSubstring());
        assertEquals(List.of("java", "spring"), q.tags());
    }

    @Test
    void parse_duplicateAndCasing_tagsNormalizedDistinctSorted() {
        SearchQuery q = SearchParser.parse("#B #a #A #b");

        assertEquals("", q.titleSubstring());
        assertEquals(List.of("a", "b"), q.tags());
    }

    @Test
    void parse_hashWithoutText_isIgnored() {
        SearchQuery q = SearchParser.parse("# hello");

        assertEquals("hello", q.titleSubstring());
        assertEquals(List.of(), q.tags());
    }

    @Test
    void parse_preservesTitleSpacingAsSingleSpaces() {
        SearchQuery q = SearchParser.parse("  a   b \n  c  ");

        assertEquals("a b c", q.titleSubstring());
        assertEquals(List.of(), q.tags());
    }
}
