package mtgjson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import mtgjson.Card.BorderColor;
import mtgjson.Card.Format;
import mtgjson.Card.Type;

public class Wordy {

	public static void main(String[] args) throws IOException {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Collection<Expansion> expansions;
		try (Reader reader = Files.newBufferedReader(Paths.get("AllPrintings.json"))) {
			AllPrintings json = gson.fromJson(reader,
					AllPrintings.class);
			expansions = json.data.values();
		}

		Map<String, String> text = new HashMap<>();

		for (Expansion expansion : expansions) {
			if (isReal(expansion)) {
				for (Card card : expansion.cards) {
					if (isReal(card)) {
						if (!card.types.contains(Type.Creature)) {
							text.putIfAbsent(card.name, normalize(card.text));
						}
					}
				}
			}
		}

		ArrayList<Entry<String, String>> cards = new ArrayList<>(text.entrySet());
		cards.sort((e1, e2) -> Integer.compare(e1.getValue().length(), e2.getValue().length()));

		for (Entry<String, String> entry : cards.subList(0, 100)) {
			System.out.printf("%03d: %-40s %s%n",
					entry.getValue().length(),
					entry.getKey(),
					entry.getValue());
		}
	}

	private static final Pattern REMINDER = Pattern.compile("\\([^\\)]+\\)");

	private static String normalize(String text) {
		return CharMatcher.whitespace().trimAndCollapseFrom(
				REMINDER.matcher(text).replaceAll(""), ' ');
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

}
