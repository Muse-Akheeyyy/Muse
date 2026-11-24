package io.github.museakheeyyy.muse.shared.network

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
