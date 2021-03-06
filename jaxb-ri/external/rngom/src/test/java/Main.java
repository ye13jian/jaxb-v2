/*
 * Copyright (C) 2004-2011
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.util.CheckingSchemaBuilder;
import org.kohsuke.rngom.dump.Dumper;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.parse.compact.CompactParseable;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Test Driver.
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Parseable p;
        
        ErrorHandler eh = new DraconianErrorHandler();
        
        // the error handler passed to Parseable will receive parsing errors.
        if(args[0].endsWith(".rng"))
            p = new SAXParseable(new InputSource(args[0]),eh);
        else
            p = new CompactParseable(new InputSource(args[0]),eh);
        
        // the error handler passed to CheckingSchemaBuilder will receive additional
        // errors found during the RELAX NG restrictions check.
        // typically you'd want to pass in the same error handler,
        // as there's really no distinction between those two kinds of errors.
        SchemaBuilder sb = new CheckingSchemaBuilder(new Dumper(),eh);
        try {
            // run the parser
            p.parse(sb);
        } catch( BuildException e ) {
            // I found that Crimson doesn't show the proper stack trace
            // when a RuntimeException happens inside a SchemaBuilder.
            // the following code shows the actual exception that happened.
            if( e.getCause() instanceof SAXException ) {
                SAXException se = (SAXException) e.getCause();
                if(se.getException()!=null)
                    se.getException().printStackTrace();
            }
            throw e;
        }
    }
}
