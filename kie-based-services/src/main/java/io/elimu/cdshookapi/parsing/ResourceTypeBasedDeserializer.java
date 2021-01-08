package io.elimu.cdshookapi.parsing;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ResourceTypeBasedDeserializer<T extends Object>  implements JsonDeserializer<T>, JsonSerializer<T> {

	private final ClassLoader serviceClassLoader;
	private final GsonBuilder builder;

	public ResourceTypeBasedDeserializer(ClassLoader serviceClassLoader, GsonBuilder builder) {
		this.serviceClassLoader = serviceClassLoader;
		this.builder = builder;
	}
	
	@Override
	public JsonElement serialize(T step, Type type, JsonSerializationContext ctx) {
		JsonObject retval = new Gson().toJsonTree(step).getAsJsonObject();
		retval.addProperty("resourceType", step.getClass().getName());
		return retval;
	}
	
	@Override @SuppressWarnings("unchecked")
	public T deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		JsonObject stepObject = json.getAsJsonObject();
		try {
			Class<? extends T> clzType = (Class<? extends T>) Class.forName(
					stepObject.get("resourceType").getAsString(), true, serviceClassLoader);
			return builder.create().fromJson(stepObject, clzType);
		} catch (Exception e) {
			throw new JsonParseException("Cannot determine type for case", e);
		}
	}


}
