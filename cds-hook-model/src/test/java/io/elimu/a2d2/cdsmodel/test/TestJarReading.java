// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.a2d2.cdsmodel.test;

import static org.junit.Assert.*;

import java.util.jar.JarFile;
import org.junit.Test;

public class TestJarReading {

	@Test
	public void test() throws Exception {
		JarFile jarWithKmodule = new JarFile(getClass().getResource("/with-kmodule.jar").getFile());
		JarFile jarWithoutKmodule = new JarFile(getClass().getResource("/without-kmodule.jar").getFile());
		assertNull(jarWithoutKmodule.getJarEntry("META-INF/kmodule.xml"));
		assertNotNull(jarWithKmodule.getJarEntry("META-INF/kmodule.xml"));
	}

}
