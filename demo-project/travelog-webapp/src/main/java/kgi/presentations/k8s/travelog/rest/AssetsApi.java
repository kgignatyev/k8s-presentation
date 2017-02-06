package kgi.presentations.k8s.travelog.rest;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.JsonNode;
import kgi.presentations.k8s.common.TravelConfigProperties;
import kgi.presentations.k8s.travelog.svc.S3UploadsSignatureHandler;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/assets/")
public class AssetsApi {

    Logger logger = LoggerFactory.getLogger(this.getClass());



    @Resource
    TravelConfigProperties travelConfigProperties;

    @Resource
    S3UploadsSignatureHandler s3UploadsSignatureHandler;

    @Resource
    AmazonS3 s3;


    @RequestMapping(path = "/s3-sign-request",
            method = RequestMethod.POST)
    public void signRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        s3UploadsSignatureHandler.handleSignatureRequest(request, response);
    }


    @RequestMapping(path = "/s3-upload-success",
            method = RequestMethod.POST)
    public void uploadSuccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        s3UploadsSignatureHandler.handleUploadSuccessRequest(request, response);
    }

    @RequestMapping(path = "/check-present",method = RequestMethod.POST)
    public ResponseEntity checkAssetPresent(@RequestBody JsonNode r) throws MalformedURLException {
        String assetUrl = r.get("url").asText();
        URL url = new URL(assetUrl);
        String assetPath = url.getPath().substring(1);//removing first slash
        int firstSlash = assetPath.indexOf('/');
        String bucket = assetPath.substring(0,firstSlash);
        String key = assetPath.substring(firstSlash+1);
        try {
            S3Object obj = s3.getObject(bucket, key);
            return ResponseEntity.ok(obj.getObjectMetadata());
        }catch (Exception e){
            logger.error("s3 problems", e);
            return ResponseEntity.notFound().build();
        }



    }

    String presignPutURIPrefix = "/api/assets/generate-post-url/";
    int presignPutURIPrefixLength = presignPutURIPrefix.length();

    @RequestMapping(path = "/generate-post-url/**",
            method = RequestMethod.POST,produces = "application/json")
    public Map presignPutRequest(@RequestBody JsonNode b, HttpServletRequest request) {
        String assetName = request.getRequestURI().substring(presignPutURIPrefixLength);
        String s3bucketName = travelConfigProperties.getInput_bucket();
        String contentType = b.get("content-type").asText();
        String travelogId = b.get("travelogId").asText();
        String objectKey = travelogId +"/" + assetName;

        logger.debug("Pre-signing for bucket={} and key={} content-type={}",s3bucketName, objectKey, contentType);
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(s3bucketName, objectKey)
                .withMethod(HttpMethod.PUT)
                .withExpiration(putRequestExpiration());
        Map res = new HashMap();
        res.put("url", s3.generatePresignedUrl(req).toString());
        return res;
    }

    private Date putRequestExpiration() {
        return new Date(System.currentTimeMillis()+ 180000);
    }



}
