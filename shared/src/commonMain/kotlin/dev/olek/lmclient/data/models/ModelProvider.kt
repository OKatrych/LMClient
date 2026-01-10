package dev.olek.lmclient.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ModelProvider(val id: String, val name: String, val config: ModelProviderConfig, val isActive: Boolean) {
    @Serializable
    sealed interface ModelProviderConfig {
        val providerId: String

        fun isValid(): Boolean

        @Serializable
        data class StandardConfig(override val providerId: String, val apiUrl: String, val apiKey: String?) :
            ModelProviderConfig {
            override fun isValid() = apiUrl.isNotBlank() && apiKey?.isNotBlank() == true

            override fun toString(): String = "StandardConfig(providerId=$providerId, apiUrl=$apiUrl, apiKey=***)"
        }

        /**
         * Config for local llm providers, e.g. Llama
         */
        @Serializable
        data class LocalConfig(override val providerId: String, val apiUrl: String) : ModelProviderConfig {
            override fun isValid() = apiUrl.isNotBlank()

            override fun toString(): String = "LocalConfig(providerId=$providerId, apiUrl=$apiUrl)"
        }
    }
}
