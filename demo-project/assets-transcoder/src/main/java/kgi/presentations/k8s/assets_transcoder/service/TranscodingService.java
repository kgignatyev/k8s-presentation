package kgi.presentations.k8s.assets_transcoder.service;

import com.amazonaws.services.s3.AmazonS3Client;
import kgi.presentations.k8s.common.TravelConfigProperties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Scanner;

@Service
public class TranscodingService implements InitializingBean{
    Logger logger = LoggerFactory.getLogger(this.getClass());


    @Resource
    TravelConfigProperties appProperties;

    @Resource
    AmazonS3Client s3Client;
    private File tempDir;

    public void transcode(String srcBucket, String srcKey) throws Exception {
        if (isImage(srcKey)) {
            performImageTransformations(srcBucket, srcKey);
        } else {
            logger.info("TO BE IMPLEMENTED transformation for type " + srcKey);
        }
    }

    void performImageTransformations(String srcBucket, String srcKey) throws Exception {

        File inFile = File.createTempFile("original", new File( srcKey).getName(), tempDir);
        FileOutputStream inFileStream = new FileOutputStream(inFile);
        logger.info("getting source from:{}/{}",srcBucket, srcKey);
        try {
            IOUtils.copy(s3Client.getObject(srcBucket, srcKey).getObjectContent(), inFileStream);
        }catch (Exception e ){
            logger.error("source does not exists or nor reachable:{}/{}",srcBucket, srcKey);
            inFileStream.close();
            return;
        }
        inFileStream.close();
        createPresentation(inFile, 1000, appProperties.output_bucket, makeOutKey(srcKey, null,".jpg"));
        createPresentation(inFile, 480, appProperties.output_bucket, makeOutKey(srcKey, "thumbnails","-md.jpg"));
        createPresentation(inFile, 100, appProperties.output_bucket, makeOutKey(srcKey, "thumbnails","-sm.jpg"));
        inFile.delete();
    }

    void createPresentation(File inFile, int maxWidth, String outBucket, String outKey) throws Exception {

        String tempFilesPrefix = "transform";

        File outFile = File.createTempFile(tempFilesPrefix, new File(outKey).getName(), tempDir);

        String[] cmd = new String[]{ "convert", inFile.getAbsolutePath(), "-resize", ""+ maxWidth + "x10000>", outFile.getAbsolutePath()};
        logger.info( "executing {}", Arrays.toString(cmd));
        Process process = Runtime.getRuntime().exec(cmd);
        Scanner errScanner = new Scanner(process.getErrorStream()).useDelimiter("\n");
        int res = process.waitFor();
        if( res != 0 ){
            while (errScanner.hasNext()){
                logger.error(errScanner.nextLine());
            }
            throw  new Exception("Could not execute: " + cmd.toString());
        }
        s3Client.putObject( outBucket,outKey, outFile);
        outFile.delete();
    }

    private String makeOutKey(String srcKey, String subdir, String suffix) {
        int dotIndex = srcKey.lastIndexOf('.');
        String k = srcKey.substring(0, dotIndex )+ suffix;
        if(StringUtils.isNotBlank(subdir)){
            int lastSlashIndex = k.lastIndexOf('/');
            k = k.substring(0,lastSlashIndex+1) + subdir + "/" + k.substring(lastSlashIndex+1);
        }
        return k;
    }

    boolean isImage(String srcKey) {
        String upper = srcKey.toUpperCase();

        return upper.endsWith("JPG") || upper.endsWith("PNG") || upper.endsWith("JPEG");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        tempDir = new File("images-temp");
        if(! tempDir.exists()){
            tempDir.mkdirs();
        }
    }
}
