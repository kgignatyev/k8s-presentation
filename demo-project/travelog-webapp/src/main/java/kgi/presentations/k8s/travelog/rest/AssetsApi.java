package kgi.presentations.k8s.travelog.rest;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import kgi.presentations.k8s.common.TravelConfigProperties;
import kgi.presentations.k8s.travelog.svc.S3UploadsSignatureHandler;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/assets/")
public class S3Api {

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


    String presignPutURIPrefix = "/api/assets/generate-post-url/";
    int presignPutURIPrefixLength = presignPutURIPrefix.length();

    @RequestMapping(path = "/generate-post-url/**",
            method = RequestMethod.POST)
    public Map presignPutRequest(HttpServletRequest request) {
        String objectKey = request.getRequestURI().substring(presignPutURIPrefixLength);
        String s3bucketName = travelConfigProperties.getInput_bucket();
        String contentType = contentTypeByExtension(objectKey);
        logger.debug("Pre-signing for bucket={} and key={} content-type=",s3bucketName, objectKey, contentType);
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(s3bucketName, objectKey)
                .withMethod(HttpMethod.PUT)
                .withContentType(contentType)
                .withExpiration(putRequestExpiration());
        Map res = new HashMap();
        res.put("url", s3.generatePresignedUrl(req).toString());
        return res;
    }

    private Date putRequestExpiration() {
        return DateTime.now().plusMinutes(30).toDate();
    }

    private String contentTypeByExtension(String objectKey) {
        return  MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(objectKey);
    }


}
