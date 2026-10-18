package com.digitalfix.rabbitadmin.config;

public final class RabbitMQTopology {

    public static final String DIRECT_EXCHANGE = "cmd.direct";
    public static final String TOPIC_EXCHANGE = "cmd.topic";
    public static final String DEAD_LETTER_EXCHANGE = "cmd.dead.dlx";

    public static final String EMAIL_QUEUE = "q.cmd.email";
    public static final String DISPATCH_QUEUE = "q.cmd.dispatch";
    public static final String REPORT_QUEUE = "q.cmd.report";

    public static final String EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String DISPATCH_DLQ = "q.cmd.dispatch.dlq";
    public static final String REPORT_DLQ = "q.cmd.report.dlq";

    private RabbitMQTopology() {
    }
}
