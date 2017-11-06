package org.logstash.gradle;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class IntegrationTests {

    public static IntegrationTests.BackgroundProcess runInBackground(final File executable) {
        return new IntegrationTests.BackgroundProcess(executable);
    }

    public static void waitForPort(final int port) {
        for (int i = 0; i < 60; ++i) {
            try (final Socket socket = new Socket(InetAddress.getLoopbackAddress(), port)) {
                if (socket.isConnected()) {
                    return;
                }
            } catch (final IOException ex) {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (final InterruptedException in) {
                    throw new IllegalStateException(in);
                }
            }
        }
        throw new IllegalStateException("Port did not become available in 60s");
    }

    public static final class BackgroundProcess implements AutoCloseable {

        private final CountDownLatch stop = new CountDownLatch(1);

        private final Thread background;

        BackgroundProcess(final File executable) {
            background = new Thread(() -> {
                try {
                    final Process process =
                        new ProcessBuilder(
                            executable.getAbsolutePath(),
                            "-Escript.inline=true", "-Escript.stored=true",
                            "-Escript.file=true"
                        ).start();
                    stop.await();
                    process.destroyForcibly().waitFor();
                } catch (final IOException | InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            });
            background.start();
        }

        @Override
        public void close() {
            stop.countDown();
            try {
                background.join();
            } catch (final InterruptedException ex) {
                background.interrupt();
                try {
                    background.join();
                } catch (final InterruptedException exx) {
                    throw new IllegalStateException(exx);
                }
            }
        }
    }
}
