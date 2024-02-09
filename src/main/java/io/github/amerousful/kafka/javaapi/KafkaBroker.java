package io.github.amerousful.kafka.javaapi;

public final class KafkaBroker {

    private final io.github.amerousful.kafka.protocol.KafkaBroker wrapped;

    KafkaBroker(io.github.amerousful.kafka.protocol.KafkaBroker wrapped) {
        this.wrapped = wrapped;
    }

    public io.github.amerousful.kafka.protocol.KafkaBroker asScala() {
        return wrapped;
    }
}
