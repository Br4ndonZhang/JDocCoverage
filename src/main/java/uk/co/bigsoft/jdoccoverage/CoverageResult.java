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

import java.io.File;


/**
 * @author Volker Berlin
 */
class CoverageResult {

	final String fileName;
    String packageName = "";
    String author;
    
    /** count of characters of comment */
    int commentCount;
    
    /** count of characters of code */
    int codeCount;
    
    /** count of methods */
    int methodCount;
    
    /** count of TODOs */
    int todoCount;
    
    /** count of see tags */
    int seeCount;
    
    
    StringBuffer formattedSourceCode = new StringBuffer();
    
    CoverageResult(String fileName){
        this.fileName = fileName;
    }
    
    void addComment(String comment){
        commentCount += comment.length();
    }
    
    
    void addCode(String code){
        codeCount += code.length();
    }
    
    
    void addMethod(String method){
        methodCount++;
    }

    
    String getResultName(){
        String sourceName;
        if(fileName.endsWith(".java")){
            sourceName = fileName.substring(0, fileName.length()-5); //Remove ".java"
        }else{
            if(fileName.length() == 0)
                return "(default)";
            return fileName;
        }
        sourceName = sourceName.replace(File.separatorChar, '.');
        sourceName = sourceName.substring(sourceName.lastIndexOf('.')+1);
        if(packageName.length() > 0)
            return packageName + '.' + sourceName;
        return sourceName;
    }
    
    
    String getResultFilename(){
        return getResultName() + ".html";
    }
    

    public void accumulate(CoverageResult result) {
        commentCount += result.commentCount;
        codeCount += result.codeCount;
        methodCount += result.methodCount;
        todoCount += result.todoCount;
        seeCount += result.seeCount;
    }
}
