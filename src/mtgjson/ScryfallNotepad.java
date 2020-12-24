package mtgjson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ScryfallNotepad {

	public static void main(String[] args) throws IOException {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		try (BufferedWriter out = Files.newBufferedWriter(Paths.get("scryfallid.jsonp"))) {
			out.append("loadScryfallIds(");
			out.append(gson.toJson(parse()));
			out.append(");");
		}
	}

	private static Map<String, String> parse() throws IOException {
		Map<String, String> data = new HashMap<>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		try (Reader reader = Files.newBufferedReader(Paths.get("AllPrintings.json"))) {
			AllPrintings json = gson.fromJson(reader, AllPrintings.class);
			for (Expansion expansion : json.data.values()) {
				for (Card card : expansion.cards) {
					data.put(card.identifiers.scryfallId.toString(),
							DIACRITICS.escape(getName(card)));
				}
			}
		}
		return data;
	}

	private static String getName(Card card) {
		switch (card.layout) {
		case adventure:
		case flip:
		case meld:
		case transform:
		case modal_dfc:
			return card.faceName;
		default:
			return card.name;
		}
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

}
