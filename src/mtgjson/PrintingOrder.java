package mtgjson;

import com.google.common.collect.ComparisonChain;

import mtgjson.Expansion.Type;
import mtgjson.MtgJson.Printing;

class PrintingOrder {

	static int latestPrinting(Printing p1, Printing p2) {
		return ComparisonChain.start()
				.compare(p2.expansion.releaseDate, p1.expansion.releaseDate)
				.compare(p1.card.number, p2.card.number)
				.result();
	}

	static int firstPrinting(Printing p1, Printing p2) {
		return ComparisonChain.start()
				.compare(p1.expansion.releaseDate, p2.expansion.releaseDate)
				.compare(p1.card.number, p2.card.number)
				.result();
	}

	static int latestStandardPrinting(Printing p1, Printing p2) {
		return ComparisonChain.start()
				.compareTrueFirst(isStandard(p1.expansion), isStandard(p2.expansion))
				.compare(p2.expansion.releaseDate, p1.expansion.releaseDate)
				.compare(p1.card.number, p2.card.number)
				.result();
	}

	static boolean isStandard(Expansion expansion) {
		return expansion.type == Type.core || expansion.type == Type.expansion;
	}

	static int latestBoosterPrinting(Printing p1, Printing p2) {
		return ComparisonChain.start()
				.compareTrueFirst(isBoosterSet(p1.expansion), isBoosterSet(p2.expansion))
				.compare(p2.expansion.releaseDate, p1.expansion.releaseDate)
				.compare(p1.card.number, p2.card.number)
				.result();
	}

	private static boolean isBoosterSet(Expansion expansion) {
		switch (expansion.type) {
		case core:
		case draft_innovation:
		case expansion:
		case starter:
			return true;
		case masters:
			return !expansion.name.contains("Mystery Booster");
		default:
			return false;
		}
	}

	static int latestPrintingWithTiers(Printing p1, Printing p2) {
		return ComparisonChain.start()
				.compare(p1.getTier(), p2.getTier())
				.compare(p2.expansion.releaseDate, p1.expansion.releaseDate)
				.compare(p1.card.number, p2.card.number)
				.result();
	}

}
