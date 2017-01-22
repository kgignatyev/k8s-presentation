package kgi.presentations.k8s.build_utils;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 */
public class BuildUtilsTest {

    @Test
    public void testTemplateProcess() throws Exception {
        Map cxt  = new HashMap();
        cxt.put("image","zzzzzzzzz");
        cxt.put("load-factor",0.5);
        String base64 = Base64.getEncoder().encodeToString("data".getBytes(Charset.forName("UTF8")));
        cxt.put("base64", base64);
        String outputFileName = "target/results/test.txt";
        BuildUtils.processTemplate("src/test/resources/test-template.txt",
                cxt, outputFileName);
        String renderedTemplate = FileUtils.readFileToString( new File(outputFileName ));
        assertTrue( renderedTemplate.indexOf(base64)>1);


    }
}
