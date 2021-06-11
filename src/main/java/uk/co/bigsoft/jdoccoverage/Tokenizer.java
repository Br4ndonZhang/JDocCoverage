/* =========================================================
 * JavaDoc Coverage : a free tool for the Java(tm) platform
 * =========================================================
 *
 * Copyright (C) 2006, by Volker Berlin
 *
 * Project Info:  http://JDocCoverage.sourceforge.net/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * Created on 08.03.2006
 */
package uk.co.bigsoft.jdoccoverage;

/**
 * @author Volker Berlin
 */
class Tokenizer {

	private final char[] content;
	private int idx;
	private char comment;

	Tokenizer(String content) {
		this.content = content.toCharArray();
	}

	Token nextToken() {
		char quote = 0;
		int start = idx;
		while (idx < content.length) {
			char c = content[idx++];
			switch (c) {
			case '\r':
				if (idx < content.length && (content[idx] == '\n') & (start + 1 == idx)) {
					start = idx;
					break;
				}
				// no break
			case '\n':
				if (start + 1 == idx)
					return new Token(Token.EOL);
				else {
					idx--;
					return new Token(content, start, idx);
				}
			case ' ':
			case '\t':
				if (quote != 0)
					break;
				if (start + 1 == idx)
					start++;
				else {
					idx--;
					return new Token(content, start, idx);
				}
				break;
			case '\'':
			case '\"':
				if (quote == 0)
					quote = c;
				else if (quote == c)
					quote = 0;

				break;
			case '/':
				if (quote != 0) {
					break;
				}
				if (idx < content.length) {
					char c2 = content[idx++];
					switch (c2) {
					case '*':
						while (idx < content.length && content[idx] == '*')
							idx++;
						return new Token(Token.COMMENT_BLOCK_START);
					case '/':
						return new Token(Token.COMMENT_LINE_START);
					}
				}
				break;
			case '*':
				int i = idx;
				while (i < content.length && content[idx] == '*')
					i++;
				if (i < content.length && content[i] == '/') {
					idx = i + 1;
					return new Token(Token.COMMENT_BLOCK_END);
				}
			case '(':
			case ')':
			case '=':
			case ';':
			case '{':
			case '}':
				if (quote != 0) {
					break;
				}
				if (start + 1 == idx)
					return new Token(c);
				else {
					idx--;
					return new Token(content, start, idx);
				}
			}
		}
		if (start < idx)
			return new Token(content, start, idx);
		return null;
	}

	String readLine() {
		int start = idx;
		while (idx < content.length) {
			char c = content[idx++];
			switch (c) {
			case '\r':
			case '\n':
				idx--;
				return new String(content, start, idx - start);
			}
		}
		return new String(content, start, idx - start);
	}

	int getCurrentIdx() {
		return idx;
	}

	String getString(int fromIdx, int toIdx) {
		return new String(content, fromIdx, toIdx - fromIdx);
	}
}
