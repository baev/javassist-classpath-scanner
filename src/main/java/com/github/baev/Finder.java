package com.github.baev;

import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import org.scannotation.archiveiterator.Filter;
import org.scannotation.archiveiterator.IteratorFactory;
import org.scannotation.archiveiterator.StreamIterator;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.baev.JavassistUtils.findOne;
import static com.github.baev.JavassistUtils.isAnnotated;
import static java.util.Collections.emptyList;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.11.15
 */
public class Finder {

    public static final String STORIES_TYPE_NAME = "ru.yandex.qatools.allure.annotations.Stories";

    public static final String FEATURES_TYPE_NAME = "ru.yandex.qatools.allure.annotations.Features";

    public static final String TEST_TYPE_NAME = "org.junit.Test";

    private List<URL> classpath;

    public Finder(URL... classpathElements) {
        classpath = Arrays.asList(classpathElements);
    }

    public List<SearchResult> search() throws IOException, URISyntaxException {
        List<SearchResult> results = new ArrayList<>();

        for (ClassFile clazz : getClasses2()) {
            List<String> classFeatures = getFeatures(clazz);
            List<String> classStories = getStories(clazz);
            List<MethodInfo> testMethods = getTestMethods(clazz);

            for (MethodInfo method : testMethods) {
                Set<String> features = new HashSet<>(classFeatures);
                Set<String> stories = new HashSet<>(classStories);

                features.addAll(getFeatures(method));
                stories.addAll(getStories(method));

                results.add(new SearchResult(clazz.getName(), method.getName(), features, stories));
            }

        }
        return results;
    }

    public List<String> findClasses() throws IOException {
        return getClasses().stream()
                .filter(this::isTestClass)
                .map(ClassFile::getName)
                .collect(Collectors.toList());
    }

    protected List<ClassFile> getClasses2() throws URISyntaxException {
        ClasspathScanner scanner = new ClasspathScanner();
        for (URL url : classpath) {
            scanner.scan(url.toURI());
        }
        return new ArrayList<>(scanner.getClasses());
    }

    protected List<ClassFile> getClasses() throws IOException {
        List<ClassFile> classFiles = new ArrayList<>();
        for (URL url : classpath) {
            Filter filter = filename -> filename.endsWith(".class");

            StreamIterator it = IteratorFactory.create(url, filter);
            InputStream stream;
            while ((stream = it.next()) != null) {
                try (DataInputStream dstream = new DataInputStream(new BufferedInputStream(stream))) {
                    ClassFile classFile = new ClassFile(dstream);
                    classFiles.add(classFile);
                }
            }
        }
        return classFiles;
    }

    protected List<String> getFeatures(ClassFile clazz) {
        return findOne(clazz, FEATURES_TYPE_NAME)
                .map(JavassistUtils::getValueAsStringArray)
                .orElse(emptyList());
    }

    protected List<String> getStories(ClassFile clazz) {
        return findOne(clazz, STORIES_TYPE_NAME)
                .map(JavassistUtils::getValueAsStringArray)
                .orElse(emptyList());
    }

    protected List<String> getFeatures(MethodInfo method) {
        return findOne(method, FEATURES_TYPE_NAME)
                .map(JavassistUtils::getValueAsStringArray)
                .orElse(emptyList());
    }

    protected List<String> getStories(MethodInfo method) {
        return findOne(method, STORIES_TYPE_NAME)
                .map(JavassistUtils::getValueAsStringArray)
                .orElse(emptyList());
    }

    protected List<MethodInfo> getTestMethods(ClassFile clazz) {
        return Stream.of(clazz.getMethods().toArray())
                .filter(MethodInfo.class::isInstance)
                .map(MethodInfo.class::cast)
                .filter(this::isTestMethod)
                .collect(Collectors.toList());
    }

    protected boolean isTestClass(ClassFile clazz) {
        return !getTestMethods(clazz).isEmpty();
    }

    protected boolean isTestMethod(MethodInfo methodInfo) {
        return isAnnotated(methodInfo, TEST_TYPE_NAME);
    }
}
