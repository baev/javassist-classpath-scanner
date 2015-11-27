package com.github.baev;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javassist.bytecode.AnnotationsAttribute.visibleTag;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.11.15
 */
public final class JavassistUtils {

    public static final String VALUE = "value";

    JavassistUtils() {
    }

    public static List<String> getValueAsStringArray(Annotation annotation) {
        Function<MemberValue, List<String>> asStringArray = JavassistUtils::asStringArray;
        return getMemberValue(annotation, VALUE, asStringArray, Collections.emptyList());
    }

    public static <T> T getMemberValue(Annotation annotation, String name, Function<MemberValue, T> function, T defaultValue) {
        MemberValue memberValue = annotation.getMemberValue(name);
        return Optional.ofNullable(memberValue).map(function).orElse(defaultValue);
    }

    public static <T> List<T> asArrayOf(MemberValue value, Function<MemberValue, T> function) {
        ArrayMemberValue arrayValue = (ArrayMemberValue) value;
        return Arrays.asList(arrayValue.getValue()).stream()
                .map(function)
                .collect(Collectors.toList());
    }

    public static List<String> asStringArray(MemberValue value) {
        return asArrayOf(value, JavassistUtils::asString);
    }

    public static String asString(MemberValue value) {
        StringMemberValue stringValue = (StringMemberValue) value;
        return stringValue.getValue();
    }


    public static boolean isAnnotated(MethodInfo method, String annotationType) {
        return contains(getAnnotations(method), annotationType);
    }

    public static boolean isAnnotated(ClassFile clazz, String annotationType) {
        return contains(getAnnotations(clazz), annotationType);
    }

    public static boolean contains(List<Annotation> annotations, String annotationType) {
        return findOne(annotations, annotationType).isPresent();
    }

    public static Optional<Annotation> findOne(MethodInfo method, String annotationType) {
        return findOne(getAnnotations(method), annotationType);
    }

    public static Optional<Annotation> findOne(ClassFile clazz, String annotationType) {
        return findOne(getAnnotations(clazz), annotationType);
    }

    public static Optional<Annotation> findOne(List<Annotation> annotations, String annotationType) {
        return annotations.stream()
                .filter(annotation -> annotationType.equals(annotation.getTypeName()))
                .findAny();
    }

    public static List<Annotation> getAnnotations(MethodInfo method) {
        return getAnnotations((AnnotationsAttribute) method.getAttribute(visibleTag));
    }

    public static List<Annotation> getAnnotations(ClassFile clazz) {
        return getAnnotations((AnnotationsAttribute) clazz.getAttribute(visibleTag));
    }

    public static List<Annotation> getAnnotations(AnnotationsAttribute attribute) {
        return attribute == null ? Collections.emptyList() : Arrays.asList(attribute.getAnnotations());
    }
}
