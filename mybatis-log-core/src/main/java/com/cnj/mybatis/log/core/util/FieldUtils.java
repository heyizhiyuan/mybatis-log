package com.cnj.mybatis.log.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * @author czz
 * @since 2022/11/13 上午11:14
 */
public class FieldUtils {

    private FieldUtils() {
    }

    /**
     * Gets an accessible {@link Field} by name respecting scope. Only the specified class will be considered.
     *
     * @param cls       the {@link Class} to reflect, must not be {@code null}
     * @param fieldName the field name to obtain
     * @return the Field object
     * @throws IllegalArgumentException if the class is {@code null}, or the field name is blank or empty
     */
    public static Field getDeclaredField(final Class<?> cls, final String fieldName) {
        return getDeclaredField(cls, fieldName, false);
    }

    /**
     * Gets an accessible {@link Field} by name, breaking scope if requested. Only the specified class will be
     * considered.
     *
     * @param cls         the {@link Class} to reflect, must not be {@code null}
     * @param fieldName   the field name to obtain
     * @param forceAccess whether to break scope restrictions using the
     *                    {@link java.lang.reflect.AccessibleObject#setAccessible(boolean)} method. {@code false} will only
     *                    match {@code public} fields.
     * @return the Field object
     * @throws IllegalArgumentException if the class is {@code null}, or the field name is blank or empty
     */
    public static Field getDeclaredField(final Class<?> cls, final String fieldName, final boolean forceAccess) {
        try {
            // only consider the specified class by using getDeclaredField()
            final Field field = cls.getDeclaredField(fieldName);
            if (!isAccessible(field)) {
                if (forceAccess) {
                    field.setAccessible(true);
                } else {
                    return null;
                }
            }
            return field;
        } catch (final NoSuchFieldException e) { // NOPMD
            // ignore
        }
        return null;
    }

    static boolean isAccessible(final Member m) {
        return m != null && Modifier.isPublic(m.getModifiers()) && !m.isSynthetic();
    }

}
