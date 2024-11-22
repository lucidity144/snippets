import java.net.InetAddress
import java.nio.ByteBuffer

fun cidrToBounds(cidr: String): Pair<String, String> {
    // Split CIDR into IP and prefix length
    val parts = cidr.split("/")
    val ip = parts[0]
    val prefixLength = parts[1].toInt()

    // Convert IP to a 32-bit integer
    val ipAsInt = ipToInt(ip)

    // Calculate the network mask
    val mask = (-1 shl (32 - prefixLength))

    // Calculate lower and upper bounds
    val lowerBound = ipAsInt and mask
    val upperBound = lowerBound or mask.inv()

    // Convert back to dotted decimal format
    val lowerBoundIp = intToIp(lowerBound)
    val upperBoundIp = intToIp(upperBound)

    return Pair(lowerBoundIp, upperBoundIp)
}

// Convert IP address from dotted decimal to 32-bit integer
fun ipToInt(ip: String): Int {
    val address = InetAddress.getByName(ip).address
    return ByteBuffer.wrap(address).int
}

// Convert 32-bit integer to dotted decimal IP address
fun intToIp(value: Int): String {
    val bytes = ByteBuffer.allocate(4).putInt(value).array()
    return InetAddress.getByAddress(bytes).hostAddress
}

// Test the function
fun main() {
    val cidr = "192.168.1.0/24"
    val (lowerBound, upperBound) = cidrToBounds(cidr)
    println("Lower Bound (Network Address): $lowerBound")
    println("Upper Bound (Broadcast Address): $upperBound")
}
