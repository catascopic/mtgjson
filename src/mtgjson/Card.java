package mtgjson;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Splitter;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class Card implements Comparable<Card> {

	String name;
	String faceName;
	String text = "";
	Identifiers identifiers;
	@JsonAdapter(CollectorNumberAdapter.class)
	CollectorNumber number;
	BorderColor borderColor;
	Layout layout;
	String artist;
	Map<Format, Legality> legalities;

	HashSet<Type> types;
	Number convertedManaCost;

	boolean isAlternative;
	boolean isFullArt;
	boolean isOnlineOnly;
	boolean isPaper;
	boolean isPromo;
	boolean isOversized;

	boolean isMtgo;

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Card)) {
			return false;
		}
		Card that = (Card) obj;
		return this.name.equals(that.name);
	}

	@Override
	public int compareTo(Card o) {
		return this.name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return name;
	}

	public static class Identifiers {
		@JsonAdapter(UUIDAdapter.class)
		UUID scryfallId;
	}

	public enum Type {
		Artifact,
		Creature,
		Enchantment,
		Instant,
		Land,
		Planeswalker,
		Sorcery,
		Tribal,

		Conspiracy,

		Scheme,
		Phenomenon,
		Plane,

		Vanguard;
	}

	public enum BorderColor {
		black, borderless, gold, silver, white;
	}

	public enum Layout {
		normal, split, flip, transform, meld, leveler, saga, planar, scheme, vanguard, token,
		double_faced_token, emblem, augment, aftermath, host, adventure, modal_dfc;
	}

	public enum Format {
		brawl, commander, duel, future, frontier, historic, legacy, modern, oldschool, pauper,
		penny, pioneer, standard, vintage;
	}

	public enum Legality {
		Legal, Restricted, Banned;
	}

	public class UUIDAdapter extends TypeAdapter<UUID> {

		@Override
		public void write(JsonWriter out, UUID value) throws IOException {
			out.value(value.toString());
		}

		@Override
		public UUID read(JsonReader in) throws IOException {
			return UUID.fromString(in.nextString());
		}
	}

	public class CollectorNumberAdapter extends TypeAdapter<CollectorNumber> {

		@Override
		public void write(JsonWriter out, CollectorNumber value) throws IOException {
			out.value(value.toString());
		}

		@Override
		public CollectorNumber read(JsonReader in) throws IOException {
			return new CollectorNumber(in.nextString());
		}
	}

}
