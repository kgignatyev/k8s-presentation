package kgi.presentations.k8s.assets_transcoder.service;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import kgi.presentations.k8s.common.TravelConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
public class SQSListenerService implements InitializingBean {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    ObjectMapper om;

    @Resource
    AmazonSQSClient sqsClient;

    @Resource
    TravelConfigProperties appProperties;

    @Resource
    TranscodingService transcodingService;

    private Thread sqsListenerThread;



    @Override
    public void afterPropertiesSet() throws Exception {

        startMessagesListener(sqsClient, appProperties.sqs_name);

    }

    private void startMessagesListener(AmazonSQSClient sqsClient, String sqs_name) {


        String queueUrl = sqsClient.getQueueUrl(sqs_name).getQueueUrl();

        final ReceiveMessageRequest msgRequest = new ReceiveMessageRequest();
        msgRequest.setQueueUrl(queueUrl);
        msgRequest.setWaitTimeSeconds(10);
        sqsListenerThread = new Thread("SQS Listener") {
            @Override
            public void run() {
                while (true) { //forever loop to receice messages
                    ReceiveMessageResult response = sqsClient.receiveMessage(msgRequest);
                    List<Message> messages = response.getMessages();
                    for (Message message : messages) {

                        String receiptHandle = message.getReceiptHandle();
                        try {
                            handleSQS(message);
                            sqsClient.deleteMessage(queueUrl, receiptHandle);
                        } catch (Exception e) {

                            logger.error("Problem handling message", e);
                        }

                    }
                }
            }
        };
        sqsListenerThread.setDaemon(true);
        sqsListenerThread.start();
        logger.info("Started SQS listener for queue:{}", queueUrl);
    }

    void handleSQS(Message message) throws Exception {
        logger.debug("Received:" + message);
        JsonNode n = om.readTree(message.getBody());
        ArrayNode records = (ArrayNode) n.get("Records");
        if( records!= null){
            for (JsonNode record : records) {
                if( isS3Message(record)){
                    handleS3Notification(record);
                }
            }
        }



    }

    void handleS3Notification(JsonNode record) throws Exception {
        if(objectCreated(record)){
            transcodingService.transcode(
                    record.get("s3").get("bucket").get("name").asText(),
                    record.get("s3").get("object").get("key").asText()
            );
        }else {
            logger.info("TO BE IMPLEMENTED handler for " + record.get("eventName").asText());
        }
    }

    private boolean objectCreated(JsonNode record) {
        return "ObjectCreated:Put".equals(record.get("eventName").asText());
    }

    boolean isS3Message(JsonNode n) {
        return   "aws:s3".equals(n.get("eventSource").asText());
    }
}
