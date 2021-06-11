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
 * Created on 17.03.2006
 */
package uk.co.bigsoft.jdoccoverage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;


/**
 * @author Volker Berlin
 */
class ResultWriter {
    
    private String title = "JDocCoverage Report - " + DateFormat.getDateTimeInstance().format( new Date() );
    private NumberFormat numFormat = new DecimalFormat("0.0%");
    
    
    PrintStream createPrintStream(CoverageResult result, File parent) throws Exception{
        File file = new File(parent, result.getResultFilename() );
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        return new PrintStream(fos, false, "UTF8");
    }
    
    
    void writeResultFileHeader(PrintStream output, CoverageResult result){
        output.println("<HTML>");
        output.println("<HEAD>");
        output.print("<TITLE>");
        output.print(title);
        output.println("</TITLE>");
        output.println("<link rel='stylesheet' type='text/css' href='default.css'>");
        output.println("</HEAD>");
        output.println("<BODY>");
        output.print("<TABLE cellspacing='0' class='header'><TR><TD class='title'>");
        output.print(title);
        output.println("</TD></TR>");
        output.print("<TR><TD class='menu'><a class='menu' href='index.html'>Overview</a>");
        if(result.packageName.length() != 0){
            output.print(" | <a class='menu' href='");
            output.print(result.packageName);
            output.print(".html'>");
            output.print(result.packageName);
            output.print("</a>");
        }
        output.println("</TD></TR></TABLE>");
        output.println("<p>");
        output.println("<TABLE cellspacing='0'>");
    }
    
    
    void writeResultHeader(PrintStream output){
        output.println("<TR><TH>Name</TH><TH>method, %</TH><TH>comment, %</TH><TH>TODO</TH><TH>@see</TH></TR>\n");
    }
    
    
    void writeResultLine(PrintStream output, CoverageResult result, boolean link){
        output.print("<TR><TD>");
        if(link){
            output.print("<a href='");
            output.print(result.getResultFilename());
            output.print("'>");
        }
        output.print(result.getResultName());
        if(link){
            output.print("</a>");
        }
        output.print("</TD><TD>");
        output.print(result.methodCount);
        output.print("</TD><TD>");
        output.print(numFormat.format(result.commentCount/(double)(result.commentCount+result.codeCount)));
        output.print(" &nbsp; ("+result.commentCount+"/"+result.codeCount+")");
        output.print("</TD><TD>");
        output.print(result.todoCount);
        output.print("</TD><TD>");
        output.print(result.seeCount);
        output.println("</TD></TR>");        
    }
    
    
    void writeResultFileFooter(PrintStream output, CoverageResult result){
        output.println("</TABLE>");
        output.println("<CODE><PRE>");
        if(result.formattedSourceCode != null){
            output.println(result.formattedSourceCode);
            result.formattedSourceCode = null;
        }
        output.println("</PRE></CODE>");
        output.println("</BODY>");
        output.println("</HTML>");
        output.flush();
        output.close();
    }
}
