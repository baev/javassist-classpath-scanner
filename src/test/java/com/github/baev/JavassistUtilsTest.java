package com.github.baev;

import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.baev.ClasspathScannerTest.getClassFiles;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 27.11.15
 */
public class JavassistUtilsTest {

    public static final String INIT = "<init>";
    public static final String FIRST_TEST = "firstTest";
    public static final String SECOND_TEST = "secondTest";
    public static final String THIRD_TEST = "thirdTest";

    public static final String STORIES = "ru.yandex.qatools.allure.annotations.Stories";
    public static final String FEATURES = "ru.yandex.qatools.allure.annotations.Features";
    public static final String TEST = "org.junit.Test";

    @Test
    public void shouldFindAllMethods() throws Exception {
        ClassFile next = getClassFile();
        List<String> methods = JavassistUtils.getMethods(next).stream()
                .map(MethodInfo::getName).collect(Collectors.toList());

        assertThat(methods, notNullValue());
        assertThat(methods, hasSize(4));
        assertThat(methods, hasItems(INIT, FIRST_TEST, SECOND_TEST, THIRD_TEST));
    }

    @Test
    public void shouldFindAllTestMethods() throws Exception {
        ClassFile next = getClassFile();
        List<String> methods = JavassistUtils.getMethods(next, x -> x.getName().endsWith("Test")).stream()
                .map(MethodInfo::getName).collect(Collectors.toList());

        assertThat(methods, notNullValue());
        assertThat(methods, hasSize(3));
        assertThat(methods, hasItems(FIRST_TEST, SECOND_TEST, THIRD_TEST));
    }

    @Test
    public void shouldGetListOfClassAnnotations() throws Exception {
        ClassFile next = getClassFile();
        List<String> annotations = JavassistUtils.getAnnotations(next).stream()
                .map(Annotation::getTypeName).collect(Collectors.toList());

        assertThat(annotations, notNullValue());
        assertThat(annotations, hasSize(2));
        assertThat(annotations, hasItems(STORIES, FEATURES));
    }

    @Test
    public void shouldGetListOfMethodAnnotations() throws Exception {
        ClassFile next = getClassFile();
        MethodInfo firstTest = next.getMethod(FIRST_TEST);

        List<String> annotations = JavassistUtils.getAnnotations(firstTest).stream()
                .map(Annotation::getTypeName).collect(Collectors.toList());

        assertThat(annotations, notNullValue());
        assertThat(annotations, hasSize(2));
        assertThat(annotations, hasItems(TEST, FEATURES));
    }

    @Test
    public void shouldFindClassAnnotation() throws Exception {
        ClassFile next = getClassFile();
        Annotation annotation = JavassistUtils.findOne(next, FEATURES).get();

        assertThat(annotation.getTypeName(), is(FEATURES));
    }

    @Test
    public void shouldFindMethodAnnotation() throws Exception {
        ClassFile next = getClassFile();
        MethodInfo firstTest = next.getMethod(FIRST_TEST);

        Annotation annotation = JavassistUtils.findOne(firstTest, FEATURES).get();

        assertThat(annotation.getTypeName(), is(FEATURES));
    }

    @Test
    public void shouldContainsClazzAnnotation() throws Exception {
        ClassFile next = getClassFile();

        boolean contains = JavassistUtils.isAnnotated(next, FEATURES);

        assertThat(contains, is(true));
    }

    @Test
    public void shouldContainsMethodAnnotation() throws Exception {
        ClassFile next = getClassFile();
        MethodInfo firstTest = next.getMethod(FIRST_TEST);

        boolean contains = JavassistUtils.isAnnotated(firstTest, FEATURES);

        assertThat(contains, is(true));
    }

    @Test
    public void shouldGetAnnotationValueAsStringArray() throws Exception {
        ClassFile next = getClassFile();
        List<String> strings = JavassistUtils.findOne(next, FEATURES)
                .map(JavassistUtils::getValueAsStringArray).get();

        assertThat(strings, notNullValue());
        assertThat(strings, hasSize(1));
        assertThat(strings, hasItems("class feature"));
    }

    public static ClassFile getClassFile() throws URISyntaxException {
        return getClassFiles(ClasspathScannerTest.TESTJAR1).iterator().next();
    }
}