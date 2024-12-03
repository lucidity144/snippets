import org.apache.kafka.streams.kstream.FixedKeyProcessor
import org.apache.kafka.streams.kstream.FixedKeyProcessorSupplier
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream

fun main() {
    val builder = StreamsBuilder()
    val inputTopic = "input-topic"
    val outputTopic = "output-topic"

    // Create a stream
    val stream: KStream<String, String> = builder.stream(inputTopic)

    // Use processValues with a FixedKeyProcessorSupplier
    stream.processValues(
        FixedKeyProcessorSupplier { key ->
            // Lambda for FixedKeyProcessor
            object : FixedKeyProcessor<String, String> {
                private lateinit var context: ProcessorContext

                override fun init(context: ProcessorContext) {
                    this.context = context
                }

                override fun process(readOnlyKey: String, value: String) {
                    // Access headers
                    val headers = context.headers()
                    val headerValue = headers.lastHeader("myHeader")?.value()?.let { String(it) }

                    println("Key: $readOnlyKey, Value: $value, Header[myHeader]: $headerValue")

                    // Forward the record if a condition is met
                    if (headerValue == "targetValue") {
                        context.forward(readOnlyKey, "Processed: $value")
                    }
                }

                override fun close() {}
            }
        }
    ).to(outputTopic)

    // Start your Kafka Streams application...
}
