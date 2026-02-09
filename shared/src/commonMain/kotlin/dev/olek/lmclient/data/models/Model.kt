package dev.olek.lmclient.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Model(
    val id: String,
    val providerId: String,
    val name: String,
    val capabilities: List<Capability>,
    val contextLength: Long,
    val maxOutputTokens: Long? = null,
    val isActive: Boolean = false,
) {
    @Serializable
    sealed class Capability(val id: String) {
        /**
         * Represents the `temperature` capability of a language model.
         *
         * This capability is utilized to adjust the model's response randomness or creativity
         * levels.
         * Higher temperature values typically produce more diverse outputs, while lower values lead
         * to more focused and deterministic responses.
         *
         */
        @Serializable
        data object Temperature : Capability("temperature")

        /**
         * Represents the capability of tools within the LLM capability hierarchy.
         *
         * The Tools capability is typically used to indicate support for external tool usage
         * or interaction by a language model. This can include functionalities such as
         * executing specific tools or integrating with external systems. It is a predefined
         * constant within the set of capabilities available for an [Model].
         *
         * Use this capability to specify or check tool interaction abilities in a model's
         * configuration.
         */
        @Serializable
        data object Tools : Capability("tools")

        /**
         * Represents how tools calling can be configured for the LLM.
         *
         * Depending on the LLM, will configure it to generate:
         * - Automatically choose to generate either text or tool call
         * - Generate only tool calls, never text
         * - Generate only text, never tool calls
         * - Force to call one specific tool among the defined tools
         */
        @Serializable
        data object ToolChoice : Capability("toolChoice")

        /**
         * Represents an LLM capability to generate multiple independent reply choices to a single
         * prompt.
         */
        @Serializable
        data object MultipleChoices : Capability("multipleChoices")

        /**
         * Represents a large language model (LLM) capability associated with vision-based tasks.
         * This capability is typically used in models that can process, analyze, and infer insights
         * from visual data or visual representations.
         */
        @Serializable
        sealed class Vision(val visionType: String) : Capability(visionType) {
            /**
             * Represents a specific capability for handling image-related vision tasks within a
             * large language model (LLM).
             *
             * This class is a concrete implementation of the `Vision` sealed class, focusing on
             * tasks such as image analysis,
             * recognition, and interpretation. It is designed to enable models with the ability
             * to process and infer
             * insights from visual data represented as static images.
             *
             * The `Image` capability is typically used in scenarios where the model's functionality
             * includes
             * understanding image content, performing image-to-text generation, or tasks that
             * require visual comprehension.
             */
            @Serializable
            data class Image(
                val fileExtensions: List<String> = listOf("png", "jpg", "jpeg", "webp", "gif")
            ) : Vision("image")

            /**
             * Represents the video processing capability within vision-based tasks.
             *
             * This capability is used to handle video-related functionalities, including analyzing
             * and processing video data. It is part of the sealed hierarchy for vision-based
             * capabilities and provides a concrete implementation specific to video inputs.
             */
            @Serializable
            data object Video : Vision("video")
        }

        /**
         * Represents a specialized capability for audio-related functionalities in the context of
         * a LLM.
         * This capability is used in models that can involving audio processing,
         * such as transcription, audio generation, or audio-based interactions.
         */
        @Serializable
        data object Audio : Capability("audio")

        /**
         * Represents a specific language model capability associated with handling documents.
         */
        @Serializable
        data class Document(
            val fileExtensions: List<String> = listOf(
                "pdf", "docx", "csv", "txt", "html", "odt", "rtf", "epub", "json", "xlsx",
            )
        ) : Capability("document")

        /**
         * Represents the "completion" capability for Language Learning Models (LLMs). This
         * capability
         * typically encompasses the generation of text or content based on the given input context.
         * It belongs to the `LLMCapability` sealed class hierarchy and is identifiable by the
         * `completion` ID.
         *
         * This capability can be utilized within an LLM to perform tasks such as completing a
         * sentence,
         * generating suggestions, or producing content that aligns with the given input data and
         * context.
         */
        @Serializable
        data object Completion : Capability("completion")

        /**
         * Represents a structured schema capability for a language model. The schema defines
         * certain characteristics or
         * functionalities related to data interaction and encoding using specific formats.
         *
         * This class is designed to encapsulate different schema configurations that the language
         * model can support,
         * such as JSON processing.
         *
         * @property lang The language format associated with the schema.
         */
        @Serializable
        sealed class Schema(val lang: String) : Capability("$lang-schema") {
            /**
             * Represents a sealed class defining JSON schema support as a part of an AI model's
             * capability.
             * Each subtype of this class specifies a distinct level of JSON support.
             *
             * @property support Describes the type of JSON support (e.g., "basic", "standard").
             */
            @Serializable
            sealed class JSON(val support: String) : Schema("$support-json") {
                /**
                 * Represents a basic JSON schema support capability.
                 * Used to specify lightweight or fundamental JSON processing capabilities.
                 * This format primarily focuses on nested data definitions without advanced JSON
                 * Schema functionalities.
                 */
                @Serializable
                data object Basic : JSON("basic")

                /**
                 * Represents a standard JSON schema support capability, according to
                 * https://json-schema.org/.
                 * This format is a proper subset of the official JSON Schema specification.
                 */
                @Serializable
                data object Standard : JSON("standard")
            }
        }
    }
}
