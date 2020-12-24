package mtgjson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;

import mtgjson.Card.BorderColor;
import mtgjson.Card.CollectorNumberAdapter;
import mtgjson.Card.Format;
import mtgjson.Card.Layout;

public class MtgJson {

	public static void main(String[] args) throws IOException {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Collection<Expansion> expansions;
		try (Reader reader = Files.newBufferedReader(Paths.get("AllPrintings.json"))) {
			Map<String, Expansion> json = gson.fromJson(reader,
					new TypeToken<Map<String, Expansion>>() {}.getType());
			expansions = json.values();
		}

		TreeMultimap<String, Printing> printings = TreeMultimap.create();
		TreeMultimap<String, Printing> meld = TreeMultimap.create();

		for (Expansion expansion : expansions) {
			if (isReal(expansion)) {
				for (Card card : expansion.cards) {
					if (isReal(card)) {
						if (isRepresentative(card)) {
							printings.put(getName(card), new Printing(card, expansion));
						} else if (card.layout == Layout.meld) {
							meld.put(getName(card), new Printing(card, expansion));
						}
					}
				}
			}
		}

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("printings.jsonp"))) {
			writer.write("initPrintings(");
			gson.toJson(Multimaps.transformValues(printings, Image::new).asMap(), writer);
			writer.write(");");
		}
	}

	private static String getName(Card card) {
		switch (card.layout) {
		case split:
		case aftermath:
			return String.join(" & ", card.names);
		default:
			return card.name;
		}
	}

	private static boolean isReal(Expansion expansion) {
		return !expansion.isOnlineOnly
				&& !expansion.isForeignOnly
				&& !expansion.isPartialPreview
				&& !expansion.name.contains("Salvat");
	}

	private static boolean isReal(Card card) {
		return card.legalities.containsKey(Format.legacy)
				&& !card.isOversized
				&& !card.isOnlineOnly
				&& card.borderColor != BorderColor.gold;
	}

	private static boolean isRepresentative(Card card) {
		return card.names.isEmpty() || !card.name.equals(card.names.get(1));
	}


	private static Tier getTier(Expansion expansion) {
		switch (expansion.type) {
		case core:
		case expansion:
			return Tier.STANDARD_RELEASE;
		case draft_innovation:
			return Tier.BOOSTER_RELEASE;
		case masters:
			return Tier.BOOSTER_REPRTINT;
		case commander:
		case planechase:
			return expansion.name.contains("Anthology")
					? Tier.DECK
					: Tier.NON_BOOSTER_RELEASE;
		case from_the_vault:
		case masterpiece:
		case promo:
		case spellbook:
			return Tier.SPECIAL;
		case starter:
			return Tier.STARTER;
		case archenemy:
		case box:
		case duel_deck:
		case premium_deck:
			return Tier.DECK;
		default:
			return Tier.OTHER;
		}
	}

	private static class Image {

		UUID id;
		String code;
		@JsonAdapter(CollectorNumberAdapter.class)
		CollectorNumber number;
		Boolean back;
		UUID meld;

		Image(Printing printing) {
			this.id = printing.card.identifiers.scryfallId;
			this.code = printing.expansion.code;
			this.number = printing.card.number;
			if (printing.card.layout == Layout.transform) {
				back = true;
			}
		}
	}

	enum Tier {
		STANDARD_RELEASE,
		BOOSTER_RELEASE,
		STARTER,
		NON_BOOSTER_RELEASE,
		BOOSTER_REPRTINT,
		SPECIAL,
		DECK,
		OTHER;
	}

	static class Printing implements Comparable<Printing> {

		Card card;
		Expansion expansion;

		Printing(Card card, Expansion expansion) {
			this.card = card;
			this.expansion = expansion;
		}

		Tier getTier() {
			if (card.number.number > expansion.baseSetSize) {
				return Tier.OTHER;
			}
			return MtgJson.getTier(expansion);
		}

		@Override
		public int hashCode() {
			return Objects.hash(expansion, card.number);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Printing)) {
				return false;
			}
			Printing that = (Printing) obj;
			return this.expansion.equals(that.expansion)
					&& this.card.number.equals(that.card.number);
		}

		@Override
		public int compareTo(Printing o) {
			return ComparisonChain.start()
					.compare(getTier(), o.getTier())
					.compare(expansion.releaseDate, o.expansion.releaseDate)
					.compare(card.number, o.card.number)
					.result();
		}

		@Override
		public String toString() {
			return card + " in " + expansion + " #" + card.number;
		}
	}

}
