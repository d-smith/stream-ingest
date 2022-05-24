package ds.streamingest.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

@Service
@Log4j2
public class LambdaCaller {
    final String functionName="formatter-dev-formatter";
    final private LambdaClient lambdaClient;

    public LambdaCaller() {

        Region region = Region.US_WEST_2;
        lambdaClient = LambdaClient
                .builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    public String invokeLambda(String s) {
        var start = System.currentTimeMillis();
        SdkBytes payload = SdkBytes.fromUtf8String(s);
        InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .payload(payload)
                .build();

        var res = lambdaClient.invoke(request);
        String value = res.payload().asUtf8String();
        var stop = System.currentTimeMillis();
        log.info("Lambda invoke duration {} ms", stop - start);
        return value;
    }
}
