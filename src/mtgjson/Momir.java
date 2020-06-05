package mtgjson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import mtgjson.Card.Format;
import mtgjson.Card.Legality;

public class Momir {

	public static void main(String[] args) throws IOException {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Collection<Expansion> expansions;
		try (Reader reader = Files.newBufferedReader(Paths.get("AllPrintings.json"))) {
			Map<String, Expansion> json = gson.fromJson(reader,
					new TypeToken<Map<String, Expansion>>() {}.getType());
			expansions = json.values();
		}

		SetMultimap<Integer, String> momir = TreeMultimap.create();
		for (Expansion expansion : expansions) {
			for (Card card : expansion.cards) {
				if (notBannedInLegacy(card)
						&& card.types.contains(Card.Type.Creature)
						&& card.isMtgo
						&& isReal(card)) {
					momir.put(card.convertedManaCost.intValue(), card.name);
				}
			}
		}

		System.out.println(gson.toJson(momir.asMap()));
	}

	private static boolean notBannedInLegacy(Card card) {
		Legality legality = card.legalities.get(Format.legacy);
		return legality == Legality.Legal || legality == Legality.Restricted;
	}

	private static boolean isReal(Card card) {
		return card.names.isEmpty() || !card.name.equals(card.names.get(1));
	}

}
