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
 * Created on 14.04.2006
 */
package uk.co.bigsoft.jdoccoverage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * @author Volker Berlin
 */
public class FileScanner {

    private final String[] patterns;
    private final ArrayList files = new ArrayList();
    private final ArrayList roots = new ArrayList();
    private File baseDir = new File(System.getProperty("user.dir"));
    
    FileScanner(ArrayList patternList){
        patterns = new String[patternList.size()];
        patternList.toArray(patterns);

        for(int i=0; i<patterns.length; i++){
            patterns[i] = normalizePathPattern(patterns[i]);
        }
    }
    
    
    void scan(){
        for(int i=0; i<patterns.length; i++){
            String root = getRootDirectory(patterns[i]);
            if(!roots.contains(root)){
                roots.add(root);
                scanDirectory(new File(baseDir, root), root);
            }
        }
    }
    
    
    ArrayList getFiles(){
        return files;
    }
    
    /**
     * Convert slashes an backslashes to the current file separator.
     */
    private String normalizePathPattern(String p) {
        String pattern = p.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        if (pattern.endsWith(File.separator)) {
            pattern += "**"+File.separatorChar+"*.java";
        }else
        if(new File(baseDir, pattern).isDirectory()){
            pattern += File.separatorChar+"**"+File.separatorChar+"*.java";
        }else{
            int pointIdx = pattern.lastIndexOf('.');
            if(pointIdx == -1)
                pattern += "*.java";
        }
        return pattern;
    }
    
    
    /**
     * Get the root irectory of the pattern. This is the base irectory without a placeholer.
     */
    private String getRootDirectory(String pattern){
        int length = pattern.length();
        int idx = pattern.indexOf('?');
        if(idx >= 0 && idx < length)
            length = idx;
        idx = pattern.indexOf('*');
        if(idx >= 0 && idx < length)
            length = idx;
        
        idx = pattern.lastIndexOf(File.separatorChar, length);
        if(idx >= 0)
            return pattern.substring(0,idx);
        return "";
    }
    
    
    /**
     * Scan a directory for files that map the one of the pattern.
     * @param directory the real directory.
     * @param dirName the directory name relative to the base directory.
     */
    private void scanDirectory(File directory, String dirName){
        String[] fileNames = directory.list();
        Arrays.sort(fileNames);
        for(int f=0; f<fileNames.length; f++){
            String fileName = fileNames[f];
            String newVpath = dirName.length() == 0 ? fileName : dirName+File.separatorChar+fileName;
            File file = new File(directory, fileName);
            if(file.isDirectory()){
                scanDirectory(file, newVpath);
            }else{
                for(int i=0; i<patterns.length; i++){
                    if(matchPath(patterns[i], newVpath)){
                        files.add(newVpath);
                    }
                }
            }
        }
    }

    
    /**
     * Tests whether or not a given path matches a given pattern.
     *
     * @param pattern The pattern to match against. Must not be null.
     * @param str     The path to match, as a String. Must not be null.
     *
     * @return true if the pattern matches the string.
     */
    private static boolean matchPath(String pattern, String str) {
        // When str starts with a File.separator, pattern has to start with a
        // File.separator.
        // When pattern starts with a File.separator, str has to start with a
        // File.separator.
        if (str.startsWith(File.separator)
                != pattern.startsWith(File.separator)) {
            return false;
        }

        String[] patDirs = tokenizePathAsArray(pattern);
        String[] strDirs = tokenizePathAsArray(str);

        int patIdxStart = 0;
        int patIdxEnd = patDirs.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strDirs.length - 1;

        // up to first '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = patDirs[patIdxStart];
            if (patDir.equals("**")) {
                break;
            }
            if (!match(patDir, strDirs[strIdxStart])) {
                patDirs = null;
                strDirs = null;
                return false;
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!patDirs[i].equals("**")) {
                    patDirs = null;
                    strDirs = null;
                    return false;
                }
            }
            return true;
        } else {
            if (patIdxStart > patIdxEnd) {
                // String not exhausted, but pattern is. Failure.
                patDirs = null;
                strDirs = null;
                return false;
            }
        }

        // up to last '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = patDirs[patIdxEnd];
            if (patDir.equals("**")) {
                break;
            }
            if (!match(patDir, strDirs[strIdxEnd])) {
                patDirs = null;
                strDirs = null;
                return false;
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!patDirs[i].equals("**")) {
                    patDirs = null;
                    strDirs = null;
                    return false;
                }
            }
            return true;
        }

        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patDirs[i].equals("**")) {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // '**/**' situation, so skip one
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop:
                        for (int i = 0; i <= strLength - patLength; i++) {
                            for (int j = 0; j < patLength; j++) {
                                String subPat = patDirs[patIdxStart + j + 1];
                                String subStr = strDirs[strIdxStart + i + j];
                                if (!match(subPat, subStr)) {
                                    continue strLoop;
                                }
                            }

                            foundIdx = strIdxStart + i;
                            break;
                        }

            if (foundIdx == -1) {
                patDirs = null;
                strDirs = null;
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (!patDirs[i].equals("**")) {
                patDirs = null;
                strDirs = null;
                return false;
            }
        }

        return true;
    }

    
    /**
     * Tests whether or not a string matches against a pattern.
     * The pattern may contain two special characters:<br>
     * '*' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern The pattern to match against. Must not be null.
     * @param str     The string which must be matched against the pattern. Must not be null.
     *
     *
     * @return true if the string matches the pattern.
     */
    private static boolean match(String pattern, String str) {
        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;

        boolean containsStar = false;
        for (int i = 0; i < patArr.length; i++) {
            if (patArr[i] == '*') {
                containsStar = true;
                break;
            }
        }

        if (!containsStar) {
            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false; // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (ch != '?') {
                    if (ch != strArr[i]) {
                        return false; // Character mismatch
                    }
                }
            }
            return true; // String matches against pattern
        }

        if (patIdxEnd == 0) {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while ((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?') {
                if (ch != strArr[strIdxStart]) {
                    return false; // Character mismatch
                }
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // Process characters after last star
        while ((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?') {
                if (ch != strArr[strIdxEnd]) {
                    return false; // Character mismatch
                }
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if (ch != '?') {
                        if (ch != strArr[strIdxStart + i
                                + j]) {
                            continue strLoop;
                        }
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a list of path tokens.
     */
    private static String[] tokenizePathAsArray(String path) {
        ArrayList tokenList = new ArrayList();
        int idx = path.indexOf(File.separatorChar);
        int start = 0;
        while(idx>0){
            tokenList.add( path.substring(start, idx) );
            start = idx+1;
            idx = path.indexOf(File.separatorChar, start);
        }
        tokenList.add( path.substring(start) );
        String[] list = new String[tokenList.size()];
        tokenList.toArray(list);
        return list;
    }


}
