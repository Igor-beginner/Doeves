package md.brainet.doeves.mail;

public record MessageRequest(
        String receiver,
        String topic,
        String content
) {
}
