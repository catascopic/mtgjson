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

			if (!expansion.isOnlineOnly
					&& !expansion.isForeignOnly
					&& !expansion.isPartialPreview
					&& !expansion.name.contains("Salvat")
					&& expansion.type != Expansion.Type.memorabilia) {
				for (Card card : expansion.cards) {
					if (card.legalities.containsKey(Format.legacy)) {
						if (isReal(card)) {
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

	private static boolean isReal(Card card) {
		return card.names.isEmpty() || !card.name.equals(card.names.get(1));
	}

	private static Tier getTier(Expansion expansion) {
		switch (expansion.type) {
		case core:
		case expansion:
		case draft_innovation:
			return Tier.BOOSTER_RELEASE;
		case starter:
			return Tier.STARTER;
		case masters:
			return Tier.BOOSTER_REPRTINT;
		case commander:
		case planechase:
			return Tier.NON_BOOSTER_RELEASE;
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
			this.id = printing.card.scryfallId;
			this.code = printing.expansion.code;
			this.number = printing.card.number;
			if (printing.card.layout == Layout.transform) {
				back = true;
			}
		}
	}

	enum Tier {
		BOOSTER_RELEASE,
		STARTER,
		NON_BOOSTER_RELEASE,
		BOOSTER_REPRTINT,
		OTHER;
	}

	private static class Printing implements Comparable<Printing> {

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
