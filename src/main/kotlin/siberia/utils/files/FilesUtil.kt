package siberia.utils.files

import io.ktor.util.date.*
import siberia.conf.AppConf
import siberia.exceptions.BadRequestException
import java.util.Base64
import kotlin.io.path.*

object FilesUtil {

    fun buildName(file: String): String {
        val currentMillis = getTimeMillis()

        val fileName = Path(file)
        return "${fileName.name}${currentMillis}.${fileName.extension}"
    }

    fun upload(base64Encoded: String, fileName: String) {
        try {
            val bytes = Base64.getDecoder().decode(base64Encoded)
            val path = Path("${AppConf.server.fileLocation}/$fileName")

            path.writeBytes(bytes)
        } catch (e: Exception) {
            throw BadRequestException("Bad file encoding")
        }
    }

    fun read(fileName: String): ByteArray? {
        return try {
            Path("${AppConf.server.fileLocation}/$fileName").readBytes()
        } catch (e: Exception) {
            null
        }
    }

    fun encodeBytes(bytes: ByteArray?): String {
        return Base64.getEncoder().encodeToString(bytes)
    }
}