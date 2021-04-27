package {{ root_package }}.server;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import {{ root_package }}.server.config.ElasticMQSettings;
import org.elasticmq.rest.sqs.SQSRestServer;
import org.elasticmq.rest.sqs.SQSRestServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableConfigurationProperties
public class {{ PrefixName }}{{ SuffixName }}ServerConfig {

    @Autowired
    ElasticMQSettings settings;

    @Bean(destroyMethod = "stopAndWait")
    public SQSRestServer sqsServer() {
        return SQSRestServerBuilder
                .withPort(settings.getPort())
                .withAWSRegion(settings.getRegion()).start();
    }

    @Bean
    public AmazonSQS sqsClient() {
        String endpoint = "http://localhost:" + sqsServer().waitUntilStarted().localAddress().getPort();
        String accessKey = "x";
        String secretKey = "x";
        return AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, settings.getRegion()))
                .build();
    }

    @PostConstruct
    public void createQueues() {
        AmazonSQS sqsClient = sqsClient();
        settings.getQueues().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(sqsClient::createQueue);
    }
}
