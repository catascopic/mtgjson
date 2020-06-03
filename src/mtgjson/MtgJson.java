package mtgjson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import mtgjson.Card.Format;
import mtgjson.Card.Legality;

public class MtgJson {

	public static void main(String[] args) throws IOException {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Collection<Expansion> expansions;
		try (Reader reader = Files.newBufferedReader(Paths.get("AllPrintings.json"))) {
			Map<String, Expansion> json = gson.fromJson(reader,
					new TypeToken<Map<String, Expansion>>() {}.getType());
			expansions = json.values();
		}

		SetMultimap<String, Printing> printings = TreeMultimap.create();
		SetMultimap<Integer, String> momir = TreeMultimap.create();

		for (Expansion expansion : expansions) {
			for (Card card : expansion.cards) {
				if (notBannedInLegacy(card)
						&& card.types.contains(Card.Type.Creature)
						&& card.isMtgo
						&& isReal(card)) {
					momir.put(card.convertedManaCost.intValue(), card.name);
				}

				printings.put(card.name, new Printing(card, expansion));
			}
		}

		for (Entry<Integer, Collection<String>> entry : momir.asMap().entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().size() + " cards");
		}
	}

	private static boolean notBannedInLegacy(Card card) {
		Legality legality = card.legalities.get(Format.legacy);
		return legality == Legality.Legal || legality == Legality.Restricted;
	}

	private static boolean isReal(Card card) {
		return card.names.isEmpty() || !card.name.equals(card.names.get(1));
	}


	static class Printing implements Comparable<Printing> {

		Card card;
		Expansion expansion;

		Printing(Card card, Expansion expansion) {
			this.card = card;
			this.expansion = expansion;
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
					.compare(expansion, o.expansion)
					.compare(card.number, o.card.number)
					.result();
		}

		@Override
		public String toString() {
			return card + " in " + expansion + " #" + card.number;
		}
	}

}
