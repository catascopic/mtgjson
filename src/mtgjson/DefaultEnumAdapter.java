package mtgjson;

import java.io.IOException;

import com.google.common.base.Enums;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class DefaultEnumAdapter<E extends Enum<E>> extends TypeAdapter<E> {

	private final Class<E> clazz;
	private final E defaultValue;

	public DefaultEnumAdapter(Class<E> clazz, E defaultValue) {
		this.clazz = clazz;
		this.defaultValue = defaultValue;
	}

	@Override
	public void write(JsonWriter out, E value) throws IOException {
		out.value(value.name());
	}

	@Override
	public E read(JsonReader in) throws IOException {
		return Enums.getIfPresent(clazz, in.nextString()).or(defaultValue);
	}

}
