package kgi.presentations.k8s.travelog.rest;

import kgi.presentations.k8s.travelog.svc.S3UploadsSignatureHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/assets/")
public class S3Api {


    @Resource
    S3UploadsSignatureHandler s3UploadsSignatureHandler;


    @RequestMapping(path = "/s3-sign-request",
            method = RequestMethod.POST)
    public void signRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        s3UploadsSignatureHandler.handleSignatureRequest(request,response);
    }


    @RequestMapping(path ="/s3-upload-success",
    method = RequestMethod.POST)
    public void uploadSuccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        s3UploadsSignatureHandler.handleUploadSuccessRequest(request,response);
    }
}
