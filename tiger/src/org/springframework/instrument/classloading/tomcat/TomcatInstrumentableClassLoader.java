/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.instrument.classloading.tomcat;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.catalina.loader.ResourceEntry;
import org.apache.catalina.loader.WebappClassLoader;

import org.springframework.instrument.classloading.WeavingTransformer;

/**
 * Extension of Tomcat's default class loader which adds instrumentation
 * to loaded classes without the need of using a VM-wide agent.
 *
 * <p>To be registered with a Loader tag in Tomcat's Context definition:
 * <code>
 * &lt;Loader loaderClass="org.springframework.instrument.classloading.tomcat.TomcatInstrumentableClassLoader"/&gt;
 * </code>
 *
 * <p>Typically used in combination with a ReflectiveLoadTimeWeaver
 * defined in the Spring application context. The <code>addTransformer</code> and
 * <code>getThrowawayClassLoader</code> methods mirror the corresponding methods
 * in the LoadTimeWeaver interface, as expected by ReflectiveLoadTimeWeaver.
 *
 * <p>See the PetClinic sample application for a full example of this
 * ClassLoader in action.
 *
 * <p><b>NOTE:</b> Requires Apache Tomcat version 5.0 or higher.
 *
 * @author Costin Leau
 * @since 2.0
 * @see #addTransformer
 * @see #getThrowawayClassLoader
 * @see org.springframework.instrument.classloading.ReflectiveLoadTimeWeaver
 */
public class TomcatInstrumentableClassLoader extends WebappClassLoader {

	/** Use an internal WeavingTransformer */
	private final WeavingTransformer weavingTransformer;


	public TomcatInstrumentableClassLoader() {
		super();
		this.weavingTransformer = new WeavingTransformer();
	}

	public TomcatInstrumentableClassLoader(ClassLoader classLoader) {
		super(classLoader);
		this.weavingTransformer = new WeavingTransformer(classLoader);
	}


	/**
	 * Delegate for LoadTimeWeaver's <code>addTransformer</code> method.
	 * Typically called through ReflectiveLoadTimeWeaver.
	 * @see org.springframework.instrument.classloading.LoadTimeWeaver#addTransformer
	 * @see org.springframework.instrument.classloading.ReflectiveLoadTimeWeaver
	 */
	public void addTransformer(ClassFileTransformer transformer) {
		this.weavingTransformer.addTransformer(transformer);
	}

	/**
	 * Delegate for LoadTimeWeaver's <code>getThrowawayClassLoader</code> method.
	 * Typically called through ReflectiveLoadTimeWeaver.
	 * @see org.springframework.instrument.classloading.LoadTimeWeaver#getThrowawayClassLoader
	 * @see org.springframework.instrument.classloading.ReflectiveLoadTimeWeaver
	 */
	public ClassLoader getThrowawayClassLoader() {
		WebappClassLoader tempLoader = new WebappClassLoader();
		// Use reflection to copy all the fields since most of them are private
		// on pre-5.5 Tomcat.
		shallowCopyFieldState(this, tempLoader);
		return tempLoader;
	}


	@Override
	protected ResourceEntry findResourceInternal(String name, String path) {
		ResourceEntry entry = super.findResourceInternal(name, path);
		// Postpone String parsing as much as possible (it is slow).
		if (entry != null && entry.binaryContent != null && path.endsWith(".class")) {
			byte[] transformed = this.weavingTransformer.transformIfNecessary(name, entry.binaryContent);
			entry.binaryContent = transformed;
		}
		return entry;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append("\r\n");
		sb.append(super.toString());
		return sb.toString();
	}


	// The code below is orginially taken from ReflectionUtils and optimized for
	// local usage. There is no dependency on ReflectionUtils to keep this class
	// self-contained (since it gets deployed into Tomcat's server class loader).

	/**
	 * Given the source object and the destination, which must be the same class
	 * or a subclass, copy all fields, including inherited fields. Designed to
	 * work on objects with public no-arg constructors.
	 * @throws IllegalArgumentException if arguments are incompatible or either
	 * is <code>null</code>
	 */
	private static void shallowCopyFieldState(final Object src, final Object dest) throws IllegalArgumentException {
		if (src == null) {
			throw new IllegalArgumentException("Source for field copy cannot be null");
		}
		if (dest == null) {
			throw new IllegalArgumentException("Destination for field copy cannot be null");
		}
		Class targetClass = findCommonAncestor(src.getClass(), dest.getClass());

		// Keep backing up the inheritance hierarchy.
		do {
			// Copy each field declared on this class unless it's static or
			// file.
			Field[] fields = targetClass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				// Skip static and final fields (the old FieldFilter)
				// do not copy resourceEntries - it's a cache that holds class entries.
				if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) ||
						field.getName().equals("resourceEntries"))) {
					try {
						// copy the field (the old FieldCallback)
						field.setAccessible(true);
						Object srcValue = field.get(src);
						field.set(dest, srcValue);
					}
					catch (IllegalAccessException ex) {
						throw new IllegalStateException("Shouldn't be illegal to access field '" + fields[i].getName()
								+ "': " + ex);
					}
				}
			}
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);
	}

	private static Class findCommonAncestor(Class one, Class two) throws IllegalArgumentException {
		Class ancestor = one;
		while (ancestor != Object.class || ancestor != null) {
			if (ancestor.isAssignableFrom(two)) {
				return ancestor;
			}
			ancestor = ancestor.getSuperclass();
		}
		// try the other class hierarchy
		ancestor = two;
		while (ancestor != Object.class || ancestor != null) {
			if (ancestor.isAssignableFrom(one)) {
				return ancestor;
			}
			ancestor = ancestor.getSuperclass();
		}
		return null;
	}

}
