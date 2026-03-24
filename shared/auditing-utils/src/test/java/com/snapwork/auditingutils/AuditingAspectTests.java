package com.snapwork.auditingutils;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditingAspectTests {

   /* private StubEventPublisher eventPublisher;
    private TestController controllerProxy;

    @BeforeEach
    void setUp() {
        eventPublisher = new StubEventPublisher();
        AuditingAspect auditingAspect = new AuditingAspect();
        auditingAspect.eventPublisher = eventPublisher;
        auditingAspect.objectMapper = new ObjectMapper();
        auditingAspect.serviceName = "test-service";
        AspectJProxyFactory aspectJProxyFactory = new AspectJProxyFactory(new TestController());
        aspectJProxyFactory.addAspect(auditingAspect);
        DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();
        AopProxy aopProxy = proxyFactory.createAopProxy(aspectJProxyFactory);
        controllerProxy = (TestController) aopProxy.getProxy();
    }

//    @Test
    void should_execute_aspect_on_success() {

        controllerProxy.doSth(Map.of("request", "test-request"));

        var events = eventPublisher.getEvents();
        assertThat(events).hasSize(1);
        StubEventPublisher.TestEvent testEventRecord = events.get(0);
        assertThat(testEventRecord.key()).isEqualTo("");
        assertThat(testEventRecord.topic()).isEqualTo(KafkaTopic.AUDIT_EVENTS.getTopicName());
        Event event = testEventRecord.event();
        AuditData auditData = (AuditData) event.getData();
        assertThat(event.getSource()).isEqualTo("test-service");
        assertThat(event.getType()).isEqualTo("com.auditing.http_request");
        assertThat(event.getSource()).isEqualTo("test-service");
        assertThat(auditData.request().getRequest()).isEqualTo(Map.of("request", "test-request"));
        assertThat(auditData.request().getResponse()).isEqualTo(Map.of("response", "test-response"));
    }

//    @Test
    void should_execute_aspect_on_exception() {
        try {
            controllerProxy.doSthOnException(Map.of("request", "test-request"));
        } catch (Exception e) {
            assertThat(e).hasMessage("testing for exception");
        }

        var events = eventPublisher.getEvents();
        assertThat(events).hasSize(1);
        StubEventPublisher.TestEvent testEventRecord = events.get(0);
        assertThat(testEventRecord.key()).isEqualTo("");
        assertThat(testEventRecord.topic()).isEqualTo(KafkaTopic.AUDIT_EVENTS.getTopicName());
        Event event = testEventRecord.event();
        AuditData auditData = (AuditData) event.getData();
        assertThat(event.getSource()).isEqualTo("test-service");
        assertThat(event.getType()).isEqualTo("com.auditing.http_request");
        assertThat(event.getSource()).isEqualTo("test-service");
        assertThat(auditData.request().getRequest()).isEqualTo(Map.of("request", "test-request"));
        var httpRequestData = auditData.request();
        assertThat(httpRequestData.getErrorMessage()).isEqualTo("testing for exception");
    }
}

class StubEventPublisher implements EventPublisher {

    private final List<TestEvent> events = new ArrayList<>();

    @Override
    public  void publish(String topic, String key, Event event) {
        events.add(new TestEvent(topic, key, event));
    }

    public record TestEvent(String topic, String key, Event event) {
    }

    public List<TestEvent> getEvents() {
        return events;
    }*/
}