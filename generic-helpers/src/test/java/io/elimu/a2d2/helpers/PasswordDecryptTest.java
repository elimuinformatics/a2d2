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

package io.elimu.a2d2.helpers;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PasswordDecryptTest {

    @BeforeClass
    public static void setup() {
        String salt = System.getenv("jasypt_encryptor_password");
        org.junit.Assume.assumeTrue(salt != null);
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        String value = "to_be_encrypted";
        String valueEncrypted = PasswordDecryptUtil.encrypt(value);
        Assert.assertNotNull(valueEncrypted);
        System.out.println(valueEncrypted);
        String valueDecrypted = PasswordDecryptUtil.decrypt(valueEncrypted);
        Assert.assertEquals(value, valueDecrypted);
    }
}
