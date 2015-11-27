package com.github.baev;

import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.11.15
 */
public class FinderTest {

    @Test
    public void shouldFindClasses() throws Exception {
        URL tests = getClass().getClassLoader().getResource("tests.jar");
        URL source = getClass().getClassLoader().getResource("source.jar");
        List<String> classes = new Finder(tests, source).findClasses();
        assertThat(classes, notNullValue());
        assertThat(classes, hasSize(1));
        assertThat(classes, hasItems("com.github.baev.MyTest"));
    }

    @Test
    public void shouldFindStories() throws Exception {
        URL tests = getClass().getClassLoader().getResource("tests.jar");
        URL source = getClass().getClassLoader().getResource("source.jar");

        List<SearchResult> search = new Finder(tests, source).search();
        assertThat(search, notNullValue());
        assertThat(search, hasSize(3));

        SearchResult firstTest = new SearchResult(
                "com.github.baev.MyTest",
                "firstTest",
                set("class feature", "test feature"),
                set("first class story", "second class story")
        );

        SearchResult secondTest = new SearchResult(
                "com.github.baev.MyTest",
                "secondTest",
                set("class feature"),
                set("first class story", "second class story")
        );

        SearchResult thirdTest = new SearchResult(
                "com.github.baev.MyTest",
                "thirdTest",
                set("class feature"),
                set("first class story", "second class story", "test story")
        );

        assertThat(search, hasItems(firstTest, secondTest, thirdTest));
    }

    private Set<String> set(String... items) {
        return new HashSet<>(Arrays.asList(items));
    }
}