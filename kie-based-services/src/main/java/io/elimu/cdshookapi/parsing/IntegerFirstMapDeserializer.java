package io.elimu.cdshookapi.parsing;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class IntegerFirstMapDeserializer implements JsonDeserializer<Map<String, Object>> {

	@Override
	public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
			throws JsonParseException {
		Map<String, Object> map = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
			JsonElement el = entry.getValue();
			Object o = null;
			if (el.isJsonArray()) {
				o = deserializeArray(typeOfT, ctx, el);
			} else if (el.isJsonObject()) {
				o = deserialize(el, typeOfT, ctx);
			} else {
				o = deserializePrim(el);
			}
			map.put(entry.getKey(), o);
		}
		return map;
	}

	private Object deserializeArray(Type typeOfT,
			JsonDeserializationContext ctx, JsonElement el) {
		Object o;
		List<Object> grab = new ArrayList<>(el.getAsJsonArray().size());
		for (JsonElement e : el.getAsJsonArray()) {
			Object subO = null;
			if (e.isJsonObject()) {
				subO = deserialize(e, typeOfT, ctx);
			} else if (e.isJsonPrimitive()) {
				subO = deserializePrim(e);
			} else if (e.isJsonArray()) {
				subO = deserializeArray(typeOfT, ctx, e);
			}
			grab.add(subO);
		}
		o = grab;
		return o;
	}

	private Object deserializePrim(JsonElement el) {
		Object o = null;
		try {
			float f = el.getAsFloat();
			// not loose precision
			if (Math.ceil(f) == f) {
				o = (int) f;
			} else {
				o = f;
			}
		} catch (Exception ignored) {
			//no-op
		}
		if (o == null) {
			try {
				o = el.getAsString();
			} catch (Exception ignored) {
				//no-op
			}
		}
		return o;
	}
}
