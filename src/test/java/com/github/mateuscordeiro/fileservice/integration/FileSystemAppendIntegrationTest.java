package com.github.mateuscordeiro.fileservice.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.AppendRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.CreateRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.ReadRequest;

import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
public class FileSystemAppendIntegrationTest extends AbstractFileSystemIntegrationTest {
    @Test
    void append_multipleThreads_preservesAppendIsolation() throws Exception {
        final String filePath = "concurrent.txt";
        final String data = "123";
        final int threadCount = 10;

        performRpc("create", CreateRequest.builder()
                                          .path(filePath)
                                          .directory(false)
                                          .build())
                .andExpect(status().isOk());

        // Launch concurrent append requests
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    performRpc("append", AppendRequest.builder()
                                                      .path(filePath)
                                                      .data(data)
                                                      .build())
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        performRpc("read", ReadRequest.builder()
                                      .path(filePath)
                                      .offset(0)
                                      .length(data.length() * threadCount)
                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data", is(data.repeat(threadCount))));

    }
}