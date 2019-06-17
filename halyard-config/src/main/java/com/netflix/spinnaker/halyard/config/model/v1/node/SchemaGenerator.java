package com.netflix.spinnaker.halyard.config.model.v1.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

public final class SchemaGenerator {
    private SchemaGenerator() { };
    public static void main(String[] args) throws Exception {
        System.out.println(SchemaGenerator.getJsonSchema(Providers.class));
    }

 
    public static <T> String getJsonSchema(Class<T> c) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        //JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);
        JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper, JsonSchemaConfig.html5EnabledSchema() );
        JsonNode jsonSchema = jsonSchemaGenerator.generateJsonSchema(c);
        String jsonSchemaAsString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema);  
        return jsonSchemaAsString;

    }
    
}