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
class Token {

	static final int WORD = 1;
	static final int EOL  = 2;
	static final int COMMENT_LINE_START  = 3;
	static final int COMMENT_BLOCK_START  = 4;
	static final int COMMENT_BLOCK_END  = 5;
    static final int PARENTHESIS_L  = '('; // 40
    static final int PARENTHESIS_R  = ')'; // 41
    static final int SEMIKOLON      = ';'; // 59
    static final int EQUALS         = '='; // 61
    static final int ESCAPE_L       = '{'; // 123
    static final int ESCAPE_R       = '}'; // 125
	
	final int type;
    String word;
	
	Token(int type){
		this.type = type;
	}
	
	Token(char[] content, int start, int end) {
		this(WORD);
		this.word = new String(content, start, end-start);
	}

}
