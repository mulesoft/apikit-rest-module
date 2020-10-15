/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import sun.reflect.ConstructorAccessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MockingUtils {

  public static <T> T createEnumValue(Class<T> enumClass, String name, int ordinal, String description) throws Exception {
    Class<T> monsterClass = enumClass;
    Constructor<?> constructor = monsterClass.getDeclaredConstructors()[0];
    constructor.setAccessible(true);

    Field constructorAccessorField = Constructor.class.getDeclaredField("constructorAccessor");
    constructorAccessorField.setAccessible(true);
    ConstructorAccessor ca = (ConstructorAccessor) constructorAccessorField.get(constructor);
    if (ca == null) {
      Method acquireConstructorAccessorMethod = Constructor.class.getDeclaredMethod("acquireConstructorAccessor");
      acquireConstructorAccessorMethod.setAccessible(true);
      ca = (ConstructorAccessor) acquireConstructorAccessorMethod.invoke(constructor);
    }
    T enumValue =
        (T) ca.newInstance(description != null ? new Object[] {name, ordinal, description} : new Object[] {name, ordinal});
    return enumValue;
  }

  public static void setAccessible(Field field, Object newValue) throws Exception {
    field.setAccessible(true);
    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    field.set(null, newValue);
  }
}
