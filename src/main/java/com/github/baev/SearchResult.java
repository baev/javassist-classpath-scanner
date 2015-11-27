package com.github.baev;

import java.util.Objects;
import java.util.Set;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.11.15
 */
public class SearchResult {

    private String className;

    private String methodName;

    private Set<String> features;

    private Set<String> stories;

    public SearchResult(String className, String methodName, Set<String> features, Set<String> stories) {
        this.className = className;
        this.methodName = methodName;
        this.features = features;
        this.stories = stories;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public Set<String> getFeatures() {
        return features;
    }

    public Set<String> getStories() {
        return stories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchResult that = (SearchResult) o;
        return Objects.equals(className, that.className) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(features, that.features) &&
                Objects.equals(stories, that.stories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, features, stories);
    }
}
