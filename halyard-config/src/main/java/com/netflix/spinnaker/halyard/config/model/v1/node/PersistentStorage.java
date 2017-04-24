package com.netflix.spinnaker.halyard.config.model.v1.node;

import com.netflix.spinnaker.halyard.config.model.v1.persistentStorage.AzsPersistentStore;
import com.netflix.spinnaker.halyard.config.model.v1.persistentStorage.GcsPersistentStore;
import com.netflix.spinnaker.halyard.config.model.v1.persistentStorage.S3PersistentStore;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
public class PersistentStorage extends Node {
  String persistentStoreType;
  S3PersistentStore s3 = new S3PersistentStore();
  GcsPersistentStore gcs = new GcsPersistentStore();
  AzsPersistentStore azs = new AzsPersistentStore();

  @Override
  public void accept(ConfigProblemSetBuilder psBuilder, Validator v) {
    v.validate(psBuilder, this);
  }

  @Override
  public String getNodeName() {
    return "persistentStorage";
  }

  @Override
  public NodeIterator getChildren() {
    return NodeIteratorFactory.makeReflectiveIterator(this);
  }

  public static Class<? extends PersistentStore> translatePersistentStoreType(String persistentStoreType) {
    Optional<? extends Class<?>> res = Arrays.stream(PersistentStorage.class.getDeclaredFields())
        .filter(f -> f.getName().equals(persistentStoreType))
        .map(Field::getType)
        .findFirst();

    if (res.isPresent()) {
      return (Class<? extends PersistentStore>) res.get();
    } else {
      throw new IllegalArgumentException("No persistent store with name \"" + persistentStoreType + "\" handled by halyard.");
    }
  }
}
