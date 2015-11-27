package com.github.baev;

import javassist.bytecode.ClassFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 27.11.15
 */
public class ClasspathScannerTest {

    public static final String TESTJAR1 = "testjar1-1.0-SNAPSHOT-tests.jar";

    public static final String TESTJAR2 = "testjar2-1.0-SNAPSHOT-tests.jar";

    public static final String MANIFESTDEPJAR = "manifestdepjar-1.0-SNAPSHOT.jar";

    public static final String WITHOUTCLASSESJAR = "withoutclassesjar-1.0-SNAPSHOT.jar";

    public static final String FIRST_TEST = "com.github.baev.FirstTest";

    public static final String SECOND_TEST = "com.github.baev.SecondTest";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldFindAllClassesInFolder() throws Exception {
        File dir = folder.newFolder();
        ZipUtil.unpack(getResourceAsFile(TESTJAR1), dir);

        Set<ClassFile> classes = getClassFiles(dir.toURI());
        assertThat(classes, notNullValue());
        assertThat(classes, hasSize(1));
        ClassFile next = classes.iterator().next();
        assertThat(next.getName(), is(FIRST_TEST));
    }

    @Test
    public void shouldFindAllClassesInJar() throws Exception {
        Set<ClassFile> classes = getClassFiles(TESTJAR2);
        assertThat(classes, notNullValue());
        assertThat(classes, hasSize(1));
        ClassFile next = classes.iterator().next();
        assertThat(next.getName(), is(SECOND_TEST));
    }

    @Test
    public void shouldReadJarManifestClasspath() throws Exception {
        Set<ClassFile> classes = getClassFiles(MANIFESTDEPJAR);
        assertThat(classes, notNullValue());
        assertThat(classes, hasSize(2));
        List<String> strings = classes.stream().map(ClassFile::getName).collect(Collectors.toList());

        assertThat(strings, hasItems(FIRST_TEST, SECOND_TEST));
    }

    @Test
    public void shouldNotFailIfNoClasses() throws Exception {
        Set<ClassFile> classes = getClassFiles(WITHOUTCLASSESJAR);
        assertThat(classes, notNullValue());
        assertThat(classes, hasSize(0));
    }

    @Test
    public void shouldFindAllClasses() throws Exception {
        Set<ClassFile> classes = getClassFiles(TESTJAR1, TESTJAR2, WITHOUTCLASSESJAR);
        assertThat(classes, notNullValue());
        assertThat(classes, hasSize(2));
        List<String> strings = classes.stream().map(ClassFile::getName).collect(Collectors.toList());

        assertThat(strings, hasItems(FIRST_TEST, SECOND_TEST));
    }

    public static Set<ClassFile> getClassFiles(URI... uris) throws URISyntaxException {
        return ClasspathScanner.getAllClasses(uris);
    }

    public static Set<ClassFile> getClassFiles(String... resources) throws URISyntaxException {
        List<URI> urls = Arrays.asList(resources).stream()
                .map(ClasspathScannerTest::getResource)
                .map(ClasspathScannerTest::safeToUri)
                .collect(Collectors.toList());
        return ClasspathScanner.getAllClasses(urls);
    }

    public static URL getResource(String resourceName) {
        return ClasspathScannerTest.class.getClassLoader().getResource(resourceName);
    }

    public static File getResourceAsFile(String resourceName) throws URISyntaxException {
        return new File(getResource(resourceName).toURI());
    }

    public static URI safeToUri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}