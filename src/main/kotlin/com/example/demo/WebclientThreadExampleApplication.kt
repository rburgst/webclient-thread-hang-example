package com.example.demo

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootApplication
class WebclientThreadExampleApplication {
    @Bean
    fun commandlineRunner(webclientBuilder: WebClient.Builder) = DemoRunner(webclientBuilder)
}

fun main(args: Array<String>) {
    runApplication<WebclientThreadExampleApplication>(*args)
}


class DemoRunner(webclientBuilder: WebClient.Builder) : CommandLineRunner {
    private val client = webclientBuilder.baseUrl("http://www.google.com").build()

    /**
     * Callback used to run the bean.
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    override fun run(vararg args: String?) {
        val countDownLatch = CountDownLatch(1)
        client.get()
                .uri("/")
                .exchange()
                .flatMap { response -> response.bodyToMono(String::class.java) }
                .subscribe(
                        { body -> println("body $body") },
                        { err ->
                            System.err.println("error $err")
                            countDownLatch.countDown()
                        },
                        { countDownLatch.countDown() }
                )

        countDownLatch.await(10, TimeUnit.SECONDS)
        println("all done")
    }

}
