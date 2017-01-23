package kgi.presentations.k8s.assets_transcoder.service;

import com.amazonaws.services.s3.AmazonS3Client;
import kgi.presentations.k8s.common.TravelConfigProperties;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;

@Service
public class TranscodingService {
    Logger logger = LoggerFactory.getLogger(this.getClass());


    @Resource
    TravelConfigProperties appProperties;

    @Resource
    AmazonS3Client s3Client;

    public void transcode(String srcBucket, String srcKey) throws Exception {
        if (isImage(srcKey)) {
            performImageTransformations(srcBucket, srcKey);
        } else {
            logger.info("TO BE IMPLEMENTED transformation for type " + srcKey);
        }
    }

    void performImageTransformations(String srcBucket, String srcKey) throws Exception {
        createPresentation("-screen.jpg", 480, srcBucket, srcKey);
        createPresentation("-th.jpg", 100, srcBucket, srcKey);
    }

    void createPresentation(String suffix, int maxWidth, String srcBucket, String srcKey) throws Exception {

        String tempFilesPrefix = "transform";
        File inFile = File.createTempFile(tempFilesPrefix, srcKey);
        FileOutputStream inFileStream = new FileOutputStream(inFile);
        IOUtils.copy(s3Client.getObject(srcBucket, srcKey).getObjectContent(), inFileStream);
        inFileStream.close();
        String outKey = makeOutKey(srcKey, suffix);
        File outFile = File.createTempFile(tempFilesPrefix, outKey);

        String[] cmd = new String[]{ "convert", inFile.getAbsolutePath(), "-resize", ""+ maxWidth + "x10000>", outFile.getAbsolutePath()};
        logger.info( "executing {}", cmd.toString());
        Process process = Runtime.getRuntime().exec(cmd);
        Scanner errScanner = new Scanner(process.getErrorStream()).useDelimiter("\n");
        int res = process.waitFor();
        if( res != 0 ){
            while (errScanner.hasNext()){
                logger.error(errScanner.nextLine());
            }
            throw  new Exception("Could not execute: " + cmd.toString());
        }
        s3Client.putObject( appProperties.getOutput_bucket(),outKey, outFile);
    }

    private String makeOutKey(String srcKey, String suffix) {
        int dotIndex = srcKey.lastIndexOf('.');
        return srcKey.substring(0, dotIndex )+ suffix;

    }

    boolean isImage(String srcKey) {
        String upper = srcKey.toUpperCase();

        return upper.endsWith("JPG") || upper.endsWith("PNG") || upper.endsWith("JPEG");
    }
}
