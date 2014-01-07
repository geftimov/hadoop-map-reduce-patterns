package com.eftimoff.mapreduce.organization.hierarchical;

import com.eftimoff.mapreduce.utils.MRDPUtils;

/* could have bypassed all this malarkey with custom Writables */
public class ReduceSideDataDiscriminator {
	public enum Type {
		POST, COMMENT, QUESTION, ANSWER, UNKNOWN, INVALID;

		public static Type translate(char symbol) {
			switch (symbol) {
			case 'P':
				return POST;
			case 'C':
				return COMMENT;
			case 'Q':
				return QUESTION;
			case 'A':
				return ANSWER;
			default:
				return UNKNOWN;
			}
		}
	}

	private final Type type;

	public Type getType() {
		return type;
	}

	public String getData() {
		return data;
	}

	public boolean isUnrecordable() {
		return type == Type.INVALID || type == Type.UNKNOWN;
	}

	private final String data;

	private ReduceSideDataDiscriminator(Type type, String data) {
		this.type = type;
		this.data = data;
	}

	public static ReduceSideDataDiscriminator parse(String data) {
		if (MRDPUtils.isNullOrEmpty(data)) {
			return new ReduceSideDataDiscriminator(Type.INVALID, "");
		}
		char symbol = data.charAt(0);

		if (data.length() == 1) {
			return new ReduceSideDataDiscriminator(Type.translate(symbol), "");
		}

		data = data.substring(1).trim();
		return new ReduceSideDataDiscriminator(Type.translate(symbol), data);
	}
}
