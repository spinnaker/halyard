/*
 * Copyright 2019 Armory, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.halyard.deploy.config.v1.secrets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.netflix.spinnaker.config.secrets.EncryptedSecret;
import com.netflix.spinnaker.halyard.config.config.v1.secrets.SecretSessionManager;
import com.netflix.spinnaker.halyard.config.model.v1.node.Secret;
import com.netflix.spinnaker.halyard.config.model.v1.node.SecretFile;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile.Profile;
import org.apache.commons.lang.RandomStringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

/**
 * DecryptingObjectMapper serializes (part of) Halyard configurations, decrypting secrets contained in fields
 * annotated with @Secret.
 *
 * It also decrypts the content of secret files, assign them a random name, and store them
 * in {@link Profile} to be serialized later.
 *
 * decryptedOutputDirectory is the path to the decrypted secret files on the service's host.
 */
public class DecryptingObjectMapper extends ObjectMapper {
    private enum SecretType {
        SECRET_FILE, SECRET, NO_SECRET
    }

    protected Profile profile;
    protected Path decryptedOutputDirectory;

    public DecryptingObjectMapper(SecretSessionManager secretSessionManager, Profile profile, Path decryptedOutputDirectory) {
        super();
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        this.profile = profile;
        this.decryptedOutputDirectory = decryptedOutputDirectory;

        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {

            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                Class _class = beanDesc.getBeanClass();

                for (Iterator<BeanPropertyWriter> it = beanProperties.iterator(); it.hasNext(); ) {
                    BeanPropertyWriter bpw = it.next();

                    switch (getFieldSecretType(_class, bpw.getName())) {
                        case SECRET:
                            // Decrypt the field secret before sending
                            bpw.assignSerializer(new StdScalarSerializer<Object>(String.class, false) {
                                @Override
                                public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                                    if (value != null) {
                                        String sValue = value.toString();
                                        if (!EncryptedSecret.isEncryptedSecret(sValue)) {
                                            gen.writeString(sValue);
                                        } else {
                                            gen.writeString(secretSessionManager.decrypt(sValue));
                                        }
                                    }
                                }
                            });
                            break;
                        case SECRET_FILE:
                            bpw.assignSerializer(new StdScalarSerializer<Object>(String.class, false) {
                                @Override
                                public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                                    if (value != null) {
                                        String sValue = value.toString();
                                        if (EncryptedSecret.isEncryptedSecret(sValue)) {
                                            // Decrypt the content of the file and store on the profile under a random
                                            // generated file name
                                            String decrypted = secretSessionManager.decrypt(sValue);
                                            String name = newRandomFilePath(bpw.getName());
                                            profile.getDecryptedFiles().put(name, decrypted);
                                            gen.writeString(getCompleteFilePath(name));
                                        } else {
                                            gen.writeString(sValue);
                                        }
                                    }
                                }
                            });
                    }
                }
                return beanProperties;
            }
        });
        this.registerModule(module);
    }

    public DecryptingObjectMapper relax() {
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
        return this;
    }

    protected SecretType getFieldSecretType(Class _class, String fieldName) {
        for (Field f : _class.getDeclaredFields()) {
            if (f.getName().equals(fieldName)) {
                if (f.isAnnotationPresent(Secret.class)) {
                    return SecretType.SECRET;
                }
                if (f.isAnnotationPresent(SecretFile.class)) {
                    return SecretType.SECRET_FILE;
                }
                return SecretType.NO_SECRET;
            }
        }
        if (_class.getSuperclass() != null) {
            return getFieldSecretType(_class.getSuperclass(), fieldName);
        }
        return SecretType.NO_SECRET;
    }


    protected String newRandomFilePath(String fieldName) {
        return fieldName + "-" + RandomStringUtils.randomAlphanumeric(5);
    }

    protected String getCompleteFilePath(String filename) {
        return Paths.get(decryptedOutputDirectory.toString(), filename).toString();
    }
}