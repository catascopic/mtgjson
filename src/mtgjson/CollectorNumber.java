package mtgjson;

import java.util.Objects;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ComparisonChain;

public class CollectorNumber implements Comparable<CollectorNumber> {

	String prefix;
	int number;
	String suffix;

	private static final CharMatcher DIGIT = CharMatcher.inRange('0', '9');

	public CollectorNumber(String number) {
		int start = DIGIT.indexIn(number);
		if (start == -1) {
			this.prefix = number;
			this.number = 0;
			this.suffix = "";
		} else {
			int end = DIGIT.negate().indexIn(number, start);
			if (end == -1) {
				end = number.length();
			}
			this.prefix = number.substring(0, start);
			this.number = Integer.parseInt(number.substring(start, end));
			this.suffix = number.substring(end);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(prefix, number, suffix);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CollectorNumber)) {
			return false;
		}
		CollectorNumber that = (CollectorNumber) obj;
		return this.prefix.equals(that.prefix)
				&& this.number == that.number
				&& this.suffix.equals(that.suffix);
	}

	@Override
	public int compareTo(CollectorNumber o) {
		return ComparisonChain.start()
				.compare(number, o.number)
				.compare(prefix, o.prefix)
				.compare(suffix, o.suffix)
				.result();
	}

	@Override
	public String toString() {
		return prefix + number + suffix;
	}

}
