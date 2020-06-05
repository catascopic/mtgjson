package mtgjson;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.ComparisonChain;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class Expansion implements Comparable<Expansion> {

	String name;
	String code;
	List<Card> cards;
	@JsonAdapter(LocalDateAdapter.class)
	LocalDate releaseDate;
	Integer baseSetSize;
	Integer totalSetSize;
	Type type;
	boolean isForeignOnly;
	boolean isFoilOnly;
	boolean isOnlineOnly;
	boolean isPartialPreview;
	String parentCode;

	@Override
	public int compareTo(Expansion o) {
		return ComparisonChain.start()
				.compare(releaseDate, o.releaseDate)
				.compare(type, o.type)
				.compare(name, o.name)
				.result();
	}

	@Override
	public String toString() {
		return name;
	}

	public enum Type {

		expansion,
		core,
		draft_innovation,
		
		starter,

		masters,
		
		commander,
		planechase,
		
		from_the_vault,
		masterpiece, 
		spellbook,
		promo, 

		premium_deck, 
		duel_deck, 
		archenemy,
		box,
		
		funny,
		memorabilia,
		
		token, 
		treasure_chest,
		vanguard;
	}

	public class LocalDateAdapter extends TypeAdapter<LocalDate> {

		@Override
		public void write(JsonWriter out, LocalDate value) throws IOException {
			out.value(value.toString());
		}

		@Override
		public LocalDate read(JsonReader in) throws IOException {
			return LocalDate.parse(in.nextString());
		}
	}

}
