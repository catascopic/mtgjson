package mtgjson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ComparisonChain;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.JsonAdapter;

import mtgjson.Card.CollectorNumberAdapter;
import mtgjson.Card.Layout;

public class Scryfall {

	public static void main(String[] args) throws IOException {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		try (BufferedWriter out = Files.newBufferedWriter(Paths.get("init.jsonp"))) {
			out.append("init(");
			out.append(gson.toJson(getData()));
			out.append(");");
		}
	}

	public static Map<String, List<CardData>> getData() throws IOException {
		return new Scryfall().parse();
	}

	private Map<String, List<CardData>> data = new HashMap<>();

	private Map<String, List<CardData>> parse() throws IOException {

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		try (Reader reader = Files.newBufferedReader(Paths.get("AllPrintings.json"))) {
			AllPrintings json = gson.fromJson(reader, AllPrintings.class);
			for (Expansion expansion : json.data.values()) {
				if (!expansion.cards.isEmpty()) {
					data.put(expansion.code, parseCards(expansion.cards));
				}
			}
		}

		return data;
	}

	private static List<CardData> parseCards(List<Card> cards) {
		List<CardData> result = new ArrayList<>();
		for (Card card : cards) {
			if (card.identifiers.scryfallId == null) {
				continue;
			}
			String computedName;
			if (card.names.isEmpty()) {
				computedName = card.name;
			} else if (card.names.get(0).equals(card.name)) {
				if (card.layout == Layout.split) {
					computedName = card.names.get(0) + " // " + card.names.get(1);
				} else {
					computedName = card.name;
				}
			} else {
				continue;
			}
			result.add(new CardData(DIACRITICS.escape(computedName),
					card.identifiers.scryfallId,
					card.number,
					card.layout == Layout.transform));
		}
		Collections.sort(result);
		return result;
	}

	private static final Escaper DIACRITICS = Escapers.builder()
			.addEscape('\u00E0', "a")
			.addEscape('\u00E1', "a")
			.addEscape('\u00E2', "a")
			.addEscape('\u00E9', "e")
			.addEscape('\u00ED', "i")
			.addEscape('\u00F6', "o")
			.addEscape('\u00FA', "u")
			.addEscape('\u00FB', "u")
			.build();

	static class CardData implements Comparable<CardData> {

		CardData(String name, UUID id, CollectorNumber number, boolean hasBack) {
			this.name = name;
			this.id = id;
			this.number = number;
			this.hasBack = hasBack ? true : null;
		}

		String name;
		UUID id;
		@JsonAdapter(CollectorNumberAdapter.class)
		CollectorNumber number;
		Boolean hasBack;

		@Override
		public int compareTo(CardData o) {
			return ComparisonChain.start()
					.compare(name, o.name)
					.compare(number, o.number).result();
		}
	}

}
