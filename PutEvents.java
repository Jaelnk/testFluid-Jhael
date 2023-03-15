package com.cobis.ach.test.intg;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.EventBridgeException;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;
import java.util.ArrayList;
import java.util.List;
/**
 * To run this Java V2 code example, ensure that you have setup your development environment, including your credentials.
 *
 * For information, see this documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */
@Log4j2
public class PutEvents {

    public static void main(String[] args) throws InterruptedException {

        ClientOverrideConfiguration conf=  ClientOverrideConfiguration.builder()
                .addExecutionInterceptor(new TracingInterceptor())
                .build();

        final String USAGE =
                "To run this example, supply two resources, identified by Amazon Resource Name (ARN), which the event primarily concerns. " +
                        "Any number, including zero, may be present. \n" +
                        "For example: PutEvents <resourceArn> <resourceArn2>\n";

//        if (args.length != 2) {
//             log.info(USAGE);
//            System.exit(1);
//        }

//        String resourceArn = args[0];
//        String resourceArn2 = args[1];
        String resourceArn = "res0";
        String resourceArn2 = "res1";

        Region region = Region.US_EAST_1;
        EventBridgeClient eventBrClient = EventBridgeClient.builder()
                .region(region)
                .overrideConfiguration(conf)
                .build();
        AWSXRay.beginSegment("EventBridge");
        putEBEvents(eventBrClient, resourceArn, resourceArn2);
        AWSXRay.endSegment();
        eventBrClient.close();
        //Thread.sleep(60000);
    }

    public static void putEBEvents(EventBridgeClient eventBrClient, String resourceArn, String resourceArn2 ) {

        try {
            // Populate a List with the resource ARN values
            List<String> resources = new ArrayList<String>();
            resources.add(resourceArn);
            resources.add(resourceArn2);

            PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder()
                    .resources(resources)
                    .source("source1")
                    .traceHeader("Root=1-5759e988-bd862e3fe1be46a994272793;Sampled=2")
                    .detailType("myDetailType")
                    .detail("{ \"key1\": \"value1\", \"key2\": \"value2\" }")
                    .build();

            // Add the PutEventsRequestEntry to a list
            List<PutEventsRequestEntry> list = new ArrayList<PutEventsRequestEntry>();
            list.add(reqEntry);

            PutEventsRequest eventsRequest = PutEventsRequest.builder()
                    .entries(reqEntry)
                    .build();

            PutEventsResponse result = eventBrClient.putEvents(eventsRequest);

            for (PutEventsResultEntry resultEntry : result.entries()) {
                if (resultEntry.eventId() != null) {
                     log.info("Event Id: " + resultEntry.eventId());
                } else {
                     log.info("Injection failed with Error Code: " + resultEntry.errorCode());
                }
            }

        } catch (EventBridgeException e) {
			log.info("Exception: " + e);

            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
