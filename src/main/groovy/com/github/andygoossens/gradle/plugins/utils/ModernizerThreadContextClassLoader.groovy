/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.andygoossens.gradle.plugins.utils

import com.github.andygoossens.gradle.plugins.ModernizerPluginExtension

class ModernizerThreadContextClassLoader implements ThreadContextClassLoader {

    private ModernizerPluginExtension modernizerPluginExtension

    public ModernizerThreadContextClassLoader(ModernizerPluginExtension modernizerPluginExtension) {
        this.modernizerPluginExtension = modernizerPluginExtension
    }

    @Override
    void withClasspath(Set<File> classpath, Closure closure) {
        ClassLoader originalClassLoader = getClass().classLoader

        try {
            Thread.currentThread().contextClassLoader = createClassLoader(classpath ?: modernizerPluginExtension.classpath?.files)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.delegate = this
            closure()
        }
        finally {
            Thread.currentThread().contextClassLoader = originalClassLoader
        }
    }

    /**
     * Creates the classloader with the given classpath files.
     *
     * @param classpathFiles Classpath files
     * @return URL classloader
     */
    private URLClassLoader createClassLoader(Set<File> classpathFiles) {
        new URLClassLoader(toURLArray(classpathFiles), ClassLoader.systemClassLoader.parent)
    }

    /**
     * Creates URL array from a set of files.
     *
     * @param files Files
     * @return URL array
     */
    private URL[] toURLArray(Set<File> files) {
        files.collect { file -> file.toURI().toURL() } as URL[]
    }

    @Override
    Class loadClass(String className) {
        Thread.currentThread().contextClassLoader.loadClass(className)
    }
}
