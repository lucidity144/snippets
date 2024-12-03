import org.apache.kafka.streams.kstream.ValueProcessorSupplier
import org.apache.kafka.streams.kstream.ValueProcessor
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream

fun main() {
    val builder = StreamsBuilder()
    val inputTopic = "input-topic"
    val outputTopic = "filtered-topic"

    val stream: KStream<String, String> = builder.stream(inputTopic)

    val filteredStream = stream.processValues { HeaderFilterProcessorSupplier("myHeader", "targetValue") }

    filteredStream.to(outputTopic)

    // Build and start your Kafka Streams application...
}

class HeaderFilterProcessorSupplier(
    private val headerKey: String,
    private val targetValue: String
) : ValueProcessorSupplier<String, String> {
    override fun get(): ValueProcessor<String, String> {
        return HeaderFilterProcessor(headerKey, targetValue)
    }
}

class HeaderFilterProcessor(
    private val headerKey: String,
    private val targetValue: String
) : ValueProcessor<String, String> {
    private lateinit var context: ProcessorContext

    override fun init(context: ProcessorContext) {
        this.context = context
    }

    override fun process(value: String): String? {
        // Access headers from the context
        val headerValue = context.headers().lastHeader(headerKey)?.value()?.let { String(it) }

        // Filter based on header value
        return if (headerValue == targetValue) value else null
    }

    override fun close() {}
}
