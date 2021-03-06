package io.asfjava.ui.core.schema;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import io.asfjava.ui.core.FormDefinitionGeneratorFactory;
import io.asfjava.ui.dto.UiForm;

public final class UiFormSchemaGenerator {

	private static UiFormSchemaGenerator instance;

	public UiForm generate(Class<? extends Serializable> formDto) throws JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper, new CustomSchemaFactoryWrapper());
		JsonSchema schema = schemaGen.generateSchema(formDto);

		ArrayNode formDefinition = mapper.createArrayNode();

		Arrays.stream(formDto.getDeclaredFields()).forEach(field -> buildFormDefinition(mapper, formDefinition, field));

		return new UiForm(schema, formDefinition);
	}

	private void buildFormDefinition(ObjectMapper mapper, ArrayNode formDefinitions, Field field) {
		Arrays.stream(field.getAnnotations())
				.forEach(annotation -> buildFieldDefinition(field, annotation, mapper, formDefinitions));
	}

	private void buildFieldDefinition(Field field, Annotation annotation, ObjectMapper mapper,
			ArrayNode formDefinitions) {
		ObjectNode fieldFormDefinition = mapper.createObjectNode();
		FormDefinitionGeneratorFactory.getInstance().getGenerator(annotation.annotationType().getName())
				.ifPresent(generator -> {
					generator.generate(fieldFormDefinition, field);
					formDefinitions.add(fieldFormDefinition);
				});
	}

	public static UiFormSchemaGenerator get() {
		if (instance == null) {
			instance = new UiFormSchemaGenerator();
		}
		return instance;
	}

	private UiFormSchemaGenerator() {
	}

}
