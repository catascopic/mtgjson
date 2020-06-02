package mtgjson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import mtgjson.Card.Format;

public class MtgJson {

	public static void main(String[] args) throws IOException {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Collection<Expansion> expansions;
		try (Reader reader = Files.newBufferedReader(Paths.get(
				"AllPrintings.json"))) {
			Map<String, Expansion> json = gson.fromJson(reader,
					new TypeToken<Map<String, Expansion>>() {}.getType());
			expansions = json.values();
		}
		Map<String, Set<Printing>> allPrintings = new HashMap<>();

		SetMultimap<Integer, String> momir = TreeMultimap.create();

		Multimap<String, Printing> notMtgo = TreeMultimap.create();
		
		for (Expansion expansion : expansions) {
			for (Card card : expansion.cards) {
				if (card.legalities.containsKey(Format.legacy) 
						&& card.types.contains(Card.Type.Creature)
						&& isReal(card)) {
					if (card.isMtgo) {
						momir.put(card.convertedManaCost.intValue(), card.name);
					} else {
						notMtgo.put(card.name, new Printing(card, expansion));
					}
				}

				Set<Printing> printings = allPrintings.computeIfAbsent(card.name,
						key -> new TreeSet<>());
				Printing printing = new Printing(card, expansion);
				printings.add(printing);
			}
		}
		for (Entry<Integer, Collection<String>> entry : momir.asMap().entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().size() + " cards");
		}
		
		notMtgo.keySet().removeAll(momir.values());
		
		System.out.println(notMtgo);
		
		// System.out.println(gson.toJson(momir.asMap()));
	}

	private static boolean isReal(Card card) {
		switch (card.layout) {
		case flip:
		case transform:
			return card.name.equals(card.names.get(0));
		case meld:
			return !card.name.equals(card.names.get(1));
		default:
			return true;
		}
	}

	static class Printing implements Comparable<Printing> {

		private final Card card;
		private final Expansion expansion;

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
					&& Objects.equals(this.card.number, that.card.number);
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
