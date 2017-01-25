package kgi.presentations.k8s.travelog.svc;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.BinaryUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java Server-Side Example for Fine Uploader S3.
 * Maintained by Widen Enterprises.
 * <p>
 * This example:
 * - handles non-CORS environments
 * - handles delete file requests via the DELETE method
 * - signs policy documents (simple uploads) and REST requests
 * (chunked/multipart uploads)
 * - handles both version 2 and version 4 signatures
 * <p>
 * Requirements:
 * - Java 1.5 or newer
 * - Google GSon
 * - Amazon Java SDK (only if utilizing the delete file feature)
 * <p>
 * If you need to install the AWS SDK, see http://docs.aws.amazon.com/aws-sdk-php-2/guide/latest/installation.html.
 */
public class S3UploadsSignatureHandler {
    // This assumes your secret key is available in an environment variable.
    // It is needed to sign policy documents.
    final String AWS_SECRET_KEY;

    // You will need to use your own public key here.
    final String AWS_PUBLIC_KEY;
    private final ObjectMapper om;

    public S3UploadsSignatureHandler(String AWS_PUBLIC_KEY, String AWS_SECRET_KEY, ObjectMapper om) {
        this.AWS_SECRET_KEY = AWS_SECRET_KEY;
        this.AWS_PUBLIC_KEY = AWS_PUBLIC_KEY;
        this.om = om;
    }


    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String key = req.getParameter("key");
        String bucket = req.getParameter("bucket");

        resp.setStatus(200);

        AWSCredentials myCredentials = new BasicAWSCredentials(AWS_PUBLIC_KEY, AWS_SECRET_KEY);
        AmazonS3 s3Client = new AmazonS3Client(myCredentials);
        s3Client.deleteObject(bucket, key);
    }

    // Called by the main POST request handler if Fine Uploader has asked for an item to be signed.  The item may be a
    // policy document or a string that represents multipart upload request headers.
    public void handleSignatureRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(200);


        ObjectNode contentJson = (ObjectNode) om.readTree(req.getReader());
        ObjectNode jsonObject = contentJson;

        if (req.getQueryString() != null && req.getQueryString().contains("v4=true")) {
            handleV4SignatureRequest(jsonObject, contentJson, req, resp);
        } else {
            handleV2SignatureRequest(jsonObject, contentJson, req, resp);
        }

        resp.setStatus(200);
    }

    private void handleV2SignatureRequest(ObjectNode payload, ObjectNode contentJson, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String signature;
        JsonNode headers = payload.get("headers");
        ObjectNode response = om.createObjectNode();

        try {
            // If this is not a multipart upload-related request, Fine Uploader will send a policy document
            // as the value of a "policy" property in the request.  In that case, we must base-64 encode
            // the policy document and then sign it. The will include the base-64 encoded policy and the signed policy document.
            if (headers == null) {
                String base64Policy = base64EncodePolicy(contentJson);
                signature = sign(base64Policy);

                // Validate the policy document to ensure the client hasn't tampered with it.
                // If it has been tampered with, set this property on the response and set the status to a non-200 value.
//                response.addProperty("invalid", true);

                response.put("policy", base64Policy);
            }

            // If this is a request to sign a multipart upload-related request, we only need to sign the headers,
            // which are passed as the value of a "headers" property from Fine Uploader.  In this case,
            // we only need to return the signed value.
            else {
                signature = sign(headers.asText());
            }

            response.put("signature", signature);
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            resp.setStatus(500);
        }
    }

    private void handleV4SignatureRequest(ObjectNode payload, ObjectNode contentJson, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String signature = null;
        JsonNode headers = payload.get("headers");
        ObjectNode response = om.createObjectNode();

        try {
            // If this is not a multipart upload-related request, Fine Uploader will send a policy document
            // as the value of a "policy" property in the request.  In that case, we must base-64 encode
            // the policy document and then sign it. The will include the base-64 encoded policy and the signed policy document.
            if (headers == null) {
                String base64Policy = base64EncodePolicy(contentJson);
                ArrayNode conditions = (ArrayNode) payload.get("conditions");
                String credentialCondition = null;
                for (int i = 0; i < conditions.size(); i++) {
                    JsonNode condition = conditions.get(i);
                    JsonNode value = condition.get("x-amz-credential");
                    if (value != null) {
                        credentialCondition = value.asText();
                        break;
                    }
                }

                // Validate the policy document to ensure the client hasn't tampered with it.
                // If it has been tampered with, set this property on the response and set the status to a non-200 value.
//                response.addProperty("invalid", true);


                Pattern pattern = Pattern.compile(".+\\/(.+)\\/(.+)\\/s3\\/aws4_request");
                Matcher matcher = pattern.matcher(credentialCondition);
                matcher.matches();
                signature = getV4Signature(matcher.group(1), matcher.group(2), base64Policy);

                response.put("policy", base64Policy);
            }

            // If this is a request to sign a multipart upload-related request, we only need to sign the headers,
            // which are passed as the value of a "headers" property from Fine Uploader.  In this case,
            // we only need to return the signed value.
            else {
                Pattern pattern = Pattern.compile(".+\\n.+\\n(\\d+)\\/(.+)\\/s3\\/aws4_request\\n(.+)", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(headers.asText());
                matcher.matches();
                String canonicalRequest = matcher.group(3);
                String hashedCanonicalRequest = hash256(canonicalRequest);
                String stringToSign = headers.asText().replaceAll("(?s)(.+s3\\/aws4_request\\n).+", "$1" + hashedCanonicalRequest);

                // Validate the policy document to ensure the client hasn't tampered with it.
                // If it has been tampered with, set this property on the response and set the status to a non-200 value.
//                response.addProperty("invalid", true);

                signature = getV4Signature(matcher.group(1), matcher.group(2), stringToSign);
            }

            response.put("signature", signature);
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            resp.setStatus(500);
        }
    }

    // Called by the main POST request handler if Fine Uploader has indicated that the file has been
    // successfully sent to S3.  You have the opportunity here to examine the file in S3 and "fail" the upload
    // if something in not correct.
    public void handleUploadSuccessRequest(HttpServletRequest req, HttpServletResponse resp) {
        String key = req.getParameter("key");
        String uuid = req.getParameter("uuid");
        String bucket = req.getParameter("bucket");
        String name = req.getParameter("name");

        resp.setStatus(200);

        System.out.println(String.format("Upload successfully sent to S3!  Bucket: %s, Key: %s, UUID: %s, Filename: %s",
                bucket, key, uuid, name));
    }

    private String getV4Signature(String date, String region, String stringToSign) throws Exception {
        byte[] kSecret = ("AWS4" + AWS_SECRET_KEY).getBytes("UTF8");
        byte[] kDate = sha256Encode(date, kSecret);
        byte[] kRegion = sha256Encode(region, kDate);
        byte[] kService = sha256Encode("s3", kRegion);
        byte[] kSigning = sha256Encode("aws4_request", kService);
        byte[] kSignature = sha256Encode(stringToSign, kSigning);

        return Hex.encodeHexString(kSignature);
    }

    private byte[] sha256Encode(String data, byte[] key) throws Exception {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    private String hash256(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data.getBytes());
        return bytesToHex(md.digest());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    private String base64EncodePolicy(JsonNode jsonElement) throws UnsupportedEncodingException {
        String policyJsonStr = jsonElement.toString();
        String base64Encoded = BinaryUtils.toBase64(policyJsonStr.getBytes("UTF-8"));
        return base64Encoded;
    }

    private String sign(String toSign) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA1");
        hmac.init(new SecretKeySpec(AWS_SECRET_KEY.getBytes("UTF-8"), "HmacSHA1"));
        String signature = BinaryUtils.toBase64(hmac.doFinal(toSign.getBytes("UTF-8")));

        return signature;
    }
}

