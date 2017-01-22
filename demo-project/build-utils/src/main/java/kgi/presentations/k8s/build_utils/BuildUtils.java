package kgi.presentations.k8s.build_utils;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class BuildUtils {

    public static void processTemplate(String templateFile, Map context, String outputFile ) throws Exception {
        File tpl = new File(templateFile);
        System.out.println("Processing template:"+ tpl.getAbsolutePath());
        if( !tpl.exists()){
           System.err.println("Template does not exist:" + tpl.getAbsolutePath());
           System.exit(-1);
        }
        Handlebars handlebars = new Handlebars();
        Template tCompiled = handlebars.compileInline(FileUtils.readFileToString( tpl, Charset.forName("UTF8")));


        File out = new File(outputFile);
        assureParentDir(out);
        FileWriter writer = new FileWriter( out );
        tCompiled.apply( context,writer );
        writer.flush();

    }

    public static void assureParentDir(File out) {
        File outParent = out.getParentFile();
        if( !outParent.exists()){
            outParent.mkdirs();
        }
    }

    public static void copy(String src, String to) throws IOException {
        File target = new File(to);
        FileUtils.copyFile( new File(src),target);
    }

    public static Map readProperties(String baseName, String env ) throws IOException {
        Properties res = new Properties();
        File f = new File( baseName + ".properties");
        if( f.exists()){
            res.load( new FileReader( f ));
        }
        File f2 = new File( baseName + "-"+ env+ ".properties");
        if( f2.exists()){
            res.load( new FileReader(f2));
        }
        return res;
    }
}
