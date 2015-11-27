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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javassist.bytecode.AnnotationsAttribute.visibleTag;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.11.15
 */
public final class JavassistUtils {

    public static final String VALUE = "value";

    JavassistUtils() {
    }

    /**
     * Get all methods from given class.
     */
    public static List<MethodInfo> getMethods(ClassFile clazz) {
        return getMethods(clazz, x -> true);
    }

    /**
     * Get all methods matches predicate from given class.
     */
    public static List<MethodInfo> getMethods(ClassFile clazz, Predicate<MethodInfo> predicate) {
        return Stream.of(clazz.getMethods().toArray())
                .filter(MethodInfo.class::isInstance)
                .map(MethodInfo.class::cast)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Get value of given annotation as string array.
     */
    public static List<String> getValueAsStringArray(Annotation annotation) {
        Function<MemberValue, List<String>> asStringArray = JavassistUtils::asStringArray;
        return getMemberValue(annotation, VALUE, asStringArray, Collections.emptyList());
    }

    /**
     * Convert the given member to string array.
     */
    public static List<String> asStringArray(MemberValue value) {
        return asArrayOf(value, JavassistUtils::asString);
    }

    /**
     * Returns the value of the member in case member is instance of {@link StringMemberValue}
     * and empty string otherwise.
     */
    public static String asString(MemberValue value) {
        return Optional.ofNullable(value)
                .filter(StringMemberValue.class::isInstance)
                .map(StringMemberValue.class::cast)
                .map(StringMemberValue::getValue)
                .orElse("");
    }

    /**
     * Returns the value of the given member as an array.
     */
    public static <T> List<T> asArrayOf(MemberValue value, Function<MemberValue, T> function) {
        return Collections.singletonList(value).stream()
                .filter(Objects::nonNull)
                .filter(ArrayMemberValue.class::isInstance)
                .map(ArrayMemberValue.class::cast)
                .map(ArrayMemberValue::getValue)
                .map(Arrays::asList)
                .flatMap(List::stream)
                .map(function)
                .collect(Collectors.toList());
    }

    /**
     * Get member value and convert it using given function.
     */
    public static <T> T getMemberValue(Annotation annotation, String name, Function<MemberValue, T> function, T defaultValue) {
        MemberValue memberValue = annotation.getMemberValue(name);
        return Optional.ofNullable(memberValue).map(function).orElse(defaultValue);
    }

    /**
     * Returns true if method annotated with given annotation.
     */
    public static boolean isAnnotated(MethodInfo method, String annotationType) {
        return contains(getAnnotations(method), annotationType);
    }

    /**
     * Returns true if class annotated with given annotation.
     */
    public static boolean isAnnotated(ClassFile clazz, String annotationType) {
        return contains(getAnnotations(clazz), annotationType);
    }

    /**
     * Returns true if given list contains specified annotation.
     */
    public static boolean contains(List<Annotation> annotations, String annotationType) {
        return findOne(annotations, annotationType).isPresent();
    }

    /**
     * Find method annotation by given annotation class.
     */
    public static Optional<Annotation> findOne(MethodInfo method, Class<?> annotationType) {
        return findOne(getAnnotations(method), annotationType.getCanonicalName());
    }

    /**
     * Find method annotation by given type.
     */
    public static Optional<Annotation> findOne(MethodInfo method, String annotationType) {
        return findOne(getAnnotations(method), annotationType);
    }

    /**
     * Find class annotation by given type.
     */
    public static Optional<Annotation> findOne(ClassFile clazz, Class<?> annotationType) {
        return findOne(getAnnotations(clazz), annotationType.getCanonicalName());
    }

    /**
     * Find class annotation by given type.
     */
    public static Optional<Annotation> findOne(ClassFile clazz, String annotationType) {
        return findOne(getAnnotations(clazz), annotationType);
    }

    /**
     * Find annotation by given type.
     */
    public static Optional<Annotation> findOne(List<Annotation> annotations, String annotationType) {
        return annotations.stream()
                .filter(annotation -> annotationType.equals(annotation.getTypeName()))
                .findAny();
    }

    /**
     * Get method annotations.
     */
    public static List<Annotation> getAnnotations(MethodInfo method) {
        return getAnnotations((AnnotationsAttribute) method.getAttribute(visibleTag));
    }

    /**
     * Get class annotations.
     */
    public static List<Annotation> getAnnotations(ClassFile clazz) {
        return getAnnotations((AnnotationsAttribute) clazz.getAttribute(visibleTag));
    }

    /**
     * Get annotations from given attribute.
     */
    public static List<Annotation> getAnnotations(AnnotationsAttribute attribute) {
        return Optional.ofNullable(attribute)
                .map(AnnotationsAttribute::getAnnotations)
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }
}
