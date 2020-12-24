package mtgjson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Artists {

	public static void main(String[] args) throws IOException {

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Collection<Expansion> expansions;
		try (Reader reader = Files.newBufferedReader(Paths.get("AllPrintings.json"))) {
			AllPrintings json = gson.fromJson(reader,
					AllPrintings.class);
			expansions = json.data.values();
		}

		SetMultimap<String, String> artists = TreeMultimap.create();

		for (Expansion expansion : expansions) {
			for (Card card : expansion.cards) {
				if (card.artist != null) {
					artists.put(card.artist, card.name);
				}
			}
		}

		ArrayList<Entry<String, Set<String>>> entries = new ArrayList<>(Multimaps.asMap(artists)
				.entrySet());

		entries.sort((e1, e2) -> -Integer.compare(e1.getValue().size(), e2.getValue().size()));

		int i = 1;
		for (Entry<String, Set<String>> entry : entries) {
			System.out.println(i++ + ". " + "https://scryfall.com/search?q=artist%3A%22"+entry.getKey().replace(' ', '+')+"%22+new%3Aart+unique%3Aprintings&unique=cards&as=grid&order=released");
		}

	}

}
