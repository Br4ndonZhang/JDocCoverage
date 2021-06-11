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
class Parser {
	
    private static final int CODE = 1;
    private static final int COMMENT_LINE = 2;
    private static final int COMMENT_BLOCK = 3;
    
	private final Tokenizer tokenizer;
    private final CoverageResult result;
	
    private int status;
    private int statusChangeIdx;
    private int tokenIdx;
    
	private int wordCountOfLine;
    private int parenthesLevel;
    private String lastWord;
    private boolean wasEquals;

    Parser(String content, String fileName){
		tokenizer = new Tokenizer(content);
		result = new CoverageResult(fileName);
	}
	
	
	CoverageResult parse(){
		Token token = tokenizer.nextToken();
        status = CODE;
		while(token != null){
			switch(token.type){
			case Token.WORD:
                wordToken(token);
                break;
            case Token.EOL:
                if(status == COMMENT_LINE)
                    setStatus(CODE);
                wordCountOfLine = 0;
                break;
            case Token.COMMENT_LINE_START:
                setStatus(COMMENT_LINE);
                wordCountOfLine = 0;
                break;
            case Token.COMMENT_BLOCK_START:
                setStatus(COMMENT_BLOCK);
                break;
            case Token.COMMENT_BLOCK_END:
                setStatus(CODE);
                break;
            case Token.EQUALS:
                wasEquals = true;
                break;
            case Token.SEMIKOLON:
                wasEquals = false;
                break;
            case Token.PARENTHESIS_L:
                if(status != CODE)
                    break;
                if(parenthesLevel == 1 && !wasEquals){
                    result.addMethod(lastWord);
                }
                break;
            case Token.PARENTHESIS_R:
                if(status != CODE)
                    break;
                break;
            case Token.ESCAPE_L:
                if(status != CODE)
                    break;
                parenthesLevel++;
                break;
            case Token.ESCAPE_R:
                if(status != CODE)
                    break;
                parenthesLevel--;
                break;
			}
            tokenIdx = tokenizer.getCurrentIdx();
			token = tokenizer.nextToken();
		}
        setStatus(CODE);
        result.formattedSourceCode.append("</span>");
		return result;
	}
    
    
    private void wordToken(Token token){
        String word = lastWord = token.word;
        wordCountOfLine++;
        switch(status){
        case CODE:
            if(word.equals("package")){
                token = tokenizer.nextToken();
                int idx = token.word.indexOf(';');
                String packageName;
                if(idx > 0){
                    packageName = token.word.substring(0, idx);
                }else{
                    packageName = token.word;
                }
                result.packageName = packageName.trim();
            }else{
                result.addCode(word);
            }
            break;
        case COMMENT_LINE:
            if(wordCountOfLine == 1 && word.equals("TODO")){
                result.todoCount++;
                //skip todo because it is not comment
                tokenizer.readLine();
            }else{
                result.addComment(word);
            }
            break;
        case COMMENT_BLOCK:
            while(wordCountOfLine == 1 && word.startsWith("*")){
                word = word.substring(1);
            }
            if(word.length() == 0)
                break;
            if(word.equals("@author")){
                result.author = readCommentLine();
            }else
            if(word.equals("@see")){
                result.seeCount++;
                readCommentLine();
            }else{
                result.addComment(word);
            }
            break;
        }
    }
    
    
    private String readCommentLine(){
        //TODO
        return tokenizer.readLine().trim();
    }
    
    
    private void setStatus(int newStatus){
        if(statusChangeIdx > 0)
            result.formattedSourceCode.append("</span>");
        result.formattedSourceCode.append("<span class='");
        int toIdx;
        if(status == CODE){
            toIdx = tokenIdx;
            result.formattedSourceCode.append("code");
        }else{
            toIdx = tokenizer.getCurrentIdx();
            result.formattedSourceCode.append("comment");
        }
        result.formattedSourceCode.append("'>");
        result.formattedSourceCode.append( convertToHtml(tokenizer.getString( statusChangeIdx, toIdx )));
        this.status = newStatus;
        this.statusChangeIdx = toIdx;
    }
    
    
    private StringBuffer convertToHtml(String str){
        StringBuffer buffer = new StringBuffer(str.length()+16);
        for(int i=0; i<str.length(); i++){
            char c = str.charAt(i);
            switch(c){
            case '<':
                buffer.append("&lt;");
                break;
            default:
                buffer.append(c);
            }
        }
        return buffer;
    }
}
