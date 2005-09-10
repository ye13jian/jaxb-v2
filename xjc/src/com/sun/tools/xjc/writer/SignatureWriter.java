/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.xjc.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * Dumps an annotated grammar in a simple format that
 * makes signature check easy.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SignatureWriter {
    
    public static void write( Outline model, Writer out )
        throws IOException {
        
        new SignatureWriter(model,out).dump();
    }
    
    private SignatureWriter( Outline model, Writer out ) {
        this.out = out;
        this.classes = model.getClasses();

        for( ClassOutline ci : classes )
            classSet.put( ci.ref, ci );
    }
    
    /** All the ClassItems in this grammar. */
    private final Collection<? extends ClassOutline> classes;
    /** Map from content interfaces to ClassItem. */
    private final Map<JDefinedClass,ClassOutline> classSet = new HashMap<JDefinedClass,ClassOutline>();
    
    private final Writer out;
    private int indent=0;
    private void printIndent() throws IOException {
        for( int i=0; i<indent; i++ )
            out.write("  ");
    }
    private void println(String s) throws IOException {
        printIndent();
        out.write(s);
        out.write('\n');
    }
    
    private void dump() throws IOException {
        
        // collect packages used in the class.
        Set<JPackage> packages = new TreeSet<JPackage>(new Comparator<JPackage>() {
            public int compare(JPackage lhs, JPackage rhs) {
                return lhs.name().compareTo(rhs.name());
            }
        });
        for( ClassOutline ci : classes )
            packages.add(ci._package()._package());

        for( JPackage pkg : packages )
            dump( pkg );
        
        out.flush();
    }
    
    private void dump( JPackage pkg ) throws IOException {
        println("package "+pkg.name()+" {");
        indent++;
        dumpChildren(pkg);
        indent--;
        println("}");
    }
    
    private void dumpChildren( JClassContainer cont ) throws IOException {
        Iterator itr = cont.classes();
        while(itr.hasNext()) {
            JDefinedClass cls = (JDefinedClass)itr.next();
            ClassOutline ci = classSet.get(cls);
            if(ci!=null)
                dump(ci);
        }
    }
    
    private void dump( ClassOutline ci ) throws IOException {
        JDefinedClass cls = ci.implClass;

        StringBuilder buf = new StringBuilder();
        buf.append("interface ");
        buf.append(cls.name());
        
        boolean first=true;
        Iterator itr = cls._implements();
        while(itr.hasNext()) {
            if(first) {
                buf.append(" extends ");
                first=false;
            } else {
                buf.append(", ");
            }
            buf.append( printName((JClass)itr.next()) );
        }
        buf.append(" {");
        println(buf.toString());
        indent++;
        
        // dump the field
        for( FieldOutline fo : ci.getDeclaredFields() ) {
            String type = printName(fo.getRawType());
            println(type+' '+fo.getPropertyInfo().getName(true)+';');
        }
        
        dumpChildren(cls);
        
        indent--;
        println("}");
    }
    
    /** Get the display name of a type. */
    private String printName( JType t ) {
        String name = t.fullName();
        if(name.startsWith("java.lang."))
            name = name.substring(10);  // chop off the package name
        return name;
    }
}
